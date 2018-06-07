package electroblob.wizardry.block;

import java.util.Random;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryGuiHandler;
import electroblob.wizardry.client.GuiArcaneWorkbench;
import electroblob.wizardry.tileentity.TileEntityArcaneWorkbench;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class BlockArcaneWorkbench extends BlockContainer{
	
	public IIcon[] icons = new IIcon[6];

	public BlockArcaneWorkbench(){
        super(Material.rock);
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.75F, 1.0F);
        this.setLightLevel(0.8f);
        this.setCreativeTab(CreativeTabs.tabDecorations);
        this.setBlockTextureName("wizardry:arcane_workbench");
	}

	@Override
	public void registerBlockIcons(IIconRegister par1IconRegister) {
	    this.icons[0] = par1IconRegister.registerIcon(this.textureName + "_bottom");
	    this.icons[1] = par1IconRegister.registerIcon(this.textureName + "_top");
	    this.icons[2] = par1IconRegister.registerIcon(this.textureName + "_side");
	    this.icons[3] = par1IconRegister.registerIcon(this.textureName + "_side");
	    this.icons[4] = par1IconRegister.registerIcon(this.textureName + "_side");
	    this.icons[5] = par1IconRegister.registerIcon(this.textureName + "_side");
	}
	
	
	
	@Override
	public IIcon getIcon(int side, int meta) {
	    return this.icons[side];
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileEntityArcaneWorkbench();
	}
	
	@Override
	public boolean renderAsNormalBlock()
    {
        return false;
    }
    
	@Override
	public boolean isOpaqueCube()
    {
        return false;
    }
    
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int metadata, float what, float these, float are) {
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (tileEntity == null || player.isSneaking()) {
			return false;
		}
		player.openGui(Wizardry.instance, WizardryGuiHandler.ARCANE_WORKBENCH, world, x, y, z);
		return true;
	}
	
	@Override
	public void breakBlock(World par1World, int par2, int par3, int par4, Block block, int par6)
    {
		Random random = new Random();
        TileEntityArcaneWorkbench tileentityarcaneworkbench = (TileEntityArcaneWorkbench)par1World.getTileEntity(par2, par3, par4);

        if (tileentityarcaneworkbench != null)
        {
            for (int j1 = 0; j1 < tileentityarcaneworkbench.getSizeInventory(); ++j1)
            {
                ItemStack itemstack = tileentityarcaneworkbench.getStackInSlot(j1);

                if (itemstack != null)
                {
                    float f = random.nextFloat() * 0.8F + 0.1F;
                    float f1 = random.nextFloat() * 0.8F + 0.1F;
                    EntityItem entityitem;

                    for (float f2 = random.nextFloat() * 0.8F + 0.1F; itemstack.stackSize > 0; par1World.spawnEntityInWorld(entityitem))
                    {
                        int k1 = random.nextInt(21) + 10;

                        if (k1 > itemstack.stackSize)
                        {
                            k1 = itemstack.stackSize;
                        }

                        itemstack.stackSize -= k1;
                        entityitem = new EntityItem(par1World, (double)((float)par2 + f), (double)((float)par3 + f1), (double)((float)par4 + f2), new ItemStack(itemstack.getItem(), k1, itemstack.getItemDamage()));
                        float f3 = 0.05F;
                        entityitem.motionX = (double)((float)random.nextGaussian() * f3);
                        entityitem.motionY = (double)((float)random.nextGaussian() * f3 + 0.2F);
                        entityitem.motionZ = (double)((float)random.nextGaussian() * f3);

                        if (itemstack.hasTagCompound())
                        {
                            entityitem.getEntityItem().setTagCompound((NBTTagCompound)itemstack.getTagCompound().copy());
                        }
                    }
                }
            }

            //par1World.func_96440_m(par2, par3, par4, block);
        }

        super.breakBlock(par1World, par2, par3, par4, block, par6);
    }

}
