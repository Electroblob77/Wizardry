package electroblob.wizardry.client.renderer.entity;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.client.DrawingUtils;
import electroblob.wizardry.client.model.ModelBoulder;
import electroblob.wizardry.entity.construct.EntityBoulder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderBoulder extends Render<EntityBoulder> {

	private static final ResourceLocation TEXTURE = new ResourceLocation(Wizardry.MODID, "textures/entity/boulder.png");

	private ModelBoulder model = new ModelBoulder();

	public RenderBoulder(RenderManager manager){
		super(manager);
	}

	@Override
	public void doRender(EntityBoulder entity, double x, double y, double z, float yaw, float partialTicks){

		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y + entity.height/2, z);
		GlStateManager.rotate(180, 0F, 0F, 1F);
		GlStateManager.rotate(yaw - 90, 0, 1, 0);
		GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, 0, 0, 1);
		GlStateManager.translate(0, -entity.height/2, 0);

		// Pass in -1 for the lifetime as the boulder crumbles when the time expires
		float s = DrawingUtils.smoothScaleFactor(-1, entity.ticksExisted, partialTicks, 10, 10);
		s *= entity.getSizeMultiplier();
		GlStateManager.scale(s, s, s);

		GlStateManager.translate(0, 0.875, 0); // No idea why it starts 7/8 of a block too low, but it does

		this.bindTexture(TEXTURE);

		model.render(entity, 0, 0, 0, 0, 0, 0.0625f);

		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityBoulder entity){
		return TEXTURE;
	}

}
