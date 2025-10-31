package io.github.Fishing_joy;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.github.Fishing_joy.Fish.Fish;

/**
 * Base class for fish that have animations. Subclasses must provide the
 * Animation via getAnimation(). This class stores per-instance stateTime
 * so each fish animates independently.
 */
public abstract class AnimatedFish extends Fish {

    protected float stateTime = 0f;

    public AnimatedFish(String name, int hp, int points, int energy) {
        // Call Fish constructor with no animation and zero speed; subclasses may
        // provide a shared animation via getAnimation().
        // pass imagePath=null, swimAnimation=null, caughtAnimation=null, speed=0f
        super(name, hp, points, energy, null, null, null, 0f);
    }

    public AnimatedFish(String name, int hp, int points, int energy, String imagePath) {
        // Pass imagePath but no per-instance animation/speed; subclasses will
        // supply the shared Animation via getAnimation().
        // pass swimAnimation=null, caughtAnimation=null, speed=0f
        super(name, hp, points, energy, imagePath, null, null, 0f);
    }

    /**
     * Subclasses must return the shared Animation for this fish type.
     */
    protected abstract Animation<TextureRegion> getAnimation();

    /**
     * Update per-instance animation time.
     */
    public void update(float delta) {
        stateTime += delta;
    }

    /**
     * Render this fish at the given world coordinates using its Animation.
     */
    public void render(SpriteBatch batch, float x, float y) {
        Animation<TextureRegion> anim = getAnimation();
        if (anim == null) return;
        TextureRegion frame = anim.getKeyFrame(stateTime, true);
        batch.draw(frame, x, y);
    }

    public void resetAnimation() {
        stateTime = 0f;
    }
}
