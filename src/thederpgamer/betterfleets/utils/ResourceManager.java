package thederpgamer.betterfleets.utils;

import api.utils.textures.StarLoaderTexture;
import org.schema.schine.graphicsengine.core.ResourceException;
import org.schema.schine.graphicsengine.forms.Sprite;
import org.schema.schine.resource.ResourceLoader;
import thederpgamer.betterfleets.BetterFleets;
import java.io.IOException;
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
            "repair-paste-fabricator-sides"
    };

    private static final String[] spriteNames = {

    };

    private static final String[] modelNames = {

    };

    private static HashMap<String, StarLoaderTexture> textureMap = new HashMap<>();
    private static HashMap<String, Sprite> spriteMap = new HashMap<>();

    public static void loadResources(final BetterFleets instance, final ResourceLoader loader) {

        StarLoaderTexture.runOnGraphicsThread(new Runnable() {
            @Override
            public void run() {
                //Load Textures
                for(String textureName : textureNames) {
                    try {
                        textureMap.put(textureName, StarLoaderTexture.newBlockTexture(instance.getJarBufferedImage("thederpgamer/betterfleets/resources/textures/" + textureName + ".png")));
                    } catch(Exception exception) {
                        LogManager.logException("Failed to load texture \"" + textureName + "\"", exception);
                    }
                }

                //Load Sprites
                for(String spriteName : spriteNames) {
                    try {
                        Sprite sprite = StarLoaderTexture.newSprite(instance.getJarBufferedImage("thederpgamer/decor/resources/sprites/" + spriteName + ".png"), instance, spriteName);
                        sprite.setName(spriteName);
                        spriteMap.put(spriteName, sprite);
                    } catch(Exception exception) {
                        LogManager.logException("Failed to load sprite \"" + spriteName + "\"", exception);
                    }
                }

                //Load models
                for(String modelName : modelNames) {
                    try {
                        loader.getMeshLoader().loadModMesh(instance, modelName, instance.getJarResource("thederpgamer/decor/resources/models/" + modelName + ".zip"), null);
                    } catch(ResourceException | IOException exception) {
                        LogManager.logException("Failed to load model \"" + modelName + "\"", exception);
                    }
                }
            }
        });
    }

    public static StarLoaderTexture getTexture(String name) {
        return textureMap.get(name);
    }

    public static Sprite getSprite(String name) {
        return spriteMap.get(name);
    }
}
