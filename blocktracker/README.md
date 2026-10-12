# Block Tracker (Fabric, Minecraft 1.21.4)

Client-side mod that draws coloured wireframe outlines around chosen block types near you,
optionally through walls.

## Build

1. Install JDK 21.
2. In this folder run `./gradlew build` (Windows: `gradlew build`). The Gradle wrapper is included,
   and the first build downloads Minecraft and Fabric, which takes a few minutes.
3. The jar is in `build/libs/blocktracker-1.0.0.jar`. Put it in your `mods` folder
   together with Fabric API for 1.21.4.

To run straight from the project: `./gradlew runClient`.

## Use

- Press **K** to toggle (rebind under Controls > Block Tracker).
- `/track add <block> [#RRGGBB]` start tracking a block (tab-completes block ids)
- `/track remove <block>`
- `/track list`
- `/track radius <1-64>` scan radius in blocks
- `/track walls <true|false>` outlines visible through walls or not
- `/track alpha <0.05-1>` outline opacity
- `/track toggle`

Settings persist in `config/blocktracker.json`. It starts with diamond ore and chests tracked.

## Notes

- The scan runs every 10 ticks and is capped at 2000 outlines, so large radii in
  ore-heavy areas stay smooth.
- Rendering code targets 1.21.4 Yarn mappings. Other versions need small changes
  (for example `VertexRendering.drawBox` was `WorldRenderer.drawBox` in 1.21.1-1.21.3).
- Using through-walls tracking on multiplayer servers may be against their rules. Check first.

## No local setup? Build on GitHub

Push this folder to a GitHub repo, open the Actions tab, run the `build` workflow, and download
the `blocktracker-jar` artifact. Use the jar that does not end in `-sources`.
