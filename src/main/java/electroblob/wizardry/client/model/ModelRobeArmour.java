package electroblob.wizardry.client.model;

import electroblob.wizardry.block.BlockStatue;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelRobeArmour extends ModelArmourFixer {

	ModelRenderer robe;
	private boolean showHeadWithBody;

	public ModelRobeArmour(float delta, boolean showHeadWithBody){

		super(delta, 0, 64, 64);

		this.showHeadWithBody = showHeadWithBody;

		bipedBody = new ModelRenderer(this, 16, 16);
		bipedBody.addBox(-4F, 0F, -2F, 8, 11, 4, delta);
		bipedBody.setRotationPoint(0F, 0F, 0F);
		bipedBody.setTextureSize(64, 64);
		bipedBody.mirror = true;
		setRotation(bipedBody, 0F, 0F, 0F);

		robe = new ModelRenderer(this, 40, 32);
		robe.addBox(-4F, 0F, -2F, 8, 9, 4, delta);
		robe.setRotationPoint(0F, 12, 0F); // 12.5 accounts for the expansion of each box
		robe.setTextureSize(64, 64);
		robe.mirror = true;
		setRotation(robe, 0F, 0F, 0F);
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
		if(showHeadWithBody) this.bipedHead.showModel = this.bipedBody.showModel;
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
