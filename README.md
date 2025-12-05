# Fishing Joy

Fishing Joy is a casual arcade-style fishing game built with libGDX.

In this fast-paced desktop game you control a cannon at the bottom of the screen and fire projectiles to capture fish swimming across the playfield. Each fish type has a different size and score value, and special fish behave unpredictably — aim and time your shots to maximize your score. The game focuses on simple, addictive mechanics, colorful pixel art, and lively sound effects.

Key features

- Easy-to-learn, hard-to-master arcade gameplay.
- Multiple fish types with different behaviors and point values.
- Responsive mouse and keyboard controls for desktop play.
- Local high-score tracking for short play sessions.
- Lightweight, cross-platform desktop build using LWJGL3.

Gameplay overview

- Objective: Catch fish to earn points. Different fish award different scores.
- Cannons and upgrades: Use the cannon to shoot nets or projectiles. Some versions include power-ups or multiple cannon levels.
- Risk vs reward: Bigger or rarer fish are harder to capture but grant more points.

Controls (desktop - LWJGL3)

- Mouse: Aim the cannon and click to fire.
- Arrow keys / A,D: (optional) Move the cannon or change aim.
- +/- or mouse wheel: Change cannon power (if implemented).
- Esc: Pause or exit the game.

How to run (desktop)

This project uses Gradle. On Windows you can run the desktop (LWJGL3) launcher using the included Gradle wrapper:

    .\gradlew.bat lwjgl3:run

Or build a runnable JAR:

    .\gradlew.bat lwjgl3:jar

The runnable jar will be produced in `lwjgl3/build/libs`.

Assets and credits

- The game includes sprite and audio assets under the `assets/` directory.
- Built with libGDX. See libGDX documentation for licensing and distribution details.


Enjoy playing Fishing Joy! Contributions, fixes, and improvements are welcome — open an issue or pull request.
