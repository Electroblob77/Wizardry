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
public class ItemCrystal extends Item {

	public ItemCrystal(){
		super();
        this.setMaxDamage(0);
        this.setCreativeTab(WizardryTabs.WIZARDRY);
    }

}
