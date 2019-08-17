package electroblob.wizardry.client.renderer;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.client.model.ModelHammer;
import electroblob.wizardry.entity.construct.EntityHammer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

public class RenderHammer extends Render<EntityHammer> {

	private static final ResourceLocation texture = new ResourceLocation(Wizardry.MODID,
			"textures/entity/lightning_hammer.png");
	private ModelHammer model = new ModelHammer();

	public RenderHammer(RenderManager renderManager){
		super(renderManager);
	}

	@Override
	public void doRender(EntityHammer entity, double x, double y, double z, float yaw, float partialTicks){

		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y + 1.5, z);
		GlStateManager.rotate(180, 0F, 0F, 1F);
		GlStateManager.rotate(yaw, 0, 1, 0);
		GlStateManager.rotate(entity.prevRotationPitch + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks, 0, 0, 1);

		this.bindTexture(texture);

		model.render(entity, 0, 0, 0, 0, 0, 0.0625f);

		GlStateManager.popMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityHammer entity){
		return texture;
	}

}
