package electroblob.wizardry.client.model;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelWizardArmour extends ModelBiped
{
    ModelRenderer Shape1;
    ModelRenderer Shape2;
    ModelRenderer Shape3;
    ModelRenderer Shape4;
    ModelRenderer Shape5;
    ModelRenderer Shape6;
    ModelRenderer Shape7;
    ModelRenderer robe;
  
  public ModelWizardArmour(float scale){
	  
	  super(scale, 0, 64, 64);
    
	  // This is necessary to stop the head from scaling.
      this.bipedHead = new ModelRenderer(this, 0, 0);
      this.bipedHead.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, 0.1f);
      this.bipedHead.setRotationPoint(0.0F, 0.0F + 0, 0.0F);
	  
      Shape1 = new ModelRenderer(this, -16, 32);
      Shape1.addBox(-8F, -7F, -8F, 16, 0, 16);
      Shape1.setRotationPoint(0F, 0F, 0F);
      Shape1.setTextureSize(64, 64);
      Shape1.mirror = true;
      setRotation(Shape1, 0F, 0F, 0F);
      
      Shape2 = new ModelRenderer(this, 0, 48);
      Shape2.addBox(0F, 0F, 0F, 6, 2, 6);
      Shape2.setRotationPoint(-3F, -10F, -3F);
      Shape2.setTextureSize(64, 64);
      Shape2.mirror = true;
      setRotation(Shape2, -0.1396263F, 0F, 0F);
      
      Shape3 = new ModelRenderer(this, 0, 56);
      Shape3.addBox(0F, 0F, 0F, 5, 2, 5);
      Shape3.setRotationPoint(-2.5F, -11.53333F, -2F);
      Shape3.setTextureSize(64, 64);
      Shape3.mirror = true;
      setRotation(Shape3, -0.2443461F, 0F, 0F);
      
      Shape4 = new ModelRenderer(this, 24, 48);
      Shape4.addBox(0F, 0F, 0F, 4, 2, 4);
      Shape4.setRotationPoint(-2F, -13F, -1F);
      Shape4.setTextureSize(64, 64);
      Shape4.mirror = true;
      setRotation(Shape4, -0.4014257F, 0F, 0F);
      
      Shape5 = new ModelRenderer(this, 24, 54);
      Shape5.addBox(0F, 0F, 0F, 3, 2, 3);
      Shape5.setRotationPoint(-1.5F, -14F, 0F);
      Shape5.setTextureSize(64, 64);
      Shape5.mirror = true;
      setRotation(Shape5, -0.5759587F, 0F, 0F);
      
      Shape6 = new ModelRenderer(this, 20, 59);
      Shape6.addBox(0F, 0F, 0F, 2, 2, 2);
      Shape6.setRotationPoint(-1F, -14F, 0F);
      Shape6.setTextureSize(64, 64);
      Shape6.mirror = true;
      setRotation(Shape6, 0.3316126F, 0F, 0F);
      
      Shape7 = new ModelRenderer(this, 28, 59);
      Shape7.addBox(0F, 0F, 0F, 1, 1, 3);
      Shape7.setRotationPoint(-0.5F, -14.5F, 2F);
      Shape7.setTextureSize(64, 64);
      Shape7.mirror = true;
      setRotation(Shape7, -0.5585054F, 0F, 0F);
      
      robe = new ModelRenderer(this, 40, 42);
      robe.addBox(-4F, 0F, -2F, 8, 18, 4, scale);
      robe.setRotationPoint(0F, 0F, 0F);
      robe.setTextureSize(64, 64);
      robe.mirror = true;
      setRotation(robe, 0F, 0F, 0F);
      
      // Makes the hat rotate with the head.
      bipedHead.addChild(Shape1);
      bipedHead.addChild(Shape2);
      bipedHead.addChild(Shape3);
      bipedHead.addChild(Shape4);
      bipedHead.addChild(Shape5);
      bipedHead.addChild(Shape6);
      bipedHead.addChild(Shape7);
      // Makes the robe move with the body
      bipedBody.addChild(robe);
  }
  
  public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
  {
    super.render(entity, f, f1, f2, f3, f4, f5);
    setRotationAngles(f, f1, f2, f3, f4, f5, entity);
  }
  
  private void setRotation(ModelRenderer model, float x, float y, float z)
  {
    model.rotateAngleX = x;
    model.rotateAngleY = y;
    model.rotateAngleZ = z;
  }
  
  public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity entity)
  {
    super.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
  }

}
