package rpg.world.playerinfo.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import rpg.api.JobApi;
import rpg.api.SkillApi;
import rpg.api.SkillSummary;
import rpg.core.player.PlayerDataManager;
import rpg.gui.framework.Gui;
import rpg.gui.framework.GuiButton;
import rpg.quest.model.ObjectiveType;
import rpg.quest.model.PlayerQuestComponent;
import rpg.quest.model.PlayerQuestProgress;
import rpg.quest.model.QuestData;
import rpg.quest.model.QuestObjective;
import rpg.quest.model.QuestState;
import rpg.quest.repository.QuestRepository;
import rpg.util.ItemBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The nether-star "プレイヤー情報" screen: a single read-only overview of the player's
 * accepted quests, job, skills and (placeholder, not yet implemented) achievements. Quest
 * data is read directly from orelia-world's own quest module; job/skill data comes from
 * orelia-core through {@link JobApi}/{@link SkillApi} since those modules live in the
 * other plugin.
 */
public final class PlayerInfoGuiScreen {

    private static final int[] QUEST_SLOTS = {1, 2, 3, 4, 5, 6, 7, 8};
    private static final int JOB_SLOT = 10;
    private static final int[] SKILL_SLOTS = {19, 20, 21, 22, 23, 24, 25, 26};
    private static final int ACHIEVEMENT_SLOT = 28;

    private final QuestRepository questRepository;
    private final PlayerDataManager playerDataManager;
    private final JobApi jobApi;
    private final SkillApi skillApi;

    public PlayerInfoGuiScreen(QuestRepository questRepository, PlayerDataManager playerDataManager,
                                JobApi jobApi, SkillApi skillApi) {
        this.questRepository = questRepository;
        this.playerDataManager = playerDataManager;
        this.jobApi = jobApi;
        this.skillApi = skillApi;
    }

    public Gui build(Player player) {
        Gui gui = new Gui("&8プレイヤー情報", 36);

        gui.set(0, GuiButton.display(new ItemBuilder(Material.WRITABLE_BOOK).name("&b受注中のクエスト").build()));
        buildQuestButtons(gui, player);

        gui.set(9, GuiButton.display(new ItemBuilder(Material.LEATHER_HELMET).name("&bジョブ").build()));
        gui.set(JOB_SLOT, buildJobButton(player));

        gui.set(18, GuiButton.display(new ItemBuilder(Material.ENCHANTED_BOOK).name("&bスキル").build()));
        buildSkillButtons(gui, player);

        gui.set(27, GuiButton.display(new ItemBuilder(Material.NETHER_STAR).name("&b実績").build()));
        gui.set(ACHIEVEMENT_SLOT, GuiButton.display(new ItemBuilder(Material.GRAY_DYE)
                .name("&7準備中")
                .lore("&8実績機能は今後実装予定です。")
                .build()));

        return gui;
    }

    private void buildQuestButtons(Gui gui, Player player) {
        PlayerQuestComponent component = playerDataManager.get(player.getUniqueId())
                .flatMap(d -> d.component(PlayerQuestComponent.class))
                .orElse(null);
        Map<String, PlayerQuestProgress> activeQuests = component == null ? Map.of() : component.getActiveQuests();

        if (activeQuests.isEmpty()) {
            gui.set(QUEST_SLOTS[0], GuiButton.display(new ItemBuilder(Material.PAPER)
                    .name("&7受注中のクエストはありません")
                    .build()));
            return;
        }

        int slotIndex = 0;
        for (var entry : activeQuests.entrySet()) {
            if (slotIndex >= QUEST_SLOTS.length) {
                break;
            }
            QuestData quest = questRepository.findById(entry.getKey()).orElse(null);
            if (quest == null) {
                continue;
            }
            gui.set(QUEST_SLOTS[slotIndex++], GuiButton.display(new ItemBuilder(Material.WRITTEN_BOOK)
                    .name("&e" + quest.getName())
                    .lore(questLore(quest, entry.getValue()))
                    .build()));
        }
    }

    private List<String> questLore(QuestData quest, PlayerQuestProgress progress) {
        List<String> lore = new ArrayList<>(quest.getDescription());
        lore.add("");
        lore.add("&7状態: " + stateLabel(progress.getState()));
        List<QuestObjective> objectives = quest.getObjectives();
        for (int i = 0; i < objectives.size(); i++) {
            QuestObjective objective = objectives.get(i);
            lore.add("&7" + objectiveLabel(objective) + ": " + progress.getProgress(i) + "/" + objective.getRequiredAmount());
        }
        return lore;
    }

    private String stateLabel(QuestState state) {
        return switch (state) {
            case IN_PROGRESS -> "&f受注中";
            case ACHIEVED, AWAITING_REPORT -> "&a報告待ち";
            case COMPLETE -> "&a完了";
            case NOT_ACCEPTED -> "&7未受注";
        };
    }

    private String objectiveLabel(QuestObjective objective) {
        String target = objective.getTargetId() == null ? "" : " (" + objective.getTargetId() + ")";
        return switch (objective.getType()) {
            case KILL_MONSTER -> "モンスター討伐" + target;
            case KILL_BOSS -> "ボス討伐" + target;
            case COLLECT_ITEM -> "アイテム収集" + target;
            case DELIVER_ITEM -> "アイテム納品" + target;
            case REACH_LOCATION -> "目的地への到達";
            case TALK_NPC -> "NPCとの会話" + target;
            case CLEAR_DUNGEON -> "ダンジョン攻略" + target;
        };
    }

    private GuiButton buildJobButton(Player player) {
        String job = jobApi.getCurrentJob(player.getUniqueId()).orElse(null);
        return GuiButton.display(new ItemBuilder(job == null ? Material.BARRIER : Material.GOLDEN_HELMET)
                .name(job == null ? "&7未就業" : "&e" + job)
                .build());
    }

    private void buildSkillButtons(Gui gui, Player player) {
        List<SkillSummary> learned = skillApi.getLearnedSkills(player.getUniqueId());
        if (learned.isEmpty()) {
            gui.set(SKILL_SLOTS[0], GuiButton.display(new ItemBuilder(Material.PAPER)
                    .name("&7習得済みスキルはありません")
                    .build()));
            return;
        }

        int slotIndex = 0;
        for (SkillSummary skill : learned) {
            if (slotIndex >= SKILL_SLOTS.length) {
                break;
            }
            gui.set(SKILL_SLOTS[slotIndex++], GuiButton.display(new ItemBuilder(Material.ENCHANTED_BOOK)
                    .name("&e" + skill.name())
                    .lore("&7Lv. " + skill.level() + " / " + skill.maxLevel())
                    .build()));
        }
    }
}
