package electroblob.wizardry.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ContainerPortableWorkbench extends ContainerWorkbench {

	public ContainerPortableWorkbench(InventoryPlayer inventory, World world, BlockPos pos){

		super(inventory, world, pos);
	}

	// Overriden to stop the crafting gui from closing when there is no crafting table.
	@Override
	public boolean canInteractWith(EntityPlayer player){
		return true;
	}

}
