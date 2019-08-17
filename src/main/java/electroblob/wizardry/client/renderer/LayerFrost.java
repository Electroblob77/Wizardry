package electroblob.wizardry.client.renderer;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.block.BlockStatue;
import electroblob.wizardry.registry.WizardryPotions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

/**
 * Layer used to render the frost texture on a creature with the frostbite effect. Handles dynamic tiling of the texture.
 * 
 * @author Electroblob
 * @since Wizardry 1.2
 */
public class LayerFrost implements LayerRenderer<EntityLivingBase> {

	protected ModelBase model;
	private final RenderLivingBase<?> renderer;

	private static final ResourceLocation texture = new ResourceLocation(Wizardry.MODID, "textures/entity/frost_overlay.png");

	public static void initialiseLayers(){

		for(Render<? extends Entity> renderer : Minecraft.getMinecraft().getRenderManager().entityRenderMap.values()){
			// Because the zombie classes are now split properly, their renderers play nicely like everything else.
			if(renderer instanceof RenderLivingBase){
				// Adds a frost layer to all the living entity renderers in the game. Whether it is actually rendered
				// is decided in doRenderLayer below on a per-entity basis.
				((RenderLivingBase<?>)renderer).addLayer(new LayerFrost((RenderLivingBase<?>)renderer));
			}
		}

		for(RenderPlayer renderer : Minecraft.getMinecraft().getRenderManager().getSkinMap().values()){
			renderer.addLayer(new LayerFrost(renderer));
		}
	}

	public LayerFrost(RenderLivingBase<?> renderer){
		this.renderer = renderer;
		this.model = renderer.getMainModel();
	}

	@Override
	public void doRenderLayer(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTicks,
			float ageInTicks, float netHeadYaw, float headPitch, float scale){

		if(entity.isPotionActive(WizardryPotions.frost) || entity.getEntityData().getBoolean(BlockStatue.FROZEN_NBT_KEY)){

			GlStateManager.enableLighting();
			int i = this.getBlockBrightnessForEntity(entity, partialTicks);

			int j = i % 65536;
			int k = i / 65536;
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j / 1.0F, (float)k / 1.0F);

			// Frost texture
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
			this.renderer.bindTexture(texture);
			this.renderEntityModel(entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw,
					headPitch, scale);
			GlStateManager.disableBlend();
			
		}
	}

	private int getBlockBrightnessForEntity(Entity entity, float partialTicks){

		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(MathHelper.floor(entity.posX), 0,
				MathHelper.floor(entity.posZ));

		if(entity.world.isBlockLoaded(pos)){
			pos.setY(MathHelper.floor(entity.posY + (double)entity.getEyeHeight()));
			return entity.world.getCombinedLight(pos, 0);
		}else{
			return 0;
		}
	}

	private void renderEntityModel(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTicks,
			float ageInTicks, float netHeadYaw, float headPitch, float scale){

		GlStateManager.pushMatrix();
		// Enables tiling (Also used for guardian beam, beacon beam and ender crystal beam)
		GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
		GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);

		GlStateManager.depthMask(true); // Some entities set depth mask to false (i.e. no sorting of faces by depth)
		// In particular, LayerSpiderEyes sets it to false when the spider is invisible, for some reason.

		// Changes the scale at which the texture is applied to the model. See LayerCreeper for a similar example,
		// but with translation instead of scaling.
		// NOTE: You can do all sorts of fun stuff with this, just by applying transformations in the 2D texture space.
		GlStateManager.matrixMode(GL11.GL_TEXTURE);
		GlStateManager.loadIdentity();
		double scaleX = 1, scaleY = 1;
		// It's more logical to use the model's texture size, but some classes don't bother setting it properly
		// (e.g. ModelVillager), so to get the correct dimensions I'm getting them from the first box instead.
		if(model.boxList != null && model.boxList.get(0) != null){
			scaleX = (double)model.boxList.get(0).textureWidth / 16d;
			scaleY = (double)model.boxList.get(0).textureHeight / 16d;
		}else{ // Fallback to model fields; should never be needed
			scaleX = (double)model.textureWidth / 16d;
			scaleY = (double)model.textureHeight / 16d;
		}
		GlStateManager.scale(scaleX, scaleY, 1);
		GlStateManager.matrixMode(GL11.GL_MODELVIEW);
		
		// Hides the hat layer for bipeds
		if(this.model instanceof ModelBiped) ((ModelBiped)this.model).bipedHeadwear.isHidden = true;

		if(this.model instanceof ModelPlayer){
			((ModelPlayer)this.model).bipedBodyWear.isHidden = true;
			((ModelPlayer)this.model).bipedLeftArmwear.isHidden = true;
			((ModelPlayer)this.model).bipedRightArmwear.isHidden = true;
			((ModelPlayer)this.model).bipedLeftLegwear.isHidden = true;
			((ModelPlayer)this.model).bipedRightLegwear.isHidden = true;
		}

		this.model.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTicks);
		this.model.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

		if(this.model instanceof ModelBiped) ((ModelBiped)this.model).bipedHeadwear.isHidden = false;

		// Undoes the texture scaling
		GlStateManager.matrixMode(GL11.GL_TEXTURE);
		GlStateManager.loadIdentity();
		GlStateManager.matrixMode(GL11.GL_MODELVIEW);

		GlStateManager.popMatrix();
	}

	@Override
	public boolean shouldCombineTextures(){
		return false;
	}
}