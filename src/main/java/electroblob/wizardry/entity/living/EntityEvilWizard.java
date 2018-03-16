package electroblob.wizardry.entity.living;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import com.google.common.base.Predicate;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.advancement.AdvancementHelper;
import electroblob.wizardry.advancement.AdvancementHelper.EnumAdvancement;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.item.ItemSpellBook;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryParticleType;
import electroblob.wizardry.util.WizardryUtilities;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAIRestrictOpenDoor;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest2;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

public class EntityEvilWizard extends EntityMob implements ISpellCaster, IEntityAdditionalSpawnData {

	private EntityAIAttackSpell spellCastingAI = new EntityAIAttackSpell(this, 0.5D, 14.0F, 30, 50);

	public int textureIndex = 0;

	public boolean hasTower = false;

	/** The entity selector passed into the new AI methods. */
	protected Predicate<Entity> targetSelector;

	/** Data parameter for the cooldown time for wizards healing themselves. */
	private static final DataParameter<Integer> HEAL_COOLDOWN = EntityDataManager.createKey(EntityEvilWizard.class,
			DataSerializers.VARINT);
	/** Data parameter for the wizard's element. */
	private static final DataParameter<Integer> ELEMENT = EntityDataManager.createKey(EntityEvilWizard.class,
			DataSerializers.VARINT);
	/** The resource location for the evil wizard's loot table. */
	private static final ResourceLocation LOOT_TABLE = new ResourceLocation(Wizardry.MODID, "entities/evil_wizard");

	// Field implementations
	private List<Spell> spells = new ArrayList<Spell>(4);
	private Spell continuousSpell;

	public EntityEvilWizard(World world){

		super(world);
		this.setSize(0.6F, 1.8F);
		((PathNavigateGround)this.getNavigator()).setBreakDoors(true);

		// For some reason this can't be done in initEntityAI
		this.tasks.addTask(3, this.spellCastingAI);

		this.detachHome();
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
		this.tasks.addTask(4, new EntityAIRestrictOpenDoor(this));
		this.tasks.addTask(5, new EntityAIOpenDoor(this, true));
		this.tasks.addTask(6, new EntityAIMoveTowardsRestriction(this, 0.6D));
		this.tasks.addTask(7, new EntityAIWatchClosest2(this, EntityPlayer.class, 3.0F, 1.0F));
		this.tasks.addTask(7, new EntityAIWander(this, 0.6D));

		this.targetSelector = new Predicate<Entity>(){

			public boolean apply(Entity entity){

				// If the target is valid and not invisible...
				if(entity != null && !entity.isInvisible()
						&& WizardryUtilities.isValidTarget(EntityEvilWizard.this, entity)){

					// ... and is a player, a summoned creature, another (non-evil) wizard ...
					if(entity instanceof EntityPlayer
							|| (entity instanceof ISummonedCreature || entity instanceof EntityWizard
					// ... or in the whitelist ...
									|| Arrays.asList(Wizardry.settings.summonedCreatureTargetsWhitelist)
											.contains(EntityList.getEntityString(entity).toLowerCase(Locale.ROOT)))
									// ... and isn't in the blacklist ...
									&& !Arrays.asList(Wizardry.settings.summonedCreatureTargetsBlacklist)
											.contains(EntityList.getEntityString(entity).toLowerCase(Locale.ROOT))){
						// ... it can be attacked.
						return true;
					}
				}

				return false;
			}
		};

		this.targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
		this.targetTasks.addTask(0, new EntityAINearestAttackableTarget<EntityLivingBase>(this, EntityLivingBase.class,
				0, false, true, this.targetSelector));
	}

