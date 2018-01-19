package electroblob.wizardry.item;

import java.util.List;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryGuiHandler;
import electroblob.wizardry.registry.WizardryTabs;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemWizardHandbook extends Item {

	// Yep, I hardcoded my own name into the mod. Don't want people changing it now, do I?
	private static final String AUTHOR = "Electroblob";
	
	public ItemWizardHandbook() {
		super();
		this.setMaxStackSize(1);
		this.setCreativeTab(WizardryTabs.WIZARDRY);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> tooltip, boolean advanced){
		tooltip.add("\u00A77" + net.minecraft.client.resources.I18n.format("item.wizardry:wizard_handbook.desc", AUTHOR));
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand){
		player.openGui(Wizardry.instance, WizardryGuiHandler.WIZARD_HANDBOOK, world, 0, 0, 0);
		return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
	}
	
	@Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tab, List<ItemStack> items){
        items.add(new ItemStack(this, 1));
    }
}
