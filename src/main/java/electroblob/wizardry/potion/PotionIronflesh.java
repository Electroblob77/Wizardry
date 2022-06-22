package electroblob.wizardry.potion;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.util.EntityUtils;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class PotionIronflesh extends PotionMagicEffect {

	public PotionIronflesh(boolean isBadEffect, int liquidColour) {
		super(isBadEffect, liquidColour, new ResourceLocation(Wizardry.MODID, "textures/gui/potion_icons/ironflesh.png"));
		// This needs to be here because registerPotionAttributeModifier doesn't like it if the potion has no name yet.
		this.setPotionName("potion." + Wizardry.MODID + ":ironflesh");
		this.registerPotionAttributeModifier(SharedMonsterAttributes.MOVEMENT_SPEED,
				"fe607d55-50e3-4f4f-a959-6571503f92f4", -0.1f, EntityUtils.Operations.MULTIPLY_CUMULATIVE);
		this.registerPotionAttributeModifier(SharedMonsterAttributes.KNOCKBACK_RESISTANCE,
				"6f78206e-8dd4-4d44-9792-d7a882111951", 0.3f, EntityUtils.Operations.ADD);
		this.registerPotionAttributeModifier(SharedMonsterAttributes.ARMOR,
				"e1adff1e-8510-4a09-96ed-ef677cad20c1", 4.0f, EntityUtils.Operations.ADD);
	}

	@Override
	public boolean isReady(int duration, int amplifier) {
		return true;
	}
}
