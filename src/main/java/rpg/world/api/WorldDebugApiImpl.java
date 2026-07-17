package rpg.world.api;

import rpg.core.config.ConfigFile;
import rpg.core.config.ConfigManager;
import rpg.npc.repository.NpcRepository;
import rpg.quest.service.QuestProgressService;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

final class WorldDebugApiImpl implements WorldDebugApi {

    private final ConfigManager configManager;
    private final QuestProgressService questProgressService;
    private final NpcRepository npcRepository;

    WorldDebugApiImpl(ConfigManager configManager, QuestProgressService questProgressService, NpcRepository npcRepository) {
        this.configManager = configManager;
        this.questProgressService = questProgressService;
        this.npcRepository = npcRepository;
    }

    @Override
    public Set<String> listConfigFiles() {
        return configManager.getRegisteredFileNames();
    }

    @Override
    public Optional<String> getConfigValue(String fileName, String path) {
        ConfigFile file = tryGet(fileName);
        if (file == null || !file.get().contains(path)) {
            return Optional.empty();
        }
        return Optional.ofNullable(file.get().get(path)).map(String::valueOf);
    }

    @Override
    public boolean setConfigValue(String fileName, String path, String rawValue) {
        ConfigFile file = tryGet(fileName);
        if (file == null) {
            return false;
        }
        file.get().set(path, parseValue(rawValue));
        file.save();
        return true;
    }

    @Override
    public void saveConfig(String fileName) {
        ConfigFile file = tryGet(fileName);
        if (file != null) {
            file.save();
        }
    }

    @Override
    public List<String> describeConfigKeys(String fileName) {
        ConfigFile file = tryGet(fileName);
        if (file == null) {
            return List.of();
        }
        return file.get().getKeys(true).stream().sorted().toList();
    }

    @Override
    public boolean forceCompleteQuestObjectives(UUID playerId, String questId) {
        return questProgressService.forceCompleteObjectives(playerId, questId);
    }

    @Override
    public List<String> listNpcIds() {
        return npcRepository.getAll().keySet().stream().sorted().toList();
    }

    private ConfigFile tryGet(String fileName) {
        try {
            return configManager.get(fileName);
        } catch (IllegalStateException e) {
            return null;
        }
    }

    private Object parseValue(String rawValue) {
        if ("true".equalsIgnoreCase(rawValue) || "false".equalsIgnoreCase(rawValue)) {
            return Boolean.parseBoolean(rawValue);
        }
        try {
            return Long.parseLong(rawValue);
        } catch (NumberFormatException ignored) {
        }
        try {
            return Double.parseDouble(rawValue);
        } catch (NumberFormatException ignored) {
        }
        return rawValue;
    }
}
