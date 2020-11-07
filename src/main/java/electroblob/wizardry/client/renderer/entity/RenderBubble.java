package electroblob.wizardry.client.renderer.entity;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.client.DrawingUtils;
import electroblob.wizardry.entity.construct.EntityBubble;
import electroblob.wizardry.util.EntityUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class RenderBubble extends Render<EntityBubble> {

	private static final ResourceLocation PARTICLE_TEXTURES = new ResourceLocation("textures/particle/particles.png");
	private static final ResourceLocation ENTRAPMENT_TEXTURE = new ResourceLocation(Wizardry.MODID, "textures/entity/entrapment.png");

	public RenderBubble(RenderManager renderManager){
		super(renderManager);
	}

	@Override
	public void doRender(EntityBubble entity, double x, double y, double z, float entityYaw, float partialTicks){

		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		float yOffset = 0;

		Entity rider = EntityUtils.getRider(entity);

		if(rider != null){
			yOffset = rider.height / 2;
		}

		GlStateManager.translate((float)x, (float)y + yOffset, (float)z);

		this.bindTexture(entity.isDarkOrb ? ENTRAPMENT_TEXTURE : PARTICLE_TEXTURES);
		float f6 = 1.0F;
		float f7 = 0.5F;
		float f8 = 0.5F;

		if(entity.isDarkOrb){
			GlStateManager.disableLighting();
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
		}else{
			int j = entity.getBrightnessForRender();
			int k = j % 65536;
			int l = j / 65536;
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)k / 1.0F, (float)l / 1.0F);
		}

		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

		// This counteracts the reverse rotation behaviour when in front f5 view.
		// Fun fact: this is a bug with vanilla too! Look at a snowball in front f5 view, for example.
		float yaw = Minecraft.getMinecraft().gameSettings.thirdPersonView == 2 ? this.renderManager.playerViewX
				: -this.renderManager.playerViewX;
		GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(yaw, 1.0F, 0.0F, 0.0F);

		// Bubble 'bursts' so doesn't shrink when is disappears
		float s = 3 * DrawingUtils.smoothScaleFactor(entity.isDarkOrb ? entity.lifetime : -1, entity.ticksExisted, partialTicks, 10, 10);
		GlStateManager.scale(s, s, s);

		double pixelwidth = (1.0d / 128);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

		if(entity.isDarkOrb){
			buffer.pos(0.0F - f7, 0.0F - f8, 0.0D).tex(0, 1).endVertex();
			buffer.pos(f6 - f7, 0.0F - f8, 0.0D).tex(1, 1).endVertex();
			buffer.pos(f6 - f7, 1.0F - f8, 0.0D).tex(1, 0).endVertex();
			buffer.pos(0.0F - f7, 1.0F - f8, 0.0D).tex(0, 0).endVertex();
		}else{
			buffer.pos(0.0F - f7, 0.0F - f8, 0.0D).tex(pixelwidth, pixelwidth * 24).endVertex();
			buffer.pos(f6 - f7, 0.0F - f8, 0.0D).tex(pixelwidth * 8, pixelwidth * 24).endVertex();
			buffer.pos(f6 - f7, 1.0F - f8, 0.0D).tex(pixelwidth * 8, pixelwidth * 17).endVertex();
			buffer.pos(0.0F - f7, 1.0F - f8, 0.0D).tex(pixelwidth, pixelwidth * 17).endVertex();
		}

		tessellator.draw();

		GlStateManager.disableBlend();
		if(entity.isDarkOrb) GlStateManager.enableLighting();
		GlStateManager.disableRescaleNormal();
		GlStateManager.popMatrix();

	}

	@Override
	protected ResourceLocation getEntityTexture(EntityBubble entity){
		return null;
	}

}
