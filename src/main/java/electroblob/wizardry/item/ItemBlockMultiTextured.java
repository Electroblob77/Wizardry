package electroblob.wizardry.item;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class ItemBlockMultiTextured extends ItemBlock implements IMultiTexturedItem {
	
	private final boolean separateNames;
	private final String[] prefixes;

	public ItemBlockMultiTextured(Block block, boolean separateNames, String... prefixes){
		super(block);
		this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.separateNames = separateNames;
        this.prefixes = prefixes;
	}
	
	@Override
	public ResourceLocation getModelName(ItemStack stack){
		int metadata = stack.getMetadata();
		if(metadata >= prefixes.length) metadata = 0;
		return getModelName(metadata);
	}

	public ResourceLocation getModelName(int metadata){
		return new ResourceLocation(this.block.getRegistryName().getNamespace(),
				prefixes[metadata] + "_" + this.block.getRegistryName().getPath());
	}

	@Override
    public String getTranslationKey(ItemStack stack){
		return this.separateNames ? "tile." + this.getModelName(stack).toString() : super.getTranslationKey(stack);
    }
	
	@Override
	public int getMetadata(int metadata){
        return metadata;
    }

}
