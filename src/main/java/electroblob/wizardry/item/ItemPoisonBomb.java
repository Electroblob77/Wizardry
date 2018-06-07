package electroblob.wizardry.item;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.entity.projectile.EntityPoisonBomb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemPoisonBomb extends Item {

    public ItemPoisonBomb()
    {
        this.maxStackSize = 16;
        this.setCreativeTab(Wizardry.tabWizardry);
    }

    /**
     * Called whenever this item is equipped and the right mouse button is pressed. Args: itemStack, world, entityPlayer
     */
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer entityplayer)
    {
        if (!entityplayer.capabilities.isCreativeMode)
        {
            --stack.stackSize;
        }

        world.playSoundAtEntity(entityplayer, "random.bow", 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

        if (!world.isRemote)
        {
            world.spawnEntityInWorld(new EntityPoisonBomb(world, entityplayer));
        }

        return stack;
    }
}