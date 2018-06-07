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
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.DamageSource;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.oredict.OreDictionary;

public class EntityWizard extends EntityVillager implements IRangedAttackMob, IEntityAdditionalSpawnData {
	
	/*
	 * After much debugging, the error in the compiled mod (outside of eclipse) was traced back to this class,
	 * specifically the methods copied in from EntityVillager when I changed this class to extend it. This figures,
	 * since I had 1.2.1 working just fine before I did that, and it was the only thing I changed. Apparently,
	 * methods and fields with obfuscated names like func_129090_a can cause problems when compiled. One of the
	 * ones here was renamed and the other deleted since it was never called. Watch out for this in future (unless,
	 * of course, they are overriding something, in which case it should be fine).
	 */
	
	// Extending EntityVillager turned out to be a pretty neat thing to do, since now zombies will attack wizards
	
    private EntityAIArrowAttack aiArrowAttack = new EntityAIArrowAttack(this, 0.5D, 20, 50, 14.0F);
    
    // Note: profession is used as element for wizards.

	public int textureIndex = 0;
	
    /** This villager's current customer. */
    private WeakReference<EntityPlayer> buyingPlayer;
    
    /** The entity selector passed into the new AI methods. */
	protected IEntitySelector targetSelector;

    /** Initialises the MerchantRecipeList.java */
    private MerchantRecipeList buyingList;
    private int timeUntilReset;

    /** addDefaultEquipmentAndRecipies is called if this is true */
    private boolean updateRecipes;
    
    /** Last player to trade with this villager, used for aggressivity. */
    private String lastBuyingPlayer;
    
    /** Index for the heal cooldown field in the datawatcher */
    private static final int healCooldownIndex = 20;
    
    private int[] spells = new int[4];
    
    private int[][] towerBlocks;

