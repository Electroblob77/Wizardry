package electroblob.wizardry.client.model;

import com.google.common.collect.ImmutableMap;
import electroblob.wizardry.Wizardry;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A special type of model that generates override models dynamically. This allows the same overrides to be applied to
 * multiple item models without needing an override version of each one just to point to a texture.
 * <p></p>
 * To use this system in a model file:<p></p>
 * 1. Make a base model file and define the item overrides in it. <b>This model must be placed in the folder
 * {@code models/item/override_generators}</b> (unfortunately the folder has to be predefined, see below).<br>
 * 2. Make the model files your overrides point to. These should define everything about the model except textures.<br>
 * 3. In each model that should generate overrides dynamically, define a single override that points to the location of
 * the model from step 1 (the predicate doesn't matter).<br>
 * <p></p>
 * It is advisable to make the parent of the override model (from step 2) the same as the parent of the model to
 * be overridden, so that they share any necessary properties and texture variables. For example, both
 * {@code ebwizardry:item/magic_wand.json} and {@code ebwizardry:item/wand_point.json} have {@code item/handheld} as
 * their parent. The override model will reference the textures in the original model, so if the override defines a
 * face with the texture {@code "#face"}, the texture used will be the file associated with {@code "face"} in the
 * original model. For simple models, this will just be the standard {@code "layer0"}.
 * @since Wizardry 4.3
 * @author Electroblob
 */
// Okay, it's not *that* bad if you only have one override, but imagine having to duplicate every single wand model 5
// times if we wanted 5 animations...
// The nice thing is resource packs don't lose any flexibility this way, they can still redefine the models themselves
// and specify custom models for the overrides if they want, or they can keep using this system - in fact, they GAIN
// flexibility because they can also overwrite the base wand.json model and specify global overrides too
@Mod.EventBusSubscriber(Side.CLIENT)
public class BakedModelGenerateOverrides implements IBakedModel {

	// I tried having this as a prefix that gets removed at runtime, which worked fine but it spammed the log with
	// errors because the prefixed file doesn't actually exist, so even though it's never used the game still complains
	private static final String OVERRIDE_GENERATORS = "override_generators";

	private final IBakedModel delegate;
	private final ItemOverrideList overrides;

	public BakedModelGenerateOverrides(IBakedModel delegate, ItemOverrideList overrides){
		this.delegate = delegate;
		this.overrides = overrides;
	}

	@Override
	public ItemOverrideList getOverrides(){
		return overrides; // The only thing that's not delegated
	}

	// Delegate everything else

	@Override
	public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand){
		return delegate.getQuads(state, side, rand);
	}

	@Override
	public boolean isAmbientOcclusion(){
		return delegate.isAmbientOcclusion();
	}

	@Override
	public boolean isGui3d(){
		return delegate.isGui3d();
	}

	@Override
	public boolean isBuiltInRenderer(){
		return delegate.isBuiltInRenderer();
	}

	@Override
	public TextureAtlasSprite getParticleTexture(){
		return delegate.getParticleTexture();
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms(){
		return delegate.getItemCameraTransforms();
	}

	@Override
	public boolean isAmbientOcclusion(IBlockState state){
		return delegate.isAmbientOcclusion(state);
	}

	@Override
	public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType){
		return delegate.handlePerspective(cameraTransformType);
	}

	// Baking

	@SubscribeEvent
	public static void bake(ModelBakeEvent event){

		for(ModelResourceLocation location : event.getModelRegistry().getKeys()){

			IBakedModel original = event.getModelRegistry().getObject(location);

			try{

				// Ignore the warnings, IntelliJ is right that these can't be null in the dev environment but certain
				// mods seem to be doing ASM hackery that makes it so they can be null
				if(original != null && original.getOverrides() != null && original.getOverrides().getOverrides() != null){
					original.getOverrides().getOverrides().stream().map(ItemOverride::getLocation)
							.filter(l -> l.getPath().contains(OVERRIDE_GENERATORS)).findFirst()
							.ifPresent(l -> event.getModelRegistry().putObject(location,
									substituteWandModel(event.getModelManager(), location, original, l)));
				}

			}catch(NullPointerException e){
				Wizardry.logger.info("The model {} threw an error when trying to access item overrides, it will be ignored. If you're an addon dev and you made this model, something is wrong with it! Otherwise, please ignore this message.", location);
			}
		}
	}

	// We have to use ModelBakeEvent for this because we need the original model to be baked with the vanilla system
	private static IBakedModel substituteWandModel(ModelManager modelManager, ModelResourceLocation location, IBakedModel original, ResourceLocation overrideLocation){

		try {

			IModel unbakedOriginal = ModelLoaderRegistry.getModel(location);
			IModel referenceModel = ModelLoaderRegistry.getModel(overrideLocation);
			ModelBlock vanillaOriginal = unbakedOriginal.asVanillaModel().orElse(null);
			ModelBlock vanillaRefModel = referenceModel.asVanillaModel().orElse(null);

			if(vanillaOriginal != null && vanillaRefModel != null){

				// So... many... data structures...
				List<ItemOverride> overrides = vanillaRefModel.getOverrides();
				Map<ResourceLocation, IBakedModel> replacementMap = new HashMap<>();

				for(ItemOverride override : overrides){
					IModel replacement = ModelLoaderRegistry.getModel(override.getLocation()).retexture(ImmutableMap.copyOf(vanillaOriginal.textures));
					replacementMap.put(override.getLocation(), replacement.bake(unbakedOriginal.getDefaultState(), DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter()));
				}

				ItemOverrideListMapped overrideList = new ItemOverrideListMapped(overrides, replacementMap);
				return new BakedModelGenerateOverrides(original, overrideList);
			}

		}catch(Exception exception){
			Wizardry.logger.error("Error baking item display override models: ", exception);
		}

		return modelManager.getMissingModel();
	}

}
