# Changelog

All notable changes to the **Flying Bird** project will be documented in this file.

## [1.0.0] - 2026-06-22

### Added
- **Core Game Engine & Physics**:
  - Classic "flap to jump" physics with smooth pitch/rotation interpolation.
  - Startup "Get Ready" hover state where the bird bobs on a sine wave until the first click/tap to prevent instant collisions.
  - Exponential difficulty scaling: obstacles scroll faster and opening gaps shrink down to a limit of 100px as the score increases.
  - Diagonal obstacles: randomized horizontal offsets for bottom obstacles starting at score >= 5.
- **Visuals & Art Assets**:
  - 15 theme-specific high-resolution PNG assets for 3 distinct biomes (Default, Sunset, Winter).
  - Distinct top and bottom obstacles for each theme, aligned/anchored without stretching/flipping bugs.
  - Smooth 4-frame flight animation cycle using custom high-resolution bird sprite sheets.
  - Dynamic weather system (slanted rain streaks for Default, warm floating embers for Sunset, soft drifting snow for Winter) customized to the active biome.
  - Contrast filters (25% higher contrast, 15 brightness offset) for foreground elements and translucent dimming on the sky background to maximize legibility.
- **User Interface (UI) & HUD**:
  - Modern, responsive Dark Terminal / Cyberpunk visual aesthetic.
  - Dynamic three-circle main menu (About, Play, Settings) and paused overlay (Restart, Resume, Menu) matching the style.
  - Screen blur effect applied dynamically behind active popups/menus.
  - Interactive "About" overlay teaching controls and rules.
  - Universal landscape HUD displaying a custom-drawn numeric score centered at the top, alongside current biome theme and high score.
- **Audio System**:
  - Continuous, seamless looping background music that does not interrupt during transitions or restarts.
  - Sound effects for wing flap, obstacle collision/hit, scoring, player death, and menu swooshes.
  - Independent, real-time volume slider controls for both music and sound effects.
- **Multiplatform Packaging**:
  - **Android**: Enabled 16KB page support, configured universal single-APK builds, locked sensor landscape orientation, and fully hid status/navigation system bars with swipe-to-reveal support.
  - **iOS**: Locked landscape orientation using SwiftUI custom AppDelegate, restored and configured compiler environments.
  - **Desktop**: Borderless undecorated main window with theme-matching title bar, custom close/minimize buttons, custom window-drag capabilities, custom fullscreen `⛶` button, and standard `F11` borderless toggle (which hides the macOS Dock/menu bar and Windows taskbar completely).
  - **Installer Targets**: Integrated MSI/EXE for Windows, DMG/PKG for macOS, and DEB/RPM/Tarballs for Linux.
