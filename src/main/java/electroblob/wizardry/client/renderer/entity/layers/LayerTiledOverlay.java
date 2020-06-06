package electroblob.wizardry.client.renderer.entity.layers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import java.util.function.Function;

/**
 * Base class for layers that overlay a tiled texture onto the entity model. Handles dynamic tiling of the texture,
 * scaling to preserve in-world pixel size (1/16 of a block), and the necessary GL state changes.
 * 
 * @author Electroblob
 * @since Wizardry 4.3
 */
public abstract class LayerTiledOverlay<T extends EntityLivingBase> implements LayerRenderer<T> {

	private final RenderLivingBase<?> renderer;

	private final int textureWidth;
	private final int textureHeight;

	/** Creates a new {@code LayerTiledOverlay} with the given renderer parameters. */
	public LayerTiledOverlay(RenderLivingBase<?> renderer, int textureWidth, int textureHeight){
		this.textureWidth = textureWidth;
		this.textureHeight = textureHeight;
		this.renderer = renderer;
	}

	/** Creates a new {@code LayerTiledOverlay} with the given renderer. Texture width and height default to 16. */
	public LayerTiledOverlay(RenderLivingBase<?> renderer){
		this(renderer, 16, 16);
	}

	/**
	 * Returns true if this layer should be rendered for the given entity, false if not.
	 * @param entity The entity being rendered
	 * @param partialTicks The current partial tick time
	 * @return True if this layer should be rendered, false if not.
	 */
	public abstract boolean shouldRender(T entity, float partialTicks);

	/**
	 * Returns the texture to use for the overlay.
	 * @param entity The entity being rendered
	 * @param partialTicks The current partial tick time
	 * @return A {@link ResourceLocation} specifying the texture to use.
	 */
	public abstract ResourceLocation getTexture(T entity, float partialTicks);

	/**
	 * Returns the model to use for the overlay. Defaults to the renderer's main model.
	 * @param entity The entity being rendered
	 * @param partialTicks The current partial tick time
	 * @return The model to use. Swapping different models in dynamically is fine, but don't create new ones each time.
	 */
	public ModelBase getModel(T entity, float partialTicks){
		return renderer.getMainModel();
	}

	/**
	 * Returns whether to render the second layer of mob/player skins (which is not an <i>actual</i> render layer, but
	 * part of the model) as part of this layer. Defaults to false.
	 * @param entity The entity being rendered
	 * @param partialTicks The current partial tick time
	 * @return True to render any non-hidden second layer boxes, false to always hide them from this layer.
	 */
	public boolean renderSecondLayer(T entity, float partialTicks){
		return false; // Normally it looks kind of weird because second layers typically have holes in them
	}

