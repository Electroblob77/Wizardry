package electroblob.wizardry.item;

import electroblob.wizardry.constants.Element;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class ItemBlockMultiTexturedElemental extends ItemBlock implements IMultiTexturedItem {
	
	private final boolean separateNames;

	public ItemBlockMultiTexturedElemental(Block block, boolean separateNames){
		super(block);
		this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.separateNames = separateNames;
	}
	
	@Override
	public ResourceLocation getModelName(ItemStack stack){
		int metadata = stack.getMetadata();
		if(metadata >= Element.values().length) metadata = 0;
		return getModelName(metadata);
	}

	public ResourceLocation getModelName(int metadata){
		return new ResourceLocation(this.block.getRegistryName().getNamespace(),
				Element.values()[metadata].getName() + "_" + this.block.getRegistryName().getPath());
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
