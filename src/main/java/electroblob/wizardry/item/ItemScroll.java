package electroblob.wizardry.item;

import java.util.List;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import electroblob.wizardry.ExtendedPlayer;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryRegistry;
import electroblob.wizardry.packet.PacketCastSpell;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.spell.Spell;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class ItemScroll extends Item {

	public ItemScroll(){
		super();
		this.setHasSubtypes(true);
		this.setMaxStackSize(1);
		this.setCreativeTab(Wizardry.tabSpells);
		this.setTextureName("wizardry:scroll");
		this.setUnlocalizedName("scroll");
	}

	@Override
	public void getSubItems(Item item, CreativeTabs par2CreativeTabs, List list){
		// Isn't this sooooo much neater with the filter thing?
		for(Spell spell : Spell.getSpells(Spell.nonContinuousSpells)){
			list.add(new ItemStack(item, 1, spell.id()));
		}
	}

	@Override
	public boolean hasEffect(ItemStack stack, int par2){
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
		return Wizardry.proxy.getScrollDisplayName(stack);

	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player){

		if(player.isPotionActive(Wizardry.arcaneJammer)) return stack;
		
		Spell spell = Spell.get(stack.getItemDamage());

		// If a spell is disabled in the config, it will not work.
		if(!spell.isEnabled()){
			if(!world.isRemote) player.addChatMessage(new ChatComponentTranslation("spell.disabled", spell.getDisplayNameWithFormatting()));
			return stack;
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
					
				if(spell.cast(world, player, 0, 1, 1, 1, 1)){
					
					if(spell.doesSpellRequirePacket()){
						// Sends a packet to all players in dimension to tell them to spawn particles.
						IMessage msg = new PacketCastSpell.Message(player.getEntityId(), 0, spell.id(), 1, 1, 1);
				    	WizardryPacketHandler.net.sendToDimension(msg, world.provider.dimensionId);
					}
					
					if(!player.capabilities.isCreativeMode && !ExtendedPlayer.get(player).hasSpellBeenDiscovered(spell) && Wizardry.discoveryMode){
						player.worldObj.playSoundAtEntity(player, "random.levelup", 1.25f, 1);
						if(!player.worldObj.isRemote) player.addChatMessage(new ChatComponentTranslation("spell.discover", spell.getDisplayNameWithFormatting()));
					}
					ExtendedPlayer.get(player).discoverSpell(spell);
					
					if(!player.capabilities.isCreativeMode) stack.stackSize--;
				}
				
			}else{
				if(spell.cast(world, player, 0, 1, 1, 1, 1)){
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

	@Override
	@SideOnly(Side.CLIENT)
	public FontRenderer getFontRenderer(ItemStack stack){
		return Wizardry.proxy.getFontRenderer(stack);
	}
}
