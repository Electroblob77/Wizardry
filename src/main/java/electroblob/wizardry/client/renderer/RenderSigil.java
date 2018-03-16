package electroblob.wizardry.client.renderer;

import org.lwjgl.opengl.GL11;

import electroblob.wizardry.entity.construct.EntityHealAura;
import electroblob.wizardry.entity.construct.EntityMagicConstruct;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class RenderSigil extends Render<EntityMagicConstruct> {

	private final ResourceLocation texture;
	private float scale = 1.0f;
	private boolean invisibleToEnemies;

	public RenderSigil(RenderManager renderManager, ResourceLocation texture, float scale, boolean invisibleToEnemies){
		super(renderManager);
		this.texture = texture;
		this.scale = scale;
		this.invisibleToEnemies = invisibleToEnemies;
	}

	@Override
	public void doRender(EntityMagicConstruct entity, double par2, double par4, double par6, float par8, float par9){

		// Makes the sigil invisible to enemies of the player that created it
		if(this.invisibleToEnemies){

			if(entity.getCaster() instanceof EntityPlayer && !WizardryUtilities
					.isPlayerAlly((EntityPlayer)entity.getCaster(), Minecraft.getMinecraft().player)){
				return;
			}
		}

		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.disableLighting();
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		float yOffset = 0;

		GlStateManager.translate((float)par2, (float)par4 + yOffset, (float)par6);

		this.bindTexture(texture);
		float f6 = 1.0F;
		float f7 = 0.5F;
		float f8 = 0.5F;

		GlStateManager.rotate(-90, 1, 0, 0);

		// Healing aura rotates slowly
		if(entity instanceof EntityHealAura) GlStateManager.rotate(entity.ticksExisted / 3.0f, 0, 0, 1);

		GlStateManager.scale(scale, scale, scale);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		// tessellator.setColorRGBA_I(k1, 128);
		// buffer.normal(0.0F, 1.0F, 0.0F);
		buffer.pos((double)(0.0F - f7), (double)(0.0F - f8), 0.01).tex(0, 1).endVertex();
		buffer.pos((double)(f6 - f7), (double)(0.0F - f8), 0.01).tex(1, 1).endVertex();
		buffer.pos((double)(f6 - f7), (double)(1.0F - f8), 0.01).tex(1, 0).endVertex();
		buffer.pos((double)(0.0F - f7), (double)(1.0F - f8), 0.01).tex(0, 0).endVertex();

		tessellator.draw();

		GlStateManager.disableBlend();
		GlStateManager.enableLighting();
		GlStateManager.disableRescaleNormal();
		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityMagicConstruct entity){
		return null;
	}

}
