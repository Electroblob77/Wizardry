package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class Permafrost extends SpellRay {

	public Permafrost(){
		super("permafrost", SpellActions.POINT, true);
		this.particleVelocity(1);
		this.particleSpacing(0.5);
		soundValues(0.5f, 1, 0);
		addProperties(DURATION, DAMAGE, EFFECT_DURATION, EFFECT_STRENGTH);
		this.ignoreLivingEntities(true);
	}

	@Override
	protected SoundEvent[] createSounds(){
		return this.createContinuousSpellSounds();
	}

	@Override
	protected void playSound(World world, EntityLivingBase entity, int ticksInUse, int duration, SpellModifiers modifiers, String... sounds){
		this.playSoundLoop(world, entity, ticksInUse);
	}

	@Override
	protected void playSound(World world, double x, double y, double z, int ticksInUse, int duration, SpellModifiers modifiers, String... sounds){
		this.playSoundLoop(world, x, y, z, ticksInUse, duration);
	}

	@Override
	protected boolean onEntityHit(World world, Entity target, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){

		boolean flag = false;

		if(!world.isRemote){

			int blastUpgradeCount = (int)((modifiers.get(WizardryItems.blast_upgrade) - 1) / Constants.BLAST_RADIUS_INCREASE_PER_LEVEL + 0.5f);
			// Results in the following patterns:
			// 0 blast upgrades: single block
			// 1 blast upgrade: 3x3 without corners or edges
			// 2 blast upgrades: 3x3 with corners
			// 3 blast upgrades: 5x5 without corners or edges
			float radius = 0.5f + 0.73f * blastUpgradeCount;

			int duration = (int)(getProperty(DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade));

			List<BlockPos> sphere = BlockUtils.getBlockSphere(pos.up(), radius);

			for(BlockPos pos1 : sphere){
				flag |= tryToPlaceIce(world, pos1, caster, duration);
			}

			return flag;

		}

		return true;

	}

	private boolean tryToPlaceIce(World world, BlockPos pos, EntityLivingBase caster, int duration){

		if(world.getBlockState(pos.down()).isSideSolid(world, pos.down(), EnumFacing.UP) && BlockUtils.canBlockBeReplaced(world, pos)){
			if(BlockUtils.canPlaceBlock(caster, world, pos)){
				world.setBlockState(pos, WizardryBlocks.permafrost.getDefaultState());
				world.scheduleUpdate(pos.toImmutable(), WizardryBlocks.permafrost, duration);
				return true;
			}
		}

		return false;
	}

	@Override
	protected boolean onMiss(World world, EntityLivingBase caster, Vec3d origin, Vec3d direction, int ticksInUse, SpellModifiers modifiers){
		return true;
	}

	@Override
	protected void spawnParticle(World world, double x, double y, double z, double vx, double vy, double vz){
		float brightness = world.rand.nextFloat();
		ParticleBuilder.create(Type.DUST).pos(x, y, z).vel(vx, vy, vz).time(8 + world.rand.nextInt(12))
				.clr(0.4f + 0.6f * brightness, 0.6f + 0.4f*brightness, 1).spawn(world);
		ParticleBuilder.create(Type.SNOW).pos(x, y, z).vel(vx, vy, vz).time(8 + world.rand.nextInt(12)).spawn(world);
	}

}
