package electroblob.wizardry.entity.construct;

import java.util.List;

import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class EntityBlizzard extends EntityMagicConstruct {

	public EntityBlizzard(World par1World){
		super(par1World);
		this.height = 1.0f;
		this.width = 1.0f;
	}

	public EntityBlizzard(World world, double x, double y, double z, EntityLivingBase caster, int lifetime,
			float damageMultiplier){
		super(world, x, y, z, caster, lifetime, damageMultiplier);
		this.height = 1.0f;
		this.width = 1.0f;
	}

	public void onUpdate(){

		if(this.ticksExisted % 120 == 1){
			this.playSound(WizardrySounds.SPELL_LOOP_WIND, 1.0f, 1.0f);
		}

		super.onUpdate();

		if(!this.world.isRemote){

			List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(3.0d, this.posX, this.posY,
					this.posZ, this.world);

			for(EntityLivingBase target : targets){

				if(this.isValidTarget(target)){

					if(this.getCaster() != null){
						WizardryUtilities.attackEntityWithoutKnockback(target,
								MagicDamage.causeIndirectMagicDamage(this, getCaster(), DamageType.FROST),
								1 * damageMultiplier);
					}else{
						WizardryUtilities.attackEntityWithoutKnockback(target, DamageSource.MAGIC,
								1 * damageMultiplier);
					}
				}

				// All entities are slowed, even the caster (except those immune to frost effects)
				if(!MagicDamage.isEntityImmune(DamageType.FROST, target))
					target.addPotionEffect(new PotionEffect(WizardryPotions.frost, 20, 0));
			}
		}else{
			for(int i=1; i<6; i++){
				
				float brightness = 0.5f + (rand.nextFloat() / 2);
				
				ParticleBuilder.create(Type.BLIZZARD)
				.pos(this.posX, this.posY + rand.nextDouble() * 3, this.posZ)
				.lifetime(100)
				.colour(brightness, brightness + 0.1f, 1.0f)
				.radius(rand.nextDouble() * 2.5d + 0.5d)
				.spawn(world);

				ParticleBuilder.create(Type.BLIZZARD)
				.pos(this.posX, this.posY + rand.nextDouble() * 3, this.posZ)
				.lifetime(100)
				.colour(1, 1, 1)
				.radius(rand.nextDouble() * 2.5d + 0.5d)
				.spawn(world);
			}
		}
	}
}
