package electroblob.wizardry.item;

import java.util.List;

import electroblob.wizardry.EnumTier;
import electroblob.wizardry.Wizardry;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

public class ItemArmourUpgrade extends Item{
	
	public ItemArmourUpgrade() {
		super();
		this.setMaxStackSize(1);
		this.setCreativeTab(Wizardry.tabWizardry);
		this.setTextureName("wizardry:armour_upgrade");
	}
	
	/**
     * Return an item rarity from EnumRarity
     */
	@Override
	public EnumRarity getRarity(ItemStack par1ItemStack)
    {
        return EnumRarity.epic;
    }
	
	@Override
	public boolean hasEffect(ItemStack stack, int par2){
		return true;
	}
	
	@Override
	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4){
		par3List.add(StatCollector.translateToLocalFormatted("item.armourUpgrade.desc1", "\u00A77"));
		par3List.add(StatCollector.translateToLocalFormatted("item.armourUpgrade.desc2", "\u00A77", "\u00A7d"));
		//par3List.add("\u00A77Upgrades any wizard armour");
		//par3List.add("\u00A77to make it \u00A7dlegendary");
	}

}
