package electroblob.wizardry.client.renderer.entity;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.entity.construct.EntityDecay;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class RenderDecay extends Render<EntityDecay> {

	private static final ResourceLocation[] TEXTURES = new ResourceLocation[10];

	public RenderDecay(RenderManager renderManager){
		super(renderManager);
		for(int i = 0; i < 10; i++){
			TEXTURES[i] = new ResourceLocation(Wizardry.MODID, "textures/entity/decay/decay_" + i + ".png");
		}
	}

	@Override
	public void doRender(EntityDecay entity, double x, double y, double z, float entityYaw, float partialTicks){

		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.disableLighting();
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		float yOffset = 0;

		GlStateManager.translate((float)x, (float)y + yOffset, (float)z);

		this.bindTexture(TEXTURES[entity.textureIndex]);
		float f6 = 1.0F;
		float f7 = 0.5F;
		float f8 = 0.5F;

		GlStateManager.rotate(-90, 1, 0, 0);

		float s = 2 * WizardryUtilities.smoothScaleFactor(entity.lifetime, entity.ticksExisted, partialTicks, 10, 50);
		GlStateManager.scale(s, s, s);

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
	protected ResourceLocation getEntityTexture(EntityDecay entity){
		return null;
	}

}
