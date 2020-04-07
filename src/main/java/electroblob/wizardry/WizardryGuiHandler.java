package electroblob.wizardry;

import electroblob.wizardry.inventory.ContainerArcaneWorkbench;
import electroblob.wizardry.inventory.ContainerBookshelf;
import electroblob.wizardry.inventory.ContainerPortableWorkbench;
import electroblob.wizardry.item.ItemSpellBook;
import electroblob.wizardry.item.ItemWizardHandbook;
import electroblob.wizardry.tileentity.TileEntityArcaneWorkbench;
import electroblob.wizardry.tileentity.TileEntityBookshelf;
import electroblob.wizardry.tileentity.TileEntityLectern;
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
	public static final int BOOKSHELF = nextGuiId++;
	public static final int LECTERN = nextGuiId++;

	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z){

		if(id == ARCANE_WORKBENCH){

			TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));

			if(tileEntity instanceof TileEntityArcaneWorkbench){
				return new ContainerArcaneWorkbench(player.inventory, (TileEntityArcaneWorkbench)tileEntity);
			}

		}else if(id == PORTABLE_CRAFTING){
			return new ContainerPortableWorkbench(player.inventory, world, new BlockPos(x, y, z));

		}else if(id == BOOKSHELF){

			TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));

			if(tileEntity instanceof TileEntityBookshelf){
				return new ContainerBookshelf(player.inventory, (TileEntityBookshelf)tileEntity);
			}
		}

		return null;
	}

	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z){

		if(id == ARCANE_WORKBENCH){

			TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));

			if(tileEntity instanceof TileEntityArcaneWorkbench){
				return new electroblob.wizardry.client.gui.GuiArcaneWorkbench(player.inventory,
						(TileEntityArcaneWorkbench)tileEntity);
			}

		}else if(id == WIZARD_HANDBOOK && (player.getHeldItemMainhand().getItem() instanceof ItemWizardHandbook
				|| player.getHeldItemOffhand().getItem() instanceof ItemWizardHandbook)){

			return new electroblob.wizardry.client.gui.handbook.GuiWizardHandbook();

		}else if(id == SPELL_BOOK){

			if(player.getHeldItemMainhand().getItem() instanceof ItemSpellBook){
				return new electroblob.wizardry.client.gui.GuiSpellBook(player.getHeldItemMainhand());
			}else if(player.getHeldItemOffhand().getItem() instanceof ItemSpellBook){
				return new electroblob.wizardry.client.gui.GuiSpellBook(player.getHeldItemOffhand());
			}

		}else if(id == PORTABLE_CRAFTING){
			return new electroblob.wizardry.client.gui.GuiPortableCrafting(player.inventory, world, new BlockPos(x, y, z));

		}else if(id == BOOKSHELF){

			TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));

			if(tileEntity instanceof TileEntityBookshelf){
				return new electroblob.wizardry.client.gui.GuiBookshelf(player.inventory,
						(TileEntityBookshelf)tileEntity);
			}

		}else if(id == LECTERN){

			TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));

			if(tileEntity instanceof TileEntityLectern){
				return new electroblob.wizardry.client.gui.GuiLectern((TileEntityLectern)tileEntity);
			}

		}

		return null;
	}
}