package electroblob.wizardry.enchantment;

import com.google.common.collect.Iterables;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.WizardryEnchantments;
import electroblob.wizardry.spell.FreezingWeapon;
import electroblob.wizardry.spell.ImbueWeapon;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.ContainerMerchant;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import java.lang.reflect.Field;
import java.util.Iterator;

/**
 * Interface for temporary enchantments that last for a certain duration ('imbuements'). This interface allows
 * {@link EnchantmentMagicSword} and {@link EnchantmentTimed} to both be treated as instances of a single type, rather
 * than having to deal with each of them separately, which would be inefficient and cumbersome (the former of those
 * classes cannot extend the latter because they both need to extend different subclasses of
 * {@link net.minecraft.enchantment.Enchantment}).
 *
 * @since Wizardry 1.2
 */
@Mod.EventBusSubscriber
public interface Imbuement {

	// This interface has no abstract methods, these handlers are just here for the sake of keeping things organised.

    static IMerchant getMerchant(ContainerMerchant container) {
        if (container == null) return null;

        try {
            Field merchantField = ContainerMerchant.class.getDeclaredField("field_75178_e");
            merchantField.setAccessible(true);
            return (IMerchant) merchantField.get(container);
        } catch (ReflectiveOperationException e) {
            Wizardry.logger.warn("Failed to access merchant from ContainerMerchant", e);
            return null;
        }
    }

	@SubscribeEvent
	public static void onLivingDropsEvent(LivingDropsEvent event){
		// Instantly disenchants an imbued weapon if it is dropped when the player dies.
		for(EntityItem item : event.getDrops()){
			// Apparently some mods don't behave and shove null items in the list, quite why I have no idea
			if(item != null) removeImbuements(item.getItem());
		}
	}

	@SubscribeEvent
	public static void onItemTossEvent(ItemTossEvent event){
		// Instantly disenchants an imbued weapon if it is thrown on the ground.
		removeImbuements(event.getEntityItem().getItem());
	}

	/** Removes all imbuements from the given itemstack. */
	static void removeImbuements(ItemStack stack){
		if(stack.isItemEnchanted()){
			// No need to check what enchantments the item has, since remove() does nothing if the element does not exist
			NBTTagList enchantmentList = stack.getItem() == Items.ENCHANTED_BOOK ?
					ItemEnchantedBook.getEnchantments(stack) : stack.getEnchantmentTagList();
			// Check all enchantments of the item
			Iterator<NBTBase> enchantmentIt = enchantmentList.iterator();
			while(enchantmentIt.hasNext()){
				NBTTagCompound enchantmentTag = (NBTTagCompound) enchantmentIt.next();
				Enchantment enchantment = Enchantment.getEnchantmentByID(enchantmentTag.getShort("id"));
				// If the item contains a magic weapon enchantment, remove it from the item
				if(enchantment instanceof Imbuement){
					((Imbuement) enchantment).onImbuementRemoval(stack);
					enchantmentIt.remove();
				}
			}
		}
	}

	/** Allows executing some custom logic before this imbuement is being removed from the stack. Called when the imbuement is about to be removed. */
	default void onImbuementRemoval(ItemStack stack){}

	/** NBT key used to store an imbuement's remaining duration directly inside its own enchantment NBT tag on the
	 * itemstack. Storing it here (rather than in a separate per-player map keyed by enchantment) means two items
	 * carrying the same imbuement don't share/overwrite each other's duration, and the duration naturally persists
	 * with the item wherever it goes (dropped, chested, traded) instead of only while a player's data is ticking it. */
	String DURATION_NBT_KEY = "WizardryImbuementDuration";

	/** Returns the NBT tag list on the given stack that holds its enchantments, accounting for enchanted books. */
	static NBTTagList getEnchantmentTagList(ItemStack stack){
		return stack.getItem() == Items.ENCHANTED_BOOK ? ItemEnchantedBook.getEnchantments(stack) : stack.getEnchantmentTagList();
	}

