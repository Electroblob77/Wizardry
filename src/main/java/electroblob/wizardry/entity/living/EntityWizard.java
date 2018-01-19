package electroblob.wizardry.entity.living;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

import com.google.common.base.Predicate;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.item.ItemSpellBook;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryAchievements;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WandHelper;
import electroblob.wizardry.util.WizardryParticleType;
import electroblob.wizardry.util.WizardryUtilities;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
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
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

public class EntityWizard extends EntityVillager implements ISpellCaster, IEntityAdditionalSpawnData {

	/*
	 * After much debugging, the error in the compiled mod (outside of eclipse) was traced back to this class,
	 * specifically the methods copied in from EntityVillager when I changed this class to extend it. This figures,
	 * since I had 1.2.1 working just fine before I did that, and it was the only thing I changed. Apparently,
	 * methods and fields with obfuscated names like func_129090_a can cause problems when compiled. One of the
	 * ones here was renamed and the other deleted since it was never called. Watch out for this in future (unless,
	 * of course, they are overriding something, in which case it should be fine).
	 */

	// Extending EntityVillager turned out to be a pretty neat thing to do, since now zombies will attack wizards

	private EntityAIAttackSpell spellCastingAI = new EntityAIAttackSpell(this, 0.5D, 14.0F, 30, 50);

	public int textureIndex = 0;

	/** The entity selector passed into the new AI methods. */
	protected Predicate<Entity> targetSelector;

	/** Copy of EntityVillager's buyingList, renamed to avoid confusion. */
	private MerchantRecipeList trades;
	private int timeUntilReset;

	/** addDefaultEquipmentAndRecipies is called if this is true */
	private boolean updateRecipes;

	/** Data parameter for the cooldown time for wizards healing themselves. */
    private static final DataParameter<Integer> HEAL_COOLDOWN = EntityDataManager.createKey(EntityWizard.class, DataSerializers.VARINT);
	/** Data parameter for the wizard's element. */
	private static final DataParameter<Integer> ELEMENT = EntityDataManager.createKey(EntityWizard.class, DataSerializers.VARINT);

    // Field implementations
	private List<Spell> spells = new ArrayList<Spell>(4);
	private Spell continuousSpell;

	/** A set of the positions of the blocks that are part of this wizard's tower. */
	private Set<BlockPos> towerBlocks;

	public EntityWizard(World world){
		super(world);
		this.detachHome();
		// For some reason this can't be in initEntityAI
		this.tasks.addTask(3, this.spellCastingAI);
	}
	
	@Override
	protected void entityInit(){
		super.entityInit();
		this.dataManager.register(HEAL_COOLDOWN, -1);
		this.dataManager.register(ELEMENT, 0);
	}
	
	@Override
	protected void initEntityAI(){

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

		this.targetSelector = new Predicate<Entity>(){

			public boolean apply(Entity entity){

				// If the target is valid and not invisible...
				if(entity != null && !entity.isInvisible() && WizardryUtilities.isValidTarget(EntityWizard.this, entity)){

					//... and is a mob, a summoned creature ...
					if((entity instanceof IMob || entity instanceof ISummonedCreature
							// ... or in the whitelist ...
							|| Arrays.asList(Wizardry.settings.summonedCreatureTargetsWhitelist).contains(EntityList.getEntityString(entity).toLowerCase(Locale.ROOT)))
							// ... and isn't in the blacklist ...
							&& !Arrays.asList(Wizardry.settings.summonedCreatureTargetsBlacklist).contains(EntityList.getEntityString(entity).toLowerCase(Locale.ROOT))){
						// ... it can be attacked.
						return true;
					}
				}

				return false;
			}
		};

		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
		// By default, wizards don't attack players unless the player has attacked them.
		this.targetTasks.addTask(0, new EntityAINearestAttackableTarget<EntityLiving>(this, EntityLiving.class, 0, false, true, this.targetSelector));
	}

