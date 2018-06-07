package electroblob.wizardry.item;

import java.lang.ref.WeakReference;
import java.util.List;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.ExtendedPlayer;
import electroblob.wizardry.SpellGlyphData;
import electroblob.wizardry.WandHelper;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.entity.living.EntitySummonedCreature;
import electroblob.wizardry.packet.PacketCastSpell;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.spell.Spell;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

/** This class is (literally) where the magic happens! All wand types are single instances of this class. There's a lot
 * of quite hard-to-read code in here, but unfortunately there's not much I can do about that. For this reason, I have
 * written the {@link WandHelper} class.<i> I strongly recommend you use it for interacting with wand items wherever
 * possible.</i>
 * <p>
 * It's unikely that anything in this class will be of much use externally, but should you wish to use it for whatever
 * reason (perhaps if you extend it), it works as follows:
 * <p>
 * - onItemRightClick is where non-continuous spells are cast, and it sets the item in use for continuous spells<br>
 * - onUsingTick does the casting for continuous spells<br>
 * - onUpdate deals with the cooldowns for the spells
 * @since Wizardry 1.0 */
public class ItemWand extends Item {

	public EnumTier tier;
	public EnumElement element;

	public ItemWand(EnumTier enumtier, EnumElement enumelement) {
		super();
		setMaxStackSize(1);
		if(enumelement == null || enumtier == EnumTier.BASIC){
			setCreativeTab(Wizardry.tabWizardry);
		}
		this.tier = enumtier;
		this.element = enumelement;
		this.setMaxDamage(tier.maxCharge);
	}

	@Override
	public boolean isFull3D(){
		return true;
	}

	// Max damage is modifiable with upgrades.
	@Override
	public int getMaxDamage(ItemStack itemstack){
		// + 0.5f corrects small float errors rounding down
		return (int)(getMaxDamage()*(1.0f + Wizardry.STORAGE_INCREASE_PER_LEVEL * WandHelper.getUpgradeLevel(itemstack, Wizardry.storageUpgrade)) + 0.5f);
	}

	@Override
	public void onCreated(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer){
		/* Removed because of mana flasks, which would cause a new book to be given each time a mana flask
		 * is crafted with the wand. Handbook is now given on acquiring a crystal.
		ExtendedPlayer properties = ExtendedPlayer.get(par3EntityPlayer);
		if(properties != null && !properties.handbookGiven){
			par3EntityPlayer.inventory.addItemStackToInventory(new ItemStack(Wizardry.wizardHandbook));
			properties.handbookGiven = true;
		}*/
		par3EntityPlayer.triggerAchievement(Wizardry.arcaneInitiate);
	}

	@Override
	public void onUpdate(ItemStack itemstack, World world, Entity entity, int slot, boolean isHeld){

		WandHelper.decrementCooldowns(itemstack);

		// Decrements wand damage (increases mana) every 1.5 seconds if it has a condenser upgrade
		if(!world.isRemote && itemstack.isItemDamaged() && world.getWorldTime() % Wizardry.CONDENSER_TICK_INTERVAL == 0){
			// If the upgrade level is 0, this does nothing anyway.
			itemstack.setItemDamage(itemstack.getItemDamage() - WandHelper.getUpgradeLevel(itemstack, Wizardry.condenserUpgrade));
		}

		if(entity instanceof EntityPlayer && this.element != null && this.element != EnumElement.MAGIC){
			// As it stands, this will trigger every tick. Not ideal, but I can't find a way to detect if a player
			// has a certain achievement.
			// EDIT: There is a way to check, using StatFileWriter#hasAchievementUnlocked, but this ends up calling the same
			// thing as triggerAchievement anyway, meaning there's no point and it's probably not much of a problem anyway.
			((EntityPlayer)entity).triggerAchievement(Wizardry.elemental);
		}
	}

	@Override
	public EnumAction getItemUseAction(ItemStack itemstack){
		return WandHelper.getCurrentSpell(itemstack).action;
	}

