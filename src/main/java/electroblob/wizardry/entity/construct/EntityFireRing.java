package electroblob.wizardry.entity.construct;

import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

import java.util.List;

public class EntityFireRing extends EntityMagicConstruct {

	// TODO: Implement blast modifiers

	public EntityFireRing(World world){
		super(world);
		this.height = 1.0f;
		this.width = 5.0f;
	}

	public void onUpdate(){

		if(this.ticksExisted % 40 == 1){
			this.playSound(WizardrySounds.ENTITY_FIRE_RING_AMBIENT, 4.0f, 0.7f);
		}

		super.onUpdate();

		if(!this.world.isRemote){

			List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(2.5d, this.posX, this.posY,
					this.posZ, this.world);

			for(EntityLivingBase target : targets){

				if(this.isValidTarget(target)){

					double velX = target.motionX;
					double velY = target.motionY;
					double velZ = target.motionZ;

					if(!MagicDamage.isEntityImmune(DamageType.FIRE, target)){

						target.setFire(Spells.ring_of_fire.getProperty(Spell.BURN_DURATION).intValue());

						float damage = Spells.ring_of_fire.getProperty(Spell.DAMAGE).floatValue() * damageMultiplier;

						if(this.getCaster() != null){
							target.attackEntityFrom(MagicDamage.causeIndirectMagicDamage(this, getCaster(),
									DamageType.FIRE), damage);
						}else{
							target.attackEntityFrom(DamageSource.MAGIC, damage);
						}
					}

					// Removes knockback
					target.motionX = velX;
					target.motionY = velY;
					target.motionZ = velZ;
				}
			}
		}
	}

}
