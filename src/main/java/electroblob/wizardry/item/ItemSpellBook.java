package electroblob.wizardry.item;

import com.google.common.collect.ImmutableMap;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryGuiHandler;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.data.SpellGlyphData;
import electroblob.wizardry.registry.WizardryTabs;
import electroblob.wizardry.spell.Spell;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import java.util.List;
import java.util.Map;

public class ItemSpellBook extends Item {

	private static final Map<Tier, ResourceLocation> guiTextures = ImmutableMap.of(
			Tier.NOVICE, 		new ResourceLocation(Wizardry.MODID, "textures/gui/spell_book_novice.png"),
			Tier.APPRENTICE, 	new ResourceLocation(Wizardry.MODID, "textures/gui/spell_book_apprentice.png"),
			Tier.ADVANCED, 		new ResourceLocation(Wizardry.MODID, "textures/gui/spell_book_advanced.png"),
			Tier.MASTER, 		new ResourceLocation(Wizardry.MODID, "textures/gui/spell_book_master.png"));

	public ItemSpellBook(){
		super();
		setHasSubtypes(true);
		setMaxStackSize(16);
		setCreativeTab(WizardryTabs.SPELLS);
		this.addPropertyOverride(new ResourceLocation("festive"), (s, w, e) -> Wizardry.tisTheSeason ? 1 : 0);
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list){

		if(tab == WizardryTabs.SPELLS){

			List<Spell> spells = Spell.getAllSpells();
			spells.removeIf(s -> !s.applicableForItem(this));

			for(Spell spell : spells){
				list.add(new ItemStack(this, 1, spell.metadata()));
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
	public void addInformation(ItemStack itemstack, World world, List<String> tooltip, net.minecraft.client.util.ITooltipFlag advanced){
		// Tooltip is left blank for wizards buying generic spell books.
		if(world != null && itemstack.getItemDamage() != OreDictionary.WILDCARD_VALUE){

			Spell spell = Spell.byMetadata(itemstack.getItemDamage());

			boolean discovered = Wizardry.proxy.shouldDisplayDiscovered(spell, itemstack);

			// Element colour is not given for undiscovered spells
			tooltip.add(discovered ? "\u00A77" + spell.getDisplayNameWithFormatting()
					: "#\u00A79" + SpellGlyphData.getGlyphName(spell, world));

			tooltip.add(spell.getTier().getDisplayNameWithFormatting());

			// Advanced tooltips display more information, mainly for searching purposes in creative
			if(discovered && advanced.isAdvanced()){ // No cheating!
				tooltip.add(spell.getElement().getDisplayName());
				tooltip.add(spell.getType().getDisplayName());
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public net.minecraft.client.gui.FontRenderer getFontRenderer(ItemStack stack){
		return Wizardry.proxy.getFontRenderer(stack);
	}

	/**
	 * Returns the GUI texture to be used when this spell book is opened.
	 * @param spell The spell for the book being opened.
	 */
	public ResourceLocation getGuiTexture(Spell spell){
		return guiTextures.get(spell.getTier());
	}

}
