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
import io.github.Fishing_joy.Bullet.Bullet4;
import io.github.Fishing_joy.Bullet.Bullet5;
import io.github.Fishing_joy.Bullet.Bullet6;
import io.github.Fishing_joy.Bullet.Bullet7;
import io.github.Fishing_joy.Cannon.Cannon;
import io.github.Fishing_joy.Cannon.Cannon1;
import io.github.Fishing_joy.Cannon.Cannon2;
import io.github.Fishing_joy.Cannon.Cannon3;
import io.github.Fishing_joy.Cannon.Cannon4;
import io.github.Fishing_joy.Cannon.Cannon5;
import io.github.Fishing_joy.Cannon.Cannon6;
import io.github.Fishing_joy.Cannon.Cannon7;
import io.github.Fishing_joy.Fish.Fish;
import io.github.Fishing_joy.Fish.Fish1;
import io.github.Fishing_joy.Fish.Fish2;
import io.github.Fishing_joy.Fish.Fish3;
import io.github.Fishing_joy.Fish.Fish4;
import io.github.Fishing_joy.Fish.Fish5;
import io.github.Fishing_joy.Fish.Fish6;
import io.github.Fishing_joy.Fish.Fish7;
import io.github.Fishing_joy.Fish.Fish8;
import io.github.Fishing_joy.util.MultiCircleCollision;

import java.util.ArrayList;
import java.util.List;

public class Swim_Animator implements ApplicationListener {

    // Objects used
    private int playerScore = 1000; // start with 1000 points so player can fire immediately
    private float playerEnergy = 0f; // starting energy (float for smooth decrease)

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

    BitmapFont font;
    ShapeRenderer shapeRenderer;
    // debug flag to draw collision radii (useful for tuning)
    // enable to draw rotated rectangle collision polygons for bullets and fish
    private boolean debugDrawCollisions = false;

    // --- Berserk mode state ---
    private boolean berserkActive = false;
    private float berserkTimer = 0f;
    private static final float BERSERK_DURATION = 20f; // seconds
    private float berserkOriginalCooldown = 0.5f; // stored to restore after berserk

    // --- Stun (定身) state triggered by Fish8 ---
    private boolean stunActive = false;
    private float stunTimer = 0f;
    private static final float STUN_DURATION = 2.0f; // seconds fish are frozen

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

    // Start screen state and button geometry
    private boolean started = false; // whether the main game has started
    private float startBtnX = 0f;
    private float startBtnY = 0f;
    private float startBtnW = 300f;
    private float startBtnH = 90f;// START button uses a static rounded rectangle (no animated gradient)
    // (previous animated gradient variables removed)

    // Track START button touch/press state so we can render a narrow shadow when pressed
    private boolean startTouchActive = false; // true if touch started inside START button
    private boolean startPressed = false;     // true while touch is held inside START button

    // Help (简介) button geometry + state (placed under START button on the start screen)
    private float helpBtnX = 0f;
    private float helpBtnY = 0f;
    private float helpBtnW = 80f;
    private float helpBtnH = 80f;
    private boolean helpTouchActive = false;
    private boolean helpPressed = false;

    // Whether the help/info overlay is currently visible
    private boolean showHelpOverlay = false;

    // Pause state and UI for in-game pause/continue toggle (shown when started)
    private boolean paused = false;
    private float pauseBtnW = 110f;
    private float pauseBtnH = 42f;
    private float pauseBtnX = 0f;
    private float pauseBtnY = 0f;
    // pressed visual states for top-left/back and top-right/pause buttons
    private boolean backPressed = false;
    private boolean pausePressed = false;

    // Back button (top-left) to return to start screen and stop music
    private float backBtnW = 110f;
    private float backBtnH = 42f;
    private float backBtnX = 10f;
    private float backBtnY = 10f;

    // Prototypes for fish info display in the help overlay
    private final java.util.List<Fish> helpFishPrototypes = new java.util.ArrayList<>();

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
        Cannon4.load();
        Cannon5.load();
        Cannon6.load();
        Cannon7.load();

        // Preload bullet texture
        Bullet1.load();
        Bullet2.load();
        Bullet3.load();
        Bullet4.load();
        Bullet5.load();
        Bullet6.load();
        Bullet7.load();
        // simple bitmap font for HUD
        // Use LibGDX built-in BitmapFont instead of FreeType
        try {
            // If you have a .fnt file, you can load it with new BitmapFont(Gdx.files.internal("fonts/myfont.fnt"));
            font = new BitmapFont();
            font.getData().setScale(1f);
            font.getCache().setUseIntegerPositions(true);
            for (com.badlogic.gdx.graphics.g2d.TextureRegion region : font.getRegions()) {
                com.badlogic.gdx.graphics.Texture tex = region.getTexture();
                if (tex != null) tex.setFilter(com.badlogic.gdx.graphics.Texture.TextureFilter.Nearest, com.badlogic.gdx.graphics.Texture.TextureFilter.Nearest);
            }
        } catch (Exception e) {
            // fallback to default font if anything goes wrong
            font = new BitmapFont();
        }

        // Load fish type resources

        Fish1.load();
        Fish2.load();
        Fish3.load();
        Fish4.load();
        Fish5.load();
        Fish6.load();
        Fish7.load();
        Fish8.load();

        // Populate help prototypes for the info overlay (name, hp, points, energy)
        helpFishPrototypes.clear();
        helpFishPrototypes.add(Fish1.create());
        helpFishPrototypes.add(Fish2.create());
        helpFishPrototypes.add(Fish3.create());
        helpFishPrototypes.add(Fish4.create());
        helpFishPrototypes.add(Fish5.create());
        helpFishPrototypes.add(Fish6.create());
        helpFishPrototypes.add(Fish7.create());
        helpFishPrototypes.add(Fish8.create());

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

        // Start screen initially shown; started=false
        started = false;
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

