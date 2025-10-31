package io.github.Fishing_joy.util;

/**
 * Collision helpers: OBB overlap test using Separating Axis Theorem (SAT).
 */
public class CollisionUtil {

    private static float dot(float ax, float ay, float bx, float by) {
        return ax * bx + ay * by;
    }

    // compute the four corners of a rectangle centered at (cx,cy) with width w and height h rotated by angleDeg
    // returns array {x0,y0,x1,y1,x2,y2,x3,y3} in order
    public static float[] computeCorners(float cx, float cy, float w, float h, float angleDeg) {
        float hw = w / 2f;
        float hh = h / 2f;
        float rad = (float) Math.toRadians(angleDeg);
        float cos = (float) Math.cos(rad);
        float sin = (float) Math.sin(rad);
        // local corners relative to center (top-left, top-right, bottom-right, bottom-left)
        float lx0 = -hw, ly0 = -hh;
        float lx1 = hw,  ly1 = -hh;
        float lx2 = hw,  ly2 = hh;
        float lx3 = -hw, ly3 = hh;
        float[] out = new float[8];
        out[0] = cx + (lx0 * cos - ly0 * sin);
        out[1] = cy + (lx0 * sin + ly0 * cos);
        out[2] = cx + (lx1 * cos - ly1 * sin);
        out[3] = cy + (lx1 * sin + ly1 * cos);
        out[4] = cx + (lx2 * cos - ly2 * sin);
        out[5] = cy + (lx2 * sin + ly2 * cos);
        out[6] = cx + (lx3 * cos - ly3 * sin);
        out[7] = cy + (lx3 * sin + ly3 * cos);
        return out;
    }

    // project polygon (corners) onto axis (ax,ay) and return min/max projection values via array [min,max]
    private static float[] projectOntoAxis(float[] corners, float ax, float ay) {
        float min = Float.POSITIVE_INFINITY;
        float max = Float.NEGATIVE_INFINITY;
        for (int i = 0; i < corners.length; i += 2) {
            float px = corners[i];
            float py = corners[i + 1];
            float p = dot(px, py, ax, ay);
            if (p < min) min = p;
            if (p > max) max = p;
        }
        return new float[]{min, max};
    }

    private static boolean overlapOnAxis(float[] c1, float[] c2, float ax, float ay) {
        // normalize axis to avoid scale issues
        float len = (float) Math.sqrt(ax * ax + ay * ay);
        if (len <= 1e-6f) return true; // degenerate axis => treat as overlap
        ax /= len; ay /= len;
        float[] p1 = projectOntoAxis(c1, ax, ay);
        float[] p2 = projectOntoAxis(c2, ax, ay);
        return !(p1[1] < p2[0] || p2[1] < p1[0]);
    }

    /**
     * OBB overlap test for two rectangles centered at (cx,cy) with dimensions (w,h) and rotation angleDeg.
     */
    public static boolean obbOverlap(float cx1, float cy1, float w1, float h1, float angleDeg1,
                                     float cx2, float cy2, float w2, float h2, float angleDeg2) {
        if (w1 <= 0 || h1 <= 0 || w2 <= 0 || h2 <= 0) return false;
        float[] c1 = computeCorners(cx1, cy1, w1, h1, angleDeg1);
        float[] c2 = computeCorners(cx2, cy2, w2, h2, angleDeg2);
        // axes: edges of both rectangles (use two unique axes from each rect)
        // axis from c1: edge c0->c1 and c1->c2
        float ax1x = c1[2] - c1[0];
        float ax1y = c1[3] - c1[1];
        float ax2x = c1[4] - c1[2];
        float ax2y = c1[5] - c1[3];
        float bx1x = c2[2] - c2[0];
        float bx1y = c2[3] - c2[1];
        float bx2x = c2[4] - c2[2];
        float bx2y = c2[5] - c2[3];

        if (!overlapOnAxis(c1, c2, ax1x, ax1y)) return false;
        if (!overlapOnAxis(c1, c2, ax2x, ax2y)) return false;
        if (!overlapOnAxis(c1, c2, bx1x, bx1y)) return false;
        if (!overlapOnAxis(c1, c2, bx2x, bx2y)) return false;
        return true;
    }
}

