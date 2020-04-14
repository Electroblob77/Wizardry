package electroblob.wizardry.block;

import electroblob.wizardry.registry.WizardryTabs;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.SoundType;

public class BlockGildedWood extends BlockPlanks {

	public BlockGildedWood(){
		super();
		this.setCreativeTab(WizardryTabs.WIZARDRY);
		this.setHardness(2.0F);
		this.setResistance(5.0F);
		this.setSoundType(SoundType.WOOD); // Why is this protected?!
	}

}
