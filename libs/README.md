# libs/ Folder

**This project does not require any JARs in this folder to build.**

Cobblemon is supplied via **Maven** in `build.gradle` (Impact Maven repo). The file `cobblemon-1.7.1-neoforge.jar` here is a **placeholder** (minimal JAR) so the IDE does not report a missing library; the real Cobblemon dependency comes from Maven. Do not replace it with a full Cobblemon JAR unless you switch to the local JAR option in `build.gradle`.

If your IDE reports a missing library like `libs/cobblemon-1.7.1-neoforge.jar`:

1. **VS Code**: Run **Java: Clean Java Language Server Workspace** from the command palette, then reload the window. Ensure the project is imported from Gradle (not an old Eclipse classpath).
2. **IntelliJ**: **File → Invalidate Caches → Invalidate and Restart**, then **Reload Gradle Project**.
3. **Command line**: `./gradlew clean build --refresh-dependencies` (build should succeed without any files in `libs/`).

Optional fallback: if Maven is unavailable, you can use a local JAR by uncommenting the `files("libs/cobblemon-1.7.2-neoforge.jar")` lines in `build.gradle` and placing the matching JAR here (filename must match exactly).
