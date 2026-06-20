/**
 * Secure Gemini API Gateway Proxy Stub (Cloudflare Workers / Node.js Express compliant)
 * This gateway abstracts the GEMINI_API_KEY from client-side bundle exposure,
 * rate limits bedtime requests to prevent billing abuse, and sanitizes payloads.
 */

const express = require('express');
const app = express();
const PORT = process.env.PORT || 3000;

app.use(express.json());

// In-memory token-bucket rate limiter map for IP addresses
const RATE_LIMIT_CACHE = new Map();
const RATE_LIMIT_WINDOW_MS = 60000; // 1 minute
const MAX_REQUESTS_PER_MINUTE = 5; // Sleep safety threshold for children's app

function checkRateLimit(ip) {
    const now = Date.now();
    const clientData = RATE_LIMIT_CACHE.get(ip) || { tokens: MAX_REQUESTS_PER_MINUTE, lastRefill: now };
    
    // Calculate token refill
    const elapsed = now - clientData.lastRefill;
    const refilledTokens = clientData.tokens + (elapsed / RATE_LIMIT_WINDOW_MS) * MAX_REQUESTS_PER_MINUTE;
    clientData.tokens = Math.min(MAX_REQUESTS_PER_MINUTE, refilledTokens);
    clientData.lastRefill = now;

    if (clientData.tokens >= 1) {
        clientData.tokens -= 1;
        RATE_LIMIT_CACHE.set(ip, clientData);
        return { allowed: true, remaining: Math.floor(clientData.tokens) };
    }

    RATE_LIMIT_CACHE.set(ip, clientData);
    return { allowed: false, remaining: 0 };
}

// Security sanitization middleware
function sanitizePayload(req, res, next) {
    const bodyString = JSON.stringify(req.body);
    // Block standard SQL injection patterns or suspicious prompt instructions (jailbreak attempts)
    const dangerousPatterns = [
        /ignore previous instructions/i,
        /system prompt/i,
        /SELECT \* FROM/i,
        /DROP TABLE/i
    ];

    for (const pattern of dangerousPatterns) {
        if (pattern.test(bodyString)) {
            return res.status(400).json({
                error: {
                    message: "Payload blocked: Safety policy violation.",
                    code: "SAFETY_BLOCK"
                }
            });
        }
    }
    next();
}

// Proxied Endpoint for Gemini Story Generation
app.post('/api/v1/story/generate', sanitizePayload, async (req, res) => {
    const clientIp = req.ip || req.headers['x-forwarded-for'] || '127.0.0.1';
    
    // Rate Limiting check
    const rateLimit = checkRateLimit(clientIp);
    res.setHeader('X-RateLimit-Limit', MAX_REQUESTS_PER_MINUTE);
    res.setHeader('X-RateLimit-Remaining', rateLimit.remaining);

    if (!rateLimit.allowed) {
        return res.status(429).json({
            error: {
                message: "Too many cozy adventures requested. Please wait a moment before creating another bedtime story.",
                code: "RATE_LIMIT_EXCEEDED"
            }
        });
    }

    const geminiKey = process.env.GEMINI_API_KEY;
    if (!geminiKey) {
        return res.status(500).json({
            error: {
                message: "Gateway configuration error: Target api key is unavailable.",
                code: "CONFIG_ERROR"
            }
        });
    }

    try {
        // Forward structured request directly to the model endpoint (gemini-3.5-flash)
        const geminiEndpoint = `https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=${geminiKey}`;
        
        // Native Node fetch (Node 18+) or axios
        const response = await fetch(geminiEndpoint, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(req.body)
        });

        const data = await response.json();
        
        if (!response.ok) {
            return res.status(response.status).json({
                error: {
                    message: "Target Gemini execution failed.",
                    details: data
                }
            });
        }

        return res.json(data);
    } catch (e) {
        return res.status(500).json({
            error: {
                message: "Internal gateway exception during proxy transport.",
                details: e.message
            }
        });
    }
});

// Proxied Endpoint for Imagen Visual Assets
app.post('/api/v1/images/generate', sanitizePayload, async (req, res) => {
    const clientIp = req.ip || req.headers['x-forwarded-for'] || '127.0.0.1';
    const rateLimit = checkRateLimit(clientIp);

    if (!rateLimit.allowed) {
        return res.status(429).json({
            error: {
                message: "Too many illustrations requested. Please save your energy for dreamland.",
                code: "RATE_LIMIT_EXCEEDED"
            }
        });
    }

    const geminiKey = process.env.GEMINI_API_KEY;
    if (!geminiKey) {
        return res.status(500).json({
            error: {
                message: "Gateway configuration error: Target api key is unavailable.",
                code: "CONFIG_ERROR"
            }
        });
    }

    try {
        const imagenEndpoint = `https://generativelanguage.googleapis.com/v1beta/models/imagen-3.0-generate-002:generateImages?key=${geminiKey}`;
        
        const response = await fetch(imagenEndpoint, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(req.body)
        });

        const data = await response.json();
        
        if (!response.ok) {
            return res.status(response.status).json({
                error: {
                    message: "Target Imagen execution failed.",
                    details: data
                }
            });
        }

        return res.json(data);
    } catch (e) {
        return res.status(500).json({
            error: {
                message: "Internal gateway exception during proxy transport.",
                details: e.message
            }
        });
    }
});

app.listen(PORT, () => {
    console.log(`Secure Bedtime Gate proxy running on port ${PORT}`);
});
