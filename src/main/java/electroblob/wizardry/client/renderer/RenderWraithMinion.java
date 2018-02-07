package electroblob.wizardry.client.renderer;

import electroblob.wizardry.entity.living.EntityBlazeMinion;
import net.minecraft.client.model.ModelBlaze;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderWraithMinion extends RenderLiving<EntityBlazeMinion> {
	private ResourceLocation texture = new ResourceLocation("textures/entity/blaze.png");

	public RenderWraithMinion(RenderManager renderManagerIn){
		super(renderManagerIn, new ModelBlaze(), 0.5F);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityBlazeMinion entity){
		return texture;
	}
}
