package me.none030.mortiskitpvp.config;

import me.none030.mortiskitpvp.kitpvp.killstreak.KillStreakManager;
import me.none030.mortiskitpvp.kitpvp.killstreak.KillStreakMilestone;
import me.none030.mortiskitpvp.utils.MessageUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;

public class KillStreakConfig extends Config {

    public KillStreakConfig(ConfigManager configManager) {
        super("killstreak.yml", configManager);
    }

    @Override
    public void loadConfig() {
        File file = saveConfig();
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        getConfigManager().getManager().setKillStreakManager(new KillStreakManager(getConfigManager().getManager().getGameManager()));
        loadMilestones(config.getConfigurationSection("milestones"));
    }

    private void loadMilestones(ConfigurationSection milestones) {
        if (milestones == null) {
            return;
        }
        for (String id : milestones.getKeys(false)) {
            ConfigurationSection section = milestones.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            MessageUtils name = new MessageUtils(section.getString("name"));
            int requirement = section.getInt("requirement");
            MessageUtils message = new MessageUtils(section.getString("message"));
            List<String> commands = section.getStringList("commands");
            KillStreakMilestone milestone = new KillStreakMilestone(name.getMessage(), requirement, message.getMessage(), commands);
            getConfigManager().getManager().getKillStreakManager().getMilestones().add(milestone);
        }
    }
}
