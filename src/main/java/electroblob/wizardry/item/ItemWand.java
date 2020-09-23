package electroblob.wizardry.item;

import com.google.common.collect.Multimap;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.client.DrawingUtils;
import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.data.SpellGlyphData;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.entity.living.ISummonedCreature;
import electroblob.wizardry.event.SpellCastEvent;
import electroblob.wizardry.event.SpellCastEvent.Source;
import electroblob.wizardry.packet.PacketCastSpell;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.registry.*;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.*;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.Slot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

/**
 * This class is (literally) where the magic happens! All of wizardry's wand items are instances of this class. As of
 * wizardry 4.2, it is no longer necessary to extend {@code ItemWand} thanks to {@link ISpellCastingItem}, though
 * extending {@code ItemWand} may still be more appropriate for items using the same casting implementation.
 * <p></p>
 * This class handles spell casting as follows:
 * <p></p>
 * - {@code onItemRightClick} is where non-continuous spells are cast, and it sets the item in use for continuous spells<br>
 * - {@code onUsingTick} does the casting for continuous spells<br>
 * - {@code onUpdate} deals with the cooldowns for the spells<br>
 * <br>
 * See {@link ISpellCastingItem} for more detail on the {@code canCast(...)} and {@code cast(...)} methods.<br>
 * See {@link WandHelper} for everything related to wand NBT.
 *
 * @since Wizardry 1.0
 */
@Mod.EventBusSubscriber
public class ItemWand extends Item implements IWorkbenchItem, ISpellCastingItem, IManaStoringItem {
	
	/** The number of spell slots a wand has with no attunement upgrades applied. */
	public static final int BASE_SPELL_SLOTS = 5;

	/** The number of ticks between each time a continuous spell is added to the player's recently-cast spells. */
	private static final int CONTINUOUS_TRACKING_INTERVAL = 20;
	/** The increase in progression for casting spells of the matching element. */
	private static final float ELEMENTAL_PROGRESSION_MODIFIER = 1.2f;
	/** The increase in progression for casting an undiscovered spell (can only happen once per spell for each player). */
	private static final float DISCOVERY_PROGRESSION_MODIFIER = 5f;
	/** The increase in progression for tiers that the player has already reached. */
	private static final float SECOND_TIME_PROGRESSION_MODIFIER = 1.5f;
	/** The fraction of progression lost when all recently-cast spells are the same as the one being cast. */
	private static final float MAX_PROGRESSION_REDUCTION = 0.75f;

	public Tier tier;
	public Element element;

	public ItemWand(Tier tier, Element element){
		super();
		setMaxStackSize(1);
		setCreativeTab(WizardryTabs.GEAR);
		this.tier = tier;
		this.element = element;
		setMaxDamage(this.tier.maxCharge);
		WizardryRecipes.addToManaFlaskCharging(this);
		// TODO: Hook to allow addon devs to have this override apply to their own animations
		addPropertyOverride(new ResourceLocation("pointing"),
				(s, w, e) -> e != null && e.getActiveItemStack() == s
						&& (s.getItemUseAction() == SpellActions.POINT
						|| s.getItemUseAction() == SpellActions.POINT_UP
						|| s.getItemUseAction() == SpellActions.POINT_DOWN
						|| s.getItemUseAction() == SpellActions.GRAPPLE
						|| s.getItemUseAction() == SpellActions.SUMMON) ? 1 : 0);
	}
	
	@Override
	public Spell getCurrentSpell(ItemStack stack){
		return WandHelper.getCurrentSpell(stack);
	}

	@Override
	public Spell getNextSpell(ItemStack stack){
		return WandHelper.getNextSpell(stack);
	}

	@Override
	public Spell getPreviousSpell(ItemStack stack){
		return WandHelper.getPreviousSpell(stack);
	}

	@Override
	public Spell[] getSpells(ItemStack stack){
		return WandHelper.getSpells(stack);
	}

	@Override
	public void selectNextSpell(ItemStack stack){
		WandHelper.selectNextSpell(stack);
	}

	@Override
	public void selectPreviousSpell(ItemStack stack){
		WandHelper.selectPreviousSpell(stack);
	}

	@Override
	public boolean selectSpell(ItemStack stack, int index){
		return WandHelper.selectSpell(stack, index);
	}

	@Override
	public int getCurrentCooldown(ItemStack stack){
		return WandHelper.getCurrentCooldown(stack);
	}

