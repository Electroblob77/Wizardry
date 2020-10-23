package electroblob.wizardry.spell;

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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

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
	protected boolean onEntityHit(World world, Entity target, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){

		if(side == EnumFacing.UP && world.getBlockState(pos).isSideSolid(world, pos, EnumFacing.UP)
				&& BlockUtils.canBlockBeReplaced(world, pos.up())){

			int duration = (int)(getProperty(DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade));

			if(!world.isRemote && BlockUtils.canPlaceBlock(caster, world, pos.up())){
				world.setBlockState(pos.up(), WizardryBlocks.permafrost.getDefaultState());
				world.scheduleUpdate(pos.up().toImmutable(), WizardryBlocks.permafrost, duration);
			}

			return true;
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
