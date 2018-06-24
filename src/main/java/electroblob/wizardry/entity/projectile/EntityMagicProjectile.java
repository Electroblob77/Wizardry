package electroblob.wizardry.entity.projectile;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

/**
 * This class is a generic superclass for all <b>non-directed</b> projectiles, namely: darkness orb, firebolt, firebomb,
 * force orb, ice charge, lightning disc, poison bomb, spark, spark bomb and thunderbolt. Directed (arrow-like)
 * projectiles should instead extend {@link EntityMagicArrow}.
 * <p>
 * This class purely handles saving of the damage multiplier; EntityThrowable is pretty well suited to my purposes as it
 * is. Range is done via the velocity when the constructor is called. Caster is already handled by
 * EntityThrowable.getThrower(), though due to a bug in vanilla it has to be synced by this class.
 * 
 * @since Wizardry 1.0
 * @author Electroblob
 * @see EntityBomb
 */
public abstract class EntityMagicProjectile extends EntityThrowable implements IEntityAdditionalSpawnData {

	public float damageMultiplier = 1.0f;

	/** Creates a new projectile in the given world. */
	public EntityMagicProjectile(World world){
		super(world);
	}

	// Initialiser methods
	
	/** Sets the shooter of the projectile to the given caster, positions the projctile at the given caster's eyes and 
	 * aims it in the direction they are looking with the given speed. */
	public void aim(EntityLivingBase caster, float speed){
		this.setPosition(caster.posX, caster.posY + (double)caster.getEyeHeight() - 0.1d, caster.posZ);
		// This is the standard set of parameters for this method, used by snowballs and ender pearls amongst others.
		this.shoot(caster, caster.rotationPitch, caster.rotationYaw, 0.0f, speed, 1.0f);
		this.thrower = caster;
		// Mojang's 'fix' for the projectile-hitting-thrower bug actually made the problem worse, hence the following line.
		this.ignoreEntity = caster;
	}

	/** Sets the shooter of the projectile to the given caster, positions the projctile at the given caster's eyes and 
	 * aims it at the given target with the given speed. The trajectory will be altered slightly by a random amount
	 * determined by the aimingError parameter. For reference, skeletons set this to 10 on easy, 6 on normal and 2 on hard
	 * difficulty. */
	public void aim(EntityLivingBase caster, Entity target, float speed, float aimingError){
		
		this.thrower = caster;
		// Mojang's 'fix' for the projectile-hitting-thrower bug actually made the problem worse, hence the following line.
		this.ignoreEntity = thrower;

		this.posY = caster.posY + (double)caster.getEyeHeight() - 0.1d;
		double dx = target.posX - caster.posX;
		double dy = !this.hasNoGravity() ? target.getEntityBoundingBox().minY + (double)(target.height / 3.0f) - this.posY
				: target.getEntityBoundingBox().minY + (double)(target.height / 2.0f) - this.posY;
		double dz = target.posZ - caster.posZ;
		double horizontalDistance = (double)MathHelper.sqrt(dx * dx + dz * dz);

		if(horizontalDistance >= 1.0E-7D){
			
			double dxNormalised = dx / horizontalDistance;
			double dzNormalised = dz / horizontalDistance;
			this.setPosition(caster.posX + dxNormalised, this.posY, caster.posZ + dzNormalised);

			// Depends on the horizontal distance between the two entities and accounts for bullet drop,
			// but of course if gravity is ignored this should be 0 since there is no bullet drop.
			float bulletDropCompensation = !this.hasNoGravity() ? (float)horizontalDistance * 0.2f : 0;
			// It turns out that this method normalises the input (x, y, z) anyway
			this.shoot(dx, dy + (double)bulletDropCompensation, dz, speed, aimingError);
		}
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
	// For now, we're only writing when the thrower exists, so subclasses MUST CALL SUPER LAST.
	// TODO: Figure out whether there's a default value we can write that is never used as an entity id (0? -1? +/-MAX_VALUE?)
	public void writeSpawnData(ByteBuf data){
		if(this.getThrower() != null) data.writeInt(this.getThrower().getEntityId());
	}

	@Override
	// For now, we're only writing when the thrower exists, so subclasses MUST CALL SUPER LAST.
	public void readSpawnData(ByteBuf data){
		if(data.isReadable()){
			Entity entity = this.world.getEntityByID(data.readInt());
			if(entity instanceof EntityLivingBase) this.thrower = (EntityLivingBase)entity;
			this.ignoreEntity = this.thrower;
		}
	}

}
