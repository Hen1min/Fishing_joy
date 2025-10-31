package io.github.Fishing_joy;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.Fishing_joy.Bullet.Bullet;
import io.github.Fishing_joy.Bullet.Bullet1;
import io.github.Fishing_joy.Bullet.Bullet2;
import io.github.Fishing_joy.Bullet.Bullet3;
import io.github.Fishing_joy.Cannon.Cannon;
import io.github.Fishing_joy.Cannon.Cannon1;
import io.github.Fishing_joy.Cannon.Cannon2;
import io.github.Fishing_joy.Cannon.Cannon3;
import io.github.Fishing_joy.Fish.Fish;
import io.github.Fishing_joy.Fish.Fish1;
import io.github.Fishing_joy.Fish.Fish2;
import io.github.Fishing_joy.Fish.Fish3;
import io.github.Fishing_joy.Fish.Fish4;

import java.util.ArrayList;
import java.util.List;

public class Swim_Animator implements ApplicationListener {

    // Objects used
    SpriteBatch spriteBatch;
    FitViewport viewport;
    Texture bgTexture;
    Texture bottomBarTexture;
    Cannon cannon;
    private int currentCannonLevel = 1;
    // UI icon press state (now managed by Swim_Animator, icons are rendered in UI layer)
    private boolean iconLeftPressed = false;
    private boolean iconRightPressed = false;
    private float iconLeftPressTime = 0f;
    private float iconRightPressTime = 0f;
    private static final float ICON_PRESS_DURATION = 0.12f;
    // UI icon spacing factor (fraction of base spacing) to move icons closer to center
    private static final float ICON_SPACING_FACTOR = 0.6f;
    Texture numbersTexture;
    Texture energyBarTexture;
    TextureRegion[] digitRegions;
    private final List<Bullet> bullets = new ArrayList<>();
    // player state
    private int playerScore = 50; // start with 50 points so player can fire immediately
    private float playerEnergy = 0f; // starting energy (float for smooth decrease)
    BitmapFont font;
    ShapeRenderer shapeRenderer;
    // debug flag to draw collision radii (useful for tuning)
    // enable to draw rotated rectangle collision polygons for bullets and fish
    private boolean debugDrawCollisions = true;

    // --- Berserk mode state ---
    private boolean berserkActive = false;
    private float berserkTimer = 0f;
    private static final float BERSERK_DURATION = 20f; // seconds
    private float berserkOriginalCooldown = 0.5f; // stored to restore after berserk

    // subtitle (centered) animation for entering/exiting berserk
    private String subtitleText = null;
    private float subtitleTimer = 0f;
    // Display subtitle at normal size for subtitleDelay seconds, then scale up over subtitleScaleDuration seconds.
    private float subtitleDelay = 1.0f; // show at normal size for 1s before scaling
    private float subtitleScaleDuration = 1.2f; // how long the scale-up lasts
    private float subtitleMaxScaleIncrease = 1.0f; // scale increase (1.0 means final scale is 1 + 1 = 2)
    private float subtitleDuration = subtitleDelay + subtitleScaleDuration; // total lifetime of subtitle animation

    // A variable for tracking elapsed time used for global operations
    float FishGeneratorGapTime;



    // A simple entity to hold a Fish and its position
    static class FishEntity {
        public Fish fish;
        public float x, y, angle;
        // spawnSide: -1 = left, 1 = right, 0 = top/other
        public int spawnSide;
        public boolean deathHandled; // whether death reward has been given and fish removed

        public FishEntity(Fish fish, float x, float y) {
            this(fish, x, y, 0f, 0);
        }

        public FishEntity(Fish fish, float x, float y, float angle) {
            this(fish, x, y, angle, 0);
        }

        public FishEntity(Fish fish, float x, float y, float angle, int spawnSide) {
            this.fish = fish;
            this.x = x;
            this.y = y;
            this.angle = angle;
            this.spawnSide = spawnSide;
            this.deathHandled = false;
        }
    }

    private final List<FishEntity> fishEntities = new ArrayList<>();

