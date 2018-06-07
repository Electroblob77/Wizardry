package electroblob.wizardry.entity.living;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.registry.IEntityAdditionalSpawnData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryRegistry;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.item.ItemSpellBook;
import electroblob.wizardry.packet.PacketCastSpell;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.spell.Spell;
import io.netty.buffer.ByteBuf;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIArrowAttack;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILookAtTradePlayer;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAIRestrictOpenDoor;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITradePlayer;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAIWatchClosest2;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.oredict.OreDictionary;

public class EntityEvilWizard extends EntityMob implements IRangedAttackMob, IEntityAdditionalSpawnData {
	
    private EntityAIArrowAttack aiArrowAttack = new EntityAIArrowAttack(this, 0.5D, 20, 50, 14.0F);

	public int textureIndex = 0;
	
	public boolean hasTower = false;
    
    /** The entity selector passed into the new AI methods. */
	protected IEntitySelector targetSelector;
    
    /** Index for the heal cooldown field in the datawatcher */
    private static final int healCooldownIndex = 20;
    /** Index for the element field in the datawatcher */
    private static final int elementIndex = 16;
    
    private int[] spells = new int[4];

    public EntityEvilWizard(World par1World)
    {
        super(par1World);
        this.setSize(0.6F, 1.8F);
        this.dataWatcher.addObject(healCooldownIndex, 0);
        this.getNavigator().setBreakDoors(true);
        this.getNavigator().setAvoidsWater(true);
        this.tasks.taskEntries.clear();
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(4, new EntityAIRestrictOpenDoor(this));
        this.tasks.addTask(5, new EntityAIOpenDoor(this, true));
        this.tasks.addTask(6, new EntityAIMoveTowardsRestriction(this, 0.6D));
        this.tasks.addTask(7, new EntityAIWatchClosest2(this, EntityPlayer.class, 3.0F, 1.0F));
        this.tasks.addTask(7, new EntityAIWander(this, 0.6D));
        
        this.targetSelector = new IEntitySelector(){

			public boolean isEntityApplicable(Entity entity){

				// If the target is valid...
				if(entity != null && WizardryUtilities.isValidTarget(EntityEvilWizard.this, entity)){

					//... and is a player, a summoned creature, another (non-evil) wizard ...
					if(entity instanceof EntityPlayer || (entity instanceof EntitySummonedCreature || entity instanceof EntityWizard
							// ... or in the whitelist ...
							|| Arrays.asList(Wizardry.summonedCreatureTargetsWhitelist).contains(EntityList.getEntityString(entity).toLowerCase(Locale.ROOT)))
							// ... and isn't in the blacklist ...
							&& !Arrays.asList(Wizardry.summonedCreatureTargetsBlacklist).contains(EntityList.getEntityString(entity).toLowerCase(Locale.ROOT))){
						// ... it can be attacked.
						return true;
					}
				}

				return false;
			}
		};
        
        this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
        this.targetTasks.addTask(0, new EntityAINearestAttackableTarget(this, EntityLivingBase.class, 0, false, true, this.targetSelector));
        
        this.tasks.addTask(3, this.aiArrowAttack);
        
        this.detachHome();
    }
    
