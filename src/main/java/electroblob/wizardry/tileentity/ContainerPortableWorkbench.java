package electroblob.wizardry.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.world.World;

public class ContainerPortableWorkbench extends ContainerWorkbench {

	public ContainerPortableWorkbench(InventoryPlayer p_i1808_1_, World p_i1808_2_, int p_i1808_3_, int p_i1808_4_, int p_i1808_5_) {
		super(p_i1808_1_, p_i1808_2_, p_i1808_3_, p_i1808_4_, p_i1808_5_);
	}

	// Overriden to stop the crafting gui from closing when there is no crafting table.
	@Override
	public boolean canInteractWith(EntityPlayer p_75145_1_)
    {
        return true;
    }
	
}
