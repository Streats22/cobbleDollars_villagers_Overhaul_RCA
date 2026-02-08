# CobbleDollars Villagers Overhaul [RCA]

Standalone NeoForge mod that replaces the vanilla villager trade GUI with a CobbleDollars-style shop, adds Buy/Sell tabs with a balance display, and lets villagers accept CobbleDollars instead of emeralds.

## Requirements
- Minecraft 1.21.1 (NeoForge 21.1.215)
- Cobblemon 1.7.2+ (NeoForge)
- CobbleDollars (installed as a mod)
- KotlinForge (required by Cobblemon)

## Setup (Development)
1. Place the CobbleDollars JAR in `libs/` as `cobbledollars-<version>.jar`.
2. Run `gradlew runClient`.

## Notes
- The shop UI opens when interacting with villagers that have a profession (not NONE/NITWIT).
- Configuration lives in the standard NeoForge config folder.