    public void spawnFish5() {
        Fish fish = Fish5.create();
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

    public void spawnFish6() {
        Fish fish = Fish6.create();
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

    public void spawnFish7() {
        Fish fish = Fish7.create();
        float Num = MathUtils.random();
        //fishEntities.add(new FishEntity(fish, -fish.getWidth(), MathUtils.random(0, viewport.getWorldHeight() - fish.getHeight()),MathUtils.random(0,360)));
        if (Num<1/3f) {
            // keep render angle as originally randomized; movement will be adjusted in update()
            fishEntities.add(new FishEntity(fish, -fish.getWidth(), MathUtils.random(0, viewport.getWorldHeight()), MathUtils.random(-45, 45), -1));//左边出
        } else if (Num < 2/3f) {
            fishEntities.add(new FishEntity(fish,  viewport.getWorldWidth() + fish.getWidth(), MathUtils.random(0,viewport.getWorldHeight()), MathUtils.random(135,225), 1));//右边出
        } else {
            fishEntities.add(new FishEntity(fish, MathUtils.random(0,viewport.getWorldWidth()) , viewport.getWorldHeight(), MathUtils.random(225,305), 0));//上边出
        }
    }

    public void spawnFish8() {
        Fish fish = Fish8.create();
        float Num = MathUtils.random();
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
        // compute start button geometry centered in world coordinates
        // compute base start button size (original behavior)
        float baseStartW = Math.min(480f, viewport.getWorldWidth() * 0.32f);
        float baseStartH = Math.min(140f, viewport.getWorldHeight() * 0.12f);
        // scale down to 70% as requested, and keep it centered
        startBtnW = baseStartW * 0.6f;
        startBtnH = baseStartH * 0.6f;
        startBtnX = (viewport.getWorldWidth() - startBtnW) / 2f;
        startBtnY = (viewport.getWorldHeight() - startBtnH) / 2f;

        // compute help button geometry: make it twice as large and place it further below the START button
        // Start from previous sizing but scale up by 2x (and cap at a larger max to avoid oversize)
        helpBtnW = Math.min(160f, startBtnW * 0.6f); // doubled from 0.25 -> 0.5 fraction
        helpBtnH = Math.min(160f, startBtnH * 0.6f);
        helpBtnX = startBtnX + (startBtnW - helpBtnW) / 2f;
        // increase vertical padding to ensure no overlap with START; use 20px padding
        helpBtnY = startBtnY - helpBtnH - 10f; // increased padding

        // compute pause button geometry (fixed size) anchored to top-right corner
        // Make pause button even smaller per user's request
        pauseBtnW = Math.min(70f, viewport.getWorldWidth() * 0.09f);
        pauseBtnH = Math.min(28f, viewport.getWorldHeight() * 0.06f);
        pauseBtnX = viewport.getWorldWidth() - pauseBtnW - 6f; // 6 pixels padding from right
        pauseBtnY = viewport.getWorldHeight() - pauseBtnH - 6f; // 6 pixels padding from top

        // compute back button geometry (fixed size) anchored to top-left corner
        // Make back button match the smaller pause button sizing
        backBtnW = Math.min(70f, viewport.getWorldWidth() * 0.09f);
        backBtnH = Math.min(28f, viewport.getWorldHeight() * 0.06f);
        backBtnX = 6f; // fixed padding from left
        backBtnY = viewport.getWorldHeight() - backBtnH - 6f; // 6 pixels padding from top
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

            // If the game hasn't started yet, draw START button overlay and skip the main game rendering
            if (!started) {
                spriteBatch.end();
                // draw a radial gradient button by rendering concentric filled circles with interpolated colors
                if (shapeRenderer != null) {
                    shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
                    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

                    float cx = startBtnX + startBtnW / 2f;
                    float cy = startBtnY + startBtnH / 2f;
                    float outerW = startBtnW;
                    float outerH = startBtnH;

                    // If the start button is pressed, draw a narrow semi-transparent shadow rounded rect slightly outside the button
                    float baseCorner = Math.min(outerW, outerH) * 0.18f;
                    if (startPressed) {
                        float pad = Math.min(8f, Math.min(outerW, outerH) * 0.06f); // narrow padding for shadow
                        float sw = outerW + pad * 2f;
                        float sh = outerH + pad * 2f;
                        float sx = cx - sw / 2f;
                        float sy = cy - sh / 2f;
                        float cornerShadow = Math.min(baseCorner + pad, Math.min(sw, sh) * 0.18f);
                        // shadow color: semi-transparent black
                        shapeRenderer.setColor(0f, 0f, 0f, 0.45f);
                        // draw rounded rect: center rect + sides + corner circles (single pass)
                        float innerW = Math.max(0f, sw - 2f * cornerShadow);
                        float innerH = Math.max(0f, sh - 2f * cornerShadow);
                        if (innerW > 0f && innerH > 0f) shapeRenderer.rect(sx + cornerShadow, sy + cornerShadow, innerW, innerH);
                        if (innerW > 0f) {
                            shapeRenderer.rect(sx + cornerShadow, sy, innerW, cornerShadow);
                            shapeRenderer.rect(sx + cornerShadow, sy + sh - cornerShadow, innerW, cornerShadow);
                        }
                        if (innerH > 0f) {
                            shapeRenderer.rect(sx, sy + cornerShadow, cornerShadow, innerH);
                            shapeRenderer.rect(sx + sw - cornerShadow, sy + cornerShadow, cornerShadow, innerH);
                        }
                        if (cornerShadow > 0f) {
                            shapeRenderer.circle(sx + cornerShadow, sy + cornerShadow, cornerShadow);
                            shapeRenderer.circle(sx + sw - cornerShadow, sy + cornerShadow, cornerShadow);
                            shapeRenderer.circle(sx + cornerShadow, sy + sh - cornerShadow, cornerShadow);
                            shapeRenderer.circle(sx + sw - cornerShadow, sy + sh - cornerShadow, cornerShadow);
                        }
                    }

                    // Draw START button using the same rounded-gradient button helper so its color/appearance behaves like the BACK button
                    drawRoundedGradientButton(shapeRenderer, startBtnX, startBtnY, startBtnW, startBtnH, 1f, 0.65f, 0f, startPressed);

                    // Draw help button as a circular gradient button (matching START color/style but circular)
                    float hx = helpBtnX + helpBtnW * 0.5f;
                    float hy = helpBtnY + helpBtnH * 0.5f;
                    float hr = Math.min(helpBtnW, helpBtnH) * 0.5f;
                    drawCircularGradientButton(shapeRenderer, hx, hy, hr, 1f, 0.65f, 0f, helpPressed);

                     shapeRenderer.end();
                }

                // draw START text (use black and visually bolder by drawing multiple slightly-offset passes)
                spriteBatch.begin();
                if (font != null) {
                    String text = "ENDLESS MODE";
                    // choose scale based on button height; slightly larger than before to help bold look
                    float scaleVal = Math.min(1.8f, startBtnH / 40f);
                    font.getData().setScale(scaleVal);

                    // compute total width/height using GlyphLayout (single-pass rendering)
                    GlyphLayout gl = new GlyphLayout(font, text);
                    float txStart = startBtnX + (startBtnW - gl.width) / 2f;
                    float ty = startBtnY + (startBtnH + gl.height) / 2f;

                    // black color for START text
                    font.setColor(0f, 0f, 0f, 1f);
                    // Draw the text once (no multiple offset passes)
                    font.draw(spriteBatch, text, txStart, ty);

                    // Draw help button label '?' centered atop the circular button
                    String q = "?";
                    // choose a base scale for the help label, then enlarge appropriately
                    float baseQScale = Math.min(1f, helpBtnH / 60f);
                    float qScale = baseQScale * 3.0f; // enlarge for visibility
                     font.getData().setScale(qScale);
                     font.setColor(0f, 0f, 0f, 1f);
                     // center '?' within the circular help button using GlyphLayout (width/height measured at current scale)
                     GlyphLayout qGl = new GlyphLayout(font, q);
                     float qX = helpBtnX + helpBtnW * 0.5f - qGl.width / 2f;
                     float qY = helpBtnY + helpBtnH * 0.5f + qGl.height / 2f;
                     font.draw(spriteBatch, q, qX, qY);

                     // restore font scale and color
                     font.getData().setScale(1f);
                     font.setColor(1f, 1f, 1f, 1f);
                 }
                 spriteBatch.end();

                 // If help overlay is visible, draw it on top and intercept input
                 if (showHelpOverlay) {
                     if (shapeRenderer != null) {
                         shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
                         shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                         // dim background
                         shapeRenderer.setColor(0f,0f,0f,0.6f);
                         shapeRenderer.rect(0,0,viewport.getWorldWidth(), viewport.getWorldHeight());

                         // draw centered panel
                         float panelW = Math.min(viewport.getWorldWidth() * 0.9f, 800f);
                         float panelH = Math.min(viewport.getWorldHeight() * 0.8f, 600f);
                         float px = (viewport.getWorldWidth() - panelW) / 2f;
                         float py = (viewport.getWorldHeight() - panelH) / 2f;
                         shapeRenderer.setColor(0.98f, 0.95f, 0.9f, 1f);
                         shapeRenderer.rect(px, py, panelW, panelH);
                         shapeRenderer.end();
                     }
                     // draw fish info text inside panel
                     spriteBatch.begin();
                     if (font != null) {
                         // shrink all help/intro overlay fonts to 0.8x so the panel fits the extra fish
                         font.getData().setScale(0.8f);
                         font.setColor(0f,0f,0f,1f);
                         String title = "Fish Guide";
                         float panelW = Math.min(viewport.getWorldWidth() * 0.9f, 800f);
                         float panelH = Math.min(viewport.getWorldHeight() * 0.8f, 600f);
                         float px = (viewport.getWorldWidth() - panelW) / 2f;
                         float py = (viewport.getWorldHeight() - panelH) / 2f;
                         GlyphLayout titleGl = new GlyphLayout(font, title);
                         font.draw(spriteBatch, title, px + 20f, py + panelH - 20f);

                        // list fish entries
                        float topY = py + panelH - 60f;
                        // prepare a temporary layout to measure font line height for vertical centering
                        GlyphLayout tmpMeasure = new GlyphLayout();
                        tmpMeasure.setText(font, "M");
                        float fontLineHeight = tmpMeasure.height;
                        final float thumbMax = 48f; // max thumbnail size in pixels

                        // Separate the stun fish from the other prototypes
                        Fish stunFish = null;
                        java.util.List<Fish> leftList = new java.util.ArrayList<>();
                        for (Fish f : helpFishPrototypes) {
                            try {
                                if (f.getImagePath() != null && f.getImagePath().equals(Fish8.IMAGE_PATH)) {
                                    stunFish = f;
                                    continue;
                                }
                            } catch (Exception ignored) {}
                            leftList.add(f);
                        }

                        // Draw the stun fish at the right-top corner of the panel (if present)
                        if (stunFish != null) {
                            TextureRegion rep = stunFish.getRepresentativeFrame();
                            float drawW = 0f, drawH = 0f;
                            if (rep != null) {
                                float rw = rep.getRegionWidth();
                                float rh = rep.getRegionHeight();
                                float scale = Math.min(thumbMax / rw, thumbMax / rh);
                                drawW = rw * scale;
                                drawH = rh * scale;
                            }
                            // right-aligned thumbnail with small padding from panel edge
                            float thumbX = px + panelW - 20f - drawW;
                            float thumbY = topY - (fontLineHeight / 2f) - (drawH / 2f);
                            if (rep != null) spriteBatch.draw(rep, thumbX, thumbY, drawW, drawH);
                            // draw right-aligned text to the left of the thumbnail
                            String stunLine = String.format("%s    HP:%d    Points:%d    Energy:%d", stunFish.getName(), stunFish.getMaxHp(), stunFish.getPoints(), stunFish.getEnergy());
                            GlyphLayout stunGl = new GlyphLayout(font, stunLine);
                            float textX = Math.max(px + 12f, thumbX - 8f - stunGl.width);
                            float textY = topY;
                            font.draw(spriteBatch, stunLine, textX, textY);
                        }

                        // Draw the left column of fish (all other prototypes). Move it slightly left (closer to panel edge)
                        float leftX = px + 8f; // was px+20f previously, moved left to give room
                        float y = topY;
                        for (Fish f : leftList) {
                            TextureRegion rep = f.getRepresentativeFrame();
                            float drawW = 0f, drawH = 0f;
                            float drawY = y - fontLineHeight / 2f;
                            if (rep != null) {
                                float rw = rep.getRegionWidth();
                                float rh = rep.getRegionHeight();
                                float scale = Math.min(thumbMax / rw, thumbMax / rh);
                                drawW = rw * scale;
                                drawH = rh * scale;
                                drawY = y - (fontLineHeight / 2f) - (drawH / 2f);
                                spriteBatch.draw(rep, leftX, drawY, drawW, drawH);
                            }
                            float textX = leftX + (drawW > 0f ? drawW + 8f : 0f);
                            String line = String.format("%s    HP:%d    Points:%d    Energy:%d", f.getName(), f.getMaxHp(), f.getPoints(), f.getEnergy());
                            font.draw(spriteBatch, line, textX, y);
                            float usedH = Math.max(fontLineHeight, drawH);
                            y -= usedH + 8f;
                        }

                        // Move the help tips (D/P) to the right-bottom inside the panel
                        String tipLine1 = "Press D to see the HitBox.";
                        String tipLine2 = "Press P to pause the game.";
                        GlyphLayout tipGl = new GlyphLayout();
                        tipGl.setText(font, tipLine2);
                        float tipPadding = 12f;
                        float tipX = px + panelW - 20f - tipGl.width; // right-aligned
                        float tipY = py + 20f + tipGl.height; // small padding above bottom
                        font.draw(spriteBatch, tipLine2, tipX, tipY);
                        // draw the first tip above the second
                        tipGl.setText(font, tipLine1);
                        font.draw(spriteBatch, tipLine1, px + panelW - 20f - tipGl.width, tipY + tipGl.height + 6f);

                        // hint to close
                        String hint = "Tap anywhere to close";
                        font.draw(spriteBatch, hint, px + 20f, py + 20f);
                        font.setColor(1f,1f,1f,1f);
                    }
                    spriteBatch.end();
                }

                return; // skip normal game rendering until started
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

                // Draw pause/continue button in the top-right using ShapeRenderer for background
                // End the batch temporarily to draw the filled rect, then resume to draw text.
                spriteBatch.end();
                if (shapeRenderer != null && viewport != null) {
                    shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
                    // Use a rounded-gradient button to match START style, scaled for smaller buttons
                    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                    // BACK button: neutral dark theme
                    drawRoundedGradientButton(shapeRenderer, backBtnX, backBtnY, backBtnW, backBtnH, 0.22f, 0.22f, 0.22f, backPressed);
                    // PAUSE/CONTINUE button: greenish when paused, dark otherwise
                    if (paused) {
                        drawRoundedGradientButton(shapeRenderer, pauseBtnX, pauseBtnY, pauseBtnW, pauseBtnH, 0.22f, 0.6f, 0.22f, pausePressed);
                    } else {
                        drawRoundedGradientButton(shapeRenderer, pauseBtnX, pauseBtnY, pauseBtnW, pauseBtnH, 0.22f, 0.22f, 0.22f, pausePressed);
                    }
                    shapeRenderer.end();
                 }
                 spriteBatch.begin();

                 // Draw button label centered
                 String pauseLabel = paused ? "CONTINUE" : "PAUSE";
                 GlyphLayout pauseGl = new GlyphLayout(font, pauseLabel);
                 // shrink button label fonts to 60% of the computed size
                 float labelScale = 0.6f * Math.min(1.0f, pauseBtnH / (pauseGl.height + 6f));
                 float oldScaleX = font.getData().scaleX;
                 float oldScaleY = font.getData().scaleY;
                 font.getData().setScale(labelScale);
                 font.setColor(1f, 1f, 1f, 1f);
                 pauseGl.setText(font, pauseLabel);
                 float textX = pauseBtnX + (pauseBtnW - pauseGl.width) / 2f;
                 float textY = pauseBtnY + (pauseBtnH + pauseGl.height) / 2f;
                 font.draw(spriteBatch, pauseLabel, textX, textY);

                 // Draw back button label
                 String backLabel = "BACK";
                 GlyphLayout backGl = new GlyphLayout(font, backLabel);
                 float backLabelScale = 0.6f * Math.min(1.0f, backBtnH / (backGl.height + 6f));
                 font.getData().setScale(backLabelScale);
                 float backTextX = backBtnX + (backBtnW - backGl.width) / 2f;
                 float backTextY = backBtnY + (backBtnH + backGl.height) / 2f;
                 font.draw(spriteBatch, backLabel, backTextX, backTextY);

                 // restore font
                 font.getData().setScale(oldScaleX, oldScaleY);
                 font.setColor(1f, 1f, 1f, 1f);

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
                    float oldScaleX2 = font.getData().scaleX;
                    float oldScaleY2 = font.getData().scaleY;
                    font.getData().setScale(scale);
                    font.setColor(1f, 0.65f, 0f, alpha); // orange color

                    for (int li = 0; li < lines.length; li++) {
                        String line = lines[li];

                        // compute width of the whole line using GlyphLayout (no per-character extra spacing)
                        GlyphLayout lineGl = new GlyphLayout(font, line);
                        float lineX = cx - lineGl.width / 2f;
                        float lineY = startY - li * (singleLineHeight + lineGap);

                        // draw the entire line in a single pass
                        font.draw(spriteBatch, line, lineX, lineY);
                    }

                    // restore font
                    font.getData().setScale(oldScaleX2, oldScaleY2);
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
                    float br = b.getCollisionRadius();
                    shapeRenderer.circle(bx, by, br);
                }
                // fish: green (draw each collision circle)
                shapeRenderer.setColor(0f, 1f, 0f, 1f);
                for (FishEntity fe : fishEntities) {
                    float fw = fe.fish.getWidth();
                    float fh = fe.fish.getHeight();
                    float fCenterX = fe.x + fw / 2f;
                    float fCenterY = fe.y + fh / 2f;
                    float fRotation = fe.angle;
                    java.util.List<MultiCircleCollision.Circle> circles = fe.fish.getCollisionCircles();
                    for (MultiCircleCollision.Circle c : circles) {
                        float[] p = MultiCircleCollision.rotatePoint(c.x, c.y, fRotation);
                        shapeRenderer.circle(fCenterX + p[0], fCenterY + p[1], c.r);
                    }
                }
                shapeRenderer.end();
            }
        }
    }


