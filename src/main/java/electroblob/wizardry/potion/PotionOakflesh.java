package electroblob.wizardry.potion;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.util.EntityUtils;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class PotionOakflesh extends PotionMagicEffect {

	public PotionOakflesh(boolean isBadEffect, int liquidColour) {
		super(isBadEffect, liquidColour, new ResourceLocation(Wizardry.MODID, "textures/gui/potion_icons/oakflesh.png"));
		// This needs to be here because registerPotionAttributeModifier doesn't like it if the potion has no name yet.
		this.setPotionName("potion." + Wizardry.MODID + ":ironflesh");
		this.registerPotionAttributeModifier(SharedMonsterAttributes.MOVEMENT_SPEED,
				"98b4ba66-7c50-4a4c-9f3f-40bcb37313b5", -0.1f, EntityUtils.Operations.MULTIPLY_CUMULATIVE);
		this.registerPotionAttributeModifier(SharedMonsterAttributes.MAX_HEALTH,
				"ed9d0423-60f4-4998-bd8d-dc7c33bd45b8", 0.2f, EntityUtils.Operations.MULTIPLY_FLAT);
		this.registerPotionAttributeModifier(SharedMonsterAttributes.ARMOR,
				"0b607c3f-fb14-43d7-96b5-1c1b6f6da242", 3.0f, EntityUtils.Operations.ADD);
	}

	@Override
	public boolean isReady(int duration, int amplifier) {
		return true;
	}
}
