<img src="https://orelia-mc.github.io/assets/logo_wide.jpg" />
<h1 align="center">Orelia World</h1>
<p align="center">Content Plugin of Orelia-MC</p>

## About

`orelia-world` は Minecraft RPG プラグイン群 **Orelia** のコンテンツプラグイン(Paper 1.21.x / Java 21)です。クエスト・NPC・ダイアログ・ストーリー・ダンジョン・カットシーン・イベントを扱います。

Orelia は 3 プラグイン構成です。

- [orelia-core](https://github.com/orelia-mc/orelia-core) — 戦闘・プレイヤー・ステータスの基盤(必須依存)
- **orelia-world**(本リポジトリ) — Quest, NPC, Dialogue, Story, Dungeon, CutScene, Event
- [orelia-extra](https://github.com/orelia-mc/orelia-extra) — 後発の MMORPG 系機能

起動には **orelia-core** が先にインストール・有効化されている必要があります(`plugin.yml` の `depend: [OreliaCore]`)。連携は `rpg.api`(Bukkit の `ServicesManager` で公開)経由のみで、orelia-core の内部モジュールクラスへ直接アクセスすることはありません。経済連携は他の Vault 対応プラグインと同様、Vault を直接利用します。

## Setup

```bash
./gradlew build
```

ビルドには `repo.papermc.io`(Paper API)と `jitpack.io`(orelia-core・Vault API を GitHub から解決)へのネットワークアクセスが必要です。

## Structure

- 設定ファイル — `quests.yml`, `npc.yml`, `dungeons.yml`, `dialogues.yml`, `story.yml`, `cutscenes.yml`, `events.yml`, `config.yml`。`/oladmin worldreload` で一括リロードできます。
- NPCは起動時に自動スポーンしません。`/oladmin npc spawnall` で `npc.yml` に設定済み・まだ出現していない全NPCをまとめて設置します(再実行しても重複しません)。職業指南役(job-change NPC)だけは対象外で、`/oladmin spawnnpc <npc-id>`(例: `/oladmin spawnnpc job_master`)で実行者の足元に個別に手動配置してください。
- ネザースターのプレイヤー情報メニュー(スキルタブ)から orelia-core の武器スキル画面(習得・レベルアップ・武器へのスキル装着)を開けます。