	@Override
	public void doRenderLayer(T entity, float limbSwing, float limbSwingAmount, float partialTicks,
			float ageInTicks, float netHeadYaw, float headPitch, float scale){

		if(shouldRender(entity, partialTicks)){
			this.renderer.bindTexture(getTexture(entity, partialTicks));
			this.renderEntityModel(entity, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
		}
	}

	@Override
	public boolean shouldCombineTextures(){
		return false;
	}

	protected static int getBlockBrightnessForEntity(Entity entity){

		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(MathHelper.floor(entity.posX), 0,
				MathHelper.floor(entity.posZ));

		if(entity.world.isBlockLoaded(pos)){
			pos.setY(MathHelper.floor(entity.posY + (double)entity.getEyeHeight()));
			return entity.world.getCombinedLight(pos, 0);
		}else{
			return 0;
		}
	}

	private void renderEntityModel(T entity, float limbSwing, float limbSwingAmount, float partialTicks,
			float ageInTicks, float netHeadYaw, float headPitch, float scale){

		GlStateManager.pushMatrix();
		// Enables texture tiling (Also used for guardian beam, beacon beam and ender crystal beam, amongst others)
		GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
		GlStateManager.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);

		GlStateManager.depthMask(true); // Some entities set depth mask to false (i.e. no sorting of faces by depth)
		// In particular, LayerSpiderEyes sets it to false when the spider is invisible, for some reason.

		// Changes the scale at which the texture is applied to the model. See LayerCreeper for a similar example,
		// but with translation instead of scaling.
		// You can do all sorts of fun stuff with this, just by applying transformations in the 2D texture space.
		GlStateManager.matrixMode(GL11.GL_TEXTURE); // Switch to the 2D texture space
		GlStateManager.loadIdentity();

		ModelBase model = getModel(entity, partialTicks);

		// Calculate the scaling required in each direction
		double scaleX = 1, scaleY = 1;
		// It's more logical to use the model's texture size, but some classes don't bother setting it properly
		// (e.g. ModelVillager), so to get the correct dimensions I'm getting them from the first box instead.
		if(model.boxList != null && model.boxList.get(0) != null){
			scaleX = (double)model.boxList.get(0).textureWidth / textureWidth;
			scaleY = (double)model.boxList.get(0).textureHeight / textureHeight;
		}else{ // Fallback to model fields; should never be needed
			scaleX = (double)model.textureWidth / textureWidth;
			scaleY = (double)model.textureHeight / textureHeight;
		}

		GlStateManager.scale(scaleX, scaleY, 1);

		GlStateManager.matrixMode(GL11.GL_MODELVIEW); // Switch back to the 3D model space

		boolean headWearHidden = false;
		boolean bodyWearHidden = false;
		boolean leftArmWearHidden = false;
		boolean rightArmWearHidden = false;
		boolean leftLegWearHidden = false;
		boolean rightLegWearHidden = false;

		if(!renderSecondLayer(entity, partialTicks)){
			// Hide the hat layer for bipeds, and all the other body part overlays for players
			if(model instanceof ModelBiped){
				headWearHidden = ((ModelBiped)model).bipedHeadwear.isHidden;
				((ModelBiped)model).bipedHeadwear.isHidden = true;
			}

			if(model instanceof ModelPlayer){
				bodyWearHidden = ((ModelPlayer)model).bipedBodyWear.isHidden;
				leftArmWearHidden = ((ModelPlayer)model).bipedLeftArmwear.isHidden;
				rightArmWearHidden = ((ModelPlayer)model).bipedRightArmwear.isHidden;
				leftLegWearHidden = ((ModelPlayer)model).bipedLeftLegwear.isHidden;
				rightLegWearHidden = ((ModelPlayer)model).bipedRightLegwear.isHidden;
				((ModelPlayer)model).bipedBodyWear.isHidden = true;
				((ModelPlayer)model).bipedLeftArmwear.isHidden = true;
				((ModelPlayer)model).bipedRightArmwear.isHidden = true;
				((ModelPlayer)model).bipedLeftLegwear.isHidden = true;
				((ModelPlayer)model).bipedRightLegwear.isHidden = true;
			}
		}

		// Actually render the model
		model.setLivingAnimations(entity, limbSwing, limbSwingAmount, partialTicks);
		model.render(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

		if(!renderSecondLayer(entity, partialTicks)){
			// Reset the hidden status of everything to how it was before
			if(model instanceof ModelBiped){
				((ModelBiped)model).bipedHeadwear.isHidden = headWearHidden;
			}

			if(model instanceof ModelPlayer){
				((ModelPlayer)model).bipedBodyWear.isHidden = bodyWearHidden;
				((ModelPlayer)model).bipedLeftArmwear.isHidden = leftArmWearHidden;
				((ModelPlayer)model).bipedRightArmwear.isHidden = rightArmWearHidden;
				((ModelPlayer)model).bipedLeftLegwear.isHidden = leftLegWearHidden;
				((ModelPlayer)model).bipedRightLegwear.isHidden = rightLegWearHidden;
			}
		}

		// Undo the texture scaling
		GlStateManager.matrixMode(GL11.GL_TEXTURE);
		GlStateManager.loadIdentity();
		GlStateManager.matrixMode(GL11.GL_MODELVIEW);

		GlStateManager.popMatrix();
	}

	/**
	 * Helper method to initialise a layer renderer for all applicable renderers. This method will work for any layer
	 * renderer, not just instances of {@code LayerTiledOverlay} (it is stored here purely for convenience).
	 * @param entityType The class of entity to initialise this layer renderer for (including subclasses)
	 * @param layerFactory A function that creates a layer for a given renderer, usually a constructor reference
	 * @param <T> The type of entity this layer renderer is applicable for
	 */
	@SuppressWarnings("unchecked")
	public static <T extends EntityLivingBase> void initialiseLayers(Class<T> entityType, Function<RenderLivingBase<T>, LayerRenderer<? extends T>> layerFactory){

		for(Class<? extends Entity> c : Minecraft.getMinecraft().getRenderManager().entityRenderMap.keySet()){
			if(entityType.isAssignableFrom(c)){
				Render<T> renderer = Minecraft.getMinecraft().getRenderManager().getEntityClassRenderObject(c);
				if(renderer instanceof RenderLivingBase<?>){ // Should always be true
					// For some reason IntelliJ is quite happy with this cast, even without suppress warnings
					// Instinct suggests Java shouldn't be happy casting to RenderLivingBase<T> but somehow it works
					((RenderLivingBase<T>)renderer).addLayer(layerFactory.apply((RenderLivingBase<T>)renderer));
				}
			}
		}

		// Players have a separate renderer map
		if(entityType.isAssignableFrom(EntityPlayer.class)){
			for(RenderPlayer renderer : Minecraft.getMinecraft().getRenderManager().getSkinMap().values()){
				renderer.addLayer(layerFactory.apply((RenderLivingBase<T>)renderer)); // This does need suppress warnings
			}
		}
	}

	/**
	 * Helper method to initialise a layer renderer for all living entity renderers. This is a shorthand version of
	 * {@link LayerTiledOverlay#initialiseLayers(Class, Function)}.
	 * @param layerFactory A function that creates a layer for a given renderer, usually a constructor reference
	 */
	public static void initialiseLayers(Function<RenderLivingBase<EntityLivingBase>, LayerRenderer<? extends EntityLivingBase>> layerFactory){
		initialiseLayers(EntityLivingBase.class, layerFactory);
	}

}