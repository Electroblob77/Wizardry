package electroblob.wizardry.entity.projectile;

import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public class EntityIceLance extends EntityMagicArrow {

	/** Creates a new ice lance in the given world. */
	public EntityIceLance(World world){
		super(world);
		this.setKnockbackStrength(1);
	}

	@Override public double getDamage(){ return 10.0d; }

	@Override public DamageType getDamageType(){ return DamageType.FROST; }

	@Override public boolean doGravity(){ return true; }

	@Override public boolean doDeceleration(){ return true; }

	@Override public boolean doOverpenetration(){ return true; }

	@Override public boolean canRenderOnFire(){ return false; }

	@Override
	public void onEntityHit(EntityLivingBase entityHit){

		// Adds a freeze effect to the target.
		if(!MagicDamage.isEntityImmune(DamageType.FROST, entityHit))
			entityHit.addPotionEffect(new PotionEffect(WizardryPotions.frost, 300, 0));

		this.playSound(SoundEvents.ENTITY_GENERIC_HURT, 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
	}

	@Override
	public void onBlockHit(){
		// Adds a particle effect when the ice lance hits a block.
		if(this.world.isRemote){
			for(int j = 0; j < 10; j++){
				ParticleBuilder.create(Type.ICE, this.rand, this.posX, this.posY, this.posZ, 0.5, true)
				.lifetime(20 + rand.nextInt(10)).spawn(world);
			}
		}
		// Parameters for sound: sound event name, volume, pitch.
		this.playSound(SoundEvents.ENTITY_SPLASH_POTION_BREAK, 1.0F, rand.nextFloat() * 0.4F + 1.2F);

	}

	@Override
	protected void entityInit(){}

}