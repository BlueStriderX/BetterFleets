package thederpgamer.betterfleets.manager;

import api.mod.config.FileConfiguration;
import thederpgamer.betterfleets.BetterFleets;

/**
 * Manages mod config files and values.
 *
 * @author TheDerpGamer
 * @since 06/07/2021
 */
public class ConfigManager {

    //Main Config
    private static FileConfiguration mainConfig;
    private static final String[] defaultMainConfig = {
            "debug-mode: false",
            "max-world-logs: 5",
            "fleet-command-update-interval: 15",
            "tactical-map-toggle-key: ,",
            "tactical-map-view-distance: 1.2"
    };

    //System Config
    private static FileConfiguration systemConfig;
    private static final String[] defaultSystemConfig = {
            "repair-paste-capacity-per-block: 10",
            "repair-paste-regen-per-block: 5",
            "repair-paste-power-consumed-per-block-resting: 5",
            "repair-paste-power-consumed-per-block-charging: 15",
            "flanking-damage-bonus-multiplier: 1.15",
            "flanking-damage-bonus-max-distance: 10000"
    };

    public static void initialize(BetterFleets instance) {
        mainConfig = instance.getConfig("config");
        mainConfig.saveDefault(defaultMainConfig);

        systemConfig = instance.getConfig("system-config");
        systemConfig.saveDefault(defaultSystemConfig);
    }

    public static FileConfiguration getMainConfig() {
        return mainConfig;
    }

    public static FileConfiguration getSystemConfig() {
        return systemConfig;
    }
}