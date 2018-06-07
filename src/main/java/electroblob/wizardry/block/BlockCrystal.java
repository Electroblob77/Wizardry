package electroblob.wizardry.block;

import java.util.Random;

import electroblob.wizardry.Wizardry;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;

public class BlockCrystal extends Block {

	public BlockCrystal(Material material) {
		super(material);
		setHarvestLevel("pickaxe", 2);
	}
}
