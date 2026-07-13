# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this repo is

`orelia-world` is a Paper (Minecraft 1.21.x / Java 21) plugin jar. It is the **content
layer** of a 3-plugin split:

- `orelia-core` (separate repo) - combat/player/status foundation. **Required dependency**,
  must be installed and enabled before this plugin loads.
- **orelia-world** (this repo) - Quest, NPC, Dialogue, Story, Dungeon, Region, CutScene, Event.
- `orelia-extra` (separate repo) - later MMORPG features, not yet implemented.

Hard rule: orelia-world only ever calls into orelia-core through `rpg.api.*` interfaces
(published via Bukkit's `ServicesManager` at runtime, e.g. `StatusApi`, `ItemApi`,
`CombatApi`, `SkillApi`, `AccessoryApi`) or generic `rpg.core.*` / `rpg.database.*`
infrastructure (`ConfigManager`, `SchedulerService`, `PlayerDataManager`,
`PlayerDataComponent`, `DatabaseManager`, `SchemaOwner`). **Never** reach into orelia-core's
internal gameplay-module classes (e.g. `rpg.status.*`, `rpg.item.*`) directly. Money
(quest rewards, NPC shop) goes straight through Vault's `Economy`, the same way any other
Vault-integrated plugin would - there is no custom EconomyApi.

## Build

```
./gradlew build
```

Requires network access to `repo.papermc.io` (Paper API) and `jitpack.io` (resolves
`orelia-core` and `VaultAPI` straight from their GitHub repos - no shared monorepo/artifact
registry). Build output is a shadowed jar (`orelia-world-<version>.jar`) via the Shadow
plugin; `build` depends on `shadowJar`.

Tests: `./gradlew test` (JUnit 5 / jupiter, via `useJUnitPlatform()`). There are currently
no test sources in `src/test` - add them there if writing tests.

## Runtime config files

`config.yml`, `quests.yml`, `npc.yml`, `dungeons.yml`, `dialogues.yml`, `story.yml`,
`regions.yml`, `cutscenes.yml`, `events.yml` - all in `src/main/resources`, all registered
through `ConfigManager` and reloadable in-process via `/rpgworldadmin reload` (no restart).

## Architecture: module system

Everything is organized as a `WorldModule` (`rpg/world/core/module/WorldModule.java`):
`getName()`, `onEnable(OreliaWorldPlugin)`, `onDisable()`, `onReload()` (default no-op).
`WorldModuleManager` (`rpg/world/core/module/WorldModuleManager.java`) holds modules in
**registration order** and enables/reloads in that order, disables in reverse. This mirrors
orelia-core's own `ModuleManager` but is a separate interface parameterized on
`OreliaWorldPlugin` rather than shared with core.

`OreliaWorldPlugin.onEnable()` (`rpg/world/core/OreliaWorldPlugin.java`) is the composition
root:
1. Looks up orelia-core's `PlayerDataManager` from `ServicesManager` - hard-fails (disables
   the plugin) if orelia-core isn't enabled yet.
2. Builds its own `ConfigManager` and `SchedulerService` (these are generic per-plugin
   infrastructure reused as-is from orelia-core, not gameplay logic).
3. Registers modules **in dependency order** - registration order doubles as enable order:
   `RegionModule -> DialogueModule -> StoryModule -> EventModule -> CutSceneModule ->
   DungeonModule -> QuestModule -> NpcModule`. When adding a new module or changing what a
   module depends on at enable-time, this order matters.

Each top-level package under `rpg/` (`quest`, `dungeon`, `npc`) or `rpg/world/` (`dialogue`,
`story`, `event`, `cutscene`, `region`) is one module and follows the same internal shape:

- `<X>Module.java` - the `WorldModule` impl; wires everything together in `onEnable`,
  fetches required services from `ServicesManager` (throwing `IllegalStateException` if a
  hard dependency like `StatusApi` is missing), registers listeners/commands, and calls a
  private `reload<X>()` that (re-)registers the module's yml with `ConfigManager` and feeds
  it to the module's repository.
- `model/` - config-derived data records (e.g. `QuestData`) plus, for modules with
  per-player state, a `PlayerDataComponent` impl (e.g. `PlayerQuestComponent`) - the
  per-player in-memory state object managed by orelia-core's `PlayerDataManager`.
- `repository/` - two flavors depending on the module:
  - **static/config repositories** (e.g. `QuestRepository`, `DungeonRepository`) parse a
    `YamlConfiguration` into in-memory definitions on `load(config)`.
  - **player-state repositories** (e.g. `PlayerQuestRepository`) implement
    `rpg.database.repository.SchemaOwner` (`createSchemaIfNotExists()`), own their own SQL
    tables, and expose `loadOrCreate(uuid)` / `save(component)`. SQL is written to be
    dialect-switchable via `databaseManager.getType()` (`SQLITE` vs `MYSQL`), e.g. `ON
    CONFLICT ... DO NOTHING` vs `INSERT IGNORE`.
- `service/` - business logic (progress tracking, reward distribution, eligibility checks),
  taking repositories + `rpg.api.*` interfaces as constructor args.
- `listener/`, `command/`, `gui/`, `manager/` - present where relevant to the module.

A `PlayerDataComponent`-owning module registers its `QuestManager`-style loader with
`plugin.getPlayerDataManager().registerLoader(...)` during `onEnable` so the component is
loaded/saved automatically as part of orelia-core's player join/quit lifecycle - modules
never manage their own player join/quit listeners for this.

## Commands (see `plugin.yml`)

- `/rpgworldadmin reload` (`orelia.world.admin`, default op) - `WorldAdminCommand` ->
  `OreliaWorldPlugin.reload()` -> `configManager.reloadAll()` then
  `moduleManager.reloadAll()` (each module's `onReload()` re-reads its yml).
- `/rpgquest <list|abandon> ...` (`orelia.quest`, default true).
- `/dialoguechoice <index>` (`orelia.dialogue`, default true) - internal command invoked by
  clickable chat components in dialogue trees, not meant to be typed by players directly.

## Adding a new module

Follow the `QuestModule`/`DungeonModule` shape: implement `WorldModule`, add a config file
under `src/main/resources` + register it in `reload<X>()`, fetch any orelia-core services
via `ServicesManager` in `onEnable` (fail loud with `IllegalStateException` for hard
dependencies), and add the module to `OreliaWorldPlugin.onEnable()`'s registration list at
the correct point relative to its dependencies.

## Committing changes

When committing, also update README.md and README_EN.md accordingly.
