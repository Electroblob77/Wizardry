package electroblob.wizardry.item;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import electroblob.wizardry.ExtendedPlayer;
import electroblob.wizardry.SpellGlyphData;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryGuiHandler;
import electroblob.wizardry.WizardryRegistry;
import electroblob.wizardry.client.GuiSpellBook;
import electroblob.wizardry.spell.Spell;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

public class ItemSpellBook extends Item{
	
	public ItemSpellBook() {
		super();
		this.setHasSubtypes(true);
		this.setMaxStackSize(1);
		this.setCreativeTab(Wizardry.tabSpells);
		this.setTextureName("wizardry:spell_book");
		this.setUnlocalizedName("spellBook");
	}
	
	@Override
	public void getSubItems(Item item, CreativeTabs tab, List list){
		// In this particular case, getTotalSpellCount() is a more efficient way of doing this since the spell instance
		// is not required, only the id.
	    for(int i = 0; i < Spell.getTotalSpellCount(); i++){
	    	// i+1 is used so that the metadata ties up with the id() method. In other words, the none spell has id
	    	// 0 and since this is not used as a spell book the metadata starts at 1.
	        list.add(new ItemStack(item, 1, i+1));
	    }
	}
	
	public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer){
		par3EntityPlayer.openGui(Wizardry.instance, WizardryGuiHandler.SPELL_BOOK, par2World, 0, 0, 0);
		return par1ItemStack;
	}
	/*
	@Override
	public String getUnlocalizedName(ItemStack stack) {
	    return EnumSpell.getSpellFromId(stack.getItemDamage()).name;
	}
	*/
	public void addInformation(ItemStack itemstack, EntityPlayer player, List text, boolean par4){
		// Tooltip is left blank for wizards buying generic spell books.
		if(itemstack.getItemDamage() != OreDictionary.WILDCARD_VALUE){
			
			Spell spell = Spell.get(itemstack.getItemDamage());
			
			boolean discovered = true;
			if(Wizardry.discoveryMode && !player.capabilities.isCreativeMode && ExtendedPlayer.get(player) != null
					&& !ExtendedPlayer.get(player).hasSpellBeenDiscovered(spell)){
				discovered = false;
			}
			
			// Element colour is not given for undiscovered spells
			text.add(discovered ? "\u00A77" + spell.getDisplayNameWithFormatting() : "#\u00A79" + SpellGlyphData.getGlyphName(spell, player.worldObj));
			text.add(spell.tier.getDisplayNameWithFormatting());
		}
		/* Removed to streamline the tooltip a bit. Information is now within the book.
		if(spell.isContinuous){
			par3List.add("\u00A79Mana Cost: " + spell.cost + " per second");
		}else{
			par3List.add("\u00A79Mana Cost: " + spell.cost);
		}
		*/
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public FontRenderer getFontRenderer(ItemStack stack){
		return Wizardry.proxy.getFontRenderer(stack);
	}

}
