package electroblob.wizardry.client.renderer.entity;

import electroblob.wizardry.client.DrawingUtils;
import electroblob.wizardry.entity.construct.EntityHealAura;
import electroblob.wizardry.entity.construct.EntityMagicConstruct;
import electroblob.wizardry.util.AllyDesignationSystem;
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
import org.lwjgl.opengl.GL11;

public class RenderSigil extends Render<EntityMagicConstruct> {

	private final ResourceLocation texture;
	private float scale;
	private boolean invisibleToEnemies;

	public RenderSigil(RenderManager renderManager, ResourceLocation texture, float scale, boolean invisibleToEnemies){
		super(renderManager);
		this.texture = texture;
		this.scale = scale;
		this.invisibleToEnemies = invisibleToEnemies;
	}

	@Override
	public void doRender(EntityMagicConstruct entity, double x, double y, double z, float entityYaw, float partialTicks){

		// Makes the sigil invisible to enemies of the player that created it
		if(this.invisibleToEnemies){
			// Unfortunately we can't access the caster's allies if they're not online, it only works the other way round
			if(entity.getCaster() instanceof EntityPlayer && !AllyDesignationSystem
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

		GlStateManager.translate((float)x, (float)y + yOffset, (float)z);

		this.bindTexture(texture);
		float f6 = 1.0F;
		float f7 = 0.5F;
		float f8 = 0.5F;

		GlStateManager.rotate(-90, 1, 0, 0);

		// Healing aura rotates slowly
		if(entity instanceof EntityHealAura) GlStateManager.rotate(entity.ticksExisted / 3.0f, 0, 0, 1);

		float s = DrawingUtils.smoothScaleFactor(entity.lifetime, entity.ticksExisted, partialTicks, 10, 10);
		GlStateManager.scale(scale * s, scale * s, scale * s);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

		buffer.pos(0.0F - f7, 0.0F - f8, 0.01).tex(0, 1).endVertex();
		buffer.pos(f6   - f7, 0.0F - f8, 0.01).tex(1, 1).endVertex();
		buffer.pos(f6   - f7, 1.0F - f8, 0.01).tex(1, 0).endVertex();
		buffer.pos(0.0F - f7, 1.0F - f8, 0.01).tex(0, 0).endVertex();

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