	@Override
	public int getMaxItemUseDuration(ItemStack par1ItemStack){
		return 72000;
	}

	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List text, boolean par4){

		// +0.5f is necessary due to the error in the way floats are calculated.
		if(element != null) text.add("\u00A78" + StatCollector.translateToLocalFormatted("item.wand.buff", (int)((tier.level+1)*Wizardry.DAMAGE_INCREASE_PER_TIER*100 + 0.5f) + "%", element.getDisplayName()));

		Spell spell = WandHelper.getCurrentSpell(itemstack);

		boolean discovered = true;
		if(Wizardry.discoveryMode && !player.capabilities.isCreativeMode && ExtendedPlayer.get(player) != null
				&& !ExtendedPlayer.get(player).hasSpellBeenDiscovered(spell)){
			discovered = false;
		}

		text.add("\u00A77" + StatCollector.translateToLocalFormatted("item.wand.spell", discovered ? "\u00A77" + spell.getDisplayNameWithFormatting() : "#\u00A79" + SpellGlyphData.getGlyphName(spell, player.worldObj)));

		text.add("\u00A79" + StatCollector.translateToLocalFormatted("item.wand.mana", (this.getMaxDamage(itemstack) - this.getDamage(itemstack)), this.getMaxDamage(itemstack)));
	}

	@Override
	public String getItemStackDisplayName(ItemStack p_77653_1_){

		return ((this.element == null ? "" : this.element.colour) + StatCollector.translateToLocal(this.getUnlocalizedNameInefficiently(p_77653_1_) + ".name")).trim();
	}

	// Continuous spells use the onUsingItemTick method instead of this one.
	/* An important thing to note about this method: it is only called on the server and the client of the player
	 * holding the item. This means if you spawn particles here they will not show up on other players' screens.
	 * Instead, this must be done via packets. */
	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player){
		
		if(this.selectMinionTarget(player, world)) return stack;

		if(player.isPotionActive(Wizardry.arcaneJammer)) return stack;

		Spell spell = WandHelper.getCurrentSpell(stack);

		// If a spell is disabled in the config, it will not work.
		if(!spell.isEnabled()){
			if(!world.isRemote) player.addChatMessage(new ChatComponentTranslation("spell.disabled", spell.getDisplayNameWithFormatting()));
			return stack;
		}

		// This is here to start the inUse thing, otherwise the onItemUsingTick method will not fire.
		// If the castSpell method then returns false nothing will happen (continuous spells have no EnumAction
		// either so setting the item in use has no direct visible effect).
		// Edit: Strictly speaking this is not true but the spells that do have an action (shield, shadow ward and levitation)
		// will never return false.
		if(spell.isContinuous && !player.isUsingItem()){
			player.setItemInUse(stack, this.getMaxItemUseDuration(stack));
		}

		// Conditions for the spell to be attempted. The tier check is a failsafe; it should never be false unless the
		// NBT is modified directly.
		if(!spell.isContinuous && spell.tier.level <= this.tier.level
				&& spell.cost <= (stack.getMaxDamage() - stack.getItemDamage())
				&& (WandHelper.getCurrentCooldown(stack) == 0 || player.capabilities.isCreativeMode)){

			// = Spell modifiers =
			float rangeMultiplier = 1.0f + WandHelper.getUpgradeLevel(stack, Wizardry.rangeUpgrade)*Wizardry.RANGE_INCREASE_PER_LEVEL;
			float durationMultiplier = 1.0f + WandHelper.getUpgradeLevel(stack, Wizardry.durationUpgrade)*Wizardry.DURATION_INCREASE_PER_LEVEL;
			float blastMultiplier = 1.0f + WandHelper.getUpgradeLevel(stack, Wizardry.blastUpgrade)*Wizardry.BLAST_RADIUS_INCREASE_PER_LEVEL;

			// I would have liked to have made potion effects increase in strength according to the damage multiplier,
			// but the amplifier level is too discrete to make this work. For example, wither 3 for 10 seconds will kill a
			// normal mob on full 20 health, but wither 2 for the same duration only deals about 6 hearts of damage in total.
			float damageMultiplier = 1.0f;

			if(this.element == spell.element){
				damageMultiplier = 1.0f + (this.tier.level + 1) * Wizardry.DAMAGE_INCREASE_PER_TIER;
			}

			// If the spell does not require a packet, the code is run in the old client-inconsistent way, since this
			// means that swingItem() doesn't need packets in order to work, improving performance.
			if(!world.isRemote){

				if(spell.cast(world, player, 0, damageMultiplier, rangeMultiplier, durationMultiplier, blastMultiplier)){

					// = Packets =
					if(spell.doesSpellRequirePacket()){
						// Sends a packet to all players in dimension to tell them to spawn particles.
						// Only sent if the spell succeeded, because if the spell failed, you wouldn't
						// need to spawn any particles!
						IMessage msg = new PacketCastSpell.Message(player.getEntityId(), 0, spell.id(), damageMultiplier, rangeMultiplier, blastMultiplier);
						WizardryPacketHandler.net.sendToDimension(msg, world.provider.dimensionId);
					}

					player.setItemInUse(stack, this.getMaxItemUseDuration(stack));

					// = Discovery =
					if(!player.capabilities.isCreativeMode && !ExtendedPlayer.get(player).hasSpellBeenDiscovered(spell) && Wizardry.discoveryMode){
						player.worldObj.playSoundAtEntity(player, "random.levelup", 1.25f, 1);
						if(!player.worldObj.isRemote) player.addChatMessage(new ChatComponentTranslation("spell.discover", spell.getDisplayNameWithFormatting()));
					}
					ExtendedPlayer.get(player).discoverSpell(spell);

					// = Cooldown =
					// Spells only have a cooldown in survival
					if(!player.capabilities.isCreativeMode){

						float cooldownMultiplier = 1.0f - WandHelper.getUpgradeLevel(stack, Wizardry.cooldownUpgrade)*Wizardry.COOLDOWN_REDUCTION_PER_LEVEL;

						if(player.isPotionActive(Wizardry.fontOfMana)){
							// Dividing by this rather than setting it takes upgrades and font of mana into account simultaneously
							cooldownMultiplier /= 2 + player.getActivePotionEffect(Wizardry.fontOfMana).getAmplifier();
						}

						WandHelper.setCurrentCooldown(stack, (int)(spell.cooldown * cooldownMultiplier));
					}

					// = Mana cost =
					// The spell costs 20% less for every armour piece of the matching element.
					int armourPieces = 0;

					for(int i=0; i<4; i++){
						if(player.getCurrentArmor(i) != null && player.getCurrentArmor(i).getItem() instanceof ItemWizardArmour
								&& ((ItemWizardArmour)player.getCurrentArmor(i).getItem()).element == spell.element) armourPieces++;
					}

					stack.damageItem((int)(spell.cost * (1.0f - armourPieces*Wizardry.COST_REDUCTION_PER_ARMOUR)), player);

				}
				
			// Client-inconsistent spells only.
			}else if(!spell.doesSpellRequirePacket()){
				// This is all that needs to happen, because everything up there works fine on just the server side.
				if(spell.cast(world, player, 0, damageMultiplier, rangeMultiplier, durationMultiplier, blastMultiplier)){
					// Added in version 1.1.3 to fix the client-side spell discovery not updating for spells with the
					// packet optimisation.
					if(ExtendedPlayer.get(player) != null){
						ExtendedPlayer.get(player).discoverSpell(spell);
					}
				}
			}
		}
		return stack;
	}


	// For continuous spells and (deprecated) spells with a charge up time.
	// The count argument actually decrements by 1 each tick.
	@Override
	public void onUsingTick(ItemStack stack, EntityPlayer player, int count){

		Spell spell = WandHelper.getCurrentSpell(stack);

		ExtendedPlayer properties = ExtendedPlayer.get(player);

		// Continuous spells (these must check if they can be cast each tick since the mana changes)
		if(spell.isContinuous && spell.tier.level <= this.tier.level
				&& spell.cost/5 <= (stack.getMaxDamage() - stack.getItemDamage() - properties.damageToApply)){

			// = Spell modifiers =
			float rangeMultiplier = 1.0f + WandHelper.getUpgradeLevel(stack, Wizardry.rangeUpgrade)*Wizardry.RANGE_INCREASE_PER_LEVEL;
			float durationMultiplier = 1.0f + WandHelper.getUpgradeLevel(stack, Wizardry.durationUpgrade)*Wizardry.DURATION_INCREASE_PER_LEVEL;
			float blastMultiplier = 1.0f + WandHelper.getUpgradeLevel(stack, Wizardry.blastUpgrade)*Wizardry.BLAST_RADIUS_INCREASE_PER_LEVEL;

			// I would have liked to have made potion effects increase in strength according to the damage multiplier,
			// but the amplifier level is too discrete to make this work. For example, wither 3 for 10 seconds will kill a
			// normal mob on full 20 health, but wither 2 for the same duration only deals about 6 hearts of damage in total.
			float damageMultiplier = 1.0f;

			if(this.element == spell.element){
				damageMultiplier = 1.0f + (this.tier.level + 1) * Wizardry.DAMAGE_INCREASE_PER_TIER;
			}

			if(spell.cast(player.worldObj, player, stack.getMaxItemUseDuration() - count, damageMultiplier, rangeMultiplier, durationMultiplier, blastMultiplier)){

				// = Discovery =
				if(!player.capabilities.isCreativeMode && !properties.hasSpellBeenDiscovered(spell) && Wizardry.discoveryMode){
					player.worldObj.playSoundAtEntity(player, "random.levelup", 1.25f, 1);
					if(!player.worldObj.isRemote) player.addChatMessage(new ChatComponentTranslation("spell.discover", spell.getDisplayNameWithFormatting()));
				}
				properties.discoverSpell(spell);

				// = Mana cost =
				// Divides the mana cost over a second appropriately; since damage is an integer it cannot
				// just be divided by 20.
				// Now does five times per second regardless of the spell cost, but each time it does 1/5 of the
				// cost per second.
				// Removed the tick counter because it is cumbersome and stupid, when I can just as easily use the
				// count argument given right here.
				//properties.incrementTickNumber();
				int tickNumber = (count % 20) + 1;
				// Tests if the tick counter is a multiple of 4 plus 1, i.e. is true when tickNumber = 1, 5, 9, 13 or 17.
				// Made a slight adjustment since the counter starts on 1 and not 4.
				if(tickNumber % 4 == 1){

					int armourPieces = 0;

					for(int i=0; i<4; i++){
						if(player.getCurrentArmor(i) != null && player.getCurrentArmor(i).getItem() instanceof ItemWizardArmour
								&& ((ItemWizardArmour)player.getCurrentArmor(i).getItem()).element == spell.element) armourPieces++;
					}

					switch(armourPieces){

					case 0: properties.damageToApply += spell.cost/5;
					break;

					case 1: if(tickNumber != 17) properties.damageToApply += spell.cost/5;
					break;

					case 2: if(tickNumber != 9 && tickNumber != 17) properties.damageToApply += spell.cost/5;
					break;

					case 3: if(tickNumber != 5 && tickNumber != 13 && tickNumber != 17) properties.damageToApply += spell.cost/5;
					break;

					case 4: if(tickNumber == 1) properties.damageToApply += spell.cost/5;
					break;

					}
				}
			}
		}
	}

	/**
	 * Called when the player releases the use item button. Args: itemstack, world, entityplayer, itemInUseCount.
	 * Here it is used to apply damage after using a continuous spell. The damage shows up on the damage bar in real-
	 * time thanks to the getItemDamageForDisplay function, but is not actually applied until the button is released.
	 * This is because applying damage causes the wand to 're-equip', interrupting the spell.
	 * (Note that if the wand has no damage then minecraft doesn't try to render the bar, so the getItemDamageForDisplay
	 * function is never called, meaning if you use a continuous spell on an undamaged wand the damage doesn't appear
	 * until you release the mouse. A minor bug, but there's nothing I can do about it.)
	 */
	@Override
	public void onPlayerStoppedUsing(ItemStack itemstack, World world, EntityPlayer entityplayer, int par4) {
		ExtendedPlayer properties = ExtendedPlayer.get(entityplayer);
		// This if statement should always be true, but is here just in case to stop the wand breaking.
		if(properties.damageToApply <= itemstack.getMaxDamage() - itemstack.getItemDamage()){
			itemstack.damageItem(properties.damageToApply, entityplayer);
		}else{
			itemstack.setItemDamage(itemstack.getMaxDamage());
		}
		properties.damageToApply = 0;
	}

	@Override
	public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player,
			EntityLivingBase entity){

		if(player.isSneaking() && entity instanceof EntityPlayer && ExtendedPlayer.get(player) != null){
			// This is one of those "the method doing the work looks as if it's just returning a value" situations.
			// ... I know, right?! I feel very programmer-y. But it's not too confusing here, and it looks neat.
			String string = ExtendedPlayer.get(player).toggleAlly((EntityPlayer)entity) ? "item.wand.addally" : "item.wand.removeally";
			if(!player.worldObj.isRemote) player.addChatMessage(new ChatComponentTranslation(string, entity.getCommandSenderName()));
			return true;
		}

		return false;
	}
	
	private boolean selectMinionTarget(EntityPlayer player, World world){
	
		MovingObjectPosition rayTrace = WizardryUtilities.standardEntityRayTrace(world, player, 16);
		
		if(rayTrace != null && rayTrace.entityHit instanceof EntityLivingBase){
			
			EntityLivingBase entity = (EntityLivingBase)rayTrace.entityHit;
			
			// Sets the selected minion's target to the right-clicked entity
			if(player.isSneaking() && ExtendedPlayer.get(player) != null && ExtendedPlayer.get(player).selectedMinion != null){
				
				EntitySummonedCreature minion = ExtendedPlayer.get(player).selectedMinion.get();
				
				if(minion != null && minion != entity){
					// New AI
					minion.setAttackTarget(entity);
					// Old AI
					minion.setTarget(entity);
					// Deselects the selected minion
					ExtendedPlayer.get(player).selectedMinion = null;
					return true;
				}
			}
		}
		
		return false;
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack){
		return Wizardry.proxy.getWandDisplayDamage(stack);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public FontRenderer getFontRenderer(ItemStack stack){
		return Wizardry.proxy.getFontRenderer(stack);
	}
}
