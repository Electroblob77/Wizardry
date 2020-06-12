package electroblob.wizardry.client.renderer.entity;

import electroblob.wizardry.entity.construct.EntityFireRing;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class RenderFireRing extends Render<EntityFireRing> {

	private final ResourceLocation texture;
	private float scale;

	public RenderFireRing(RenderManager renderManager, ResourceLocation texture, float scale){
		super(renderManager);
		this.texture = texture;
		this.scale = scale;
	}

	@Override
	public void doRender(EntityFireRing entity, double x, double y, double z, float entityYaw, float partialTicks){

		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.disableLighting();
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

		float yOffset = 0;

		GlStateManager.translate(x, y + yOffset, z);

		this.bindTexture(texture);
		float f6 = 1.0F;
		float f7 = 0.5F;
		float f8 = 0.5F;

		GlStateManager.rotate(-90, 1, 0, 0);

		float s = WizardryUtilities.smoothScaleFactor(entity.lifetime, entity.ticksExisted, partialTicks, 10, 10);
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

		// Fire

		if(s >= 1){
			GlStateManager.disableLighting();
			TextureAtlasSprite icon = Minecraft.getMinecraft().getBlockRendererDispatcher()
					.getModelForState(Blocks.FIRE.getDefaultState()).getParticleTexture();
			int sides = 16;
			float height = 1.0f;

			for(int k = 0; k < sides; k++){

				GlStateManager.pushMatrix();
				GlStateManager.translate((float)x, (float)y + 0.05f, (float)z);
				float f1 = 1.0f;
				GlStateManager.scale(f1, f1, f1);
				float f2 = 0.5F;
				float f3 = 0.0F;
				float f4 = 0.2f;
				float f5 = (float)(entity.posY - entity.getEntityBoundingBox().minY);
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
				float f61 = 0.0F;
				int i = 0;

				GlStateManager.rotate((360f / (float)sides) * k, 0, 1, 0);
				GlStateManager.translate(0, 0, -2.3f);

				buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

				while(f4 > 0.0F){

					this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
					float f71 = icon.getMinU();
					float f81 = icon.getMinV();
					float f9 = icon.getMaxU();
					float f10 = icon.getMaxV();

					if(i / 2 % 2 == 0){
						float f11 = f9;
						f9 = f71;
						f71 = f11;
					}

					buffer.pos((f2 - f3), (0.0F - f5), f61).tex(f9, f10).endVertex();
					buffer.pos((-f2 - f3), (0.0F - f5), f61).tex(f71, f10).endVertex();
					buffer.pos((-f2 - f3), (height - f5), f61).tex(f71, f81).endVertex();
					buffer.pos((f2 - f3), (height - f5), f61).tex(f9, f81).endVertex();
					f4 -= 0.45F;
					f5 -= 0.45F;
					f2 *= 0.9F;
					f61 += 0.03F;
					++i;
				}

				tessellator.draw();
				GlStateManager.popMatrix();
			}

			for(int k = 0; k < sides; k++){

				GlStateManager.pushMatrix();
				GlStateManager.translate((float)x, (float)y + 0.05f, (float)z);
				float f1 = 1.0f;
				GlStateManager.scale(f1, f1, f1);
				float f2 = 0.5F;
				float f3 = 0.0F;
				float f4 = 0.2f;
				float f5 = (float)(entity.posY - entity.getEntityBoundingBox().minY);
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
				float f61 = 0.0F;
				int i = 0;

				GlStateManager.rotate((360f / (float)sides) * k, 0, 1, 0);
				GlStateManager.translate(0, 0, 2.3f);

				buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);

				while(f4 > 0.0F){

					this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
					float f71 = icon.getMinU();
					float f81 = icon.getMinV();
					float f9 = icon.getMaxU();
					float f10 = icon.getMaxV();

					if(i / 2 % 2 == 0){
						float f11 = f9;
						f9 = f71;
						f71 = f11;
					}

					buffer.pos((f2 - f3), (0.0F - f5), f61).tex(f9, f10).endVertex();
					buffer.pos((-f2 - f3), (0.0F - f5), f61).tex(f71, f10).endVertex();
					buffer.pos((-f2 - f3), (height - f5), f61).tex(f71, f81).endVertex();
					buffer.pos((f2 - f3), (height - f5), f61).tex(f9, f81).endVertex();
					f4 -= 0.45F;
					f5 -= 0.45F;
					f2 *= 0.9F;
					f61 += 0.03F;
					++i;
				}

				tessellator.draw();
				GlStateManager.popMatrix();
			}
		}

		GlStateManager.enableLighting();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityFireRing entity){
		return null;
	}

}
