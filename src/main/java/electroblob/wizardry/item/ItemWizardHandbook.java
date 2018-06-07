package electroblob.wizardry.item;

import java.util.List;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryGuiHandler;
import electroblob.wizardry.client.GuiWizardHandbook;
import electroblob.wizardry.spell.Spell;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class ItemWizardHandbook extends Item{
	
	public ItemWizardHandbook() {
		super();
		this.setMaxStackSize(1);
		this.setCreativeTab(Wizardry.tabWizardry);
		this.setTextureName("wizardry:wizard_handbook");
		this.setUnlocalizedName("wizardHandbook");
	}
	
	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4){
		Spell spell = Spell.get(par1ItemStack.getItemDamage());
		// Yep, I hardcoded my own name into the mod. Don't want people changing it now, do I?
		par3List.add("\u00A77" + StatCollector.translateToLocalFormatted("item.wizardHandbook.desc", "Electroblob"));
	}

	public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer){
		par3EntityPlayer.openGui(Wizardry.instance, WizardryGuiHandler.WIZARD_HANDBOOK, par2World, 0, 0, 0);
		return par1ItemStack;
	}
}
