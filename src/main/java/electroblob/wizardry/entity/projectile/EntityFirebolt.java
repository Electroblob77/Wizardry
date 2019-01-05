package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import net.minecraft.entity.Entity;
import net.minecraft.init.SoundEvents;
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
			float damage = 5 * damageMultiplier;

			entityHit.attackEntityFrom(
					MagicDamage.causeIndirectMagicDamage(this, this.getThrower(), DamageType.FIRE).setProjectile(),
					damage);

			if(!MagicDamage.isEntityImmune(DamageType.FIRE, entityHit)) entityHit.setFire(5);
		}

		this.playSound(SoundEvents.BLOCK_LAVA_POP, 2, 0.8f + rand.nextFloat() * 0.3f);

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
			for(int i = 0; i < 4; i++){
				world.spawnParticle(EnumParticleTypes.FLAME, this.posX + rand.nextFloat() * 0.2 - 0.1,
						this.posY + this.height / 2 + rand.nextFloat() * 0.2 - 0.1,
						this.posZ + rand.nextFloat() * 0.2 - 0.1, 0, 0, 0);
			}
		}

		if(this.ticksExisted > 8){
			this.setDead();
		}
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
