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
        // rotation: add +15 degrees to existing tilt
        // increased tilt: add +30 degrees (previously 15°; user requested extra +15°)
        float angleDeg = 30f;
        double rad = Math.toRadians(angleDeg);
        float cos = (float)Math.cos(rad);
        float sin = (float)Math.sin(rad);
        float base1 = base * 1.2f;
        // helper to rotate a point (rawX, rawY) around (centerX, centerY)
        // main top-right body (center stays same)
        circles.add(new MultiCircleCollision.Circle(centerX, centerY, base1));
        // prepare raw points
        float r1x = centerX + halfW * 0.20f; float r1y = centerY + halfH * 0.22f; // forward-right head
        float r2x = centerX - halfW * 0.40f; float r2y = centerY - halfH * 0.08f; // backward-left tail
        float r3x = centerX - halfW * 0.65f; float r3y = centerY - halfH * 0.22f; // diagonal filler
        float r4x = centerX - halfW * 0.9f;  float r4y = centerY - halfH * 0.38f; // far tail
        // rotate and add with corresponding radii
        float dx, dy, rx, ry;
        dx = r1x - centerX; dy = r1y - centerY; rx = centerX + (dx * cos - dy * sin); ry = centerY + (dx * sin + dy * cos);
        circles.add(new MultiCircleCollision.Circle(rx, ry, base1 * 0.95f));
        dx = r2x - centerX; dy = r2y - centerY; rx = centerX + (dx * cos - dy * sin); ry = centerY + (dx * sin + dy * cos);
        circles.add(new MultiCircleCollision.Circle(rx, ry, base1 * 0.65f));
        dx = r3x - centerX; dy = r3y - centerY; rx = centerX + (dx * cos - dy * sin); ry = centerY + (dx * sin + dy * cos);
        circles.add(new MultiCircleCollision.Circle(rx, ry, base1 * 0.45f));
        dx = r4x - centerX; dy = r4y - centerY; rx = centerX + (dx * cos - dy * sin); ry = centerY + (dx * sin + dy * cos);
        circles.add(new MultiCircleCollision.Circle(rx, ry, base1 * 0.30f));
        f.setCollisionCircles(circles);
        return f;
    }
}
