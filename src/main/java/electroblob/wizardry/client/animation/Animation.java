package electroblob.wizardry.client.animation;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHandSide;

/**
 * Represents a player animation. Use one of the predefined subclasses below for common animation types, or extend this
 * class directly to define a completely custom animation. Register instances of this class using
 * {@link PlayerAnimator#registerAnimation(Animation)}.
 * @author Electroblob
 * @since Wizardry 4.3
 * @see ActionAnimation
 */
public abstract class Animation {

	private final String name;

	public Animation(String name){
		this.name = name;
	}

	/** Returns the name of this animation, currently only used for warning messages / debugging. */
	public String getName(){
		return name;
	}

	/**
	 * Returns whether this animation should be displayed for the given player. <i>Note that if two registered
	 * animations have overlapping conditions, both will attempt to display simultaneously, so if they both animate the
	 * same model part, that part may not behave as expected.</i>
	 * @param player The player being animated
	 * @param firstPerson True if the player being animated is the local client player, and they are in first-person
	 *                    view. In first-person, animations are only useful for animating the player's empty hand.
	 * @return True if the animation should be displayed, false if not.
	 */
	public abstract boolean shouldDisplay(EntityPlayer player, boolean firstPerson);

	/**
	 * Sets the rotation of the model parts for this animation. This method is called every time the player is rendered
	 * when {@link Animation#shouldDisplay(EntityPlayer, boolean)} returns true.
	 * @param player The player being animated
	 * @param model The model to animate. All of the standard {@link ModelBiped} parts will already be wrapped and may
	 *              be safely cast to {@code ModelRendererExtended} in order to override the rotations set by the model
	 *              itself - see {@link ModelRendererExtended ModelRendererExtended} for details.
	 * @param partialTicks The current partial tick time
	 * @param firstPerson True if the player being animated is the local client player, and they are in first-person
	 *                    view. In first-person, animations are only useful for animating the player's empty hand.
	 */
	public abstract void setRotationAngles(EntityPlayer player, ModelBiped model, float partialTicks, boolean firstPerson);

	/**
	 * Returns whether the boxes that form the second layer of the player's skin (for models that are instances of
	 * {@link net.minecraft.client.model.ModelPlayer ModelPlayer}) should automatically be set to the same angles as
	 * their corresponding first-layer parts.
	 * @param player The player being rendered, for reference
	 * @param firstPerson True if the player being animated is the local client player, and they are in first-person
	 *                    view. In first-person, animations are only useful for animating the player's empty hand.
	 * @return True to let {@link PlayerAnimator} auto-rotate the second skin layer, false to rotate them manually.
	 */
	public boolean autoRotateSecondLayer(EntityPlayer player, boolean firstPerson){
		return true;
	}

	/**
	 * Returns the arm of the given model corresponding to the given side. Function is identical to the method of the
	 * same name in {@link ModelBiped}.
	 * @param model The model to get the arm of
	 * @param side The {@link EnumHandSide} to return the arm for
	 * @return The {@link ModelRenderer} corresponding to the given arm
	 */
	public static ModelRenderer getArmForSide(ModelBiped model, EnumHandSide side){
		return side == EnumHandSide.LEFT ? model.bipedLeftArm : model.bipedRightArm;
	}

}
