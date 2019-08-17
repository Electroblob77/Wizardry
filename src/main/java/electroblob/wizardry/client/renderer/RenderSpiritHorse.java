package electroblob.wizardry.client.renderer;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.entity.living.EntitySpiritHorse;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderHorse;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

//@SideOnly(Side.CLIENT)
public class RenderSpiritHorse extends RenderHorse {

	private static final ResourceLocation texture = new ResourceLocation(Wizardry.MODID,
			"textures/entity/spirit_horse.png");

//	private static final int GHOST_COPIES = 3;
//	private static final float DECONVERGENCE = 0.35f;

	public RenderSpiritHorse(RenderManager renderManager){
		super(renderManager);
	}

	@Override
	protected ResourceLocation getEntityTexture(EntityHorse entity){
		return texture;
	}

	@Override
	protected void preRenderCallback(EntityHorse horse, float partialTickTime){
		super.preRenderCallback(horse, partialTickTime);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		if(horse instanceof EntitySpiritHorse){ // Always true
			GlStateManager.color(1, 1, 1, ((EntitySpiritHorse)horse).getOpacity());
		}
	}

	@Override
	public void doRender(EntityHorse entity, double x, double y, double z, float entityYaw, float partialTicks){

		super.doRender(entity, x, y, z, entityYaw, partialTicks);

//		double dx = (entity.posX - entity.prevPosX) * DECONVERGENCE;
//		double dy = (entity.posY - entity.prevPosY) * DECONVERGENCE;
//		double dz = (entity.posZ - entity.prevPosZ) * DECONVERGENCE;
//		float dyaw = (entity.rotationYaw - entity.prevRotationYaw) * DECONVERGENCE;
//
//		float opacity = 1;
//		if(entity instanceof EntitySpiritHorse){ // Always true
//			opacity = ((EntitySpiritHorse)entity).getOpacity();
//		}
//
//		for(int i = 0; i < GHOST_COPIES; i++){
//
//			GlStateManager.color(1, 1, 1, opacity * (0.6f - (float)i/(GHOST_COPIES*2)));
//
//			super.doRender(entity, x - dx * i, y - dy * i, z - dz * i, entityYaw - dyaw * i, partialTicks);
//		}
	}

}
