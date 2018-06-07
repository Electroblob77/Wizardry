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

public class ItemArcaneTome extends Item {
	
	public ItemArcaneTome() {
		super();
		this.setHasSubtypes(true);
		this.setMaxStackSize(1);
		this.setCreativeTab(Wizardry.tabWizardry);
		this.setTextureName("wizardry:arcane_tome");
		this.setUnlocalizedName("arcaneTome");
	}
	
	@Override
	public void getSubItems(Item item, CreativeTabs par2CreativeTabs, List list){
	    for (int i=1; i<EnumTier.values().length; i++) {
	        list.add(new ItemStack(item, 1, i));
	    }
	}
	
	@Override
	public boolean hasEffect(ItemStack stack, int par2){
		return true;
	}
	
	@Override
	public EnumRarity getRarity(ItemStack par1ItemStack)
    {
    	switch(this.getDamage(par1ItemStack)){
    	case 1: return EnumRarity.uncommon;
    	case 2: return EnumRarity.rare;
    	case 3: return EnumRarity.epic;
    	}
    	return EnumRarity.common;
    }
    
	@Override
	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4){
		EnumTier tier = EnumTier.values()[par1ItemStack.getItemDamage()];
		EnumTier tier2 = EnumTier.values()[par1ItemStack.getItemDamage()-1];
		par3List.add(tier.getDisplayNameWithFormatting());
		par3List.add("\u00A77" + StatCollector.translateToLocalFormatted("item.arcaneTome.desc1", tier2.getDisplayNameWithFormatting()));
		par3List.add("\u00A77" + StatCollector.translateToLocalFormatted("item.arcaneTome.desc2", tier.getDisplayNameWithFormatting() + "\u00A77"));
	}

}
