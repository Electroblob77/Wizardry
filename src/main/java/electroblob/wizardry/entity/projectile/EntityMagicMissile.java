package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.world.World;

public class EntityMagicMissile extends EntityMagicArrow {

	/** Creates a new magic missile in the given world. */
	public EntityMagicMissile(World world){
		super(world);
	}

	@Override public double getDamage(){ return 4.0d; }

	@Override public boolean doGravity(){ return false; }

	@Override public boolean doDeceleration(){ return false; }

	@Override
	public void onEntityHit(EntityLivingBase entityHit){
		this.playSound(SoundEvents.ENTITY_GENERIC_HURT, 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
		if(this.world.isRemote) spawnImpactParticles();
	}
	
	@Override
	public void onBlockHit(){
		if(this.world.isRemote) spawnImpactParticles();
	}
	
	private void spawnImpactParticles(){
		ParticleBuilder.create(Type.FLASH).pos(posX, posY, posZ).colour(0.5f + rand.nextFloat()/2, 0.5f + rand.nextFloat()/2,
					0.5f + rand.nextFloat()/2).spawn(world);
	}

	@Override
	public void tickInAir(){

		if(this.ticksExisted > 20){
			this.setDead();
		}

		if(this.world.isRemote){
			ParticleBuilder.create(Type.SPARKLE).pos(this.posX, this.posY, this.posZ).lifetime(20 + rand.nextInt(10))
			.colour(0.5f + (rand.nextFloat() / 2), 0.5f + (rand.nextFloat() / 2), 0.5f + (rand.nextFloat() / 2)).spawn(world);
		}
	}

	@Override
	protected void entityInit(){ }

}