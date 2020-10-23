package electroblob.wizardry.client.model;

import electroblob.wizardry.Wizardry;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;

public class ModelLoaderBookshelf implements ICustomModelLoader {

	private static final String SMART_MODEL_PREFIX = "models/block/smart_model/";

	@Override
	public void onResourceManagerReload(IResourceManager resourceManager){
		// Do nothing
	}

	@Override
	public boolean accepts(ResourceLocation modelLocation){
		return modelLocation.getNamespace().equals(Wizardry.MODID) && modelLocation.getPath().startsWith(SMART_MODEL_PREFIX);
	}

	@Override
	public IModel loadModel(ResourceLocation modelLocation){
		return new ModelBookshelf(modelLocation.getPath().substring(SMART_MODEL_PREFIX.length()));
	}

}
