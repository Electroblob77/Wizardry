package electroblob.wizardry.client.renderer.entity;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.client.model.ModelPhoenix;
import electroblob.wizardry.entity.living.EntityPhoenix;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

//@SideOnly(Side.CLIENT)
public class RenderPhoenix extends RenderLiving<EntityPhoenix> {

	private static final ResourceLocation texture = new ResourceLocation(Wizardry.MODID, "textures/entity/phoenix.png");

	public RenderPhoenix(RenderManager renderManager){
		super(renderManager, new ModelPhoenix(), 1.0f);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityPhoenix entity){
		return texture;
	}

	@Override
	protected void applyRotations(EntityPhoenix par1EntityPhoenix, float par2, float par3, float par4){
		GlStateManager.translate(0.0F, -0.1F, 0.0F);
		super.applyRotations(par1EntityPhoenix, par2, par3, par4);
	}

	@Override
	public void doRender(EntityPhoenix phoenix, double par2, double par4, double par6, float par8, float par9){

		GlStateManager.pushMatrix();

		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);

		super.doRender(phoenix, par2, par4, par6, par8, par9);

		GlStateManager.enableLighting();
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
	}
}
