package electroblob.wizardry.entity.projectile;

import java.util.List;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.WizardryParticleType;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityIceCharge extends EntityBomb {
	
    public EntityIceCharge(World par1World)
    {
        super(par1World);
    }

    public EntityIceCharge(World par1World, EntityLivingBase par2EntityLivingBase)
    {
        super(par1World, par2EntityLivingBase);
    }
    
    public EntityIceCharge(World par1World, EntityLivingBase par2EntityLivingBase, float damageMultiplier, float blastMultiplier)
    {
        super(par1World, par2EntityLivingBase, damageMultiplier, blastMultiplier);
    }

    public EntityIceCharge(World par1World, double par2, double par4, double par6)
    {
        super(par1World, par2, par4, par6);
    }

    /**
     * Called when this EntityThrowable hits a block or entity.
     */
    protected void onImpact(RayTraceResult par1RayTraceResult)
    {
    	Entity entityHit = par1RayTraceResult.entityHit;
    	
        if (entityHit != null)
        {
        	// This is if the ice charge gets a direct hit
            float damage = 4 * damageMultiplier;
            
            entityHit.attackEntityFrom(MagicDamage.causeIndirectMagicDamage(this, this.getThrower(), DamageType.FROST).setProjectile(), damage);
            
            if(entityHit instanceof EntityLivingBase && !MagicDamage.isEntityImmune(DamageType.FROST, entityHit))
            	((EntityLivingBase)entityHit).addPotionEffect(new PotionEffect(WizardryPotions.frost, 120, 1));
        }

        // Particle effect
        if(worldObj.isRemote){
    		this.worldObj.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX, this.posY, this.posZ, 0, 0, 0);
			for(int i=0;i<30*blastMultiplier;i++){
				float brightness = 0.4f + rand.nextFloat()*0.5f;
				Wizardry.proxy.spawnParticle(WizardryParticleType.ICE, worldObj, this.posX + (this.rand.nextDouble()*4 - 2)*blastMultiplier, this.posY + (this.rand.nextDouble()*4 - 2)*blastMultiplier, this.posZ + (this.rand.nextDouble()*4 - 2)*blastMultiplier, 0.0d, 0.0d, 0.0d, 35);
				Wizardry.proxy.spawnParticle(WizardryParticleType.DARK_MAGIC, worldObj, this.posX + (this.rand.nextDouble()*4 - 2)*blastMultiplier, this.posY + (this.rand.nextDouble()*4 - 2)*blastMultiplier, this.posZ + (this.rand.nextDouble()*4 - 2)*blastMultiplier, 0.0d, 0.0d, 0.0d, 0, brightness, brightness+0.1f, 1.0f);
			}
        }

        if(!this.worldObj.isRemote){
        	
	    	this.playSound(SoundEvents.ENTITY_SPLASH_POTION_BREAK, 1.5f, rand.nextFloat() * 0.4f + 0.6f);
	    	this.playSound(WizardrySounds.SPELL_ICE, 1.2f, rand.nextFloat() * 0.4f + 1.2f);
			
	    	double radius = 3.0d*blastMultiplier;
			
			List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(radius, this.posX, this.posY, this.posZ, this.worldObj);
			
			// Slows targets
			for(EntityLivingBase target : targets){
				if(target != entityHit && target != this.getThrower()){
		            if(!MagicDamage.isEntityImmune(DamageType.FROST, target)) target.addPotionEffect(new PotionEffect(WizardryPotions.frost, 100, 0));
				}
			}
	    	
	    	// Places snow and ice on ground.
	    	for(int i=-1; i<2; i++){
				for(int j=-1; j<2; j++){
					
					BlockPos pos = new BlockPos(this.posX + i, this.posY, this.posZ + j);
					
					int y = WizardryUtilities.getNearestFloorLevelB(worldObj, pos, 7);
					
					pos = new BlockPos(pos.getX(), y, pos.getZ());
					
					double dist = this.getDistance(pos.getX(), pos.getY(), pos.getZ());
					
					// Randomised with weighting so that the nearer the block the more likely it is to be snowed.
					if(y != -1 && rand.nextInt((int)dist*2 + 1) < 1 && dist < 2){
						if(worldObj.getBlockState(pos.down()).getBlock() == Blocks.WATER){
							worldObj.setBlockState(pos.down(), Blocks.ICE.getDefaultState());
						}else{
							// Don't need to check whether the block at pos can be replaced since getNearestFloorLevelB
							// only ever returns floors with air above them.
							worldObj.setBlockState(pos, Blocks.SNOW_LAYER.getDefaultState());
						}
					}
				}
			}
	    	
	    	// Releases shards
			for(int i=0; i<10; i++){
				double dx = rand.nextDouble()-0.5;
				double dy = rand.nextDouble()-0.5;
				double dz = rand.nextDouble()-0.5;
				EntityIceShard iceshard = new EntityIceShard(worldObj, this.posX + dx, this.posY + dy, this.posZ + dz);
				iceshard.motionX = dx;
				iceshard.motionY = dy;
				iceshard.motionZ = dz;
				iceshard.setShootingEntity(this.getThrower());
				iceshard.damageMultiplier = this.damageMultiplier;
				worldObj.spawnEntityInWorld(iceshard);
			}
			
            this.setDead();
        }
    }
    
    @Override
	public boolean canRenderOnFire() {
		return false;
	}
}
