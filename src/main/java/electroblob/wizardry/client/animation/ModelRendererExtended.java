package electroblob.wizardry.client.animation;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;

/**
 * A wrapper around {@link ModelRenderer} which can be swapped into models to allow render pre/post events to
 * circumvent {@code ModelBase.setRotationAngles(...)} and set model part rotations themselves. Instances of this class
 * keep a reference to the original {@code ModelRenderer} and delegate all the actual rendering to it.
 * <p></p>
 * {@link PlayerAnimator PlayerAnimator} does all the necessary setup with this
 * class for player animations. It is also possible to set up custom animations for non-player entities with this class,
 * see {@link ModelRendererExtended#wrap(ModelBiped)} and {@link ModelRendererExtended#wrap(ModelBase, ModelRenderer)}.
 * @author Electroblob
 * @since Wizardry 4.3
 */
public class ModelRendererExtended extends ModelRenderer {

	// We probably could have copied all the fields over instead, but this way if anyone else modifies the boxes we
	// won't overwrite their changes
	// This way also avoids reflection because we don't need to copy private fields, which is better for performance
	private ModelRenderer delegate;

	private float actualRotationX;
	private float actualRotationY;
	private float actualRotationZ;

	private float extraRotationX;
	private float extraRotationY;
	private float extraRotationZ;

	private ModelRendererExtended(ModelBase baseModel, ModelRenderer delegate){
		super(baseModel, delegate.boxName);
		this.delegate = delegate;
		// Copy over any public fields that might get changed
		this.textureWidth = delegate.textureWidth;
		this.textureHeight = delegate.textureHeight;
		this.showModel = delegate.showModel;
		this.isHidden = delegate.isHidden;
		this.rotationPointX = delegate.rotationPointX;
		this.rotationPointY = delegate.rotationPointY;
		this.rotationPointZ = delegate.rotationPointZ;
		this.cubeList = delegate.cubeList;
		this.childModels = delegate.childModels;
		this.resetRotation();
	}

	// Static initialisers

	/**
	 * Replaces the given part of the given {@link ModelBase} with a new {@code ModelRendererExtended} that wraps the
	 * original part. This method takes care of modifying the model's box list, but <b>is unable to change any
	 * individual {@link ModelRenderer} fields</b> - this method instead returns the resulting
	 * {@code ModelRendererExtended} to be assigned to the appropriate field.
	 * @param model The model whose part is to be wrapped
	 * @param box The part of the model to wrap, must belong to the above model
	 * @return The resulting wrapped model part, this should be assigned to the appropriate part field in the model. If
	 * the given model part was already wrapped, this method simply returns it unchanged.
	 * @throws IllegalArgumentException if the given box does not belong to the given model
	 */
	public static ModelRendererExtended wrap(ModelBase model, ModelRenderer box){
		if(box instanceof ModelRendererExtended) return (ModelRendererExtended)box; // Ignore already-wrapped parts
		ModelRendererExtended wrapper = new ModelRendererExtended(model, box);
		int index = model.boxList.indexOf(box);
		if(index < 0) throw new IllegalArgumentException(String.format("The given ModelRenderer %s does not belong to the given model %s", box, model));
		model.boxList.set(index, wrapper); // I doubt the order matters but we may as well put it at the same index
		return wrapper;
	}

	/**
	 * Replaces the given {@link ModelBiped}'s parts with new {@code ModelRendererExtended} versions that wrap the
	 * original parts. If the given model is a {@link ModelPlayer}, the extra boxes for player skin overlays will also
	 * be wrapped.
	 */
	public static void wrap(ModelBiped model){
		// MMmmmmmm wraps
		model.bipedHead = wrap(model, model.bipedHead);
		model.bipedBody = wrap(model, model.bipedBody);
		model.bipedRightArm = wrap(model, model.bipedRightArm);
		model.bipedLeftArm = wrap(model, model.bipedLeftArm);
		model.bipedRightLeg = wrap(model, model.bipedRightLeg);
		model.bipedLeftLeg = wrap(model, model.bipedLeftLeg);
		model.bipedHeadwear = wrap(model, model.bipedHeadwear);

		if(model instanceof ModelPlayer){
			((ModelPlayer)model).bipedBodyWear = wrap(model, ((ModelPlayer)model).bipedBodyWear);
			((ModelPlayer)model).bipedRightArmwear = wrap(model, ((ModelPlayer)model).bipedRightArmwear);
			((ModelPlayer)model).bipedLeftArmwear = wrap(model, ((ModelPlayer)model).bipedLeftArmwear);
			((ModelPlayer)model).bipedRightLegwear = wrap(model, ((ModelPlayer)model).bipedRightLegwear);
			((ModelPlayer)model).bipedLeftLegwear = wrap(model, ((ModelPlayer)model).bipedLeftLegwear);
		}
	}

	/** Resets the rotation of this model part to the angle set by the parent model. */
	public void resetRotation(){
		this.actualRotationX = Float.NaN;
		this.actualRotationY = Float.NaN;
		this.actualRotationZ = Float.NaN;
		this.extraRotationX = 0;
		this.extraRotationY = 0;
		this.extraRotationZ = 0;
	}

	/** Sets the rotation of this model part, which will overwrite the angle set by the parent model. */
	public void setRotation(float x, float y, float z){
		this.actualRotationX = x;
		this.actualRotationY = y;
		this.actualRotationZ = z;
	}

	/** Sets the extra rotation of this model part, which will be added onto the angle set by the parent model. */
	public void addRotation(float x, float y, float z){
		this.extraRotationX = x;
		this.extraRotationY = y;
		this.extraRotationZ = z;
	}

	/** Sets the rotation of this model part to the same values as the given box. */
	public void setRotationTo(ModelRendererExtended box){
		this.actualRotationX = box.actualRotationX;
		this.actualRotationY = box.actualRotationY;
		this.actualRotationZ = box.actualRotationZ;
		this.extraRotationX = box.extraRotationX;
		this.extraRotationY = box.extraRotationY;
		this.extraRotationZ = box.extraRotationZ;
	}

	// Delegate rendering, but fiddle with the angles first

	@Override
	public void render(float scale){

		// Need to copy these over each time in case they were changed
		delegate.showModel = this.showModel;
		delegate.isHidden = this.isHidden;
		delegate.rotationPointX = this.rotationPointX;
		delegate.rotationPointY = this.rotationPointY;
		delegate.rotationPointZ = this.rotationPointZ;

		if(!Float.isNaN(actualRotationX) && !Float.isNaN(actualRotationY) && !Float.isNaN(actualRotationZ)){
			delegate.rotateAngleX = actualRotationX;
			delegate.rotateAngleY = actualRotationY;
			delegate.rotateAngleZ = actualRotationZ;
		}else{
			delegate.rotateAngleX = this.rotateAngleX + extraRotationX;
			delegate.rotateAngleY = this.rotateAngleY + extraRotationY;
			delegate.rotateAngleZ = this.rotateAngleZ + extraRotationZ;
		}

		delegate.render(scale);
	}

	// Delegate all other methods

	@Override
	public void addChild(ModelRenderer renderer){
		delegate.addChild(renderer);
	}

	@Override
	public void renderWithRotation(float scale){
		delegate.renderWithRotation(scale);
	}

	@Override
	public void postRender(float scale){
		// Exactly the same setup as above, just add item rotation/translation fields and setters
//		float angle = 1;
//		float radius = 10;
//		delegate.rotationPointY -= radius * MathHelper.cos(angle);
//		delegate.rotationPointZ -= radius * MathHelper.sin(angle);
//		delegate.rotateAngleX += angle;
		delegate.postRender(scale);
	}

}