	@Override
	public int getCurrentMaxCooldown(ItemStack stack){
		return WandHelper.getCurrentMaxCooldown(stack);
	}

	@Override
	public boolean showSpellHUD(EntityPlayer player, ItemStack stack){
		return true;
	}

	@Override
	public boolean showTooltip(ItemStack stack){
		return true;
	}

	/** Does nothing, use {@link ItemWand#setMana(ItemStack, int)} to modify wand mana. */
	@Override
	public void setDamage(ItemStack stack, int damage){
		// Overridden to do nothing to stop repair things from 'repairing' the mana in a wand
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
	public boolean isFull3D(){
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public net.minecraft.client.gui.FontRenderer getFontRenderer(ItemStack stack){
		return Wizardry.proxy.getFontRenderer(stack);
	}

	@Override
	public boolean isEnchantable(ItemStack stack){
		return false;
	}

	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book){
		return false;
	}

	@Override
	public boolean hasEffect(ItemStack stack){
		return !Wizardry.settings.legacyWandLevelling && this.tier.level < Tier.MASTER.level
				&& WandHelper.getProgression(stack) >= tier.next().progression;
	}

	@Override
	public int getRGBDurabilityForDisplay(ItemStack stack){
		return DrawingUtils.mix(0xff8bfe, 0x8e2ee4, (float)getDurabilityForDisplay(stack));
	}

	// Max damage is modifiable with upgrades.
	@Override
	public int getMaxDamage(ItemStack stack){
		// + 0.5f corrects small float errors rounding down
		return (int)(super.getMaxDamage(stack) * (1.0f + Constants.STORAGE_INCREASE_PER_LEVEL
				* WandHelper.getUpgradeLevel(stack, WizardryItems.storage_upgrade)) + 0.5f);
	}

	@Override
	public void onCreated(ItemStack stack, World worldIn, EntityPlayer playerIn){
		setMana(stack, 0); // Wands are empty when first crafted
	}

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isHeld){

		WandHelper.decrementCooldowns(stack);

		// Decrements wand damage (increases mana) every 1.5 seconds if it has a condenser upgrade
		if(!world.isRemote && !this.isManaFull(stack) && world.getTotalWorldTime() % Constants.CONDENSER_TICK_INTERVAL == 0){
			// If the upgrade level is 0, this does nothing anyway.
			this.rechargeMana(stack, WandHelper.getUpgradeLevel(stack, WizardryItems.condenser_upgrade));
		}
	}

