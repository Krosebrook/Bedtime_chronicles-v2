import type { Express } from "express";
import { StoryRequestSchema } from "../validation";
import { getStorySystemPrompt, getStoryUserPrompt, getPartCount, getWordCount, STORY_RESPONSE_SCHEMA } from "../prompts";
import { classifyError, createErrorResponse } from "../utils";
import { IdempotencyCache } from "../idempotency";
import { aiRouter, idempotencyCache } from "./context";
import { rateLimited, sendRouteError } from "./helpers";

export function registerStoryRoutes(app: Express): void {
  app.post("/api/generate-story", rateLimited(), async (req, res) => {
    const parsed = StoryRequestSchema.safeParse(req.body);
    if (!parsed.success) {
      return res.status(400).json({ error: parsed.error.issues[0]?.message || "Invalid request" });
    }

    const { heroName, heroTitle, heroPower, heroDescription, duration, mode, madlibWords, soundscape, setting, tone, childName, sidekick, problem, musicType } = parsed.data;

    // Bind the idempotency key to the caller so identical bodies from different
    // users never collide on a shared cached generation.
    const idempotencyKey = IdempotencyCache.keyFromBody({ ...parsed.data, _uid: req.user?.uid ?? 'anon' });
    const cached = idempotencyCache.get(idempotencyKey);
    if (cached) {
      req.log?.info('story request deduplicated (idempotency hit)');
      const result = await cached;
      return res.json(result);
    }

    const generationPromise = (async () => {
      const partCount = getPartCount(duration);
      const wordCount = getWordCount(duration);

      const systemPrompt = getStorySystemPrompt(mode, partCount);
      const userPrompt = getStoryUserPrompt(mode, heroName, heroTitle, heroPower, heroDescription, wordCount, partCount, madlibWords, soundscape, setting, tone, childName, sidekick, problem);

      const storyPromise = aiRouter.generateText("story", {
        systemPrompt,
        userPrompt,
        temperature: mode === "sleep" ? 0.7 : 0.9,
        maxTokens: 8192,
        jsonMode: true,
        responseSchema: STORY_RESPONSE_SCHEMA,
        timeoutMs: 60_000,
        requestId: req.requestId,
      });

      let lyriaPromise: Promise<string | undefined> = Promise.resolve(undefined);
      if (musicType === "lyria-clip" || musicType === "lyria-pro") {
        lyriaPromise = (async () => {
          try {
            const musicPrompt = `Generate a very gentle, soothing, slow-tempo bedtime lullaby for a child. 
Theme: ${heroName}, the ${heroTitle}. 
Setting: ${setting || "a magical calm galaxy under starlight"}. 
Tone: serene, peaceful, sleep-inducing.
Instrumentation: Soft celestial synth, warm ambient pads, gentle music box chime, minimal piano keys. No loud beats, percussion, vocals, or intense climaxes to ensure baby falls asleep gracefully.`;

            const lyriaModel = musicType === "lyria-clip" ? "lyria-3-clip-preview" : "lyria-3-pro-preview";
            
            // Get client
            const { GoogleGenAI } = await import("@google/genai");
            const baseUrl = process.env.AI_INTEGRATIONS_GEMINI_BASE_URL;
            const ai = new GoogleGenAI({
              apiKey: process.env.AI_INTEGRATIONS_GEMINI_API_KEY,
              httpOptions: baseUrl ? { baseUrl } : undefined,
            });

            const musicRes = await ai.models.generateContent({
              model: lyriaModel,
              contents: [{ role: "user", parts: [{ text: musicPrompt }] }],
              config: {
                responseModalities: ["AUDIO"]
              }
            });

            const candidate = musicRes.candidates?.[0];
            const audioPart = candidate?.content?.parts?.find(p => p.inlineData && p.inlineData.mimeType?.startsWith("audio/"));
            if (audioPart?.inlineData?.data) {
              const base64Audio = audioPart.inlineData.data;
              const audioBuffer = Buffer.from(base64Audio, "base64");

              const fs = await import("fs");
              const path = await import("path");
              const generatedMusicDir = path.resolve("assets", "generated_music");
              if (!fs.existsSync(generatedMusicDir)) {
                fs.mkdirSync(generatedMusicDir, { recursive: true });
              }

              const musicId = `lyria_${Date.now()}_${Math.random().toString(36).substring(2, 9)}`;
              const fileName = `${musicId}.mp3`;
              const filePath = path.join(generatedMusicDir, fileName);
              fs.writeFileSync(filePath, audioBuffer);

              return `/assets/generated_music/${fileName}`;
            } else {
              req.log?.warn?.("Lyria returned no audio inlineData");
            }
          } catch (err) {
            req.log?.error?.({ err }, "Lyria music generation failed, fallback to classic");
          }
          return undefined;
        })();
      }

      const [aiResponse, musicUrl] = await Promise.all([storyPromise, lyriaPromise]);

      if (!aiResponse.parsedJson) {
        throw new Error("Invalid story response");
      }

      req.log?.info({ provider: aiResponse.provider, model: aiResponse.model }, 'story generated');

      const story = aiResponse.parsedJson as Record<string, unknown>;

      if (!story.parts || !Array.isArray(story.parts)) {
        throw new Error("Invalid story structure");
      }

      story.parts = (story.parts as Array<{ text?: string; choices?: string[] }>).map((part, i) => ({
        text: part.text || "",
        choices: mode === "sleep" ? undefined : (part.choices || undefined),
        partIndex: i,
      }));

      if ((story.parts as unknown[]).length > 0 && mode !== "sleep") {
        delete (story.parts as Record<string, unknown>[])[(story.parts as unknown[]).length - 1].choices;
      }

      if (musicUrl) {
        story.musicUrl = musicUrl;
      }

      return story;
    })();

    idempotencyCache.set(idempotencyKey, generationPromise);

    try {
      const story = await generationPromise;
      res.json(story);
    } catch (error: unknown) {
      idempotencyCache.delete(idempotencyKey);
      sendRouteError(req, res, error, 'story generation failed', 'Failed to generate story');
    }
  });

  app.post("/api/generate-story-stream", rateLimited(), async (req, res) => {
    const parsed = StoryRequestSchema.safeParse(req.body);
    if (!parsed.success) {
      return res.status(400).json({ error: parsed.error.issues[0]?.message || "Invalid request" });
    }

    const { heroName, heroTitle, heroPower, heroDescription, duration, mode, madlibWords, soundscape, setting, tone, childName, sidekick, problem } = parsed.data;

    try {
      const partCount = getPartCount(duration);
      const wordCount = getWordCount(duration);

      const systemPrompt = getStorySystemPrompt(mode, partCount);
      const userPrompt = getStoryUserPrompt(mode, heroName, heroTitle, heroPower, heroDescription, wordCount, partCount, madlibWords, soundscape, setting, tone, childName, sidekick, problem);

      res.setHeader("Content-Type", "text/event-stream");
      res.setHeader("Cache-Control", "no-cache");
      res.setHeader("Connection", "keep-alive");

      const stream = aiRouter.generateTextStream("story", {
        systemPrompt,
        userPrompt,
        temperature: mode === "sleep" ? 0.7 : 0.9,
        maxTokens: 8192,
      });

      let providerInfo = "";
      for await (const chunk of stream) {
        if (!providerInfo) {
          providerInfo = `${chunk.provider}`;
          res.write(`data: ${JSON.stringify({ type: "provider", provider: chunk.provider, model: chunk.model })}\n\n`);
        }
        if (chunk.done) {
          res.write(`data: ${JSON.stringify({ type: "done" })}\n\n`);
        } else {
          res.write(`data: ${JSON.stringify({ type: "chunk", text: chunk.text })}\n\n`);
        }
      }

      req.log?.info({ provider: providerInfo }, 'story stream completed');
      res.end();
    } catch (error: unknown) {
      // SSE: once headers are sent the error must go down the open stream,
      // so this handler cannot use sendRouteError.
      req.log?.error({ err: error }, 'story streaming failed');
      const kind = classifyError(error);
      if (res.headersSent) {
        res.write(`data: ${JSON.stringify({ type: "error", error: "Failed to generate story", retryable: kind === 'transient' })}\n\n`);
        res.end();
      } else {
        res.status(kind === 'transient' ? 503 : 500).json(createErrorResponse('Failed to generate story', kind));
      }
    }
  });

  app.post("/api/sync/interactions", rateLimited(), async (req, res) => {
    try {
      const { interactions } = req.body;
      if (!interactions || !Array.isArray(interactions)) {
        return res.status(400).json({ error: "Missing or invalid interactions array" });
      }

      req.log?.info({ count: interactions.length }, "Syncing interactions batch from client");

      // Log/validate and process each synced interaction
      const validated = interactions.map((inter: any) => {
        req.log?.info(
          { type: inter.type, storyId: inter.storyId, timestamp: inter.timestamp },
          "Synced offline interaction"
        );
        return {
          id: inter.id || `act_${Math.random().toString(36).substring(2, 11)}`,
          type: inter.type,
          storyId: inter.storyId,
          timestamp: inter.timestamp || Date.now(),
          status: "processed"
        };
      });

      return res.json({
        success: true,
        syncedCount: validated.length,
        results: validated
      });
    } catch (error: unknown) {
      sendRouteError(req, res, error, "interactions sync failed", "Failed to sync offline interactions");
    }
  });
}
