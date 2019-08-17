package electroblob.wizardry.client.renderer;

import electroblob.wizardry.entity.living.EntityStrayMinion;
import net.minecraft.client.model.ModelSkeleton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;

/** Had to copy this entire class just because of one unnecessarily specific type parameter. The type parameter has been
 * changed to {@code EntityStrayMinion} and parameter types updated accordingly. Everything else is identical. */
public class LayerStrayMinionClothing implements LayerRenderer<EntityStrayMinion> {

	private static final ResourceLocation STRAY_CLOTHES_TEXTURES = new ResourceLocation("textures/entity/skeleton/stray_overlay.png");
	private final RenderLivingBase<?> renderer;
	private final ModelSkeleton layerModel = new ModelSkeleton(0.25F, true);

	public LayerStrayMinionClothing(RenderLivingBase<?> renderer){
		this.renderer = renderer;
	}

	public void doRenderLayer(EntityStrayMinion entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale){
		this.layerModel.setModelAttributes(this.renderer.getMainModel());
		this.layerModel.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTicks);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.renderer.bindTexture(STRAY_CLOTHES_TEXTURES);
		this.layerModel.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
	}

	public boolean shouldCombineTextures(){
		return true;
	}
}