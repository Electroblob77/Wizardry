package electroblob.wizardry.entity.living;

import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;

public class EntityStrayMinion extends EntitySkeletonMinion {

	/** Creates a new stray minion in the given world. */
	public EntityStrayMinion(World world){
		super(world);
	}

	@Override protected SoundEvent getAmbientSound(){ return SoundEvents.ENTITY_STRAY_AMBIENT; }
	@Override protected SoundEvent getHurtSound(DamageSource source){ return SoundEvents.ENTITY_STRAY_HURT; }
	@Override protected SoundEvent getDeathSound(){ return SoundEvents.ENTITY_STRAY_DEATH; }
	@Override protected SoundEvent getStepSound(){ return SoundEvents.ENTITY_STRAY_STEP; }

	@Override
	protected EntityArrow getArrow(float distanceFactor){

		EntityArrow entityarrow = super.getArrow(distanceFactor);

		if(entityarrow instanceof EntityTippedArrow){
			((EntityTippedArrow)entityarrow).addEffect(new PotionEffect(MobEffects.SLOWNESS, 600));
		}

		return entityarrow;
	}
}