	/** Returns the enchantment NBT tag for the given imbuement on the given stack, or null if the stack does not
	 * currently carry that imbuement. */
	static NBTTagCompound getImbuementTag(ItemStack stack, Enchantment enchantment){
		if(!stack.isItemEnchanted()) return null;
		int id = Enchantment.getEnchantmentID(enchantment);
		NBTTagList list = getEnchantmentTagList(stack);
		for(int i = 0; i < list.tagCount(); i++){
			NBTTagCompound tag = list.getCompoundTagAt(i);
			if(tag.getShort("id") == id) return tag;
		}
		return null;
	}

	/** Returns the remaining duration (in ticks) of the given imbuement on the given stack, or 0 if the stack does
	 * not currently carry it. */
	static int getDuration(ItemStack stack, Enchantment enchantment){
		NBTTagCompound tag = getImbuementTag(stack, enchantment);
		return tag == null ? 0 : tag.getInteger(DURATION_NBT_KEY);
	}

	/**
	 * Sets the remaining duration (in ticks) of the given imbuement on the given stack. The stack must already carry
	 * the enchantment (e.g. having just been given it via {@link ItemStack#addEnchantment}).
	 *
	 * @throws IllegalArgumentException if the given {@link Enchantment} is not an {@link Imbuement}, or if the stack
	 * does not currently carry it.
	 */
	static void setDuration(ItemStack stack, Enchantment enchantment, int duration){
		if(!(enchantment instanceof Imbuement)){
			throw new IllegalArgumentException("Attempted to set an imbuement duration for something that isn't an Imbuement!");
		}
		NBTTagCompound tag = getImbuementTag(stack, enchantment);
		if(tag == null){
			throw new IllegalArgumentException("Attempted to set an imbuement duration for an imbuement the stack does not have!");
		}
		tag.setInteger(DURATION_NBT_KEY, duration);
	}

	/**
	 * Returns whether the given player currently has the given imbuement active on any item in their main inventory,
	 * armour or offhand. Used to prevent casting an imbuement spell again while it is already active elsewhere.
	 */
	static boolean isActive(EntityPlayer player, Enchantment enchantment){
		for(ItemStack stack : player.inventory.mainInventory) if(getDuration(stack, enchantment) > 0) return true;
		for(ItemStack stack : player.inventory.armorInventory) if(getDuration(stack, enchantment) > 0) return true;
		for(ItemStack stack : player.inventory.offHandInventory) if(getDuration(stack, enchantment) > 0) return true;
		return false;
	}

