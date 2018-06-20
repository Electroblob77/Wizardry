package electroblob.wizardry.item;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.event.SpellCastEvent;
import electroblob.wizardry.event.SpellCastEvent.Source;
import electroblob.wizardry.packet.PacketCastSpell;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.registry.WizardryTabs;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemScroll extends Item {

	public ItemScroll(){
		super();
		setHasSubtypes(true);
		setMaxStackSize(1);
		setCreativeTab(WizardryTabs.SPELLS);
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list){
		if (isInCreativeTab(tab)) {
			for(Spell spell : Spell.getSpells(Spell.nonContinuousSpells)){
				list.add(new ItemStack(this, 1, spell.id()));
			}
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
		 * case the server wants to do something with it, and in that case returning world-specific gobbledegook is not
		 * particularly helpful. On the other hand, something might happen that causes this method to be called on the
		 * server side, but the result to then be sent to the client, which means broken discovery system. Simply put, I
		 * can't predict that, and it's not my job to cater for other people's incorrect usage of code, especially when
		 * that might compromise some perfectly reasonable use (think Bibliocraft's 'best guess' book detection). */
		return Wizardry.proxy.getScrollDisplayName(stack);

	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand){

		ItemStack stack = player.getHeldItem(hand);

		Spell spell = Spell.get(stack.getItemDamage());
		// By default, scrolls have no modifiers - but with the event system, they could be added.
		SpellModifiers modifiers = new SpellModifiers();

		// If anything stops the spell working at this point, nothing else happens.
		if(MinecraftForge.EVENT_BUS.post(new SpellCastEvent.Pre(player, spell, modifiers, Source.SCROLL))){
			return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);
		}

		if(!spell.isContinuous){

			if(!world.isRemote){

				if(spell.cast(world, player, hand, 0, new SpellModifiers())){

					MinecraftForge.EVENT_BUS.post(new SpellCastEvent.Post(player, spell, modifiers, Source.SCROLL));

					if(spell.doesSpellRequirePacket()){
						// Sends a packet to all players in dimension to tell them to spawn particles.
						IMessage msg = new PacketCastSpell.Message(player.getEntityId(), hand, spell.id(), modifiers);
						WizardryPacketHandler.net.sendToDimension(msg, world.provider.getDimension());
					}

					// Scrolls are consumed upon successful use in survival mode
					if(!player.capabilities.isCreativeMode) stack.shrink(1);

					return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
				}

				// This else if check was bugging me for AGES! I can't believe I didn't compare to ItemWand before.
			}else if(!spell.doesSpellRequirePacket()){
				// Client-inconsistent spell casting. This code only runs client-side.
				if(spell.cast(world, player, hand, 0, modifiers)){
					// This is all that needs to happen, because everything above works fine on just the server side.
					MinecraftForge.EVENT_BUS.post(new SpellCastEvent.Post(player, spell, modifiers, Source.SCROLL));
					return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
				}
			}
		}

		return new ActionResult<ItemStack>(EnumActionResult.FAIL, stack);

	}

	@Override
	@SideOnly(Side.CLIENT)
	public FontRenderer getFontRenderer(ItemStack stack){
		return Wizardry.proxy.getFontRenderer(stack);
	}
}
