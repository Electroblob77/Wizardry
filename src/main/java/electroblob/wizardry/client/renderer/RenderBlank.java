package electroblob.wizardry.client.renderer;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class RenderBlank extends Render<Entity> {

	public RenderBlank(RenderManager renderManager) {
		super(renderManager);
	}

	@Override
	public void doRender(Entity entity, double d0, double d1, double d2,
			float f, float f1) {

	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return null;
	}

}
