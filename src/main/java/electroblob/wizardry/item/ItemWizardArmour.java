package electroblob.wizardry.item;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.registry.WizardryAdvancementTriggers;
import electroblob.wizardry.registry.WizardryTabs;
import electroblob.wizardry.spell.Petrify;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber
public class ItemWizardArmour extends ItemArmor {

	//VanillaCopy, ItemArmor has this set to private for some reason.
    public static final UUID[] ARMOR_MODIFIERS = new UUID[] {UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"), UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D"), UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E"), UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150")};
	//Damage reduction values that used to be in WizardryItems.SILK [feet, legs, chest, head]
    private static int[] reductions = new int[]{2, 4, 5, 2};
	
	public Element element;

	public ItemWizardArmour(ArmorMaterial material, int renderIndex, EntityEquipmentSlot armourType, Element element){
		super(material, renderIndex, armourType);
		this.element = element;
		setCreativeTab(WizardryTabs.WIZARDRY);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag advanced){

		if(stack.hasTagCompound() && stack.getTagCompound().getBoolean("legendary")) tooltip
		.add("\u00A7d" + net.minecraft.client.resources.I18n.format("item." + Wizardry.MODID + ":wizard_armour.legendary"));
		if(element != null)
			tooltip.add("\u00A78" + net.minecraft.client.resources.I18n.format("item." + Wizardry.MODID + ":wizard_armour.buff",
					(int)(Constants.COST_REDUCTION_PER_ARMOUR * 100) + "%", element.getDisplayName()));
		tooltip.add("\u00A79" + net.minecraft.client.resources.I18n.format("item." + Wizardry.MODID + ":wizard_armour.mana",
				(this.getMaxDamage(stack) - this.getDamage(stack)), this.getMaxDamage(stack)));
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack){
		return ((this.element == null ? "" : this.element.getFormattingCode()) + super.getItemStackDisplayName(stack));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack stack){
		return stack.hasTagCompound() && stack.getTagCompound().getBoolean("legendary");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack,
			EntityEquipmentSlot armourSlot, ModelBiped _default){

		ModelBiped model = Wizardry.proxy.getWizardArmourModel();

		// Legs use modelBiped
		if(armourSlot == EntityEquipmentSlot.LEGS) return null;

		if(model != null){

			model.bipedHead.showModel = armourSlot == EntityEquipmentSlot.HEAD;
			model.bipedHeadwear.showModel = false;
			model.bipedBody.showModel = armourSlot == EntityEquipmentSlot.CHEST
					|| armourSlot == EntityEquipmentSlot.LEGS;
			model.bipedRightArm.showModel = armourSlot == EntityEquipmentSlot.CHEST;
			model.bipedLeftArm.showModel = armourSlot == EntityEquipmentSlot.CHEST;
			model.bipedRightLeg.showModel = armourSlot == EntityEquipmentSlot.LEGS
					|| armourSlot == EntityEquipmentSlot.FEET;
			model.bipedLeftLeg.showModel = armourSlot == EntityEquipmentSlot.LEGS
					|| armourSlot == EntityEquipmentSlot.FEET;

			model.isSneak = entityLiving.isSneaking();
			model.isRiding = entityLiving.isRiding();
			model.isChild = entityLiving.isChild();

			boolean leftHanded = entityLiving.getPrimaryHand() == EnumHandSide.LEFT;

			ItemStack itemstackR = leftHanded ? entityLiving.getHeldItemOffhand() : entityLiving.getHeldItemMainhand();
			ItemStack itemstackL = leftHanded ? entityLiving.getHeldItemMainhand() : entityLiving.getHeldItemOffhand();

			if(!itemstackR.isEmpty()){
				model.rightArmPose = ModelBiped.ArmPose.ITEM;

				if(entityLiving.getItemInUseCount() > 0){
					EnumAction enumaction = itemstackR.getItemUseAction();

					if(enumaction == EnumAction.BLOCK){
						model.rightArmPose = ModelBiped.ArmPose.BLOCK;
					}else if(enumaction == EnumAction.BOW){
						model.rightArmPose = ModelBiped.ArmPose.BOW_AND_ARROW;
					}
				}
			}

			if(!itemstackL.isEmpty()){
				model.leftArmPose = ModelBiped.ArmPose.ITEM;

				if(entityLiving.getItemInUseCount() > 0){
					EnumAction enumaction1 = itemstackL.getItemUseAction();

					if(enumaction1 == EnumAction.BLOCK){
						model.leftArmPose = ModelBiped.ArmPose.BLOCK;
					}else if(enumaction1 == EnumAction.BOW){
						model.leftArmPose = ModelBiped.ArmPose.BOW_AND_ARROW;
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
		if(entity instanceof EntityLivingBase && ((EntityLivingBase)entity).isInvisible()
				&& !entity.getEntityData().getBoolean(Petrify.NBT_KEY))
			return "ebwizardry:textures/armour/invisible_armour.png";

		if(slot == EntityEquipmentSlot.LEGS)
			return this.element == null ? "ebwizardry:textures/armour/wizard_armour_legs.png"
					: "ebwizardry:textures/armour/wizard_armour_" + this.element.getUnlocalisedName() + "_legs.png";

		return this.element == null ? "ebwizardry:textures/armour/wizard_armour.png"
				: "ebwizardry:textures/armour/wizard_armour_" + this.element.getUnlocalisedName() + ".png";
	}

	@Override
	public boolean getIsRepairable(ItemStack stack, ItemStack par2ItemStack){
		return false;
	}
	
	/*
	 * Properly handles the defense value of the armor.  This method is responisble for the tooltip on top of
	 * the armor value.  It is also what handles armor toughness, but the wizard armor had a value of 0 for that.
	 */
	@Override
	public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
		Multimap<String, AttributeModifier> map = HashMultimap.create();
		if(stack.getItemDamage() < stack.getMaxDamage() && this.armorType == slot) {
			int defense = reductions[slot.getIndex()];
			if(stack.hasTagCompound() && stack.getTagCompound().getBoolean("legendary")) 
				defense = ArmorMaterial.DIAMOND.getDamageReductionAmount(slot);
			map.put(SharedMonsterAttributes.ARMOR.getName(), new AttributeModifier(ARMOR_MODIFIERS[slot.getIndex()], 
					"Armor modifier", defense, 0));
		}
		return map;
	}

	// Fixes wizard armor breaking by disallowing setting damage above the max, since damageArmor is not always called.
	// Since ISpecialArmor has been removed from this class, this may no longer be necessary, but keeping it won't hurt.
	@Override
	public void setDamage(ItemStack stack, int damage) {
		if(damage <= stack.getMaxDamage()) super.setDamage(stack, damage);
		else super.setDamage(stack, stack.getMaxDamage());
	}

	@SubscribeEvent
	public static void onLivingUpdateEvent(LivingUpdateEvent event){

		if(event.getEntityLiving() instanceof EntityPlayer){
			
			EntityPlayer player = (EntityPlayer)event.getEntityLiving();

			for(ItemStack stack : player.getArmorInventoryList()){
				if(!(stack.getItem() instanceof ItemWizardArmour)){
					return; // If any of the armour slots doesn't contain wizard armour, don't trigger the achievement.
				}
			}
			// If it gets this far, then all slots must be wizard armour, so trigger the achievement.
			WizardryAdvancementTriggers.armour_set.triggerFor(player);
		}
	}

}
