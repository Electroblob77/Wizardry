package electroblob.wizardry.entity;

import java.util.Iterator;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityFallingGrass extends EntityFallingBlock {

	public EntityFallingGrass(World world) {
		super(world);
	}

	public EntityFallingGrass(World world, double x, double y, double z,
			Block block) {
		super(world, x, y, z, block);
	}

	public EntityFallingGrass(World world, double x, double y, double z,
			Block block, int metadata) {
		super(world, x, y, z, block, metadata);
	}

	@Override
	public void onUpdate(){

		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		++this.field_145812_b;
		this.motionY -= 0.03999999910593033D;
		this.moveEntity(this.motionX, this.motionY, this.motionZ);
		this.motionX *= 0.9800000190734863D;
		this.motionY *= 0.9800000190734863D;
		this.motionZ *= 0.9800000190734863D;

		if (!this.worldObj.isRemote)
		{
			int i = MathHelper.floor_double(this.posX);
			int j = MathHelper.floor_double(this.posY);
			int k = MathHelper.floor_double(this.posZ);

			if (this.field_145812_b == 1)
			{
				if (this.worldObj.getBlock(i, j, k) != Blocks.grass)
				{
					this.setDead();
					return;
				}

				this.worldObj.setBlockToAir(i, j, k);
			}

			if (this.onGround)
			{
				this.motionX *= 0.699999988079071D;
				this.motionZ *= 0.699999988079071D;
				this.motionY *= -0.5D;

				if (this.worldObj.getBlock(i, j, k) != Blocks.piston_extension)
				{
					this.setDead();

                    if (this.worldObj.canPlaceEntityOnSide(Blocks.grass, i, j, k, true, 1, (Entity)null, (ItemStack)null) && !BlockFalling.func_149831_e(this.worldObj, i, j - 1, k) && this.worldObj.setBlock(i, j, k, Blocks.grass, this.field_145814_a, 3))
                    {
                        // The stuff in here only related to tile entities, so was removed, but since the enclosing if statement
                    	// involves setting the block it has to stay (and I'm too lazy to rearrange it).
                    }
                }
            }
			else if (this.field_145812_b > 100 && !this.worldObj.isRemote && (j < 1 || j > 256) || this.field_145812_b > 600)
			{
				if (this.field_145813_c)
				{
					this.entityDropItem(new ItemStack(Blocks.grass, 1, Blocks.grass.damageDropped(this.field_145814_a)), 0.0F);
				}

				this.setDead();
			}
		}

	}
}
