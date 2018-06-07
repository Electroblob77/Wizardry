package electroblob.wizardry.entity.projectile;

import java.util.List;

import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.MagicDamage;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.MagicDamage.DamageType;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class EntityIceCharge extends EntityMagicProjectile implements IEntityAdditionalSpawnData
{
	/** The entity blast multiplier. Only some projectiles cause a blast, which is why this isn't in EntityMagicProjectile. */
	public float blastMultiplier;
	
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
        super(par1World, par2EntityLivingBase, damageMultiplier);
        this.blastMultiplier = blastMultiplier;
    }

    public EntityIceCharge(World par1World, double par2, double par4, double par6)
    {
        super(par1World, par2, par4, par6);
    }

    /**
     * Called when this EntityThrowable hits a block or entity.
     */
    protected void onImpact(MovingObjectPosition par1MovingObjectPosition)
    {
    	Entity entityHit = par1MovingObjectPosition.entityHit;
    	
        if (entityHit != null)
        {
        	// This is if the ice charge gets a direct hit
            float damage = 4 * damageMultiplier;
            
            entityHit.attackEntityFrom(MagicDamage.causeIndirectEntityMagicDamage(this, this.getThrower(), DamageType.FROST).setProjectile(), damage);
            
            if(entityHit instanceof EntityLivingBase && !MagicDamage.isEntityImmune(DamageType.FROST, entityHit))
            	((EntityLivingBase)entityHit).addPotionEffect(new PotionEffect(Wizardry.frost.id, 120, 1));
        }

        // Particle effect
        if(worldObj.isRemote){
    		this.worldObj.spawnParticle("largeexplode", this.posX, this.posY, this.posZ, 0, 0, 0);
			for(int i=0;i<30*blastMultiplier;i++){
				float brightness = 0.4f + rand.nextFloat()*0.5f;
				Wizardry.proxy.spawnParticle(EnumParticleType.ICE, worldObj, this.posX + (this.rand.nextDouble()*4 - 2)*blastMultiplier, this.posY + (this.rand.nextDouble()*4 - 2)*blastMultiplier, this.posZ + (this.rand.nextDouble()*4 - 2)*blastMultiplier, 0.0d, 0.0d, 0.0d, 35);
				Wizardry.proxy.spawnParticle(EnumParticleType.DARK_MAGIC, worldObj, this.posX + (this.rand.nextDouble()*4 - 2)*blastMultiplier, this.posY + (this.rand.nextDouble()*4 - 2)*blastMultiplier, this.posZ + (this.rand.nextDouble()*4 - 2)*blastMultiplier, 0.0d, 0.0d, 0.0d, 0, brightness, brightness+0.1f, 1.0f);
			}
        }

        if(!this.worldObj.isRemote){
        	
	    	this.worldObj.playSoundEffect(this.posX, this.posY, this.posZ, "random.glass", 1.5f, rand.nextFloat() * 0.4f + 0.6f);
	    	this.worldObj.playSoundEffect(this.posX, this.posY, this.posZ, "wizardry:ice", 1.2f, rand.nextFloat() * 0.4f + 1.2f);
			
	    	double radius = 3.0d*blastMultiplier;
			
			List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(radius, this.posX, this.posY, this.posZ, this.worldObj);
			
			// Slows targets
			for(EntityLivingBase target : targets){
				if(target != entityHit && target != this.getThrower()){
		            if(!MagicDamage.isEntityImmune(DamageType.FROST, target)) target.addPotionEffect(new PotionEffect(Wizardry.frost.id, 100, 0, true));
				}
			}
	    	
	    	// Places snow and ice on ground.
	    	for(int i=-1; i<2; i++){
				for(int j=-1; j<2; j++){
					int y = WizardryUtilities.getNearestFloorLevelB(worldObj, (int)this.posX + i, (int)this.posY, (int)this.posZ + j, 7);
					double dist = this.getDistance((int)this.posX + i, y, (int)this.posZ + j);
					// Randomised with weighting so that the nearer the block the more likely it is to be snowed.
					if(y != -1 && rand.nextInt((int)dist*2 + 1) < 1 && dist < 2){
						if(worldObj.getBlock((int)this.posX + i, y-1, (int)this.posZ + j) == Blocks.water){
							worldObj.setBlock((int)this.posX + i, y-1, (int)this.posZ + j, Blocks.ice);
						}else{
							worldObj.setBlock((int)this.posX + i, y, (int)this.posZ + j, Blocks.snow_layer);
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
    
	@Override
	public void writeSpawnData(ByteBuf buffer) {
		buffer.writeFloat(blastMultiplier);
	}

	@Override
	public void readSpawnData(ByteBuf buffer) {
		blastMultiplier = buffer.readFloat();
	}
	
	@Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound){
    	super.readEntityFromNBT(nbttagcompound);
        blastMultiplier = nbttagcompound.getFloat("blastMultiplier");
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound){
		super.writeEntityToNBT(nbttagcompound);
		nbttagcompound.setFloat("blastMultiplier", blastMultiplier);
	}
}
