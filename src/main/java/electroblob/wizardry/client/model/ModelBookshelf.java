package electroblob.wizardry.client.model;

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

import java.util.function.Function;

public class ModelBookshelf implements IModel {

	private final ModelResourceLocation bookshelfModelLocation;
	private final String variant;

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
				IModel bookModel = ModelLoaderRegistry.getModel(new ModelResourceLocation(new ResourceLocation(Wizardry.MODID, "bookshelf_parts/books" + i), variant));
				books[i] = bookModel.bake(bookModel.getDefaultState(), format, bakedTextureGetter); // Same here!
			}

			return new BakedModelBookshelf(bookshelf, books);

		}catch(Exception exception){
			Wizardry.logger.error("Error baking bookshelf models: ", exception);
			return ModelLoaderRegistry.getMissingModel().bake(state, format, bakedTextureGetter);
		}

	}

}
