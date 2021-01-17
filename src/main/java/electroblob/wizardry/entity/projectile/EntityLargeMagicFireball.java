package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * It's like {@link EntityMagicFireball}, but bigger... the wizardry version of vanilla's
 * {@link net.minecraft.entity.projectile.EntityLargeFireball}
 */
@Mod.EventBusSubscriber
public class EntityLargeMagicFireball extends EntityMagicFireball {

	public static final String EXPLOSION_POWER = "explosion_power";

	/** The entity blast multiplier. This is now synced and saved centrally from {@link EntityBomb}. */
	public float blastMultiplier = 1.0f;

	/** The explosion power of this fireball. If this is -1, the damage for the fireball
	 * spell will be used instead; this is for when the fireball is not from a spell (i.e. a vanilla fireball replacement). */
	protected float explosionPower = -1;

	public EntityLargeMagicFireball(World world){
		super(world);
		this.setSize(1, 1);
	}

	public void setExplosionPower(float explosionPower){
		this.explosionPower = explosionPower;
	}

	public float getExplosionPower(){
		return explosionPower == -1 ? Spells.greater_fireball.getProperty(EXPLOSION_POWER).floatValue() : explosionPower;
	}

	@Override
	public float getDamage(){
		return damage == -1 ? Spells.greater_fireball.getProperty(Spell.DAMAGE).floatValue() : damage;
	}

	@Override
	protected DamageSource getDamageSource(Entity entityHit){
		if(entityHit instanceof EntityGhast){
			return MagicDamage.causeIndirectMagicDamage(this, this.getThrower(), DamageType.MAGIC).setProjectile();
		}else{
			return super.getDamageSource(entityHit);
		}
	}

	@Override
	protected void onImpact(RayTraceResult rayTrace){

		if(!world.isRemote){
			boolean terrainDamage = EntityUtils.canDamageBlocks(this.getThrower(), world);
			this.world.newExplosion(null, this.posX, this.posY, this.posZ, getExplosionPower() * blastMultiplier, terrainDamage, terrainDamage);
		}

		super.onImpact(rayTrace);
	}

	@Override
	public void writeSpawnData(ByteBuf buffer){
		buffer.writeFloat(blastMultiplier);
		super.writeSpawnData(buffer);
	}

	@Override
	public void readSpawnData(ByteBuf buffer){
		blastMultiplier = buffer.readFloat();
		super.readSpawnData(buffer);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound){
		super.readEntityFromNBT(nbttagcompound);
		blastMultiplier = nbttagcompound.getFloat("blastMultiplier");
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound){
		super.writeEntityToNBT(nbttagcompound);
		nbttagcompound.setFloat("blastMultiplier", blastMultiplier);
	}

	@SubscribeEvent
	public static void onEntityJoinWorldEvent(EntityJoinWorldEvent event){
		// Replaces all vanilla large fireballs with wizardry ones
		if(Wizardry.settings.replaceVanillaFireballs && event.getEntity() instanceof EntityLargeFireball){

			event.setCanceled(true);

			EntityLargeMagicFireball fireball = new EntityLargeMagicFireball(event.getWorld());
			fireball.thrower = ((EntityLargeFireball)event.getEntity()).shootingEntity;
			fireball.setPosition(event.getEntity().posX, event.getEntity().posY, event.getEntity().posZ);
			fireball.setDamage(6);
			// Don't set the burn duration because vanilla large fireballs don't set mobs on fire directly
			fireball.setExplosionPower(((EntityLargeFireball)event.getEntity()).explosionPower);
			fireball.setLifetime(75);

			fireball.motionX = ((EntityLargeFireball)event.getEntity()).accelerationX * ACCELERATION_CONVERSION_FACTOR;
			fireball.motionY = ((EntityLargeFireball)event.getEntity()).accelerationY * ACCELERATION_CONVERSION_FACTOR;
			fireball.motionZ = ((EntityLargeFireball)event.getEntity()).accelerationZ * ACCELERATION_CONVERSION_FACTOR;

			event.getWorld().spawnEntity(fireball);
		}
	}
}
