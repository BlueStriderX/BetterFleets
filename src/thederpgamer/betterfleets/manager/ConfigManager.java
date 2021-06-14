package thederpgamer.betterfleets.manager;

import api.mod.config.FileConfiguration;
import thederpgamer.betterfleets.BetterFleets;

/**
 * ConfigManager
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
            "max-world-logs: 5"
    };


    public static void initialize(BetterFleets instance) {
        mainConfig = instance.getConfig("config");
        mainConfig.saveDefault(defaultMainConfig);
    }

    public static FileConfiguration getMainConfig() {
        return mainConfig;
    }
}