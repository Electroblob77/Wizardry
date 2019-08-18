package electroblob.wizardry.potion;

import net.minecraft.util.ResourceLocation;

/**
 * Same as {@link PotionMagicEffect}, but also implements {@link ICustomPotionParticles} to allow anonymous classes to
 * extend it and add their own particles. It is advised that all other (named) classes extend and implement the
 * underlying class and interface rather than extending this class.
 */
public abstract class PotionMagicEffectParticles extends PotionMagicEffect implements ICustomPotionParticles {

	public PotionMagicEffectParticles(boolean isBadEffect, int liquidColour, ResourceLocation texture){
		super(isBadEffect, liquidColour, texture);
	}

}
