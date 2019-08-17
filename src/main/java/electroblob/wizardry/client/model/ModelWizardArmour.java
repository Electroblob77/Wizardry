package electroblob.wizardry.client.model;

import electroblob.wizardry.block.BlockStatue;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelWizardArmour extends ModelArmourFixer {

	ModelRenderer hatBrim;
	ModelRenderer hatSegment1;
	ModelRenderer hatSegment2;
	ModelRenderer hatSegment3;
	ModelRenderer hatSegment4;
	ModelRenderer hatSegment5;
	ModelRenderer hatSegment6;
	ModelRenderer robe;

	public ModelWizardArmour(float delta){

		super(delta, 0, 64, 64);

		// This is necessary to stop the head from scaling.
		this.bipedHead = new ModelRenderer(this, 0, 0);
		// The hat layer has an offset of 0.5, so 0.6 is about the smallest we can get away with
		this.bipedHead.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.6f);
		this.bipedHead.setRotationPoint(0.0F, 0.0F + 0, 0.0F);

		hatBrim = new ModelRenderer(this, 0, 47);
		// Making the height 1 stops the top and bottom z-fighting when the hat is enchanted
		hatBrim.addBox(-8F, -6.85F, -8F, 16, 1, 16, 0.6f);
		hatBrim.setRotationPoint(0F, 0F, 0F);
		hatBrim.setTextureSize(64, 64);
		hatBrim.mirror = true;
		setRotation(hatBrim, 0F, 0F, 0F);

		hatSegment1 = new ModelRenderer(this, 0, 32);
		hatSegment1.addBox(0F, 0F, 0F, 6, 2, 6, 0.2f);
		hatSegment1.setRotationPoint(-3F, -10.6F, -3F);
		hatSegment1.setTextureSize(64, 64);
		hatSegment1.mirror = true;
		setRotation(hatSegment1, -0.1396263F, 0F, 0F);

		hatSegment2 = new ModelRenderer(this, 0, 40);
		hatSegment2.addBox(0F, 0F, 0F, 5, 2, 5, 0.1f);
		hatSegment2.setRotationPoint(-2.5F, -12.13333F, -2F);
		hatSegment2.setTextureSize(64, 64);
		hatSegment2.mirror = true;
		setRotation(hatSegment2, -0.2443461F, 0F, 0F);

		hatSegment3 = new ModelRenderer(this, 24, 32);
		hatSegment3.addBox(0F, 0F, 0F, 4, 2, 4);
		hatSegment3.setRotationPoint(-2F, -13.6F, -1F);
		hatSegment3.setTextureSize(64, 64);
		hatSegment3.mirror = true;
		setRotation(hatSegment3, -0.4014257F, 0F, 0F);

		hatSegment4 = new ModelRenderer(this, 24, 38);
		hatSegment4.addBox(0F, 0F, 0F, 3, 2, 3);
		hatSegment4.setRotationPoint(-1.5F, -14.6F, 0F);
		hatSegment4.setTextureSize(64, 64);
		hatSegment4.mirror = true;
		setRotation(hatSegment4, -0.5759587F, 0F, 0F);

		hatSegment5 = new ModelRenderer(this, 20, 43);
		hatSegment5.addBox(0F, 0F, 0F, 2, 2, 2);
		hatSegment5.setRotationPoint(-1F, -14.6F, 0F);
		hatSegment5.setTextureSize(64, 64);
		hatSegment5.mirror = true;
		setRotation(hatSegment5, 0.3316126F, 0F, 0F);

		hatSegment6 = new ModelRenderer(this, 28, 43);
		hatSegment6.addBox(0F, 0F, 0F, 1, 1, 3);
		hatSegment6.setRotationPoint(-0.5F, -15.1F, 2F);
		hatSegment6.setTextureSize(64, 64);
		hatSegment6.mirror = true;
		setRotation(hatSegment6, -0.5585054F, 0F, 0F);

		bipedBody = new ModelRenderer(this, 16, 16);
		bipedBody.addBox(-4F, 0F, -2F, 8, 11, 4, delta);
		bipedBody.setRotationPoint(0F, 0F, 0F);
		bipedBody.setTextureSize(64, 64);
		bipedBody.mirror = true;
		setRotation(bipedBody, 0F, 0F, 0F);

		robe = new ModelRenderer(this, 40, 32);
		robe.addBox(-4F, 0F, -2F, 8, 7, 4, delta);
		robe.setRotationPoint(0F, 12, 0F); // 12.5 accounts for the expansion of each box
		robe.setTextureSize(64, 64);
		robe.mirror = true;
		setRotation(robe, 0F, 0F, 0F);

		// Makes the hat rotate with the head.
		bipedHead.addChild(hatBrim);
		bipedHead.addChild(hatSegment1);
		bipedHead.addChild(hatSegment2);
		bipedHead.addChild(hatSegment3);
		bipedHead.addChild(hatSegment4);
		bipedHead.addChild(hatSegment5);
		bipedHead.addChild(hatSegment6);
	}

	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5){
		if(entity.isInvisible() && !entity.getEntityData().getBoolean(BlockStatue.PETRIFIED_NBT_KEY)
				&& !entity.getEntityData().getBoolean(BlockStatue.FROZEN_NBT_KEY)) return;
		super.render(entity, f, f1, f2, f3, f4, f5);
		setRotationAngles(f, f1, f2, f3, f4, f5, entity);
		this.robe.render(f5);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z){
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

	public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity entity){
		super.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
		this.robe.showModel = this.bipedBody.showModel;
		if(this.isSneak){
			//this.robe.rotationPointY = 10.5f;
			this.robe.rotationPointZ = 4;
		}else{
			//this.robe.rotationPointY = 12.5f;
			this.robe.rotationPointZ = 0;
		}

		// The bottom part of the robe takes the y rotation from the rest of the robe but the x/z rotation
		// from the average of the two legs
		this.robe.rotateAngleX = (this.bipedLeftLeg.rotateAngleX + this.bipedRightLeg.rotateAngleX) / 2f;
		this.robe.rotateAngleY = this.bipedBody.rotateAngleY;
		this.robe.rotateAngleZ = (this.bipedLeftLeg.rotateAngleZ + this.bipedRightLeg.rotateAngleZ) / 2f;
	}

}
