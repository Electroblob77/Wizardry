package electroblob.wizardry.block;

import java.util.Random;

import electroblob.wizardry.ExtendedPlayer;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryRegistry;
import electroblob.wizardry.item.ItemWand;
import electroblob.wizardry.spell.Spell;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockTransportationStone extends Block{

	public BlockTransportationStone(Material material){
		super(material);
		this.setBlockBounds(0.0625f*5, 0, 0.0625f*5, 0.0625f*11, 0.0625f*6, 0.0625f*11);
        this.setTickRandomly(true);
	}

	@Override
	public boolean isOpaqueCube(){
		return false;
	}
	
	@Override
	public boolean renderAsNormalBlock(){
		return false;
	}
	
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block)
    {
        super.onNeighborBlockChange(world, x, y, z, block);
        if(!world.isSideSolid(x, y-1, z, ForgeDirection.UP)){
        	this.dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
        	world.setBlockToAir(x, y, z);
        }
    }
	
	@Override
	public void updateTick(World world, int x, int y, int z, Random random)
    {
        if(!world.isSideSolid(x, y-1, z, ForgeDirection.UP)){
        	this.dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
        	world.setBlockToAir(x, y, z);
        }
    }
	
	@Override
	public boolean canPlaceBlockAt(World par1World, int x, int y, int z)
    {
        return super.canPlaceBlockAt(par1World, x, y, z) && par1World.isSideSolid(x, y-1, z, ForgeDirection.UP);
    }
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityplayer, int par6, float par7, float par8, float par9)
    {
		if(entityplayer.getHeldItem() != null && entityplayer.getHeldItem().getItem() instanceof ItemWand){
			if(ExtendedPlayer.get(entityplayer) != null){
				ExtendedPlayer extendedplayer = ExtendedPlayer.get(entityplayer);
				
				int x1=x-1, z1=z-1;
				if(testForCircle(world, x1, y, z1)){
					extendedplayer.setStoneLocation(x1, y, z1, world.provider.dimensionId);
					if(!world.isRemote) entityplayer.addChatMessage(new ChatComponentTranslation("tile.transportationStone.confirm", WizardryRegistry.transportation.getDisplayNameWithFormatting()));//"You will now be returned here upon casting " + EnumSpell.TRANSPORTATION.name);
					return true;
				}
				
				x1=x;
				z1=z-1;
				if(testForCircle(world, x1, y, z1)){
					extendedplayer.setStoneLocation(x1, y, z1, world.provider.dimensionId);
					if(!world.isRemote) entityplayer.addChatMessage(new ChatComponentTranslation("tile.transportationStone.confirm", WizardryRegistry.transportation.getDisplayNameWithFormatting()));//"You will now be returned here upon casting " + EnumSpell.TRANSPORTATION.name);
					return true;
				}
				
				x1=x+1;
				z1=z-1;
				if(testForCircle(world, x1, y, z1)){
					extendedplayer.setStoneLocation(x1, y, z1, world.provider.dimensionId);
					if(!world.isRemote) entityplayer.addChatMessage(new ChatComponentTranslation("tile.transportationStone.confirm", WizardryRegistry.transportation.getDisplayNameWithFormatting()));//"You will now be returned here upon casting " + EnumSpell.TRANSPORTATION.name);
					return true;
				}
				
				x1=x-1;
				z1=z;
				if(testForCircle(world, x1, y, z1)){
					extendedplayer.setStoneLocation(x1, y, z1, world.provider.dimensionId);
					if(!world.isRemote) entityplayer.addChatMessage(new ChatComponentTranslation("tile.transportationStone.confirm", WizardryRegistry.transportation.getDisplayNameWithFormatting()));//"You will now be returned here upon casting " + EnumSpell.TRANSPORTATION.name);
					return true;
				}
				
				x1=x+1;
				z1=z;
				if(testForCircle(world, x1, y, z1)){
					extendedplayer.setStoneLocation(x1, y, z1, world.provider.dimensionId);
					if(!world.isRemote) entityplayer.addChatMessage(new ChatComponentTranslation("tile.transportationStone.confirm", WizardryRegistry.transportation.getDisplayNameWithFormatting()));//"You will now be returned here upon casting " + EnumSpell.TRANSPORTATION.name);
					return true;
				}
				
				x1=x-1;
				z1=z+1;
				if(testForCircle(world, x1, y, z1)){
					extendedplayer.setStoneLocation(x1, y, z1, world.provider.dimensionId);
					if(!world.isRemote) entityplayer.addChatMessage(new ChatComponentTranslation("tile.transportationStone.confirm", WizardryRegistry.transportation.getDisplayNameWithFormatting()));//"You will now be returned here upon casting " + EnumSpell.TRANSPORTATION.name);
					return true;
				}
				
				x1=x;
				z1=z+1;
				if(testForCircle(world, x1, y, z1)){
					extendedplayer.setStoneLocation(x1, y, z1, world.provider.dimensionId);
					if(!world.isRemote) entityplayer.addChatMessage(new ChatComponentTranslation("tile.transportationStone.confirm", WizardryRegistry.transportation.getDisplayNameWithFormatting()));//"You will now be returned here upon casting " + EnumSpell.TRANSPORTATION.name);
					return true;
				}
				
				x1=x+1;
				z1=z+1;
				if(testForCircle(world, x1, y, z1)){
					extendedplayer.setStoneLocation(x1, y, z1, world.provider.dimensionId);
					if(!world.isRemote) entityplayer.addChatMessage(new ChatComponentTranslation("tile.transportationStone.confirm", WizardryRegistry.transportation.getDisplayNameWithFormatting()));//"You will now be returned here upon casting " + EnumSpell.TRANSPORTATION.name);
					return true;
				}
				
				if(!world.isRemote) entityplayer.addChatMessage(new ChatComponentTranslation("tile.transportationStone.invalid"));//"You must make a circle with 8 stones of transportation first!");
				return true;
			}
		}
        return false;
    }
	
	/** Returns whether the specified location is surrounded by a complete cicle of 8 transportation stones. */
	public static boolean testForCircle(World world, int x, int y, int z){
		return world.getBlock(x-1, y, z-1) == Wizardry.transportationStone
				&& world.getBlock(x, y, z-1) == Wizardry.transportationStone
				&& world.getBlock(x+1, y, z-1) == Wizardry.transportationStone
				&& world.getBlock(x-1, y, z) == Wizardry.transportationStone
				&& !world.getBlock(x, y, z).getMaterial().blocksMovement()
				&& world.getBlock(x+1, y, z) == Wizardry.transportationStone
				&& world.getBlock(x-1, y, z+1) == Wizardry.transportationStone
				&& world.getBlock(x, y, z+1) == Wizardry.transportationStone
				&& world.getBlock(x+1, y, z+1) == Wizardry.transportationStone;
	}
}
