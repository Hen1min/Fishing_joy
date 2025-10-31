package io.github.Fishing_joy.util;

/**
 * Global collision configuration used by Fish/Bullet to compute default collision radii.
 */
public class CollisionConfig {
    /**
     * Fraction applied to half the minimum sprite dimension to compute a default circular collision radius.
     * Lower -> tighter collision. 0.5 means radius = min(w,h) * 0.5 * 0.5 = 0.25 * minDim.
     */
    public static final float DEFAULT_COLLISION_SCALE = 0.5f;

    /** Fallback radii when texture sizes are unavailable. */
    public static final float BULLET_FALLBACK_RADIUS = 6f;
    public static final float FISH_FALLBACK_RADIUS = 10f;
}

