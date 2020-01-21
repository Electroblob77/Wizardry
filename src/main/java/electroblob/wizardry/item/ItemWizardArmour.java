package electroblob.wizardry.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Streams;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.event.SpellCastEvent;
import electroblob.wizardry.registry.WizardryAdvancementTriggers;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryRecipes;
import electroblob.wizardry.registry.WizardryTabs;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.Slot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHandSide;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber
public class ItemWizardArmour extends ItemArmor implements IWorkbenchItem, IManaStoringItem {

	// VanillaCopy, ItemArmor has this set to private for some reason.
    public static final UUID[] ARMOR_MODIFIERS = new UUID[] {UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"), UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D"), UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E"), UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150")};
	// Damage reduction values that used to be in WizardryItems.SILK [feet, legs, chest, head]
    private static int[] reductions = new int[]{2, 4, 5, 2};
	
	public Element element;

	public ItemWizardArmour(ArmorMaterial material, int renderIndex, EntityEquipmentSlot armourType, Element element){
		super(material, renderIndex, armourType);
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
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> tooltip, net.minecraft.client.util.ITooltipFlag advanced){

		if(stack.hasTagCompound() && stack.getTagCompound().getBoolean("legendary"))
			tooltip.add("\u00A7d" + net.minecraft.client.resources.I18n.format("item." + Wizardry.MODID + ":wizard_armour.legendary"));

		if(element != null){
			tooltip.add("\u00A78" + net.minecraft.client.resources.I18n.format("item." + Wizardry.MODID + ":wizard_armour.buff",
					(int)(Constants.COST_REDUCTION_PER_ARMOUR * 100) + "%", element.getDisplayName()));
		}

		//		tooltip.add("\u00A79" + net.minecraft.client.resources.I18n.format("item." + Wizardry.MODID + ":wizard_armour.mana",
//				(this.getMaxDamage(stack) - this.getDamage(stack)), this.getMaxDamage(stack)));

//		ChargeStatus status = ChargeStatus.getChargeStatus(stack);
//
//		tooltip.add(status.getFormattingCode() + status.getDisplayName());
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack){
		return ((this.element == null ? "" : this.element.getFormattingCode()) + super.getItemStackDisplayName(stack));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public net.minecraft.client.model.ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack,
			EntityEquipmentSlot armourSlot, net.minecraft.client.model.ModelBiped _default){

		// Legs use modelBiped
		if(armourSlot == EntityEquipmentSlot.LEGS && !entityLiving.isInvisible()) return null;

		net.minecraft.client.model.ModelBiped model = Wizardry.proxy.getWizardArmourModel();

		if(model != null){

			model.bipedHead.showModel = armourSlot == EntityEquipmentSlot.HEAD;
			model.bipedHeadwear.showModel = false;
			model.bipedBody.showModel = armourSlot == EntityEquipmentSlot.CHEST;
			model.bipedRightArm.showModel = armourSlot == EntityEquipmentSlot.CHEST;
			model.bipedLeftArm.showModel = armourSlot == EntityEquipmentSlot.CHEST;
			model.bipedRightLeg.showModel = armourSlot == EntityEquipmentSlot.FEET;
			model.bipedLeftLeg.showModel = armourSlot == EntityEquipmentSlot.FEET;

			model.isSneak = entityLiving.isSneaking();
			model.isRiding = entityLiving.isRiding();
			model.isChild = entityLiving.isChild();

			boolean leftHanded = entityLiving.getPrimaryHand() == EnumHandSide.LEFT;

			ItemStack itemstackR = leftHanded ? entityLiving.getHeldItemOffhand() : entityLiving.getHeldItemMainhand();
			ItemStack itemstackL = leftHanded ? entityLiving.getHeldItemMainhand() : entityLiving.getHeldItemOffhand();

			if(!itemstackR.isEmpty()){
				model.rightArmPose = net.minecraft.client.model.ModelBiped.ArmPose.ITEM;

				if(entityLiving.getItemInUseCount() > 0){
					EnumAction enumaction = itemstackR.getItemUseAction();

					if(enumaction == EnumAction.BLOCK){
						model.rightArmPose = net.minecraft.client.model.ModelBiped.ArmPose.BLOCK;
					}else if(enumaction == EnumAction.BOW){
						model.rightArmPose = net.minecraft.client.model.ModelBiped.ArmPose.BOW_AND_ARROW;
					}
				}
			}

			if(!itemstackL.isEmpty()){
				model.leftArmPose = net.minecraft.client.model.ModelBiped.ArmPose.ITEM;

				if(entityLiving.getItemInUseCount() > 0){
					EnumAction enumaction1 = itemstackL.getItemUseAction();

					if(enumaction1 == EnumAction.BLOCK){
						model.leftArmPose = net.minecraft.client.model.ModelBiped.ArmPose.BLOCK;
					}else if(enumaction1 == EnumAction.BOW){
						model.leftArmPose = net.minecraft.client.model.ModelBiped.ArmPose.BOW_AND_ARROW;
					}
				}
			}
		}

		return model;
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type){

		// Returns a completely transparent texture if the player is invisible. This is such an annoyingly easy
		// fix, considering how long I spent trying to do this before - a bit of lateral thinking was all it took.
		// Do note however that a texture pack could override this.
//		if(entity instanceof EntityLivingBase && entity.isInvisible() && !entity.getEntityData().getBoolean(BlockStatue.PETRIFIED_NBT_KEY))
//			return "ebwizardry:textures/armour/invisible_armour.png";

		String s = "wizard_armour";

		if(Wizardry.tisTheSeason){
			s = s + "_festive";
		}else{
			if(this.element != null) s = s + "_" + this.element.getName();
			if(stack.hasTagCompound() && stack.getTagCompound().getBoolean("legendary")) s = "legendary_" + s;
		}

		if(slot == EntityEquipmentSlot.LEGS) s = s + "_legs";

		return "ebwizardry:textures/armour/" + s + ".png";
	}

	@Override
	public boolean getIsRepairable(ItemStack stack, ItemStack material){
		return false;
	}
	
	/**
	 * Properly handles the defence value of the armour. This method is responsible for the tooltip on top of
	 * the armour value. It is also what handles armour toughness.
	 */
	@Override
	public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack){
		
		Multimap<String, AttributeModifier> map = HashMultimap.create();
		
		if(!this.isManaEmpty(stack) && this.armorType == slot){
			
			int defense = reductions[slot.getIndex()];
			float toughness = 0f;
			
			if(stack.hasTagCompound() && stack.getTagCompound().getBoolean("legendary")) {
				defense = ArmorMaterial.DIAMOND.getDamageReductionAmount(slot);
				toughness = ArmorMaterial.DIAMOND.getToughness();
			}
			
			map.put(SharedMonsterAttributes.ARMOR.getName(), new AttributeModifier(ARMOR_MODIFIERS[slot.getIndex()], 
					"Armor modifier", defense, 0));
			map.put(SharedMonsterAttributes.ARMOR.getName(), new AttributeModifier(ARMOR_MODIFIERS[slot.getIndex()], 
					"Armor toughness", toughness, 0));
		}
		
		return map;
	}

	// Workbench stuff

	@Override
	public boolean showTooltip(ItemStack stack){ return true; }

	@Override
	public int getSpellSlotCount(ItemStack stack){
		return 0; // Doesn't have any spell slots!
	}

	@Override
	public boolean onApplyButtonPressed(EntityPlayer player, Slot centre, Slot crystals, Slot upgrade, Slot[] spellBooks){
		
		boolean changed = false;
		
		// Applies legendary upgrade
		if(upgrade.getStack().getItem() == WizardryItems.armour_upgrade){
			
			if(!centre.getStack().hasTagCompound()){
				centre.getStack().setTagCompound(new NBTTagCompound());
			}
			
			if(!centre.getStack().getTagCompound().hasKey("legendary")){
				
				centre.getStack().getTagCompound().setBoolean("legendary", true);
				upgrade.decrStackSize(1);
				WizardryAdvancementTriggers.legendary.triggerFor(player);
				changed = true;
			}
		}
		
		// Charges armour by appropriate amount
		if(crystals.getStack() != ItemStack.EMPTY && !this.isManaFull(centre.getStack())){

			int chargeDepleted = this.getManaCapacity(centre.getStack()) - this.getMana(centre.getStack());

			if(crystals.getStack().getCount() * Constants.MANA_PER_CRYSTAL < chargeDepleted){
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

//	@SubscribeEvent
//	public static void onLivingUpdateEvent(LivingUpdateEvent event){
//
//		if(event.getEntityLiving() instanceof EntityPlayer){
//
//			EntityPlayer player = (EntityPlayer)event.getEntityLiving();
//
//			for(ItemStack stack : player.getArmorInventoryList()){
//				if(!(stack.getItem() instanceof ItemWizardArmour)){
//					return; // If any of the armour slots doesn't contain wizard armour, don't trigger the achievement.
//				}
//			}
//			// If it gets this far, then all slots must be wizard armour, so trigger the achievement.
//			WizardryAdvancementTriggers.armour_set.triggerFor(player);
//		}
//	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onSpellCastPreEvent(SpellCastEvent.Pre event){
		// Armour cost reduction
		if(event.getCaster() == null) return;
		int armourPieces = getMatchingArmourCount(event.getCaster(), event.getSpell().getElement());
		float multiplier = 1f - armourPieces * Constants.COST_REDUCTION_PER_ARMOUR;
		if(armourPieces == WizardryUtilities.ARMOUR_SLOTS.length) multiplier -= Constants.FULL_ARMOUR_SET_BONUS;
		event.getModifiers().set(SpellModifiers.COST, event.getModifiers().get(SpellModifiers.COST) * multiplier, false);
	}

	/** Counts the number of armour pieces the given entity is wearing that match the given element. */
	public static int getMatchingArmourCount(EntityLivingBase entity, Element element){
		return (int)Arrays.stream(WizardryUtilities.ARMOUR_SLOTS)
				.map(s -> entity.getItemStackFromSlot(s).getItem())
				.filter(i -> i instanceof ItemWizardArmour && ((ItemWizardArmour)i).element == element)
				.count();
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
			followRange *= (double)(0.7F * f);
			// Don't need to worry about the isSuitableTarget check since it must already have been checked to get this far
			if(event.getTarget().getDistance(event.getEntity()) > followRange) ((EntityLiving)event.getEntityLiving()).setAttackTarget(null);
		}
	}

}
