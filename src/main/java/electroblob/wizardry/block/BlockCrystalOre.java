package electroblob.wizardry.block;

import electroblob.wizardry.registry.WizardryItems;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Random;

public class BlockCrystalOre extends Block {

	public BlockCrystalOre(Material material){
		super(material);
		this.setSoundType(SoundType.STONE);
		setResistance(5.0F);
		setHarvestLevel("pickaxe", 2);
	}

	@Override
	public int quantityDropped(IBlockState state, int fortune, Random random){
		// This now works the same way as vanilla ores
		if(fortune > 0){
			
			int i = random.nextInt(fortune + 2) - 1;
			
            if(i < 0) i = 0;
            
			return (random.nextInt(3) + 1) * (i + 1);
			
		}else{
			return random.nextInt(3) + 1;
		}
	}

	@Override
	public Item getItemDropped(IBlockState state, Random random, int fortune){
		return WizardryItems.crystal_magic;
	}
	
	@Override
	public int getExpDrop(IBlockState state, IBlockAccess world, BlockPos pos, int fortune){
		
		Random rand = world instanceof World ? ((World)world).rand : RANDOM;
		
        if(this.getItemDropped(state, rand, fortune) != Item.getItemFromBlock(this)){
            return MathHelper.getInt(rand, 1, 4);
        }
        
        return 0;
	}
}
