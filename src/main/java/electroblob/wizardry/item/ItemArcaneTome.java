package electroblob.wizardry.item;

import java.util.List;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryTabs;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemArcaneTome extends Item {

	public ItemArcaneTome(){
		super();
		setHasSubtypes(true);
		setMaxStackSize(1);
		setCreativeTab(WizardryTabs.WIZARDRY);
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list){
		if(tab == WizardryTabs.WIZARDRY){ // Don't use isInCreativeTab here.
			for(int i = 1; i < Tier.values().length; i++){
				list.add(new ItemStack(this, 1, i));
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack stack){
		return true;
	}

	@Override
	public EnumRarity getRarity(ItemStack stack){
		switch(this.getDamage(stack)){
		case 1:
			return EnumRarity.UNCOMMON;
		case 2:
			return EnumRarity.RARE;
		case 3:
			return EnumRarity.EPIC;
		}
		return EnumRarity.COMMON;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag showAdvanced){
		Tier tier = Tier.values()[stack.getItemDamage()];
		Tier tier2 = Tier.values()[stack.getItemDamage() - 1];
		tooltip.add(tier.getDisplayNameWithFormatting());
		tooltip.add("\u00A77" + net.minecraft.client.resources.I18n.format("item." + Wizardry.MODID + ":arcane_tome.desc1",
				tier2.getDisplayNameWithFormatting()));
		tooltip.add("\u00A77" + net.minecraft.client.resources.I18n.format("item." + Wizardry.MODID + ":arcane_tome.desc2",
				tier.getDisplayNameWithFormatting() + "\u00A77"));
	}

}
