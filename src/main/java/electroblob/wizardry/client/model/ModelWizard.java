package electroblob.wizardry.client.model;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelWizard extends ModelBiped
{
  //fields
    ModelRenderer Shape5;
    ModelRenderer Shape8;
    ModelRenderer Shape9;
    ModelRenderer Shape10;
    ModelRenderer Shape7;
    ModelRenderer Shape11;
    ModelRenderer Shape12;
    ModelRenderer beard;
    ModelRenderer Shape13;
  
  public ModelWizard()
  {
	  //super(0, 0, 64, 32);
	  /*
      bipedRightLeg = new ModelRenderer(this, 32, 0); // 32 and 0 are the x and y texture offsets respectively.
      bipedRightLeg.addBox(-2F, 0F, -2F, 4, 12, 4); // x, y, z, u, v, w.
      bipedRightLeg.setRotationPoint(-2F, 12F, 0F); // Rotation point xyz (absolute, not relative)
      bipedRightLeg.setTextureSize(64, 64);
      bipedRightLeg.mirror = true;
      setRotation(bipedRightLeg, 0F, 0F, 0F);
      
      bipedLeftLeg.mirror = true;
      bipedLeftLeg = new ModelRenderer(this, 32, 0);
      bipedLeftLeg.addBox(-2F, 0F, -2F, 4, 12, 4);
      bipedLeftLeg.setRotationPoint(2F, 12F, 0F);
      bipedLeftLeg.setTextureSize(64, 64);
      bipedLeftLeg.mirror = true;
      setRotation(bipedLeftLeg, 0F, 0F, 0F);
      bipedLeftLeg.mirror = false;
      
      bipedBody = new ModelRenderer(this, 0, 16);
      bipedBody.addBox(0F, 0F, 0F, 8, 12, 4);
      bipedBody.setRotationPoint(-4F, 0F, -2F);
      bipedBody.setTextureSize(64, 64);
      bipedBody.mirror = true;
      setRotation(bipedBody, 0F, 0F, 0F);
      
      bipedLeftArm.mirror = true;
      bipedLeftArm = new ModelRenderer(this, 48, 0);
      bipedLeftArm.addBox(-1F, 0F, -2F, 4, 12, 4);
      bipedLeftArm.setRotationPoint(4F, 0F, 0F);
      bipedLeftArm.setTextureSize(64, 64);
      bipedLeftArm.mirror = true;
      setRotation(bipedLeftArm, 0F, 0F, 0F);
      bipedLeftArm.mirror = false;
      
      bipedRightArm = new ModelRenderer(this, 48, 0);
      bipedRightArm.addBox(-3F, 0F, -2F, 4, 12, 4);
      bipedRightArm.setRotationPoint(-4F, 0F, 0F);
      bipedRightArm.setTextureSize(64, 64);
      bipedRightArm.mirror = true;
      setRotation(bipedRightArm, 0F, 0F, 0F);
      
      bipedHead = new ModelRenderer(this, 0, 0);
      bipedHead.addBox(-4F, -8F, -4F, 8, 8, 8);
      bipedHead.setRotationPoint(0F, 0F, 0F);
      bipedHead.setTextureSize(64, 64);
      bipedHead.mirror = true;
      setRotation(bipedHead, 0F, 0F, 0F);
      */
      Shape5 = new ModelRenderer(this, 0, 51);
      Shape5.addBox(0F, 0F, 0F, 12, 1, 12);
      Shape5.setRotationPoint(-6F, -7F, -6F);
      Shape5.setTextureSize(64, 64);
      Shape5.mirror = true;
      setRotation(Shape5, 0F, 0F, 0F);
      Shape8 = new ModelRenderer(this, 0, 32);
      Shape8.addBox(0F, 0F, 0F, 6, 1, 6);
      Shape8.setRotationPoint(-3F, -9F, -3F);
      Shape8.setTextureSize(64, 64);
      Shape8.mirror = true;
      setRotation(Shape8, -0.0349066F, 0F, 0F);
      Shape9 = new ModelRenderer(this, 24, 32);
      Shape9.addBox(0F, 0F, 0F, 3, 3, 3);
      Shape9.setRotationPoint(-1.5F, -13F, -0.5F);
      Shape9.setTextureSize(64, 64);
      Shape9.mirror = true;
      setRotation(Shape9, -0.2511622F, 0F, 0F);
      Shape10 = new ModelRenderer(this, 0, 39);
      Shape10.addBox(0F, 0F, 0F, 5, 1, 5);
      Shape10.setRotationPoint(-2.5F, -10F, -2.5F);
      Shape10.setTextureSize(64, 64);
      Shape10.mirror = true;
      setRotation(Shape10, -0.0698132F, 0F, 0F);
      Shape7 = new ModelRenderer(this, 0, 45);
      Shape7.addBox(0F, 0F, 0F, 4, 2, 4);
      Shape7.setRotationPoint(-2F, -11F, -1.5F);
      Shape7.setTextureSize(64, 64);
      Shape7.mirror = true;
      setRotation(Shape7, -0.1396263F, 0F, 0F);
      Shape11 = new ModelRenderer(this, 20, 39);
      Shape11.addBox(0F, 0F, 0F, 2, 3, 2);
      Shape11.setRotationPoint(-1F, -15F, 1F);
      Shape11.setTextureSize(64, 64);
      Shape11.mirror = true;
      setRotation(Shape11, -0.4363323F, 0F, 0F);
      Shape12 = new ModelRenderer(this, 28, 39);
      Shape12.addBox(0F, 0F, 0F, 1, 2, 1);
      Shape12.setRotationPoint(-0.5F, -16F, 2.5F);
      Shape12.setTextureSize(64, 64);
      Shape12.mirror = true;
      setRotation(Shape12, -0.715585F, 0F, 0F);
      beard = new ModelRenderer(this, 32, 0);
      beard.addBox(0F, 0F, 0F, 8, 5, 0);
      beard.setRotationPoint(-4F, 0F, -4F);
      beard.setTextureSize(64, 64);
      beard.mirror = true;
      setRotation(beard, 0F, 0F, 0F);
      Shape13 = new ModelRenderer(this, 36, 16);
      Shape13.addBox(4F, 0F, 2F, 8, 20, 6);
      Shape13.setRotationPoint(-4F, 0F, -3F);
      Shape13.setTextureSize(64, 64);
      Shape13.mirror = true;
      setRotation(Shape13, 0F, 0F, 0F);
      
      // Makes head bits move with head
      //bipedHead.addChild(Shape5);
      bipedHead.addChild(beard);
      //bipedHead.addChild(Shape7);
      //bipedHead.addChild(Shape8);
      //bipedHead.addChild(Shape9);
      //bipedHead.addChild(Shape10);
      //bipedHead.addChild(Shape11);
      //bipedHead.addChild(Shape12);
      
      // Makes cloak attached to body
      //bipedBody.addChild(Shape13);
      
      // No outer head layer
      this.bipedHeadwear.isHidden = true;
  }
  /*
  public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
  {
    super.render(entity, f, f1, f2, f3, f4, f5);
    setRotationAngles(f, f1, f2, f3, f4, f5, entity);
    bipedRightLeg.render(f5);
    bipedLeftLeg.render(f5);
    bipedBody.render(f5);
    bipedLeftArm.render(f5);
    bipedRightArm.render(f5);
    bipedHead.render(f5);
    Shape5.render(f5);
    Shape8.render(f5);
    Shape9.render(f5);
    Shape10.render(f5);
    Shape7.render(f5);
    Shape11.render(f5);
    Shape12.render(f5);
    Shape6.render(f5);
    Shape13.render(f5);
  }
  */
  private void setRotation(ModelRenderer model, float x, float y, float z)
  {
    model.rotateAngleX = x;
    model.rotateAngleY = y;
    model.rotateAngleZ = z;
  }
  /*
  public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity entity)
  {
    super.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
  }
*/
}
