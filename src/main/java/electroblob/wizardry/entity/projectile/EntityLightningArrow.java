package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;

public class EntityLightningArrow extends EntityMagicArrow {

	/** Creates a new lightning arrow in the given world. */
	public EntityLightningArrow(World world){
		super(world);
	}

	@Override public double getDamage(){ return 7.0d; }

	@Override public DamageType getDamageType(){ return DamageType.SHOCK; }

	@Override public boolean doGravity(){ return false; }

	@Override public boolean doDeceleration(){ return false; }

	@Override
	public void onEntityHit(EntityLivingBase entityHit){

		if(world.isRemote){
			for(int j = 0; j < 8; j++){
				ParticleBuilder.create(Type.SPARK, rand, posX, posY + height / 2, posZ, 1, false).spawn(world);
			}
		}
		
		this.playSound(WizardrySounds.SPELL_SPARK, 1.0F, 1.0F);
	}

	@Override
	public void tickInAir(){

		if(this.ticksExisted > 20){
			this.setDead();
		}

		if(world.isRemote){
			ParticleBuilder.create(Type.SPARK).pos(posX, posY, posZ).spawn(world);
		}

	}

	@Override
	protected void entityInit(){}

}