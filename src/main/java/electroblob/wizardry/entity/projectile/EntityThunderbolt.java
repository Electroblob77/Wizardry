package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityThunderbolt extends EntityMagicProjectile {

	public EntityThunderbolt(World par1World){
		super(par1World);
	}

	public EntityThunderbolt(World par1World, EntityLivingBase par2EntityLivingBase){
		super(par1World, par2EntityLivingBase);
	}

	public EntityThunderbolt(World par1World, EntityLivingBase par2EntityLivingBase, float damageMultiplier){
		super(par1World, par2EntityLivingBase, damageMultiplier);
	}

	public EntityThunderbolt(World par1World, double par2, double par4, double par6){
		super(par1World, par2, par4, par6);
	}

	/** This is the speed */
	protected float getSpeed(){
		return 2.5F;
	}

	/**
	 * Called when this EntityThrowable hits a block or entity.
	 */
	protected void onImpact(RayTraceResult par1RayTraceResult){
		
		Entity entityHit = par1RayTraceResult.entityHit;

		if(entityHit != null){

			float damage = 3 * damageMultiplier;

			entityHit.attackEntityFrom(
					MagicDamage.causeIndirectMagicDamage(this, this.getThrower(), DamageType.SHOCK).setProjectile(),
					damage);

			// Knockback
			entityHit.addVelocity(this.motionX * 0.2, this.motionY * 0.2, this.motionZ * 0.2);
		}

		this.playSound(SoundEvents.ENTITY_FIREWORK_LARGE_BLAST, 1.4F, 0.5f + this.rand.nextFloat() * 0.1F);

		// Particle effect
		if(world.isRemote){
			world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX, this.posY, this.posZ, 0, 0, 0);
		}

		this.setDead();
	}

	public void onUpdate(){

		super.onUpdate();

		if(world.isRemote){
			ParticleBuilder.create(Type.SPARK, rand, posX, posY + height/2, posZ, 0.1, false).spawn(world);
			for(int i = 0; i < 4; i++){
				world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX + rand.nextFloat() * 0.2 - 0.1,
						this.posY + this.height / 2 + rand.nextFloat() * 0.2 - 0.1,
						this.posZ + rand.nextFloat() * 0.2 - 0.1, 0, 0, 0);
			}
		}

		if(this.ticksExisted > 8){
			this.setDead();
		}
	}

	/**
	 * Gets the amount of gravity to apply to the thrown entity with each tick.
	 */
	protected float getGravityVelocity(){
		return 0.0F;
	}

	/**
	 * Return whether this entity should be rendered as on fire.
	 */
	public boolean canRenderOnFire(){
		return false;
	}
}
