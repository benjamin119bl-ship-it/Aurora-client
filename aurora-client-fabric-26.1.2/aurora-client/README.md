# Aurora Client (Fabric, Minecraft 26.1.2, client-side only)

Zero mixins. Only Fabric API hooks, so it coexists with Sodium, Iris, Lithium, Lunar's own mods, etc.

## Build
Requires JDK 25. Easiest path: generate a fresh project for 26.1.2 at https://fabricmc.net/develop/template
(gets you the Gradle wrapper), then copy `src/`, `build.gradle`, `gradle.properties` over it.
Then:  `./gradlew build`  ->  `build/libs/aurora-client-1.0.0.jar`

## Install
Put the jar and Fabric API (26.1.2) in the mods folder of your Fabric 26.1.2 profile.

## Keys (rebindable under Controls > Aurora Client)
C zoom (hold) | B fullbright | V cycle look | G rain+sky FX | H info HUD

## Config
`config/aurora.json` (edit with the game closed): rainDensity, rainRadius, zoomFov, adaptivePerformance, ...
