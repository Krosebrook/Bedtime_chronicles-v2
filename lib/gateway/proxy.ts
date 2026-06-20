import { Request, Response } from 'express';
import rateLimit from 'express-rate-limit';

/**
 * AuthenticatedRequest interface extends standard Express Request 
 * to provide type safety for JWT decoded payloads.
 */
export interface AuthenticatedRequest extends Request {
    user?: {
        uid?: string;
        sub?: string;
        name?: string;
        email?: string;
        role?: string;
        [key: string]: any;
    };
}

/**
 * PRODUCTION-GRADE USER-SPECIFIC BEDTIME RATE LIMITER
 * 
 * Enforces security, cost targets, and bedtime safety limits per minute per single active child profile. 
 * This prevents bots or automated loops from exceeding billing parameters while providing safe guidance.
 * 
 * Key features:
 * - Uses the JWT-verified unique identifier (uid or sub) as the scaling bucket key
 * - Low-friction, sliding window tracker conforming to standard Express middleware
 * - Graceful fallback to client IP if the request flow bypasses token checks
 */
export const userSpecificRateLimiter = rateLimit({
    windowMs: 60 * 1000, // 1 minute sliding window
    max: 5, // Maximum 5 requests per minutes per profile (cozy guard-rails)
    standardHeaders: true, // Return RFC-compliant RateLimit headers
    legacyHeaders: false, // Turn off old non-standard headers
    
    /**
     * Dynamically selects the key based on validated JWT details.
     */
    keyGenerator: (req) => {
        const authReq = req as AuthenticatedRequest;
        // Primary key: verified user unique identifier (UID or SUB) from token payload.
        // Secondary key fallback: client source IP to manage anonymous pathways.
        return authReq.user?.uid || authReq.user?.sub || authReq.ip || 'anonymous_sleepy_guest';
    },

    /**
     * Production execution callback on rate limit violation.
     * Delivers client action targets with warm, emotionally intelligent response copies.
     */
    handler: (req: Request, res: Response) => {
        const authReq = req as AuthenticatedRequest;
        const profileId = authReq.user?.uid || authReq.user?.sub || 'anonymous';
        
        console.warn(`[RATE_LIMIT_TRIGGERED] Bedtime profile: ${profileId} exceeded generation parameters.`);
        
        res.status(429).json({
            error: {
                message: "A cozy limit has been reached! Let's take a deep breath before spinning our next magical starry adventure. Retry in one minute.",
                code: "USER_RATE_LIMIT_EXCEEDED",
                limit: 5,
                windowResetMs: 60000
            }
        });
    }
});
