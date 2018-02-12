package electroblob.wizardry.item;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Allows wizardry to identify items that are conjured (and therefore need destroying if they leave the inventory)
 * without explicitly referencing each, thereby allowing for better expandibility.
 */
@Mod.EventBusSubscriber
public interface IConjuredItem {

	/** The NBT tag key used to store the duration multiplier for conjured items. */
	public static final String DURATION_MULTIPLIER_KEY = "durationMultiplier";

	/** Helper method for setting the duration multiplier (via NBT) for conjured items. */
	public static void setDurationMultiplier(ItemStack stack, float multiplier){
		if(!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
		stack.getTagCompound().setFloat(DURATION_MULTIPLIER_KEY, multiplier);
	}

	/**
	 * Helper method for returning the max damage of a conjured item based on its NBT data. Centralises the code.
	 * Implementors will almost certainly want to call this from {@link Item#getMaxDamage(ItemStack stack)}.
	 */
	public default int getMaxDamageFromNBT(ItemStack stack){
		if(stack.hasTagCompound() && stack.getTagCompound().hasKey(DURATION_MULTIPLIER_KEY)){
			return (int)(this.getBaseDuration() * stack.getTagCompound().getFloat(DURATION_MULTIPLIER_KEY));
		}
		return this.getBaseDuration();
	}

	/**
	 * Returns the base duration in ticks for this conjured item. Should be a constant (commonly 600). Implementors may
	 * want to call this when setting an item's max damage in its constructor.
	 */
	public int getBaseDuration();

	@SubscribeEvent
	public static void onLivingDropsEvent(LivingDropsEvent event){
		// Destroys conjured items if their caster dies.
		for(EntityItem item : event.getDrops()){
			if(item.getItem().getItem() instanceof IConjuredItem){
				item.setDead();
			}
		}
	}

	@SubscribeEvent
	public static void onItemTossEvent(ItemTossEvent event){
		// Prevents conjured items being thrown by dragging and dropping outside the inventory.
		if(event.getEntityItem().getItem().getItem() instanceof IConjuredItem){
			event.setCanceled(true);
			event.getPlayer().inventory.addItemStackToInventory(event.getEntityItem().getItem());
		}
	}
}
