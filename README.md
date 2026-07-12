# orelia-world

Orelia content plugin (Paper 1.21.x / Java 21) - quest, NPC, dialogue, story, dungeon,
cutscene, event.

Part of the Orelia 3-plugin split:

- [orelia-core](https://github.com/orelia-mc/orelia-core) - combat/player/status foundation (required dependency)
- **orelia-world** (this repo) - Quest, NPC, Dialogue, Story, Dungeon, CutScene, Event
- [orelia-extra](https://github.com/rasp1220/orelia-extra) - later MMORPG features, not yet implemented

Requires **orelia-core** to be installed and enabled first (`depend: [OreliaCore]` in
`plugin.yml`). Talks to it only through `rpg.api` (published via Bukkit's
`ServicesManager`) - never through orelia-core's internal module classes. Money goes
through Vault directly, the same way any other Vault-integrated plugin would.

## Building

```
./gradlew build
```

Requires network access to `repo.papermc.io` (Paper API) and `jitpack.io` (resolves
orelia-core and Vault API straight from GitHub).

## Config

`quests.yml`, `npc.yml`, `dungeons.yml`, `dialogues.yml`, `story.yml`,
`cutscenes.yml`, `events.yml`, `config.yml`. Reload all of them with `/oladmin worldreload`.
