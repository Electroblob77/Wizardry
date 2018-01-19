package electroblob.wizardry.block;

import java.util.Random;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.util.WizardryParticleType;
import net.minecraft.block.BlockBush;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;

// Extending BlockBush allows me to remove nearly everything from this class.
public class BlockCrystalFlower extends BlockBush {
	
    private static final AxisAlignedBB AABB = new AxisAlignedBB(0.5F - 0.2f, 0.0F, 0.5F - 0.2f, 0.5F + 0.2f, 0.2f * 3.0F, 0.5F + 0.2f);

	public BlockCrystalFlower(Material par2Material) {
		super(par2Material);
		this.setLightLevel(0.5f);
        this.setTickRandomly(true);
		this.setSoundType(SoundType.PLANT);
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        return AABB;
    }
	
	@Override
	public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random random){
		if(world.isRemote && random.nextBoolean()){
			Wizardry.proxy.spawnParticle(WizardryParticleType.SPARKLE, world, pos.getX()+random.nextDouble(), pos.getY()+random.nextDouble()/2+0.5, pos.getZ()+random.nextDouble(), 0d, 0.01, 0d, 20 + random.nextInt(10), 0.5f + (random.nextFloat()/2), 0.5f + (random.nextFloat()/2), 0.5f + (random.nextFloat()/2));
		}
	}

	@Override
	public EnumPlantType getPlantType(IBlockAccess world, BlockPos pos) {
		return EnumPlantType.Plains;
	}
}