	@Override
	protected void applyEntityAttributes(){
		super.applyEntityAttributes();
		this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.5D);
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
	public void onLivingUpdate(){

		super.onLivingUpdate();

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

			// Heal particles
			if(world.isRemote){
				for(int i = 0; i < 10; i++){
					double d0 = (double)((float)this.posX + rand.nextFloat() * 2 - 1.0F);
					// Apparently the client side spawns the particles 1 block higher than it should... hence the -
					// 0.5F.
					double d1 = (double)((float)this.posY - 0.5F + rand.nextFloat());
					double d2 = (double)((float)this.posZ + rand.nextFloat() * 2 - 1.0F);
					Wizardry.proxy.spawnParticle(WizardryParticleType.SPARKLE, world, d0, d1, d2, 0, 0.1F, 0,
							48 + rand.nextInt(12), 1.0f, 1.0f, 0.3f);
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
			this.setHealCooldown(healCooldown - 1);
		}
	}

	@Override
	protected boolean processInteract(EntityPlayer player, EnumHand hand){

		ItemStack stack = player.getHeldItem(hand);

		// Debugging
		// player.addChatComponentMessage(new TextComponentTranslation("wizard.debug",
		// Spell.get(spells[1]).getDisplayName(), Spell.get(spells[2]).getDisplayName(),
		// Spell.get(spells[3]).getDisplayName()));

		// When right-clicked with a spell book in creative, sets one of the spells to that spell
		if(player.capabilities.isCreativeMode && stack.getItem() instanceof ItemSpellBook){
			if(this.spells.size() >= 4 && Spell.get(stack.getItemDamage()).canBeCastByNPCs()){
				this.spells.set(rand.nextInt(3) + 1, Spell.get(stack.getItemDamage()));
				return true;
			}
		}

		return false;
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt){
		super.writeEntityToNBT(nbt);
		nbt.setInteger("element", this.getElement().ordinal());
		nbt.setInteger("skin", this.textureIndex);
		nbt.setTag("spells", WizardryUtilities.listToNBT(spells, spell -> new NBTTagInt(spell.id())));
		nbt.setBoolean("hasTower", this.hasTower);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt){
		super.readEntityFromNBT(nbt);
		this.setElement(Element.values()[nbt.getInteger("element")]);
		this.textureIndex = nbt.getInteger("skin");
		this.spells = (List<Spell>)WizardryUtilities.NBTToList(nbt.getTagList("spells", NBT.TAG_INT),
				(NBTTagInt tag) -> Spell.get(tag.getInt()));
		this.hasTower = nbt.getBoolean("hasTower");
	}

	@Override
	protected boolean canDespawn(){
		// Evil wizards can only despawn if they don't have a tower (i.e. if they spawned naturally at night)
		return !this.hasTower;
	}

	@Override
	protected float getSoundPitch(){
		return (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 0.6F;
	}

	@Override
	protected SoundEvent getAmbientSound(){
		return SoundEvents.ENTITY_WITCH_AMBIENT;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source){
		return SoundEvents.ENTITY_WITCH_HURT;
	}

	@Override
	protected SoundEvent getDeathSound(){
		return SoundEvents.ENTITY_WITCH_DEATH;
	}

	// Although it *looks* like this is still called, in actual fact the only method that calls it is overridden in
	// EntityLiving to use the loot table system instead. This has been kept as a fallback in case the loot table is
	// not found.
	@Override
	protected void dropFewItems(boolean hitByPlayer, int lootingLevel){
		// Drops 3-5 crystals without looting bonuses
		int j = 3 + this.rand.nextInt(3) + this.rand.nextInt(1 + lootingLevel);

		for(int k = 0; k < j; k++){
			this.dropItem(WizardryItems.magic_crystal, 1);
		}

		// Evil wizards occasionally drop one of their spells as a spell book, but not magic missile. This isn't in
		// the dropRareDrop method because that would be just as rare as normal mobs; instead this is half as rare.
		if(this.spells.size() > 0 && rand.nextInt(100) - lootingLevel < 5)
			this.entityDropItem(new ItemStack(WizardryItems.spell_book, 1,
					this.spells.get(1 + rand.nextInt(this.spells.size() - 1)).id()), 0);
	}

	@Override
	protected ResourceLocation getLootTable(){
		return LOOT_TABLE;
	}

	@Override
	public void onDeath(DamageSource source){

		super.onDeath(source);
		if(source.getTrueSource() instanceof EntityPlayer){
			AdvancementHelper.grantAdvancement((EntityPlayer)source.getTrueSource(), EnumAdvancement.defeat_evil_wizard);
		}
	}

	@Override
	public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, IEntityLivingData data){

		data = super.onInitialSpawn(difficulty, data);

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
		for(EntityEquipmentSlot slot : EntityEquipmentSlot.values())
			this.setDropChance(slot, 0.0f);

		// All wizards know magic missile, even if it is disabled.
		spells.add(Spells.magic_missile);

		Tier maxTier = EntityWizard.populateSpells(spells, element, 3, rand);

		// Now done after the spells so it can take the tier into account. For evil wizards this is slightly different;
		// it picks a random wand which is at least a high enough tier for the spells the wizard has.
		Tier tier = Tier.values()[maxTier.ordinal() + rand.nextInt(Tier.values().length - maxTier.ordinal())];
		this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(WizardryUtilities.getWand(tier, element)));

		return data;
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