    public void update(){
        float delta = Gdx.graphics.getDeltaTime();
        // update audio manager each frame (handles fades)
        // Only update audio if the AudioManager singleton was already created (avoid creating it on app open)
        AudioManager.updateIfExists(delta);

        // -- Handle pause/unpause immediately to avoid any game-state changes on the same frame --
        if (Gdx.input.justTouched()) {
            int sx = Gdx.input.getX();
            int sy = Gdx.input.getY();
            Vector3 world = new Vector3(sx, sy, 0);
            if (viewport != null) viewport.unproject(world);
            // If touch begins inside BACK or PAUSE, set pressed visual and consume the touch
            // but do NOT perform the action until release inside the same button.
            if (isPointInBackButton(world.x, world.y) && started) {
                backPressed = true;
                // consume so other UI doesn't react to the same down event
                return;
            }
            if (isPointInPauseButton(world.x, world.y)) {
                pausePressed = true;
                // consume so other UI doesn't react to the same down event
                return;
            }
        }

        // While touch is held, update top-left/top-right pressed visuals so they render properly
        if (Gdx.input.isTouched()) {
            int sx2 = Gdx.input.getX();
            int sy2 = Gdx.input.getY();
            Vector3 world2 = new Vector3(sx2, sy2, 0);
            if (viewport != null) viewport.unproject(world2);
            if (backPressed) backPressed = isPointInBackButton(world2.x, world2.y);
            if (pausePressed) pausePressed = isPointInPauseButton(world2.x, world2.y);
        } else {
            // On release: if previous down began inside BACK/PAUSE and release is also inside, perform action
            if (backPressed || pausePressed) {
                int sxr = Gdx.input.getX();
                int syr = Gdx.input.getY();
                Vector3 worldr = new Vector3(sxr, syr, 0);
                if (viewport != null) viewport.unproject(worldr);

                if (backPressed) {
                    boolean releasedInBack = isPointInBackButton(worldr.x, worldr.y);
                    if (releasedInBack && started) {
                        try { goBackToStart(); } catch (Exception ignored) {}
                        Gdx.app.log("Game", "Back button pressed -> returning to start screen");
                    }
                }

                if (pausePressed) {
                    boolean releasedInPause = isPointInPauseButton(worldr.x, worldr.y);
                    if (releasedInPause) {
                        paused = !paused;
                        try {
                            if (paused) AudioManager.get().pausePlayback();
                            else AudioManager.get().resumePlayback();
                        } catch (Exception ignored) {}
                        Gdx.app.log("Game", "Pause button pressed. paused=" + paused + ", world=(" + worldr.x + "," + worldr.y + ")");
                    }
                }
            }

            // clear transient pressed visuals on release
            backPressed = false;
            pausePressed = false;
        }

        // Process keyboard pause/unpause here so 'P' works even while paused (matches pause button behavior)
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            paused = !paused;
            try {
                if (paused) AudioManager.get().pausePlayback();
                else AudioManager.get().resumePlayback();
            } catch (Exception ignored) {}
            Gdx.app.log("Game", "Pause toggled by key P. paused=" + paused);
            return; // consume frame so no state updates this frame
        }

