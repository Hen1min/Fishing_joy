package io.github.Fishing_joy.Fish;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.ArrayList;
import java.util.List;
import io.github.Fishing_joy.util.MultiCircleCollision;

/**
 * Fish1 is a helper/factory for creating Fish instances of type Fish1.
 * It manages static animation resources and provides factory methods to
 * create Fish objects with animation and a given speed.
 */
public class Fish7 {

    // Public image path constant (relative to assets/)
    public static final String IMAGE_PATH = "fish7.png";

    private static Texture texture;
    private static Animation<TextureRegion> swimAnimation;
    private static Animation<TextureRegion> caughtAnimation;
    private static boolean loaded = false;

    // 视为竖向帧图，按实际帧数修改
    private static final int FRAME_COUNT = 10;
    private static final float FRAME_DURATION = 0.1f;

    // 默认属性（可改）
    private static final String DEFAULT_NAME = "Fish7";
    private static final int DEFAULT_HP = 300;
    private static final int DEFAULT_POINTS = 550;
    private static final int DEFAULT_ENERGY = 3;
    private static final float DEFAULT_SPEED = 12f; // units per second (pixels or world units)

    private Fish7() {
        // private - static helper class
    }

    // 静态资源加载（游戏启动或需要显示前调用）
    public static void load() {
        if (loaded) return;
        texture = new Texture(Gdx.files.internal(IMAGE_PATH));
        int frameWidth = texture.getWidth();
        int frameHeight = texture.getHeight() / FRAME_COUNT;
        TextureRegion[] frames = new TextureRegion[FRAME_COUNT];
        for (int i = 0; i < FRAME_COUNT; i++) frames[i] = new TextureRegion(texture, 0, i * frameHeight, frameWidth, frameHeight);
        int half = Math.max(1, FRAME_COUNT * 6 / 10);
        TextureRegion[] swimFrames = new TextureRegion[half];
        TextureRegion[] caughtFrames = new TextureRegion[FRAME_COUNT - half];
        System.arraycopy(frames, 0, swimFrames, 0, half);
        System.arraycopy(frames, half, caughtFrames, 0, FRAME_COUNT - half);
        swimAnimation = new Animation<>(FRAME_DURATION, swimFrames); swimAnimation.setPlayMode(Animation.PlayMode.LOOP);
        caughtAnimation = new Animation<>(FRAME_DURATION, caughtFrames); caughtAnimation.setPlayMode(Animation.PlayMode.NORMAL);
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
    public static Animation<TextureRegion> getSwimAnimation() { if (!loaded) load(); return swimAnimation; }
    public static Animation<TextureRegion> getCaughtAnimation() { if (!loaded) load(); return caughtAnimation; }
    public static Texture getTexture() { if (!loaded) load(); return texture; }

    // Factory: create a Fish instance for Fish1 with the type's default speed
    public static Fish create() {
        if (!loaded) load();
        Fish f = new Fish(DEFAULT_NAME, DEFAULT_HP, DEFAULT_POINTS, DEFAULT_ENERGY, IMAGE_PATH, swimAnimation, caughtAnimation, DEFAULT_SPEED);
        int frameWidth = texture.getWidth(); int frameHeight = texture.getHeight() / FRAME_COUNT;
        float tunedRadius = Math.min(frameWidth, frameHeight) * 0.5f * 0.6f; f.setCollisionRadius(tunedRadius);
        float halfW = frameWidth / 2f; float halfH = frameHeight / 2f;
        // enlarge and lift circles to cover the visible fish body (avoid shadow below)
        float base = Math.min(frameWidth, frameHeight) * 0.5f * 0.58f;
        // main body runs from right-top to left-bottom. Place centroid in right-top half.
        float centerX = halfW * 0.30f; // right bias
        // nudged upward so collision circles angle upward more
        float centerY = halfH * 0.37f; // increased up bias
        List<MultiCircleCollision.Circle> circles = new ArrayList<>();
        // main top-right body
        circles.add(new MultiCircleCollision.Circle(centerX, centerY, base));
        // forward-right head (shifted up)
        circles.add(new MultiCircleCollision.Circle(centerX + halfW * 0.20f, centerY + halfH * 0.22f, base * 0.95f));
        // backward-left tail area (smaller, shifted up)
        circles.add(new MultiCircleCollision.Circle(centerX - halfW * 0.40f, centerY - halfH * 0.08f, base * 0.65f));
        // diagonal fillers up-left to cover remaining body (moved upward)
        circles.add(new MultiCircleCollision.Circle(centerX - halfW * 0.65f, centerY - halfH * 0.22f, base * 0.45f));
        circles.add(new MultiCircleCollision.Circle(centerX - halfW * 0.9f, centerY - halfH * 0.38f, base * 0.30f));
        f.setCollisionCircles(circles);
        return f;
    }
}
