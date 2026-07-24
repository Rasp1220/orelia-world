<img src="https://orelia-mc.github.io/assets/logo_wide.jpg" />
<h1 align="center">Orelia World</h1>
<p align="center">Content Plugin of Orelia-MC</p>

## About

`orelia-world` is the content plugin (Paper 1.21.x / Java 21) of the Minecraft RPG plugin suite **Orelia**, covering quest, NPC, dialogue, story, dungeon, cutscene, and event systems.

Orelia is split into 3 plugins:

- [orelia-core](https://github.com/orelia-mc/orelia-core) — combat/player/status foundation (required dependency)
- **orelia-world** (this repo) — Quest, NPC, Dialogue, Story, Dungeon, CutScene, Event
- [orelia-extra](https://github.com/orelia-mc/orelia-extra) — later MMORPG features

Requires **orelia-core** to be installed and enabled first (`depend: [OreliaCore]` in `plugin.yml`). Talks to it only through `rpg.api` (published via Bukkit's `ServicesManager`) — never through orelia-core's internal module classes. Money goes through Vault directly, the same way any other Vault-integrated plugin would.

## Setup

```bash
./gradlew build
```

Requires network access to `repo.papermc.io` (Paper API) and `jitpack.io` (resolves orelia-core and Vault API straight from GitHub).

## Structure

- Config — `quests.yml`, `npc.yml`, `dungeons.yml`, `dialogues.yml`, `story.yml`, `cutscenes.yml`, `events.yml`, `config.yml`. Reload all of them with `/oladmin worldreload`.
- NPCs are no longer auto-spawned on startup. Run `/oladmin npc spawnall` to place every configured, not-yet-present NPC from `npc.yml` at once (safe to re-run, never duplicates). The job-change NPC is the one exception - place it individually at the sender's location with `/oladmin spawnnpc <npc-id>` (e.g. `/oladmin spawnnpc job_master`).
- The nether-star player-info menu's skill tab now opens orelia-core's weapon-skill screen (learn/level-up/socket skills onto the held weapon).
- Quests — prerequisite quests (`quests.yml`'s `prerequisite-quests`) already worked and now also notify the player when completing one unlocks another. Repeatable quests (`repeatable: true`) can set `cooldown-hours` to require a wait before re-accepting (unset/0 = instantly re-acceptable, same as before). `/ol quest list` now shows a progress bar per objective.
- Titles — a title earned from a quest reward (`reward.title`) can be viewed with `/ol title list` and equipped with `/ol title equip <title>` (`/ol title unequip` to remove it). The equipped title is exposed via `QuestApi#getEquippedTitle` for cross-plugin display - see orelia-serverutil's `{title}` placeholder for chat/tab-list.