	@SubscribeEvent
	public static void onPlayerOpenContainerEvent(PlayerContainerEvent event){

		// Brute-force fix to stop illegal (imbuement) enchanted books from showing up in villager trades.
		// Villagers pick a completely random enchantment for these books (EntityVillager$ListEnchantedBookForEmeralds),
		// bypassing isAllowedOnBooks/canApply/EnumEnchantmentType checks entirely, unlike enchanting table/anvil use.
		// Note that IMerchant#getRecipes() returns a *copy* of the trade list (see ForgeEventFactory#listTradeOffers),
		// so the villager's actual buyingList field has to be modified directly for the fix to take effect; both
		// fields are only accessible here because of the access transformer (see accesstransformer.cfg).
//		if(event.getContainer() instanceof ContainerMerchant){
//
//			IMerchant merchant = ((ContainerMerchant)event.getContainer()).merchant;
//
//			if(merchant instanceof EntityVillager){
//
//				MerchantRecipeList recipes = ((EntityVillager)merchant).buyingList;

        if(event.getContainer() instanceof ContainerMerchant){

            IMerchant merchant = getMerchant((ContainerMerchant) event.getContainer());

            if(merchant instanceof EntityVillager){

                MerchantRecipeList recipes = ((EntityVillager) merchant).buyingList;

				if(recipes != null && recipes.removeIf(recipe -> hasIllegalEnchantment(recipe.getItemToSell()))){
					Wizardry.logger.info("Removed illegal enchanted book trade(s) from villager");
				}
			}
		}

		// Brute-force fix to stop enchanted books in dungeon chests from having imbuements on them.
		if(event.getContainer() instanceof ContainerChest){
			// Still not sure if it's better to set stacks in slots or modify the itemstack list directly, but I would
			// imagine it's the former.
			for(Slot slot : event.getContainer().inventorySlots){
				ItemStack slotStack = slot.getStack();
				if(slotStack.getItem() instanceof ItemEnchantedBook){
					// We don't care about the level of the enchantments
					NBTTagList enchantmentList = ItemEnchantedBook.getEnchantments(slotStack);
					// Removes all imbuements
					if(Iterables.removeIf(enchantmentList, tag -> {
						NBTTagCompound enchantmentTag = (NBTTagCompound) tag;
						return Enchantment.getEnchantmentByID(enchantmentTag.getShort("id"))
								instanceof Imbuement;
					})){
						// If any imbuements were removed, inform about the removal of the enchantment(s), or
						// delete the book entirely if there are none left.
						if(enchantmentList.isEmpty()){
							slot.putStack(ItemStack.EMPTY);
							Wizardry.logger.info("Deleted enchanted book with illegal enchantments");
						}else{
							// Inform about enchantment removal
							Wizardry.logger.info("Removed illegal enchantments from enchanted book");
						}
					}
				}
			}
		}
	}

	/** Returns true if the given stack is an enchanted book carrying at least one illegal (imbuement) enchantment. */
	static boolean hasIllegalEnchantment(ItemStack stack){
		if(stack.getItem() instanceof ItemEnchantedBook){
			NBTTagList enchantmentList = ItemEnchantedBook.getEnchantments(stack);
			for(int i = 0; i < enchantmentList.tagCount(); i++){
				if(Enchantment.getEnchantmentByID(enchantmentList.getCompoundTagAt(i).getShort("id")) instanceof Imbuement){
					return true;
				}
			}
		}
		return false;
	}

	@SubscribeEvent
	public static void onEntityJoinWorld(EntityJoinWorldEvent event){
		// Rather long-winded (but necessary) way of getting an arrow just after it has been fired, checking if the bow
		// that fired it has the imbuement enchantment, and applying extra damage accordingly.
		if(!event.getEntity().world.isRemote && event.getEntity() instanceof EntityArrow){

			EntityArrow arrow = (EntityArrow)event.getEntity();

			if(arrow.shootingEntity instanceof EntityLivingBase){

				EntityLivingBase archer = (EntityLivingBase)arrow.shootingEntity;

				ItemStack bow = archer.getHeldItemMainhand();

				if(!ImbueWeapon.isBow(bow)){
					bow = archer.getHeldItemOffhand();
					if(!ImbueWeapon.isBow(bow)) return;
				}

				// Taken directly from ItemBow, so it works exactly the same as the power enchantment.
				int level = EnchantmentHelper.getEnchantmentLevel(WizardryEnchantments.magic_bow, bow);

				if(level > 0){
					arrow.setDamage(arrow.getDamage() + (double)level * 0.5D + 0.5D);
				}

				if(EnchantmentHelper.getEnchantmentLevel(WizardryEnchantments.flaming_weapon, bow) > 0){
					// Again, this is exactly what happens in ItemBow (flame is flame; level does nothing).
					arrow.setFire(100);
				}

				level = EnchantmentHelper.getEnchantmentLevel(WizardryEnchantments.freezing_weapon, bow);

				if(level > 0){
					if(arrow.getEntityData() != null){
						arrow.getEntityData().setInteger(FreezingWeapon.FREEZING_ARROW_NBT_KEY, level);
					}
				}
			}
		}
	}

}
