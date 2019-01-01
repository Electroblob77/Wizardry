package electroblob.wizardry.entity.construct;

import java.util.List;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntityHammer extends EntityMagicConstruct {

	/** How long the hammer has been falling for. */
	public int fallTime;

	public EntityHammer(World world){
		super(world);
		this.setSize(1.0f, 1.9F);
		this.motionX = 0.0D;
		this.motionY = 0.0D;
		this.motionZ = 0.0D;
		this.noClip = false;
	}

	@Override
	public boolean isBurning(){
		return false;
	}

	@Override
	public boolean canBeCollidedWith(){
		return true;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(){
		return this.getEntityBoundingBox();
	}

	@Override
	public void onUpdate(){

		super.onUpdate();

		if(this.ticksExisted % 20 == 1 && !this.onGround && world.isRemote){
			// Though this sound does repeat, it stops when it hits the ground.
			Wizardry.proxy.playMovingSound(this, WizardrySounds.SPELL_LOOP_LIGHTNING, 3.0f, 1.0f, false);
		}

		if(this.world.isRemote && this.ticksExisted % 3 == 0){
			ParticleBuilder.create(Type.SPARK)
			.pos(this.posX - 0.5d + rand.nextDouble(), this.posY + 2 * rand.nextDouble(), this.posZ - 0.5d + rand.nextDouble())
			.spawn(world);
		}

		if(!this.world.isRemote){

			this.prevPosX = this.posX;
			this.prevPosY = this.posY;
			this.prevPosZ = this.posZ;
			++this.fallTime;
			this.motionY -= 0.03999999910593033D;
			this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
			this.motionX *= 0.9800000190734863D;
			this.motionY *= 0.9800000190734863D;
			this.motionZ *= 0.9800000190734863D;

			if(this.onGround){

				this.motionX *= 0.699999988079071D;
				this.motionZ *= 0.699999988079071D;
				this.motionY *= -0.5D;

				if(this.ticksExisted % 40 == 0){

					double seekerRange = 10.0d;

					List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(seekerRange, this.posX,
							this.posY + 1, this.posZ, world);

					// For this spell there is no limit to the amount of secondary targets!
					for(EntityLivingBase target : targets){

						if(this.isValidTarget(target)){

							if(world.isRemote){
								
								ParticleBuilder.create(Type.LIGHTNING).pos(posX, posY + height - 0.1, posZ) .target(target).spawn(world);
								
								ParticleBuilder.spawnShockParticles(world, target.posX,
										target.getEntityBoundingBox().minY + target.height, target.posZ);
							}

							target.playSound(WizardrySounds.SPELL_SPARK, 1.0F, rand.nextFloat() * 0.4F + 1.5F);

							if(this.getCaster() != null){
								WizardryUtilities.attackEntityWithoutKnockback(target,
										MagicDamage.causeIndirectMagicDamage(this, getCaster(), DamageType.SHOCK),
										6 * damageMultiplier);
								WizardryUtilities.applyStandardKnockback(this, target);
							}else{
								target.attackEntityFrom(DamageSource.MAGIC, 6 * damageMultiplier);
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void despawn(){

		this.playSound(SoundEvents.ENTITY_GENERIC_EXPLODE, 1.0F, 1.0f);

		if(this.world.isRemote){
			this.world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX, this.posY, this.posZ, 0, 0, 0);
		}

		super.despawn();
	}

	@Override
	public void fall(float distance, float damageMultiplier){

		if(world.isRemote){
			for(int i = 0; i < 40; i++){
				double particleX = this.posX - 1.0d + 2 * rand.nextDouble();
				double particleZ = this.posZ - 1.0d + 2 * rand.nextDouble();
				// Roundabout way of getting a block instance for the block the hammer is standing on (if any).
				IBlockState block = world.getBlockState(new BlockPos(this.posX, this.posY - 2, this.posZ));

				if(block != null){
					world.spawnParticle(EnumParticleTypes.BLOCK_DUST, particleX, this.posY, particleZ,
							particleX - this.posX, 0, particleZ - this.posZ, Block.getStateId(block));
				}
			}
		}else{
			// Just to check the hammer has actually fallen from the sky, rather than the block under it being broken.
			if(this.fallDistance > 10){
				EntityLightningBolt entitylightning = new EntityLightningBolt(world, this.posX, this.posY, this.posZ,
						false);
				world.addWeatherEffect(entitylightning);
			}
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound){
		super.writeEntityToNBT(nbttagcompound);
		nbttagcompound.setByte("Time", (byte)this.fallTime);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound){
		super.readEntityFromNBT(nbttagcompound);
		this.fallTime = nbttagcompound.getByte("Time") & 255;
	}

	@Override
	public boolean isInRangeToRenderDist(double distance){
		return true;
	}
}
