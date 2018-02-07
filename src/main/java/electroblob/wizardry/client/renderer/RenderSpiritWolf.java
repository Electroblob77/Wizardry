package electroblob.wizardry.client.renderer;

import org.lwjgl.opengl.GL11;

import electroblob.wizardry.Wizardry;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderWolf;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderSpiritWolf extends RenderWolf {

	private static final ResourceLocation texture = new ResourceLocation(Wizardry.MODID,
			"textures/entity/spirit_wolf.png");

	public RenderSpiritWolf(RenderManager renderManager){
		super(renderManager);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityWolf entity){
		return texture;
	}

	@Override
	protected void preRenderCallback(EntityWolf entity, float partialTickTime){
		super.preRenderCallback(entity, partialTickTime);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	}
}
