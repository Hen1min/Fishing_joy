package io.github.Fishing_joy.Bullet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Bullet1 implementation (basic bullet) that uses bullet1.png and web1.png.
 * Now extends the generic Bullet base class.
 */
public class Bullet1 extends Bullet {
    private static Texture texture;
    private static TextureRegion region;
    private static Texture webTexture;
    private static TextureRegion webRegion;
    public static float DEFAULT_SPEED = 600f;

    public Bullet1(float x, float y, float angleDeg) {
        this(x, y, angleDeg, DEFAULT_SPEED);
    }

    public Bullet1(float x, float y, float angleDeg, float speed) {
        super(x, y, angleDeg, speed, region, webRegion);
        // default damage/cost for Bullet1
        this.minDamage = 5;
        this.maxDamage = 12;
        this.cost = 8;
        // tune collision radius to be tighter than sprite bounds
        if (region != null) {
            float rw = region.getRegionWidth();
            float rh = region.getRegionHeight();
            float tuned = Math.min(rw, rh) * 0.5f * 0.6f; // 60% of half-min-dim
            setCollisionRadius(tuned);
        }
    }

    public static void load() {
        if (texture == null) {
            texture = new Texture(Gdx.files.internal("bullet1.png"));
            region = new TextureRegion(texture);
        }
        if (webTexture == null) {
            webTexture = new Texture(Gdx.files.internal("web1.png"));
            webRegion = new TextureRegion(webTexture);
        }
    }

    public static void disposeTexture() {
        if (texture != null) {
            texture.dispose();
            texture = null;
            region = null;
        }
        if (webTexture != null) {
            webTexture.dispose();
            webTexture = null;
            webRegion = null;
        }
    }
}
