package electroblob.wizardry.spell;

import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.util.*;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class LifeDrain extends SpellRay {

	public static final String HEAL_FACTOR = "heal_factor";

	public LifeDrain(){
		super("life_drain", true, SpellActions.POINT);
		this.particleVelocity(-0.5);
		this.particleSpacing(0.4);
		addProperties(DAMAGE, HEAL_FACTOR);
		this.soundValues(0.6f, 1, 0);
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
		
		if(EntityUtils.isLiving(target)){

			if(ticksInUse % 12 == 0){
				
				float damage = getProperty(DAMAGE).floatValue() * modifiers.get(SpellModifiers.POTENCY);
				
				EntityUtils.attackEntityWithoutKnockback(target, MagicDamage.causeDirectMagicDamage(caster,
						DamageType.MAGIC), damage);
				
				if(caster != null) caster.heal(damage * getProperty(HEAL_FACTOR).floatValue());
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
		return true;
	}
	
	@Override
	protected void spawnParticle(World world, double x, double y, double z, double vx, double vy, double vz){
		if(world.rand.nextInt(5) == 0) ParticleBuilder.create(Type.DARK_MAGIC).pos(x, y, z).clr(0.1f, 0, 0).spawn(world);
		// This used to multiply the velocity by the distance from the caster
		ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).vel(vx, vy, vz).time(8 + world.rand.nextInt(6))
		.clr(0.5f, 0, 0).spawn(world);
	}

}
