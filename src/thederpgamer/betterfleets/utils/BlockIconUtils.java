package thederpgamer.betterfleets.utils;

import api.utils.textures.StarLoaderTexture;
import com.bulletphysics.linearmath.Transform;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.util.vector.Matrix4f;
import org.schema.game.client.view.cubes.shapes.BlockStyle;
import org.schema.game.client.view.tools.SingleBlockDrawer;
import org.schema.game.common.data.element.ElementInformation;
import org.schema.game.common.data.element.ElementKeyMap;
import org.schema.schine.graphicsengine.core.*;
import org.schema.schine.graphicsengine.core.settings.EngineSettings;
import org.schema.schine.graphicsengine.forms.gui.GUIElement;

import javax.imageio.ImageIO;
import javax.vecmath.Matrix3f;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;

/**
 * <Description>
 *
 * @author TheDerpGamer
 * @version 1.0 - [03/07/2022]
 */
public class BlockIconUtils {

	private final Transform orientation = new Transform();
	private final Transform orientationTmp = new Transform();
	private final Matrix3f rot = new Matrix3f();
	private final Transform mView = new Transform();
	private final FloatBuffer fb = BufferUtils.createFloatBuffer(16);
	private final float[] ff = new float[16];
	int xMod, yMod;
	private boolean write = false;

	public BlockIconUtils() {
		orientation.setIdentity();
		orientationTmp.setIdentity();
	}

	public static void createBlockIcon(final ElementInformation elementInfo) {
		StarLoaderTexture.runOnGraphicsThread(new Runnable() {
			@Override
			public void run() {
				try {
					BufferedImage image = (new BlockIconUtils()).bake(elementInfo.id);
					StarLoaderTexture texture = StarLoaderTexture.newIconTexture(image);
					elementInfo.setBuildIconNum(texture.getTextureId());
				} catch(Exception exception) {
					exception.printStackTrace();
				}
			}
		});
	}

	public BufferedImage bake(short id) throws GLException, IOException {
		FrameBufferObjects fb = new FrameBufferObjects("IconBakery", 1024, 1024);
		fb.initialize();
		fb.enable();
		GL11.glClearColor(0, 0, 0, 0);
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);
		int sizeX = (Integer) EngineSettings.ICON_BAKERY_SINGLE_RESOLUTION.getCurrentState();
		int sizeY = (Integer) EngineSettings.ICON_BAKERY_SINGLE_RESOLUTION.getCurrentState();
		xMod = sizeX / 2;
		yMod = sizeY / 2;
		GL11.glViewport(0, 0, sizeX, sizeY);
		ElementInformation info = ElementKeyMap.getInfo(id);
		write = true;
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);
		drawSingle(id, sizeX / 2, sizeX, sizeY);
		write = false;

		String path = DataUtils.getResourcesPath() + "/" + info.getName().replaceAll(" ", "_");
		GlUtil.writeScreenToDisk(path, "png", sizeX, sizeY, 4, fb);
		GL11.glViewport(0, 0, GLFrame.getWidth(), GLFrame.getHeight());
		fb.disable();
		fb.cleanUp();
		File imageFile = new File(path + ".png");
		if(imageFile.exists()) {
			BufferedImage bufferedImage = ImageIO.read(imageFile);
			imageFile.delete();
			return bufferedImage;
		}
		return null;
	}

	private void drawSingle(short e, float size, int width, int height) {
		Matrix4f modelviewMatrix = Controller.modelviewMatrix;
		fb.rewind();
		modelviewMatrix.store(fb);
		fb.rewind();
		fb.get(ff);
		mView.setFromOpenGLMatrix(ff);
		mView.origin.set(0, 0, 0);
		GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT);

		if(write) GUIElement.enableOrthogonal3d(width, height);
		else GUIElement.enableOrthogonal3d();
		GlUtil.glDisable(GL11.GL_DEPTH_TEST);
		GlUtil.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
		GlUtil.glPushMatrix();
		GlUtil.translateModelview(xMod, yMod, 0);
		GlUtil.scaleModelview(size, -size, size);
		if(ElementKeyMap.getInfo(e).getBlockStyle() == BlockStyle.SPRITE) {
			orientationTmp.basis.set(mView.basis);
			mView.basis.setIdentity();
		} else {
			rot.set(orientation.basis);
			mView.basis.mul(rot);
		}

		GlUtil.glMultMatrix(mView);
		if(ElementKeyMap.getInfo(e).getBlockStyle() == BlockStyle.SPRITE) mView.basis.set(orientationTmp.basis);
		SingleBlockDrawer drawer = new SingleBlockDrawer();
		drawer.setLightAll(false);
		GlUtil.glPushMatrix();
		if(ElementKeyMap.getInfo(e).getBlockStyle() != BlockStyle.NORMAL) GlUtil.rotateModelview((Float) EngineSettings.ICON_BAKERY_BLOCKSTYLE_ROTATE_DEG.getCurrentState(), 0, 1, 0);
		drawer.drawType(e);
		GlUtil.glPopMatrix();
		GlUtil.glPopMatrix();

		GUIElement.disableOrthogonal();
		GlUtil.glEnable(GL11.GL_LIGHTING);
		GlUtil.glDisable(GL11.GL_NORMALIZE);
		GlUtil.glEnable(GL11.GL_DEPTH_TEST);
	}
}
