package electroblob.wizardry.potion;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.util.EntityUtils;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class PotionDiamondflesh extends PotionMagicEffect {

	public PotionDiamondflesh(boolean isBadEffect, int liquidColour) {
		super(isBadEffect, liquidColour, new ResourceLocation(Wizardry.MODID, "textures/gui/potion_icons/diamondflesh.png"));
		// This needs to be here because registerPotionAttributeModifier doesn't like it if the potion has no name yet.
		this.setPotionName("potion." + Wizardry.MODID + ":ironflesh");
		this.registerPotionAttributeModifier(SharedMonsterAttributes.MOVEMENT_SPEED,
				"158a8af2-6db0-4340-a01c-a7b60d10ddf4", -0.1f, EntityUtils.Operations.MULTIPLY_CUMULATIVE);
		this.registerPotionAttributeModifier(SharedMonsterAttributes.ARMOR_TOUGHNESS,
				"a68d4532-5847-426c-9b03-d541b113cec2", 3.0f, EntityUtils.Operations.ADD);
		this.registerPotionAttributeModifier(SharedMonsterAttributes.ARMOR,
				"46a095be-82dd-43fd-8b67-13f51591eb8e", 4.0f, EntityUtils.Operations.ADD);
	}

	@Override
	public boolean isReady(int duration, int amplifier) {
		return true;
	}
}
