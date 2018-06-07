package electroblob.wizardry;

import cpw.mods.fml.common.network.IGuiHandler;
import electroblob.wizardry.client.GuiArcaneWorkbench;
import electroblob.wizardry.client.GuiPortableCrafting;
import electroblob.wizardry.client.GuiSpellBook;
import electroblob.wizardry.client.GuiWizardHandbook;
import electroblob.wizardry.item.ItemSpellBook;
import electroblob.wizardry.item.ItemWizardHandbook;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.tileentity.ContainerArcaneWorkbench;
import electroblob.wizardry.tileentity.ContainerPortableWorkbench;
import electroblob.wizardry.tileentity.TileEntityArcaneWorkbench;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class WizardryGuiHandler implements IGuiHandler {

	/** Incrementable index for the gui ID */
	private static int nextGuiId = 0;
	
	public static final int SPELL_BOOK = nextGuiId++;
	public static final int ARCANE_WORKBENCH = nextGuiId++;
	public static final int WIZARD_HANDBOOK = nextGuiId++;
	public static final int PORTABLE_CRAFTING = nextGuiId++;

	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world,
			int x, int y, int z) {
		if(id == ARCANE_WORKBENCH){
			TileEntity tileEntity = world.getTileEntity(x, y, z);
			if(tileEntity instanceof TileEntityArcaneWorkbench){
				return new ContainerArcaneWorkbench(player.inventory, (TileEntityArcaneWorkbench) tileEntity);
			}
		}
		else if(id == PORTABLE_CRAFTING){
			return new ContainerPortableWorkbench(player.inventory, world, x, y, z);
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world,
			int x, int y, int z) {
		if(id == ARCANE_WORKBENCH){
			TileEntity tileEntity = world.getTileEntity(x, y, z);
			if(tileEntity instanceof TileEntityArcaneWorkbench){
				return new GuiArcaneWorkbench(player.inventory, (TileEntityArcaneWorkbench) tileEntity);
			}
		}
		else if(id == WIZARD_HANDBOOK && player.getHeldItem().getItem() instanceof ItemWizardHandbook){
			return new GuiWizardHandbook();
		}
		else if(id == SPELL_BOOK && player.getHeldItem().getItem() instanceof ItemSpellBook){
			return new GuiSpellBook(Spell.get(player.getHeldItem().getItemDamage()));
		}
		else if(id == PORTABLE_CRAFTING){
			return new GuiPortableCrafting(player.inventory, world, x, y, z);
		}
		return null;
	}
}