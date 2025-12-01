// New file: Fish8.java
package io.github.Fishing_joy.Fish;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.ArrayList;
import java.util.List;
import io.github.Fishing_joy.util.MultiCircleCollision;

/**
 * Fish8: a special "stun" fish. When its HP drops to 0 it triggers a 2-second stun
 * on all fish (game system will catch this via a listener in Swim_Animator). Uses
 * fish8.png from assets/.
 *
 * Stats required by user: HP=1, points=0, energy=0
 */
public class Fish8 {

    public static final String IMAGE_PATH = "fish8.png";

    private static Texture texture;
    private static Animation<TextureRegion> swimAnimation;
    private static Animation<TextureRegion> caughtAnimation;
    private static boolean loaded = false;

    // Assume same frame layout as other fish types; adjust if different
    private static final int FRAME_COUNT = 12;
    private static final float FRAME_DURATION = 0.1f;

    private static final String DEFAULT_NAME = "StunFish";
    private static final int DEFAULT_HP = 1;
    private static final int DEFAULT_POINTS = 0;
    private static final int DEFAULT_ENERGY = 0;
    private static final float DEFAULT_SPEED = 20f; // relatively slow

    private Fish8() {}

    public static void load() {
        if (loaded) return;
        texture = new Texture(Gdx.files.internal(IMAGE_PATH));
        int frameWidth = texture.getWidth();
        int frameHeight = texture.getHeight() / FRAME_COUNT;
        TextureRegion[] frames = new TextureRegion[FRAME_COUNT];
        for (int i = 0; i < FRAME_COUNT; i++) frames[i] = new TextureRegion(texture, 0, i * frameHeight, frameWidth, frameHeight);
        int half = FRAME_COUNT / 3 * 2;
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

    public static void dispose() {
        if (texture != null) {
            texture.dispose();
            texture = null;
        }
        swimAnimation = null;
        caughtAnimation = null;
        loaded = false;
    }

    public static Animation<TextureRegion> getSwimAnimation() { if (!loaded) load(); return swimAnimation; }
    public static Animation<TextureRegion> getCaughtAnimation() { if (!loaded) load(); return caughtAnimation; }
    public static Texture getTexture() { if (!loaded) load(); return texture; }

    public static Fish create() {
        if (!loaded) load();
        Fish f = new Fish(DEFAULT_NAME, DEFAULT_HP, DEFAULT_POINTS, DEFAULT_ENERGY, IMAGE_PATH, swimAnimation, caughtAnimation, DEFAULT_SPEED);
        // tune collision radius
        int frameWidth = texture.getWidth();
        int frameHeight = texture.getHeight() / FRAME_COUNT;
        float tunedRadius = Math.min(frameWidth, frameHeight) * 0.5f * 0.5f;
        f.setCollisionRadius(tunedRadius);
        // add main central circle and two smaller circles on the left side
        List<MultiCircleCollision.Circle> circles = new ArrayList<>();
        // main central circle at (0,0)
        circles.add(new MultiCircleCollision.Circle(0f, 20f, tunedRadius));
        // two small circles placed to the left of center (negative x).
        // offsets chosen relative to tunedRadius to keep them visually on the left side.
        float smallRadius = tunedRadius * 0.5f; // smaller than main
        float leftOffsetX = -tunedRadius * 0.7f; // push left from center
        circles.add(new MultiCircleCollision.Circle(leftOffsetX - 20f, 20f, smallRadius));
        circles.add(new MultiCircleCollision.Circle(leftOffsetX - 40f, 20f, smallRadius * 0.3f));
        circles.add(new MultiCircleCollision.Circle(leftOffsetX - 47f, 20f, smallRadius * 0.3f));
        f.setCollisionCircles(circles);
        return f;
    }
}