        // If the game is paused, skip all game-state updates. We still processed pause/unpause input above
        // so the player can unpause using the pause button or 'P' key while paused.
        if (paused) {
            return;
        }

        // If the game hasn't started yet, advance the START button gradient animation and check for clicks
        if (!started) {
            // If help overlay is visible, a tap anywhere should close it
            if (showHelpOverlay) {
                if (Gdx.input.justTouched()) {
                    showHelpOverlay = false;
                }
                // While overlay is visible, do not process other start-screen input
                return;
            }

            // Touch down: if pointer just touched and the touch began inside the start button or help button, mark active pressed state
            if (Gdx.input.justTouched()) {
                int sx = Gdx.input.getX();
                int sy = Gdx.input.getY();
                Vector3 world = new Vector3(sx, sy, 0);
                if (viewport != null) viewport.unproject(world);

                boolean inStart = world.x >= startBtnX && world.x <= startBtnX + startBtnW && world.y >= startBtnY && world.y <= startBtnY + startBtnH;
                boolean inHelp = world.x >= helpBtnX && world.x <= helpBtnX + helpBtnW && world.y >= helpBtnY && world.y <= helpBtnY + helpBtnH;

                if (inHelp) {
                    helpTouchActive = true;
                    helpPressed = true;
                    // don't set start touch when help was pressed
                    startTouchActive = false;
                    startPressed = false;
                } else if (inStart) {
                    startTouchActive = true;
                    startPressed = true;
                    helpTouchActive = false;
                    helpPressed = false;
                } else {
                    startTouchActive = false;
                    startPressed = false;
                    helpTouchActive = false;
                    helpPressed = false;
                }
            }

            // While touch is held, update pressed flag depending on whether pointer remains inside respective button
            if (Gdx.input.isTouched()) {
                int sx = Gdx.input.getX();
                int sy = Gdx.input.getY();
                Vector3 world = new Vector3(sx, sy, 0);
                if (viewport != null) viewport.unproject(world);
                boolean insideStart = world.x >= startBtnX && world.x <= startBtnX + startBtnW && world.y >= startBtnY && world.y <= startBtnY + startBtnH;
                boolean insideHelp = world.x >= helpBtnX && world.x <= helpBtnX + helpBtnW && world.y >= helpBtnY && world.y <= helpBtnY + helpBtnH;
                if (startTouchActive) startPressed = insideStart;
                if (helpTouchActive) helpPressed = insideHelp;
            }

            // On touch release: if the touch began inside the start button and the release is also inside, start the game
            if (!Gdx.input.isTouched() && (startTouchActive || helpTouchActive)) {
                int sx = Gdx.input.getX();
                int sy = Gdx.input.getY();
                Vector3 world = new Vector3(sx, sy, 0);
                if (viewport != null) viewport.unproject(world);

                if (helpTouchActive) {
                    boolean releasedInHelp = world.x >= helpBtnX && world.x <= helpBtnX + helpBtnW && world.y >= helpBtnY && world.y <= helpBtnY + helpBtnH;
                    if (releasedInHelp) {
                        // open the help overlay
                        showHelpOverlay = true;
                    }
                    helpTouchActive = false;
                    helpPressed = false;
                }

                if (startTouchActive) {
                    // Use startBtnH here (was incorrectly using helpBtnH) so the release-hitbox matches the START button height
                    boolean releasedInStart = world.x >= startBtnX && world.x <= startBtnX + startBtnW && world.y >= startBtnY && world.y <= startBtnY + startBtnH;
                    if (releasedInStart) {
                        started = true;
                        FishGeneratorGapTime = 0f; // start spawning immediately
                        // Start playing main background music from the beginning (or resume if previously paused)
                        AudioManager.get().playMain();
                    }
                    startTouchActive = false;
                    startPressed = false;
                }
            }

            return;
        }
        // update cannon cooldown timer so canFire()/tryFire() work correctly
        if (cannon != null) cannon.updateFireCooldown(delta);
        FishGeneratorGapTime += delta;
        // spawn interval is shorter during berserk
        float spawnInterval = berserkActive ? 0.3f : 1.0f;
        if (FishGeneratorGapTime >= spawnInterval) {
            // Spawn a new fish at a random vertical position on the left side
            for (int i = 0; i < MathUtils.random(1,4); i++) {
                float ran = MathUtils.random(0,100);

                if (ran < 50.0f) {
                    spawnFish1();
                }else if(ran < 67f){
                    spawnFish2();
                }else if(ran < 79f){
                    spawnFish3();
                }else if(ran < 88f){
                    spawnFish4();
                }else if(ran < 95f){
                    spawnFish5();
                }else if(ran <= 98f){
                    spawnFish6();
                }else if (ran <= 99f) {
                    spawnFish7();
                } else {
                    // very rare stun fish
                    spawnFish8();
                 }
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
                     int levels = 7;
                     currentCannonLevel = (currentCannonLevel - 2 + levels) % levels + 1; // downgrade
                     float ang = cannon.getAngleDeg();
                     float cx = cannon.getX();
                     float cy = cannon.getY();
                     Cannon newC;
                     if (currentCannonLevel == 1) newC = new Cannon1(cx, cy);
                     else if (currentCannonLevel == 2) newC = new Cannon2(cx, cy);
                     else if (currentCannonLevel == 3) newC = new Cannon3(cx, cy);
                     else if (currentCannonLevel == 4) newC = new Cannon4(cx, cy);
                     else if (currentCannonLevel == 5) newC = new Cannon5(cx, cy);
                     else if (currentCannonLevel == 6) newC = new Cannon6(cx, cy);
                     else newC = new Cannon7(cx, cy);
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
                     int levels = 7;
                     currentCannonLevel = (currentCannonLevel % levels) + 1; // upgrade
                     float ang = cannon.getAngleDeg();
                     float cx = cannon.getX();
                     float cy = cannon.getY();

                     Cannon newC;
                     if (currentCannonLevel == 1) newC = new Cannon1(cx, cy);
                     else if (currentCannonLevel == 2) newC = new Cannon2(cx, cy);
                     else if (currentCannonLevel == 3) newC = new Cannon3(cx, cy);
                     else if (currentCannonLevel == 4) newC = new Cannon4(cx, cy);
                     else if (currentCannonLevel == 5) newC = new Cannon5(cx, cy);
                     else if (currentCannonLevel == 6) newC = new Cannon6(cx, cy);
                     else newC = new Cannon7(cx, cy);

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
                    // Create the bullet for this cannon level first, then ask it for its cost.
                    Bullet b;
                    if (level == 1) {
                        b = new Bullet1(muzzle[0], muzzle[1], cannon.getAngleDeg() + 90f);
                    } else if (level == 2) {
                        b = new Bullet2(muzzle[0], muzzle[1], cannon.getAngleDeg() + 90f);
                    } else if (level == 3){ // level == 3
                        b = new Bullet3(muzzle[0], muzzle[1], cannon.getAngleDeg() + 90f);
                    } else if (level == 4){
                        b = new Bullet4(muzzle[0], muzzle[1], cannon.getAngleDeg() + 90f);
                    } else if (level == 5){
                        b = new Bullet5(muzzle[0], muzzle[1], cannon.getAngleDeg() + 90f);
                    } else if (level == 6){
                        b = new Bullet6(muzzle[0], muzzle[1], cannon.getAngleDeg() + 90f);
                    } else {
                        b = new Bullet7(muzzle[0], muzzle[1], cannon.getAngleDeg() + 90f);
                    }

                    int cost = b.getCost();
                    int effectiveCost = berserkActive ? (cost / 2) : cost; // halve cost during berserk (floor)
                    if (playerScore >= effectiveCost) {
                        playerScore -= effectiveCost;
                        bullets.add(b);
                    } else {
                        Gdx.app.log("Game", "Not enough points to fire: need " + effectiveCost + ", have " + playerScore);
                    }
                } else {
                    Gdx.app.log("Game", "Cannon cooling down: cannot fire yet");
                }
             }
         }

        for (FishEntity e : fishEntities) {
            e.fish.update(delta);
            // movement uses each fish's configured speed; do not move dying fish so death animation stays in place
            // when stun is active, freeze non-dying fish in place (they can still be hit)
            if (!e.fish.isDying() && !stunActive) {
                // Use a separate movementAngle so we can rotate movement for specific fish types
                float moveAngle = e.angle;
                try {
                    // If this is a Fish7 instance, rotate its movement CCW by 45° while keeping render angle unchanged
                    if (e.fish.getImagePath() != null && e.fish.getImagePath().equals(Fish7.IMAGE_PATH)) {
                        // increase movement tilt by additional 15° -> total +60°
                        moveAngle = e.angle + 60f;
                    }
                } catch (Exception ignored) {}
                e.x += e.fish.getSpeed() * delta * MathUtils.cosDeg(moveAngle);
                e.y += e.fish.getSpeed() * delta * MathUtils.sinDeg(moveAngle);
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
                 // Fish is drawn with batch.draw(..., x, y, width/2, height/2, width, height, 1,1, angle)
                 // where x,y are the bottom-left corner. Build polygon using the sprite center.
                 float fw = fe.fish.getWidth();
                 float fh = fe.fish.getHeight();
                 float fCenterX = fe.x + fw / 2f;
                 float fCenterY = fe.y + fh / 2f;
                 float fRotation = fe.angle;

                float br = b.getCollisionRadius();
                boolean hit = MultiCircleCollision.bulletHitsSpriteCircles(bx, by, br, fCenterX, fCenterY, fRotation, fe.fish.getCollisionCircles());

                if (hit) {
                    int dmg = b.getDamage();
                    boolean died = fe.fish.takeDamage(dmg);
                    Gdx.app.log("Game","Bullet hit fish: dmg=" + dmg + " hpRemaining=" + fe.fish.getCurrentHp());
                    // always convert the bullet into a web (capture) regardless of fish death
                    b.capture();
                    if (died) {
                        // If this fish is the stun fish, trigger global stun effect
                        try {
                            if (Fish8.IMAGE_PATH.equals(fe.fish.getImagePath())) {
                                stunActive = true;
                                stunTimer = 0f;
                                Gdx.app.log("Game", "Stun triggered by StunFish: all fish frozen for " + STUN_DURATION + "s");
                            }
                        } catch (Exception ignored) {}
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
                if (berserkActive) points = (int) (points * 1.5f); // double points during berserk
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

        // Stun timer update
        if (stunActive) {
            stunTimer += delta;
            if (stunTimer >= STUN_DURATION) {
                stunActive = false;
                stunTimer = 0f;
                Gdx.app.log("Game", "Stun ended: fish resume movement");
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
        Fish1.dispose();
        Fish2.dispose();
        Fish3.dispose();
        Fish4.dispose();
        Fish5.dispose();
        Fish6.dispose();
        Fish7.dispose();
        Fish8.dispose();
        // Dispose cannon resources
        Cannon.disposeCommonIcons();
        Cannon1.disposeStatic();
        Cannon2.disposeStatic();
        Cannon3.disposeStatic();
        Cannon4.disposeStatic();
        Cannon5.disposeStatic();
        Cannon6.disposeStatic();
        Cannon7.disposeStatic();
        // Dispose bullet textures
        Bullet1.disposeTexture();
        Bullet2.disposeTexture();
        Bullet3.disposeTexture();
        Bullet4.disposeTexture();
        Bullet5.disposeTexture();
        Bullet6.disposeTexture();
        Bullet7.disposeTexture();

        // Dispose audio manager
        AudioManager.disposeIfExists();

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
        float baseSpacing = (cannon.getWidth() >  0) ? cannon.getWidth() + 8f : 48f;
        float spacing = baseSpacing * ICON_SPACING_FACTOR;
        float rW = reg.getRegionWidth();
        float rH = reg.getRegionHeight();
        float rightX = cannon.getX() + spacing - rW / 2f;
        float rightY = cannon.getY() - rH / 2f;
        return wx >= rightX && wx <= rightX + rW && wy >= rightY && wy <= rightY + rH;
    }

    // Draw a rounded-gradient button matching the START button style but scaled for small UI buttons
    private void drawRoundedGradientButton(ShapeRenderer sr, float x, float y, float w, float h, float baseR, float baseG, float baseB, boolean pressed) {
        if (sr == null) return;
        float cx = x + w / 2f;
        float cy = y + h / 2f;
        float outerW = w;
        float outerH = h;

        // Optional pressed shadow behind the button
        if (pressed) {
            sr.setColor(0f, 0f, 0f, 0.45f);
            float pad = Math.min(8f, Math.min(outerW, outerH) * 0.06f);
            float sw = outerW + pad * 2f;
            float sh = outerH + pad * 2f;
            float sx = cx - sw / 2f;
            float sy = cy - sh / 2f;
            float cornerShadow = Math.min(Math.min(outerW, outerH) * 0.18f + pad, Math.min(sw, sh) * 0.18f);
            float innerW = Math.max(0f, sw - 2f * cornerShadow);
            float innerH = Math.max(0f, sh - 2f * cornerShadow);
            if (innerW > 0f && innerH > 0f) sr.rect(sx + cornerShadow, sy + cornerShadow, innerW, innerH);
            if (innerW > 0f) {
                sr.rect(sx + cornerShadow, sy, innerW, cornerShadow);
                sr.rect(sx + cornerShadow, sy + sh - cornerShadow, innerW, cornerShadow);
            }
            if (innerH > 0f) {
                sr.rect(sx, sy + cornerShadow, cornerShadow, innerH);
                sr.rect(sx + sw - cornerShadow, sy + cornerShadow, cornerShadow, innerH);
            }
            if (cornerShadow > 0f) {
                sr.circle(sx + cornerShadow, sy + cornerShadow, cornerShadow);
                sr.circle(sx + sw - cornerShadow, sy + cornerShadow, cornerShadow);
                sr.circle(sx + cornerShadow, sy + sh - cornerShadow, cornerShadow);
                sr.circle(sx + sw - cornerShadow, sy + sh - cornerShadow, cornerShadow);
            }
        }

        // concentric rings for gradient (white -> base color)
        int rings = 12; // fewer rings for small buttons
        float baseCorner = Math.min(outerW, outerH) * 0.18f;
        for (int i = rings - 1; i >= 0; i--) {
            float radiusFrac = (float) i / (rings - 1);
            float w_i = Math.max(0f, outerW * radiusFrac);
            float h_i = Math.max(0f, outerH * radiusFrac);
            if (w_i <= 0f || h_i <= 0f) continue;
            float x_i = cx - w_i / 2f;
            float y_i = cy - h_i / 2f;

            float centerFrac = 1f - radiusFrac; // 0 at edge -> 1 at center
            float t = MathUtils.clamp(centerFrac / 1f, 0f, 1f); // full lerp across rings

            // lerp white -> base color
            float rcol = (1f - t) + baseR * t;
            float gcol = (1f - t) + baseG * t;
            float bcol = (1f - t) + baseB * t;
            sr.setColor(rcol, gcol, bcol, 1f);

            float corner = baseCorner * radiusFrac;
            float innerW = Math.max(0f, w_i - 2f * corner);
            float innerH = Math.max(0f, h_i - 2f * corner);
            if (innerW > 0f && innerH > 0f) sr.rect(x_i + corner, y_i + corner, innerW, innerH);
            if (innerW > 0f) {
                sr.rect(x_i + corner, y_i, innerW, corner);
                sr.rect(x_i + corner, y_i + h_i - corner, innerW, corner);
            }
            if (innerH > 0f) {
                sr.rect(x_i, y_i + corner, corner, innerH);
                sr.rect(x_i + w_i - corner, y_i + corner, corner, innerH);
            }
            if (corner > 0f) {
                sr.circle(x_i + corner, y_i + corner, corner);
                sr.circle(x_i + w_i - corner, y_i + corner, corner);
                sr.circle(x_i + corner, y_i + h_i - corner, corner);
                sr.circle(x_i + w_i - corner, y_i + h_i - corner, corner);
            }
        }
    }

    // Draw a circular gradient button (white center -> base color rim) with optional pressed shadow
    private void drawCircularGradientButton(ShapeRenderer sr, float cx, float cy, float radius, float baseR, float baseG, float baseB, boolean pressed) {
        if (sr == null) return;
        // pressed shadow
        if (pressed) {
            sr.setColor(0f, 0f, 0f, 0.45f);
            float pad = Math.min(8f, radius * 0.06f);
            sr.circle(cx, cy, radius + pad);
        }

        int rings = 18; // more rings for smoother circular gradient
        for (int i = rings - 1; i >= 0; i--) {
            float frac = (float) i / (rings - 1); // 0..1
            float r = Math.max(0f, radius * frac);
            if (r <= 0f) continue;
            float t = 1f - frac; // 0 at edge -> 1 at center
            float rcol = (1f - t) + baseR * t;
            float gcol = (1f - t) + baseG * t;
            float bcol = (1f - t) + baseB * t;
            sr.setColor(rcol, gcol, bcol, 1f);
            sr.circle(cx, cy, r);
        }
    }

    // UI hit test for pause button
    private boolean isPointInPauseButton(float wx, float wy) {
        return wx >= pauseBtnX && wx <= pauseBtnX + pauseBtnW && wy >= pauseBtnY && wy <= pauseBtnY + pauseBtnH;
    }

    // UI hit test for back button (top-left)
    private boolean isPointInBackButton(float wx, float wy) {
        return wx >= backBtnX && wx <= backBtnX + backBtnW && wy >= backBtnY && wy <= backBtnY + backBtnH;
    }

    // Helper to return to the start screen and stop music
    private void goBackToStart() {
        // Stop and dispose audio to ensure music fully stops
        try {
            AudioManager.disposeIfExists();
        } catch (Exception ignored) {}

        // Clear runtime entities and bullets
        bullets.clear();
        fishEntities.clear();

        // Reset core game state to initial values
        started = false;
        paused = false;
        berserkActive = false;
        berserkTimer = 0f;
        subtitleText = null;
        subtitleTimer = 0f;
        playerEnergy = 0f;
        playerScore = 1000;
        FishGeneratorGapTime = 2.0f;

        // Reset cannon to level 1 centered
        if (viewport != null) {
            cannon = new Cannon1(viewport.getWorldWidth() / 2f, viewport.getWorldHeight() / 2f);
        } else {
            cannon = new Cannon1(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f);
        }
        currentCannonLevel = 1;

        // Recompute UI geometry
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
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

        // Notify audio manager to enter berserk (pause main, fade in berserk track)
        AudioManager.get().enterBerserk();
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
         subtitleText = "EXIT BERSERK MODE";
         subtitleTimer = 0f;
         // keep the end text visible for 0.5s before any scaling/fade
         subtitleDelay = 0.5f;
         subtitleScaleDuration = 0.6f; // then scale/fade over 0.6s
         subtitleMaxScaleIncrease = 0.3f; // small scale-up
         subtitleDuration = subtitleDelay + subtitleScaleDuration;

         // Notify audio manager to exit berserk (fade out berserk track and resume main)
         AudioManager.get().exitBerserk();
     }

}

