package electroblob.wizardry.block;

import java.util.Random;

import electroblob.wizardry.registry.WizardryItems;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;

public class BlockCrystalOre extends Block {

	public BlockCrystalOre(Material material) {
		super(material);
		this.setSoundType(SoundType.STONE);
		setResistance(5.0F);
		setHarvestLevel("pickaxe", 2);
	}
	
	@Override
	public int quantityDropped(IBlockState state, int fortune, Random random)
    {
		if(fortune > 0){
			return random.nextInt(2) + 1 + random.nextInt(fortune);
		}else{
			return random.nextInt(2) + 1;
		}
    }
	
	@Override
	public Item getItemDropped(IBlockState state, Random random, int fortune){
		return WizardryItems.magic_crystal;
	}
}