    public EntityWizard(World par1World)
    {
        super(par1World);
        this.setSize(0.6F, 1.8F);
        this.dataWatcher.addObject(healCooldownIndex, 0);
        this.getNavigator().setBreakDoors(true);
        this.getNavigator().setAvoidsWater(true);
        // EntityVillager adds unwanted AI, so this gets rid of it.
        this.tasks.taskEntries.clear();
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAITradePlayer(this));
        this.tasks.addTask(1, new EntityAILookAtTradePlayer(this));
        this.tasks.addTask(4, new EntityAIRestrictOpenDoor(this));
        this.tasks.addTask(5, new EntityAIOpenDoor(this, true));
        this.tasks.addTask(6, new EntityAIMoveTowardsRestriction(this, 0.6D));
        this.tasks.addTask(7, new EntityAIWatchClosest2(this, EntityPlayer.class, 3.0F, 1.0F));
        this.tasks.addTask(7, new EntityAIWatchClosest2(this, EntityWizard.class, 5.0F, 0.02F));
        this.tasks.addTask(7, new EntityAIWander(this, 0.6D));
        this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityLiving.class, 8.0F));
        
        this.targetSelector = new IEntitySelector(){

			public boolean isEntityApplicable(Entity entity){

				// If the target is valid...
				if(entity != null && WizardryUtilities.isValidTarget(EntityWizard.this, entity)){

					//... and is a mob, a summoned creature ...
					if((entity instanceof IMob || entity instanceof EntitySummonedCreature
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
        // By default, wizards don't attack players unless the player has attacked them.
        this.targetTasks.addTask(0, new EntityAINearestAttackableTarget(this, EntityLiving.class, 0, false, true, this.targetSelector));
        
        this.tasks.addTask(3, this.aiArrowAttack);
        
        this.detachHome();
    }

    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setBaseValue(0.5D);
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
    		this.heal(this.getProfession() == 7? 8 : 4);
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
     * main AI tick function, replaces updateEntityActionState
     */
    protected void updateAITick()
    {
        if (!this.isTrading() && this.timeUntilReset > 0)
        {
            --this.timeUntilReset;

            if (this.timeUntilReset <= 0)
            {
                if (this.updateRecipes)
                {
                    if (this.buyingList.size() > 1)
                    {
                        Iterator iterator = this.buyingList.iterator();

                        while (iterator.hasNext())
                        {
                            MerchantRecipe merchantrecipe = (MerchantRecipe)iterator.next();

                            if (merchantrecipe.isRecipeDisabled())
                            {
                            	// Increases the number of allowed uses of a disabled recipe by a random number.
                                merchantrecipe.func_82783_a(this.rand.nextInt(6) + this.rand.nextInt(6) + 2);
                            }
                        }
                    }
                    
                    if(this.buyingList.size() < 12){
                    	this.addRandomRecipes(1);
                    }
                    this.updateRecipes = false;
                }

                this.addPotionEffect(new PotionEffect(Potion.regeneration.id, 200, 0));
            }
        }
        
        // Super call removed because EntityVillager's version does things I don't want and the next one up is
        // in EntityLivingBase and does nothing.
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

        // Won't trade with a player that has attacked them.
        if (this.isEntityAlive() && !this.isTrading() && !this.isChild() && !player.isSneaking() && this.getAttackTarget() != player)
        {
            if (!this.worldObj.isRemote)
            {
                this.setCustomer(player);
                player.displayGUIMerchant(this, this.getElement().getWizardName());
            }

            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    public void writeEntityToNBT(NBTTagCompound tagcompound)
    {
        super.writeEntityToNBT(tagcompound);
        tagcompound.setInteger("Profession", this.getProfession());
        tagcompound.setInteger("skin", this.textureIndex);

        if (this.buyingList != null)
        {
            tagcompound.setTag("Offers", this.buyingList.getRecipiesAsTags());
        }
        
        tagcompound.setIntArray("spells", spells);
	    
        if(this.towerBlocks != null && this.towerBlocks.length > 0){
        
	        NBTTagList blocks = new NBTTagList();
	        
	        for(int[] block : this.towerBlocks){
	        	blocks.appendTag(new NBTTagIntArray(block));
	        }
	        
	        tagcompound.setTag("towerBlocks", blocks);
        }
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readEntityFromNBT(NBTTagCompound tagcompound)
    {
        super.readEntityFromNBT(tagcompound);
        this.setProfession(tagcompound.getInteger("Profession"));
        this.textureIndex = tagcompound.getInteger("skin");

        if (tagcompound.hasKey("Offers"))
        {
            NBTTagCompound nbttagcompound1 = tagcompound.getCompoundTag("Offers");
            this.buyingList = new MerchantRecipeList(nbttagcompound1);
        }
        
        this.spells = tagcompound.getIntArray("spells");
        
        NBTTagList blocks = tagcompound.getTagList("towerBlocks", NBT.TAG_INT_ARRAY);
        
        if(blocks != null){
        
	        if(this.towerBlocks == null) this.towerBlocks = new int[blocks.tagCount()][3];
	        
	        for(int i=0; i<blocks.tagCount(); i++){
		        this.towerBlocks[i] = blocks.func_150306_c(i);
	        }
        }
    }

    /**
     * Determines if an entity can be despawned, used on idle far away entities
     */
    protected boolean canDespawn()
    {
        return false;
    }

    /**
     * Returns the sound this mob makes while it's alive.
     */
    protected String getLivingSound()
    {
        return this.isTrading() ? "mob.villager.haggle" : "mob.villager.idle";
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
    	return EnumElement.values()[this.getProfession()];
    }

    /**
     * Called when the mob's health reaches 0.
     */
    public void onDeath(DamageSource par1DamageSource)
    {
        super.onDeath(par1DamageSource);
    }

    public void setCustomer(EntityPlayer par1EntityPlayer)
    {
        this.setBuyingPlayer(par1EntityPlayer);
    }

    public EntityPlayer getCustomer()
    {
        return this.getBuyingPlayer();
    }

    public boolean isTrading()
    {
        return this.getBuyingPlayer() != null;
    }

    public void useRecipe(MerchantRecipe merchantrecipe){
    
        merchantrecipe.incrementToolUses();
        this.livingSoundTime = -this.getTalkInterval();
        this.playSound("mob.villager.yes", this.getSoundVolume(), this.getSoundPitch());
        
        // Achievements
        if (this.getBuyingPlayer() != null)
        {
			this.getBuyingPlayer().triggerAchievement(Wizardry.wizardTrade);
			
			if(merchantrecipe.getItemToSell().getItem() instanceof ItemSpellBook
					&& Spell.get(merchantrecipe.getItemToSell().getItemDamage()).tier == EnumTier.MASTER){
    			this.getBuyingPlayer().triggerAchievement(Wizardry.buyMasterSpell);
			}
        }
        
        // It seems that if the recipe given is the last (newest) recipe, stuff happens.
        //if(par1MerchantRecipe.hasSameIDsAs((MerchantRecipe)this.buyingList.get(this.buyingList.size() - 1))){
        // Changed to a 4 in 5 chance of unlocking a new recipe.
        if(this.rand.nextInt(5) > 0){
        	this.timeUntilReset = 40;
            this.updateRecipes = true;

            if (this.getBuyingPlayer() != null)
            {
                this.lastBuyingPlayer = this.getBuyingPlayer().getCommandSenderName();
            }
            else
            {
                this.lastBuyingPlayer = null;
            }
        }
    }

    public void func_110297_a_(ItemStack par1ItemStack)
    {
        if (!this.worldObj.isRemote && this.livingSoundTime > -this.getTalkInterval() + 20)
        {
            this.livingSoundTime = -this.getTalkInterval();

            if (par1ItemStack != null)
            {
                this.playSound("mob.villager.yes", this.getSoundVolume(), this.getSoundPitch());
            }
            else
            {
                this.playSound("mob.villager.no", this.getSoundVolume(), this.getSoundPitch());
            }
        }
    }

    // This is called from the gui in order to display the recipes (no surprise there), and this is actually where
    // the initialisation is done, i.e. the trades don't actually exist until some player goes to trade with the
    // villager, at which point the first is added.
    public MerchantRecipeList getRecipes(EntityPlayer par1EntityPlayer){
    	
        if(this.buyingList == null){
        	
        	this.buyingList = new MerchantRecipeList();
        	
            // All wizards will buy spell books
            ItemStack anySpellBook = new ItemStack(Wizardry.spellBook, 1, OreDictionary.WILDCARD_VALUE);
            ItemStack crystalStack = new ItemStack(Wizardry.magicCrystal, 5);
            
            this.buyingList.add(new MerchantRecipe(anySpellBook, crystalStack));
            
            this.addRandomRecipes(3);
        }

        return this.buyingList;
    }

    /**
     * This is called once on initialisation and then once each time the wizard gains new trades (the particle thingy).
     */
    private void addRandomRecipes(int numberOfItemsToAdd){

        MerchantRecipeList merchantrecipelist;
        merchantrecipelist = new MerchantRecipeList();
        
        for(int i=0; i<numberOfItemsToAdd; i++){
        	
        	ItemStack itemToSell = null;
        	
        	boolean itemAlreadySold = true;
        	
        	// Prevents the wizard from selling the same thing twice. This appears not to be working at the moment, and
        	// even more strangely, the bit of code after Collections.shuffle(merchantrecipelist) should be doing this
        	// anyway. Also, the line: int a = this.rand.nextInt(6) + 6; very much implies that wizards should have 6-12
        	// trades, but many I have seen only have 3 or 4 (as far as I could be bothered to try anyway...). Not a major
        	// issue but could be annoying.
        	// EDIT: Fixed these problems by overhauling the trading system.
    		EnumTier tier = EnumTier.BASIC;
    		
        	while(itemAlreadySold){
        		
        		itemAlreadySold = false;
        		
        		/* New way of getting random item, by giving a chance to increase the tier which depends on how much the
        		 * player has already traded with the wizard. The more the player has traded with the wizard, the more
        		 * likely it is to get items of a higher tier. The -4 is to ignore the original 4 trades.
        		 * For reference, the chances are as follows:
        		 * Trades done		Basic		Apprentice		Advanced		Master 
        		 * 0				50%			25%				18%				8%
        		 * 1				46%			25%				20%				9%
        		 * 2				42%			24%				22%				12%
        		 * 3				38%			24%				24%				14%
        		 * 4				34%			22%				26%				17%
        		 * 5				30%			21%				28%				21%
        		 * 6				26%			19%				30%				24%
        		 * 7				22%			17%				32%				28%
        		 * 8				18%			15%				34%				33% */
        		
        		double tierIncreaseChance = 0.5 + 0.04*(Math.max(this.buyingList.size()-4, 0));

        		tier = EnumTier.BASIC;

        		if(rand.nextDouble() < tierIncreaseChance){
        			tier = EnumTier.APPRENTICE;
        			if(rand.nextDouble() < tierIncreaseChance){
            			tier = EnumTier.ADVANCED;
            			if(rand.nextDouble() < tierIncreaseChance*0.6){
                			tier = EnumTier.MASTER;
                		}
            		}
        		}
        		
    			itemToSell = this.getRandomItemOfTier(tier);
        		
        		for(Object recipe : merchantrecipelist){
        			if(ItemStack.areItemStacksEqual(((MerchantRecipe)recipe).getItemToSell(), itemToSell)) itemAlreadySold = true;
        		}
        		
        		if(this.buyingList != null){
	        		for(Object recipe : this.buyingList){
	        			if(ItemStack.areItemStacksEqual(((MerchantRecipe)recipe).getItemToSell(), itemToSell)) itemAlreadySold = true;
	        		}
        		}
        	}
        	
        	// Don't know how it can ever be null here, but saves it crashing.
        	if(itemToSell == null) return;
        	
        	merchantrecipelist.add(new MerchantRecipe(this.getRandomPrice(tier), new ItemStack(Wizardry.magicCrystal, tier.ordinal()*3 + 1 + rand.nextInt(4)), itemToSell));
        }

        Collections.shuffle(merchantrecipelist);

        if (this.buyingList == null)
        {
            this.buyingList = new MerchantRecipeList();
        }

        for (int j1 = 0; j1 < merchantrecipelist.size(); ++j1)
        {
            this.buyingList.add((MerchantRecipe)merchantrecipelist.get(j1));
        }
    }
    
    private ItemStack getRandomPrice(EnumTier tier) {
    	ItemStack itemstack = null;
    	switch(this.rand.nextInt(3)){
    	case 0:
    		itemstack = new ItemStack(Items.gold_ingot, (tier.ordinal()+1)*8-1 + rand.nextInt(6));
    		break;
    	case 1:
    		itemstack = new ItemStack(Items.diamond, (tier.ordinal()+1)*4-2 + rand.nextInt(3));
    		break;
    	case 2:
    		itemstack = new ItemStack(Items.emerald, (tier.ordinal()+1)*6-1 + rand.nextInt(3));
    		break;
    	}
    	return itemstack;
	}
    
	private ItemStack getRandomItemOfTier(EnumTier tier){
		
		int randomiser;
		
		// All enabled spells of the given tier
		List<Spell> spells = Spell.getSpells(new Spell.TierElementFilter(tier, null));
		// All enabled spells of the given tier that match this wizard's element
		List<Spell> specialismSpells = Spell.getSpells(new Spell.TierElementFilter(tier, this.getElement()));
		
		// This code is sooooooo much neater with the new filter system!
		switch(tier){
		
		case BASIC:
			randomiser = rand.nextInt(5);
			if(randomiser < 4 && !spells.isEmpty()){
				if(this.getProfession() > 0 && rand.nextInt(4) > 0 && !specialismSpells.isEmpty()){
					// This means it is more likely for spell books sold to be of the same element as the wizard if the wizard has an element.
					return new ItemStack(Wizardry.spellBook, 1, specialismSpells.get(rand.nextInt(specialismSpells.size())).id());
				}else{
					return new ItemStack(Wizardry.spellBook, 1, spells.get(rand.nextInt(spells.size())).id());
				}
			}else{
				if(this.getProfession() > 0 && rand.nextInt(4) > 0){
					// This means it is more likely for wands sold to be of the same element as the wizard if the wizard has an element.
					return new ItemStack(WizardryUtilities.getWand(tier, this.getElement()));
				}else{
					return new ItemStack(WizardryUtilities.getWand(tier, EnumElement.values()[rand.nextInt(EnumElement.values().length)]));
				}
			}
			
		case APPRENTICE:
			randomiser = rand.nextInt(Wizardry.discoveryMode ? 12 : 10);
			if(randomiser < 5 && !spells.isEmpty()){
				if(this.getProfession() > 0 && rand.nextInt(4) > 0 && !specialismSpells.isEmpty()){
					// This means it is more likely for spell books sold to be of the same element as the wizard if the wizard has an element.
					return new ItemStack(Wizardry.spellBook, 1, specialismSpells.get(rand.nextInt(specialismSpells.size())).id());
				}else{
					return new ItemStack(Wizardry.spellBook, 1, spells.get(rand.nextInt(spells.size())).id());
				}
			}else if(randomiser < 6){
				if(this.getProfession() > 0 && rand.nextInt(4) > 0){
					// This means it is more likely for wands sold to be of the same element as the wizard if the wizard has an element.
					return new ItemStack(WizardryUtilities.getWand(tier, this.getElement()));
				}else{
					return new ItemStack(WizardryUtilities.getWand(tier, EnumElement.values()[rand.nextInt(EnumElement.values().length)]));
				}
			}else if(randomiser < 8){
				return new ItemStack(Wizardry.arcaneTome, 1, 1);
			}else if(randomiser < 10){
				int slot = rand.nextInt(4);
				if(this.getProfession() > 0 && rand.nextInt(4) > 0){
					// This means it is more likely for armour sold to be of the same element as the wizard if the wizard has an element.
					return new ItemStack(WizardryUtilities.getArmour(this.getElement(), slot));
				}else{
					return new ItemStack(WizardryUtilities.getArmour(EnumElement.values()[rand.nextInt(EnumElement.values().length)], slot));
				}
			}else{
				// Don't need to check for discovery mode here since it is done above
				return new ItemStack(Wizardry.identificationScroll);
			}
			
		case ADVANCED:
			randomiser = rand.nextInt(12);
			if(randomiser < 5 && !spells.isEmpty()){
				if(this.getProfession() > 0 && rand.nextInt(4) > 0 && !specialismSpells.isEmpty()){
					// This means it is more likely for spell books sold to be of the same element as the wizard if the wizard has an element.
					return new ItemStack(Wizardry.spellBook, 1, specialismSpells.get(rand.nextInt(specialismSpells.size())).id());
				}else{
					return new ItemStack(Wizardry.spellBook, 1, spells.get(rand.nextInt(spells.size())).id());
				}
			}else if(randomiser < 6){
				if(this.getProfession() > 0 && rand.nextInt(4) > 0){
					// This means it is more likely for wands sold to be of the same element as the wizard if the wizard has an element.
					return new ItemStack(WizardryUtilities.getWand(tier, this.getElement()));
				}else{
					return new ItemStack(WizardryUtilities.getWand(tier, EnumElement.values()[rand.nextInt(EnumElement.values().length)]));
				}
			}else if(randomiser < 8){
				return new ItemStack(Wizardry.arcaneTome, 1, 2);
			}else{
				randomiser = rand.nextInt(8);
				switch(randomiser){
				case 0: return new ItemStack(Wizardry.condenserUpgrade);
				case 1: return new ItemStack(Wizardry.siphonUpgrade);
				case 2: return new ItemStack(Wizardry.storageUpgrade);
				case 3: return new ItemStack(Wizardry.rangeUpgrade);
				case 4: return new ItemStack(Wizardry.durationUpgrade);
				case 5: return new ItemStack(Wizardry.cooldownUpgrade);
				case 6: return new ItemStack(Wizardry.blastUpgrade);
				case 7: return new ItemStack(Wizardry.attunementUpgrade);
				}
			}
			
		case MASTER:
			// If a regular wizard rolls a master trade, it can only be a simple master wand or a tome of arcana
			randomiser = this.getProfession() > 0 ? rand.nextInt(8) : 5 + rand.nextInt(3);
			
			if(randomiser < 5 && this.getProfession() > 0 && !specialismSpells.isEmpty()){
				// Master spells can only be sold by a specialist in that element.
				return new ItemStack(Wizardry.spellBook, 1, specialismSpells.get(rand.nextInt(specialismSpells.size())).id());
					
			}else if(randomiser < 6){
				if(this.getProfession() > 0 && rand.nextInt(4) > 0){
					// Master elemental wands can only be sold by a specialist in that element.
					return new ItemStack(WizardryUtilities.getWand(tier, this.getElement()));
				}else{
					return new ItemStack(Wizardry.masterWand);
				}
			}else{
				return new ItemStack(Wizardry.arcaneTome, 1, 3);
			}
		}

		return new ItemStack(Blocks.stone);
	}

    @SideOnly(Side.CLIENT)
    public void setRecipes(MerchantRecipeList par1MerchantRecipeList) {}

    public IEntityLivingData onSpawnWithEgg(IEntityLivingData par1EntityLivingData)
    {
        par1EntityLivingData = super.onSpawnWithEgg(par1EntityLivingData);

		textureIndex = this.rand.nextInt(6);
        
        if(rand.nextBoolean()){
        	this.setProfession(rand.nextInt(EnumElement.values().length - 1) + 1);
        }else{
        	this.setProfession(0);
        }
        
        EnumElement e = this.getElement();
        
        this.setCurrentItemOrArmor(1, new ItemStack(WizardryUtilities.getArmour(e, 3)));
        this.setCurrentItemOrArmor(2, new ItemStack(WizardryUtilities.getArmour(e, 2)));
        this.setCurrentItemOrArmor(3, new ItemStack(WizardryUtilities.getArmour(e, 1)));
        this.setCurrentItemOrArmor(4, new ItemStack(WizardryUtilities.getArmour(e, 0)));
        
        // This is the tier of the highest tier spell the wizard has.
        EnumTier maxTier = EnumTier.BASIC;
        
        // Default chance is 0.085f, for reference.
        this.setEquipmentDropChance(0, 0.0f);
        this.setEquipmentDropChance(1, 0.0f);
        this.setEquipmentDropChance(2, 0.0f);
        this.setEquipmentDropChance(3, 0.0f);
        this.setEquipmentDropChance(4, 0.0f);
        
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
        
        // Now done after the spells so it can take the tier into account.
        this.setCurrentItemOrArmor(0, new ItemStack(WizardryUtilities.getWand(maxTier, e)));
        
        return par1EntityLivingData;
    }

    public EntityWizard doSomething(EntityAgeable par1EntityAgeable)
    {
        EntityWizard entityvillager = new EntityWizard(this.worldObj);
        entityvillager.onSpawnWithEgg((IEntityLivingData)null);
        return entityvillager;
    }

    public boolean allowLeashing()
    {
        return false;
    }

    public EntityVillager createChild(EntityAgeable par1EntityAgeable)
    {
        return this.doSomething(par1EntityAgeable);
    }

	@Override
	public void writeSpawnData(ByteBuf data){
		data.writeInt(textureIndex);
	}

	@Override
	public void readSpawnData(ByteBuf data){
		textureIndex = data.readInt();
	}

	private EntityPlayer getBuyingPlayer() {
		return buyingPlayer == null ? null : buyingPlayer.get();
	}

	private void setBuyingPlayer(EntityPlayer buyingPlayer) {
		this.buyingPlayer = new WeakReference(buyingPlayer);
	}
	
	@Override
	public boolean attackEntityFrom(DamageSource source, float damage){
		
		if(source.getEntity() instanceof EntityPlayer){
			((EntityPlayer)source.getEntity()).triggerAchievement(Wizardry.angerWizard);
		}
			
		return super.attackEntityFrom(source, damage);
	}
	
	/** 
	 * Sets the list of blocks that are part of this wizard's tower. If a player breaks any of these blocks, the wizard
	 * will get angry and attack them.
	 * @param blocks An array of coordinate arrays for the blocks in the tower.
	 */
	public void setTowerBlocks(int[][] blocks){
		this.towerBlocks = blocks;
	}
	
	/**
	 * Tests whether the block at the given coordinates is part of this wizard's tower.
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public boolean isBlockPartOfTower(int x, int y, int z){
		
		if(this.towerBlocks == null || this.towerBlocks.length <= 0) return false;
		
		for(int[] block : this.towerBlocks){
			if(block != null && block[0] == x && block[1] == y && block[2] == z) return true;
		}
		
		return false;
	}
	
	// Start of EntityVillager overrides
	
	@Override
    public boolean isMating()
    {
        return false;
    }
	
	@Override
    public void setMating(boolean p_70947_1_){}
	
	@Override
    public void setPlaying(boolean p_70939_1_){}
	
	@Override
    public boolean isPlaying()
    {
        return false;
    }
	
	@Override
    public void setLookingForHome(){}
	
	
}