    protected void entityInit(){
        super.entityInit();
        this.dataWatcher.addObject(16, Integer.valueOf(0));
    }

    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.5D);
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setBaseValue(30);
    }

    /**
     * Returns true if the newer Entity AI code should be run
     */
    public boolean isAIEnabled()
    {
        return true;
    }
    
    @Override
    public void onLivingUpdate(){
    	
    	super.onLivingUpdate();
    	
    	int healCooldown = this.dataWatcher.getWatchableObjectInt(healCooldownIndex);
    	
    	// This is now done slightly differently because isPotionActive doesn't work on client here, meaning that when
    	// affected with arcane jammer and healCooldown == 0, whilst the wizard didn't actually heal or play the sound,
    	// the particles still spawned, and since healCooldown wasn't reset they spawned every tick until the arcane
    	// jammer wore off.
    	if(healCooldown == 0 && this.getHealth() < this.getMaxHealth() && this.getHealth() > 0 && !this.isPotionActive(Wizardry.arcaneJammer)){
    		
    		// Healer wizards use greater heal.
    		this.heal(this.getElement() == EnumElement.HEALING ? 8 : 4);
    		this.dataWatcher.updateObject(healCooldownIndex, -1);
    		
    	// deathTime == 0 checks the wizard isn't currently dying
    	}else if(healCooldown == -1 && this.deathTime == 0){
    		
    		// Heal particles
			if(worldObj.isRemote){
				for(int i=0; i<10; i++){
					double d0 = (double)((float)this.posX + rand.nextFloat()*2 - 1.0F);
					// Apparently the client side spawns the particles 1 block higher than it should... hence the - 0.5F.
					double d1 = (double)((float)this.posY - 0.5F + rand.nextFloat());
					double d2 = (double)((float)this.posZ + rand.nextFloat()*2 - 1.0F);
					Wizardry.proxy.spawnParticle(EnumParticleType.SPARKLE, worldObj, d0, d1, d2, 0, 0.1F, 0, 48 + rand.nextInt(12), 1.0f, 1.0f, 0.3f);
				}
			}else{
	    		if(this.getHealth() < 10){
	    			// Wizard heals himself more often if he has low health
	        		this.dataWatcher.updateObject(healCooldownIndex, 150);
	    		}else{
	        		this.dataWatcher.updateObject(healCooldownIndex, 400);
	    		}
	    		
				worldObj.playSoundAtEntity(this, "wizardry:heal", 0.7F, rand.nextFloat() * 0.4F + 1.0F);
			}
    	}
    	if(healCooldown > 0){// && !worldObj.isRemote){
    		this.dataWatcher.updateObject(healCooldownIndex, healCooldown-1);
    	}
    }
    
    @Override
	public void attackEntityWithRangedAttack(EntityLivingBase target, float f){
    	
    	if(f < 30.0F && !this.isPotionActive(Wizardry.arcaneJammer)){
    		
            double d0 = target.posX - this.posX;
            double d1 = target.boundingBox.minY + (double)(target.height / 2.0F) - (this.posY + (double)(this.height / 2.0F));
            double d2 = target.posZ - this.posZ;
            
            if (this.attackTime == 0 && spells.length > 0){
            	
            	if(!this.worldObj.isRemote){
            		
            		// New way of choosing a spell; keeps trying until one works or all have been tried
            		
            		List<Spell> spellsArray = new ArrayList<Spell>(spells.length);
            		
            		for(int i=0; i<spells.length; i++){
            			spellsArray.add(Spell.get(spells[i]));
            		}
            		
            		Spell spell;
            		
            		casting:
            		while(!spellsArray.isEmpty()){
            			
            			spell = spellsArray.get(rand.nextInt(spellsArray.size()));
            			
	            		if(spell != null && spell.cast(worldObj, this, target, 1, 1, 1, 1)){
	            			
		            		if(spell.doesSpellRequirePacket()){
								// Sends a packet to all players in dimension to tell them to spawn particles.
								IMessage msg = new PacketCastSpell.Message(this.getEntityId(), target.getEntityId(), spell.id(), 1, 1, 1);
								WizardryPacketHandler.net.sendToDimension(msg, worldObj.provider.dimensionId);
							}

		    	            this.rotationYaw = (float)(Math.atan2(d2, d0) * 180.0D / Math.PI) - 90.0F;
		    	            this.hasAttacked = true;
		    	            
		            		break casting;
		            		
	            		}else{
	            			spellsArray.remove(spell);
	            		}
            		}
            	}
            }
        }
	}

    /**
     * Called when a player interacts with a mob. e.g. gets milk from a cow, gets into the saddle on a pig.
     */
    public boolean interact(EntityPlayer player){
    	
    	// Debugging
    	//player.addChatComponentMessage(new ChatComponentTranslation("wizard.debug", Spell.get(spells[1]).getDisplayName(), Spell.get(spells[2]).getDisplayName(), Spell.get(spells[3]).getDisplayName()));
    	
        ItemStack itemstack = player.inventory.getCurrentItem();
        
        // When right-clicked with a spell book in creative, sets one of the spells to that spell
        if(player.capabilities.isCreativeMode && itemstack != null && itemstack.getItem() instanceof ItemSpellBook){
        	if(Spell.get(itemstack.getItemDamage()).canBeCastByNPCs()){
        		this.spells[rand.nextInt(3)+1] = itemstack.getItemDamage();
        		return true;
        	}
        }
        
        return false;
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    public void writeEntityToNBT(NBTTagCompound tagcompound)
    {
        super.writeEntityToNBT(tagcompound);
        tagcompound.setInteger("element", this.getElement().ordinal());
        tagcompound.setInteger("skin", this.textureIndex);
        tagcompound.setIntArray("spells", spells);
        tagcompound.setBoolean("hasTower", this.hasTower);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readEntityFromNBT(NBTTagCompound tagcompound)
    {
        super.readEntityFromNBT(tagcompound);
        this.setElement(EnumElement.values()[tagcompound.getInteger("element")]);
        this.textureIndex = tagcompound.getInteger("skin");
        this.spells = tagcompound.getIntArray("spells");
        this.hasTower = tagcompound.getBoolean("hasTower");
    }

    /**
     * Determines if an entity can be despawned, used on idle far away entities
     */
    @Override
    protected boolean canDespawn(){
    	// Evil wizards can only despawn if they don't have a tower (i.e. if they spawned naturally at night)
        return !this.hasTower;
    }

    /**
     * Returns the sound this mob makes while it's alive.
     */
    protected String getLivingSound()
    {
        return "mob.villager.no";
    }

    /**
     * Returns the sound this mob makes when it is hurt.
     */
    protected String getHurtSound()
    {
        return "mob.villager.hit";
    }

    /**
     * Returns the sound this mob makes on death.
     */
    protected String getDeathSound()
    {
        return "mob.villager.death";
    }
    
    public EnumElement getElement(){
    	return EnumElement.values()[this.dataWatcher.getWatchableObjectInt(elementIndex)];
    }
    
    public void setElement(EnumElement element){
    	this.dataWatcher.updateObject(elementIndex, Integer.valueOf(element.ordinal()));
    }
    
    @Override
    protected void dropFewItems(boolean hitByPlayer, int lootingLevel){
    	// Drops 3-5 crystals without looting bonuses
        int j = 3 + this.rand.nextInt(3) + this.rand.nextInt(1 + lootingLevel);

        for(int k=0; k<j; k++){
            this.dropItem(Wizardry.magicCrystal, 1);
        }
        
        // Evil wizards occasionally drop one of their spells as a spell book, but not magic missile. This isn't in
        // the dropRareDrop method because that would be just as rare as normal mobs; instead this is half as rare.
    	if(this.spells.length > 0 && rand.nextInt(100) - lootingLevel < 5) this.entityDropItem(new ItemStack(Wizardry.spellBook, 1, this.spells[1 + rand.nextInt(this.spells.length - 1)]), 0);
    }

    public void onDeath(DamageSource source){
    	
        super.onDeath(source);
        if(source.getEntity() instanceof EntityPlayer){
        	((EntityPlayer)source.getEntity()).triggerAchievement(Wizardry.defeatEvilWizard);
        }
    }

    public IEntityLivingData onSpawnWithEgg(IEntityLivingData par1EntityLivingData)
    {
        par1EntityLivingData = super.onSpawnWithEgg(par1EntityLivingData);

		textureIndex = this.rand.nextInt(6);
        
        if(rand.nextBoolean()){
        	this.setElement(EnumElement.values()[rand.nextInt(EnumElement.values().length - 1) + 1]);
        }else{
        	this.setElement(EnumElement.MAGIC);
        }
        
        EnumElement e = this.getElement();
        
        this.setCurrentItemOrArmor(1, new ItemStack(WizardryUtilities.getArmour(e, 3)));
        this.setCurrentItemOrArmor(2, new ItemStack(WizardryUtilities.getArmour(e, 2)));
        this.setCurrentItemOrArmor(3, new ItemStack(WizardryUtilities.getArmour(e, 1)));
        this.setCurrentItemOrArmor(4, new ItemStack(WizardryUtilities.getArmour(e, 0)));
        
        // This is the tier of the highest tier spell the wizard has.
        EnumTier maxTier = EnumTier.BASIC;
        
        // Default chance is 0.085f, for reference.
        this.setEquipmentDropChance(0, 0.1f);
        this.setEquipmentDropChance(1, 0.1f);
        this.setEquipmentDropChance(2, 0.1f);
        this.setEquipmentDropChance(3, 0.1f);
        this.setEquipmentDropChance(4, 0.1f);
        
        // All wizards know magic missile, even if it is disabled.
        spells[0] = WizardryRegistry.magicMissile.id();
        
        List<Spell> npcSpells = Spell.getSpells(Spell.npcSpells);
        
        for(int i=1; i<spells.length; i++){
        	
        	EnumTier tier;
        	// If the wizard has no element, it picks a random one each time.
    		EnumElement element = e == EnumElement.MAGIC ? EnumElement.values()[rand.nextInt(EnumElement.values().length)] : e;
        	
        	int randomiser = rand.nextInt(20);
        	
    		if(randomiser < 10){
    			tier = EnumTier.BASIC;
    		}else if(randomiser < 16){
    			tier = EnumTier.APPRENTICE;
    		}else{
    			tier = EnumTier.ADVANCED;
    		}
    		
    		if(tier.ordinal() > maxTier.ordinal()) maxTier = tier;
    		
    		// Finds all the spells of the chosen tier and element
    		List<Spell> list = Spell.getSpells(new Spell.TierElementFilter(tier, element));
    		// Keeps only spells which can be cast by NPCs
    		list.retainAll(npcSpells);
    		// Removes spells that the wizard already has
    		for(int j=0; j<i; j++){
    			list.remove(Spell.get(spells[j]));
    		}

        	// Ensures the tier chosen actually has spells in it. (isEmpty() is exactly the same as size() == 0)
    		if(list.isEmpty()){
    			// If there are no spells applicable, tier and element restrictions are removed to give maximum
    			// possibility of there being an applicable spell.
    			list = npcSpells;
    			// Removes spells that the wizard already has
        		for(int j=0; j<i; j++){
        			list.remove(Spell.get(spells[j]));
        		}
    		}

    		// If the list is still empty now, there must be less than 3 enabled spells that can be cast by wizards
    		// (excluding magic missile). In this case, having empty slots seems reasonable.
        	if(!list.isEmpty()) spells[i] = list.get(rand.nextInt(list.size())).id();
        	
        }
        
        // Now done after the spells so it can take the tier into account. For evil wizards this is slightly different;
        // it picks a random wand which is at least a high enough tier for the spells the wizard has.
        EnumTier tier = EnumTier.values()[maxTier.ordinal() + rand.nextInt(EnumTier.values().length - maxTier.ordinal())];
        this.setCurrentItemOrArmor(0, new ItemStack(WizardryUtilities.getWand(tier, e)));
        
        return par1EntityLivingData;
    }

	@Override
	public void writeSpawnData(ByteBuf data){
		data.writeInt(textureIndex);
	}

	@Override
	public void readSpawnData(ByteBuf data){
		textureIndex = data.readInt();
	}
	
}
