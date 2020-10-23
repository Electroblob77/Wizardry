package electroblob.wizardry.block;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.EntityUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockPermafrost extends BlockDryFrostedIce {

	protected static final AxisAlignedBB BOUNDING_BOX = new AxisAlignedBB(0, 0, 0, 1, 0, 1);
	protected static final AxisAlignedBB SELECTION_BOUNDING_BOX = new AxisAlignedBB(0, 0, 0, 1, 0.125, 1);

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos){
		return BOUNDING_BOX;
	}

	@Override
	public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World worldIn, BlockPos pos){
		return SELECTION_BOUNDING_BOX;
	}

	@Nullable
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos){
		return super.getCollisionBoundingBox(blockState, worldIn, pos);
	}

	@Override
	public boolean isFullCube(IBlockState state){
		return false;
	}

	@Override
	public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity){

		if(EntityUtils.isLiving(entity) && entity.ticksExisted % 30 == 0){
			// Can't make it player damage unless we make this block a tile entity, but there will be too many for that
			entity.attackEntityFrom(DamageSource.MAGIC, Spells.permafrost.getProperty(Spell.DAMAGE).floatValue());
			int duration = Spells.permafrost.getProperty(Spell.EFFECT_DURATION).intValue();
			int amplifier = Spells.permafrost.getProperty(Spell.EFFECT_STRENGTH).intValue();
			((EntityLivingBase)entity).addPotionEffect(new PotionEffect(WizardryPotions.frost, duration, amplifier));
		}
	}
}
