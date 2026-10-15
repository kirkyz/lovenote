LOVENOTE - BATCH 1
==================

Drop these 9 files into the ROOT of your repo and commit. Overwrite when asked.

OVERWRITTEN
  Colors.java            - 32 themes + custom HSV / chroma support
  InterfaceModule.java   - theme picker + Hue/Sat/Brightness/Spread/Chroma controls
  ParticleUtil.java      - 70 particle types (was 14)
  AmbientParticles.java  - 11 spawn shapes
  ModuleManager.java     - registers SkyShader, keeps chroma animating

NEW
  Tex.java                - runtime-generated glow / soft / sparkle textures (no assets folder needed)
  SkyShader.java          - 12 sky presets, stars, shooting stars, nebula, aurora, sun & moon
  WorldRendererMixin.java - hooks renderSky
  lovenote.mixins.json    - updated (now lists WorldRendererMixin)

HOW TO USE
  1. Build via your existing GitHub Action.
  2. In game: open the menu (INSERT) -> hud -> Interface -> Theme -> "Custom"
     to get the HSV picker, or pick any of the 32 presets.
  3. visuals -> SkyShader to pick a sky. Enable "Theme colours" to drive the
     nebula / aurora from your Interface theme.

NOTES
  * SkyShader targets WorldRenderer.renderSky. If the build log says
    "Cannot find method renderSky", the descriptor needs a tweak - send me the log.
  * Settings are saved to config/lovenote.json as usual.
