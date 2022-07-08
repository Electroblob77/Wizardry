package electroblob.wizardry.item;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Streams;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.client.DrawingUtils;
import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.event.SpellCastEvent;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryItems.Materials;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.registry.WizardryRecipes;
import electroblob.wizardry.registry.WizardryTabs;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.EntityUtils.Operations;
import electroblob.wizardry.util.InventoryUtils;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentMending;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.EntityEquipmentSlot.Type;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

@Mod.EventBusSubscriber
public class ItemWizardArmour extends ItemArmor implements IWorkbenchItem, IManaStoringItem {

	// Full set bonuses
	private static final float SAGE_OTHER_COST_REDUCTION = 0.2f;
	private static final float WARLOCK_SPEED_BOOST = 0.2f;
	private static final UUID WARLOCK_SPEED_BOOST_UUID = UUID.fromString("4bad7152-2663-4b1b-bb59-552e92847031");
	// Standard ItemArmor modifiers
	private static final UUID[] ARMOR_MODIFIERS = new UUID[] {UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"), UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D"), UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E"), UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150")};

	@Override
	public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot slot) {
		// The vanilla armor handling logic from ItemArmor.getItemAttributeModifiers was moved to ItemWizardArmour.getAttributeModifiers to allow checking if the stack has enough mana
		Multimap<String, AttributeModifier> multimap = ArrayListMultimap.create();
		return multimap;
	}

	@Override
	public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
		Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(slot, stack);

		// only grant armor if the equipment has mana
		if (slot == this.armorType && !((ItemWizardArmour) stack.getItem()).isManaEmpty(stack)) {
			multimap.put(SharedMonsterAttributes.ARMOR.getName(), new AttributeModifier(ARMOR_MODIFIERS[slot.getIndex()], "Armor modifier", this.damageReduceAmount, 0));
			multimap.put(SharedMonsterAttributes.ARMOR_TOUGHNESS.getName(), new AttributeModifier(ARMOR_MODIFIERS[slot.getIndex()], "Armor toughness", this.toughness, 0));
		}

		return multimap;
	}

	public enum ArmourClass {

		WIZARD(Materials.SILK, () -> null, "wizard", 0.1f, 0, "hat", "robe", "leggings", "boots"),
		SAGE(Materials.SAGE, () -> WizardryItems.resplendent_thread, "sage", 0.2f, 0, "hat", "robe", "leggings", "boots"),
		BATTLEMAGE(Materials.BATTLEMAGE, () -> WizardryItems.crystal_silver_plating, "battlemage", 0.05f, 0.05f, "helmet", "chestplate", "leggings", "boots"),
		WARLOCK(Materials.WARLOCK, () -> WizardryItems.ethereal_crystalweave, "warlock", 0.1f, 0.1f, "hood", "robe", "leggings", "boots");

		/** The armour material to use for this armour class. */
		final ArmorMaterial material;
		/** The item that upgrades regular wizard armour into this class of armour. */
		final Supplier<Item> upgradeItem;
		/** The fraction by which spell cost is reduced for each armour piece of this class with the matching element. */
		final float elementalCostReduction;
		/** The fraction by which spell cooldown is reduced for each armour piece of this class. */
		final float cooldownReduction;
		/** The first part of the texture filenames for this armour class. Also defines certain translation keys. */
		final String name;
		/** The middle part of each item name (in the registry) for this armour class, e.g. "hat" or "chestplate". */
		final Map<EntityEquipmentSlot, String> armourPieceNames;

		ArmourClass(ArmorMaterial material, Supplier<Item> upgradeItem, String name, float elementalCostReduction,
					float cooldownReduction, String... armourPieceNames){
			this.material = material;
			this.upgradeItem = upgradeItem;
			this.name = name;
			this.elementalCostReduction = elementalCostReduction;
			this.cooldownReduction = cooldownReduction;
			if(armourPieceNames.length != 4) throw new IllegalArgumentException("armourPieceNames must have a length of 4");
			this.armourPieceNames = new EnumMap<>(EntityEquipmentSlot.class);
			this.armourPieceNames.put(EntityEquipmentSlot.HEAD,  armourPieceNames[0]);
			this.armourPieceNames.put(EntityEquipmentSlot.CHEST, armourPieceNames[1]);
			this.armourPieceNames.put(EntityEquipmentSlot.LEGS,  armourPieceNames[2]);
			this.armourPieceNames.put(EntityEquipmentSlot.FEET,  armourPieceNames[3]);
		}

	}

	public Element element; // Should be final, but isn't for backwards compatibility
	public final ArmourClass armourClass;

	@Deprecated // Retained for backwards-compatibility with addons, will be removed in future
	public ItemWizardArmour(ArmorMaterial material, int renderIndex, EntityEquipmentSlot armourType, Element element){
		super(material, renderIndex, armourType);
		this.armourClass = ArmourClass.WIZARD;
		this.element = element;
		setCreativeTab(WizardryTabs.GEAR);
		WizardryRecipes.addToManaFlaskCharging(this);
	}

	public ItemWizardArmour(ArmourClass armourClass, EntityEquipmentSlot armourType, Element element){
		super(armourClass.material, 1, armourType);
		this.armourClass = armourClass;
		this.element = element;
		setCreativeTab(WizardryTabs.GEAR);
		WizardryRecipes.addToManaFlaskCharging(this);
	}

	/** Should only be used by vanilla's armour damage calculations; use {@link ItemWizardArmour#setMana(ItemStack, int)}
	 * to modify armour mana from elsewhere. */
	@Override
	public void setDamage(ItemStack stack, int damage){
		// Overridden to stop repair things from 'repairing' the mana in wizard armour
		// This being armour, it's much easier to let its damage increase normally, but block it from being decreased
		if(stack.getItemDamage() < damage) super.setDamage(stack, Math.min(damage, stack.getMaxDamage()));
	}

	@Override
	public void setMana(ItemStack stack, int mana){
		// Using super (which can only be done from in here) bypasses the above override
		super.setDamage(stack, getManaCapacity(stack) - mana);
	}

	@Override
	public int getMana(ItemStack stack){
		return getManaCapacity(stack) - getDamage(stack);
	}

	@Override
	public int getManaCapacity(ItemStack stack){
		return this.getMaxDamage(stack);
	}

	@Override
	public int getRGBDurabilityForDisplay(ItemStack stack){
		return DrawingUtils.mix(0xff8bfe, 0x8e2ee4, (float)getDurabilityForDisplay(stack));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> tooltip, net.minecraft.client.util.ITooltipFlag advanced){

		if(element != null){
			tooltip.add(Wizardry.proxy.translate("item." + Wizardry.MODID + ":wizard_armour.element_cost_reduction",
					new Style().setColor(TextFormatting.DARK_GRAY),
					(int)(armourClass.elementalCostReduction * 100), element.getDisplayName()));
		}

		if(armourClass == ArmourClass.SAGE){
			tooltip.add(Wizardry.proxy.translate("item." + Wizardry.MODID + ":wizard_armour.enchantability",
					new Style().setColor(TextFormatting.BLUE)));
		}

		if(armourClass.cooldownReduction > 0){
			tooltip.add(Wizardry.proxy.translate("item." + Wizardry.MODID + ":wizard_armour.cooldown_reduction",
					new Style().setColor(TextFormatting.DARK_GRAY), (int)(armourClass.cooldownReduction * 100)));
		}

		if(armourClass != ArmourClass.WIZARD){

			tooltip.add(Wizardry.proxy.translate("item." + Wizardry.MODID + ":wizard_armour.full_set",
					new Style().setColor(TextFormatting.AQUA)));

			Object args = new Object[0];

			if(armourClass == ArmourClass.SAGE) args = (int)(SAGE_OTHER_COST_REDUCTION * 100);
			if(armourClass == ArmourClass.WARLOCK) args = (int)(WARLOCK_SPEED_BOOST * 100);

			tooltip.add(Wizardry.proxy.translate("item." + Wizardry.MODID + ":" + armourClass.name
					+ "_armour.full_set_bonus", new Style().setColor(TextFormatting.AQUA), args));

		}

	}

	@Override
	public void onArmorTick(World world, EntityPlayer player, ItemStack itemStack){
		if(armorType == EntityEquipmentSlot.HEAD && player.ticksExisted % 20 == 0
				&& isWearingFullSet(player, element, ArmourClass.BATTLEMAGE) && doAllArmourPiecesHaveMana(player)){
			player.addPotionEffect(new PotionEffect(WizardryPotions.ward, 219, 0, true, false));
		}
	}

	/**
	 * Allows this armour piece to change the modifiers for spells cast while wearing it. This is called once for
	 * <b>each piece of wizard armour</b> the caster is wearing. This means if you need to apply a full set bonus, you
	 * should have only one of the pieces handle it (by convention, this is usually the helmet, but it doesn't matter).
	 * <p></p>
	 * <i>N.B. The modifiers object passed into this method is not the actual one from the spell cast event. It is a
	 * blank one local to armour that then gets combined with the actual one via
	 * {@link SpellModifiers#combine(SpellModifiers)} after all four pieces have been processed. This is important as it
	 * allows armour modifiers to stack linearly. This means the values should be <b>added/subtracted rather than multiplied.</b></i>
	 * @param caster The entity casting the spell and wearing the armour.
	 * @param spell The spell being cast.
	 * @param modifiers The modifiers specific to armour to apply to the global ones after all four slots are processed.
	 */
	protected void applySpellModifiers(EntityLivingBase caster, Spell spell, SpellModifiers modifiers){

		if(spell.getElement() == this.element){
			modifiers.set(SpellModifiers.COST, modifiers.get(SpellModifiers.COST) - armourClass.elementalCostReduction, false);
		}

		modifiers.set(WizardryItems.cooldown_upgrade, modifiers.get(WizardryItems.cooldown_upgrade) - armourClass.cooldownReduction, true);

		// Full set bonuses
		if(this.armorType == EntityEquipmentSlot.HEAD && isWearingFullSet(caster, element, armourClass) && doAllArmourPiecesHaveMana(caster)){
			if(armourClass == ArmourClass.SAGE && spell.getElement() != this.element){
				modifiers.set(SpellModifiers.COST, 1 - SAGE_OTHER_COST_REDUCTION, false);
			}
		}

	}

	@Override
	public String getItemStackDisplayName(ItemStack stack){
		return ((this.element == null ? "" : this.element.getFormattingCode()) + super.getItemStackDisplayName(stack));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public net.minecraft.client.model.ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack,
			EntityEquipmentSlot armourSlot, net.minecraft.client.model.ModelBiped original){

		// Legs use modelBiped
		if(armourSlot == EntityEquipmentSlot.LEGS && !entityLiving.isInvisible()) return null;

		return Wizardry.proxy.getWizardArmourModel(getArmorMaterial());
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type){

		String s = armourClass.name + "_armour";

		if(Wizardry.tisTheSeason && armourClass == ArmourClass.WIZARD){
			s = s + "_festive";
		}else{
			if(this.element != null) s = s + "_" + this.element.getName();
		}

		if(slot == EntityEquipmentSlot.LEGS) s = s + "_legs";

		return "ebwizardry:textures/armour/" + s + ".png";
	}

	@Override
	public boolean getIsRepairable(ItemStack stack, ItemStack material){
		return false;
	}

	// This is misleadingly-named, it also applies to enchanting with books at an anvil
	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment){
		return !(enchantment instanceof EnchantmentMending) && super.canApplyAtEnchantingTable(stack, enchantment);
	}

	// Workbench stuff

	@Override
	public boolean showTooltip(ItemStack stack){ return true; }

	@Override
	public int getSpellSlotCount(ItemStack stack){
		return 0; // Doesn't have any spell slots!
	}

	@Override
	public ItemStack applyUpgrade(@Nullable EntityPlayer player, ItemStack stack, ItemStack upgrade){

		// Apply class upgrades
		if(this.armourClass == ArmourClass.WIZARD){
			for(ArmourClass armourClass : ArmourClass.values()){
				if(upgrade.getItem() == armourClass.upgradeItem.get()){
					Item newArmour = getArmour(this.element, armourClass, this.armorType);
					ItemStack newStack = new ItemStack(newArmour);
					((ItemWizardArmour)newArmour).setMana(newStack, this.getMana(stack));
					newStack.setTagCompound(stack.getTagCompound());
					upgrade.shrink(1);
					return newStack;
				}
			}
		}

		return stack;
	}

	@Override
	public boolean onApplyButtonPressed(EntityPlayer player, Slot centre, Slot crystals, Slot upgrade, Slot[] spellBooks){
		
		boolean changed = false;

		if(upgrade.getHasStack()){
			ItemStack original = centre.getStack().copy();
			centre.putStack(this.applyUpgrade(player, centre.getStack(), upgrade.getStack()));
			changed = !ItemStack.areItemStacksEqual(centre.getStack(), original);
		}
		
		// Charges armour by appropriate amount
		if(crystals.getStack() != ItemStack.EMPTY && !this.isManaFull(centre.getStack())){

			int chargeDepleted = this.getManaCapacity(centre.getStack()) - this.getMana(centre.getStack());

			// Not too pretty but allows addons implementing the IManaStoringItem interface to provide their mana amount for custom crystals,
			// previously this was defaulted to the regular crystal's amount, allowing players to exploit it if a crystal was worth less mana than that.
			int manaPerItem = crystals.getStack().getItem() instanceof IManaStoringItem ?
					((IManaStoringItem) crystals.getStack().getItem()).getMana(crystals.getStack()) :
					crystals.getStack().getItem() instanceof ItemCrystal ? Constants.MANA_PER_CRYSTAL : Constants.MANA_PER_SHARD;

			if(crystals.getStack().getItem() == WizardryItems.crystal_shard) manaPerItem = Constants.MANA_PER_SHARD;
			if(crystals.getStack().getItem() == WizardryItems.grand_crystal) manaPerItem = Constants.GRAND_CRYSTAL_MANA;

			if(crystals.getStack().getCount() * manaPerItem < chargeDepleted){
				// If there aren't enough crystals to fully charge the armour
				this.rechargeMana(centre.getStack(), crystals.getStack().getCount() * Constants.MANA_PER_CRYSTAL);
				crystals.decrStackSize(crystals.getStack().getCount());

			}else{
				// If there are excess crystals (or just enough)
				this.setMana(centre.getStack(), this.getManaCapacity(centre.getStack()));
				crystals.decrStackSize((int)Math.ceil(((double)chargeDepleted) / Constants.MANA_PER_CRYSTAL));
			}

			changed = true;
		}
		
		return changed;
	}

	// Event Handlers

	// Can't use Item#getItemAttributeModifiers because it's not player-sensitive, grrr
	@SubscribeEvent
	public static void onLivingEquipmentChangeEvent(LivingEquipmentChangeEvent event){

		IAttributeInstance movementSpeed = event.getEntityLiving().getAttributeMap().getAttributeInstance(SharedMonsterAttributes.MOVEMENT_SPEED);

		if(isWearingFullSet(event.getEntityLiving(), null, ArmourClass.WARLOCK) && doAllArmourPiecesHaveMana(event.getEntityLiving())){
			// Only apply the modifier once (can't just check this is the helmet since it might not be the last piece equipped)
			if(movementSpeed.getModifier(WARLOCK_SPEED_BOOST_UUID) == null){
				movementSpeed.applyModifier(new AttributeModifier(WARLOCK_SPEED_BOOST_UUID, "Warlock set bonus", WARLOCK_SPEED_BOOST, Operations.MULTIPLY_FLAT));
			}
		}else{
			movementSpeed.removeModifier(WARLOCK_SPEED_BOOST_UUID);
		}
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onSpellCastPreEvent(SpellCastEvent.Pre event){
		// Armour cost reduction
		if(event.getCaster() == null) return;

		final SpellModifiers armourModifiers = new SpellModifiers();

		Arrays.stream(InventoryUtils.ARMOUR_SLOTS).map(s -> event.getCaster().getItemStackFromSlot(s).getItem())
				.filter(i -> i instanceof ItemWizardArmour)
				.forEach(i -> ((ItemWizardArmour)i).applySpellModifiers(event.getCaster(), event.getSpell(), armourModifiers));

		event.getModifiers().combine(armourModifiers);
	}

	/**
	 * Counts the number of armour pieces the given entity is wearing that match the given element.
	 * @deprecated in favour of {@link ItemWizardArmour#applySpellModifiers(EntityLivingBase, Spell, SpellModifiers)}
	 * and {@link ItemWizardArmour#isWearingFullSet(EntityLivingBase, Element, ArmourClass)}. Retained for API stability
	 * between minor versions, will be removed in future.
	 */
	@Deprecated
	public static int getMatchingArmourCount(EntityLivingBase entity, Element element){
		return (int)Arrays.stream(InventoryUtils.ARMOUR_SLOTS)
				.map(s -> entity.getItemStackFromSlot(s).getItem())
				.filter(i -> i instanceof ItemWizardArmour && ((ItemWizardArmour)i).element == element)
				.count();
	}

	/**
	 * Helper method to return the appropriate wand based on tier and element. This replaces the cumbersome wand map in
	 * {@link WizardryItems} by accessing the item registry dynamically by generating the registry name on the fly.
	 * <p></p>
	 * <i><b>This method will only return armour from the base mod.</b> It is unlikely that addons will need it, but it
	 * has been left public just in case. The intention is that this method is only used where there is no alternative.</i>
	 *
	 * @param element The element of the wand required. Null will be converted to {@link Element#MAGIC}.
	 * @param armourClass The {@link ArmourClass} required
	 * @param slot EntityEquipmentSlot of the armour piece required
	 * @return The wand item which corresponds to the given tier and element, or null if no such item exists.
	 * @throws NullPointerException if the given tier is null.
	 */
	// As noted above, in a few SPECIFIC cases this method is necessary (without using a data-driven system, at least,
	// which I'm not going to spend the time making in the near future). Wizard trades and gear have been left using the
	// WizardryItems version because they need to be replaced with a better system that doesn't use this at all.
	public static Item getArmour(Element element, ArmourClass armourClass, EntityEquipmentSlot slot){
		if(slot == null || slot.getSlotType() != Type.ARMOR)
			throw new IllegalArgumentException("Must be a valid armour slot");
		if(element == null) element = Element.MAGIC;
		String registryName = armourClass.name + "_" + armourClass.armourPieceNames.get(slot);
		if(element != Element.MAGIC) registryName = registryName + "_" + element.getName();
		return Item.REGISTRY.getObject(new ResourceLocation(Wizardry.MODID,  registryName));
	}

	/**
	 * Returns whether the given entity is wearing a full set of wizard armour of the given class and element.
	 * @param entity The entity to query.
	 * @param element The element to check, or null to accept any element as long as they are all the same.
	 * @param armourClass The class to check, or null to accept any class as long as they are all the same.
	 * @return True if the entity is wearing a full set of the given element and class, false otherwise.
	 */
	public static boolean isWearingFullSet(EntityLivingBase entity, @Nullable Element element, @Nullable ArmourClass armourClass){
		ItemStack helmet = entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
		if(!(helmet.getItem() instanceof ItemWizardArmour)) return false;
		Element e = element == null ? ((ItemWizardArmour)helmet.getItem()).element : element;
		ArmourClass ac = armourClass == null ? ((ItemWizardArmour)helmet.getItem()).armourClass : armourClass;
		return Arrays.stream(InventoryUtils.ARMOUR_SLOTS)
				.allMatch(s -> entity.getItemStackFromSlot(s).getItem() instanceof ItemWizardArmour
						&& ((ItemWizardArmour)entity.getItemStackFromSlot(s).getItem()).element == e
						&& ((ItemWizardArmour)entity.getItemStackFromSlot(s).getItem()).armourClass == ac);
	}

	/**
	 * Returns whether the given entity's armour pieces which implement {@link IManaStoringItem} has mana. Ignores pieces which doesn't have a mana storage.
	 * @param entity The entity to query.
	 * @return True if all armour pieces which implement {@link IManaStoringItem} has mana, false otherwise.
	 */
	public static boolean doAllArmourPiecesHaveMana(EntityLivingBase entity){
		return Arrays.stream(InventoryUtils.ARMOUR_SLOTS).noneMatch(s -> entity.getItemStackFromSlot(s).getItem() instanceof IManaStoringItem
						&& ((IManaStoringItem) entity.getItemStackFromSlot(s).getItem()).isManaEmpty(entity.getItemStackFromSlot(s)));
	}

	@SubscribeEvent
	public static void onLivingSetAttackTargetEvent(LivingSetAttackTargetEvent event){
		// Undo the mob detection penalty for wearing armour when invisible
		// Only bother doing this for players because the penalty only applies to them
		if(event.getTarget() instanceof EntityPlayer && event.getEntityLiving() instanceof EntityLiving
				&& event.getEntityLiving().isInvisible()){

			int armourPieces = (int)Streams.stream(event.getTarget().getArmorInventoryList())
					.filter(s -> !s.isEmpty() && !(s.getItem() instanceof ItemWizardArmour))
					.count();

			if(armourPieces == 0) return;

			// Repeat the calculation from EntityAIFindNearestPlayer, but ignoring wizard armour
			IAttributeInstance attribute = event.getEntityLiving().getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE);
			double followRange = attribute == null ? 16 : attribute.getAttributeValue();
			if(event.getTarget().isSneaking()) followRange *= 0.8;
			float f = (float)armourPieces / ((EntityPlayer)event.getTarget()).inventory.armorInventory.size();
			if(f < 0.1F) f = 0.1F;
			followRange *= 0.7F * f;
			// Don't need to worry about the isSuitableTarget check since it must already have been checked to get this far
			if(event.getTarget().getDistance(event.getEntity()) > followRange) ((EntityLiving)event.getEntityLiving()).setAttackTarget(null);
		}
	}

}
