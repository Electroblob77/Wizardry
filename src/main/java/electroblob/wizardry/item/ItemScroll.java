package electroblob.wizardry.item;

import java.util.List;

import electroblob.wizardry.WizardData;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.packet.PacketCastSpell;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.registry.WizardryTabs;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemScroll extends Item {

	public ItemScroll(){
		super();
		this.setHasSubtypes(true);
		this.setMaxStackSize(1);
		this.setCreativeTab(WizardryTabs.SPELLS);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs par2CreativeTabs, List<ItemStack> list){
		// Isn't this sooooo much neater with the filter thing?
		for(Spell spell : Spell.getSpells(Spell.nonContinuousSpells)){
			list.add(new ItemStack(item, 1, spell.id()));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack stack){
		return true;
	}

	@Override
	// Item's version of this method is, quite frankly, an abomination. Why is a deprecated method being used as such
	// an integral part of the code? And what's the point in getUnlocalisedNameInefficiently?
	public String getItemStackDisplayName(ItemStack stack){
		
		/* Ok, so this method can be called from either the client or the server side. Obviously, on the client the
		 * spell name is either translated or obfuscated, then it is put into the item name as part of that translation.
		 * On the server side, however, there's a problem: on the one hand, the spell name shouldn't be obfuscated in
		 * case the server wants to do something with it, and in that case returning world-specific gobbledegook is
		 * not particularly helpful. On the other hand, something might happen that causes this method to be called on
		 * the server side, but the result to then be sent to the client, which means broken discovery system.
		 * Simply put, I can't predict that, and it's not my job to cater for other people's incorrect usage of code,
		 * especially when that might compromise some perfectly reasonable use (think Bibliocraft's 'best guess' book
		 * detection). */
		// TODO: Backport this proxy-based fix.
		return Wizardry.proxy.getScrollDisplayName(stack);

	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack stack, World world, EntityPlayer player, EnumHand hand){

		if(player.isPotionActive(WizardryPotions.arcane_jammer)) return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);;

		Spell spell = Spell.get(stack.getItemDamage());

		// If a spell is disabled in the config, it will not work.
		if(!spell.isEnabled()){
			if(!world.isRemote) player.addChatMessage(new TextComponentTranslation("spell.disabled", spell.getNameForTranslationFormatted()));
			return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
		}

		if(!spell.isContinuous){

			/*
			if(spell.chargeup > 0 && !entityplayer.isUsingItem()){
				// Spells with a chargeup time are now handled separately.
				entityplayer.setItemInUse(stack, this.getMaxItemUseDuration(stack));
				return stack;
			}
			 */

			if(!world.isRemote){
				
				if(spell.cast(world, player, hand, 0, new SpellModifiers())){

					if(spell.doesSpellRequirePacket()){
						// Sends a packet to all players in dimension to tell them to spawn particles.
						IMessage msg = new PacketCastSpell.Message(player.getEntityId(), hand, spell.id(), new SpellModifiers());
						WizardryPacketHandler.net.sendToDimension(msg, world.provider.getDimension());
					}

					if(!player.capabilities.isCreativeMode && !WizardData.get(player).hasSpellBeenDiscovered(spell) && Wizardry.settings.discoveryMode){
						player.playSound(SoundEvents.ENTITY_PLAYER_LEVELUP, 1.25f, 1);
						if(!player.worldObj.isRemote) player.addChatMessage(new TextComponentTranslation("spell.discover", spell.getNameForTranslationFormatted()));
					}
					WizardData.get(player).discoverSpell(spell);

					if(!player.capabilities.isCreativeMode) stack.stackSize--;

					return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
				}

			// Client-inconsistent spell casting. The code inside the else if statement only runs client-side.
			// This else if check was bugging me for AGES! I can't believe I didn't compare to ItemWand before.
			}else if(!spell.doesSpellRequirePacket()){
				// This is all that needs to happen, because everything above works fine on just the server side.
				if(spell.cast(world, player, hand, 0, new SpellModifiers())){
					// Added in version 1.1.3 to fix the client-side spell discovery not updating for spells with the
					// packet optimisation.
					if(WizardData.get(player) != null){
						WizardData.get(player).discoverSpell(spell);
					}

					new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
				}
			}
		}

		return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);

	}

	@Override
	@SideOnly(Side.CLIENT)
	public net.minecraft.client.gui.FontRenderer getFontRenderer(ItemStack stack){
		return Wizardry.proxy.getFontRenderer(stack);
	}
}
