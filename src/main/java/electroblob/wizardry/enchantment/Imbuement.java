package electroblob.wizardry.enchantment;

import java.util.Map;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.WizardryEnchantments;
import electroblob.wizardry.spell.FreezingWeapon;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Interface for temporary enchantments that last for a certain duration ('imbuements'). This interface allows
 * {@link EnchantmentMagicSword} and {@link EnchantmentTimed} to both be treated as instances of a
 * single type, rather than having to deal with each of them separately,
 * which would be inefficient and cumbersome (the former of those classes cannot extend the latter because they both
 * need to extend different subclasses of {@link net.minecraft.enchantment.Enchantment}).
 * @since Wizardry 1.2 */
@Mod.EventBusSubscriber
public interface Imbuement {

	// This interface has no abstract methods, these handlers are just here for the sake of keeping things organised.

	@SubscribeEvent
	public static void onLivingDropsEvent(LivingDropsEvent event){
		// Instantly disenchants an imbued weapon if it is dropped when the player dies.
		for(EntityItem item : event.getDrops()){
			removeImbuements(item.getEntityItem());
		}
	}

	@SubscribeEvent
	public static void onItemTossEvent(ItemTossEvent event){
		// Instantly disenchants an imbued weapon if it is thrown on the ground.
		removeImbuements(event.getEntityItem().getEntityItem());
	}
	
	/** Removes all imbuements from the given itemstack. */
	static void removeImbuements(ItemStack stack){
		if(stack.isItemEnchanted()){
			// No need to check what enchantments the item has, since remove() does nothing if the element does not exist.
			Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
			// Removes the magic weapon enchantments from the enchantment map
			enchantments.entrySet().removeIf(entry -> entry.getKey() instanceof Imbuement);
			// Applies the new enchantment map to the item
			EnchantmentHelper.setEnchantments(enchantments, stack);
		}
	}
	
	@SubscribeEvent
	public static void onPlayerOpenContainerEvent(PlayerContainerEvent event){
		// Brute-force fix to stop enchanted books in dungeon chests from having imbuements on them.
		if(event.getContainer() instanceof ContainerChest){
			// Still not sure if it's better to set stacks in slots or modify the itemstack list directly, but I would
			// imagine it's the former.
			for(Slot slot : event.getContainer().inventorySlots){
				if(slot.getStack() != null && slot.getStack().getItem() instanceof ItemEnchantedBook){
					// We don't care about the level of the enchantments
					Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(slot.getStack());
					// Removes all imbuements
					if(enchantments.keySet().removeIf(e -> e instanceof Imbuement)){
						// If any imbuements were removed, replaces the enchantments on the book with the new ones, or
						// deletes the book entirely if there are none left.
						if(enchantments.isEmpty()){
							slot.putStack(null); // NOTE: Will need changing in 1.11
							Wizardry.logger.info("Deleted enchanted book with illegal enchantments");
						}else{
							EnchantmentHelper.setEnchantments(enchantments, slot.getStack());
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
		if(!event.getEntity().worldObj.isRemote && event.getEntity() instanceof EntityArrow){

			EntityArrow arrow = (EntityArrow)event.getEntity();

			if(arrow.shootingEntity instanceof EntityLivingBase){

				EntityLivingBase archer = (EntityLivingBase)arrow.shootingEntity;

				ItemStack bow = archer.getHeldItemMainhand();

				if(bow == null || !(bow.getItem() instanceof ItemBow)){
					bow = archer.getHeldItemOffhand();
					if(bow == null || !(bow.getItem() instanceof ItemBow)) return;
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
