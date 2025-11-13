package io.github.Fishing_joy.Fish;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Fish1 is a helper/factory for creating Fish instances of type Fish1.
 * It manages static animation resources and provides factory methods to
 * create Fish objects with animation and a given speed.
 */
public class Fish1 {

    // Public image path constant (relative to assets/)
    public static final String IMAGE_PATH = "fish1.png";

    private static Texture texture;
    private static Animation<TextureRegion> swimAnimation;
    private static Animation<TextureRegion> caughtAnimation;
    private static boolean loaded = false;

    // 视为竖向帧图，按实际帧数修改
    private static final int FRAME_COUNT = 8;
    private static final float FRAME_DURATION = 0.1f;

    // 默认属性（可改）
    private static final String DEFAULT_NAME = "Fish1";
    private static final int DEFAULT_HP = 12;
    private static final int DEFAULT_POINTS = 10;
    private static final int DEFAULT_ENERGY = 0;
    private static final float DEFAULT_SPEED = 30f; // units per second (pixels or world units)

    private Fish1() {
        // private - static helper class
    }

    // 静态资源加载（游戏启动或需要显示前调用）
    public static void load() {
        if (loaded) return;
        texture = new Texture(Gdx.files.internal(IMAGE_PATH));
        int frameWidth = texture.getWidth();
        int frameHeight = texture.getHeight() / FRAME_COUNT;
        TextureRegion[] frames = new TextureRegion[FRAME_COUNT];
        for (int i = 0; i < FRAME_COUNT; i++) {
            frames[i] = new TextureRegion(texture, 0, i * frameHeight, frameWidth, frameHeight);
        }
        // First half are swim frames, second half are caught frames (based on user's sprite layout)
        int half = FRAME_COUNT / 2;
        TextureRegion[] swimFrames = new TextureRegion[half];
        TextureRegion[] caughtFrames = new TextureRegion[FRAME_COUNT - half];
        System.arraycopy(frames, 0, swimFrames, 0, half);
        System.arraycopy(frames, half, caughtFrames, 0, FRAME_COUNT - half);

        swimAnimation = new Animation<>(FRAME_DURATION, swimFrames);
        swimAnimation.setPlayMode(Animation.PlayMode.LOOP);

        caughtAnimation = new Animation<>(FRAME_DURATION, caughtFrames);
        caughtAnimation.setPlayMode(Animation.PlayMode.NORMAL);

        loaded = true;
    }

    // 释放纹理（在退出或切换时调用）
    public static void dispose() {
        if (texture != null) {
            texture.dispose();
            texture = null;
        }
        swimAnimation = null;
        caughtAnimation = null;
        loaded = false;
    }

    // Provide accessors so other systems can use the animation
    public static Animation<TextureRegion> getSwimAnimation() {
        if (!loaded) load();
        return swimAnimation;
    }

    public static Animation<TextureRegion> getCaughtAnimation() {
        if (!loaded) load();
        return caughtAnimation;
    }

    public static Texture getTexture() {
        if (!loaded) load();
        return texture;
    }

    // Factory: create a Fish instance for Fish1 with the type's default speed
    public static Fish create() {
        if (!loaded) load();
        // Pass both swim and caught animations so Fish can play death animation when needed
        Fish f = new Fish(DEFAULT_NAME, DEFAULT_HP, DEFAULT_POINTS, DEFAULT_ENERGY, IMAGE_PATH, swimAnimation, caughtAnimation, DEFAULT_SPEED);
        // tune collision radius to a tighter value (percentage of half the min frame dimension)
        int frameWidth = texture.getWidth();
        int frameHeight = texture.getHeight() / FRAME_COUNT;
        float tunedRadius = Math.min(frameWidth, frameHeight) * 0.5f * 0.6f; // 60% of half-min-dim
        f.setCollisionRadius(tunedRadius);
        return f;
    }
}
