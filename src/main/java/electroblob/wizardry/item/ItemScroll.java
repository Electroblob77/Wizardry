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
import net.minecraft.entity.EntityLivingBase;
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
	
	/** The maximum number of ticks a continuous spell scroll can be cast for (by holding the use item button). */
	// TODO: Make this configurable
	public static final int CASTING_TIME = 120;

	public ItemScroll(){
		super();
		setHasSubtypes(true);
		setMaxStackSize(16);
		setCreativeTab(WizardryTabs.SPELLS);
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list){
		if(tab == WizardryTabs.SPELLS){
			// In this particular case, getTotalSpellCount() is a more efficient way of doing this since the spell instance
			// is not required, only the id.
			for(int i = 0; i < Spell.getTotalSpellCount(); i++){
				// i+1 is used so that the metadata ties up with the id() method. In other words, the none spell has id
				// 0 and since this is not used as a spell book the metadata starts at 1.
				list.add(new ItemStack(this, 1, i + 1));
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
	public int getMaxItemUseDuration(ItemStack stack){
		return CASTING_TIME;
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

		// Now we can cast continuous spells with scrolls!
		if(spell.isContinuous){
			if(!player.isHandActive()){
				player.setActiveHand(hand);
				return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
			}
		}else{

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
	
	// For continuous spells. The count argument actually decrements by 1 each tick.
	@Override
	public void onUsingTick(ItemStack stack, EntityLivingBase user, int count){

		if(user instanceof EntityPlayer){

			EntityPlayer player = (EntityPlayer)user;

			Spell spell = Spell.get(stack.getItemDamage());
			// By default, scrolls have no modifiers - but with the event system, they could be added.
			SpellModifiers modifiers = new SpellModifiers();
			int castingTick = stack.getMaxItemUseDuration() - count;

			if(MinecraftForge.EVENT_BUS.post(new SpellCastEvent.Tick(Source.SCROLL, spell, player, modifiers, castingTick)))
				return;

			// Continuous spells (these must check if they can be cast each tick since the mana changes)
			if(spell.isContinuous){

				if(spell.cast(player.world, player, player.getActiveHand(), castingTick, modifiers)){

					if(castingTick == 0)
						MinecraftForge.EVENT_BUS.post(new SpellCastEvent.Post(Source.SCROLL, spell, player, modifiers));
				}
			}
		}
	}
	
	@Override
	public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase user, int timeLeft){
		// Consumes a continuous spell scroll when a player in survival mode stops using it.
		if(Spell.get(stack.getItemDamage()).isContinuous
				&& (!(user instanceof EntityPlayer) || !((EntityPlayer)user).capabilities.isCreativeMode)){
			stack.shrink(1);
		}
	}
	
	@Override
	public ItemStack onItemUseFinish(ItemStack stack, World world, EntityLivingBase user){
		// Consumes a continuous spell scroll when the casting elapses whilst in use by a player in survival mode.
		if(Spell.get(stack.getItemDamage()).isContinuous
				&& (!(user instanceof EntityPlayer) || !((EntityPlayer)user).capabilities.isCreativeMode)){
			stack.shrink(1);
		}
		
		return stack;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public FontRenderer getFontRenderer(ItemStack stack){
		return Wizardry.proxy.getFontRenderer(stack);
	}
}
