package electroblob.wizardry.potion;

/** Same as {@link PotionMagicEffect}, but also implements {@link ICustomPotionParticles} to allow anonymous classes
 * to extend it and add their own particles. */
public abstract class PotionMagicEffectParticles extends PotionMagicEffect implements ICustomPotionParticles {

	public PotionMagicEffectParticles(boolean isBadEffect, int liquidColour, int textureIndex){
		super(isBadEffect, liquidColour, textureIndex);
	}

}
