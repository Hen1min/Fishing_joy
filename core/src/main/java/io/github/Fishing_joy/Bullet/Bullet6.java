package io.github.Fishing_joy.Bullet;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;


public class Bullet6 extends Bullet {
    private static Texture texture;
    private static TextureRegion region;
    private static Texture webTexture;
    private static TextureRegion webRegion;
    public static float DEFAULT_SPEED = 700f; // slightly faster

    public Bullet6(float x, float y, float angleDeg) {
        this(x, y, angleDeg, DEFAULT_SPEED);
    }

    public Bullet6(float x, float y, float angleDeg, float speed) {
        super(x, y, angleDeg, speed, region, webRegion);
        this.minDamage = 80; // stronger
        this.maxDamage = 140;
        this.cost = 60; // costs more points to fire
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
            texture = new Texture(Gdx.files.internal("bullet6.png"));
            region = new TextureRegion(texture);
        }
        if (webTexture == null) {
            webTexture = new Texture(Gdx.files.internal("web6.png"));
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
