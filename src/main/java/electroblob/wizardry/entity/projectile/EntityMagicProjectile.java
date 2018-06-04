package electroblob.wizardry.entity.projectile;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

/**
 * This class is a generic superclass for all <b>non-directed</b> projectiles, namely: darkness orb, firebolt, firebomb,
 * force orb, ice charge, lightning disc, poison bomb, spark, spark bomb and thunderbolt. Directed (arrow-like)
 * projectiles should instead extend {@link EntityMagicArrow}.
 * <p>
 * This class purely handles saving of the damage multiplier; EntityThrowable is pretty well suited to my purposes as it
 * is. Range is done via the velocity when the constructor is called. Caster is already handled by
 * EntityThrowable.getThrower().
 * 
 * Note that this class does not implement {@link IEntityAdditionalSpawnData}; subclasses that need to transfer extra
 * data to the client should implement that interface themselves. See {@link EntityBomb} for an example.
 * 
 * @since Wizardry 1.0
 * @author Electroblob
 * @see EntityBomb
 */
public abstract class EntityMagicProjectile extends EntityThrowable implements IEntityAdditionalSpawnData {

	public float damageMultiplier = 1.0f;

	public EntityMagicProjectile(World world){
		super(world);
	}

	public EntityMagicProjectile(World world, EntityLivingBase thrower){
		super(world, thrower);
	}

	public EntityMagicProjectile(World world, EntityLivingBase thrower, float damageMultiplier){
		super(world, thrower);
		// This is the standard set of parameters for this method, used by snowballs and ender pearls amongst others.
		this.shoot(thrower, thrower.rotationPitch, thrower.rotationYaw, 0.0f, this.getSpeed(), 1.0f);
		this.damageMultiplier = damageMultiplier;
		// Mojang's 'fix' for the projectile-hitting-thrower bug actually made the problem worse, hence the following line.
		this.ignoreEntity = thrower;
	}

	public EntityMagicProjectile(World world, double x, double y, double z){
		super(world, x, y, z);
	}

	/** This got removed at some point since 1.7.10, but I liked it so I thought I'd add it back in again. */
	protected float getSpeed(){
		return 1.5f;
	}

	/** Sets this projectile's velocity as a normalised vector towards the target. */
	public void directTowards(Entity target, float velocity){

		double dx = target.posX - this.posX;
		double dy = target.getEntityBoundingBox().minY + (double)(target.height / 2.0F)
				- (this.posY + (double)(this.height / 2.0F));
		double dz = target.posZ - this.posZ;

		this.motionX = dx / this.getDistance(target) * velocity;
		this.motionY = dy / this.getDistance(target) * velocity;
		this.motionZ = dz / this.getDistance(target) * velocity;
	}
	
	@Override
	public void onUpdate(){
		// This fixes the client-side projectile-hitting-thrower bug. Comparing with 1.10.2, this was caused by a change
		// to the line EntityThrowable:215, where a thrower != null check was added. Since the thrower field is not synced,
		// this fails and the ignoreEntity field is never set, causing the projectile to hit its thrower client-side.
		// The 'proper' way to fix this is to use IEntityAdditionalSpawnData to sync the thrower field, but I don't really
		// want to waste packets like that, so, since things worked just fine in 1.10.2 without the thrower != null check,
		// it makes sense to just duplicate that block of code and remove the offending check.
		// The only side-effect (and probably why the change was made to vanilla) is that if this entity is summoned
		// inside a mob using commands, it wouldn't hit that mob. This is so minor that it's not worth sending a packet
		// for, though it may become more noticeable if spells firing from blocks are added.
		// TODO: Investigate whether this is still necessary in 1.12
//		if(this.world.isRemote){
//			
//			List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox().expand(this.motionX, this.motionY, this.motionZ).grow(1.0D));
//	        
//			for(Entity entity : list){ // Why does vanilla still not use a for-each loop?
//	            if(entity.canBeCollidedWith() && this.ticksExisted < 2 && this.ignoreEntity == null){
//	            	this.ignoreEntity = entity;
//	            }
//	        }
//			// Pretty sure EntityThrowable handles the rest.
//		}
		
		super.onUpdate();
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound){
		super.readEntityFromNBT(nbttagcompound);
		damageMultiplier = nbttagcompound.getFloat("damageMultiplier");
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound){
		super.writeEntityToNBT(nbttagcompound);
		nbttagcompound.setFloat("damageMultiplier", damageMultiplier);
	}

	@Override
	public void writeSpawnData(ByteBuf data){
		data.writeInt(this.getThrower().getEntityId());
	}

	@Override
	public void readSpawnData(ByteBuf data){
		Entity entity = this.world.getEntityByID(data.readInt());
		if(entity instanceof EntityLivingBase) this.thrower = (EntityLivingBase)entity;
		this.ignoreEntity = this.thrower;
	}

}
