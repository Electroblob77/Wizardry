package electroblob.wizardry.entity.projectile;

import java.util.List;

import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntitySmokeBomb extends EntityBomb {

	public EntitySmokeBomb(World par1World){
		super(par1World);
	}

	public EntitySmokeBomb(World par1World, EntityLivingBase par2EntityLivingBase){
		super(par1World, par2EntityLivingBase);
	}

	public EntitySmokeBomb(World par1World, EntityLivingBase par2EntityLivingBase, float damageMultiplier,
			float blastMultiplier){
		super(par1World, par2EntityLivingBase, damageMultiplier, blastMultiplier);
	}

	public EntitySmokeBomb(World par1World, double par2, double par4, double par6){
		super(par1World, par2, par4, par6);
	}

	@Override
	protected void onImpact(RayTraceResult par1RayTraceResult){

		// Particle effect
		if(world.isRemote){
			this.world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX, this.posY, this.posZ, 0, 0, 0);
			for(int i = 0; i < 60 * blastMultiplier; i++){
				
				this.world.spawnParticle(EnumParticleTypes.SMOKE_LARGE,
						this.posX + (this.rand.nextDouble() * 4 - 2) * blastMultiplier,
						this.posY + (this.rand.nextDouble() * 4 - 2) * blastMultiplier,
						this.posZ + (this.rand.nextDouble() * 4 - 2) * blastMultiplier, 0, 0, 0);
				
				float brightness = rand.nextFloat() * 0.3f;
				ParticleBuilder.create(Type.DARK_MAGIC, rand, posX, posY, posZ, 2*blastMultiplier, false)
				.colour(brightness, brightness, brightness).spawn(world);
			}
		}

		if(!this.world.isRemote){

			this.playSound(SoundEvents.ENTITY_SPLASH_POTION_BREAK, 1.5F, rand.nextFloat() * 0.4F + 0.6F);
			this.playSound(SoundEvents.BLOCK_FIRE_EXTINGUISH, 1.2F, 1.0f);

			double range = 3.0d * blastMultiplier;

			List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(range, this.posX, this.posY,
					this.posZ, this.world);

			for(EntityLivingBase target : targets){
				if(target != this.getThrower()){
					// Gives the target blindness if it is a player, mind trick otherwise (since this has the desired
					// effect of preventing targeting)
					if(target instanceof EntityPlayer){
						target.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 120, 0));
					}else if(target instanceof EntityLiving){
						// New AI
						((EntityLiving)target).setAttackTarget(null);

						target.addPotionEffect(new PotionEffect(WizardryPotions.mind_trick, 120, 0));
					}
				}
			}

			this.setDead();
		}
	}
}