	@Override
	public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack){

		Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(slot, stack);

		if(slot == EntityEquipmentSlot.MAINHAND){
			int level = WandHelper.getUpgradeLevel(stack, WizardryItems.melee_upgrade);
			// This check doesn't affect the damage output, but it does stop a blank line from appearing in the tooltip.
			if(level > 0 && !this.isManaEmpty(stack)){
				multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(),
						new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Melee upgrade modifier", 2 * level, 0));
				multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Melee upgrade modifier", -2.4000000953674316D, 0));
			}
		}

		return multimap;
	}

	@Override
	public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase wielder){

		int level = WandHelper.getUpgradeLevel(stack, WizardryItems.melee_upgrade);
		int mana = this.getMana(stack);

		if(level > 0 && mana > 0) this.consumeMana(stack, level * 4, wielder);

		return true;
	}

	@Override
	public boolean canDestroyBlockInCreative(World world, BlockPos pos, ItemStack stack, EntityPlayer player){
		return WandHelper.getUpgradeLevel(stack, WizardryItems.melee_upgrade) == 0;
	}

	// A proper hook was introduced for this in Forge build 14.23.5.2805 - Hallelujah, finally!
	// The discussion about this was quite interesting, see the following:
	// https://github.com/TeamTwilight/twilightforest/blob/1.12.x/src/main/java/twilightforest/item/ItemTFScepterLifeDrain.java
	// https://github.com/MinecraftForge/MinecraftForge/pull/4834
	// Among the things mentioned were that it can be 'fixed' by doing the exact same hacks that I did, and that
	// returning a result of PASS rather than SUCCESS from onItemRightClick also solves the problem (not sure why
	// though, and again it's not a perfect solution)
	// Edit: It seems that the hacky fix in previous versions actually introduced a wand duplication bug... oops

	@Override
	public boolean canContinueUsing(ItemStack oldStack, ItemStack newStack){
		// Ignore durability changes
		if(ItemStack.areItemsEqualIgnoreDurability(oldStack, newStack)) return true;
		return super.canContinueUsing(oldStack, newStack);
	}

	@Override
	public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack){
		// Ignore durability changes
		if(ItemStack.areItemsEqualIgnoreDurability(oldStack, newStack)) return false;
		return super.shouldCauseBlockBreakReset(oldStack, newStack);
	}

	@Override
	// Only called client-side
	// This method is always called on the item in oldStack, meaning that oldStack.getItem() == this
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged){

		// This method does some VERY strange things! Despite its name, it also seems to affect the updating of NBT...

		if(!oldStack.isEmpty() || !newStack.isEmpty()){
			// We only care about the situation where we specifically want the animation NOT to play.
			if(oldStack.getItem() == newStack.getItem() && !slotChanged && oldStack.getItem() instanceof ItemWand
					&& newStack.getItem() instanceof ItemWand
					&& WandHelper.getCurrentSpell(oldStack) == WandHelper.getCurrentSpell(newStack))
				return false;
		}

		return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
	}

	@Override
	public EnumAction getItemUseAction(ItemStack itemstack){
		return WandHelper.getCurrentSpell(itemstack).action;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack stack){
		return 72000;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void addInformation(ItemStack stack, World world, List<String> text, net.minecraft.client.util.ITooltipFlag advanced){

		EntityPlayer player = net.minecraft.client.Minecraft.getMinecraft().player;
		if (player == null) { return; }
		// +0.5f is necessary due to the error in the way floats are calculated.
		if(element != null) text.add(Wizardry.proxy.translate("item." + Wizardry.MODID + ":wand.buff",
				new Style().setColor(TextFormatting.DARK_GRAY),
				(int)((tier.level + 1) * Constants.POTENCY_INCREASE_PER_TIER * 100 + 0.5f), element.getDisplayName()));

		Spell spell = WandHelper.getCurrentSpell(stack);

		boolean discovered = true;
		if(Wizardry.settings.discoveryMode && !player.isCreative() && WizardData.get(player) != null
				&& !WizardData.get(player).hasSpellBeenDiscovered(spell)){
			discovered = false;
		}

		text.add(Wizardry.proxy.translate("item." + Wizardry.MODID + ":wand.spell", new Style().setColor(TextFormatting.GRAY),
				discovered ? spell.getDisplayNameWithFormatting() : "#" + TextFormatting.BLUE + SpellGlyphData.getGlyphName(spell, player.world)));

		if(advanced.isAdvanced()){
			// Advanced tooltips for debugging
			text.add(Wizardry.proxy.translate("item." + Wizardry.MODID + ":wand.mana", new Style().setColor(TextFormatting.BLUE),
					this.getMana(stack), this.getManaCapacity(stack)));

			text.add(Wizardry.proxy.translate("item." + Wizardry.MODID + ":wand.progression", new Style().setColor(TextFormatting.GRAY),
					WandHelper.getProgression(stack), this.tier.level < Tier.MASTER.level ? tier.next().progression : 0));
		}
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack){
		return (this.element == null ? "" : this.element.getFormattingCode()) + super.getItemStackDisplayName(stack);
	}

	// Continuous spells use the onUsingItemTick method instead of this one.
	/* An important thing to note about this method: it is only called on the server and the client of the player
	 * holding the item (I call this client-inconsistency). This means if you spawn particles here they will not show up
	 * on other players' screens. Instead, this must be done via packets. */
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand){

		ItemStack stack = player.getHeldItem(hand);

		// Alternate right-click function; overrides spell casting.
		if(this.selectMinionTarget(player, world)) return new ActionResult<>(EnumActionResult.SUCCESS, stack);

		Spell spell = WandHelper.getCurrentSpell(stack);
		SpellModifiers modifiers = this.calculateModifiers(stack, player, spell);

		if(canCast(stack, spell, player, hand, 0, modifiers)){
			// Need to account for the modifier since it could be zero even if the original charge-up wasn't
			int chargeup = (int)(spell.getChargeup() * modifiers.get(SpellModifiers.CHARGEUP));

			if(spell.isContinuous || chargeup > 0){
				// Spells that need the mouse to be held (continuous, charge-up or both)
				if(!player.isHandActive()){
					player.setActiveHand(hand);
					// Store the modifiers for use later
					if(WizardData.get(player) != null) WizardData.get(player).itemCastingModifiers = modifiers;
					if(chargeup > 0 && world.isRemote) Wizardry.proxy.playChargeupSound(player);
					return new ActionResult<>(EnumActionResult.SUCCESS, stack);
				}
			}else{
				// All other (instant) spells
				if(cast(stack, spell, player, hand, 0, modifiers)){
					return new ActionResult<>(EnumActionResult.SUCCESS, stack);
				}
			}
		}

		return new ActionResult<>(EnumActionResult.FAIL, stack);
	}

	// For continuous spells and spells with a charge-up time. The count argument actually decrements by 1 each tick.
	// N.B. The first time this gets called is the tick AFTER onItemRightClick is called, not the same tick
	@Override
	public void onUsingTick(ItemStack stack, EntityLivingBase user, int count){

		if(user instanceof EntityPlayer){

			EntityPlayer player = (EntityPlayer)user;

			Spell spell = WandHelper.getCurrentSpell(stack);

			SpellModifiers modifiers;

			if(WizardData.get(player) != null){
				modifiers = WizardData.get(player).itemCastingModifiers;
			}else{
				modifiers = this.calculateModifiers(stack, (EntityPlayer)user, spell); // Fallback to the old way, should never be used
			}

			int useTick = stack.getMaxItemUseDuration() - count;
			int chargeup = (int)(spell.getChargeup() * modifiers.get(SpellModifiers.CHARGEUP));

			if(spell.isContinuous){
				// Continuous spell charge-up is simple, just don't do anything until it's charged
				if(useTick >= chargeup){
					// castingTick needs to be relative to when the spell actually started
					int castingTick = useTick - chargeup;
					// Continuous spells (these must check if they can be cast each tick since the mana changes)
					// Don't call canCast when castingTick == 0 because we already did it in onItemRightClick - even
					// with charge-up times, because we don't want to trigger events twice
					if(castingTick == 0 || canCast(stack, spell, player, player.getActiveHand(), castingTick, modifiers)){
						cast(stack, spell, player, player.getActiveHand(), castingTick, modifiers);
					}else{
						// Stops the casting if it was interrupted, either by events or because the wand ran out of mana
						player.stopActiveHand();
					}
				}
			}else{
				// Non-continuous spells need to check they actually have a charge-up since ALL spells call setActiveHand
				if(chargeup > 0 && useTick == chargeup){
					// Once the spell is charged, it's exactly the same as in onItemRightClick
					cast(stack, spell, player, player.getActiveHand(), 0, modifiers);
				}
			}
		}
	}

	@Override
	public boolean canCast(ItemStack stack, Spell spell, EntityPlayer caster, EnumHand hand, int castingTick, SpellModifiers modifiers){

		// Spells can only be cast if the casting events aren't cancelled...
		if(castingTick == 0){
			if(MinecraftForge.EVENT_BUS.post(new SpellCastEvent.Pre(Source.WAND, spell, caster, modifiers))) return false;
		}else{
			if(MinecraftForge.EVENT_BUS.post(new SpellCastEvent.Tick(Source.WAND, spell, caster, modifiers, castingTick))) return false;
		}

		int cost = (int)(spell.getCost() * modifiers.get(SpellModifiers.COST) + 0.1f); // Weird floaty rounding

		// As of wizardry 4.2 mana cost is only divided over two intervals each second
		if(spell.isContinuous) cost = getDistributedCost(cost, castingTick);

		// ...and the wand has enough mana to cast the spell...
		return cost <= this.getMana(stack) // This comes first because it changes over time
				// ...and the wand is the same tier as the spell or higher...
				&& spell.getTier().level <= this.tier.level
				// ...and either the spell is not in cooldown or the player is in creative mode
				&& (WandHelper.getCurrentCooldown(stack) == 0 || caster.isCreative());
	}

	@Override
	public boolean cast(ItemStack stack, Spell spell, EntityPlayer caster, EnumHand hand, int castingTick, SpellModifiers modifiers){

		World world = caster.world;

		if(world.isRemote && !spell.isContinuous && spell.requiresPacket()) return false;

		if(spell.cast(world, caster, hand, castingTick, modifiers)){

			if(castingTick == 0) MinecraftForge.EVENT_BUS.post(new SpellCastEvent.Post(Source.WAND, spell, caster, modifiers));

			if(!world.isRemote){

				// Continuous spells never require packets so don't rely on the requiresPacket method to specify it
				if(!spell.isContinuous && spell.requiresPacket()){
					// Sends a packet to all players in dimension to tell them to spawn particles.
					IMessage msg = new PacketCastSpell.Message(caster.getEntityId(), hand, spell, modifiers);
					WizardryPacketHandler.net.sendToDimension(msg, world.provider.getDimension());
				}

				// Mana cost
				int cost = (int)(spell.getCost() * modifiers.get(SpellModifiers.COST) + 0.1f); // Weird floaty rounding
				// As of wizardry 4.2 mana cost is only divided over two intervals each second
				if(spell.isContinuous) cost = getDistributedCost(cost, castingTick);

				if(cost > 0) this.consumeMana(stack, cost, caster);

			}

			caster.setActiveHand(hand);

			// Cooldown
			if(!spell.isContinuous && !caster.isCreative()){ // Spells only have a cooldown in survival
				WandHelper.setCurrentCooldown(stack, (int)(spell.getCooldown() * modifiers.get(WizardryItems.cooldown_upgrade)));
			}

			// Progression
			if(this.tier.level < Tier.MASTER.level && castingTick % CONTINUOUS_TRACKING_INTERVAL == 0){

				// We don't care about cost modifiers here, otherwise players would be penalised for wearing robes!
				int progression = (int)(spell.getCost() * modifiers.get(SpellModifiers.PROGRESSION));
				WandHelper.addProgression(stack, progression);

				if(!Wizardry.settings.legacyWandLevelling){ // Don't display the message if legacy wand levelling is enabled
					// If the wand just gained enough progression to be upgraded...
					Tier nextTier = tier.next();
					int excess = WandHelper.getProgression(stack) - nextTier.progression;
					if(excess >= 0 && excess < progression){
						// ...display a message above the player's hotbar
						caster.playSound(WizardrySounds.ITEM_WAND_LEVELUP, 1.25f, 1);
						WizardryAdvancementTriggers.wand_levelup.triggerFor(caster);
						if(!world.isRemote)
							caster.sendMessage(new TextComponentTranslation("item." + Wizardry.MODID + ":wand.levelup",
									this.getItemStackDisplayName(stack), nextTier.getNameForTranslationFormatted()));
					}
				}

				WizardData.get(caster).trackRecentSpell(spell);
			}

			return true;
		}

		return false;
	}

	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase user, int timeLeft){

		if(user instanceof EntityPlayer){

			EntityPlayer player = (EntityPlayer)user;

			Spell spell = WandHelper.getCurrentSpell(stack);

			SpellModifiers modifiers;

			if(WizardData.get(player) != null){
				modifiers = WizardData.get(player).itemCastingModifiers;
			}else{
				modifiers = this.calculateModifiers(stack, (EntityPlayer)user, spell); // Fallback to the old way, should never be used
			}

			int castingTick = stack.getMaxItemUseDuration() - timeLeft; // Might as well include this

			int cost = getDistributedCost((int)(spell.getCost() * modifiers.get(SpellModifiers.COST) + 0.1f), castingTick);

			// Still need to check there's enough mana or the spell will finish twice, since running out of mana is
			// handled separately.
			if(spell.isContinuous && spell.getTier().level <= this.tier.level && cost <= this.getMana(stack)){

				MinecraftForge.EVENT_BUS.post(new SpellCastEvent.Finish(Source.WAND, spell, player, modifiers, castingTick));
				spell.finishCasting(world, player, Double.NaN, Double.NaN, Double.NaN, null, castingTick, modifiers);

				if(!player.isCreative()){ // Spells only have a cooldown in survival
					WandHelper.setCurrentCooldown(stack, (int)(spell.getCooldown() * modifiers.get(WizardryItems.cooldown_upgrade)));
				}
			}
		}
	}

	@Override
	public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase entity, EnumHand hand){

		if(player.isSneaking() && entity instanceof EntityPlayer && WizardData.get(player) != null){
			String string = WizardData.get(player).toggleAlly((EntityPlayer)entity) ? "item." + Wizardry.MODID + ":wand.addally"
					: "item." + Wizardry.MODID + ":wand.removeally";
			if(!player.world.isRemote) player.sendMessage(new TextComponentTranslation(string, entity.getName()));
			return true;
		}

		return false;
	}

	/** Distributes the given cost (which should be the per-second cost of a continuous spell) over a second and
	 * returns the appropriate cost to be applied for the given tick. Currently the cost is distributed over 2
	 * intervals per second, meaning the returned value is 0 unless {@code castingTick} is a multiple of 10.*/
	protected static int getDistributedCost(int cost, int castingTick){

		int partialCost;

		if(castingTick % 20 == 0){ // Whole number of seconds has elapsed
			partialCost = cost / 2 + cost % 2; // Make sure cost adds up to the correct value by adding the remainder here
		}else if(castingTick % 10 == 0){ // Something-and-a-half seconds has elapsed
			partialCost = cost/2;
		}else{ // Some other number of ticks has elapsed
			partialCost = 0; // Wands aren't damaged within half-seconds
		}

		return partialCost;
	}

	/** Returns a SpellModifiers object with the appropriate modifiers applied for the given ItemStack and Spell. */
	// This is now public because artefacts use it
	public SpellModifiers calculateModifiers(ItemStack stack, EntityPlayer player, Spell spell){

		SpellModifiers modifiers = new SpellModifiers();

		// Now we only need to add multipliers if they are not 1.
		int level = WandHelper.getUpgradeLevel(stack, WizardryItems.range_upgrade);
		if(level > 0)
			modifiers.set(WizardryItems.range_upgrade, 1.0f + level * Constants.RANGE_INCREASE_PER_LEVEL, true);

		level = WandHelper.getUpgradeLevel(stack, WizardryItems.duration_upgrade);
		if(level > 0)
			modifiers.set(WizardryItems.duration_upgrade, 1.0f + level * Constants.DURATION_INCREASE_PER_LEVEL, false);

		level = WandHelper.getUpgradeLevel(stack, WizardryItems.blast_upgrade);
		if(level > 0)
			modifiers.set(WizardryItems.blast_upgrade, 1.0f + level * Constants.BLAST_RADIUS_INCREASE_PER_LEVEL, true);

		level = WandHelper.getUpgradeLevel(stack, WizardryItems.cooldown_upgrade);
		if(level > 0)
			modifiers.set(WizardryItems.cooldown_upgrade, 1.0f - level * Constants.COOLDOWN_REDUCTION_PER_LEVEL, true);

		float progressionModifier = 1.0f - ((float)WizardData.get(player).countRecentCasts(spell) / WizardData.MAX_RECENT_SPELLS)
				* MAX_PROGRESSION_REDUCTION;

		if(this.element == spell.getElement()){
			modifiers.set(SpellModifiers.POTENCY, 1.0f + (this.tier.level + 1) * Constants.POTENCY_INCREASE_PER_TIER, true);
			progressionModifier *= ELEMENTAL_PROGRESSION_MODIFIER;
		}

		if(WizardData.get(player) != null){

			if(!WizardData.get(player).hasSpellBeenDiscovered(spell)){
				// Casting an undiscovered spell now grants 5x progression
				progressionModifier *= DISCOVERY_PROGRESSION_MODIFIER;
			}

			if(!WizardData.get(player).hasReachedTier(this.tier.next())){
				// 1.5x progression for tiers that have already been reached
				progressionModifier *= SECOND_TIME_PROGRESSION_MODIFIER;
			}
		}

		modifiers.set(SpellModifiers.PROGRESSION, progressionModifier, false);

		return modifiers;
	}

	private boolean selectMinionTarget(EntityPlayer player, World world){

		RayTraceResult rayTrace = RayTracer.standardEntityRayTrace(world, player, 16, false);

		if(rayTrace != null && EntityUtils.isLiving(rayTrace.entityHit)){

			EntityLivingBase entity = (EntityLivingBase)rayTrace.entityHit;

			// Sets the selected minion's target to the right-clicked entity
			if(player.isSneaking() && WizardData.get(player) != null && WizardData.get(player).selectedMinion != null){

				ISummonedCreature minion = WizardData.get(player).selectedMinion.get();

				if(minion instanceof EntityLiving && minion != entity){
					// There is now only the new AI! (which greatly improves things)
					((EntityLiving)minion).setAttackTarget(entity);
					// Deselects the selected minion
					WizardData.get(player).selectedMinion = null;
					return true;
				}
			}
		}

		return false;
	}

	// Workbench stuff

	@Override
	public int getSpellSlotCount(ItemStack stack){
		return BASE_SPELL_SLOTS + WandHelper.getUpgradeLevel(stack, WizardryItems.attunement_upgrade);
	}

	@Override
	public ItemStack applyUpgrade(@Nullable EntityPlayer player, ItemStack wand, ItemStack upgrade){

		// Upgrades wand if necessary. Damage is copied, preserving remaining durability,
		// and also the entire NBT tag compound.
		if(upgrade.getItem() == WizardryItems.arcane_tome){

			Tier tier = Tier.values()[upgrade.getItemDamage()];

			// Checks the wand upgrade is for the tier above the wand's tier, and that either the wand has enough
			// progression or the player is in creative mode.
			if((player == null || player.isCreative() || Wizardry.settings.legacyWandLevelling
					|| WandHelper.getProgression(wand) >= tier.progression)
					&& tier == this.tier.next() && this.tier != Tier.MASTER){

				if(Wizardry.settings.legacyWandLevelling){
					// Progression has little meaning with legacy upgrade mechanics so just reset it
					// In theory, you can get 'free' progression when upgrading since progression can't be negative,
					// so the flipside of that is you lose any excess
					WandHelper.setProgression(wand, 0);
				}else{
					// Carry excess progression over to the new stack
					WandHelper.setProgression(wand, WandHelper.getProgression(wand) - tier.progression);
				}

				if(player != null) WizardData.get(player).setTierReached(tier);

				ItemStack newWand = new ItemStack(WizardryItems.getWand(tier, this.element));
				newWand.setTagCompound(wand.getTagCompound());
				// This needs to be done after copying the tag compound so the mana capacity for the new wand
				// takes storage upgrades into account
				// Note the usage of the new wand item and not 'this' to ensure the correct capacity is used
				((IManaStoringItem)newWand.getItem()).setMana(newWand, this.getMana(wand));

				upgrade.shrink(1);

				return newWand;
			}

		}else if(WandHelper.isWandUpgrade(upgrade.getItem())){

			// Special upgrades
			Item specialUpgrade = upgrade.getItem();

			int maxUpgrades = this.tier.upgradeLimit;
			if(this.element == Element.MAGIC) maxUpgrades += Constants.NON_ELEMENTAL_UPGRADE_BONUS;

			if(WandHelper.getTotalUpgrades(wand) < maxUpgrades
					&& WandHelper.getUpgradeLevel(wand, specialUpgrade) < Constants.UPGRADE_STACK_LIMIT){

				// Used to preserve existing mana when upgrading storage rather than creating free mana.
				int prevMana = this.getMana(wand);

				WandHelper.applyUpgrade(wand, specialUpgrade);

				// Special behaviours for specific upgrades
				if(specialUpgrade == WizardryItems.storage_upgrade){

					this.setMana(wand, prevMana);

				}else if(specialUpgrade == WizardryItems.attunement_upgrade){

					int newSlotCount = BASE_SPELL_SLOTS + WandHelper.getUpgradeLevel(wand,
							WizardryItems.attunement_upgrade);

					Spell[] spells = WandHelper.getSpells(wand);
					Spell[] newSpells = new Spell[newSlotCount];

					for(int i = 0; i < newSpells.length; i++){
						newSpells[i] = i < spells.length && spells[i] != null ? spells[i] : Spells.none;
					}

					WandHelper.setSpells(wand, newSpells);

					int[] cooldowns = WandHelper.getCooldowns(wand);
					int[] newCooldowns = new int[newSlotCount];

					if(cooldowns.length > 0){
						System.arraycopy(cooldowns, 0, newCooldowns, 0, cooldowns.length);
					}

					WandHelper.setCooldowns(wand, newCooldowns);
				}

				upgrade.shrink(1);

				if(player != null){

					WizardryAdvancementTriggers.special_upgrade.triggerFor(player);

					if(WandHelper.getTotalUpgrades(wand) == Tier.MASTER.upgradeLimit){
						WizardryAdvancementTriggers.max_out_wand.triggerFor(player);
					}
				}

			}
		}

		return wand;
	}

	@Override
	public boolean onApplyButtonPressed(EntityPlayer player, Slot centre, Slot crystals, Slot upgrade, Slot[] spellBooks){
		
		boolean changed = false; // Used for advancements

		if(upgrade.getHasStack()){
			ItemStack original = centre.getStack().copy();
			centre.putStack(this.applyUpgrade(player, centre.getStack(), upgrade.getStack()));
			changed = ItemStack.areItemStacksEqual(centre.getStack(), original);
		}

		// Reads NBT spell metadata array to variable, edits this, then writes it back to NBT.
		// Original spells are preserved; if a slot is left empty the existing spell binding will remain.
		// Accounts for spells which cannot be applied because they are above the wand's tier; these spells
		// will not bind but the existing spell in that slot will remain and other applicable spells will
		// be bound as normal, along with any upgrades and crystals.
		Spell[] spells = WandHelper.getSpells(centre.getStack());
		
		if(spells.length <= 0){
			// Base value here because if the spell array doesn't exist, the wand can't possibly have attunement upgrades
			spells = new Spell[BASE_SPELL_SLOTS];
		}
		
		for(int i = 0; i < spells.length; i++){
			if(spellBooks[i].getStack() != ItemStack.EMPTY){
				
				Spell spell = Spell.byMetadata(spellBooks[i].getStack().getItemDamage());
				// If the wand is powerful enough for the spell, it's not already bound to that slot and it's enabled for wands
				if(!(spell.getTier().level > this.tier.level) && spells[i] != spell && spell.isEnabled(SpellProperties.Context.WANDS)){
					spells[i] = spell;
					changed = true;
				}
			}
		}
		
		WandHelper.setSpells(centre.getStack(), spells);

		// Charges wand by appropriate amount
		if(crystals.getStack() != ItemStack.EMPTY && !this.isManaFull(centre.getStack())){
			
			int chargeDepleted = this.getManaCapacity(centre.getStack()) - this.getMana(centre.getStack());

			int manaPerItem = Constants.MANA_PER_CRYSTAL;
			if(crystals.getStack().getItem() == WizardryItems.crystal_shard) manaPerItem = Constants.MANA_PER_SHARD;
			if(crystals.getStack().getItem() == WizardryItems.grand_crystal) manaPerItem = Constants.GRAND_CRYSTAL_MANA;
			
			if(crystals.getStack().getCount() * manaPerItem < chargeDepleted){
				// If there aren't enough crystals to fully charge the wand
				this.rechargeMana(centre.getStack(), crystals.getStack().getCount() * manaPerItem);
				crystals.decrStackSize(crystals.getStack().getCount());

			}else{
				// If there are excess crystals (or just enough)
				this.setMana(centre.getStack(), this.getManaCapacity(centre.getStack()));
				crystals.decrStackSize((int)Math.ceil(((double)chargeDepleted) / manaPerItem));
			}

			changed = true;
		}
		
		return changed;
	}

	// hitEntity is only called server-side, so we'll have to use events
	@SubscribeEvent
	public static void onAttackEntityEvent(AttackEntityEvent event){

		EntityPlayer player = event.getEntityPlayer();
		ItemStack stack = player.getHeldItemMainhand(); // Can't melee with offhand items

		if(stack.getItem() instanceof IManaStoringItem){

			// Nobody said it had to be a wand, as long as it's got a melee upgrade it counts
			int level = WandHelper.getUpgradeLevel(stack, WizardryItems.melee_upgrade);
			int mana = ((IManaStoringItem)stack.getItem()).getMana(stack);

			if(level > 0 && mana > 0){

				Random random = player.world.rand;

				player.world.playSound(player.posX, player.posY, player.posZ, WizardrySounds.ITEM_WAND_MELEE, SoundCategory.PLAYERS, 0.75f, 1, false);

				if(player.world.isRemote){

					Vec3d origin = player.getPositionEyes(1);
					Vec3d hit = origin.add(player.getLookVec().scale(player.getDistance(event.getTarget())));
					// Generate two perpendicular vectors in the plane perpendicular to the look vec
					Vec3d vec1 = player.getLookVec().rotatePitch(90);
					Vec3d vec2 = player.getLookVec().crossProduct(vec1);

					for(int i = 0; i < 15; i++){
						ParticleBuilder.create(Type.SPARKLE).pos(hit)
								.vel(vec1.scale(random.nextFloat() * 0.3f - 0.15f).add(vec2.scale(random.nextFloat() * 0.3f - 0.15f)))
								.clr(1f, 1f, 1f).fade(0.3f, 0.5f, 1)
								.time(8 + random.nextInt(4)).spawn(player.world);
					}
				}
			}
		}
	}

}
