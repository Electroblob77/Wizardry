package electroblob.wizardry.item;

import electroblob.wizardry.registry.WizardryTabs;
import net.minecraft.item.Item;

public class ItemSpectralDust extends Item {

	public ItemSpectralDust(){
		super();
        this.setMaxDamage(0);
        this.setCreativeTab(WizardryTabs.WIZARDRY);
    }
}
