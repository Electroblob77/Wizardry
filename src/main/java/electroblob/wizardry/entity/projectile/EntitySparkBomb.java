package electroblob.wizardry.entity.projectile;

import java.util.List;
import java.util.UUID;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.MagicDamage;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.MagicDamage.DamageType;
import electroblob.wizardry.entity.EntityArc;
import io.netty.buffer.ByteBuf;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class EntitySparkBomb extends EntityMagicProjectile implements IEntityAdditionalSpawnData
{
	/** For client use, because thrower field is not visible. */ 
	private int casterID;
	
	/** The entity blast multiplier. Only some projectiles cause a blast, which is why this isn't in EntityMagicProjectile. */
	public float blastMultiplier;
	
    public EntitySparkBomb(World par1World)
    {
        super(par1World);
    }

    public EntitySparkBomb(World par1World, EntityLivingBase par2EntityLivingBase)
    {
        super(par1World, par2EntityLivingBase);
    }
    
    public EntitySparkBomb(World par1World, EntityLivingBase par2EntityLivingBase, float damageMultiplier, float blastMultiplier)
    {
        super(par1World, par2EntityLivingBase, damageMultiplier);
        this.blastMultiplier = blastMultiplier;
    }

    public EntitySparkBomb(World par1World, double par2, double par4, double par6)
    {
        super(par1World, par2, par4, par6);
    }

    /**
     * Called when this EntityThrowable hits a block or entity.
     */
    protected void onImpact(MovingObjectPosition par1MovingObjectPosition)
    {
        this.playSound("fireworks.blast_far", 0.5f, 0.5f);

    	Entity entityHit = par1MovingObjectPosition.entityHit;
    	
        if (entityHit != null)
        {
        	// This is if the spark bomb gets a direct hit
            float damage = 6 * damageMultiplier;
            
            this.playSound("game.neutral.hurt", 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));

            entityHit.attackEntityFrom(MagicDamage.causeIndirectEntityMagicDamage(this, this.getThrower(), DamageType.SHOCK).setProjectile(), damage);
           
        }

        // Particle effect
        if(worldObj.isRemote){
			for(int i=0;i<8;i++){
				Wizardry.proxy.spawnParticle(EnumParticleType.SPARK, worldObj, this.posX + rand.nextFloat() - 0.5, this.posY + this.height/2 + rand.nextFloat() - 0.5, this.posZ + rand.nextFloat() - 0.5, 0, 0, 0, 3);
				worldObj.spawnParticle("largesmoke", this.posX + rand.nextFloat() - 0.5, this.posY + this.height/2 + rand.nextFloat() - 0.5, this.posZ + rand.nextFloat() - 0.5, 0, 0, 0);
			}
        }
		
        double seekerRange = 5.0d * blastMultiplier;
		
		List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(seekerRange, this.posX, this.posY, this.posZ, this.worldObj);
		
		for(int i=0; i<Math.min(targets.size(), 4); i++){
			
			boolean flag = targets.get(i) != entityHit && targets.get(i) != this.getThrower() && !(targets.get(i) instanceof EntityPlayer && ((EntityPlayer)targets.get(i)).capabilities.isCreativeMode);
			
			// Detects (client side) if target is the thrower, to stop particles being spawned around them.
			if(flag && worldObj.isRemote && targets.get(i).getEntityId() == this.casterID) flag = false;
			
			if(flag){
				
				EntityLivingBase target = targets.get(i);
				
				if(!this.worldObj.isRemote){
					
					EntityArc arc = new EntityArc(this.worldObj);
					arc.setEndpointCoords(this.posX, this.posY, this.posZ,
							target.posX, target.posY + target.height/2, target.posZ);
					this.worldObj.spawnEntityInWorld(arc);
					
					worldObj.playSoundAtEntity(target, "wizardry:arc", 1.0F, rand.nextFloat() * 0.4F + 1.5F);
					
					target.attackEntityFrom(MagicDamage.causeIndirectEntityMagicDamage(this, this.getThrower(), DamageType.SHOCK), 5.0f * damageMultiplier);
				
				}else{
					// Particle effect
					for(int j=0;j<8;j++){
						Wizardry.proxy.spawnParticle(EnumParticleType.SPARK, worldObj, target.posX + rand.nextFloat() - 0.5, WizardryUtilities.getEntityFeetPos(target) + target.height*rand.nextFloat(), target.posZ + rand.nextFloat() - 0.5, 0, 0, 0, 3);
						worldObj.spawnParticle("largesmoke", target.posX + rand.nextFloat() - 0.5, WizardryUtilities.getEntityFeetPos(target) + target.height*rand.nextFloat(), target.posZ + rand.nextFloat() - 0.5, 0, 0, 0);
					}
				}
			}
		}

        this.setDead();
    }

	@Override
	public void writeSpawnData(ByteBuf data) {
		data.writeInt(this.getThrower().getEntityId());
		data.writeFloat(blastMultiplier);
	}

	@Override
	public void readSpawnData(ByteBuf data) {
		this.casterID = data.readInt();
		this.blastMultiplier = data.readFloat();
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
