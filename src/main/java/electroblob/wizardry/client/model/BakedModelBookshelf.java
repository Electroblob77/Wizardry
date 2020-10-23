package electroblob.wizardry.client.model;

import electroblob.wizardry.block.BlockBookshelf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IExtendedBlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BakedModelBookshelf implements IBakedModel {

	private final IBakedModel bookshelf;
	private final IBakedModel[][] books;

	public BakedModelBookshelf(IBakedModel bookshelf, IBakedModel[][] books){
		this.bookshelf = bookshelf;
		this.books = books;
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand){

		IBakedModel fallback = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelManager().getMissingModel();
		if(state == null) return fallback.getQuads(null, side, rand);

		// It took me waaay too long to realise I needed a new list here
		List<BakedQuad> quads = new ArrayList<>(bookshelf.getQuads(state, side, rand)); // Lists ain't immutable, chief

		IExtendedBlockState extendedState = (IExtendedBlockState)state;

		for(int i = 0; i<BlockBookshelf.SLOT_COUNT; i++){
			Integer value = extendedState.getValue(BlockBookshelf.BOOKS[i]);
			if(value == null || value < 0) return fallback.getQuads(null, side, rand);
			if(value < books.length) quads.addAll(books[value][i].getQuads(state, side, rand)); // Empty slots use books.length
		}

		return quads;
	}

	@Override
	public boolean isBuiltInRenderer(){
		return false;
	}

	// Delegate everything else to the main bookshelf model

	@Override
	public boolean isAmbientOcclusion(){
		return bookshelf.isAmbientOcclusion();
	}

	@Override
	public boolean isGui3d(){
		return bookshelf.isGui3d();
	}

	@Override
	public TextureAtlasSprite getParticleTexture(){
		return bookshelf.getParticleTexture();
	}

	@Override
	public ItemOverrideList getOverrides(){
		return bookshelf.getOverrides();
	}

}
