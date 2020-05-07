package electroblob.wizardry.client.model;

import com.google.common.collect.ImmutableList;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.block.BlockBookshelf;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class ModelBookshelf implements IModel {

	private static final ResourceLocation BOOK_TEXTURE = new ResourceLocation(Wizardry.MODID, "blocks/books");

	private static final List<ResourceLocation> bookModelLocations = new ArrayList<>();

	private final ModelResourceLocation bookshelfModelLocation;
	private final String variant;

	static {
		for(int i = 0; i < BlockBookshelf.SLOT_COUNT; i++){
			bookModelLocations.add(new ResourceLocation(Wizardry.MODID, "bookshelf_parts/books" + i));
		}
	}

	public ModelBookshelf(String bookshelfModelName){
		String[] args = bookshelfModelName.split("#", 2);
		String model = args[0];
		variant = args.length == 2 ? args[1] : null;
		bookshelfModelLocation = new ModelResourceLocation(new ResourceLocation(Wizardry.MODID, "bookshelf_parts/" + model), variant);
	}

	@Override
	public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter){

		try {

			IModel bookshelfModel = ModelLoaderRegistry.getModel(bookshelfModelLocation);
			// I don't know why default state works here (surely it ought to be the state param?), but it works so who cares
			IBakedModel bookshelf = bookshelfModel.bake(bookshelfModel.getDefaultState(), format, bakedTextureGetter);

			IBakedModel[] books = new IBakedModel[BlockBookshelf.SLOT_COUNT];

			for(int i = 0; i < BlockBookshelf.SLOT_COUNT; i++){
				IModel bookModel = ModelLoaderRegistry.getModel(new ModelResourceLocation(bookModelLocations.get(i), variant));
				books[i] = bookModel.bake(bookModel.getDefaultState(), format, bakedTextureGetter); // Same here!
			}

			// To clarify: the whole point of doing this was to avoid having to bake 4 * 2^12 models per bookshelf type
			// Therefore we have to have a baked bookshelf model that's kind of dynamic
			return new BakedModelBookshelf(bookshelf, books);

		}catch(Exception exception){
			Wizardry.logger.error("Error baking bookshelf models: ", exception);
			return ModelLoaderRegistry.getMissingModel().bake(state, format, bakedTextureGetter);
		}

	}

	@Override
	public Collection<ResourceLocation> getDependencies(){
		return bookModelLocations;
	}

	@Override
	public Collection<ResourceLocation> getTextures(){

		// I simply have no idea how I'm *supposed* to do it, so I'm using the best solution I can come up with
		// This is at least better than manually registering it with TextureStitchEvent
		// However I'm also pretty sure that if a texture pack decides to change the texture that the book models point
		// to, it won't work... but since nobody ever will, I don't really care!

		// Why am I confused?
		// 1. How come it magically finds the bookshelf texture, but not the book texture?
		// 2. Why don't the textures get retrieved automatically based on getDependencies? Surely that's the point?
		// 3. Is it correct to use getModel() to get the dependency models upon construction of this class so I can
		// access their textures properly? Or is that too early?
		// 4. If so, what's the point of getDependencies, and why should I override it?
		return ImmutableList.of(BOOK_TEXTURE);
	}

}
