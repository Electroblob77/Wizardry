package electroblob.wizardry;

import electroblob.wizardry.item.ItemSpellBook;
import electroblob.wizardry.item.ItemWizardHandbook;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.tileentity.ContainerArcaneWorkbench;
import electroblob.wizardry.tileentity.ContainerPortableWorkbench;
import electroblob.wizardry.tileentity.TileEntityArcaneWorkbench;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class WizardryGuiHandler implements IGuiHandler {

	/** Incrementable index for the gui ID */
	private static int nextGuiId = 0;

	public static final int SPELL_BOOK = nextGuiId++;
	public static final int ARCANE_WORKBENCH = nextGuiId++;
	public static final int WIZARD_HANDBOOK = nextGuiId++;
	public static final int PORTABLE_CRAFTING = nextGuiId++;

	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z){
		if(id == ARCANE_WORKBENCH){
			TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
			if(tileEntity instanceof TileEntityArcaneWorkbench){
				return new ContainerArcaneWorkbench(player.inventory, (TileEntityArcaneWorkbench)tileEntity);
			}
		}else if(id == PORTABLE_CRAFTING){
			return new ContainerPortableWorkbench(player.inventory, world, new BlockPos(x, y, z));
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z){
		if(id == ARCANE_WORKBENCH){
			TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
			if(tileEntity instanceof TileEntityArcaneWorkbench){
				return new electroblob.wizardry.client.GuiArcaneWorkbench(player.inventory,
						(TileEntityArcaneWorkbench)tileEntity);
			}
		}else if(id == WIZARD_HANDBOOK && ((player.getHeldItemMainhand() != null
				&& player.getHeldItemMainhand().getItem() instanceof ItemWizardHandbook)
				|| (player.getHeldItemOffhand() != null
						&& player.getHeldItemOffhand().getItem() instanceof ItemWizardHandbook))){
			return new electroblob.wizardry.client.GuiWizardHandbook();
		}else if(id == SPELL_BOOK){
			if(player.getHeldItemMainhand() != null && player.getHeldItemMainhand().getItem() instanceof ItemSpellBook){
				return new electroblob.wizardry.client.GuiSpellBook(
						Spell.get(player.getHeldItemMainhand().getItemDamage()));
			}else if(player.getHeldItemOffhand() != null
					&& player.getHeldItemOffhand().getItem() instanceof ItemSpellBook){
				return new electroblob.wizardry.client.GuiSpellBook(
						Spell.get(player.getHeldItemOffhand().getItemDamage()));
			}
		}else if(id == PORTABLE_CRAFTING){
			return new electroblob.wizardry.client.GuiPortableCrafting(player.inventory, world, new BlockPos(x, y, z));
		}
		return null;
	}
}