package electroblob.wizardry.block;

import java.util.Random;

import electroblob.wizardry.Wizardry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;

public class BlockCrystalOre extends Block {

	public BlockCrystalOre(Material material) {
		super(material);
		setStepSound(Block.soundTypeStone);
		setBlockName("crystalOre");
		setResistance(5.0F);
		setHarvestLevel("pickaxe", 2);
	}
	
	@Override
	public int quantityDropped(int meta, int fortune, Random random)
    {
		if(fortune > 0){
			return random.nextInt(2) + 1 + random.nextInt(fortune);
		}else{
			return random.nextInt(2) + 1;
		}
    }
	
	@Override
	public Item getItemDropped(int Metadata, Random random, int fortune){
		return Wizardry.magicCrystal;
	}
}
