package electroblob.wizardry.client.renderer.entity.layers;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.entity.living.ISummonedCreature;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
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
public class LayerSummonAnimation<T extends EntityLivingBase> extends LayerTiledOverlay<T> {

	private static final int ANIMATION_TICKS = 19;
	private static final int HIDE_MODEL_TICKS = 9;

	private static final ResourceLocation[] TEXTURES = new ResourceLocation[ANIMATION_TICKS];

	static {
		for(int i=0; i<ANIMATION_TICKS; i++){
			TEXTURES[i] = new ResourceLocation(Wizardry.MODID, "textures/entity/summon_overlay/summon_overlay_" + i + ".png");
		}
	}

	public LayerSummonAnimation(RenderLivingBase<?> renderer){
		super(renderer, 32, 32);
	}

	@Override
	public boolean shouldRender(T entity, float partialTicks){
		return entity instanceof ISummonedCreature && ((ISummonedCreature)entity).hasAnimation()
				&& getFrameNumber(entity) < ANIMATION_TICKS;
	}

	@Override
	public ResourceLocation getTexture(T entity, float partialTicks){
		return TEXTURES[getFrameNumber(entity)];
	}

	private int getFrameNumber(T entity){
		if(!(entity instanceof ISummonedCreature)) throw new IllegalArgumentException("Entity must be an ISummonedCreature");
		return Math.min(entity.ticksExisted, Math.max(((ISummonedCreature)entity).getLifetime() - entity.ticksExisted - 1, 0));
	}

	@Override
	public void doRenderLayer(T entity, float limbSwing, float limbSwingAmount, float partialTicks,
							  float ageInTicks, float netHeadYaw, float headPitch, float scale){

		if(!(entity instanceof ISummonedCreature)) return;

		GlStateManager.enableBlend();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);

		int colour = ((ISummonedCreature)entity).getAnimationColour((float)getFrameNumber(entity) / ANIMATION_TICKS);
		GlStateManager.color((colour >> 16 & 255) / 255f, (colour >> 8 & 255) / 255f, (colour & 255) / 255f);

		super.doRenderLayer(entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);

		GlStateManager.disableBlend();
	}

	@SubscribeEvent
	public static void onRenderLivingEvent(RenderLivingEvent.Pre<EntityLivingBase> event){
		if(shouldHideMainModel(event.getEntity())) event.getEntity().setInvisible(true);
	}

	@SubscribeEvent
	public static void onRenderLivingEvent(RenderLivingEvent.Post<EntityLivingBase> event){
		if(shouldHideMainModel(event.getEntity())) event.getEntity().setInvisible(false);
	}

	private static boolean shouldHideMainModel(EntityLivingBase entity){
		return entity instanceof ISummonedCreature && ((ISummonedCreature)entity).hasAnimation()
				&& (entity.ticksExisted < HIDE_MODEL_TICKS
				|| ((ISummonedCreature)entity).getLifetime() - entity.ticksExisted < HIDE_MODEL_TICKS);
	}

}
