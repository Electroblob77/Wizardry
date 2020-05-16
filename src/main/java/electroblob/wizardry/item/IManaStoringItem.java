package electroblob.wizardry.item;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

/**
 * Interface for any items that store mana. This interface simply specifies methods for setting and getting the amount
 * of mana held in the item (plus a few convenience methods); implementations may differ between items.
 * <p></p>
 * In wizardry itself, mana is still implemented as durability, however, as of wizardry 4.2, the vanilla method
 * {@code Item.setDamage()} has been overridden to do nothing. Instead, mana must be interacted with using the methods
 * in this interface. This means operating via the item rather than the stack.
 * <p></p>
 * This change prevents general item repair methods working on mana items (see issues #66 and #153), and also allows
 * other items to implement mana differently if they wish. For example, a weapon that can cast spells as an ability
 * might want regular durability in addition to mana, so the mana might be stored in NBT instead. Items that do not use
 * durability to represent mana may need to do zero-checking themselves, as appropriate.
 * <p></p>
 * <i>Wizardry's items implement mana as the <b>inverse</b> of item damage; i.e. the <b>more damaged</b> the item, the
 * <b>less mana</b> it has. Beware of this when converting to the new system.</i>
 * <p></p>
 * @author Electroblob
 * @since Wizardry 4.2
 */
public interface IManaStoringItem {

	/** Returns the amount of mana contained in the given item stack. */
	int getMana(ItemStack stack);

	/** Sets the amount of mana contained in the given item stack to the given value. This method does not perform any
	 * checks for creative mode, etc. */
	void setMana(ItemStack stack, int mana);

	/** Returns the maximum amount of mana that the given item stack can hold. */
	int getManaCapacity(ItemStack stack);

	/**
	 * Returns whether this item's mana should be displayed in the arcane workbench tooltip. Only called client-side.
	 * Ignore this method if this item is not an {@link IWorkbenchItem}.
	 * @param player The player using the workbench.
	 * @param stack The itemstack to query.
	 * @return True if the mana should be shown, false if not. Returns true by default.
	 */
	default boolean showManaInWorkbench(EntityPlayer player, ItemStack stack){
		return true;
	}

	/** Convenience method that decreases the amount of mana contained in the given item stack by the given value. This
	 * method automatically limits the mana to a minimum of 0 and performs the relevant checks for creative mode, etc. */
	default void consumeMana(ItemStack stack, int mana, @Nullable EntityLivingBase wielder){
		if(wielder instanceof EntityPlayer && ((EntityPlayer)wielder).isCreative()) return; // Mana isn't consumed in creative
		setMana(stack, Math.max(getMana(stack) - mana, 0));
	}

	/** Convenience method that increases the amount of mana contained in the given item stack by the given value.
	 * This method automatically limits the mana to within the item's capacity. */
	// We don't really need to limit this one because Item#setDamage() ultimately limits it anyway, but we may as well
	default void rechargeMana(ItemStack stack, int mana){
		setMana(stack, Math.min(getMana(stack) + mana, getManaCapacity(stack)));
	}

	/** Convenience method that returns true if the given stack contains the maximum amount of mana, false otherwise. */
	default boolean isManaFull(ItemStack stack){
		return getMana(stack) == getManaCapacity(stack);
	}

	/** Convenience method that returns true if the given stack contains no mana, false otherwise. */
	default boolean isManaEmpty(ItemStack stack){
		return getMana(stack) == 0;
	}
}
