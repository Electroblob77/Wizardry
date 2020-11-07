package electroblob.wizardry.item;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.registry.WizardryTabs;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import java.util.Arrays;

public class ItemSpectralDust extends Item implements IMultiTexturedItem {

	public ItemSpectralDust(){
		super();
	    this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.setCreativeTab(WizardryTabs.WIZARDRY);
    }

	@Override
	public ResourceLocation getModelName(ItemStack stack){
		int metadata = stack.getMetadata();
		if(metadata >= Element.values().length) metadata = 0;
		return new ResourceLocation(Wizardry.MODID, "spectral_dust_" + Element.values()[metadata].getName());
	}

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items){
        if(tab == WizardryTabs.WIZARDRY){
        	for(Element element : Arrays.copyOfRange(Element.values(), 1, Element.values().length)){
        		items.add(new ItemStack(this, 1, element.ordinal()));
        	}
        }
    }

}
