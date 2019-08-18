package electroblob.wizardry.entity.living;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntityHuskMinion extends EntityZombieMinion {

	/** Creates a new husk minion in the given world. */
	public EntityHuskMinion(World world){
		super(world);
	}

	@Override
	protected boolean shouldBurnInDay(){
		return false;
	}

	@Override protected SoundEvent getAmbientSound(){ return SoundEvents.ENTITY_HUSK_AMBIENT; }
	@Override protected SoundEvent getHurtSound(DamageSource damageSourceIn){ return SoundEvents.ENTITY_HUSK_HURT; }
	@Override protected SoundEvent getDeathSound(){ return SoundEvents.ENTITY_HUSK_DEATH; }
	@Override protected SoundEvent getStepSound(){ return SoundEvents.ENTITY_HUSK_STEP; }

	@Override
	public boolean attackEntityAsMob(Entity target){

		boolean flag = super.attackEntityAsMob(target);

		if(flag && this.getHeldItemMainhand().isEmpty() && target instanceof EntityLivingBase){
			float f = this.world.getDifficultyForLocation(new BlockPos(this)).getAdditionalDifficulty();
			((EntityLivingBase)target).addPotionEffect(new PotionEffect(MobEffects.HUNGER, 140 * (int)f));
		}

		return flag;
	}
}