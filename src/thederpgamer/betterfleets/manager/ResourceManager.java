package thederpgamer.betterfleets.manager;

import api.utils.textures.StarLoaderTexture;
import org.schema.schine.graphicsengine.forms.Sprite;
import thederpgamer.betterfleets.BetterFleets;

import java.util.HashMap;

/**
 * Manages mod files and resources.
 *
 * @author TheDerpGamer
 * @since 07/02/2021
 */
public class ResourceManager {

    private static final String[] textureNames = {
            "repair-paste-fabricator-caps",
            "repair-paste-fabricator-sides",
            "repair-paste-fabricator-icon"
    };

    private static final String[] spriteNames = {
            "tactical-map-indicators",
            "critical-overlay-sprite"
    };

    private static final HashMap<String, StarLoaderTexture> textureMap = new HashMap<>();
    private static final HashMap<String, Sprite> spriteMap = new HashMap<>();

    public static void loadResources(final BetterFleets instance) {
        StarLoaderTexture.runOnGraphicsThread(new Runnable() {
            @Override
            public void run() {
                //Load Textures
                for(String textureName : textureNames) {
                    try {
                        if(textureName.endsWith("icon")) textureMap.put(textureName, StarLoaderTexture.newIconTexture(instance.getJarBufferedImage("thederpgamer/betterfleets/resources/textures/" + textureName + ".png")));
                        else textureMap.put(textureName, StarLoaderTexture.newBlockTexture(instance.getJarBufferedImage("thederpgamer/betterfleets/resources/textures/" + textureName + ".png")));
                    } catch(Exception exception) {
                        LogManager.logException("Failed to load texture \"" + textureName + "\"", exception);
                    }
                }

                //Load Sprites
                for(String spriteName : spriteNames) {
                    try {
                        spriteMap.put(spriteName, StarLoaderTexture.newSprite(instance.getJarBufferedImage("thederpgamer/betterfleets/resources/sprites/" + spriteName + ".png"), BetterFleets.getInstance(), spriteName, false, false));
                        spriteMap.get(spriteName).setName(spriteName);
                    } catch(Exception exception) {
                        LogManager.logException("Failed to load sprite \"" + spriteName + "\"", exception);
                    }
                }
            }
        });
    }

    public static StarLoaderTexture getTexture(String name) {
        return textureMap.get(name);
    }

    public static Sprite getSprite(String name) {
        Sprite sprite = StarLoaderTexture.newSprite(BetterFleets.getInstance().getJarBufferedImage("thederpgamer/betterfleets/resources/sprites/" + name + ".png"), BetterFleets.getInstance(), name, false, false);
        sprite.setName(name);
        return sprite;
        //return spriteMap.get(name);
    }
}
