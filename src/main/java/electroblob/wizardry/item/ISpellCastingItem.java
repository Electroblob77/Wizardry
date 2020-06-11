package electroblob.wizardry.item;

import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

/**
 * Interface for items that can hold and cast one or more spells. These may be consumables, like scrolls, or they may be
 * durability-based, like wands. Custom spell casting items should implement this interface to integrate properly into
 * wizardry. <i>It is no longer necessary to extend {@code ItemWand}, but you may still do so instead of implementing
 * this interface if appropriate.</i>
 * <p></p>
 * This interface is used for the following:<br>
 *     - General-purpose detection of continuous spell casting (see {@link electroblob.wizardry.util.WizardryUtilities#isCasting(EntityLivingBase, Spell)})<br>
 *     - Display of the arcane workbench tooltip (in conjunction with {@link IManaStoringItem})<br>
 *     - Spell HUD visibility<br>
 *     - Spell switching controls (they won't do anything unless the player is holding an {@code ISpellCastingItem})<br>
 *     - Artefacts that trigger a player's wands/scrolls to cast spells
 * @author Electroblob
 * @since Wizardry 4.2
 */
// This could probably be turned into a capability at some point, but for the moment it's fine like this
// As we've already noted, capabilities are only useful for optional dependencies anyway
public interface ISpellCastingItem {

	/**
	 * Returns the spell currently equipped on the given itemstack. The given itemstack will be of this item.
	 * @param stack The itemstack to query.
	 * @return The currently equipped spell, or {@link electroblob.wizardry.registry.Spells#none Spells.none} if no spell
	 * is equipped.
	 */
	@Nonnull
	Spell getCurrentSpell(ItemStack stack);

	/**
	 * Returns all the spells currently bound to the given itemstack. The given itemstack will be of this item.
	 * @param stack The itemstack to query.
	 * @return The bound spells, or {@link electroblob.wizardry.registry.Spells#none Spells.none} if no spell
	 * is equipped.
	 */
	default Spell[] getSpells(ItemStack stack){
		return new Spell[]{getCurrentSpell(stack)}; // Default implementation for single-spell items, because I'm lazy
	}

	/**
	 * Selects the next spell bound to the given itemstack. The given itemstack will be of this item.
	 * @param stack The itemstack to query.
	 */
	default void selectNextSpell(ItemStack stack){
		// If it doesn't need spell-switching then don't bother the implementor with it
	}

	/**
	 * Selects the previous spell bound to the given itemstack. The given itemstack will be of this item.
	 * @param stack The itemstack to query.
	 */
	default void selectPreviousSpell(ItemStack stack){
		// Nothing here either
	}

	/**
	 * Selects the spell at the given index bound to the given itemstack. The given itemstack will be of this item.
	 * @param stack The itemstack to query.
	 * @param index The index to set.
	 * @return True if the operation succeeded, false if not.
	 */
	default boolean selectSpell(ItemStack stack, int index){
		return false;
	}

	/**
	 * Returns whether the spell HUD should be shown when a player is holding this item. Only called client-side.
	 * @param player The player holding the item.
	 * @param stack The itemstack to query.
	 * @return True if the spell HUD should be shown, false if not.
	 */
	boolean showSpellHUD(EntityPlayer player, ItemStack stack);

	/**
	 * Returns whether this item's spells should be displayed in the arcane workbench tooltip. Only called client-side.
	 * Ignore this method if this item is not an {@link IWorkbenchItem}.
	 * @param player The player using the workbench.
	 * @param stack The itemstack to query.
	 * @return True if the spells should be shown, false if not. Returns true by default.
	 */
	default boolean showSpellsInWorkbench(EntityPlayer player, ItemStack stack){
		return true;
	}

	// These methods were made with intention of standardising the code for casting spells using items.
	// For most external uses there's no reason for them to be separate, however, it makes more sense to do so because
	// then we can eliminate a bit of duplicate code from continuous vs. non-continuous spell casting. Otherwise, we'd
	// need a separate method for casting continuous spells anyway.

	/**
	 * Returns whether the given spell can be cast by the given stack in its current state. Does not perform any actual
	 * spellcasting.
	 *
	 * @param stack The stack being queried; will be of this item.
	 * @param spell The spell to be cast.
	 * @param caster The player doing the casting.
	 * @param hand The hand in which the casting item is being held.
	 * @param castingTick For continuous spells, the number of ticks the spell has already been cast for. For all other
	 *                    spells, this will be zero.
	 * @param modifiers The modifiers with which the spell is being cast.
	 * @return True if the spell can be cast, false if not.
	 */
	boolean canCast(ItemStack stack, Spell spell, EntityPlayer caster, EnumHand hand, int castingTick, SpellModifiers modifiers);

	/**
	 * Casts the given spell using the given item stack. <b>This method does not perform any checks</b>; these are done
	 * in {@link ISpellCastingItem#canCast(ItemStack, Spell, EntityPlayer, EnumHand, int, SpellModifiers)}. This method
	 * also performs any post-casting logic, such as mana costs and cooldowns. This method does not handle charge-up
	 * times.
	 * <p></p>
	 * <i>N.B. Continuous spell casting from outside of the items requires a bit of extra legwork, see
	 * {@link WizardData} for an example.</i>
	 *
	 * @param stack The stack being queried; will be of this item.
	 * @param spell The spell to be cast.
	 * @param caster The player doing the casting.
	 * @param hand The hand in which the casting item is being held.
	 * @param castingTick For continuous spells, the number of ticks the spell has already been cast for. For all other
	 *                    spells, this will be zero.
	 * @param modifiers The modifiers with which the spell is being cast.
	 * @return True if the spell succeeded, false if not. This is only really for the purpose of returning a result from
	 * {@link net.minecraft.item.Item#onItemRightClick(World, EntityPlayer, EnumHand)} and similar methods; mana costs,
	 * cooldowns and whatever else you might want to do post-spellcasting should be done within this method so that
	 * external sources don't allow spells to be cast for free, for example.
	 */
	boolean cast(ItemStack stack, Spell spell, EntityPlayer caster, EnumHand hand, int castingTick, SpellModifiers modifiers);
	
}
