package io.github.Fishing_joy.Fish;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * 表示一条鱼的基础数据和行为：
 * - name: 鱼的名称
 * - maxHp/currentHp: 血量
 * - points: 捕获后给予的积分
 * - energy: 捕获后给予的能量
 * - imagePath: （可选）鱼对应的图片文件路径（相对于 assets/）
 * - animation: 可选的动画材质（由外部加载并传入）
 * - speed: 鱼的移动速度（世界/像素单位，由使用的 viewport 决定）
 */
public class Fish {

    private final String name;
    private final int maxHp;
    private int currentHp;
    private final int points;
    private final int energy;
    private final String imagePath;

    // swim and death (caught) animations and per-instance state
    private final Animation<TextureRegion> swimAnimation;
    private final Animation<TextureRegion> caughtAnimation;
    private float stateTime = 0f;
    private boolean dying = false; // true when death/caught animation is playing

    // movement speed (units per second)
    private final float speed;
    // collision radius used for simplified circle-based collision detection.
    // If <= 0, getCollisionRadius() will compute a reasonable default from sprite size.
    private float collisionRadius = -1f;

    public float getWidth() {
        Animation<TextureRegion> a = dying ? caughtAnimation : swimAnimation;
        if (a != null) return a.getKeyFrame(0).getRegionWidth();
        return 0;
    }

    public float getHeight(){
        Animation<TextureRegion> a = dying ? caughtAnimation : swimAnimation;
        if (a != null) return a.getKeyFrame(0).getRegionHeight();
        return 0;
    }

    /**
     * New constructor that accepts an animation (material) and speed.
     * animation may be null for non-animated fish; speed can be 0 for stationary fish.
     */
    public Fish(String name, int hp, int points, int energy, String imagePath,
                Animation<TextureRegion> swimAnimation, Animation<TextureRegion> caughtAnimation, float speed) {
        if (hp <= 0) throw new IllegalArgumentException("hp must be > 0");
        this.name = name;
        this.maxHp = hp;
        this.currentHp = hp;
        this.points = points;
        this.energy = energy;
        this.imagePath = imagePath;
        this.swimAnimation = swimAnimation;
        this.caughtAnimation = caughtAnimation;
        this.speed = speed;
    }

    public String getName() {
        return name;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public int getPoints() {
        return points;
    }

    public int getEnergy() {
        return energy;
    }

    public String getImagePath() {
        return imagePath;
    }

    public float getSpeed() {
        return speed;
    }

    /**
     * Update per-instance animation time (and other per-frame logic if needed).
     */
    public void update(float delta) {
        // Always advance stateTime so both swim and death animations progress when active
        stateTime += delta;
    }

    /**
     * Render this fish at the given coordinates using its animation (if any).
     */
    public void render(SpriteBatch batch, float x, float y, float angle) {
        Animation<TextureRegion> a = dying ? caughtAnimation : swimAnimation;
        if (a != null) {
            TextureRegion frame = a.getKeyFrame(stateTime, !dying); // loop if swimming, no loop if dying
            batch.draw(frame, x, y, frame.getRegionWidth() / 2f, frame.getRegionHeight() / 2f,
                    frame.getRegionWidth(), frame.getRegionHeight(),
                    1f, 1f, angle);
        }
    }

    public void resetAnimation() {
        stateTime = 0f;
    }

    /**
     * Start playing the caught/death animation. Safe to call multiple times.
     */
    public void startDeathAnimation() {
        if (dying) return;
        dying = true;
        stateTime = 0f;
    }

    /**
     * Whether the death/caught animation has finished playing (or no death animation exists).
     */
    public boolean isDeathAnimationFinished() {
        if (!dying) return false;
        if (caughtAnimation == null) return true;
        return caughtAnimation.isAnimationFinished(stateTime);
    }

    public boolean isDying() {
        return dying;
    }

    /**
     * 对鱼造成伤害，返回是否死亡（被捕获）。
     * 如果死亡，currentHp 不会变为负数。
     */
    public boolean takeDamage(int damage) {
        if (damage <= 0) return false;
        currentHp -= damage;
        if (currentHp <= 0) {
            currentHp = 0;
            return true;
        }
        return false;
    }

    /**
     * 当鱼被捕获时，返回捕获奖励（积分与能量）。
     */
    public FishReward getCaptureReward() {
        return new FishReward(points, energy);
    }

    /**
     * 重置血量（例如重生或复用对象）。
     */
    public void reset() {
        this.currentHp = this.maxHp;
    }

    /**
     * Get a collision radius for this fish. If not set explicitly, compute a default
     * value based on the current sprite frame size (smaller than half the min dimension).
     */
    public float getCollisionRadius() {
        if (collisionRadius > 0f) return collisionRadius;
        float w = getWidth();
        float h = getHeight();
        if (w > 0 && h > 0) {
            collisionRadius = Math.min(w, h) * 0.5f * 0.75f; // 75% of half the min dimension
        } else {
            collisionRadius = 12f; // fallback
        }
        return collisionRadius;
    }

    /** Allow explicit tuning of the collision radius for this fish. */
    public void setCollisionRadius(float r) {
        this.collisionRadius = Math.max(0f, r);
    }

    public boolean isDead() {
        return currentHp <= 0;
    }

    @Override
    public String toString() {
        return "Fish{name='" + name + "', hp=" + currentHp + "/" + maxHp +
                ", points=" + points + ", energy=" + energy +
                ", imagePath=" + imagePath + ", speed=" + speed + "}";
    }

    /**
     * 捕获奖励数据结构：包含积分和能量。
     */
    public static class FishReward {
        private final int points;
        private final int energy;

        public FishReward(int points, int energy) {
            this.points = points;
            this.energy = energy;
        }

        public int getPoints() {
            return points;
        }

        public int getEnergy() {
            return energy;
        }

        @Override
        public String toString() {
            return "FishReward{points=" + points + ", energy=" + energy + "}";
        }
    }
}
