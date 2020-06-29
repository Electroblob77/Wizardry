package electroblob.wizardry.spell;

import electroblob.wizardry.client.DrawingUtils;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.BlockUtils.SurfaceCriteria;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class Firestorm extends SpellAreaEffect {

	public Firestorm(){
		super("firestorm", SpellActions.POINT_DOWN, false);
		this.soundValues(2f, 1.0f, 0);
		this.alwaysSucceed(true);
		addProperties(BURN_DURATION);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){
		burnNearbyBlocks(world, new Vec3d(caster.posX, caster.getEntityBoundingBox().minY, caster.posZ), caster, modifiers);
		return super.cast(world, caster, hand, ticksInUse, modifiers);
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target, SpellModifiers modifiers){
		burnNearbyBlocks(world, caster.getPositionVector(), caster, modifiers);
		return super.cast(world, caster, hand, ticksInUse, target, modifiers);
	}

	@Override
	public boolean cast(World world, double x, double y, double z, EnumFacing direction, int ticksInUse, int duration, SpellModifiers modifiers){
		burnNearbyBlocks(world, new Vec3d(x, y, z), null, modifiers);
		return super.cast(world, x, y, z, direction, ticksInUse, duration, modifiers);
	}

	@Override
	protected boolean affectEntity(World world, Vec3d origin, @Nullable EntityLivingBase caster, EntityLivingBase target, int targetCount, int ticksInUse, SpellModifiers modifiers){
		target.setFire(getProperty(BURN_DURATION).intValue());
		return true;
	}

	@Override
	protected void spawnParticleEffect(World world, Vec3d origin, double radius, @Nullable EntityLivingBase caster, SpellModifiers modifiers){

		for(int i=0; i<100; i++){
			float r = world.rand.nextFloat();
			double speed = 0.02/r * (1 + world.rand.nextDouble());//(world.rand.nextBoolean() ? 1 : -1) * (0.05 + 0.02 * world.rand.nextDouble());
			ParticleBuilder.create(Type.MAGIC_FIRE)
					.pos(origin.x, origin.y + world.rand.nextDouble() * 3, origin.z)
					.vel(0, 0, 0)
					.scale(2)
					.time(40 + world.rand.nextInt(10))
					.spin(world.rand.nextDouble() * (radius - 0.5) + 0.5, speed)
					.spawn(world);
		}

		for(int i=0; i<60; i++){
			float r = world.rand.nextFloat();
			double speed = 0.02/r * (1 + world.rand.nextDouble());//(world.rand.nextBoolean() ? 1 : -1) * (0.05 + 0.02 * world.rand.nextDouble());
			ParticleBuilder.create(Type.CLOUD)
					.pos(origin.x, origin.y + world.rand.nextDouble() * 2.5, origin.z)
					.clr(DrawingUtils.mix(DrawingUtils.mix(0xffbe00, 0xff3600, r/0.6f), 0x222222, (r - 0.6f)/0.4f))
					.spin(r * (radius - 1) + 0.5, speed)
					.spawn(world);
		}
	}

	private void burnNearbyBlocks(World world, Vec3d origin, @Nullable EntityLivingBase caster, SpellModifiers modifiers){

		if(!world.isRemote && EntityUtils.canDamageBlocks(caster, world)){

			double radius = getProperty(EFFECT_RADIUS).floatValue() * modifiers.get(WizardryItems.blast_upgrade);

			for(int i = -(int)radius; i <= (int)radius; i++){
				for(int j = -(int)radius; j <= (int)radius; j++){

					BlockPos pos = new BlockPos(origin).add(i, 0, j);

					Integer y = BlockUtils.getNearestSurface(world, new BlockPos(pos), EnumFacing.UP, (int)radius, true, SurfaceCriteria.NOT_AIR_TO_AIR);

					if(y != null){

						pos = new BlockPos(pos.getX(), y, pos.getZ());

						double dist = origin.distanceTo(new Vec3d(origin.x + i, y, origin.z + j));

						// Randomised with weighting so that the nearer the block the more likely it is to be set alight.
						if(y != -1 && world.rand.nextInt((int)(dist * 2) + 1) < radius && dist < radius && dist > 1.5){
							world.setBlockState(pos, Blocks.FIRE.getDefaultState());
						}
					}
				}
			}
		}
	}

}
