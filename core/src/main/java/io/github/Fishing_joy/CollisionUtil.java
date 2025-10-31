package io.github.Fishing_joy;

/**
 * Utility functions for polygon collision detection using the Separating Axis Theorem (SAT).
 *
 * The methods operate on polygons expressed as float arrays: {x0,y0,x1,y1,...}.
 */
public final class CollisionUtil {

    private CollisionUtil() {}

    /**
     * Build a rotated rectangle polygon (4 points) given center, size and rotation in degrees.
     * Returns an array of 8 floats: {x0,y0,x1,y1,x2,y2,x3,y3} in CCW order.
     */
    public static float[] buildRectPoly(float centerX, float centerY, float width, float height, float rotationDeg) {
        float hw = width / 2f;
        float hh = height / 2f;
        // local corner coordinates (CCW)
        float lx0 = -hw, ly0 = -hh;
        float lx1 = hw,  ly1 = -hh;
        float lx2 = hw,  ly2 = hh;
        float lx3 = -hw, ly3 = hh;
        // rotation
        double rad = Math.toRadians(rotationDeg);
        float cos = (float)Math.cos(rad);
        float sin = (float)Math.sin(rad);
        float[] out = new float[8];
        out[0] = centerX + (lx0 * cos - ly0 * sin);
        out[1] = centerY + (lx0 * sin + ly0 * cos);
        out[2] = centerX + (lx1 * cos - ly1 * sin);
        out[3] = centerY + (lx1 * sin + ly1 * cos);
        out[4] = centerX + (lx2 * cos - ly2 * sin);
        out[5] = centerY + (lx2 * sin + ly2 * cos);
        out[6] = centerX + (lx3 * cos - ly3 * sin);
        out[7] = centerY + (lx3 * sin + ly3 * cos);
        return out;
    }

    private static void projectOntoAxis(float[] poly, float axisX, float axisY, float[] out) {
        // out[0] = min, out[1] = max
        float min = Float.POSITIVE_INFINITY;
        float max = Float.NEGATIVE_INFINITY;
        for (int i = 0; i < poly.length; i += 2) {
            float vx = poly[i];
            float vy = poly[i+1];
            float proj = vx * axisX + vy * axisY; // dot product
            if (proj < min) min = proj;
            if (proj > max) max = proj;
        }
        out[0] = min;
        out[1] = max;
    }

    /**
     * Test intersection between two convex polygons (each as {x0,y0,x1,y1,...}).
     * Uses SAT: returns true if polygons overlap.
     */
    public static boolean polygonsIntersect(float[] a, float[] b) {
        // for each edge of both polygons
        float[] tmp = new float[2];
        // check edges of A
        for (int i = 0; i < a.length; i += 2) {
            int ni = (i + 2) % a.length;
            float ex = a[ni] - a[i];
            float ey = a[ni+1] - a[i+1];
            // normal (axis) perpendicular to edge
            float axisX = -ey;
            float axisY = ex;
            // normalize axis to avoid large numbers (optional but safer)
            float len = (float)Math.sqrt(axisX * axisX + axisY * axisY);
            if (len == 0f) continue;
            axisX /= len; axisY /= len;
            projectOntoAxis(a, axisX, axisY, tmp);
            float aMin = tmp[0], aMax = tmp[1];
            projectOntoAxis(b, axisX, axisY, tmp);
            float bMin = tmp[0], bMax = tmp[1];
            if (aMax < bMin || bMax < aMin) return false; // separation found
        }
        // check edges of B
        for (int i = 0; i < b.length; i += 2) {
            int ni = (i + 2) % b.length;
            float ex = b[ni] - b[i];
            float ey = b[ni+1] - b[i+1];
            float axisX = -ey;
            float axisY = ex;
            float len = (float)Math.sqrt(axisX * axisX + axisY * axisY);
            if (len == 0f) continue;
            axisX /= len; axisY /= len;
            projectOntoAxis(a, axisX, axisY, tmp);
            float aMin = tmp[0], aMax = tmp[1];
            projectOntoAxis(b, axisX, axisY, tmp);
            float bMin = tmp[0], bMax = tmp[1];
            if (aMax < bMin || bMax < aMin) return false;
        }
        return true; // no separating axis found
    }
}

