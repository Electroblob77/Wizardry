package electroblob.wizardry.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelHammer extends ModelBase {
	
	ModelRenderer hammerHead;
	ModelRenderer handle;
	ModelRenderer handleEnd;
	ModelRenderer handleBase;
	ModelRenderer ring1;
	ModelRenderer ring2;

	public ModelHammer(){
		
		textureWidth = 64;
		textureHeight = 64;

		hammerHead = new ModelRenderer(this, 0, 0);
		hammerHead.addBox(0F, 0F, 0F, 20, 12, 12);
		hammerHead.setRotationPoint(-10F, 12F, -6F);
		hammerHead.setTextureSize(64, 64);
		hammerHead.mirror = true;
		setRotation(hammerHead, 0F, 0F, 0F);
		
		handle = new ModelRenderer(this, 0, 24);
		handle.addBox(0F, 0F, 0F, 4, 14, 4);
		handle.setRotationPoint(-2F, -2F, -2F);
		handle.setTextureSize(64, 64);
		handle.mirror = true;
		setRotation(handle, 0F, 0F, 0F);
		
		handleEnd = new ModelRenderer(this, 0, 49);
		handleEnd.addBox(0F, 0F, 0F, 5, 5, 5);
		handleEnd.setRotationPoint(-2.5F, -7F, -2.5F);
		handleEnd.setTextureSize(64, 64);
		handleEnd.mirror = true;
		setRotation(handleEnd, 0F, 0F, 0F);
		
		handleBase = new ModelRenderer(this, 0, 42);
		handleBase.addBox(0F, 0F, 0F, 5, 2, 5);
		handleBase.setRotationPoint(-2.5F, 10F, -2.5F);
		handleBase.setTextureSize(64, 64);
		handleBase.mirror = true;
		setRotation(handleBase, 0F, 0F, 0F);
		
		ring1 = new ModelRenderer(this, 20, 24);
		ring1.addBox(0F, 0F, 0F, 2, 14, 14);
		ring1.setRotationPoint(-8F, 11F, -7F);
		ring1.setTextureSize(64, 64);
		ring1.mirror = true;
		setRotation(ring1, 0F, 0F, 0F);
		
		ring2 = new ModelRenderer(this, 20, 24);
		ring2.addBox(0F, 0F, 0F, 2, 14, 14);
		ring2.setRotationPoint(6F, 11F, -7F);
		ring2.setTextureSize(64, 64);
		ring2.mirror = true;
		setRotation(ring2, 0F, 0F, 0F);
	}

	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5){
		super.render(entity, f, f1, f2, f3, f4, f5);
		setRotationAngles(f, f1, f2, f3, f4, f5, entity);
		hammerHead.render(f5);
		handle.render(f5);
		handleEnd.render(f5);
		handleBase.render(f5);
		ring1.render(f5);
		ring2.render(f5);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z){
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

	public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity entity){
		super.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
	}

}
