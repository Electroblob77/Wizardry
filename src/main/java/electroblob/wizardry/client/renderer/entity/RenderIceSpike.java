package electroblob.wizardry.client.renderer.entity;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.entity.construct.EntityIceSpike;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class RenderIceSpike extends Render<EntityIceSpike> {

	private static final ResourceLocation texture = new ResourceLocation(Wizardry.MODID,
			"textures/entity/ice_spike.png");

	public RenderIceSpike(RenderManager renderManager){
		super(renderManager);
	}

	@Override
	public void doRender(EntityIceSpike entity, double x, double y, double z, float yaw, float partialTickTime){

		GlStateManager.pushMatrix();

		GlStateManager.translate((float)x, (float)y, (float)z);
		GlStateManager.rotate(entity.rotationYaw - 90.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(entity.rotationPitch - 90, 0.0F, 0.0F, 1.0F);

		GlStateManager.disableLighting();

		int j = entity.getBrightnessForRender();
		int k = j % 65536;
		int l = j / 65536;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)k / 1.0F, (float)l / 1.0F);

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();

		bindTexture(texture);

		// West face
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		buffer.pos(0, 0, 0.5).tex(1, 1).endVertex();
		buffer.pos(0, 1, 0.5).tex(1, 0).endVertex();
		buffer.pos(0, 1, -0.5).tex(0, 0).endVertex();
		buffer.pos(0, 0, -0.5).tex(0, 1).endVertex();
		tessellator.draw();
		// South face
		buffer.begin(GL11.GL_QUADS, net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_TEX);
		buffer.pos(0.5, 0, 0).tex(1, 1).endVertex();
		buffer.pos(0.5, 1, 0).tex(1, 0).endVertex();
		buffer.pos(-0.5, 1, 0).tex(0, 0).endVertex();
		buffer.pos(-0.5, 0, 0).tex(0, 1).endVertex();
		tessellator.draw();
		// East face
		buffer.begin(GL11.GL_QUADS, net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_TEX);
		buffer.pos(0, 0, -0.5).tex(0, 1).endVertex();
		buffer.pos(0, 1, -0.5).tex(0, 0).endVertex();
		buffer.pos(0, 1, 0.5).tex(1, 0).endVertex();
		buffer.pos(0, 0, 0.5).tex(1, 1).endVertex();
		tessellator.draw();
		// North face
		buffer.begin(GL11.GL_QUADS, net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_TEX);
		buffer.pos(-0.5, 0, 0).tex(0, 1).endVertex();
		buffer.pos(-0.5, 1, 0).tex(0, 0).endVertex();
		buffer.pos(0.5, 1, 0).tex(1, 0).endVertex();
		buffer.pos(0.5, 0, 0).tex(1, 1).endVertex();
		tessellator.draw();

		GlStateManager.enableLighting();
		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityIceSpike entity){
		return texture;
	}

}