package electroblob.wizardry.item;

import electroblob.wizardry.event.SpellBindEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

/**
 * Items that implement this interface may be placed in the central slot of the arcane workbench as long as
 * {@link IWorkbenchItem#canPlace(ItemStack)} returns true. The number of spell book slots displayed is also specified
 * using {@link IWorkbenchItem#getSpellSlotCount(ItemStack)}.
 * <p></p>
 * Items that implement this interface define what happens if they are in the central slot of the arcane workbench and
 * the apply button is pressed, in {@link IWorkbenchItem#onApplyButtonPressed(EntityPlayer, Slot, Slot, Slot, Slot[])}.
 * This is a core part of the arcane workbench refactoring in version 4.2 and allows for custom spell casting items and
 * chargeable armour without requiring that they extend {@link ItemWand} or {@link ItemWizardArmour}.
 * @author Electroblob
 * @since Wizardry 4.2
 */
public interface IWorkbenchItem {
	
	/** 
	 * Returns true if the item can be placed in the central slot of an arcane workbench, false otherwise. Allows
	 * for itemstack-sensitive behaviour. Returns true by default.
	 * @param stack The stack that is being placed into the workbench.
	 * @return True to allow the item to be placed into the workbench, false to prevent that from happening.
	 */
	default boolean canPlace(ItemStack stack){
		return true;
	}
	
	/**
	 * Returns the number of spell book slots that should appear in the workbench when this item is placed into it,
	 * based on the given itemstack.
	 * @param stack The stack that is being placed into the workbench.
	 * @return The number of spell book slots that should appear around this item when it is placed into the workbench.
	 * Can be 0, but must not be negative.
	 */
	int getSpellSlotCount(ItemStack stack);

	/**
	 * Called when this item is in the central slot of an arcane workbench and the apply button is pressed. Items must
	 * implement this method to define what happens when the apply button is pressed. Note that {@link SpellBindEvent}
	 * is fired <i>before</i> this method is called.
	 * @param player The player that pressed the apply button.
	 * @param centre The central slot in the arcane workbench. This slot will always contain a stack of the implementing
	 * item, or in other words, <i>it is guaranteed that</i> {@code this == centre.getStack().getItem()}.
	 * @param crystals The magic crystal slot of the arcane workbench.
	 * @param upgrade The upgrade slot of the arcane workbench.
	 * @param spellBooks An array of the <i>active</i> (visible) spell book slots in the arcane workbench. The length of
	 * the array will be equal to the value returned by {@link IWorkbenchItem#getSpellSlotCount(ItemStack)}.
	 * @return True if anything changed, false if not.
	 */
	boolean onApplyButtonPressed(EntityPlayer player, Slot centre, Slot crystals, Slot upgrade, Slot[] spellBooks);

	/**
	 * Called when this item is in the central slot of an arcane workbench and the apply clear is pressed. Items must
	 * implement this method to define what happens when the apply button is pressed.
	 * @param player The player that pressed the apply button.
	 * @param centre The central slot in the arcane workbench. This slot will always contain a stack of the implementing
	 * item, or in other words, <i>it is guaranteed that</i> {@code this == centre.getStack().getItem()}.
	 * @param crystals The magic crystal slot of the arcane workbench.
	 * @param upgrade The upgrade slot of the arcane workbench.
	 * @param spellBooks An array of the <i>active</i> (visible) spell book slots in the arcane workbench. The length of
	 * the array will be equal to the value returned by {@link IWorkbenchItem#getSpellSlotCount(ItemStack)}.
	 * */
	default void onClearButtonPressed(EntityPlayer player, Slot centre, Slot crystals, Slot upgrade, Slot[] spellBooks){};

	/**
	 * Must be overridden in the item class to make the clear button in the Arcane Workbench clickable.
	 * */
	default boolean isClearable() {
		return false;
	}

	/**
	 * Returns whether the tooltip (dark grey box) should be drawn when this item is in an arcane workbench. Only
	 * called client-side.
	 * @param stack The itemstack to query.
	 * @return True if the workbench tooltip should be shown, false if not.
	 */
	boolean showTooltip(ItemStack stack);

	/**
	 * Applies the given upgrade to this wand. This method is responsible for all checks including tier, progression,
	 * upgrade stack limits, etc. Subclasses are responsible for calling this (usually from
	 * {@link IWorkbenchItem#onApplyButtonPressed(EntityPlayer, Slot, Slot, Slot, Slot[])}), but it has been extracted
	 * as an interface method here for use by JEI 'recipes'.
	 * @param player The player doing the upgrading, or null during JEI recipe lookup (mainly used for advancements)
	 * @param stack The stack being upgraded (it is guaranteed that {@code this == stack.getItem()})
	 * @param upgrade The upgrade item stack being applied. <b>This method is responsible for consuming it!</b>
	 * @return The resulting upgraded wand stack. In many cases, this is simply the input {@code stack}, which has
	 * had its NBT modified. If the given upgrade cannot be applied, simply return the input {@code stack}.
	 */
	default ItemStack applyUpgrade(@Nullable EntityPlayer player, ItemStack stack, ItemStack upgrade){
		return stack;
	}
	
}
