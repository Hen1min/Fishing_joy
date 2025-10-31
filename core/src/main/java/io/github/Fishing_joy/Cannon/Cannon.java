package io.github.Fishing_joy.Cannon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Abstract base for cannons. Handles common helpers; subclasses provide level-specific visuals and muzzle position.
 */
public abstract class Cannon {
    protected float x, y;
    protected float angleDeg = 0f;

    // icon textures (shared)
    protected static Texture plusTexture;
    protected static TextureRegion plusRegion;
    protected static Texture plusDownTexture;
    protected static TextureRegion plusDownRegion;
    protected static Texture minusTexture;
    protected static TextureRegion minusRegion;
    protected static Texture minusDownTexture;
    protected static TextureRegion minusDownRegion;

    protected float iconSpacing = 48f;

    // firing cooldown: minimum interval between shots (seconds) and timer
    protected float fireCooldown = 0.4f; // default minimum interval between shots
    protected float timeSinceLastShot = 0f;

    /**
     * Accumulate time (should be called each frame with delta time). This will allow canFire()/tryFire() to work.
     */
    public void updateFireCooldown(float delta) {
        if (delta > 0f) timeSinceLastShot += delta;
    }

    /** Returns true if cooldown has elapsed and cannon may fire. Does not modify timer. */
    public boolean canFire() {
        return timeSinceLastShot >= fireCooldown;
    }

    /**
     * Attempt to fire: if cooldown elapsed, reset timer and return true. Otherwise return false.
     * Callers should call this when attempting to spawn a bullet.
     */
    public boolean tryFire() {
        if (canFire()) {
            timeSinceLastShot = 0f;
            return true;
        }
        return false;
    }

    /** Set minimum interval (seconds) between shots. Non-negative. */
    public void setFireCooldown(float seconds) {
        this.fireCooldown = Math.max(0f, seconds);
    }

    /** Get current minimum interval between shots (seconds). */
    public float getFireCooldown() {
        return this.fireCooldown;
    }

    /** Reset the internal shot timer to the current cooldown so the cannon can fire immediately. */
    public void resetFireTimer() {
        this.timeSinceLastShot = this.fireCooldown;
    }

    /** Remaining seconds until next allowed shot (<=0 means ready). */
    public float getRemainingCooldown() {
        return Math.max(0f, fireCooldown - timeSinceLastShot);
    }

    protected Cannon(float x, float y) {
        this.x = x; this.y = y;
        // Allow newly created cannons to fire immediately by initializing the timer to the cooldown
        this.timeSinceLastShot = this.fireCooldown;
    }

    // Subclasses should load their own visuals (animations/textures)
    public static void loadCommonIcons() {
        try { plusTexture = new Texture(Gdx.files.internal("cannon_plus.png")); plusRegion = new TextureRegion(plusTexture); } catch (Exception e) { plusRegion = null; }
        try { plusDownTexture = new Texture(Gdx.files.internal("cannon_plus_down.png")); plusDownRegion = new TextureRegion(plusDownTexture); } catch (Exception e) { plusDownRegion = null; }
        try { minusTexture = new Texture(Gdx.files.internal("cannon_minus.png")); minusRegion = new TextureRegion(minusTexture); } catch (Exception e) { minusRegion = null; }
        try { minusDownTexture = new Texture(Gdx.files.internal("cannon_minus_down.png")); minusDownRegion = new TextureRegion(minusDownTexture); } catch (Exception e) { minusDownRegion = null; }
    }

    public static void disposeCommonIcons() {
        if (plusTexture != null) { plusTexture.dispose(); plusTexture = null; plusRegion = null; }
        if (plusDownTexture != null) { plusDownTexture.dispose(); plusDownTexture = null; plusDownRegion = null; }
        if (minusTexture != null) { minusTexture.dispose(); minusTexture = null; minusRegion = null; }
        if (minusDownTexture != null) { minusDownTexture.dispose(); minusDownTexture = null; minusDownRegion = null; }
    }

    // Public static accessors so UI layer can render icons separately
    public static TextureRegion getPlusRegion() { return plusRegion; }
    public static TextureRegion getPlusDownRegion() { return plusDownRegion; }
    public static TextureRegion getMinusRegion() { return minusRegion; }
    public static TextureRegion getMinusDownRegion() { return minusDownRegion; }

    // basic setters/getters
    public void setPosition(float x, float y) { this.x = x; this.y = y; }
    public void setAngleDeg(float a) { this.angleDeg = a; }
    public float getAngleDeg() { return angleDeg; }
    public float getX() { return x; }
    public float getY() { return y; }

    // lifecycle / rendering methods to be implemented by subclasses
    public abstract void update(float delta);
    public abstract void render(SpriteBatch batch);
    public abstract void triggerAnimation();
    public abstract float[] getMuzzlePosition();
    public abstract int getLevel();
    public abstract float getWidth();
    public abstract float getHeight();
    public abstract void dispose();
}
