package electroblob.wizardry.client.animation;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.util.InventoryUtils;
import electroblob.wizardry.util.JavaUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handles the setup and rendering events for custom player animations, as well as registering of animations. Addons
 * can also register their own animations here, see {@link PlayerAnimator#registerAnimation(Animation)}.
 * @author Electroblob
 * @since Wizardry 4.3
 * @see electroblob.wizardry.item.SpellActions SpellActions
 */
@Mod.EventBusSubscriber(Side.CLIENT)
public class PlayerAnimator {

	// TODO: Attempt to extend this to all bipeds

	private static final List<Animation> animations = new ArrayList<>();

	/** Stores all registered layer renderers for lazy-loading of armour models later. */
	private static final Map<RenderPlayer, List<LayerRenderer<? extends EntityLivingBase>>> playerLayers = new HashMap<>();
	/** Stores the main model for each renderer plus all models for registered layer renderers. */
	private static final Map<RenderPlayer, List<ModelBiped>> playerLayerModels = new HashMap<>();
	/** Reflected into {@link RenderLivingBase}{@code #layerRenderers}. */
	private static final Field layerRenderers;

	/** True if wizardry's player animations have been overridden with another mod (currently only MoBends) */
	private static boolean overridden;

	static {
		layerRenderers = ObfuscationReflectionHelper.findField(RenderLivingBase.class, "field_177097_h");
	}

	/**
	 * Registers a new player animation. This method should be called from the {@code init()} phase via a client proxy.
	 * @param animation The animation to register
	 */
	// N.B. This can (and will) be called before PlayerAnimator#init() is called
	public static void registerAnimation(Animation animation){
		if(animations.contains(animation)){
			Wizardry.logger.warn("Animation {} is already registered!", animation.getName());
		}else{
			animations.add(animation);
		}
	}

	/** Returns true if spellcasting animations are enabled, false otherwise. */
	public static boolean areAnimationsEnabled(){
		return Wizardry.settings.spellcastingAnimations && !overridden;
	}

	// TODO: Figure out if calling this from postInit ensures we catch all the layers, and lazy-load it if not
	@SuppressWarnings("unchecked")
	public static void init(){

		if(Loader.isModLoaded("mobends")) overridden = true;

		if(!areAnimationsEnabled()) return;

		for(RenderPlayer renderer : Minecraft.getMinecraft().getRenderManager().getSkinMap().values()){

			List<ModelBiped> models = new ArrayList<>();
			ModelRendererExtended.wrap(renderer.getMainModel());
			models.add(renderer.getMainModel());

			try {

				List<LayerRenderer<? extends EntityLivingBase>> layers = (List<LayerRenderer<? extends EntityLivingBase>>)layerRenderers.get(renderer);

				// Save armour layers for lazy-loading later (ignore other layers so iteration is faster)
				playerLayers.put(renderer, layers.stream().filter(l -> l instanceof LayerBipedArmor).collect(Collectors.toList()));

				for(LayerRenderer<?> layer : layers){

					for(Field field : JavaUtils.getAllFields(layer.getClass())){

						field.setAccessible(true);

						// If your layer model doesn't extend ModelBiped, you DESERVE to be incompatible!
						// (Just kidding... there's nothing I can do about it anyway)
						if(field.get(layer) instanceof ModelBiped){
							ModelBiped model = (ModelBiped)field.get(layer);
							ModelRendererExtended.wrap(model);
							models.add(model);
						}
					}

				}

			}catch(IllegalAccessException e){
				Wizardry.logger.error("Error during reflective access of render layers: ", e);
			}

			playerLayerModels.put(renderer, models);

		}
	}

	private static void updateModels(EntityPlayer player, Render<?> renderer, float partialTicks, boolean firstPerson){

		// TODO: This will stop the crashing but it might break animations for any mod that replaces the player renderer
		//       There may be a way to make it compatible but we'll have to look into that at a later date
		List<ModelBiped> models = playerLayerModels.get(renderer);

		// If no models were registered for the given renderer, don't do anything
		if(models != null){

			List<LayerRenderer<? extends EntityLivingBase>> layers = playerLayers.get(renderer);

			// If no layers were registered for the given renderer there's no point trying to lazy-load anything!
			if(layers != null){

				// Biped armour is special because of Forge's armour item render hook
				// This needs to be lazy-loaded because we need access to an actual item
				for(LayerRenderer<? extends EntityLivingBase> layer : layers){
					if(layer instanceof LayerBipedArmor){
						for(EntityEquipmentSlot slot : InventoryUtils.ARMOUR_SLOTS){

							ItemStack armour = player.getItemStackFromSlot(slot);
							// This method could behave dynamically depending on stuff like NBT so there's not really any way to
							// optimise it further, we *have* to retrieve the model every time (shouldn't be too bad though)
							ModelBiped model = ForgeHooksClient.getArmorModel(player, armour, slot, ((LayerBipedArmor)layer).getModelFromSlot(slot));

							if(!models.contains(model)){ // Ignore already-wrapped models
								models.add(model);
								ModelRendererExtended.wrap(model);
							}
						}
					}
				}
			}

			for(Animation animation : animations){

				if(animation.shouldDisplay(player, firstPerson)){

					boolean autoRotateSecondLayer = animation.autoRotateSecondLayer(player, firstPerson);

					for(ModelBiped model : models){

						if(!ModelRendererExtended.isWrapped(model)) continue; // Should never happen but somehow it does

						animation.setRotationAngles(player, model, partialTicks, firstPerson);

						if(autoRotateSecondLayer && model instanceof ModelPlayer){
							alignSecondLayer((ModelPlayer)model);
						}
					}
				}
			}
		}

	}

	/** Rotates the second layer boxes of the given (wrapped) player model to match the first layer. */
	public static void alignSecondLayer(ModelPlayer model){
		((ModelRendererExtended)model.bipedBodyWear)	.setRotationTo((ModelRendererExtended)model.bipedBody);
		((ModelRendererExtended)model.bipedRightArmwear).setRotationTo((ModelRendererExtended)model.bipedRightArm);
		((ModelRendererExtended)model.bipedLeftArmwear)	.setRotationTo((ModelRendererExtended)model.bipedLeftArm);
		((ModelRendererExtended)model.bipedRightLegwear).setRotationTo((ModelRendererExtended)model.bipedBody);
		((ModelRendererExtended)model.bipedLeftLegwear)	.setRotationTo((ModelRendererExtended)model.bipedBody);
	}

	@SubscribeEvent
	public static void onRenderHandEvent(RenderSpecificHandEvent event){

		if(!areAnimationsEnabled()) return;

		AbstractClientPlayer player = Minecraft.getMinecraft().player;

		EnumAction action = event.getItemStack().getItemUseAction();

		if(player.isHandActive() && player.getActiveHand() == event.getHand() && SpellActions.getSpellActions().contains(action)){
			// Minecraft's item renderer helpfully doesn't have a default case for the usage action so it doesn't do any
			// transformations at all (which is VERY ANNOYING!) - the following lines are from ItemRenderer#transformSideFirstPerson
			int i = (player.getPrimaryHand() == EnumHandSide.RIGHT) == (event.getHand() == EnumHand.MAIN_HAND) ? 1 : -1;
			GlStateManager.translate((float)i * 0.56F, -0.52F + event.getEquipProgress() * -0.6F, -0.72F);
		}

		updateModels(player, Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(player), event.getPartialTicks(), true);
	}

	@SubscribeEvent
	public static void onRenderPlayerPreEvent(RenderPlayerEvent.Pre event){

		if(!areAnimationsEnabled()) return;

//		boolean firstPerson = event.getEntityPlayer() == Minecraft.getMinecraft().player
//				&& Minecraft.getMinecraft().gameSettings.thirdPersonView == 0;

		updateModels(event.getEntityPlayer(), event.getRenderer(), event.getPartialRenderTick(), false);
	}

	@SubscribeEvent
	public static void onRenderPlayerPostEvent(RenderPlayerEvent.Post event){

		if(!areAnimationsEnabled()) return;

		for(ModelBiped model : playerLayerModels.get(event.getRenderer())){
			for(ModelRenderer box : model.boxList){ // For ModelPlayer, this will include the second layer
				// Some models have extra boxes, they (probably) don't need wrapping but we need this check!
				if(box instanceof ModelRendererExtended){
					((ModelRendererExtended)box).resetRotation();
				}
			}
		}

	}

}
