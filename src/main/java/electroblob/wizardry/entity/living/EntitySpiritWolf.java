package electroblob.wizardry.entity.living;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.ExtendedPlayer;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.ItemWand;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAIFollowOwner;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILeapAtTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIOwnerHurtByTarget;
import net.minecraft.entity.ai.EntityAIOwnerHurtTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntitySpiritWolf extends EntityWolf {

    public EntitySpiritWolf(World par1World){
    	
        super(par1World);
        // Removes all the AI in EntityWolf so I can add what I want instead.
        this.tasks.taskEntries.clear();
        this.targetTasks.taskEntries.clear();
        this.getNavigator().setAvoidsWater(true);
        this.tasks.addTask(1, new EntityAISwimming(this));
        this.tasks.addTask(2, this.aiSit);
        this.tasks.addTask(3, new EntityAILeapAtTarget(this, 0.4F));
        this.tasks.addTask(4, new EntityAIAttackOnCollide(this, 1.0D, true));
        this.tasks.addTask(5, new EntityAIFollowOwner(this, 1.0D, 10.0F, 2.0F));
        this.tasks.addTask(7, new EntityAIWander(this, 1.0D));
        this.tasks.addTask(9, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(9, new EntityAILookIdle(this));
        this.targetTasks.addTask(1, new EntityAIOwnerHurtByTarget(this));
        this.targetTasks.addTask(2, new EntityAIOwnerHurtTarget(this));
        this.targetTasks.addTask(3, new EntityAIHurtByTarget(this, true));
        this.experienceValue = 0;
    }

    @Override
    public void onDeath(DamageSource par1DamageSource){
    	
    	// Allows player to summon another spirit wolf once this one has died.
    	// TODO: Or at least, it should...
		if(this.getOwner() instanceof EntityPlayer && ExtendedPlayer.get((EntityPlayer)this.getOwner()) != null){
			ExtendedPlayer.get((EntityPlayer)this.getOwner()).hasSpiritWolf = false;
		}
		
    	super.onDeath(par1DamageSource);
    }
    
    @Override
    protected int getExperiencePoints(EntityPlayer p_70693_1_){
        return 0;
    }
    
    @Override
    public IEntityLivingData onSpawnWithEgg(IEntityLivingData par1EntityLivingData){
    	
    	// Adds Particles on spawn. Due to client/server differences this cannot be done in the item.
    	if(this.worldObj.isRemote){
    		for(int i=0;i<15;i++){
    			Wizardry.proxy.spawnParticle(EnumParticleType.SPARK, worldObj, this.posX - this.width/2 + this.rand.nextFloat()*width, this.posY + this.height*this.rand.nextFloat() + 0.2f, this.posZ - this.width/2 + this.rand.nextFloat()*width, 0, 0, 0, 48 + this.rand.nextInt(12), 0.8f, 0.8f, 1.0f);
    		}
    	}
		return par1EntityLivingData;
    }
    
    @Override
    public void onUpdate(){
    	
        super.onUpdate();
        
        // Adds a dust particle effect
    	if(this.worldObj.isRemote){
    		Wizardry.proxy.spawnParticle(EnumParticleType.DUST, worldObj, this.posX - this.width/2 + this.rand.nextFloat()*width, this.posY + this.height*this.rand.nextFloat() + 0.2f, this.posZ - this.width/2 + this.rand.nextFloat()*width, 0, 0, 0, 0, 0.8f, 0.8f, 1.0f);
    	}
    }

    @Override
    public boolean interact(EntityPlayer player)
    {
        ItemStack itemstack = player.inventory.getCurrentItem();

        if (this.isTamed())
        {
            if (itemstack != null){
            	
                // Allows the owner (but not other players) to dispel the spirit wolf using a wand.
                if(itemstack != null && itemstack.getItem() instanceof ItemWand && this.getOwner() == player && player.isSneaking()){
                	// Prevents accidental double clicking.
                	if(this.ticksExisted > 20){
                		for(int i=0;i<10;i++){
                			Wizardry.proxy.spawnParticle(EnumParticleType.SPARKLE, worldObj, this.posX - this.width/2 + this.rand.nextFloat()*width, this.posY + this.height*this.rand.nextFloat() + 0.2f, this.posZ - this.width/2 + this.rand.nextFloat()*width, 0, 0, 0, 48 + this.rand.nextInt(12), 0.8f, 0.8f, 1.0f);
                		}
                		this.setDead();
                		if(ExtendedPlayer.get(player) != null){
                			ExtendedPlayer.get(player).hasSpiritWolf = false;
                		}
                		this.playSound("wizardry:heal", 0.7F, rand.nextFloat() * 0.4F + 1.0F);
                		// This is necessary to prevent the wand's spell being cast when performing this action.
                		return true;
                	}
                }
            }
        }

        return super.interact(player);
    }

    @Override
    public EntityWolf createChild(EntityAgeable par1EntityAgeable)
    {
        return null;
    }

    @Override
    protected void func_145780_a(int p_145780_1_, int p_145780_2_, int p_145780_3_, Block p_145780_4_)
    {
        this.playSound("mob.wolf.step", 0.15F, 1.0F);
    }

    @Override
    protected Item getDropItem()
    {
        return null;
    }

}
