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
            "repair-paste-fabricator/repair-paste-fabricator-icon",
            "repair-paste-fabricator/repair-paste-fabricator-caps",
            "repair-paste-fabricator/repair-paste-fabricator-sides"
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
                for(String texturePath : textureNames) {
                    String textureName = texturePath.substring(texturePath.lastIndexOf('/') + 1);
                    try {
                        if(textureName.endsWith("icon")) {
                            textureMap.put(textureName, StarLoaderTexture.newIconTexture(instance.getJarBufferedImage("thederpgamer/betterfleets/resources/textures/" + texturePath + ".png")));
                        } else {
                            textureMap.put(textureName, StarLoaderTexture.newBlockTexture(instance.getJarBufferedImage("thederpgamer/betterfleets/resources/textures/" + texturePath + ".png")));
                        }
                    } catch(Exception exception) {
                        LogManager.logException("Failed to load texture \"" + texturePath + "\"", exception);
                    }
                }

                //Load Sprites
                for(String spriteName : spriteNames) {
                    try {
                        Sprite sprite = StarLoaderTexture.newSprite(instance.getJarBufferedImage("thederpgamer/betterfleets/resources/sprites/" + spriteName + ".png"), instance, spriteName);
                        sprite.setName(spriteName);
                        spriteMap.put(spriteName, sprite);
                    } catch(Exception exception) {
                        LogManager.logException("Failed to load sprite \"" + spriteName + "\"", exception);
                    }
                }

                //Load models
                for(String modelName : modelNames) {
                    try {
                        loader.getMeshLoader().loadModMesh(instance, modelName, instance.getJarResource("thederpgamer/betterfleets/resources/models/" + modelName + ".zip"), null);
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
