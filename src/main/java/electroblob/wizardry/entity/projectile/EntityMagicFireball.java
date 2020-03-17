package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.WizardryUtilities;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * It's a fireball - but unlike vanilla fireballs, it actually looks like a fireball, and isn't completely useless for
 * attacking things (acceleration from stationary? Really, Mojang? No wonder I had so many blaze rods back in the day...)
 */
@Mod.EventBusSubscriber
public class EntityMagicFireball extends EntityMagicProjectile {

	protected static final int ACCELERATION_CONVERSION_FACTOR = 10;

	/** The damage dealt by this fireball. If this is -1, the damage for the fireball spell will be used instead;
	 * this is for when the fireball is not from a spell (i.e. a vanilla fireball replacement). */
	protected float damage = -1;
	/** The number of seconds entities are set on fire by this fireball. If this is -1, the damage for the fireball
	 * spell will be used instead; this is for when the fireball is not from a spell (i.e. a vanilla fireball replacement). */
	protected int burnDuration = -1;
	/** The lifetime of this fireball in ticks. This needs to be stored so that it can be changed for vanilla replacements,
	 * or mobs that shoot fireballs would have severely reduced range! */
	protected int lifetime = 16;

	public EntityMagicFireball(World world){
		super(world);
		this.setSize(0.5f, 0.5f);
	}

	public void setDamage(float damage){
		this.damage = damage;
	}

	public void setBurnDuration(int burnDuration){
		this.burnDuration = burnDuration;
	}

	public float getDamage(){
		// I'm lazy, I'd rather not have an entire fireball spell class just to set two fields on the entity
		return damage == -1 ? Spells.fireball.getProperty(Spell.DAMAGE).floatValue() : damage;
	}

	public int getBurnDuration(){
		return burnDuration == -1 ? Spells.fireball.getProperty(Spell.BURN_DURATION).intValue() : burnDuration;
	}

	@Override
	protected void onImpact(RayTraceResult rayTrace){

		if(!world.isRemote){

			Entity entityHit = rayTrace.entityHit;

			if(entityHit != null){

				float damage = getDamage() * damageMultiplier;

				entityHit.attackEntityFrom(
						MagicDamage.causeIndirectMagicDamage(this, this.getThrower(), DamageType.FIRE).setProjectile(),
						damage);

				if(!MagicDamage.isEntityImmune(DamageType.FIRE, entityHit) && getBurnDuration() > 0)
					entityHit.setFire(getBurnDuration());

			}else{

				if(this.getThrower() == null || WizardryUtilities.canDamageBlocks(this.getThrower(), world)){

					BlockPos blockpos = rayTrace.getBlockPos().offset(rayTrace.sideHit);

					if(this.world.isAirBlock(blockpos)){
						this.world.setBlockState(blockpos, Blocks.FIRE.getDefaultState());
					}
				}
			}

			//this.playSound(WizardrySounds.ENTITY_MAGIC_FIREBALL_HIT, 2, 0.8f + rand.nextFloat() * 0.3f);

			this.setDead();
		}
	}

	@Override
	public void onUpdate(){

		super.onUpdate();

		if(world.isRemote){

			for(int i=0; i<5; i++){

				double dx = (rand.nextDouble() - 0.5) * width;
				double dy = (rand.nextDouble() - 0.5) * height + this.height/2 - 0.1; // -0.1 because flames aren't centred
				double dz = (rand.nextDouble() - 0.5) * width;
				double v = 0.06;
				ParticleBuilder.create(ParticleBuilder.Type.MAGIC_FIRE)
						.pos(this.getPositionVector().add(dx - this.motionX/2, dy, dz - this.motionZ/2))
						.vel(-v * dx, -v * dy, -v * dz).scale(width*2).time(10).spawn(world);

				if(ticksExisted > 1){
					dx = (rand.nextDouble() - 0.5) * width;
					dy = (rand.nextDouble() - 0.5) * height + this.height / 2 - 0.1;
					dz = (rand.nextDouble() - 0.5) * width;
					ParticleBuilder.create(ParticleBuilder.Type.MAGIC_FIRE)
							.pos(this.getPositionVector().add(dx - this.motionX, dy, dz - this.motionZ))
							.vel(-v * dx, -v * dy, -v * dz).scale(width*2).time(10).spawn(world);
				}
			}
		}
	}

	@Override
	public boolean canBeCollidedWith(){
		return true;
	}

	@Override
	public float getCollisionBorderSize(){
		return 1.0F;
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float amount){

		if(this.isEntityInvulnerable(source)){
			return false;

		}else{

			this.markVelocityChanged();

			if(source.getTrueSource() != null){

				Vec3d vec3d = source.getTrueSource().getLookVec();

				if(vec3d != null){

					double speed = MathHelper.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);

					this.motionX = vec3d.x * speed;
					this.motionY = vec3d.y * speed;
					this.motionZ = vec3d.z * speed;

					this.lifetime = 160;

				}

				if(source.getTrueSource() instanceof EntityLivingBase){
					this.setCaster((EntityLivingBase)source.getTrueSource());
				}

				return true;

			}else{
				return false;
			}
		}
	}

	public void setLifetime(int lifetime){
		this.lifetime = lifetime;
	}

	@Override
	public int getLifetime(){
		return lifetime;
	}

	@Override
	public boolean hasNoGravity(){
		return true;
	}

	@Override
	public boolean canRenderOnFire(){
		return false;
	}

	@Override
	public void writeSpawnData(ByteBuf buffer){
		buffer.writeInt(lifetime);
		super.writeSpawnData(buffer);
	}

	@Override
	public void readSpawnData(ByteBuf buffer){
		lifetime = buffer.readInt();
		super.readSpawnData(buffer);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound){
		super.readEntityFromNBT(nbttagcompound);
		lifetime = nbttagcompound.getInteger("lifetime");
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound){
		super.writeEntityToNBT(nbttagcompound);
		nbttagcompound.setInteger("lifetime", lifetime);
	}

	@SubscribeEvent
	public static void onEntityJoinWorldEvent(EntityJoinWorldEvent event){
		// Replaces all vanilla fireballs with wizardry ones
		if(Wizardry.settings.replaceVanillaFireballs && event.getEntity() instanceof EntitySmallFireball){

			event.setCanceled(true);

			EntityMagicFireball fireball = new EntityMagicFireball(event.getWorld());
			fireball.thrower = ((EntitySmallFireball)event.getEntity()).shootingEntity;
			fireball.setPosition(event.getEntity().posX, event.getEntity().posY, event.getEntity().posZ);
			fireball.setDamage(5);
			fireball.setBurnDuration(5);
			fireball.setLifetime(40);

			fireball.motionX = ((EntitySmallFireball)event.getEntity()).accelerationX * ACCELERATION_CONVERSION_FACTOR;
			fireball.motionY = ((EntitySmallFireball)event.getEntity()).accelerationY * ACCELERATION_CONVERSION_FACTOR;
			fireball.motionZ = ((EntitySmallFireball)event.getEntity()).accelerationZ * ACCELERATION_CONVERSION_FACTOR;

			event.getWorld().spawnEntity(fireball);
		}
	}
}
