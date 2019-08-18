package electroblob.wizardry.client.renderer;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.entity.construct.EntityBubble;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class RenderBubble extends Render<EntityBubble> {

	private static final ResourceLocation particleTextures = new ResourceLocation("textures/particle/particles.png");
	private static final ResourceLocation darkOrbTexture = new ResourceLocation(Wizardry.MODID,
			"textures/entity/dark_orb.png");

	public RenderBubble(RenderManager renderManager){
		super(renderManager);
	}

	@Override
	public void doRender(EntityBubble entity, double par2, double par4, double par6, float par8, float par9){

		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		float yOffset = 0;

		if(WizardryUtilities.getRider(entity) != null){
			yOffset = WizardryUtilities.getRider(entity).height / 2;
		}

		GlStateManager.translate((float)par2, (float)par4 + yOffset, (float)par6);

		this.bindTexture(((EntityBubble)entity).isDarkOrb ? darkOrbTexture : particleTextures);
		float f6 = 1.0F;
		float f7 = 0.5F;
		float f8 = 0.5F;

		if(((EntityBubble)entity).isDarkOrb){
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

		float f11 = 3.0F;
		GlStateManager.scale(f11, f11, f11);

		double pixelwidth = (1.0d / 128);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		// tessellator.setColorRGBA_I(k1, 128);
		// buffer.normal(0.0F, 1.0F, 0.0F);

		if(((EntityBubble)entity).isDarkOrb){
			buffer.pos((double)(0.0F - f7), (double)(0.0F - f8), 0.0D).tex(0, 1).endVertex();
			buffer.pos((double)(f6 - f7), (double)(0.0F - f8), 0.0D).tex(1, 1).endVertex();
			buffer.pos((double)(f6 - f7), (double)(1.0F - f8), 0.0D).tex(1, 0).endVertex();
			buffer.pos((double)(0.0F - f7), (double)(1.0F - f8), 0.0D).tex(0, 0).endVertex();
		}else{
			buffer.pos((double)(0.0F - f7), (double)(0.0F - f8), 0.0D).tex(pixelwidth, pixelwidth * 24).endVertex();
			buffer.pos((double)(f6 - f7), (double)(0.0F - f8), 0.0D).tex(pixelwidth * 8, pixelwidth * 24).endVertex();
			buffer.pos((double)(f6 - f7), (double)(1.0F - f8), 0.0D).tex(pixelwidth * 8, pixelwidth * 17).endVertex();
			buffer.pos((double)(0.0F - f7), (double)(1.0F - f8), 0.0D).tex(pixelwidth, pixelwidth * 17).endVertex();
		}

		tessellator.draw();

		GlStateManager.disableBlend();
		if(((EntityBubble)entity).isDarkOrb){
			GlStateManager.enableLighting();
		}
		GlStateManager.disableRescaleNormal();
		GlStateManager.popMatrix();

	}

	@Override
	protected ResourceLocation getEntityTexture(EntityBubble entity){
		return null;
	}

}
