package electroblob.wizardry.spell;

import electroblob.wizardry.block.BlockStatue;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class IceAge extends SpellAreaEffect {

	public static final String FREEZE_DURATION = "freeze_duration";

	public IceAge(){
		super("ice_age", SpellActions.POINT_DOWN, false);
		this.soundValues(1.5f, 1.0f, 0);
		this.alwaysSucceed(true);
		addProperties(FREEZE_DURATION, EFFECT_DURATION, EFFECT_STRENGTH);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){
		freezeNearbyBlocks(world, new Vec3d(caster.posX, caster.getEntityBoundingBox().minY, caster.posZ), caster, modifiers);
		return super.cast(world, caster, hand, ticksInUse, modifiers);
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target, SpellModifiers modifiers){
		freezeNearbyBlocks(world, caster.getPositionVector(), caster, modifiers);
		return super.cast(world, caster, hand, ticksInUse, target, modifiers);
	}

	@Override
	public boolean cast(World world, double x, double y, double z, EnumFacing direction, int ticksInUse, int duration, SpellModifiers modifiers){
		freezeNearbyBlocks(world, new Vec3d(x, y, z), null, modifiers);
		return super.cast(world, x, y, z, direction, ticksInUse, duration, modifiers);
	}

	@Override
	protected boolean affectEntity(World world, Vec3d origin, @Nullable EntityLivingBase caster, EntityLivingBase target, int targetCount, int ticksInUse, SpellModifiers modifiers){

		if(target instanceof EntityLiving){
			if(((BlockStatue)WizardryBlocks.ice_statue).convertToStatue((EntityLiving)target,
					(int)(getProperty(FREEZE_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)))){
				target.playSound(WizardrySounds.MISC_FREEZE, 1.0F, world.rand.nextFloat() * 0.4F + 0.8F);
			}
		}else if(target instanceof EntityPlayer){
			target.addPotionEffect(new PotionEffect(WizardryPotions.frost,
					(int)(getProperty(EFFECT_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)),
					getProperty(EFFECT_STRENGTH).intValue()));
		}

		return true;
	}

	@Override
	protected void spawnParticleEffect(World world, Vec3d origin, double radius, @Nullable EntityLivingBase caster, SpellModifiers modifiers){

		for(int i=0; i<100; i++){
			float r = world.rand.nextFloat();
			double speed = 0.02/r * (1 + world.rand.nextDouble());//(world.rand.nextBoolean() ? 1 : -1) * (0.05 + 0.02 * world.rand.nextDouble());
			ParticleBuilder.create(Type.SNOW)
					.pos(origin.x, origin.y + world.rand.nextDouble() * 3, origin.z)
					.vel(0, 0, 0)
					.scale(2)
					.spin(world.rand.nextDouble() * (radius - 0.5) + 0.5, speed)
					.shaded(true)
					.spawn(world);
		}

		for(int i=0; i<60; i++){
			float r = world.rand.nextFloat();
			double speed = 0.02/r * (1 + world.rand.nextDouble());//(world.rand.nextBoolean() ? 1 : -1) * (0.05 + 0.02 * world.rand.nextDouble());
			ParticleBuilder.create(Type.CLOUD)
					.pos(origin.x, origin.y + world.rand.nextDouble() * 2.5, origin.z)
					.clr(0xffffff)
					.spin(world.rand.nextDouble() * (radius - 1) + 0.5, speed)
					.shaded(true)
					.spawn(world);
		}
	}

	private void freezeNearbyBlocks(World world, Vec3d origin, @Nullable EntityLivingBase caster, SpellModifiers modifiers){

		if(!world.isRemote && EntityUtils.canDamageBlocks(caster, world)){

			double radius = getProperty(EFFECT_RADIUS).floatValue() * modifiers.get(WizardryItems.blast_upgrade);

			for(int i = -(int)radius; i <= (int)radius; i++){
				for(int j = -(int)radius; j <= (int)radius; j++){

					BlockPos pos = new BlockPos(origin).add(i, 0, j);

					Integer y = BlockUtils.getNearestSurface(world, new BlockPos(pos), EnumFacing.UP, (int)radius, true, BlockUtils.SurfaceCriteria.SOLID_LIQUID_TO_AIR);

					if(y != null){

						pos = new BlockPos(pos.getX(), y, pos.getZ());

						double dist = origin.distanceTo(new Vec3d(origin.x + i, y, origin.z + j));

						// Randomised with weighting so that the nearer the block the more likely it is to be snowed.
						if(y != -1 && world.rand.nextInt((int)(dist * 2) + 1) < radius && dist < radius){
							BlockUtils.freeze(world, pos.down(), true);
						}
					}
				}
			}
		}
	}

}
