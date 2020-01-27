package electroblob.wizardry.entity.living;

import com.google.common.base.Predicate;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.event.DiscoverSpellEvent;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.item.ItemSpellBook;
import electroblob.wizardry.misc.WildcardTradeList;
import electroblob.wizardry.registry.*;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.*;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nullable;
import java.util.*;

@Mod.EventBusSubscriber
public class EntityWizard extends EntityCreature implements INpc, IMerchant, ISpellCaster, IEntityAdditionalSpawnData {

	private EntityAIAttackSpell<EntityWizard> spellCastingAI = new EntityAIAttackSpell<>(this, 0.5D, 14.0F, 30, 50);

	public int textureIndex = 0;

	/** The entity selector passed into the new AI methods. */
	protected Predicate<Entity> targetSelector;

	/** The wizard's trades. */
	private MerchantRecipeList trades;
    /** The wizard's current customer. */
    @Nullable
    private EntityPlayer customer;
    
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
	private int spellCounter;

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
		// Why would you go to the effort of making the IMerchant interface and then have the AI classes only accept
		// EntityVillager?
		this.tasks.addTask(1, new EntityAITradePlayer(this));
		this.tasks.addTask(1, new EntityAILookAtTradePlayer(this));
		this.tasks.addTask(4, new EntityAIRestrictOpenDoor(this));
		this.tasks.addTask(5, new EntityAIOpenDoor(this, true));
		this.tasks.addTask(6, new EntityAIMoveTowardsRestriction(this, 0.6D));
		this.tasks.addTask(7, new EntityAIWatchClosest2(this, EntityPlayer.class, 3.0F, 1.0F));
		this.tasks.addTask(7, new EntityAIWatchClosest2(this, EntityWizard.class, 5.0F, 0.02F));
		this.tasks.addTask(7, new EntityAIWander(this, 0.6D));
		this.tasks.addTask(8, new EntityAIWatchClosest(this, EntityLiving.class, 8.0F));

