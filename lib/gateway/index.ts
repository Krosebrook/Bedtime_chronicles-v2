import express, { Request, Response, NextFunction } from 'express';
import rateLimit from 'express-rate-limit';
import jwt from 'jsonwebtoken';
import jwksRsa from 'jwks-rsa';

/**
 * PRODUCTION-GRADE SECURE AI GATEWAY PROXY
 * Language: TypeScript / Node.js
 * 
 * Provides:
 *  1. JWT Validation via JWKS (Firebase Auth or Auth0 compliant)
 *  2. Express Rate-limiting using Sliding Window (express-rate-limit)
 *  3. Child Safety Core / Prompt Filtering
 *  4. Secure abstraction of GEMINI_API_KEY from Android Clients
 */

const app = express();
const PORT = process.env.PORT || 3000;

app.use(express.json());

// ==========================================
// 1. JWT AUTHENTICATION MIDDLEWARE
// ==========================================
// Configured to dynamically verify JWT signatures using JWKS (JSON Web Key Sets).
// Typically point this to: https://www.googleapis.com/robot/v1/metadata/jwk/securetoken@system.gserviceaccount.com (Firebase Auth)
const JWKS_URI = process.env.JWKS_URI || 'https://your-auth-domain.auth0.com/.well-known/jwks.json';
const AUTH_ISSUER = process.env.AUTH_ISSUER || 'https://securetoken.google.com/infinity-heroes-bedtime';
const AUTH_AUDIENCE = process.env.AUTH_AUDIENCE || 'infinity-heroes-bedtime';

const jwksClientInstance = jwksRsa({
    cache: true,
    rateLimit: true,
    jwksRequestsPerMinute: 10,
    jwksUri: JWKS_URI
});

/**
 * Retrieves the cryptographic public key to verify incoming JWT signature
 */
function getKey(header: jwt.JwtHeader, callback: jwt.SigningKeyCallback) {
    if (!header.kid) {
        return callback(new Error('JWT is missing key ID (kid) header claim.'));
    }
    jwksClientInstance.getSigningKey(header.kid, (err, key) => {
        if (err || !key) {
            return callback(err || new Error('JWKS public key resolution error.'));
        }
        const publicKey = key.getPublicKey();
        callback(null, publicKey);
    });
}

/**
 * Authenticates client requests using bearer tokens
 */
function authenticateJWT(req: Request, res: Response, next: NextFunction) {
    const authHeader = req.headers.authorization;
    
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
        // Safe cozy fallback messages for child context
        return res.status(401).json({
            error: {
                message: "A cozy credentials session token is required to start your adventure.",
                code: "UNAUTHORIZED"
            }
        });
    }

    const token = authHeader.split(' ')[1];

    // Development Bypass Flag
    if (process.env.NODE_ENV === 'development' && token === 'dev-cozy-storytime-token-2026') {
        (req as any).user = { uid: 'dev-kid-user', name: "Finn-Dev" };
        return next();
    }

    jwt.verify(token, getKey, {
        issuer: AUTH_ISSUER,
        audience: AUTH_AUDIENCE,
        algorithms: ['RS256']
    }, (err, decoded) => {
        if (err || !decoded) {
            return res.status(403).json({
                error: {
                    message: "Bedtime session has expired. Please restart your story application.",
                    code: "FORBIDDEN",
                    details: err?.message
                }
            });
        }
        
        (req as any).user = decoded;
        next();
    });
}

// ==========================================
// 2. RATE LIMITING MIDDLEWARE
// ==========================================
// Protects upstream Gemini / Imagen endpoints from high billing expenditure
// and prevents kids from looping API generate calls infinitely during sleep.
const storyRateLimiter = rateLimit({
    windowMs: 15 * 60 * 1000, // 15 minutes
    max: 10, // Max 10 custom bedtime stories per 15 mins
    standardHeaders: true,
    legacyHeaders: false,
    message: {
        error: {
            message: "Too many cozy adventures requested. Take a gentle breath and retry in a few moments.",
            code: "RATE_LIMIT_EXCEEDED"
        }
    },
    keyGenerator: (req) => {
        // Authenticated ID or client IP
        return (req as any).user?.sub || req.ip || 'anonymous';
    }
});

