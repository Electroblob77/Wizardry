package electroblob.wizardry.item;

import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.spell.SpellConjuration;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Allows wizardry to identify items that are conjured (and therefore need destroying if they leave the inventory)
 * without explicitly referencing each, thereby allowing for better expandability.
 */
@Mod.EventBusSubscriber
public interface IConjuredItem {

	/** The NBT tag key used to store the duration multiplier for conjured items. */
	String DURATION_MULTIPLIER_KEY = "durationMultiplier";
	/** The NBT tag key used to store the damage multiplier for conjured items. */
	String DAMAGE_MULTIPLIER = "damageMultiplier";

	UUID POTENCY_MODIFIER = UUID.fromString("da067ea6-0b35-4140-8436-5476224de9dd");

	/** Helper method for setting the duration multiplier (via NBT) for conjured items. */
	static void setDurationMultiplier(ItemStack stack, float multiplier){
		if(!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
		stack.getTagCompound().setFloat(DURATION_MULTIPLIER_KEY, multiplier);
	}

	/** Helper method for setting the damage multiplier (via NBT) for conjured items. */
	static void setDamageMultiplier(ItemStack stack, float multiplier){
		if(!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
		stack.getTagCompound().setFloat(DAMAGE_MULTIPLIER, multiplier);
	}

	/** Helper method for getting the damage multiplier (via NBT) for conjured items. */
	static float getDamageMultiplier(ItemStack stack){
		if(!stack.hasTagCompound()) return 1;
		return stack.getTagCompound().getFloat(DAMAGE_MULTIPLIER);
	}

	/**
	 * Helper method for returning the max damage of a conjured item based on its NBT data. Centralises the code.
	 * Implementors will almost certainly want to call this from {@link Item#getMaxDamage(ItemStack stack)}.
	 */
	default int getMaxDamageFromNBT(ItemStack stack, Spell spell){

		if(!spell.arePropertiesInitialised()) return 600; // Failsafe, some edge-cases call this during preInit

		float baseDuration = spell.getProperty(SpellConjuration.ITEM_LIFETIME).floatValue();

		if(stack.hasTagCompound() && stack.getTagCompound().hasKey(DURATION_MULTIPLIER_KEY)){
			return (int)(baseDuration * stack.getTagCompound().getFloat(DURATION_MULTIPLIER_KEY));
		}

		return (int)baseDuration;
	}

	/**
	 * Adds property overrides to define the conjuring/vanishing animation. Call this from the item's constructor.
	 */
	default void addAnimationPropertyOverrides(){

		if(!(this instanceof Item)) throw new ClassCastException("Cannot set up conjuring animations for a non-item!");

		Item item = (Item)this;

		final int frames = getAnimationFrames();

		item.addPropertyOverride(new ResourceLocation("conjure"), new IItemPropertyGetter(){
			@SideOnly(Side.CLIENT)
			public float apply(ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity){
				return stack.getItemDamage() < frames ? (float)stack.getItemDamage() / frames
						: (float)(stack.getMaxDamage() - stack.getItemDamage()) / frames;
			}
		});
		item.addPropertyOverride(new ResourceLocation("conjuring"), new IItemPropertyGetter(){
			@SideOnly(Side.CLIENT)
			public float apply(ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity){
				return stack.getItemDamage() < frames
						|| stack.getItemDamage() > stack.getMaxDamage() - frames ? 1.0F : 0.0F;
			}
		});
	}

	/** Returns the number of frames in the conjuring/vanishing animation. Override to change the number of frames
	 * set by {@link IConjuredItem#addAnimationPropertyOverrides()}. */
	default int getAnimationFrames(){
		return 8;
	}

	@SubscribeEvent
	static void onLivingDropsEvent(LivingDropsEvent event){
		// Destroys conjured items if their caster dies.
		for(EntityItem item : event.getDrops()){
			// Apparently some mods don't behave and shove null items in the list, quite why I have no idea
			if(item != null && item.getItem() != null && item.getItem().getItem() instanceof IConjuredItem){
				item.setDead();
			}
		}
	}

	@SubscribeEvent
	static void onItemTossEvent(ItemTossEvent event){
		// Prevents conjured items being thrown by dragging and dropping outside the inventory.
		if(event.getEntityItem().getItem().getItem() instanceof IConjuredItem){
			event.setCanceled(true);
			event.getPlayer().inventory.addItemStackToInventory(event.getEntityItem().getItem());
		}
	}
}
