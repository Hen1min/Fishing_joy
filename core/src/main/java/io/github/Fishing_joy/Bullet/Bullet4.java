package io.github.Fishing_joy.Bullet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Bullet2: stronger bullet (higher damage and cost) using bullet2.png and web2.png
 */
public class Bullet4 extends Bullet {
    private static Texture texture;
    private static TextureRegion region;
    private static Texture webTexture;
    private static TextureRegion webRegion;
    public static float DEFAULT_SPEED = 700f; // slightly faster

    public Bullet4(float x, float y, float angleDeg) {
        this(x, y, angleDeg, DEFAULT_SPEED);
    }

    public Bullet4(float x, float y, float angleDeg, float speed) {
        super(x, y, angleDeg, speed, region, webRegion);
        this.minDamage = 45; // stronger
        this.maxDamage = 70;
        this.cost = 30; // costs more points to fire
        // tune collision radius to be slightly tighter than sprite bounds
        if (region != null) {
            float rw = region.getRegionWidth();
            float rh = region.getRegionHeight();
            float tuned = Math.min(rw, rh) * 0.5f * 0.6f;
            setCollisionRadius(tuned);
        }
    }

    public static void load() {
        if (texture == null) {
            texture = new Texture(Gdx.files.internal("bullet4.png"));
            region = new TextureRegion(texture);
        }
        if (webTexture == null) {
            webTexture = new Texture(Gdx.files.internal("web4.png"));
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