const imageRateLimiter = rateLimit({
    windowMs: 60 * 60 * 1000, // 1 hour
    max: 5, // Tight restriction on Imagen calls (high billing cost)
    standardHeaders: true,
    legacyHeaders: false,
    message: {
        error: {
            message: "We are sketching beautiful imagery as fast as we can! Please save your illustrations request for sweet dreamland.",
            code: "RATE_LIMIT_EXCEEDED"
        }
    },
    keyGenerator: (req) => {
        return (req as any).user?.sub || req.ip || 'anonymous';
    }
});

// ==========================================
// 3. CHILD COMPLIANT SAFETY FILTER
// ==========================================
function sanitizeAndGuardPrompts(req: Request, res: Response, next: NextFunction) {
    const payloadStr = JSON.stringify(req.body);
    
    // Strict regular expressions to detect child-unfriendly topics or prompt injections
    const dangerousPatterns = [
        /ignore previous instructions/i,
        /system prompt/i,
        /SELECT \* FROM/i,
        /horror /i,
        /scary/i,
        /violence/i,
        /nightmare/i,
        /weapons/i
    ];

    for (const pattern of dangerousPatterns) {
        if (pattern.test(payloadStr)) {
            return res.status(400).json({
                error: {
                    message: "Safety Guard Alert: Let's focus on magical, calming and peaceful happy stories!",
                    code: "SAFETY_FILTER_VIOLATION"
                }
            });
        }
    }
    
    next();
}

/**
 * Standardizes upstream API responses and maps complex error statuses 
 * into warm, parent-friendly and child-appropriate JSON logs.
 */
function mapUpstreamError(status: number, data: any): { status: number; body: any } {
    let clientCode = "UNKNOWN_ERROR";
    let clientMessage = "Something went a bit astray in the stardust. Let's take a deep, calm breath and try again.";

    const errorObj = data?.error || (Array.isArray(data) ? data[0]?.error : null) || data;
    const rawMessage = errorObj?.message || "";
    const rawStatus = errorObj?.status || "";
    
    // Attempt parsing Google rpc.ErrorInfo reason
    let errorReason = "";
    if (errorObj && Array.isArray(errorObj.details)) {
        const errorInfo = errorObj.details.find((d: any) => d["@type"]?.includes("ErrorInfo"));
        if (errorInfo) {
            errorReason = errorInfo.reason || "";
        }
    }

    if (status === 400) {
        if (rawMessage.includes("API key not valid") || errorReason === "API_KEY_INVALID") {
            clientCode = "INVALID_API_KEY";
            clientMessage = "The celestial access key is incorrect. Ask parents to check development API credentials.";
        } else if (rawMessage.includes("location") || rawStatus === "FAILED_PRECONDITION" || rawMessage.includes("not supported")) {
            clientCode = "LOCATION_NOT_SUPPORTED";
            clientMessage = "Gemini bedtime chronicles are currently resting or unavailable in your region. Enjoy our offline library books!";
        } else {
            clientCode = "INVALID_REQUEST";
            clientMessage = "Our storybook recipe has invalid options or formatting. Let's refine your companion keywords!";
        }
    } else if (status === 403) {
        if (rawMessage.includes("permission") || rawStatus === "PERMISSION_DENIED" || rawMessage.includes("blocked")) {
            clientCode = "PERMISSION_DENIED";
            clientMessage = "The magical generator has closed its gates for a brief rest. Please verify API key permissions.";
        } else {
            clientCode = "ACCESS_FORBIDDEN";
            clientMessage = "Access keys for generating bedtime scenes has expired. Please check your session.";
        }
    } else if (status === 429 || rawStatus === "RESOURCE_EXHAUSTED" || rawMessage.includes("quota")) {
         clientCode = "RATE_LIMIT_EXCEEDED";
         clientMessage = "Our stellar illustrators and storytellers are extra tired from telling stories! Let's take a deep breath and retry in a minute.";
    } else if (status >= 500) {
         clientCode = "UPSTREAM_TEMPORARILY_OFFLINE";
         clientMessage = "The magical star server is taking a tiny nap. Let's try once more, or enjoy our cozy offline library stories.";
    }

    return {
        status: status,
        body: {
            error: {
                code: clientCode,
                message: clientMessage,
                status: status,
                originalMessage: rawMessage || undefined
            }
        }
    };
}

