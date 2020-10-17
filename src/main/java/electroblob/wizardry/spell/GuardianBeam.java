package electroblob.wizardry.spell;

import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.util.*;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class GuardianBeam extends SpellRay {

	public static final String AIR_DEPLETION = "air_depletion";

	public GuardianBeam(){
		super("guardian_beam", SpellActions.POINT, true);
		addProperties(DAMAGE, AIR_DEPLETION);
	}

	@Override
	protected void playSound(World world, double x, double y, double z, int ticksInUse, int duration, SpellModifiers modifiers, String... sounds){
		if(ticksInUse % 50 == 1) super.playSound(world, x, y, z, ticksInUse, duration, modifiers, sounds);
	}

	@Override
	protected boolean onEntityHit(World world, Entity target, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){

		if(EntityUtils.isLiving(target)){

			if(ticksInUse % 50 == 1){

				EntityUtils.attackEntityWithoutKnockback(target,
						MagicDamage.causeDirectMagicDamage(caster, DamageType.MAGIC),
						getProperty(DAMAGE).floatValue() * modifiers.get(SpellModifiers.POTENCY));

				if(!((EntityLivingBase)target).canBreatheUnderwater() && !((EntityLivingBase)target).isPotionActive(MobEffects.WATER_BREATHING)){
					target.setAir(Math.max(-20, target.getAir() - getProperty(AIR_DEPLETION).intValue()));
				}
			}
			
			if(world.isRemote){

				float t = (ticksInUse % 50) / 50f;
				float yellowness = t * t;
				int r = 64 + (int)(yellowness * 191.0F);
				int g = 32 + (int)(yellowness * 191.0F);
				int b = 128 - (int)(yellowness * 64.0F);

				if(ticksInUse % 3 == 0) ParticleBuilder.create(Type.GUARDIAN_BEAM).entity(caster)
				.pos(caster != null ? origin.subtract(caster.getPositionVector()) : origin).target(target)
				.clr(r, g, b).spawn(world);

				Vec3d direction = GeometryUtils.getCentre(target).subtract(origin);
				Vec3d pos = origin.add(direction.scale(world.rand.nextFloat()));
				ParticleBuilder.create(Type.MAGIC_BUBBLE, world.rand, pos.x, pos.y, pos.z, 0.15, false).spawn(world);
			}
		}

		return true;
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onMiss(World world, EntityLivingBase caster, Vec3d origin, Vec3d direction, int ticksInUse, SpellModifiers modifiers){
		return false; // Only works on hit
	}

	@Override
	protected void spawnParticle(World world, double x, double y, double z, double vx, double vy, double vz){
	}
}
