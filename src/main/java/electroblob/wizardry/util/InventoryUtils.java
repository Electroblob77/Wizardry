package electroblob.wizardry.util;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.EntityEquipmentSlot.Type;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.NonNullList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Contains useful static methods for interacting with item stacks and entity inventories. These methods used to be part
 * of {@code WizardryUtilities}.
 * @author Electroblob
 * @since Wizardry 4.3
 */
public final class InventoryUtils {

	private InventoryUtils(){} // No instances!

	/** Constant which is simply an array of the four armour slots. (Could've sworn this exists somewhere in vanilla,
	 * but I can't find it anywhere...) */
	public static final EntityEquipmentSlot[] ARMOUR_SLOTS;

	static {
		// The list of slots needs to be mutable.
		List<EntityEquipmentSlot> slots = new ArrayList<>(Arrays.asList(EntityEquipmentSlot.values()));
		slots.removeIf(slot -> slot.getSlotType() != Type.ARMOR);
		ARMOUR_SLOTS = slots.toArray(new EntityEquipmentSlot[0]);
	}

	/**
	 * Returns a list of the itemstacks in the given player's hotbar. Defined here for convenience and to centralise the
	 * (unfortunately unavoidable) use of hardcoded numbers to reference the inventory slots. The returned list is a
	 * modifiable copy of part of the player's inventory stack list; as such, changes to the list are <b>not</b> written
	 * through to the player's inventory. However, the ItemStack instances themselves are not copied, so changes to any
	 * of their fields (size, metadata...) will change those in the player's inventory.
	 *
	 * @since Wizardry 1.2
	 */
	public static List<ItemStack> getHotbar(EntityPlayer player){
		NonNullList<ItemStack> hotbar = NonNullList.create();
		hotbar.addAll(player.inventory.mainInventory.subList(0, 9));
		return hotbar;
	}

	/**
	 * Returns a list of the itemstacks in the given player's hotbar and offhand, sorted into the following order: main
	 * hand, offhand, rest of hotbar left-to-right. The returned list is a modifiable shallow copy of part of the player's
	 * inventory stack list; as such, changes to the list are <b>not</b> written through to the player's inventory.
	 * However, the ItemStack instances themselves are not copied, so changes to any of their fields (size, metadata...)
	 * will change those in the player's inventory.
	 *
	 * @since Wizardry 1.2
	 */
	public static List<ItemStack> getPrioritisedHotbarAndOffhand(EntityPlayer player){
		List<ItemStack> hotbar = getHotbar(player);
		// Adds the offhand item to the beginning of the list so it is processed before the hotbar
		hotbar.add(0, player.getHeldItemOffhand());
		// Moves the item in the main hand to the beginning of the list so it is processed first
		hotbar.remove(player.getHeldItemMainhand());
		hotbar.add(0, player.getHeldItemMainhand());
		return hotbar;
	}

	/** Returns which {@link EnumHandSide} the given {@link EnumHand} is on for the given entity. */
	public static EnumHandSide getSideForHand(EntityLivingBase entity, EnumHand hand){
		return hand == EnumHand.MAIN_HAND ? entity.getPrimaryHand() : entity.getPrimaryHand().opposite();
	}

	/** Returns which {@link EnumHand} is on the given {@link EnumHandSide} for the given entity. */
	public static EnumHand getHandForSide(EntityLivingBase entity, EnumHandSide side){
		return side == entity.getPrimaryHand() ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
	}

	/** Returns the opposite {@link EnumHand} to the one given. */
	public static EnumHand getOpposite(EnumHand hand){
		return hand == EnumHand.OFF_HAND ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
	}

	/**
	 * Tests whether the specified player has any of the specified item in their entire inventory, including armour
	 * slots and offhand.
	 */
	public static boolean doesPlayerHaveItem(EntityPlayer player, Item item){

		for(ItemStack stack : player.inventory.mainInventory){
			if(stack.getItem() == item){
				return true;
			}
		}

		for(ItemStack stack : player.inventory.armorInventory){
			if(stack.getItem() == item){
				return true;
			}
		}

		for(ItemStack stack : player.inventory.offHandInventory){
			if(stack.getItem() == item){
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns a new {@link ItemStack} that is identical to the supplied one, except with the metadata changed to the
	 * new value given.
	 * @param toCopy The stack to copy
	 * @param newMetadata The new metadata value
	 * @return The resulting {@link ItemStack}
	 */
	public static ItemStack copyWithMeta(ItemStack toCopy, int newMetadata){
		ItemStack copy = new ItemStack(toCopy.getItem(), toCopy.getCount(), newMetadata);
		NBTTagCompound compound = toCopy.getTagCompound();
		if(compound != null) copy.setTagCompound(compound.copy());
		return copy;
	}

	/**
	 * Returns whether the two given item stacks can be merged, i.e. if they both contain the same (stackable) item,
	 * metadata and NBT. Importantly, the number of items in each stack need not be the same. No actual merging is
	 * performed by this method; the input stacks will not be modified.
	 * @param stack1 The first stack to be tested for mergeability
	 * @param stack2 The second stack to be tested for mergeability (order does not matter)
	 * @return True if the two stacks can be merged, false if not
	 */
	public static boolean canMerge(ItemStack stack1, ItemStack stack2){
		return !stack1.isEmpty() && !stack2.isEmpty()
				&& stack1.isStackable() && stack2.isStackable()
				&& stack1.getItem() == stack2.getItem()
				&& (!stack1.getHasSubtypes() || stack1.getMetadata() == stack2.getMetadata())
				&& ItemStack.areItemStackTagsEqual(stack1, stack2);
	}

}