    @Override
    public void create() {

        // Initialize viewport (pixel coordinates by default)
        viewport = new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Load background texture
        bgTexture = new Texture(Gdx.files.internal("game_bg_2_hd.jpg"));

        // Load bottom bar UI texture
        bottomBarTexture = new Texture(Gdx.files.internal("bottom-bar.png"));

        // Load digit spritesheet (number_black.png) - top-to-bottom: 9..0
        numbersTexture = new Texture(Gdx.files.internal("number_black.png"));
        // Load energy bar texture (used to show current energy fill)
        energyBarTexture = new Texture(Gdx.files.internal("energy-bar.png"));
        int numFrameW = numbersTexture.getWidth();
        int numFrameH = numbersTexture.getHeight() / 10; // 10 digits
        digitRegions = new TextureRegion[10];
        // texture coordinates: y measured from bottom, user image arranged top-to-bottom 9->0
        for (int d = 0; d <= 9; d++) {
            // numbers image is arranged top->bottom as 9..0, but TextureRegion y is from bottom,
            // so set region for digit d at y = d * frameHeight (bottom-most is 0)
            int y = d * numFrameH;
            digitRegions[9-d] = new TextureRegion(numbersTexture, 0, y, numFrameW, numFrameH);
        }

        // Load cannon resources and create a centered cannon
        // load common cannon icons and level visuals
        Cannon.loadCommonIcons();
        Cannon1.load();
        Cannon2.load();
        Cannon3.load();
        // Preload bullet texture
        Bullet1.load();
        Bullet2.load();
        Bullet3.load();
        // simple bitmap font for HUD
        font = new BitmapFont();
         // create SpriteBatch before Cannon instance so viewport sizes are usable

        // Load fish type resources

        Fish1.load();
        Fish2.load();
        Fish3.load();
        Fish4.load();

        // Create a few Fish instances at different positions with different speed
        // All Fish1 instances use the type default speed (Fish1.DEFAULT_SPEED internally)
        // Instantiate a SpriteBatch for drawing
        spriteBatch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        // Now safe to create cannon (start at level 1)
        cannon = new Cannon1(viewport.getWorldWidth() / 2f, viewport.getWorldHeight() / 2f);
        currentCannonLevel = 1;
        // Position cannon correctly relative to bottom bar
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        FishGeneratorGapTime = 2.0f;
    }





    public void spawnFish1() {
        Fish fish = Fish1.create();
        float Num = MathUtils.random();
        //fishEntities.add(new FishEntity(fish, -fish.getWidth(), MathUtils.random(0, viewport.getWorldHeight() - fish.getHeight()),MathUtils.random(0,360)));
        if (Num<1/3f) {
            fishEntities.add(new FishEntity(fish, -fish.getWidth(), MathUtils.random(0, viewport.getWorldHeight()), MathUtils.random(-45, 45), -1));//左边出
        } else if (Num < 2/3f) {
            fishEntities.add(new FishEntity(fish,  viewport.getWorldWidth() + fish.getWidth(), MathUtils.random(0,viewport.getWorldHeight()), MathUtils.random(135,225), 1));//右边出
        } else {
            fishEntities.add(new FishEntity(fish, MathUtils.random(0,viewport.getWorldWidth()) , viewport.getWorldHeight(), MathUtils.random(225,305), 0));//上边出
        }
    }

    public void spawnFish2() {
        Fish fish = Fish2.create();
        float Num = MathUtils.random();
        //fishEntities.add(new FishEntity(fish, -fish.getWidth(), MathUtils.random(0, viewport.getWorldHeight() - fish.getHeight()),MathUtils.random(0,360)));
        if (Num<1/3f) {
            fishEntities.add(new FishEntity(fish, -fish.getWidth(), MathUtils.random(0, viewport.getWorldHeight()), MathUtils.random(-45, 45), -1));//左边出
        } else if (Num < 2/3f) {
            fishEntities.add(new FishEntity(fish,  viewport.getWorldWidth() + fish.getWidth(), MathUtils.random(0,viewport.getWorldHeight()), MathUtils.random(135,225), 1));//右边出
        } else {
            fishEntities.add(new FishEntity(fish, MathUtils.random(0,viewport.getWorldWidth()) , viewport.getWorldHeight(), MathUtils.random(225,305), 0));//上边出
        }
    }

    public void spawnFish3() {
        Fish fish = Fish3.create();
        float Num = MathUtils.random();
        //fishEntities.add(new FishEntity(fish, -fish.getWidth(), MathUtils.random(0, viewport.getWorldHeight() - fish.getHeight()),MathUtils.random(0,360)));
        if (Num<1/3f) {
            fishEntities.add(new FishEntity(fish, -fish.getWidth(), MathUtils.random(0, viewport.getWorldHeight()), MathUtils.random(-45, 45), -1));//左边出
        } else if (Num < 2/3f) {
            fishEntities.add(new FishEntity(fish,  viewport.getWorldWidth() + fish.getWidth(), MathUtils.random(0,viewport.getWorldHeight()), MathUtils.random(135,225), 1));//右边出
        } else {
            fishEntities.add(new FishEntity(fish, MathUtils.random(0,viewport.getWorldWidth()) , viewport.getWorldHeight(), MathUtils.random(225,305), 0));//上边出
        }
    }

