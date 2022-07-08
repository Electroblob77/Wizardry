package electroblob.wizardry.client.renderer.entity;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.entity.projectile.EntityConjuredArrow;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderArrow;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

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

	@Override
	public void doRender(EntityConjuredArrow entity, double x, double y, double z, float entityYaw, float partialTicks) {
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.color(1, 1, 1, 0.4f);
		super.doRender(entity, x, y, z, entityYaw, partialTicks);
		GlStateManager.popMatrix();
	}
}
