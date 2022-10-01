package thederpgamer.betterfleets.utils;

import api.mod.StarLoader;

/**
 * Utilities for functionality with other mods.
 */

public class ModUtils {

	public static boolean checkForBetterChambers() {
		return StarLoader.getModFromName("BetterChambers") != null;
	}

	public static Class<?> getBetterChambersAPI() {
		if(checkForBetterChambers()) {
			try {
				return Class.forName("thederpgamer.betterchambers.api.BetterChambersAPI");
			} catch(ClassNotFoundException exception) {
				exception.printStackTrace();
			}
		}
		return null;
	}

	private static Class<?> getBetterChambersElementManager() {
		if(checkForBetterChambers()) {
			try {
				return Class.forName("thederpgamer.betterchambers.element.ElementManager");
			} catch(ClassNotFoundException exception) {
				exception.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * Fetches a block ID from BetterChambers.
	 *
	 * @param name The name of the block.
	 * @return The ID of the block.
	 */
	public static short getBCID(String name) {
		Class<?> elementManager = getBetterChambersElementManager();
		if(elementManager != null) {
			try {
				Object block = elementManager.getMethod("getBlock", String.class).invoke(null, name);
				return (short) block.getClass().getMethod("getId").invoke(block);
			} catch(Exception exception) {
				exception.printStackTrace();
			}
		}
		return 0;
	}
}