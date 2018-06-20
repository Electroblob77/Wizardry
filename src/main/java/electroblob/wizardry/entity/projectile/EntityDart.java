package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public class EntityDart extends EntityMagicArrow {
	
	/** Creates a new dart in the given world. */
	public EntityDart(World world){
		super(world);
	}

	@Override public double getDamage(){ return 4.0d; }

	@Override public boolean doGravity(){ return true; }

	@Override public boolean doDeceleration(){ return true; }

	@Override
	public void onEntityHit(EntityLivingBase entityHit){
		// Adds a weakness effect to the target.
		entityHit.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 200, 1, false, false));
		this.playSound(SoundEvents.ENTITY_GENERIC_HURT, 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
	}

	@Override
	public void onBlockHit(){
		this.playSound(SoundEvents.ENTITY_ARROW_HIT, 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
	}

	@Override
	public void tickInAir(){
		if(this.world.isRemote){
			ParticleBuilder.create(Type.LEAF).pos(this.posX, this.posY, this.posZ).lifetime(10 + rand.nextInt(5));
		}
	}

	// Replicates the original behaviour of staying stuck in block for a few seconds before disappearing.
	@Override
	public void tickInGround(){
		if(this.ticksInGround > 60){
			this.setDead();
		}
	}

	@Override
	protected void entityInit(){}

}