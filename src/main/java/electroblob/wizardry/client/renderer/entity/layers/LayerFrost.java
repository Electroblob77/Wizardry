package electroblob.wizardry.client.renderer.entity.layers;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.block.BlockStatue;
import electroblob.wizardry.registry.WizardryPotions;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

/**
 * Layer used to render the frost texture on creatures with the frostbite effect.
 *
 * @author Electroblob
 * @since Wizardry 1.2
 */
public class LayerFrost extends LayerTiledOverlay<EntityLivingBase> {

	private static final ResourceLocation TEXTURE = new ResourceLocation(Wizardry.MODID, "textures/entity/frost_overlay.png");

	public LayerFrost(RenderLivingBase<?> renderer){
		super(renderer);
	}

	@Override
	public boolean shouldRender(EntityLivingBase entity, float partialTicks){
		return !entity.isInvisible() && entity.isPotionActive(WizardryPotions.frost) || entity.getEntityData().getBoolean(BlockStatue.FROZEN_NBT_KEY);
	}

	@Override
	public ResourceLocation getTexture(EntityLivingBase entity, float partialTicks){
		return TEXTURE;
	}

	@Override
	public void doRenderLayer(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTicks,
			float ageInTicks, float netHeadYaw, float headPitch, float scale){

		GlStateManager.enableBlend();
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);

		super.doRenderLayer(entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);

		GlStateManager.disableBlend();
	}

}