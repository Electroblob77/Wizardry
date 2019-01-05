package electroblob.wizardry.item;

import java.util.List;

import electroblob.wizardry.SpellGlyphData;
import electroblob.wizardry.WizardData;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryGuiHandler;
import electroblob.wizardry.registry.WizardryTabs;
import electroblob.wizardry.spell.Spell;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

public class ItemSpellBook extends Item {

	public ItemSpellBook(){
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
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand){
		ItemStack stack = player.getHeldItem(hand);
		player.openGui(Wizardry.instance, WizardryGuiHandler.SPELL_BOOK, world, 0, 0, 0);
		return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemstack, World world, List<String> tooltip, ITooltipFlag advanced){
		// Tooltip is left blank for wizards buying generic spell books.
		if(itemstack.getItemDamage() != OreDictionary.WILDCARD_VALUE){
			EntityPlayerSP player = Minecraft.getMinecraft().player;

			Spell spell = Spell.get(itemstack.getItemDamage());

			boolean discovered = true;
			if(player != null && Wizardry.settings.discoveryMode && !player.capabilities.isCreativeMode && WizardData.get(player) != null
					&& !WizardData.get(player).hasSpellBeenDiscovered(spell)){
				discovered = false;
			}

			// Element colour is not given for undiscovered spells
			tooltip.add(discovered ? "\u00A77" + spell.getDisplayNameWithFormatting()
					: "#\u00A79" + SpellGlyphData.getGlyphName(spell, player.world));
			tooltip.add(spell.tier.getDisplayNameWithFormatting());
		}
		/* Removed to streamline the tooltip a bit. Information is now within the book. if(spell.isContinuous){
		 * tooltip.add("\u00A79Mana Cost: " + spell.cost + " per second"); }else{ tooltip.add("\u00A79Mana Cost: " +
		 * spell.cost); } */
	}

	@Override
	@SideOnly(Side.CLIENT)
	public FontRenderer getFontRenderer(ItemStack stack){
		return Wizardry.proxy.getFontRenderer(stack);
	}

}
