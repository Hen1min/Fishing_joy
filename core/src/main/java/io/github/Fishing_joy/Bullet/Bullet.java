package io.github.Fishing_joy.Bullet;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;

/**
 * Generic bullet (net) base class used by Bullet1 / Bullet2.
 * Handles movement, flying texture and hit -> web animation.
 */
public class Bullet {
    protected TextureRegion region;
    protected TextureRegion webRegion;
    protected float x, y;
    protected float vx, vy;
    protected float speed;
    protected float width, height;
    protected float angleDeg;
    // collision radius (in world units) used for simplified circle-based collision detection
    protected float collisionRadius = 0f;

    private enum State { FLYING, HIT, DONE }
    private State state = State.FLYING;
    private float hitTime = 0f;
    private final float HIT_DURATION = 0.6f;
    private final float MAX_SCALE = 1.25f;
    private float currentScale = 1f;

    protected int minDamage = 8;
    protected int maxDamage = 12;
    protected int cost = 4;

    public Bullet(float x, float y, float angleDeg, float speed, TextureRegion region, TextureRegion webRegion) {
        this.region = region;
        this.webRegion = webRegion;
        this.x = x;
        this.y = y;
        this.angleDeg = angleDeg;
        this.speed = speed;
        if (region != null) {
            this.width = region.getRegionWidth();
            this.height = region.getRegionHeight();
        } else if (webRegion != null) {
            this.width = webRegion.getRegionWidth();
            this.height = webRegion.getRegionHeight();
        }
        // initialize a reasonable default collision radius (use smaller than half size to avoid false positives)
        if (this.width > 0 && this.height > 0) {
            this.collisionRadius = Math.min(this.width, this.height) * 0.5f * 0.75f; // 75% of half the min dimension
        } else {
            this.collisionRadius = 8f; // fallback
        }
        float rad = MathUtils.degRad * angleDeg;
        this.vx = MathUtils.cos(rad) * this.speed;
        this.vy = MathUtils.sin(rad) * this.speed;
    }

    /**
     * Get collision radius used for circle-based collision tests (in same units as getX/getY).
     */
    public float getCollisionRadius() { return collisionRadius; }

    /** Allow tuning collision radius (e.g., in subclasses) */
    public void setCollisionRadius(float r) { this.collisionRadius = Math.max(0f, r); }

    public void setSpeed(float newSpeed) {
        if (newSpeed <= 0) return;
        float ang = MathUtils.atan2(vy, vx) * MathUtils.radiansToDegrees;
        this.speed = newSpeed;
        float rad = MathUtils.degRad * ang;
        this.vx = MathUtils.cos(rad) * this.speed;
        this.vy = MathUtils.sin(rad) * this.speed;
    }

    public void update(float delta) {
        if (state == State.FLYING) {
            x += vx * delta;
            y += vy * delta;
        } else if (state == State.HIT) {
            hitTime += delta;
            float half = HIT_DURATION / 2f;
            if (hitTime <= half) {
                currentScale = 1f + (MAX_SCALE - 1f) * (hitTime / half);
            } else if (hitTime <= HIT_DURATION) {
                currentScale = MAX_SCALE - (MAX_SCALE - 1f) * ((hitTime - half) / half);
            } else {
                currentScale = 1f;
                state = State.DONE;
            }
        }
    }

    public void render(SpriteBatch batch) {
        if (state == State.DONE) return;
        if (state == State.FLYING) {
            if (region == null) return;
            batch.draw(region,
                    x - width / 2f,
                    y - height / 2f,
                    width / 2f,
                    height / 2f,
                    width,
                    height,
                    1f,
                    1f,
                    angleDeg - 80);
        } else if (state == State.HIT) {
            if (webRegion == null) return;
            float w = webRegion.getRegionWidth() * currentScale;
            float h = webRegion.getRegionHeight() * currentScale;
            batch.draw(webRegion,
                    x - w / 2f,
                    y - h / 2f,
                    w / 2f,
                    h / 2f,
                    w,
                    h,
                    1f,
                    1f,
                    0f);
        }
    }

    public void capture() {
        if (state != State.FLYING) return;
        state = State.HIT;
        vx = 0f;
        vy = 0f;
        hitTime = 0f;
        currentScale = 1f;
    }

    public boolean isDone() { return state == State.DONE; }
    public boolean isCaptured() { return state != State.FLYING; }

    public float getX() { return x; }
    public float getY() { return y; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }
    public float getAngleDeg() { return angleDeg; }

    /**
     * Return a randomized damage according to bullet type ranges.
     */
    public int getDamage() {
        return MathUtils.random(minDamage, maxDamage);
    }

    public int getCost() { return cost; }
}
