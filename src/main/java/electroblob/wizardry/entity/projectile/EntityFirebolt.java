package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityFirebolt extends EntityMagicProjectile {
	
	public EntityFirebolt(World world){
		super(world);
	}

	@Override
	protected void onImpact(RayTraceResult rayTrace){
		
		Entity entityHit = rayTrace.entityHit;

		if(entityHit != null){

			float damage = Spells.firebolt.getProperty(Spell.DAMAGE).floatValue() * damageMultiplier;

			entityHit.attackEntityFrom(
					MagicDamage.causeIndirectMagicDamage(this, this.getThrower(), DamageType.FIRE).setProjectile(),
					damage);

			if(!MagicDamage.isEntityImmune(DamageType.FIRE, entityHit))
				entityHit.setFire(Spells.firebolt.getProperty(Spell.BURN_DURATION).intValue());
		}

		this.playSound(WizardrySounds.ENTITY_FIREBOLT_HIT, 2, 0.8f + rand.nextFloat() * 0.3f);

		// Particle effect
		if(world.isRemote){
			for(int i = 0; i < 8; i++){
				world.spawnParticle(EnumParticleTypes.LAVA, this.posX + rand.nextFloat() - 0.5,
						this.posY + this.height / 2 + rand.nextFloat() - 0.5, this.posZ + rand.nextFloat() - 0.5, 0, 0, 0);
			}
		}

		this.setDead();
	}

	@Override
	public void onUpdate(){

		super.onUpdate();

		if(world.isRemote){
			ParticleBuilder.create(ParticleBuilder.Type.MAGIC_FIRE, this).time(14).spawn(world);

			if(this.ticksExisted > 1){ // Don't spawn particles behind where it started!
				double x = posX - motionX/2 + rand.nextFloat() * 0.2 - 0.1;
				double y = posY + this.height/2 - motionY/2 + rand.nextFloat() * 0.2 - 0.1;
				double z = posZ - motionZ/2 + rand.nextFloat() * 0.2 - 0.1;
				ParticleBuilder.create(ParticleBuilder.Type.MAGIC_FIRE).pos(x, y, z).time(14).spawn(world);
			}
		}
	}

	@Override
	public int getLifetime(){
		return 6;
	}

	@Override
	public boolean hasNoGravity(){
		return true;
	}

	@Override
	public boolean canRenderOnFire(){
		return false;
	}
}
