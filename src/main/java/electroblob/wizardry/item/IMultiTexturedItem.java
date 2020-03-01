package electroblob.wizardry.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/**
 * Interface for items that change their texture depending on their metadata. This is mainly to facilitate use of the
 * convenience method {@link electroblob.wizardry.client.model.WizardryModels#registerMultiTexturedModel(Item) WizardryModels.registerMultiTexturedModel(T)}. Also works well for {@code ItemBlock}s!
 * @author Electroblob
 * @since Wizardry 4.2
 * @see ItemBlockMultiTextured
 */
public interface IMultiTexturedItem {
	
	/**
	 * Returns the appropriate {@code ResourceLocation} for this item's model, based on the given itemstack.
	 * @param stack The itemstack to return the model name for.
	 * @return A {@code ResourceLocation} pointing to the appropriate model file. As with any other model, this should
	 * include the domain (mod ID) and filename, without the rest of the filepath.
	 */
	ResourceLocation getModelName(ItemStack stack);
}
