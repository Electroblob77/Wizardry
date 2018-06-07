package electroblob.wizardry.entity.construct;

import java.util.List;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.MagicDamage;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.MagicDamage.DamageType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class EntityFireSigil extends EntityMagicConstruct {

	public EntityFireSigil(World par1World) {
		super(par1World);
		this.height = 0.2f;
		this.width = 2.0f;
	}

	public EntityFireSigil(World par1World, double x, double y, double z, EntityLivingBase caster, float damageMultiplier) {
		super(par1World, x, y, z, caster, -1, damageMultiplier);
		this.height = 0.2f;
		this.width = 2.0f;
	}

	// Overrides the original to stop the entity moving when it intersects stuff. The default arrow does this to allow
	// it to stick in blocks.
	public void setPositionAndRotation2(double par1, double par3, double par5, float par7, float par8, int par9)
	{
		this.setPosition(par1, par3, par5);
		this.setRotation(par7, par8);
	}

	public void onUpdate(){

		super.onUpdate();

		if(!this.worldObj.isRemote){

			List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(1.0d, this.posX, this.posY, this.posZ, this.worldObj);

			for(EntityLivingBase target : targets){

				if(this.isValidTarget(target)){

					double velX = target.motionX;
					double velY = target.motionY;
					double velZ = target.motionZ;
					
					target.attackEntityFrom(this.getCaster() != null ? MagicDamage.causeIndirectEntityMagicDamage(this, this.getCaster(), DamageType.FIRE) : DamageSource.magic, 6);

					// Removes knockback
					target.motionX = velX;
					target.motionY = velY;
					target.motionZ = velZ;

					if(!MagicDamage.isEntityImmune(DamageType.FIRE, target)) target.setFire(10);
					
					worldObj.playAuxSFX(1009, (int)this.posX, (int)this.posY, (int)this.posZ, 0);
					
					// The trap is destroyed once triggered.
					this.setDead();
				}
			}
		}else if(this.rand.nextInt(15) == 0){
			double radius = 0.5 + rand.nextDouble()*0.3;
			double angle = rand.nextDouble()*Math.PI*2;
			worldObj.spawnParticle("flame", this.posX + radius*Math.cos(angle), this.posY + 0.1, this.posZ + radius*Math.sin(angle), 0, 0, 0);
		}
	}

	@Override
	protected void entityInit() {

	}

	/**
	 * Return whether this entity should be rendered as on fire.
	 */
	public boolean canRenderOnFire()
	{
		return false;
	}

}
