package electroblob.wizardry.client.renderer.entity;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.entity.projectile.EntityConjuredArrow;
import net.minecraft.client.renderer.entity.RenderArrow;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderConjuredArrow extends RenderArrow<EntityConjuredArrow> {

	public static final ResourceLocation CONJURED_ARROW_TEXTURE = new ResourceLocation(Wizardry.MODID, "textures/entity/conjured_arrow.png");

	public RenderConjuredArrow(RenderManager manager) {
		super(manager);

	}

	@Override
	protected ResourceLocation getEntityTexture(EntityConjuredArrow entity) {
		return CONJURED_ARROW_TEXTURE;
	}
}
