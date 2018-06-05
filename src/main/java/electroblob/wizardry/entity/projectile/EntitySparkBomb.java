package electroblob.wizardry.entity.projectile;

import java.util.List;

import electroblob.wizardry.entity.EntityArc;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.WizardryUtilities;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntitySparkBomb extends EntityBomb {

	/** For client use, because thrower field is not visible. */
	private int casterID;

	public EntitySparkBomb(World par1World){
		super(par1World);
	}

	public EntitySparkBomb(World par1World, EntityLivingBase par2EntityLivingBase){
		super(par1World, par2EntityLivingBase);
	}

	public EntitySparkBomb(World par1World, EntityLivingBase par2EntityLivingBase, float damageMultiplier,
			float blastMultiplier){
		super(par1World, par2EntityLivingBase, damageMultiplier, blastMultiplier);
	}

	public EntitySparkBomb(World par1World, double par2, double par4, double par6){
		super(par1World, par2, par4, par6);
	}

	/**
	 * Called when this EntityThrowable hits a block or entity.
	 */
	protected void onImpact(RayTraceResult par1RayTraceResult){
		this.playSound(SoundEvents.ENTITY_FIREWORK_BLAST_FAR, 0.5f, 0.5f);

		Entity entityHit = par1RayTraceResult.entityHit;

		if(entityHit != null){
			// This is if the spark bomb gets a direct hit
			float damage = 6 * damageMultiplier;

			this.playSound(SoundEvents.ENTITY_GENERIC_HURT, 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));

			entityHit.attackEntityFrom(
					MagicDamage.causeIndirectMagicDamage(this, this.getThrower(), DamageType.SHOCK).setProjectile(),
					damage);

		}

		// Particle effect
		if(world.isRemote){
			for(int i = 0; i < 8; i++){
				double x = this.posX + rand.nextDouble() - 0.5;
				double y = this.posY + this.height / 2 + rand.nextDouble() - 0.5;
				double z = this.posZ + rand.nextDouble() - 0.5;
				ParticleBuilder.create(Type.SPARK).pos(x, y, z).spawn(world);
				world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX + rand.nextFloat() - 0.5,
						this.posY + this.height / 2 + rand.nextFloat() - 0.5, this.posZ + rand.nextFloat() - 0.5, 0, 0,
						0);
			}
		}

		double seekerRange = 5.0d * blastMultiplier;

		List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(seekerRange, this.posX, this.posY,
				this.posZ, this.world);

		for(int i = 0; i < Math.min(targets.size(), 4); i++){

			boolean flag = targets.get(i) != entityHit && targets.get(i) != this.getThrower()
					&& !(targets.get(i) instanceof EntityPlayer
							&& ((EntityPlayer)targets.get(i)).capabilities.isCreativeMode);

			// Detects (client side) if target is the thrower, to stop particles being spawned around them.
			if(flag && world.isRemote && targets.get(i).getEntityId() == this.casterID) flag = false;

			if(flag){

				EntityLivingBase target = targets.get(i);

				if(!this.world.isRemote){

					EntityArc arc = new EntityArc(this.world);
					arc.setEndpointCoords(this.posX, this.posY, this.posZ, target.posX, target.posY + target.height / 2,
							target.posZ);
					this.world.spawnEntity(arc);

					target.playSound(WizardrySounds.SPELL_SPARK, 1.0F, rand.nextFloat() * 0.4F + 1.5F);

					target.attackEntityFrom(
							MagicDamage.causeIndirectMagicDamage(this, this.getThrower(), DamageType.SHOCK),
							5.0f * damageMultiplier);

				}else{
					// Particle effect
					for(int j = 0; j < 8; j++){
						double x = target.posX + rand.nextFloat() - 0.5;
						double y = target.getEntityBoundingBox().minY + target.height * rand.nextFloat();
						double z = target.posZ + rand.nextFloat() - 0.5;
						ParticleBuilder.create(Type.SPARK).pos(x, y,  z).spawn(world);
						world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, target.posX + rand.nextFloat() - 0.5,
								target.getEntityBoundingBox().minY + target.height * rand.nextFloat(),
								target.posZ + rand.nextFloat() - 0.5, 0, 0, 0);
					}
				}
			}
		}

		this.setDead();
	}

	@Override
	public void writeSpawnData(ByteBuf data){
		super.writeSpawnData(data);
		data.writeInt(this.getThrower().getEntityId());
	}

	@Override
	public void readSpawnData(ByteBuf data){
		super.readSpawnData(data);
		this.casterID = data.readInt();
	}
}
