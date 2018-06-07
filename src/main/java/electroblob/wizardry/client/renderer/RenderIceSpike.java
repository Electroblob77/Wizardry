package electroblob.wizardry.client.renderer;

import org.lwjgl.opengl.GL11;

import electroblob.wizardry.entity.construct.EntityIceSpike;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class RenderIceSpike extends Render {

	private static final ResourceLocation texture = new ResourceLocation("wizardry:textures/entity/ice_spike.png");

	public RenderIceSpike(){

	}

	@Override
	public void doRender(Entity entity, double x, double y, double z, float fa, float partialTickTime) {

		GL11.glPushMatrix();
		
		GL11.glTranslatef((float)x, (float)y, (float)z);
		// Apparently, disabling lighting... doesn't disable lighting. Or at least, you can still set the brightness
		// with setLightmapTextureCoords.
        GL11.glDisable(GL11.GL_LIGHTING);
		
		int j = entity.getBrightnessForRender(partialTickTime);
        int k = j % 65536;
        int l = j / 65536;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)k / 1.0F, (float)l / 1.0F);
        
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

		Tessellator tessellator = Tessellator.instance;

		bindTexture(texture);

		// West face
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(0, 0,  0.5, 1, 1);
		tessellator.addVertexWithUV(0, 1,  0.5, 1, 0);
		tessellator.addVertexWithUV(0, 1, -0.5, 0, 0);
		tessellator.addVertexWithUV(0, 0, -0.5, 0, 1);
		tessellator.draw();
		// South face
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV( 0.5, 0, 0, 1, 1);
		tessellator.addVertexWithUV( 0.5, 1, 0, 1, 0);
		tessellator.addVertexWithUV(-0.5, 1, 0, 0, 0);
		tessellator.addVertexWithUV(-0.5, 0, 0, 0, 1);
		tessellator.draw();
		// East face
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(0, 0, -0.5, 0, 1);
		tessellator.addVertexWithUV(0, 1, -0.5, 0, 0);
		tessellator.addVertexWithUV(0, 1,  0.5, 1, 0);
		tessellator.addVertexWithUV(0, 0,  0.5, 1, 1);
		tessellator.draw();
		// North face
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(-0.5, 0, 0, 0, 1);
		tessellator.addVertexWithUV(-0.5, 1, 0, 0, 0);
		tessellator.addVertexWithUV( 0.5, 1, 0, 1, 0);
		tessellator.addVertexWithUV( 0.5, 0, 0, 1, 1);
		tessellator.draw();

        GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glPopMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return texture;
	}

}