	@Override
	protected void applyEntityAttributes(){
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.5);
	}
	
	private int getHealCooldown(){
		return this.dataManager.get(HEAL_COOLDOWN);
	}
	
	private void setHealCooldown(int cooldown){
		this.dataManager.set(HEAL_COOLDOWN, cooldown);
	}

	public Element getElement(){
		return Element.values()[this.dataManager.get(ELEMENT)];
	}

	public void setElement(Element element){
		this.dataManager.set(ELEMENT, element.ordinal());
	}

	@Override
	public List<Spell> getSpells(){
		return this.spells;
	}

	@Override
	public SpellModifiers getModifiers(){
		return new SpellModifiers();
	}
	
	@Override
	public void setContinuousSpell(Spell spell){
		this.continuousSpell = spell;
	}
	
	@Override
	public Spell getContinuousSpell(){
		return this.continuousSpell;
	}

	@Override
	public void onLivingUpdate(){

		super.onLivingUpdate();

		// Still better to store this to a local variable as it's almost certainly more efficient.
		int healCooldown = this.getHealCooldown();

		// This is now done slightly differently because isPotionActive doesn't work on client here, meaning that when
		// affected with arcane jammer and healCooldown == 0, whilst the wizard didn't actually heal or play the sound,
		// the particles still spawned, and since healCooldown wasn't reset they spawned every tick until the arcane
		// jammer wore off.
		if(healCooldown == 0 && this.getHealth() < this.getMaxHealth() && this.getHealth() > 0 && !this.isPotionActive(WizardryPotions.arcane_jammer)){

			// Healer wizards use greater heal.
			this.heal(this.getElement() == Element.HEALING ? 8 : 4);
			this.setHealCooldown(-1);

		// deathTime == 0 checks the wizard isn't currently dying
		}else if(healCooldown == -1 && this.deathTime == 0){

			// Heal particles
			if(worldObj.isRemote){
				for(int i=0; i<10; i++){
					double d0 = (double)((float)this.posX + rand.nextFloat()*2 - 1.0F);
					// Apparently the client side spawns the particles 1 block higher than it should... hence the - 0.5F.
					double d1 = (double)((float)this.posY - 0.5F + rand.nextFloat());
					double d2 = (double)((float)this.posZ + rand.nextFloat()*2 - 1.0F);
					Wizardry.proxy.spawnParticle(WizardryParticleType.SPARKLE, worldObj, d0, d1, d2, 0, 0.1F, 0, 48 + rand.nextInt(12), 1.0f, 1.0f, 0.3f);
				}
			}else{
				if(this.getHealth() < 10){
					// Wizard heals himself more often if he has low health
					this.setHealCooldown(150);
				}else{
					this.setHealCooldown(400);
				}

				this.playSound(WizardrySounds.SPELL_HEAL, 0.7F, rand.nextFloat() * 0.4F + 1.0F);
			}
		}
		
		if(healCooldown > 0){
			this.setHealCooldown(healCooldown-1);
		}
	}

	@Override
	protected void updateAITasks(){

		if(!this.isTrading() && this.timeUntilReset > 0){

			--this.timeUntilReset;

			if(this.timeUntilReset <= 0){

				if(this.updateRecipes){

					for(MerchantRecipe merchantrecipe : this.trades){

						if(merchantrecipe.isRecipeDisabled()){
							// Increases the number of allowed uses of a disabled recipe by a random number.
							merchantrecipe.increaseMaxTradeUses(this.rand.nextInt(6) + this.rand.nextInt(6) + 2);
						}
					}

					if(this.trades.size() < 12){
						this.addRandomRecipes(1);
					}

					this.updateRecipes = false;
				}

				this.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 200, 0));
			}
		}

		// Super call removed because EntityVillager's version does things I don't want and the next one up is
		// in EntityLivingBase and does nothing.
	}

	@Override
	public boolean processInteract(EntityPlayer player, EnumHand hand, ItemStack stack) {

		// Debugging
		//player.addChatComponentMessage(new TextComponentTranslation("wizard.debug", Spell.get(spells[1]).getDisplayName(), Spell.get(spells[2]).getDisplayName(), Spell.get(spells[3]).getDisplayName()));

		// When right-clicked with a spell book in creative, sets one of the spells to that spell
		if(player.capabilities.isCreativeMode && stack != null && stack.getItem() instanceof ItemSpellBook){
			if(this.spells.size() >= 4 && Spell.get(stack.getItemDamage()).canBeCastByNPCs()){
				this.spells.set(rand.nextInt(3)+1, Spell.get(stack.getItemDamage()));
				return true;
			}
		}

		// Won't trade with a player that has attacked them.
		if (this.isEntityAlive() && !this.isTrading() && !this.isChild() && !player.isSneaking() && this.getAttackTarget() != player)
		{
			if (!this.worldObj.isRemote)
			{
				this.setCustomer(player);
				player.displayVillagerTradeGui(this);
				//player.displayGUIMerchant(this, this.getElement().getWizardName());
			}

			return true;
		}
		else
		{
			return false;
		}
	}

	@Override
	public ITextComponent getDisplayName() {
		return this.getElement().getWizardName();
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt){
		
		super.writeEntityToNBT(nbt);
		
		if (this.trades != null){
            nbt.setTag("trades", this.trades.getRecipiesAsTags());
        }

		nbt.setInteger("element", this.getElement().ordinal());
		nbt.setInteger("skin", this.textureIndex);
		nbt.setTag("spells", WizardryUtilities.listToNBT(spells, spell -> new NBTTagInt(spell.id())));

		if(this.towerBlocks != null && this.towerBlocks.size() > 0){
			nbt.setTag("towerBlocks", WizardryUtilities.listToNBT(this.towerBlocks, pos -> new NBTTagLong(pos.toLong())));
		}
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt){
		
		super.readEntityFromNBT(nbt);
		
		if(nbt.hasKey("trades")){
            NBTTagCompound nbttagcompound1 = nbt.getCompoundTag("trades");
            this.trades = new MerchantRecipeList(nbttagcompound1);
        }

		this.setElement(Element.values()[nbt.getInteger("element")]);
		this.textureIndex = nbt.getInteger("skin");
		this.spells = (List<Spell>) WizardryUtilities.NBTToList(nbt.getTagList("spells", NBT.TAG_INT),
				(NBTTagInt tag) -> Spell.get(tag.getInt()));

		this.towerBlocks = new HashSet<BlockPos>(WizardryUtilities.NBTToList(nbt.getTagList("towerBlocks",
				NBT.TAG_LONG), (NBTTagLong tag) -> BlockPos.fromLong(tag.getLong())));
	}

	@Override
	protected boolean canDespawn(){
		return false;
	}

	@Override
	public boolean isTrading()
	{
		return this.getCustomer() != null;
	}

	@Override
	public void useRecipe(MerchantRecipe merchantrecipe){

		merchantrecipe.incrementToolUses();
		this.livingSoundTime = -this.getTalkInterval();
		this.playSound(SoundEvents.ENTITY_VILLAGER_YES, this.getSoundVolume(), this.getSoundPitch());

		// Achievements
		if (this.getCustomer() != null)
		{
			this.getCustomer().addStat(WizardryAchievements.wizard_trade);

			if(merchantrecipe.getItemToSell().getItem() instanceof ItemSpellBook
					&& Spell.get(merchantrecipe.getItemToSell().getItemDamage()).tier == Tier.MASTER){
				this.getCustomer().addStat(WizardryAchievements.buy_master_spell);
			}
		}

		// Changed to a 4 in 5 chance of unlocking a new recipe.
		if(this.rand.nextInt(5) > 0){
			this.timeUntilReset = 40;
			this.updateRecipes = true;

			if (this.getCustomer() != null)
			{
				this.getCustomer().getName();
			}
			else
			{
			}
		}
	}

	// This is called from the gui in order to display the recipes (no surprise there), and this is actually where
	// the initialisation is done, i.e. the trades don't actually exist until some player goes to trade with the
	// villager, at which point the first is added.
	@Override
	public MerchantRecipeList getRecipes(EntityPlayer par1EntityPlayer){

		if(this.trades == null){

			this.trades = new MerchantRecipeList();

			// All wizards will buy spell books
			ItemStack anySpellBook = new ItemStack(WizardryItems.spell_book, 1, OreDictionary.WILDCARD_VALUE);
			ItemStack crystalStack = new ItemStack(WizardryItems.magic_crystal, 5);

			// NOTE: For wizardry 1.2, increase the number of uses of this trade. The default is 7, for reference.
			this.trades.add(new MerchantRecipe(anySpellBook, crystalStack));

			this.addRandomRecipes(3);
		}

		return this.trades;
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

			Tier tier = Tier.BASIC;

			while(itemAlreadySold){

				itemAlreadySold = false;

				/* New way of getting random item, by giving a chance to increase the tier which depends on how much the
				 * player has already traded with the wizard. The more the player has traded with the wizard, the more
				 * likely they are to get items of a higher tier. The -4 is to ignore the original 4 trades.
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

				double tierIncreaseChance = 0.5 + 0.04*(Math.max(this.trades.size()-4, 0));

				tier = Tier.BASIC;

				if(rand.nextDouble() < tierIncreaseChance){
					tier = Tier.APPRENTICE;
					if(rand.nextDouble() < tierIncreaseChance){
						tier = Tier.ADVANCED;
						if(rand.nextDouble() < tierIncreaseChance*0.6){
							tier = Tier.MASTER;
						}
					}
				}

				itemToSell = this.getRandomItemOfTier(tier);

				for(Object recipe : merchantrecipelist){
					if(ItemStack.areItemStacksEqual(((MerchantRecipe)recipe).getItemToSell(), itemToSell)) itemAlreadySold = true;
				}

				if(this.trades != null){
					for(Object recipe : this.trades){
						if(ItemStack.areItemStacksEqual(((MerchantRecipe)recipe).getItemToSell(), itemToSell)) itemAlreadySold = true;
					}
				}
			}

			// Don't know how it can ever be null here, but saves it crashing.
			if(itemToSell == null) return;

			merchantrecipelist.add(new MerchantRecipe(this.getRandomPrice(tier), new ItemStack(WizardryItems.magic_crystal, tier.ordinal()*3 + 1 + rand.nextInt(4)), itemToSell));
		}

		Collections.shuffle(merchantrecipelist);

		if (this.trades == null)
		{
			this.trades = new MerchantRecipeList();
		}

		for (int j1 = 0; j1 < merchantrecipelist.size(); ++j1)
		{
			this.trades.add(merchantrecipelist.get(j1));
		}
	}

	private ItemStack getRandomPrice(Tier tier) {
		ItemStack itemstack = null;
		switch(this.rand.nextInt(3)){
		case 0:
			itemstack = new ItemStack(Items.GOLD_INGOT, (tier.ordinal()+1)*8-1 + rand.nextInt(6));
			break;
		case 1:
			itemstack = new ItemStack(Items.DIAMOND, (tier.ordinal()+1)*4-2 + rand.nextInt(3));
			break;
		case 2:
			itemstack = new ItemStack(Items.EMERALD, (tier.ordinal()+1)*6-1 + rand.nextInt(3));
			break;
		}
		return itemstack;
	}

	private ItemStack getRandomItemOfTier(Tier tier){

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
				if(this.getElement() != Element.MAGIC && rand.nextInt(4) > 0 && !specialismSpells.isEmpty()){
					// This means it is more likely for spell books sold to be of the same element as the wizard if the wizard has an element.
					return new ItemStack(WizardryItems.spell_book, 1, specialismSpells.get(rand.nextInt(specialismSpells.size())).id());
				}else{
					return new ItemStack(WizardryItems.spell_book, 1, spells.get(rand.nextInt(spells.size())).id());
				}
			}else{
				if(this.getElement() != Element.MAGIC && rand.nextInt(4) > 0){
					// This means it is more likely for wands sold to be of the same element as the wizard if the wizard has an element.
					return new ItemStack(WizardryUtilities.getWand(tier, this.getElement()));
				}else{
					return new ItemStack(WizardryUtilities.getWand(tier, Element.values()[rand.nextInt(Element.values().length)]));
				}
			}

		case APPRENTICE:
			randomiser = rand.nextInt(Wizardry.settings.discoveryMode ? 12 : 10);
			if(randomiser < 5 && !spells.isEmpty()){
				if(this.getElement() != Element.MAGIC && rand.nextInt(4) > 0 && !specialismSpells.isEmpty()){
					// This means it is more likely for spell books sold to be of the same element as the wizard if the wizard has an element.
					return new ItemStack(WizardryItems.spell_book, 1, specialismSpells.get(rand.nextInt(specialismSpells.size())).id());
				}else{
					return new ItemStack(WizardryItems.spell_book, 1, spells.get(rand.nextInt(spells.size())).id());
				}
			}else if(randomiser < 6){
				if(this.getElement() != Element.MAGIC && rand.nextInt(4) > 0){
					// This means it is more likely for wands sold to be of the same element as the wizard if the wizard has an element.
					return new ItemStack(WizardryUtilities.getWand(tier, this.getElement()));
				}else{
					return new ItemStack(WizardryUtilities.getWand(tier, Element.values()[rand.nextInt(Element.values().length)]));
				}
			}else if(randomiser < 8){
				return new ItemStack(WizardryItems.arcane_tome, 1, 1);
			}else if(randomiser < 10){
				EntityEquipmentSlot slot = WizardryUtilities.ARMOUR_SLOTS[rand.nextInt(WizardryUtilities.ARMOUR_SLOTS.length)];
				if(this.getElement() != Element.MAGIC && rand.nextInt(4) > 0){
					// This means it is more likely for armour sold to be of the same element as the wizard if the wizard has an element.
					return new ItemStack(WizardryUtilities.getArmour(this.getElement(), slot));
				}else{
					return new ItemStack(WizardryUtilities.getArmour(Element.values()[rand.nextInt(Element.values().length)], slot));
				}
			}else{
				// Don't need to check for discovery mode here since it is done above
				return new ItemStack(WizardryItems.identification_scroll);
			}

		case ADVANCED:
			randomiser = rand.nextInt(12);
			if(randomiser < 5 && !spells.isEmpty()){
				if(this.getElement() != Element.MAGIC && rand.nextInt(4) > 0 && !specialismSpells.isEmpty()){
					// This means it is more likely for spell books sold to be of the same element as the wizard if the wizard has an element.
					return new ItemStack(WizardryItems.spell_book, 1, specialismSpells.get(rand.nextInt(specialismSpells.size())).id());
				}else{
					return new ItemStack(WizardryItems.spell_book, 1, spells.get(rand.nextInt(spells.size())).id());
				}
			}else if(randomiser < 6){
				if(this.getElement() != Element.MAGIC && rand.nextInt(4) > 0){
					// This means it is more likely for wands sold to be of the same element as the wizard if the wizard has an element.
					return new ItemStack(WizardryUtilities.getWand(tier, this.getElement()));
				}else{
					return new ItemStack(WizardryUtilities.getWand(tier, Element.values()[rand.nextInt(Element.values().length)]));
				}
			}else if(randomiser < 8){
				return new ItemStack(WizardryItems.arcane_tome, 1, 2);
			}else{
				List<Item> upgrades = new ArrayList<Item>(WandHelper.getSpecialUpgrades());
				randomiser = rand.nextInt(upgrades.size());
				return new ItemStack(upgrades.get(randomiser));
			}

		case MASTER:
			// If a regular wizard rolls a master trade, it can only be a simple master wand or a tome of arcana
			randomiser = this.getElement() != Element.MAGIC ? rand.nextInt(8) : 5 + rand.nextInt(3);

			if(randomiser < 5 && this.getElement() != Element.MAGIC && !specialismSpells.isEmpty()){
				// Master spells can only be sold by a specialist in that element.
				return new ItemStack(WizardryItems.spell_book, 1, specialismSpells.get(rand.nextInt(specialismSpells.size())).id());

			}else if(randomiser < 6){
				if(this.getElement() != Element.MAGIC && rand.nextInt(4) > 0){
					// Master elemental wands can only be sold by a specialist in that element.
					return new ItemStack(WizardryUtilities.getWand(tier, this.getElement()));
				}else{
					return new ItemStack(WizardryItems.master_wand);
				}
			}else{
				return new ItemStack(WizardryItems.arcane_tome, 1, 3);
			}
		}

		return new ItemStack(Blocks.STONE);
	}
	
	@Override
	public void setProfession(VillagerProfession prof) {
		// Disables Forge's stuff.
	}

	@Override
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData livingdata){

		livingdata = super.onInitialSpawn(difficulty, livingdata);

		textureIndex = this.rand.nextInt(6);

		if(rand.nextBoolean()){
			this.setElement(Element.values()[rand.nextInt(Element.values().length - 1) + 1]);
		}else{
			this.setElement(Element.MAGIC);
		}

		Element element = this.getElement();

		// Adds armour.
		for(EntityEquipmentSlot slot : WizardryUtilities.ARMOUR_SLOTS){
			this.setItemStackToSlot(slot, new ItemStack(WizardryUtilities.getArmour(element, slot)));
		}

		// Default chance is 0.085f, for reference.
		for(EntityEquipmentSlot slot : EntityEquipmentSlot.values()) this.setDropChance(slot, 0.0f);

		// All wizards know magic missile, even if it is disabled.
		spells.add(Spells.magic_missile);
		
		Tier maxTier = populateSpells(spells, element, 3, rand);

		// Now done after the spells so it can take the tier into account.
		this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(WizardryUtilities.getWand(maxTier, element)));

		return livingdata;
	}
	
	/**
	 * Adds n random spells to the given list. The spells will be of the given element if possible. Extracted as a
	 * separate function since it was the same in both EntityWizard and EntityEvilWizard.
	 * @param spells The spell list to be populated.
	 * @param e The element that the spells should belong to, or {@link Element#MAGIC} for a random element each time.
	 * @param n The number of spells to add.
	 * @param random A random number generator to use.
	 * @return The tier of the highest-tier spell that was added to the list.
	 */
	static Tier populateSpells(List<Spell> spells, Element e, int n, Random random){

		// This is the tier of the highest tier spell added.
		Tier maxTier = Tier.BASIC;
		
		List<Spell> npcSpells = Spell.getSpells(Spell.npcSpells);

		for(int i=0; i<3; i++){

			Tier tier;
			// If the wizard has no element, it picks a random one each time.
			Element element = e == Element.MAGIC ? Element.values()[random.nextInt(Element.values().length)] : e;

			int randomiser = random.nextInt(20);

			// Uses its own special weighting
			if(randomiser < 10){
				tier = Tier.BASIC;
			}else if(randomiser < 16){
				tier = Tier.APPRENTICE;
			}else{
				tier = Tier.ADVANCED;
			}

			if(tier.ordinal() > maxTier.ordinal()) maxTier = tier;

			// Finds all the spells of the chosen tier and element
			List<Spell> list = Spell.getSpells(new Spell.TierElementFilter(tier, element));
			// Keeps only spells which can be cast by NPCs
			list.retainAll(npcSpells);
			// Removes spells that the wizard already has
			list.removeAll(spells);

			// Ensures the tier chosen actually has spells in it. (isEmpty() is exactly the same as size() == 0)
			if(list.isEmpty()){
				// If there are no spells applicable, tier and element restrictions are removed to give maximum
				// possibility of there being an applicable spell.
				list = npcSpells;
				// Removes spells that the wizard already has
				list.removeAll(spells);
			}

			// If the list is still empty now, there must be less than 3 enabled spells that can be cast by wizards
			// (excluding magic missile). In this case, having empty slots seems reasonable.
			if(!list.isEmpty()) spells.add(list.get(random.nextInt(list.size())));

		}
		
		return maxTier;
	}

	@Override
	public void writeSpawnData(ByteBuf data){
		data.writeInt(textureIndex);
	}

	@Override
	public void readSpawnData(ByteBuf data){
		textureIndex = data.readInt();
	}

	@Override
	public boolean attackEntityFrom(DamageSource source, float damage){

		if(source.getEntity() instanceof EntityPlayer){
			((EntityPlayer)source.getEntity()).addStat(WizardryAchievements.anger_wizard);
		}

		return super.attackEntityFrom(source, damage);
	}

	/** 
	 * Sets the list of blocks that are part of this wizard's tower. If a player breaks any of these blocks, the wizard
	 * will get angry and attack them.
	 * @param blocks A Set of BlockPos objects representing the blocks in the tower.
	 */
	public void setTowerBlocks(Set<BlockPos> blocks){
		this.towerBlocks = blocks;
	}

	/**
	 * Tests whether the block at the given coordinates is part of this wizard's tower.
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public boolean isBlockPartOfTower(BlockPos pos){
		if(this.towerBlocks == null) return false;
		// Uses .equals() rather than == so this will work fine.
		return this.towerBlocks.contains(pos);
	}

	// EntityVillager overrides (that don't add features)

	@Override public boolean isMating(){ return false; }
	@Override public void setMating(boolean p_70947_1_){}
	@Override public void setPlaying(boolean p_70939_1_){}
	@Override public boolean isPlaying(){ return false; }
	@Override public void setLookingForHome(){}
	// Doesn't say it, but this is in fact nullable.
	@Override public EntityVillager createChild(EntityAgeable par1EntityAgeable){ return null; }
	@SideOnly(Side.CLIENT)
	@Override public void setRecipes(MerchantRecipeList par1MerchantRecipeList){}
	@Override
	public void onStruckByLightning(EntityLightningBolt lightningBolt){
		// Restores the normal behaviour, replacing EntityVillager's witch conversion.
		this.attackEntityFrom(DamageSource.lightningBolt, 5.0F);
		// Entity's version does something strange with the private fire variable, but since I don't have access this
		// will probably be fine.
        this.setFire(8);
	}

}
