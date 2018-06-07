package electroblob.wizardry.entity.construct;

import java.util.List;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.MagicDamage;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.MagicDamage.DamageType;
import electroblob.wizardry.entity.EntityArc;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class EntityLightningSigil extends EntityMagicConstruct {
	
	public EntityLightningSigil(World par1World) {
		super(par1World);
		this.height = 0.2f;
		this.width = 2.0f;
	}
	
	public EntityLightningSigil(World par1World, double x, double y, double z, EntityLivingBase caster, float damageMultiplier) {
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
		
		if(this.ticksExisted > 600 && this.getCaster() == null && !this.worldObj.isRemote){
			this.setDead();
		}
		
		//if(!this.worldObj.isRemote){
			
			List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(1.0d, this.posX, this.posY, this.posZ, this.worldObj);
			
			for(EntityLivingBase target : targets){
				
				if(this.isValidTarget(target)){
					
					double velX = target.motionX;
					double velY = target.motionY;
					double velZ = target.motionZ;
					
					// Only works if target is actually damaged to account for hurtResistantTime
					if(target.attackEntityFrom(getCaster() != null ? MagicDamage.causeIndirectEntityMagicDamage(this, getCaster(), DamageType.SHOCK) : DamageSource.magic, 6)){
						
						// Removes knockback
						target.motionX = velX;
						target.motionY = velY;
						target.motionZ = velZ;
						
						this.playSound("wizardry:arc", 1.0f, 1.0f);
						
						// Secondary chaining effect
						double seekerRange = 5.0d;

						List<EntityLivingBase> secondaryTargets = WizardryUtilities.getEntitiesWithinRadius(seekerRange, target.posX, target.posY + target.height/2, target.posZ, worldObj);

						for(int j=0;j<Math.min(secondaryTargets.size(), 3);j++){
							
							EntityLivingBase secondaryTarget = secondaryTargets.get(j);

							if(secondaryTarget != target && this.isValidTarget(secondaryTarget)){
								
								if(!worldObj.isRemote){
									EntityArc arc = new EntityArc(worldObj);
									arc.setEndpointCoords(target.posX, target.posY + target.height/2, target.posZ,
											secondaryTarget.posX, secondaryTarget.posY + secondaryTarget.height/2, secondaryTarget.posZ);
									worldObj.spawnEntityInWorld(arc);
								}else{
									for(int k=0;k<8;k++){
										Wizardry.proxy.spawnParticle(EnumParticleType.SPARK, worldObj, secondaryTarget.posX + worldObj.rand.nextFloat() - 0.5, WizardryUtilities.getEntityFeetPos(secondaryTarget) + secondaryTarget.height/2 + worldObj.rand.nextFloat()*2 - 1, secondaryTarget.posZ + worldObj.rand.nextFloat() - 0.5, 0, 0, 0, 3);
										worldObj.spawnParticle("largesmoke", secondaryTarget.posX + worldObj.rand.nextFloat() - 0.5, WizardryUtilities.getEntityFeetPos(secondaryTarget) + secondaryTarget.height/2 + worldObj.rand.nextFloat()*2 - 1, secondaryTarget.posZ + worldObj.rand.nextFloat() - 0.5, 0, 0, 0);
									}
								}
								
								worldObj.playSoundAtEntity(secondaryTarget, "wizardry:arc", 1.0F, worldObj.rand.nextFloat() * 0.4F + 1.5F);
								
								secondaryTarget.attackEntityFrom(MagicDamage.causeIndirectEntityMagicDamage(this, getCaster(), DamageType.SHOCK), 4);
							}
						
						}
						// The trap is destroyed once triggered.
						this.setDead();
					}
				}
			}
		//}
		
		if(this.worldObj.isRemote && this.rand.nextInt(15) == 0){
			double radius = 0.5 + rand.nextDouble()*0.3;
			double angle = rand.nextDouble()*Math.PI*2;
			Wizardry.proxy.spawnParticle(EnumParticleType.SPARK, worldObj, this.posX + radius*Math.cos(angle), this.posY + 0.1, this.posZ + radius*Math.sin(angle), 0, 0, 0, 3);
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