    public void spawnFish4() {
        Fish fish = Fish4.create();
        float Num = MathUtils.random();
        //fishEntities.add(new FishEntity(fish, -fish.getWidth(), MathUtils.random(0, viewport.getWorldHeight() - fish.getHeight()),MathUtils.random(0,360)));
        if (Num<1/3f) {
            fishEntities.add(new FishEntity(fish, -fish.getWidth(), MathUtils.random(0, viewport.getWorldHeight()), MathUtils.random(-45, 45), -1));//左边出
        } else if (Num < 2/3f) {
            fishEntities.add(new FishEntity(fish,  viewport.getWorldWidth() + fish.getWidth(), MathUtils.random(0,viewport.getWorldHeight()), MathUtils.random(135,225), 1));//右边出
        } else {
            fishEntities.add(new FishEntity(fish, MathUtils.random(0,viewport.getWorldWidth()) , viewport.getWorldHeight(), MathUtils.random(225,305), 0));//上边出
        }
    }




    @Override
    public void resize(int width, int height) {
        if (viewport == null) {
            viewport = new FitViewport(width, height);
        }
        viewport.update(width, height, true);
        // Position cannon horizontally centered and vertically above bottom bar
        if (cannon != null) {
            cannon.setPosition(viewport.getWorldWidth() / 2f + 25f, 12);
        }
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {
    }

    @Override
    public void render() {
        update();

        // clear screen
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // ensure viewport is applied and sprite batch uses its camera
        if (viewport != null) viewport.apply();
        if (spriteBatch != null && viewport != null) spriteBatch.setProjectionMatrix(viewport.getCamera().combined);

        // Draw background and all fish
        if (spriteBatch != null) {
            spriteBatch.begin();
            if (bgTexture != null) {
                // Draw background stretched to viewport size
                spriteBatch.draw(bgTexture, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
            }
            for (FishEntity e : fishEntities) {
                e.fish.render(spriteBatch, e.x, e.y, e.angle);
            }

            // Render cannon on top of fish but below UI


            // Draw bottom bar UI anchored at the bottom (y=0), scaled to 3/4 of viewport width and centered
            if (bottomBarTexture != null) {
                float targetWidth = viewport.getWorldWidth() * 0.75f;
                float scale = targetWidth / (float) bottomBarTexture.getWidth();
                float barHeight = bottomBarTexture.getHeight() * scale;
                float x = (viewport.getWorldWidth() - targetWidth) / 2f;
                spriteBatch.draw(bottomBarTexture, x, 0, targetWidth, barHeight);
            }

            if (cannon != null) {
                cannon.render(spriteBatch);
            }

            // Render upgrade/downgrade icons (detached from cannon internals)
            // Determine spacing based on cannon width
            if (cannon != null) {
                TextureRegion plus = Cannon.getPlusRegion();
                TextureRegion plusDown = Cannon.getPlusDownRegion();
                TextureRegion minus = Cannon.getMinusRegion();
                TextureRegion minusDown = Cannon.getMinusDownRegion();
                float baseSpacing = (cannon.getWidth() > 0) ? cannon.getWidth() + 8f : 48f;
                float spacing = baseSpacing * ICON_SPACING_FACTOR; // move icons closer
                float cx = cannon.getX();
                float cy = cannon.getY();
                // left icon: now show MINUS on the left
                TextureRegion leftToDraw = (iconLeftPressed && minusDown != null) ? minusDown : minus;
                if (leftToDraw != null) {
                    float lW = leftToDraw.getRegionWidth();
                    float lH = leftToDraw.getRegionHeight();
                    float leftX = cx - spacing - lW / 2f;
                    float leftY = cy - lH / 2f;
                    spriteBatch.draw(leftToDraw, leftX, leftY);
                }
                // right icon: now show PLUS on the right
                TextureRegion rightToDraw = (iconRightPressed && plusDown != null) ? plusDown : plus;
                if (rightToDraw != null) {
                    float rW = rightToDraw.getRegionWidth();
                    float rH = rightToDraw.getRegionHeight();
                    float rightX = cx + spacing - rW / 2f;
                    float rightY = cy - rH / 2f;
                    spriteBatch.draw(rightToDraw, rightX, rightY);
                }
            }

            // Render bullets on top of cannon/fish
            if (!bullets.isEmpty()) {
                for (Bullet b : bullets) {
                    b.render(spriteBatch);
                }
            }

            // HUD: score and energy
            if (font != null) {
                // draw score as six digits using number_black.png, but render smaller (scale down)
                String scoreStr = String.format("%06d", Math.max(0, playerScore));
                int digitW = digitRegions[0].getRegionWidth();
                int digitH = digitRegions[0].getRegionHeight();
                float startX = 92f;
                float topY = 17f; // top padding
                // scale factor to reduce digit size (e.g. 0.7 = 70% of original)
                float digitScale = 0.62f;
                float scaledW = digitW * digitScale;
                float scaledH = digitH * digitScale;
                // small extra spacing between digits (scaled with UI)
                float digitSpacing = 2f;
                float drawY = topY - scaledH; // spriteBatch.draw uses bottom-left y
                for (int i = 0; i < 6; i++) {
                    int digit = scoreStr.charAt(i) - '0';
                    TextureRegion reg = digitRegions[digit];
                    float x = startX + i * (scaledW + digitSpacing);
                    spriteBatch.draw(reg, x, drawY, scaledW, scaledH);
                }

                // Draw energy bar fill using energyBarTexture (range 0..100)
                if (energyBarTexture != null) {
                    float digitsTotalW = 6 * scaledW + 5 * digitSpacing;
                    float barX = startX + digitsTotalW + 12f + 232.5f; // position right after digits
                    float barHeight = scaledH * 0.8f ;
                    float barY = drawY + (scaledH - barHeight) / 2f + 2f;
                    // scale texture to desired barHeight
                    float texH = energyBarTexture.getHeight();
                    float scaleBar = barHeight / texH;
                     // compute fill ratio using 0..100 range (keep playerEnergy initial value unchanged)
                     float fillRatio = MathUtils.clamp(playerEnergy / 100f, 0f, 1f);
                     int srcW = Math.max(1, (int)(energyBarTexture.getWidth() * fillRatio));
                     TextureRegion fillRegion = new TextureRegion(energyBarTexture, 0, 0, srcW, energyBarTexture.getHeight());
                     spriteBatch.draw(fillRegion, barX, barY, srcW * scaleBar, barHeight);
                 }

                // Draw berserk subtitle centered, scaling up and fading out
                if (subtitleText != null) {
                    // subtitleTimer is incremented in update(); here compute where we are in the sequence
                    float t = subtitleTimer;
                    // Determine scale progress: 0 until subtitleDelay, then 0..1 over subtitleScaleDuration
                    float scaleProgress;
                    if (t <= subtitleDelay) scaleProgress = 0f;
                    else scaleProgress = Math.min(1f, (t - subtitleDelay) / Math.max(0.0001f, subtitleScaleDuration));
                    // scale goes from 1.0 -> 1.0 + subtitleMaxScaleIncrease
                    float scale = 1f + subtitleMaxScaleIncrease * scaleProgress;
                    // alpha fades only during scaling (from 1 -> 0)
                    float alpha = 1f - scaleProgress;

                    // Draw multiline text with per-character spacing increased during scaling to avoid overlap
                    String[] lines = subtitleText.split("\\n");
                    float cx = viewport.getWorldWidth() / 2f;
                    float cy = viewport.getWorldHeight() / 2f;

                    // base extra spacing between characters (in pixels at scale 1); will grow with scaleProgress
                    float baseExtraSpacing = 1.5f; // tweakable
                    float extraSpacing = baseExtraSpacing * (1f + 1.5f * scaleProgress); // increases as scaling proceeds

                    // compute scaled heights and vertical layout
                    GlyphLayout tmp = new GlyphLayout();
                    tmp.setText(font, "M");
                    float singleLineHeight = tmp.height * scale;
                    // vertical spacing between lines
                    float lineGap = singleLineHeight * 0.2f;

                    // compute total height for all lines to position them centered
                    float totalHeight = lines.length * singleLineHeight + (lines.length - 1) * lineGap;
                    float startY = cy + (totalHeight / 2f) - (singleLineHeight * 0.25f);

                    // Save old font state
                    float oldScaleX = font.getData().scaleX;
                    float oldScaleY = font.getData().scaleY;
                    font.getData().setScale(scale);
                    font.setColor(1f, 0.5f, 0f, alpha); // orange color

                    for (int li = 0; li < lines.length; li++) {
                        String line = lines[li];
                        // compute width of line with per-char spacing
                        float lineWidth = 0f;
                        for (int ci = 0; ci < line.length(); ci++) {
                            char ch = line.charAt(ci);
                            tmp.setText(font, String.valueOf(ch));
                            lineWidth += tmp.width;
                            if (ci < line.length() - 1) lineWidth += extraSpacing;
                        }
                        float lineX = cx - lineWidth / 2f;
                        float lineY = startY - li * (singleLineHeight + lineGap);

                        // draw characters one-by-one with extra spacing
                        float penX = lineX;
                        for (int ci = 0; ci < line.length(); ci++) {
                            String chs = String.valueOf(line.charAt(ci));
                            tmp.setText(font, chs);
                            font.draw(spriteBatch, chs, penX, lineY);
                            penX += tmp.width + extraSpacing;
                        }
                    }

                    // restore font
                    font.getData().setScale(oldScaleX, oldScaleY);
                    font.setColor(1f, 1f, 1f, 1f);
                }

            }
            spriteBatch.end();
            // Optional debug drawing: draw collision circles for bullets and fish
            if (debugDrawCollisions && shapeRenderer != null && viewport != null) {
                // use camera projection so shapes align with sprites
                shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
                shapeRenderer.begin(com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType.Line);
                // bullets: red
                shapeRenderer.setColor(1f, 0f, 0f, 1f);
                for (Bullet b : bullets) {
                    // show only flying bullets
                    if (b.isCaptured()) continue;
                    float bx = b.getX();
                    float by = b.getY();
                    float bw = b.getWidth();
                    float bh = b.getHeight();
                    float bRotation = b.getAngleDeg() - 80f; // match rendering rotation
                    float[] bPoly = CollisionUtil.buildRectPoly(bx, by, bw, bh, bRotation);
                    for (int i = 0; i < bPoly.length; i += 2) {
                        int ni = (i + 2) % bPoly.length;
                        shapeRenderer.line(bPoly[i], bPoly[i+1], bPoly[ni], bPoly[ni+1]);
                    }
                }
                // fish: green
                shapeRenderer.setColor(0f, 1f, 0f, 1f);
                for (FishEntity fe : fishEntities) {
                    float fw = fe.fish.getWidth();
                    float fh = fe.fish.getHeight();
                    float fCenterX = fe.x + fw / 2f;
                    float fCenterY = fe.y + fh / 2f;
                    float fRotation = fe.angle;
                    float[] fPoly = CollisionUtil.buildRectPoly(fCenterX, fCenterY, fw, fh, fRotation);
                    for (int i = 0; i < fPoly.length; i += 2) {
                        int ni = (i + 2) % fPoly.length;
                        shapeRenderer.line(fPoly[i], fPoly[i+1], fPoly[ni], fPoly[ni+1]);
                    }
                }
                shapeRenderer.end();
            }
        }
    }


    public void update(){
        float delta = Gdx.graphics.getDeltaTime();
        // update cannon cooldown timer so canFire()/tryFire() work correctly
        if (cannon != null) cannon.updateFireCooldown(delta);
        FishGeneratorGapTime += delta;
        if (FishGeneratorGapTime >= 1.0f) {
            // Spawn a new fish at a random vertical position on the left side
            float ran = MathUtils.random();

            if (ran < 0.4f){
                spawnFish1();
            }else if(ran < 0.7f){
                spawnFish2();
            }else if(ran < 0.9f){
                spawnFish3();
            }else{
                spawnFish4();
            }
            FishGeneratorGapTime = 0f;
        }

        // handle input: on click rotate cannon to point to mouse and trigger one-shot animation
        if (Gdx.input.justTouched()) {
            // get screen coords and convert to world coords
            int sx = Gdx.input.getX();
            int sy = Gdx.input.getY();
            Vector3 world = new Vector3(sx, sy, 0);
            if (viewport != null) viewport.unproject(world);
            if (cannon != null) {
                // if clicked on left/right icons (UI layer), handle press state and swap level
                // Left icon is MINUS: perform downgrade
                if (isPointInLeftIcon(world.x, world.y)) {
                     iconLeftPressed = true;
                     iconLeftPressTime = 0f;
                     int levels = 3;
                     currentCannonLevel = (currentCannonLevel - 2 + levels) % levels + 1; // downgrade
                     float ang = cannon.getAngleDeg();
                     float cx = cannon.getX();
                     float cy = cannon.getY();
                     Cannon newC;
                     if (currentCannonLevel == 1) newC = new Cannon1(cx, cy);
                     else if (currentCannonLevel == 2) newC = new Cannon2(cx, cy);
                     else newC = new Cannon3(cx, cy);
                    // if berserk active, ensure new cannon has berserk cooldown
                    if (berserkActive) {
                         newC.setFireCooldown(0.25f);
                         newC.resetFireTimer();
                     }
                     newC.setAngleDeg(ang);
                     cannon = newC;
                     return;
                 }
                // Right icon is PLUS: perform upgrade
                if (isPointInRightIcon(world.x, world.y)) {
                     iconRightPressed = true;
                     iconRightPressTime = 0f;
                     int levels = 3;
                     currentCannonLevel = (currentCannonLevel % levels) + 1; // upgrade
                     float ang = cannon.getAngleDeg();
                     float cx = cannon.getX();
                     float cy = cannon.getY();

                     Cannon newC;
                     if (currentCannonLevel == 1) newC = new Cannon1(cx, cy);
                     else if (currentCannonLevel == 2) newC = new Cannon2(cx, cy);
                     else newC = new Cannon3(cx, cy);

                     if (berserkActive) {
                         newC.setFireCooldown(0.25f);
                         newC.resetFireTimer();
                     }
                     newC.setAngleDeg(ang);
                     cannon = newC;
                     return;
                 }

                float dx = world.x - (cannon.getX());
                float dy = world.y - (cannon.getY());
                float ang = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees;
                cannon.setAngleDeg(ang - 90);
                // Attempt to fire respecting cannon cooldown. Only spawn bullet and deduct score when tryFire() succeeds.
                if (cannon.tryFire()) {
                    cannon.triggerAnimation();
                    float[] muzzle = cannon.getMuzzlePosition();
                    int level = cannon.getLevel();
                    if (level == 1) {
                        int cost = 4;
                        int effectiveCost = berserkActive ? (cost / 2) : cost; // halve cost during berserk (floor)
                        if (playerScore >= effectiveCost) {
                            playerScore -= effectiveCost;
                            Bullet b = new Bullet1(muzzle[0], muzzle[1], cannon.getAngleDeg() + 90f);
                            bullets.add(b);
                        } else {
                            Gdx.app.log("Game", "Not enough points to fire: need " + effectiveCost + ", have " + playerScore);
                        }
                    } else if (level == 2) {
                        int cost = 6;
                        int effectiveCost = berserkActive ? (cost / 2) : cost;
                        if (playerScore >= effectiveCost) {
                            playerScore -= effectiveCost;
                            Bullet b = new Bullet2(muzzle[0], muzzle[1], cannon.getAngleDeg() + 90f);
                            bullets.add(b);
                        } else {
                            Gdx.app.log("Game", "Not enough points to fire: need " + effectiveCost + ", have " + playerScore);
                        }
                    } else { // level == 3
                        int cost = 10;
                        int effectiveCost = berserkActive ? (cost / 2) : cost;
                        if (playerScore >= effectiveCost) {
                            playerScore -= effectiveCost;
                            Bullet b = new Bullet3(muzzle[0], muzzle[1], cannon.getAngleDeg() + 90f);
                            bullets.add(b);
                        } else {
                            Gdx.app.log("Game", "Not enough points to fire: need " + effectiveCost + ", have " + playerScore);
                        }
                    }
                } else {
                    Gdx.app.log("Game", "Cannon cooling down: cannot fire yet");
                }
             }
         }

        for (FishEntity e : fishEntities) {
            e.fish.update(delta);
            // movement uses each fish's configured speed; do not move dying fish so death animation stays in place
            if (!e.fish.isDying()) {
                e.x += e.fish.getSpeed() * delta * MathUtils.cosDeg(e.angle);
                e.y += e.fish.getSpeed() * delta * MathUtils.sinDeg(e.angle);
            }
        }

        // update cannon animation
        if (cannon != null) cannon.update(delta);

        // update icon press timers (UI layer)
        if (iconLeftPressed) {
            iconLeftPressTime += delta;
            if (iconLeftPressTime >= ICON_PRESS_DURATION) iconLeftPressed = false;
        }
        if (iconRightPressed) {
            iconRightPressTime += delta;
            if (iconRightPressTime >= ICON_PRESS_DURATION) iconRightPressed = false;
        }

        // update bullets
        for (Bullet b : bullets) b.update(delta);

        // debug toggle: press D to toggle collision debug drawing
        if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            debugDrawCollisions = !debugDrawCollisions;
        }

        // check collisions: flying bullets vs fish
        // collision detection (AABB) - iterate bullets and fish (enhanced for)
        for (Bullet b : bullets) {
            // only consider bullets that are still flying
            if (b.isCaptured()) continue;
            float bx = b.getX();
            float by = b.getY();
            float bw = b.getWidth();
            float bh = b.getHeight();
            for (FishEntity fe : fishEntities) {
                if (fe.fish.isDying()) continue; // don't collide with fish already in death animation
                // Use SAT polygon intersection on the drawn rectangles (pixel-aligned sprite boxes rotated by their draw angles)
                // Bullet stores its center at (bx,by) and is drawn rotated by (angleDeg - 80) in its render method, so use the same.
                float bRotation = b.getAngleDeg() - 80f;
                float[] bPoly = CollisionUtil.buildRectPoly(bx, by, bw, bh, bRotation);
                // Fish is drawn with batch.draw(..., x, y, width/2, height/2, width, height, 1,1, angle)
                // where x,y are the bottom-left corner. Build polygon using the sprite center.
                float fw = fe.fish.getWidth();
                float fh = fe.fish.getHeight();
                float fCenterX = fe.x + fw / 2f;
                float fCenterY = fe.y + fh / 2f;
                float fRotation = fe.angle;
                float[] fPoly = CollisionUtil.buildRectPoly(fCenterX, fCenterY, fw, fh, fRotation);

                if (CollisionUtil.polygonsIntersect(bPoly, fPoly)) {
                    int dmg = b.getDamage();
                    boolean died = fe.fish.takeDamage(dmg);
                    Gdx.app.log("Game","Bullet hit fish: dmg=" + dmg + " hpRemaining=" + fe.fish.getCurrentHp());
                    // always convert the bullet into a web (capture) regardless of fish death
                    b.capture();
                    if (died) {
                        // fish died: start death animation and defer awarding/removal until animation finishes
                        fe.fish.startDeathAnimation();
                        fe.deathHandled = true;
                        Gdx.app.log("Game","Fish died — playing death animation");
                    } else {
                        // fish still alive: reset its animation to show it was hit
                        fe.fish.resetAnimation();
                    }
                }
            }
        }

        // remove bullets outside viewport bounds
        bullets.removeIf(b -> b.getX() < -b.getWidth() || b.getX() > viewport.getWorldWidth() + b.getWidth() || b.getY() < -b.getHeight() || b.getY() > viewport.getWorldHeight() + b.getHeight());

        // remove bullets whose hit animation finished
        bullets.removeIf(Bullet::isDone);

        // Handle fish death animations (remove fish and award points/energy)
        for (int i = fishEntities.size() - 1; i >= 0; i--) {
            FishEntity fe = fishEntities.get(i);
            if (fe.deathHandled && fe.fish.isDeathAnimationFinished()) {
                // award points/energy based on fish type now that death animation finished
                Fish.FishReward reward = fe.fish.getCaptureReward();
                int points = reward.getPoints();
                if (berserkActive) points = points * 2; // double points during berserk
                playerScore += points;
                int energyGain = reward.getEnergy();
                if (!berserkActive) {
                    playerEnergy += energyGain;
                    // cap energy to max 100
                    if (playerEnergy > 100f) playerEnergy = 100f;
                }
                Gdx.app.log("Game","Fish death animation finished: +" + points + " pts, +" + (berserkActive ? 0 : energyGain) + " energy");
                // If energy reached full and berserk not active, trigger berserk
                if (!berserkActive && playerEnergy >= 100f) {
                    startBerserk();
                }
                fishEntities.remove(i);
            }
        }

        // remove fish that moved off-screen — but keep dying fish until their death animation finishes
        fishEntities.removeIf(e -> !e.fish.isDying() && (e.x > viewport.getWorldWidth() + 2 * e.fish.getWidth() || e.x < -3 * e.fish.getWidth() || e.y > viewport.getWorldHeight() + 2 * e.fish.getHeight() || e.y < -3 * e.fish.getHeight()));

        // ---- Berserk per-frame updates ----
        // subtitle timing
        if (subtitleText != null) {
            subtitleTimer += delta;
            if (subtitleTimer >= subtitleDuration) subtitleText = null;
        }

        if (berserkActive) {
            berserkTimer += delta;
            // uniformly decrease energy from 100 -> 0 over BERSERK_DURATION seconds
            float decreasePerSec = 100f / BERSERK_DURATION;
            playerEnergy = Math.max(0f, playerEnergy - decreasePerSec * delta);
            // end berserk when timer completes or energy reaches 0
            if (berserkTimer >= BERSERK_DURATION || playerEnergy <= 0f) {
                endBerserk();
            }
        }
     }

    @Override
    public void dispose() { // SpriteBatches and Textures must always be disposed
        if (spriteBatch != null) spriteBatch.dispose();
        if (bgTexture != null) bgTexture.dispose();
        if (bottomBarTexture != null) bottomBarTexture.dispose();
        if (font != null) font.dispose();
        if (numbersTexture != null) numbersTexture.dispose();
        if (energyBarTexture != null) energyBarTexture.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        // Dispose shared fish resources
        Fish2.dispose();
        // Dispose cannon resources
        Cannon.disposeCommonIcons();
        Cannon1.disposeStatic();
        Cannon2.disposeStatic();
        Cannon3.disposeStatic();
        // Dispose bullet textures
        Bullet1.disposeTexture();
        Bullet2.disposeTexture();
        Bullet3.disposeTexture();
    }

    // UI-layer hit tests for icons (compute positions from cannon position & icon regions)
    private boolean isPointInLeftIcon(float wx, float wy) {
        TextureRegion reg = (Cannon.getMinusRegion() != null) ? Cannon.getMinusRegion() : Cannon.getMinusDownRegion();
        if (reg == null || cannon == null) return false;
        float baseSpacing = (cannon.getWidth() > 0) ? cannon.getWidth() + 8f : 48f;
        float spacing = baseSpacing * ICON_SPACING_FACTOR;
        float lW = reg.getRegionWidth();
        float lH = reg.getRegionHeight();
        float leftX = cannon.getX() - spacing - lW / 2f;
        float leftY = cannon.getY() - lH / 2f;
        return wx >= leftX && wx <= leftX + lW && wy >= leftY && wy <= leftY + lH;
    }

    private boolean isPointInRightIcon(float wx, float wy) {
        TextureRegion reg = (Cannon.getPlusRegion() != null) ? Cannon.getPlusRegion() : Cannon.getPlusDownRegion();
        if (reg == null || cannon == null) return false;
        float baseSpacing = (cannon.getWidth() > 0) ? cannon.getWidth() + 8f : 48f;
        float spacing = baseSpacing * ICON_SPACING_FACTOR;
        float rW = reg.getRegionWidth();
        float rH = reg.getRegionHeight();
        float rightX = cannon.getX() + spacing - rW / 2f;
        float rightY = cannon.getY() - rH / 2f;
        return wx >= rightX && wx <= rightX + rW && wy >= rightY && wy <= rightY + rH;
    }

    // --- Berserk helpers ---
    private void startBerserk() {
        if (berserkActive) return;
        berserkActive = true;
        berserkTimer = 0f;
        // store and set cannon cooldown
        if (cannon != null) {
            berserkOriginalCooldown = cannon.getFireCooldown();
            cannon.setFireCooldown(0.25f);
            cannon.resetFireTimer();
        }
        // Start subtitle animation
        subtitleText = "ENTER BERSERK MODE - 20s\nCOSTS HALVED\nPOINTS DOUBLED";
        // keep subtitle at normal size for 1s, then scale up over subtitleScaleDuration
        subtitleTimer = 0f;
        subtitleDelay = 1.0f;
        subtitleScaleDuration = 1.2f;
        subtitleMaxScaleIncrease = 1.0f; // final scale = 2.0
        subtitleDuration = subtitleDelay + subtitleScaleDuration;
        // ensure energy is full (start decreasing from 100)
        playerEnergy = 100f;
    }

    private void endBerserk() {
         if (!berserkActive) return;
         berserkActive = false;
         berserkTimer = 0f;
         // restore cannon cooldown
         if (cannon != null) {
             cannon.setFireCooldown(berserkOriginalCooldown);
             cannon.resetFireTimer();
         }
         // show exit subtitle briefly
         subtitleText = "EXIT BERSERK MODE"; // show Chinese text for end
         subtitleTimer = 0f;
         // keep the end text visible for 0.5s before any scaling/fade
         subtitleDelay = 0.5f;
         subtitleScaleDuration = 0.6f; // then scale/fade over 0.6s
         subtitleMaxScaleIncrease = 0.3f; // small scale-up
         subtitleDuration = subtitleDelay + subtitleScaleDuration;
     }
 }
