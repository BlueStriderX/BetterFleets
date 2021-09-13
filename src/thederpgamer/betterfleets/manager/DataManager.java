package thederpgamer.betterfleets.manager;

import thederpgamer.betterfleets.data.PersistentData;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Abstract class for managing persistent mod data.
 *
 * @author TheDerpGamer
 * @version 1.0 - [09/13/2021]
 */
public abstract class DataManager<E extends PersistentData> {

    protected static final ConcurrentHashMap<String, PersistentData> dataMap = new ConcurrentHashMap<>();
}