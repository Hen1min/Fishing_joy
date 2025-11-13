package io.github.Fishing_joy;

// ...existing code...

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.math.MathUtils;

/**
 * Simple audio manager to handle background music playback with pause/resume and cross-fade between two tracks.
 * Usage (example):
 *   AudioManager.get().playMain();
 *   AudioManager.get().enterBerserk();
 *   AudioManager.get().exitBerserk();
 */
public class AudioManager implements Disposable {
    private static AudioManager instance;

    private Music mainMusic; // 1.mp3
    private Music berserkMusic; // 2.mp3

    private float targetMainVolume = 1f;
    private float targetBerserkVolume = 1f;

    private float fadeDuration = 1.0f; // seconds for fade in/out

    private boolean berserkActive = false;

    // For resuming main from paused position we just pause it (Music.pause()) and then resume

    // Track whether audio was paused due to user pausing the game
    private boolean pausedByUser = false;
    // Track whether main was playing before an external pause so we can resume appropriately
    private boolean mainWasPlayingBeforePause = false;

    private AudioManager() {
        try {
            mainMusic = Gdx.audio.newMusic(Gdx.files.internal("1.mp3"));
        } catch (Exception e) {
            Gdx.app.log("AudioManager", "Failed to load 1.mp3: " + e.getMessage());
        }
        try {
            berserkMusic = Gdx.audio.newMusic(Gdx.files.internal("2.mp3"));
        } catch (Exception e) {
            Gdx.app.log("AudioManager", "Failed to load 2.mp3: " + e.getMessage());
        }
        if (mainMusic != null) {
            mainMusic.setLooping(true);
            mainMusic.setVolume(1f);
        }
        if (berserkMusic != null) {
            berserkMusic.setLooping(true);
            berserkMusic.setVolume(0f);
        }
    }

    public static AudioManager get() {
        if (instance == null) instance = new AudioManager();
        return instance;
    }

    // Call when Start button pressed
    public void playMain() {
        if (mainMusic == null) return;
        if (!mainMusic.isPlaying()) {
            mainMusic.play();
        }
        mainMusic.setVolume(targetMainVolume);
    }

    // Pause all music playback and record previous state
    public void pausePlayback() {
        pausedByUser = true;
        if (mainMusic != null) {
            mainWasPlayingBeforePause = mainMusic.isPlaying();
            if (mainMusic.isPlaying()) mainMusic.pause();
        }
        if (berserkMusic != null && berserkMusic.isPlaying()) {
            berserkMusic.pause();
        }
    }

    // Resume playback after a user pause. Resumes the appropriate track depending on berserk state.
    public void resumePlayback() {
        if (!pausedByUser) return;
        pausedByUser = false;
        // If berserk is active, resume berserk music; otherwise resume main only if it was playing earlier
        if (berserkActive) {
            if (berserkMusic != null && !berserkMusic.isPlaying()) berserkMusic.play();
        } else {
            if (mainWasPlayingBeforePause && mainMusic != null && !mainMusic.isPlaying()) mainMusic.play();
        }
    }

    // Enter berserk: pause main (keep position) and fade in berserkMusic
    public void enterBerserk() {
        if (berserkMusic == null) return;
        if (mainMusic != null && mainMusic.isPlaying()) {
            mainMusic.pause();
        }
        berserkActive = true;
        berserkMusic.setVolume(0f);
        if (!berserkMusic.isPlaying()) berserkMusic.play();
        // start a fade coroutine-like by registering to render loop; user must call update(delta)
    }

    // Exit berserk: fade out berserk and resume main from paused position only after fade completes
    public void exitBerserk() {
        berserkActive = false;
        // user must call update(delta) to progress fades
    }

    // Should be called every frame with delta time (seconds). Integrate from game's render/update loop.
    public void update(float delta) {
        // If user paused the game, don't auto-resume or fade audio; keep paused state until user resumes
        if (pausedByUser) return;

        // If berserk active: increase berserk volume to target, keep main paused
        if (berserkActive) {
            if (berserkMusic != null) {
                float v = berserkMusic.getVolume();
                float next = v + (delta / Math.max(fadeDuration, 0.001f)) * targetBerserkVolume;
                if (next >= targetBerserkVolume) {
                    berserkMusic.setVolume(targetBerserkVolume);
                } else {
                    berserkMusic.setVolume(next);
                }
            }
        } else {
            // Berserk not active -> fade out berserk and resume main only after berserk stopped
            if (berserkMusic != null && berserkMusic.isPlaying()) {
                float v = berserkMusic.getVolume();
                float next = v - (delta / Math.max(fadeDuration, 0.001f)) * targetBerserkVolume;
                if (next <= 0f) {
                    berserkMusic.setVolume(0f);
                    berserkMusic.stop();
                } else {
                    berserkMusic.setVolume(next);
                }
            }
            // Only resume main when berserk music is not playing (fully faded/stopped)
            if (mainMusic != null && !mainMusic.isPlaying() && (berserkMusic == null || !berserkMusic.isPlaying())) {
                mainMusic.play();
                mainMusic.setVolume(targetMainVolume);
            }
        }
    }

    public void setFadeDuration(float seconds) {
        this.fadeDuration = Math.max(0.01f, seconds);
    }

    public void setMainVolume(float vol) {
        this.targetMainVolume = MathUtils.clamp(vol, 0f, 1f);
        if (mainMusic != null) mainMusic.setVolume(this.targetMainVolume);
    }

    public void setBerserkVolume(float vol) {
        this.targetBerserkVolume = MathUtils.clamp(vol, 0f, 1f);
    }

    @Override
    public void dispose() {
        if (mainMusic != null) {
            mainMusic.stop();
            mainMusic.dispose();
            mainMusic = null;
        }
        if (berserkMusic != null) {
            berserkMusic.stop();
            berserkMusic.dispose();
            berserkMusic = null;
        }
        instance = null;
    }

    // Safely dispose if the singleton was created; avoids creating it just to dispose.
    public static void disposeIfExists() {
        if (instance != null) {
            instance.dispose();
        }
    }

    // Update only if the singleton already exists; this avoids creating the AudioManager on app startup.
    public static void updateIfExists(float delta) {
        if (instance != null) {
            instance.update(delta);
        }
    }
}
