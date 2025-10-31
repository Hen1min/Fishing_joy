package io.github.Fishing_joy.Cannon;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

/**
 * Level 3 cannon implementation (uses cannon3.png sprite sheet).
 */
public class Cannon4 extends Cannon {
    private static Texture texture;
    private static Animation<TextureRegion> animation;
    private static TextureRegion[] frames;
    private static boolean loaded = false;

    private static final int FRAME_COUNT = 5;
    private static final float FRAME_DURATION = 0.08f;

    private boolean animating = false;
    private float stateTime = 0f;
    private TextureRegion currentFrame;

    public Cannon4(float x, float y) {
        super(x, y);
        if (!loaded) load();
        currentFrame = animation.getKeyFrame(0f);
        this.iconSpacing = currentFrame.getRegionWidth() + 8f;
    }

    public static void load() {
        if (loaded) return;
        Cannon.loadCommonIcons();
        texture = new Texture(Gdx.files.internal("cannon4.png"));
        int frameW = texture.getWidth();
        int frameH = texture.getHeight() / FRAME_COUNT;
        frames = new TextureRegion[FRAME_COUNT];
        for (int i = 0; i < FRAME_COUNT; i++) frames[i] = new TextureRegion(texture, 0, i * frameH, frameW, frameH);
        animation = new Animation<>(FRAME_DURATION, frames);
        animation.setPlayMode(Animation.PlayMode.NORMAL);
        loaded = true;
    }

    public static void disposeStatic() {
        if (texture != null) { texture.dispose(); texture = null; }
        animation = null;
        frames = null;
        loaded = false;
    }

    @Override
    public void update(float delta) {
        if (animating) {
            stateTime += delta;
            if (animation.isAnimationFinished(stateTime)) {
                animating = false;
                stateTime = animation.getAnimationDuration();
                currentFrame = animation.getKeyFrame(stateTime);
            } else {
                currentFrame = animation.getKeyFrame(stateTime);
            }
        }
    }

    @Override
    public void render(SpriteBatch batch) {
        if (currentFrame == null) currentFrame = animation.getKeyFrame(0f);
        batch.draw(currentFrame,
            x - currentFrame.getRegionWidth() / 2f,
            y - currentFrame.getRegionHeight() / 2f,
            currentFrame.getRegionWidth() / 2f,
            currentFrame.getRegionHeight() / 2f,
            currentFrame.getRegionWidth(),
            currentFrame.getRegionHeight(),
            1f,1f, angleDeg);
    }

    @Override
    public void triggerAnimation() {
        if (!animating) {
            animating = true;
            stateTime = 0f;
            currentFrame = animation.getKeyFrame(0f);
        }
    }

    @Override
    public float[] getMuzzlePosition() {
        float rad = MathUtils.degRad * angleDeg;
        float localX = 0f;
        float localY = currentFrame.getRegionHeight() / 2f;
        float rx = MathUtils.cos(rad) * localX - MathUtils.sin(rad) * localY;
        float ry = MathUtils.sin(rad) * localX + MathUtils.cos(rad) * localY;
        return new float[]{x + rx, y + ry};
    }

    @Override
    public int getLevel() { return 4; }

    @Override
    public float getWidth() { return currentFrame.getRegionWidth(); }

    @Override
    public float getHeight() { return currentFrame.getRegionHeight(); }

    @Override
    public void dispose() {
        // no-op; static disposed via disposeStatic
    }
}
