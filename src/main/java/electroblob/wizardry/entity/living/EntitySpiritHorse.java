package electroblob.wizardry.entity.living;

import java.util.UUID;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.ExtendedPlayer;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.item.ItemWand;
import net.minecraft.block.Block;
import net.minecraft.block.Block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.AnimalChest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class EntitySpiritHorse extends EntityHorse {
	
	private int idleTimer = 0;

	public EntitySpiritHorse(World par1World)
	{
		super(par1World);
	}

	/**
	 * Gets the username of the entity.
	 */
	@Override
	public String getCommandSenderName()
	{
		if (this.hasCustomNameTag())
		{
			return this.getCustomNameTag();
		}
		else
		{
			return StatCollector.translateToLocal("entity.wizardry.Spirit Horse.name");
		}
	}

	@Override
	public boolean isChested()
	{
		return false;
	}
	
	@Override
	public int getTotalArmorValue()
	{
		return 0;
	}
	
	@Override
    protected int getExperiencePoints(EntityPlayer p_70693_1_){
        return 0;
    }
	
	@Override
	protected Item getDropItem(){
		
        return null;
    }

	@Override
	protected void dropFewItems(boolean par1, int par2){}

	@Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(24.0D);
    }
	
	@Override
	public void openGUI(EntityPlayer p_110199_1_){}

	@Override
	public boolean interact(EntityPlayer par1EntityPlayer){
		
		ItemStack itemstack = par1EntityPlayer.inventory.getCurrentItem();
		
		// Allows the owner (but not other players) to dispel the spirit horse using a wand (shift-clicking, because clicking mounts the horse in this case).
		if(itemstack != null && itemstack.getItem() instanceof ItemWand && this.getOwner() == par1EntityPlayer && par1EntityPlayer.isSneaking()){
			// Prevents accidental double clicking.
			if(this.ticksExisted > 20){
				for(int i=0;i<15;i++){
					Wizardry.proxy.spawnParticle(EnumParticleType.SPARKLE, worldObj, this.posX - this.width/2 + this.rand.nextFloat()*width, this.posY + this.height*this.rand.nextFloat() + 0.2f, this.posZ - this.width/2 + this.rand.nextFloat()*width, 0, 0, 0, 48 + this.rand.nextInt(12), 0.8f, 0.8f, 1.0f);
				}
				this.setDead();
				if(ExtendedPlayer.get(par1EntityPlayer) != null){
					ExtendedPlayer.get(par1EntityPlayer).hasSpiritHorse = false;
				}
				this.playSound("wizardry:heal", 0.7F, rand.nextFloat() * 0.4F + 1.0F);
				// This is necessary to prevent the wand's spell being cast when performing this action.
				return true;
			}
			return false;
		}
		
		return super.interact(par1EntityPlayer);
	}
	
	@Override
	public void onDeath(DamageSource par1DamageSource){
		
		super.onDeath(par1DamageSource);
		
		// Allows player to summon another spirit horse once this one has died.
		if(this.getOwner() instanceof EntityPlayer && ExtendedPlayer.get((EntityPlayer)this.getOwner()) != null){
			ExtendedPlayer.get((EntityPlayer)this.getOwner()).hasSpiritHorse = false;
		}
	}

	// I wrote this one!
	private EntityLivingBase getOwner(){

		// func_152119_ch retrieves the owner UUID from the datawatcher
		if(this.func_152119_ch() == null || WizardryUtilities.verifyUUIDString(this.func_152119_ch())) return null;
		
		Entity owner = WizardryUtilities.getEntityByUUID(worldObj, UUID.fromString(this.func_152119_ch()));
		
		if(owner instanceof EntityLivingBase){
			return (EntityLivingBase)owner;
		}else{
			return null;
		}
	}

	@Override
	public void onUpdate(){
		
		super.onUpdate();

		// Adds a dust particle effect
		if(this.worldObj.isRemote){
			Wizardry.proxy.spawnParticle(EnumParticleType.DUST, worldObj, this.posX - this.width/2 + this.rand.nextFloat()*width, this.posY + this.height*this.rand.nextFloat() + 0.2f, this.posZ - this.width/2 + this.rand.nextFloat()*width, 0, 0, 0, 0, 0.8f, 0.8f, 1.0f);
		}

		// Spirit horse disappears a short time after being dismounted.
		if(this.riddenByEntity == null){
			this.idleTimer++;
		}else if(this.idleTimer > 0){
			this.idleTimer = 0;
		}

		if(this.idleTimer > 200){
			if(this.worldObj.isRemote){
				for(int i=0;i<15;i++){
					Wizardry.proxy.spawnParticle(EnumParticleType.SPARKLE, worldObj, this.posX - this.width/2 + this.rand.nextFloat()*width, this.posY + this.height*this.rand.nextFloat() + 0.2f, this.posZ - this.width/2 + this.rand.nextFloat()*width, 0, 0, 0, 48 + this.rand.nextInt(12), 0.8f, 0.8f, 1.0f);
				}
			}
			this.playSound("wizardry:heal", 0.7F, rand.nextFloat() * 0.4F + 1.0F);
			// Allows player to summon another spirit horse once this one has disappeared.
			if(this.getOwner() instanceof EntityPlayer && ExtendedPlayer.get((EntityPlayer)this.getOwner()) != null){
				ExtendedPlayer.get((EntityPlayer)this.getOwner()).hasSpiritHorse = false;
			}
			this.setDead();
		}
	}

	@Override
	public boolean canMateWith(EntityAnimal par1EntityAnimal){
		return false;
	}
	
	@Override
	public IEntityLivingData onSpawnWithEgg(IEntityLivingData par1EntityLivingData){
		
		// Adds Particles on spawn. Due to client/server differences this cannot be done in the item.
		if(this.worldObj.isRemote){
			for(int i=0;i<15;i++){
				Wizardry.proxy.spawnParticle(EnumParticleType.SPARKLE, worldObj, this.posX - this.width/2 + this.rand.nextFloat()*width, this.posY + this.height*this.rand.nextFloat() + 0.2f, this.posZ - this.width/2 + this.rand.nextFloat()*width, 0, 0, 0, 48 + this.rand.nextInt(12), 0.8f, 0.8f, 1.0f);
			}
		}
		
		return super.onSpawnWithEgg(par1EntityLivingData);
	}

}
