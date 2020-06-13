package electroblob.wizardry.spell;

import electroblob.wizardry.block.BlockStatue;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.*;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class WallOfFrost extends SpellRay {

	private static final int MINIMUM_PLACEMENT_RANGE = 2;
	
	public WallOfFrost(){
		super("wall_of_frost", true, SpellActions.POINT);
		this.particleVelocity(1);
		this.particleSpacing(0.5);
		addProperties(DURATION);
		soundValues(0.5f, 1, 0);
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
		// Wall of frost now freezes entities solid too!
		if(target instanceof EntityLiving && !world.isRemote){
			// Unchecked cast is fine because the block is a static final field
			if(((BlockStatue)WizardryBlocks.ice_statue).convertToStatue((EntityLiving)target,
					(int)(getProperty(DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)))){
				
				target.playSound(WizardrySounds.MISC_FREEZE, 1.0F, world.rand.nextFloat() * 0.4F + 0.8F);
			}
		}
		
		return true;
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){

		if(!world.isRemote && EntityUtils.canDamageBlocks(caster, world)){

			// Stops the ice being placed floating above snow and grass. Directions other than up included for
			// completeness.
			if(BlockUtils.canBlockBeReplaced(world, pos)){
				// Moves the blockpos back into the block
				pos = pos.offset(side.getOpposite());
			}

			if(origin.squareDistanceTo(pos.getX(), pos.getY(), pos.getZ()) > MINIMUM_PLACEMENT_RANGE * MINIMUM_PLACEMENT_RANGE
					&& world.getBlockState(pos).getBlock() != WizardryBlocks.ice_statue && world.getBlockState(pos).getBlock() != WizardryBlocks.dry_frosted_ice){

				pos = pos.offset(side);
				
				int duration = (int)(getProperty(DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade));

				if(BlockUtils.canBlockBeReplaced(world, pos)){
					world.setBlockState(pos, WizardryBlocks.dry_frosted_ice.getDefaultState());
					world.scheduleUpdate(pos.toImmutable(), WizardryBlocks.dry_frosted_ice, duration);
				}

				// Builds a 2 block high wall if it hits the ground
				if(side == EnumFacing.UP){
					pos = pos.offset(side);

					if(BlockUtils.canBlockBeReplaced(world, pos)){
						world.setBlockState(pos, WizardryBlocks.dry_frosted_ice.getDefaultState());
						world.scheduleUpdate(pos.toImmutable(), WizardryBlocks.dry_frosted_ice, duration);
					}
				}
			}
		}
		
		return true;
	}

	@Override
	protected boolean onMiss(World world, EntityLivingBase caster, Vec3d origin, Vec3d direction, int ticksInUse, SpellModifiers modifiers){
		return true;
	}
	
	@Override
	protected void spawnParticle(World world, double x, double y, double z, double vx, double vy, double vz){
		float brightness = world.rand.nextFloat();
		ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).vel(vx, vy, vz).time(8 + world.rand.nextInt(12))
		.clr(0.4f + 0.6f * brightness, 0.6f + 0.4f*brightness, 1).spawn(world);
		ParticleBuilder.create(Type.SNOW).pos(x, y, z).vel(vx, vy, vz).time(8 + world.rand.nextInt(12)).spawn(world);
	}

}
