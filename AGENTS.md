# AGENTS.md

## Cursor Cloud specific instructions

This is a **NeoForge Minecraft mod** (Java 21, Gradle 9.2.1) targeting Minecraft 1.21.1.

### Key commands

| Task | Command |
|------|---------|
| Build | `./gradlew build` |
| Check/Lint | `./gradlew check` |
| Clean + rebuild | `./gradlew clean build --refresh-dependencies` |
| Run client (needs display) | `./gradlew runClient` |
| Run server (headless) | `./gradlew runServer` |
| Run game tests | `./gradlew runGameTestServer` |
| List all tasks | `./gradlew tasks` |

### Non-obvious caveats

- **EULA**: Before running `./gradlew runServer` or `runClient`, ensure `run/eula.txt` exists with `eula=true`. Without it, the Minecraft server will refuse to start.
- **CobbleDollars dependency**: The mod requires the CobbleDollars JAR in `libs/` (matching `cobbledollars-*.jar`) for full runtime testing. Without it, the server/client will crash at mod loading with a dependency error. The build (`./gradlew build`) succeeds without it.
- **Cobblemon**: Supplied via Maven (Impact Maven repo) — no local JAR needed. The `cobblemon-1.7.1-neoforge.jar` in `libs/` is a placeholder for IDE resolution only.
- **KotlinForge**: Automatically copied to `run/mods/` by the `copyKotlinForgeToRunMods` Gradle task (runs before `runClient`/`runServer`).
- **No unit tests**: The project has no JUnit-style test sources. `./gradlew check` and `./gradlew test` succeed trivially. Game testing uses `./gradlew runGameTestServer`.
- **First build is slow**: The initial `./gradlew build` downloads ~800 MB of Minecraft assets, NeoForge tooling, and decompiles Minecraft sources. Subsequent builds are cached and take a few seconds.
- **`--no-daemon` flag**: Useful in CI/cloud environments to avoid lingering Gradle daemons.
