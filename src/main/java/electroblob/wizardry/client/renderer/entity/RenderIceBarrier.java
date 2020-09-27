package electroblob.wizardry.client.renderer.entity;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.client.model.ModelIceBarrier;
import electroblob.wizardry.entity.construct.EntityIceBarrier;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderIceBarrier extends Render<EntityIceBarrier> {

	private static final ResourceLocation TEXTURE = new ResourceLocation(Wizardry.MODID, "textures/entity/ice_barrier.png");

	private ModelIceBarrier model = new ModelIceBarrier();

	public RenderIceBarrier(RenderManager manager){
		super(manager);
	}

	@Override
	public void doRender(EntityIceBarrier entity, double x, double y, double z, float yaw, float partialTicks){

		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y + entity.height/2, z);
		GlStateManager.rotate(180, 0F, 0F, 1F);
		GlStateManager.rotate(yaw, 0, 1, 0);
		//GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, 0, 0, 1);
		GlStateManager.translate(0, -entity.height/2 - 0.3, 0);

		// Pass in -1 for the lifetime as the boulder crumbles when the time expires
//		float s = DrawingUtils.smoothScaleFactor(-1, entity.ticksExisted, partialTicks, 10, 10);
		float s = entity.getSizeMultiplier();
		GlStateManager.scale(s, s, s);

//		GlStateManager.translate(0, 0.875, 0); // No idea why it starts 7/8 of a block too low, but it does

		this.bindTexture(TEXTURE);

		model.render(entity, 0, 0, 0, 0, 0, 0.0625f);

		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityIceBarrier entity){
		return TEXTURE;
	}

}
