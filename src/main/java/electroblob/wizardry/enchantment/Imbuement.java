package electroblob.wizardry.enchantment;

import java.util.Iterator;
import java.util.Map;

import com.google.common.collect.Iterables;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.WizardryEnchantments;
import electroblob.wizardry.spell.FreezingWeapon;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

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

	@SubscribeEvent
	public static void onLivingDropsEvent(LivingDropsEvent event){
		// Instantly disenchants an imbued weapon if it is dropped when the player dies.
		for(EntityItem item : event.getDrops()){
			removeImbuements(item.getItem());
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
			// No need to check what enchantments the item has, since remove() does nothing if the element does not
			// exist.
			NBTTagList enchantmentList = stack.getItem() == Items.ENCHANTED_BOOK ?
					ItemEnchantedBook.getEnchantments(stack) : stack.getEnchantmentTagList();
			// Check all enchantments of the item
			Iterator<NBTBase> enchantmentIt = enchantmentList.iterator();
			while(enchantmentIt.hasNext()){
				NBTTagCompound enchantmentTag = (NBTTagCompound) enchantmentIt.next();
				Enchantment enchantment = Enchantment.getEnchantmentByID(enchantmentTag.getShort("id"));
				// If the item contains a magic weapon enchantment, remove it from the item
				if(enchantment instanceof Imbuement){
					enchantmentIt.remove();
				}
			}
		}
	}

	@SubscribeEvent
	public static void onPlayerOpenContainerEvent(PlayerContainerEvent event){
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
						if(enchantmentList.hasNoTags()){
							slot.putStack(ItemStack.EMPTY); // NOTE: Will need changing in 1.11
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

	@SubscribeEvent
	public static void onEntityJoinWorld(EntityJoinWorldEvent event){
		// Rather long-winded (but necessary) way of getting an arrow just after it has been fired, checking if the bow
		// that fired it has the imbuement enchantment, and applying extra damage accordingly.
		if(!event.getEntity().world.isRemote && event.getEntity() instanceof EntityArrow){

			EntityArrow arrow = (EntityArrow)event.getEntity();

			if(arrow.shootingEntity instanceof EntityLivingBase){

				EntityLivingBase archer = (EntityLivingBase)arrow.shootingEntity;

				ItemStack bow = archer.getHeldItemMainhand();

				if(!(bow.getItem() instanceof ItemBow)){
					bow = archer.getHeldItemOffhand();
					if(!(bow.getItem() instanceof ItemBow)) return;
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
