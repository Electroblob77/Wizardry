package electroblob.wizardry.entity.construct;

import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public class EntityIceSpike extends EntityMagicConstruct {

	public EntityIceSpike(World world){
		super(world);
		this.setSize(0.5f, 1.0f);
	}

	@Override
	public void onUpdate(){

		if(lifetime - this.ticksExisted < 15){
			this.motionY = -0.01 * (this.ticksExisted - (lifetime - 15));
		}else if(lifetime - this.ticksExisted < 25){
			this.motionY = 0;
		}else if(lifetime - this.ticksExisted < 28){
			this.motionY = 0.25;
		}

		this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);

		if(lifetime - this.ticksExisted == 30) this.playSound(WizardrySounds.SPELL_ICE, 1, 2);

		if(!this.world.isRemote){
			for(Object entity : this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox())){
				if(entity instanceof EntityLivingBase && this.isValidTarget((EntityLivingBase)entity)){
					// Potion effect only gets added if the damage succeeded.
					if(((EntityLivingBase)entity).attackEntityFrom(
							MagicDamage.causeDirectMagicDamage(this.getCaster(), DamageType.FROST),
							5 * this.damageMultiplier))
						((EntityLivingBase)entity).addPotionEffect(new PotionEffect(WizardryPotions.frost, 100, 0));
				}
			}
		}

		super.onUpdate();
	}

}
