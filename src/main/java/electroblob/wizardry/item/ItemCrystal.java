package electroblob.wizardry.item;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.registry.WizardryTabs;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

/** Note that in 1.13, <i>the flattening</i> will make this class redundant, much like ItemCoal, which is probably its
 * closest analog in vanilla. */
public class ItemCrystal extends Item implements IMultiTexturedItem {

	public ItemCrystal(){
		super();
	    this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.setCreativeTab(WizardryTabs.WIZARDRY);
    }

	@Override
	public ResourceLocation getModelName(ItemStack stack){
		int metadata = stack.getMetadata();
		if(metadata >= Element.values().length) metadata = 0;
		return new ResourceLocation(Wizardry.MODID, "crystal_" + Element.values()[metadata].getName());
	}

	@Override
    public String getTranslationKey(ItemStack stack){
		return "item." + this.getModelName(stack).toString();
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items){
        if(tab == WizardryTabs.WIZARDRY){
        	for(Element element : Element.values()){
        		items.add(new ItemStack(this, 1, element.ordinal()));
        	}
        }
    }

}
