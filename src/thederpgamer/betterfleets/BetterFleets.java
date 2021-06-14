package thederpgamer.betterfleets;

import api.mod.StarMod;
import thederpgamer.betterfleets.manager.ConfigManager;
import thederpgamer.betterfleets.manager.LogManager;

/**
 * BetterFleets mod main class.
 *
 * @author TheDerpGamer
 * @since 06/14/2021
 */
public class BetterFleets extends StarMod {

    //Instance
    private static BetterFleets instance;
    public static BetterFleets getInstance() {
        return instance;
    }
    public BetterFleets() {

    }
    public static void main(String[] args) {

    }

    @Override
    public void onEnable() {
        instance = this;
        ConfigManager.initialize(this);
        LogManager.initialize();
        registerListeners();
    }

    private void registerListeners() {

    }
}
