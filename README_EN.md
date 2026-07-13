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
