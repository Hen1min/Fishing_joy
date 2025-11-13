package io.github.Fishing_joy.util;

import java.util.List;

/**
 * Simple multi-circle collision utilities. Circles are defined in a sprite-local coordinate
 * system where (0,0) is the sprite center used for rendering. To test collisions in world
 * space you must translate/rotate the local circle centers by the sprite's world center
 * and rotation.
 */
public final class MultiCircleCollision {

    private MultiCircleCollision() {}

    public static class Circle {
        // center in local sprite coordinates (pixels), radius in pixels/world units (same units as rendering)
        public final float x;
        public final float y;
        public final float r;
        public Circle(float x, float y, float r) {
            this.x = x;
            this.y = y;
            this.r = r;
        }
    }

    /**
     * Test two circles for overlap.
     */
    public static boolean circlesOverlap(float ax, float ay, float ar, float bx, float by, float br) {
        float dx = ax - bx;
        float dy = ay - by;
        float rsum = ar + br;
        return dx * dx + dy * dy <= rsum * rsum;
    }

    /**
     * Rotate a point (px,py) by degrees around origin (0,0).
     */
    public static float[] rotatePoint(float px, float py, float degrees) {
        if (degrees == 0f) return new float[] { px, py };
        double rad = Math.toRadians(degrees);
        double c = Math.cos(rad);
        double s = Math.sin(rad);
        float rx = (float) (px * c - py * s);
        float ry = (float) (px * s + py * c);
        return new float[] { rx, ry };
    }

    /**
     * Check whether a bullet (circle at bx,by with radius br) collides with any of the
     * circles in the supplied list. The circles are defined in sprite-local coordinates
     * and must be transformed by the sprite center (fx,fy) and rotation degrees.
     */
    public static boolean bulletHitsSpriteCircles(float bx, float by, float br,
                                                   float fx, float fy, float spriteRotationDeg,
                                                   List<Circle> spriteCircles) {
        if (spriteCircles == null || spriteCircles.isEmpty()) return false;
        for (Circle c : spriteCircles) {
            float[] p = rotatePoint(c.x, c.y, spriteRotationDeg);
            float worldCx = fx + p[0];
            float worldCy = fy + p[1];
            if (circlesOverlap(bx, by, br, worldCx, worldCy, c.r)) return true;
        }
        return false;
    }
}

