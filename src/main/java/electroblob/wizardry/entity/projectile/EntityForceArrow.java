package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.util.MagicDamage.DamageType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.world.World;

public class EntityForceArrow extends EntityMagicArrow {

	/** Creates a new force arrow in the given world. */
	public EntityForceArrow(World world){
		super(world);
	}

	@Override
	public void onEntityHit(EntityLivingBase entityHit){
		this.playSound(SoundEvents.ENTITY_FIREWORK_BLAST, 1.0F, 1.0F);
	}

	@Override
	public void tickInGround(){
		this.setDead();
	}

	@Override
	public void onBlockHit(){
		this.playSound(SoundEvents.ENTITY_FIREWORK_BLAST, 1.0F, 1.0F);
	}

	@Override
	public void tickInAir(){
		if(this.ticksExisted > 20){
			this.setDead();
		}
	}

	@Override
	public double getDamage(){
		return 7.0d;
	}

	@Override
	public DamageType getDamageType(){
		return DamageType.FORCE;
	}

	@Override
	public boolean doGravity(){
		return false;
	}

	@Override
	public boolean doDeceleration(){
		return false;
	}

	@Override
	protected void entityInit(){
		// auto generated
	}

}