package electroblob.wizardry.client.renderer;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.client.model.ModelIceGiant;
import electroblob.wizardry.entity.living.EntityIceGiant;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

//@SideOnly(Side.CLIENT)
public class RenderIceGiant extends RenderLiving<EntityIceGiant> {

	private static final ResourceLocation texture = new ResourceLocation(Wizardry.MODID,
			"textures/entity/ice_giant.png");

	public RenderIceGiant(RenderManager renderManager){
		super(renderManager, new ModelIceGiant(), 0.5F);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityIceGiant entity){
		return texture;
	}

	@Override
	protected void applyRotations(EntityIceGiant entityLiving, float pitch, float yaw, float partialTicks){

		super.applyRotations(entityLiving, pitch, yaw, partialTicks);

		if((double)entityLiving.limbSwingAmount >= 0.01D){
			float f3 = 13.0F;
			float f4 = entityLiving.limbSwing - entityLiving.limbSwingAmount * (1.0F - partialTicks) + 6.0F;
			float f5 = (Math.abs(f4 % f3 - f3 * 0.5F) - f3 * 0.25F) / (f3 * 0.25F);
			GlStateManager.rotate(6.5F * f5, 0.0F, 0.0F, 1.0F);
		}
	}
}
