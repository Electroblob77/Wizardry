package electroblob.wizardry.item;

import java.util.List;

import electroblob.wizardry.registry.WizardryTabs;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemArmourUpgrade extends Item {
	
	public ItemArmourUpgrade() {
		super();
		this.setMaxStackSize(1);
		this.setCreativeTab(WizardryTabs.WIZARDRY);
	}
	
	@Override
	public EnumRarity getRarity(ItemStack stack){
        return EnumRarity.EPIC;
    }
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack stack){
		return true;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced){
		tooltip.add(net.minecraft.client.resources.I18n.format("item.wizardry:armour_upgrade.desc1", "\u00A77"));
		tooltip.add(net.minecraft.client.resources.I18n.format("item.wizardry:armour_upgrade.desc2", "\u00A77", "\u00A7d"));
	}
	
	@Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tab, List<ItemStack> subItems){
		subItems.add(new ItemStack(this, 1));
    }

}