// ==========================================
// 4. ROUTE ROUTERS (PROXY DIRECT)
// ==========================================

/**
 * POST /api/v1/story/generate
 * Abstracted proxy routing to Gemini model
 */
app.post('/api/v1/story/generate', authenticateJWT, storyRateLimiter, sanitizeAndGuardPrompts, async (req: Request, res: Response) => {
    const API_KEY = process.env.GEMINI_API_KEY;
    if (!API_KEY) {
        return res.status(500).json({
            error: {
                message: "Bedtime gateway credentials misconfigured on cloud host.",
                code: "INTERNAL_GATEWAY_ERROR"
            }
        });
    }

    try {
        const geminiEndpoint = `https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=${API_KEY}`;
        
        // Proxy fetch upstream
        const upstreamResponse = await fetch(geminiEndpoint, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(req.body)
        });

        const data = await upstreamResponse.json();
        
        if (!upstreamResponse.ok) {
            const mapped = mapUpstreamError(upstreamResponse.status, data);
            return res.status(mapped.status).json(mapped.body);
        }

        // Intercept API level safety block triggers in successful responses
        if (data.promptFeedback?.blockReason === "SAFETY") {
            return res.status(200).json({
                error: {
                    code: "SAFETY_FILTER_VIOLATION",
                    message: "The stars want us to have a purely cozy, happy sleep! Let's try adjusting the character details or keywords to keep the dreams sweet.",
                    status: 200
                }
            });
        }

        const candidate = data.candidates?.[0];
        if (candidate?.finishReason === "SAFETY") {
            return res.status(200).json({
                error: {
                    code: "SAFETY_FILTER_VIOLATION",
                    message: "The magical storytelling paintbrush chose to paint a cozy sky instead of that scene. Let's try choosing gentler companion words to ensure a peaceful night.",
                    status: 200
                }
            });
        }

        return res.status(200).json(data);

    } catch (error: any) {
        return res.status(500).json({
            error: {
                message: "Transport error communicating with bedtime chronicles generators.",
                details: error.message
            }
        });
    }
});

/**
 * POST /api/v1/images/generate
 * Abstracted proxy routing to Imagen
 */
app.post('/api/v1/images/generate', authenticateJWT, imageRateLimiter, sanitizeAndGuardPrompts, async (req: Request, res: Response) => {
    const API_KEY = process.env.GEMINI_API_KEY;
    if (!API_KEY) {
        return res.status(500).json({
            error: {
                message: "Bedtime gateway credentials misconfigured on cloud host.",
                code: "INTERNAL_GATEWAY_ERROR"
            }
        });
    }

    try {
        const imagenEndpoint = `https://generativelanguage.googleapis.com/v1beta/models/imagen-3.0-generate-002:generateImages?key=${API_KEY}`;
        
        const upstreamResponse = await fetch(imagenEndpoint, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(req.body)
        });

        const data = await upstreamResponse.json();
        
        if (!upstreamResponse.ok) {
            const mapped = mapUpstreamError(upstreamResponse.status, data);
            return res.status(mapped.status).json(mapped.body);
        }

        return res.status(200).json(data);

    } catch (error: any) {
        return res.status(500).json({
            error: {
                message: "Transport error communicating with bedtime chronicles illustration sketching.",
                details: error.message
            }
        });
    }
});

// ==========================================
// STARTUP SERVER VOICE
// ==========================================
app.listen(PORT, () => {
    console.log(`[SECURE COMPLIANT GATEWAY] Active on Port: ${PORT}`);
    console.log(`[SECURITY STATS] JWT Signature JWKS Point: ${JWKS_URI}`);
});
