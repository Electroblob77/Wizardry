package electroblob.wizardry.client.model;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.client.model.pipeline.VertexLighterFlat;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom baked model that stores a list of texture names and sets the lighting to full brightness for any quads
 * with one of those textures. Most of this code was copied and adapted from refined storage, which is licensed under
 * the MIT license. https://github.com/raoulvdberge/refinedstorage<br>
 * <br>
 * <i>N.B. This doesn't cover item models, and all of the code/model solutions to this that I have tried haven't worked.
 * However, I noticed that the shade tag seems to work perfectly for items, so instead I've simply duplicated the block
 * models into the item models folder and add {@code "shade":false} where appropriate. (If it works, it works, right?)</i>
 *
 * @author Electroblob
 * @author raoulvdberge
 */
public class BakedModelGlowingOverlay implements IBakedModel {

	// Something something something cache, says Forge
	// This breaks randomised block models (because it's cached, duh...) but simply including the rand parameter will
	// completely defeat the point of the cache so I need some way of storing the randomised model variants... hmmm...
	// See WeightedBakedModel for more on randomisation (it's pretty similar to my 1.7 implementation from years ago)

//	private class CacheKey {
//
//		private IBakedModel base;
//		private String suffix;
//		private IBlockState state;
//		private EnumFacing side;
//
//		public CacheKey(IBakedModel base, String suffix, IBlockState state, EnumFacing side){
//			this.base = base;
//			this.suffix = suffix;
//			this.state = state;
//			this.side = side;
//		}
//
//		@Override
//		public boolean equals(Object o){
//
//			if(this == o) return true;
//			if(o == null || getClass() != o.getClass()) return false;
//
//			CacheKey cacheKey = (CacheKey)o;
//
//			if(cacheKey.side != side) return false;
//			if(!state.equals(cacheKey.state)) return false;
//
//			return true;
//		}
//
//		@Override
//		public int hashCode() {
//			return state.hashCode() + (31 * (side != null ? side.hashCode() : 0));
//		}
//	}
//
//	private static final LoadingCache<CacheKey, List<BakedQuad>> CACHE = CacheBuilder.newBuilder().build(new CacheLoader<CacheKey, List<BakedQuad>>() {
//		@Override
//		public List<BakedQuad> load(CacheKey key) {
//			return transformQuads(key.base.getQuads(key.state, key.side, 0), key.suffix);
//		}
//	});

	private final IBakedModel delegate;
	private String suffix;

	public BakedModelGlowingOverlay(IBakedModel delegate, String suffix){
		this.delegate = delegate;
		this.suffix = suffix;
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand){
		if(state == null) return delegate.getQuads(state, side, rand);
		return transformQuads(delegate.getQuads(state, side, rand), suffix);
		//return CACHE.getUnchecked(new CacheKey(delegate, suffix, state instanceof IExtendedBlockState ? ((IExtendedBlockState) state).getClean() : state, side));
	}

	// I would write these myself but I'd end up with almost the exact same thing anyway
	// They replace the quads from the original (delegate) model with full-brightness quads if their texture has the given suffix

	private static List<BakedQuad> transformQuads(List<BakedQuad> oldQuads, String suffix){

		List<BakedQuad> quads = new ArrayList<>(oldQuads);

		for(int i = 0; i < quads.size(); ++i){
			BakedQuad quad = quads.get(i);

			if(quad.getSprite().getIconName().endsWith(suffix)){
				quads.set(i, transformQuad(quad, 0.007F)); // What's the significance of 0.007?
			}
		}

		return quads;
	}

	private static BakedQuad transformQuad(BakedQuad quad, float light){

		if(isLightMapDisabled()){
			return quad;
		}

		VertexFormat newFormat = getFormatWithLightMap(quad.getFormat());

		UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(newFormat);

		VertexLighterFlat trans = new VertexLighterFlat(Minecraft.getMinecraft().getBlockColors()) {
			@Override
			protected void updateLightmap(float[] normal, float[] lightmap, float x, float y, float z){
				lightmap[0] = light;
				lightmap[1] = light;
			}

			@Override
			public void setQuadTint(int tint){
				// NO OP
			}
		};

		trans.setParent(builder);

		quad.pipe(trans);

		builder.setQuadTint(quad.getTintIndex());
		builder.setQuadOrientation(quad.getFace());
		builder.setTexture(quad.getSprite());
		builder.setApplyDiffuseLighting(false);

		return builder.build();
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
	public ItemOverrideList getOverrides(){
		return delegate.getOverrides();//BakedModelItemOverride.instance;
	}

	@Override
	public boolean isAmbientOcclusion(IBlockState state){
		return delegate.isAmbientOcclusion(state);
	}

	@Override
	public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType){
		return delegate.handlePerspective(cameraTransformType);
	}

	// Utilities

	private static boolean isLightMapDisabled(){
		return FMLClientHandler.instance().hasOptifine() || !ForgeModContainer.forgeLightPipelineEnabled;
	}

	private static final VertexFormat ITEM_FORMAT_WITH_LIGHTMAP = new VertexFormat(DefaultVertexFormats.ITEM).addElement(DefaultVertexFormats.TEX_2S);

	private static VertexFormat getFormatWithLightMap(VertexFormat format){

		if(isLightMapDisabled()){
			return format;
		}

		if(format == DefaultVertexFormats.BLOCK){
			return DefaultVertexFormats.BLOCK;
		}else if(format == DefaultVertexFormats.ITEM){
			return ITEM_FORMAT_WITH_LIGHTMAP;
		}else if(!format.hasUvOffset(1)){
			VertexFormat result = new VertexFormat(format);
			result.addElement(DefaultVertexFormats.TEX_2S);
			return result;
		}

		return format;
	}

	// Bit of a weird way of doing things if you ask me, but as far as I can tell it's how you're supposed to do it
//	public static final class BakedModelItemOverride extends ItemOverrideList {
//
//		// This class doesn't contain any data of its own so it can be a singleton
//		public static final BakedModelItemOverride instance = new BakedModelItemOverride();
//
//		private BakedModelItemOverride(){
//			super(ImmutableList.of()); // We're not using the list functionality
//		}
//
//		@Override
//		public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity){
//			return new BakedModelGlowingOverlay(originalModel, "overlay"); // Bish bash bosh
//		}
//	}
}
