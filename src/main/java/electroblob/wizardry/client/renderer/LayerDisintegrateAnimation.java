package electroblob.wizardry.client.renderer;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.spell.Disintegration;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Layer used to render the appear/disappear animation for summoned creatures.
 *
 * @author Electroblob
 * @since Wizardry 4.3
 */
@EventBusSubscriber
public class LayerDisintegrateAnimation<T extends EntityLivingBase> extends LayerTiledOverlay<T> {

	private static final int ANIMATION_TICKS = 19; // One less than the max death time

	private static final ResourceLocation[] TEXTURES = new ResourceLocation[ANIMATION_TICKS];

	static {
		for(int i=0; i<ANIMATION_TICKS; i++){
			TEXTURES[i] = new ResourceLocation(Wizardry.MODID, "textures/entity/disintegrate_overlay/disintegrate_overlay_" + i + ".png");
		}
	}

	public LayerDisintegrateAnimation(RenderLivingBase<?> renderer){
		super(renderer, 32, 32);
	}

	@Override
	public boolean shouldRender(T entity, float partialTicks){
		return entity.getEntityData().hasKey(Disintegration.NBT_KEY);
	}

	@Override
	public ResourceLocation getTexture(T entity, float partialTicks){
		return TEXTURES[MathHelper.clamp(entity.ticksExisted - entity.getEntityData().getInteger(Disintegration.NBT_KEY), 0, ANIMATION_TICKS - 1)];
	}

	@Override
	public void doRenderLayer(T entity, float limbSwing, float limbSwingAmount, float partialTicks,
							  float ageInTicks, float netHeadYaw, float headPitch, float scale){
		GlStateManager.disableLighting();
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
		super.doRenderLayer(entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
		GlStateManager.enableLighting();
	}

	@SubscribeEvent
	public static void onRenderLivingEvent(RenderLivingEvent.Pre<EntityLivingBase> event){
		if(event.getEntity().getEntityData().hasKey(Disintegration.NBT_KEY)){
			event.getEntity().deathTime = 0;
			event.getEntity().setInvisible(true);
		}
	}

	@SubscribeEvent
	public static void onRenderLivingEvent(RenderLivingEvent.Post<EntityLivingBase> event){
		if(event.getEntity().getEntityData().hasKey(Disintegration.NBT_KEY)) event.getEntity().setInvisible(false);
	}

}
