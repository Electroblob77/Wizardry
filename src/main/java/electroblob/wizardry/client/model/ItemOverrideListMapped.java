package electroblob.wizardry.client.model;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/** A version of {@link ItemOverrideList} that uses a predefined map to substitute a specific baked model depending on
 * the location the currently-applicable override points to. */
public class ItemOverrideListMapped extends ItemOverrideList {

	private final Map<ResourceLocation, IBakedModel> replacementMap;

	public ItemOverrideListMapped(List<ItemOverride> overrides, Map<ResourceLocation, IBakedModel> replacementMap){
		super(overrides);
		this.replacementMap = ImmutableMap.copyOf(replacementMap);
	}

	@Override
	public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity){

		if(!stack.isEmpty() && stack.getItem().hasCustomProperties()){

			// Get the location the original override points to...
			ResourceLocation location = applyOverride(stack, world, entity); // I wonder why this is deprecated?

			if(location != null){
				return replacementMap.get(location); // ... then substitute in the corresponding generated model
			}
		}

		return originalModel;
	}
}