		this.targetSelector = entity -> {

			// If the target is valid and not invisible...
			if(entity != null && !entity.isInvisible()
					&& AllyDesignationSystem.isValidTarget(EntityWizard.this, entity)){

				// ... and is a mob, a summoned creature ...
				if((entity instanceof IMob || entity instanceof ISummonedCreature
						// ... or in the whitelist ...
						|| Arrays.asList(Wizardry.settings.summonedCreatureTargetsWhitelist)
								.contains(EntityList.getKey(entity.getClass())))
						// ... and isn't in the blacklist ...
						&& !Arrays.asList(Wizardry.settings.summonedCreatureTargetsBlacklist)
								.contains(EntityList.getKey(entity.getClass()))){
					// ... it can be attacked.
					return true;
				}
			}

			return false;
		};

		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
		// By default, wizards don't attack players unless the player has attacked them.
		this.targetTasks.addTask(0, new EntityAINearestAttackableTarget<EntityLiving>(this, EntityLiving.class, 0,
				false, true, this.targetSelector));
	}

	@Override
	protected void applyEntityAttributes(){
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.5);
		this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30);
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
	public void setSpellCounter(int count){
		spellCounter = count;
	}

	@Override
	public int getSpellCounter(){
		return spellCounter;
	}

	@Override
	public int getAimingError(EnumDifficulty difficulty){
		// Being more intelligent than skeletons, wizards are a little more accurate.
		switch(difficulty){
		case EASY: return 7;
		case NORMAL: return 4;
		case HARD: return 1;
		default: return 7; // Peaceful counts as easy
		}
	}

	@Override
	public void setCustomer(EntityPlayer player){
		this.customer = player;
	}

	@Override
	public EntityPlayer getCustomer(){
		return this.customer;
	}

	public boolean isTrading(){
		return this.getCustomer() != null;
	}

	@Override
	public void verifySellingItem(ItemStack stack){
		// Copied from EntityVillager
		if(!this.world.isRemote && this.livingSoundTime > -this.getTalkInterval() + 20){
            this.livingSoundTime = -this.getTalkInterval();
            SoundEvent yes = Wizardry.tisTheSeason ? WizardrySounds.ENTITY_WIZARD_HOHOHO : WizardrySounds.ENTITY_WIZARD_YES;
            this.playSound(stack.isEmpty() ? WizardrySounds.ENTITY_WIZARD_NO : yes, this.getSoundVolume(), this.getSoundPitch());
        }
	}

	@Override
	public World getWorld(){
		return this.world;
	}

	@Override
	public BlockPos getPos(){
        return new BlockPos(this);
    }

	@Override
	@SideOnly(Side.CLIENT)
	public void setRecipes(MerchantRecipeList recipeList){
		// Apparently nothing goes here, and nothing's here in EntityVillager either...
	}

	// TESTME: Should this be getName instead?
	@Override
	public ITextComponent getDisplayName(){
		
		if(this.hasCustomName()){
            return super.getDisplayName();
        }
		
		return this.getElement().getWizardName();
	}

	@Override
	protected boolean canDespawn(){
		return false;
	}
	
	@Override
	protected SoundEvent getAmbientSound(){
		if(Wizardry.tisTheSeason) return WizardrySounds.ENTITY_WIZARD_HOHOHO;
		return this.isTrading() ? WizardrySounds.ENTITY_WIZARD_TRADING : WizardrySounds.ENTITY_WIZARD_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source){
		return WizardrySounds.ENTITY_WIZARD_HURT;
	}

	@Override
	protected SoundEvent getDeathSound(){
		return WizardrySounds.ENTITY_WIZARD_DEATH;
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
		if(healCooldown == 0 && this.getHealth() < this.getMaxHealth() && this.getHealth() > 0
				&& !this.isPotionActive(WizardryPotions.arcane_jammer)){

			// Healer wizards use greater heal.
			this.heal(this.getElement() == Element.HEALING ? 8 : 4);
			this.setHealCooldown(-1);

			// deathTime == 0 checks the wizard isn't currently dying
		}else if(healCooldown == -1 && this.deathTime == 0){

			// Heal particles TODO: Change this so it uses the heal spell directly
			if(world.isRemote){
				ParticleBuilder.spawnHealParticles(world, this);
			}else{
				if(this.getHealth() < 10){
					// Wizards heal themseselves more often if they have low health
					this.setHealCooldown(150);
				}else{
					this.setHealCooldown(400);
				}

				this.playSound(Spells.heal.getSounds()[0], 0.7F, rand.nextFloat() * 0.4F + 1.0F);
			}
		}

		if(healCooldown > 0){
			this.setHealCooldown(healCooldown - 1);
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

		super.updateAITasks(); // This actually does nothing
	}

	@Override
	public boolean processInteract(EntityPlayer player, EnumHand hand){

		ItemStack stack = player.getHeldItem(hand);

		// Debugging
		// player.addChatComponentMessage(new TextComponentTranslation("wizard.debug",
		// Spell.get(spells[1]).getDisplayName(), Spell.get(spells[2]).getDisplayName(),
		// Spell.get(spells[3]).getDisplayName()));

		// When right-clicked with a spell book in creative, sets one of the spells to that spell
		if(player.isCreative() && stack.getItem() instanceof ItemSpellBook){
			Spell spell = Spell.byMetadata(stack.getItemDamage());
			if(this.spells.size() >= 4 && spell.canBeCastBy(this, true)){
				// The set(...) method returns the element that was replaced - neat!
				player.sendMessage(new TextComponentTranslation("item." + Wizardry.MODID + ":spell_book.apply_to_wizard",
						this.getDisplayName(), this.spells.set(rand.nextInt(3) + 1, spell).getNameForTranslationFormatted(),
						spell.getNameForTranslationFormatted()));
				return true;
			}
		}

		// Won't trade with a player that has attacked them.
		if(this.isEntityAlive() && !this.isTrading() && !this.isChild() && !player.isSneaking()
				&& this.getAttackTarget() != player){
			if(!this.world.isRemote){
				this.setCustomer(player);
				player.displayVillagerTradeGui(this);
				// player.displayGUIMerchant(this, this.getElement().getWizardName());
			}

			return true;
		}else{
			return false;
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt){

		super.writeEntityToNBT(nbt);

		if(this.trades != null){
			NBTExtras.storeTagSafely(nbt, "trades", this.trades.getRecipiesAsTags());
		}

		nbt.setInteger("element", this.getElement().ordinal());
		nbt.setInteger("skin", this.textureIndex);
		NBTExtras.storeTagSafely(nbt, "spells", NBTExtras.listToNBT(spells, spell -> new NBTTagInt(spell.metadata())));

		if(this.towerBlocks != null && this.towerBlocks.size() > 0){
			NBTExtras.storeTagSafely(nbt, "towerBlocks", NBTExtras.listToNBT(this.towerBlocks, NBTUtil::createPosTag));
		}
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt){

		super.readEntityFromNBT(nbt);

		if(nbt.hasKey("trades")){
			NBTTagCompound nbttagcompound1 = nbt.getCompoundTag("trades");
			this.trades = new WildcardTradeList(nbttagcompound1);
		}

		this.setElement(Element.values()[nbt.getInteger("element")]);
		this.textureIndex = nbt.getInteger("skin");
		this.spells = (List<Spell>)NBTExtras.NBTToList(nbt.getTagList("spells", NBT.TAG_INT),
				(NBTTagInt tag) -> Spell.byMetadata(tag.getInt()));

		NBTTagList tagList = nbt.getTagList("towerBlocks", NBT.TAG_COMPOUND);
		if(!tagList.isEmpty()){
			this.towerBlocks = new HashSet<>(NBTExtras.NBTToList(tagList, NBTUtil::getPosFromTag));
		}else{
			// Fallback to old packed long format
			this.towerBlocks = new HashSet<>(NBTExtras.NBTToList(nbt.getTagList("towerBlocks", NBT.TAG_LONG),
					(NBTTagLong tag) -> BlockPos.fromLong(tag.getLong())));
		}
	}

	@Override
	public void useRecipe(MerchantRecipe merchantrecipe){

		merchantrecipe.incrementToolUses();
		this.livingSoundTime = -this.getTalkInterval();
		this.playSound(WizardrySounds.ENTITY_WIZARD_YES, this.getSoundVolume(), this.getSoundPitch());

		if(this.getCustomer() != null){

			// Achievements
			WizardryAdvancementTriggers.wizard_trade.triggerFor(this.getCustomer());

			if(merchantrecipe.getItemToSell().getItem() instanceof ItemSpellBook){

				Spell spell = Spell.byMetadata(merchantrecipe.getItemToSell().getItemDamage());

				if(spell.getTier() == Tier.MASTER) WizardryAdvancementTriggers.buy_master_spell.triggerFor(this.getCustomer());

				// Spell discovery (a lot of this is the same as in the event handler)
				WizardData data = WizardData.get(this.getCustomer());

				if(data != null){

					if(!MinecraftForge.EVENT_BUS.post(new DiscoverSpellEvent(this.getCustomer(), spell,
							DiscoverSpellEvent.Source.PURCHASE)) && data.discoverSpell(spell)){

						data.sync();

						if(!world.isRemote && !this.getCustomer().isCreative() && Wizardry.settings.discoveryMode){
							// Sound and text only happen server-side, in survival, with discovery mode on
							WizardryUtilities.playSoundAtPlayer(this.getCustomer(), WizardrySounds.MISC_DISCOVER_SPELL, 1.25f, 1);
							this.getCustomer().sendMessage(new TextComponentTranslation("spell.discover",
									spell.getNameForTranslationFormatted()));
						}
					}
				}
			}
		}

		// Changed to a 4 in 5 chance of unlocking a new recipe.
		if(this.rand.nextInt(5) > 0 || ItemArtefact.isArtefactActive(customer, WizardryItems.charm_haggler)){
			this.timeUntilReset = 40;
			this.updateRecipes = true;

			if(this.getCustomer() != null){
				this.getCustomer().getName();
			}else{
			}
		}
	}

	// This is called from the gui in order to display the recipes (no surprise there), and this is actually where
	// the initialisation is done, i.e. the trades don't actually exist until some player goes to trade with the
	// villager, at which point the first is added.
	@Override
	public MerchantRecipeList getRecipes(EntityPlayer par1EntityPlayer){

		if(this.trades == null){

			this.trades = new WildcardTradeList();

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

		for(int i = 0; i < numberOfItemsToAdd; i++){

			ItemStack itemToSell = ItemStack.EMPTY;

			boolean itemAlreadySold = true;

			Tier tier = Tier.NOVICE;

			while(itemAlreadySold){

				itemAlreadySold = false;

				/* New way of getting random item, by giving a chance to increase the tier which depends on how much the
				 * player has already traded with the wizard. The more the player has traded with the wizard, the more
				 * likely they are to get items of a higher tier. The -4 is to ignore the original 4 trades. For
				 * reference, the chances are as follows: Trades done Basic Apprentice Advanced Master 0 50% 25% 18% 8%
				 * 1 46% 25% 20% 9% 2 42% 24% 22% 12% 3 38% 24% 24% 14% 4 34% 22% 26% 17% 5 30% 21% 28% 21% 6 26% 19%
				 * 30% 24% 7 22% 17% 32% 28% 8 18% 15% 34% 33% */

				double tierIncreaseChance = 0.5 + 0.04 * (Math.max(this.trades.size() - 4, 0));

				tier = Tier.NOVICE;

				if(rand.nextDouble() < tierIncreaseChance){
					tier = Tier.APPRENTICE;
					if(rand.nextDouble() < tierIncreaseChance){
						tier = Tier.ADVANCED;
						if(rand.nextDouble() < tierIncreaseChance * 0.6){
							tier = Tier.MASTER;
						}
					}
				}

				itemToSell = this.getRandomItemOfTier(tier);

				for(Object recipe : merchantrecipelist){
					if(ItemStack.areItemStacksEqual(((MerchantRecipe)recipe).getItemToSell(), itemToSell))
						itemAlreadySold = true;
				}

				if(this.trades != null){
					for(Object recipe : this.trades){
						if(ItemStack.areItemStacksEqual(((MerchantRecipe)recipe).getItemToSell(), itemToSell))
							itemAlreadySold = true;
					}
				}
			}

			// Don't know how it can ever be empty here, but it's a failsafe.
			if(itemToSell.isEmpty()) return;

			ItemStack secondItemToBuy = tier == Tier.MASTER ? new ItemStack(WizardryItems.astral_diamond)
					: new ItemStack(WizardryItems.magic_crystal, tier.ordinal() * 3 + 1 + rand.nextInt(4));

			merchantrecipelist.add(new MerchantRecipe(this.getRandomPrice(tier), secondItemToBuy, itemToSell));
		}

		Collections.shuffle(merchantrecipelist);

		if(this.trades == null){
			this.trades = new WildcardTradeList();
		}

		this.trades.addAll(merchantrecipelist);
	}

	// TODO: Switch all of this over to some kind of loot pool system?

	private ItemStack getRandomPrice(Tier tier){

		Map<ResourceLocation, Integer> map = Wizardry.settings.currencyItems;
		// This isn't that efficient but it's not called very often really so it doesn't matter
		ResourceLocation itemName = map.keySet().toArray(new ResourceLocation[0])[rand.nextInt(map.size())];
		Item item = Item.REGISTRY.getObject(itemName);
		int value;

		if(item == null){
			Wizardry.logger.warn("Invalid item in currency items: {}", itemName);
			item = Items.EMERALD; // Fallback item
			value = 6;
		}else{
			value = map.get(itemName);
		}

		// ((tier.ordinal() + 1) * 16 + rand.nextInt(6)) gives a 'value' for the item being bought
		// This is then divided by the value of the currency item to give a price
		// The absolute maximum stack size that can result from this calculation (with value = 1) is 64.
		return new ItemStack(item, (8 + tier.ordinal() * 16 + rand.nextInt(9)) / value);
	}

	private ItemStack getRandomItemOfTier(Tier tier){

		int randomiser;

		// All enabled spells of the given tier
		List<Spell> spells = Spell.getSpells(new Spell.TierElementFilter(tier, null, SpellProperties.Context.TRADES));
		// All enabled spells of the given tier that match this wizard's element
		List<Spell> specialismSpells = Spell.getSpells(new Spell.TierElementFilter(tier, this.getElement(), SpellProperties.Context.TRADES));

		// Wizards don't sell scrolls
		spells.removeIf(s -> !s.isEnabled(SpellProperties.Context.BOOK));
		specialismSpells.removeIf(s -> !s.isEnabled(SpellProperties.Context.BOOK));

		// This code is sooooooo much neater with the new filter system!
		switch(tier){

		case NOVICE:
			randomiser = rand.nextInt(5);
			if(randomiser < 4 && !spells.isEmpty()){
				if(this.getElement() != Element.MAGIC && rand.nextInt(4) > 0 && !specialismSpells.isEmpty()){
					// This means it is more likely for spell books sold to be of the same element as the wizard if the
					// wizard has an element.
					return new ItemStack(WizardryItems.spell_book, 1,
							specialismSpells.get(rand.nextInt(specialismSpells.size())).metadata());
				}else{
					return new ItemStack(WizardryItems.spell_book, 1, spells.get(rand.nextInt(spells.size())).metadata());
				}
			}else{
				if(this.getElement() != Element.MAGIC && rand.nextInt(4) > 0){
					// This means it is more likely for wands sold to be of the same element as the wizard if the wizard
					// has an element.
					return new ItemStack(WizardryItems.getWand(tier, this.getElement()));
				}else{
					return new ItemStack(
							WizardryItems.getWand(tier, Element.values()[rand.nextInt(Element.values().length)]));
				}
			}

		case APPRENTICE:
			randomiser = rand.nextInt(Wizardry.settings.discoveryMode ? 12 : 10);
			if(randomiser < 5 && !spells.isEmpty()){
				if(this.getElement() != Element.MAGIC && rand.nextInt(4) > 0 && !specialismSpells.isEmpty()){
					// This means it is more likely for spell books sold to be of the same element as the wizard if the
					// wizard has an element.
					return new ItemStack(WizardryItems.spell_book, 1,
							specialismSpells.get(rand.nextInt(specialismSpells.size())).metadata());
				}else{
					return new ItemStack(WizardryItems.spell_book, 1, spells.get(rand.nextInt(spells.size())).metadata());
				}
			}else if(randomiser < 6){
				if(this.getElement() != Element.MAGIC && rand.nextInt(4) > 0){
					// This means it is more likely for wands sold to be of the same element as the wizard if the wizard
					// has an element.
					return new ItemStack(WizardryItems.getWand(tier, this.getElement()));
				}else{
					return new ItemStack(
							WizardryItems.getWand(tier, Element.values()[rand.nextInt(Element.values().length)]));
				}
			}else if(randomiser < 8){
				return new ItemStack(WizardryItems.arcane_tome, 1, 1);
			}else if(randomiser < 10){
				EntityEquipmentSlot slot = WizardryUtilities.ARMOUR_SLOTS[rand.nextInt(WizardryUtilities.ARMOUR_SLOTS.length)];
				if(this.getElement() != Element.MAGIC && rand.nextInt(4) > 0){
					// This means it is more likely for armour sold to be of the same element as the wizard if the
					// wizard has an element.
					return new ItemStack(WizardryItems.getArmour(this.getElement(), slot));
				}else{
					return new ItemStack(
							WizardryItems.getArmour(Element.values()[rand.nextInt(Element.values().length)], slot));
				}
			}else{
				// Don't need to check for discovery mode here since it is done above
				return new ItemStack(WizardryItems.identification_scroll);
			}

		case ADVANCED:
			randomiser = rand.nextInt(12);
			if(randomiser < 5 && !spells.isEmpty()){
				if(this.getElement() != Element.MAGIC && rand.nextInt(4) > 0 && !specialismSpells.isEmpty()){
					// This means it is more likely for spell books sold to be of the same element as the wizard if the
					// wizard has an element.
					return new ItemStack(WizardryItems.spell_book, 1,
							specialismSpells.get(rand.nextInt(specialismSpells.size())).metadata());
				}else{
					return new ItemStack(WizardryItems.spell_book, 1, spells.get(rand.nextInt(spells.size())).metadata());
				}
			}else if(randomiser < 6){
				if(this.getElement() != Element.MAGIC && rand.nextInt(4) > 0){
					// This means it is more likely for wands sold to be of the same element as the wizard if the wizard
					// has an element.
					return new ItemStack(WizardryItems.getWand(tier, this.getElement()));
				}else{
					return new ItemStack(
							WizardryItems.getWand(tier, Element.values()[rand.nextInt(Element.values().length)]));
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
				return new ItemStack(WizardryItems.spell_book, 1,
						specialismSpells.get(rand.nextInt(specialismSpells.size())).metadata());

			}else if(randomiser < 6){
				if(this.getElement() != Element.MAGIC && rand.nextInt(4) > 0){
					// Master elemental wands can only be sold by a specialist in that element.
					return new ItemStack(WizardryItems.getWand(tier, this.getElement()));
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
			this.setItemStackToSlot(slot, new ItemStack(WizardryItems.getArmour(element, slot)));
		}

		// Default chance is 0.085f, for reference.
		for(EntityEquipmentSlot slot : EntityEquipmentSlot.values())
			this.setDropChance(slot, 0.0f);

		// All wizards know magic missile, even if it is disabled.
		spells.add(Spells.magic_missile);

		Tier maxTier = populateSpells(this, spells, element, false, 3, rand);

		// Now done after the spells so it can take the tier into account.
		ItemStack wand = new ItemStack(WizardryItems.getWand(maxTier, element));
		ArrayList<Spell> list = new ArrayList<>(spells);
		list.add(Spells.heal);
		WandHelper.setSpells(wand, list.toArray(new Spell[5]));
		this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, wand);

		this.setHealCooldown(50);

		return livingdata;
	}

	/**
	 * Adds n random spells to the given list. The spells will be of the given element if possible. Extracted as a
	 * separate function since it was the same in both EntityWizard and EntityEvilWizard.
	 *
	 * @param wizard The wizard whose spells are to be populated.
	 * @param spells The spell list to be populated.
	 * @param e The element that the spells should belong to, or {@link Element#MAGIC} for a random element each time.
	 * @param master Whether to include master spells.
	 * @param n The number of spells to add.
	 * @param random A random number generator to use.
	 * @return The tier of the highest-tier spell that was added to the list.
	 */
	static Tier populateSpells(final EntityLiving wizard, List<Spell> spells, Element e, boolean master, int n, Random random){

		// This is the tier of the highest tier spell added.
		Tier maxTier = Tier.NOVICE;

		List<Spell> npcSpells = Spell.getSpells(s -> s.canBeCastBy(wizard, false));
		npcSpells.removeIf(s -> !s.applicableForItem(WizardryItems.spell_book));

		for(int i = 0; i < n; i++){

			Tier tier;
			// If the wizard has no element, it picks a random one each time.
			Element element = e == Element.MAGIC ? Element.values()[random.nextInt(Element.values().length)] : e;

			int randomiser = random.nextInt(20);

			// Uses its own special weighting
			if(randomiser < 10){
				tier = Tier.NOVICE;
			}else if(randomiser < 16){
				tier = Tier.APPRENTICE;
			}else if(randomiser < 19 || !master){
				tier = Tier.ADVANCED;
			}else{
				tier = Tier.MASTER;
			}

			if(tier.ordinal() > maxTier.ordinal()) maxTier = tier;

			// Finds all the spells of the chosen tier and element
			List<Spell> list = Spell.getSpells(new Spell.TierElementFilter(tier, element, SpellProperties.Context.NPCS));
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

		if(source.getTrueSource() instanceof EntityPlayer){
			WizardryAdvancementTriggers.anger_wizard.triggerFor((EntityPlayer)source.getTrueSource());
		}

		return super.attackEntityFrom(source, damage);
	}

	/**
	 * Sets the list of blocks that are part of this wizard's tower. If a player breaks any of these blocks, the wizard
	 * will get angry and attack them.
	 * 
	 * @param blocks A Set of BlockPos objects representing the blocks in the tower.
	 */
	public void setTowerBlocks(Set<BlockPos> blocks){
		this.towerBlocks = blocks;
	}

	/** Tests whether the block at the given coordinates is part of this wizard's tower. */
	public boolean isBlockPartOfTower(BlockPos pos){
		if(this.towerBlocks == null) return false;
		// Uses .equals() rather than == so this will work fine.
		return this.towerBlocks.contains(pos);
	}

	@SubscribeEvent
	public static void onBlockBreakEvent(BlockEvent.BreakEvent event){
		// Makes wizards angry if a player breaks a block in their tower
		if(!(event.getPlayer() instanceof FakePlayer)){

			List<EntityWizard> wizards = WizardryUtilities.getEntitiesWithinRadius(64, event.getPos().getX(),
					event.getPos().getY(), event.getPos().getZ(), event.getWorld(), EntityWizard.class);

			if(!wizards.isEmpty()){
				for(EntityWizard wizard : wizards){
					if(wizard.isBlockPartOfTower(event.getPos())){
						wizard.setRevengeTarget(event.getPlayer());
						WizardryAdvancementTriggers.anger_wizard.triggerFor(event.getPlayer());
					}
				}
			}
		}
	}
	
	// Copied from their respective AI classes
	
	public static class EntityAILookAtTradePlayer extends EntityAIWatchClosest {
		
	    private final EntityWizard wizard;

	    public EntityAILookAtTradePlayer(EntityWizard wizard){
	        super(wizard, EntityPlayer.class, 8.0F);
	        this.wizard = wizard;
	    }

	    @Override
	    public boolean shouldExecute(){
	        if(this.wizard.isTrading()){
	            this.closestEntity = this.wizard.getCustomer();
	            return true;
	        }else{
	            return false;
	        }
	    }
	}
	
	public static class EntityAITradePlayer extends EntityAIBase {
		
		private final EntityWizard wizard;

	    public EntityAITradePlayer(EntityWizard wizard){
	        this.wizard = wizard;
	        this.setMutexBits(5);
	    }

	    @Override
	    public boolean shouldExecute(){
	    	
	        if(!this.wizard.isEntityAlive()){
	            return false;
	        }else if(this.wizard.isInWater()){
	            return false;
	        }else if(!this.wizard.onGround){
	            return false;
	        }else if(this.wizard.velocityChanged){
	            return false;
	        }else{
	        	
	            EntityPlayer entityplayer = this.wizard.getCustomer();

	            if(entityplayer == null){
	                return false;
	            }else if(this.wizard.getDistanceSq(entityplayer) > 16.0D){
	                return false;
	            }else{
	                return entityplayer.openContainer != null;
	            }
	        }
	    }

	    @Override
	    public void startExecuting(){
	        this.wizard.getNavigator().clearPath();
	    }

	    @Override
	    public void resetTask(){
	        this.wizard.setCustomer((EntityPlayer)null);
	    }
	}

}
