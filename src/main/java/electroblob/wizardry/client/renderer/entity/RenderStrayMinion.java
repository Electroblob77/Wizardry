package electroblob.wizardry.client.renderer.entity;

import electroblob.wizardry.client.renderer.entity.layers.LayerStrayMinionClothing;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderSkeleton;
import net.minecraft.entity.monster.AbstractSkeleton;
import net.minecraft.util.ResourceLocation;

/** This class also had to be copied for the same reason as {@link LayerStrayMinionClothing}. */
public class RenderStrayMinion extends RenderSkeleton {

	private static final ResourceLocation STRAY_SKELETON_TEXTURES = new ResourceLocation("textures/entity/skeleton/stray.png");

	public RenderStrayMinion(RenderManager manager){
		super(manager);
		this.addLayer(new LayerStrayMinionClothing(this)); // This is the only change
	}

	/**
	 * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
	 */
	protected ResourceLocation getEntityTexture(AbstractSkeleton entity){
		return STRAY_SKELETON_TEXTURES;
	}
}