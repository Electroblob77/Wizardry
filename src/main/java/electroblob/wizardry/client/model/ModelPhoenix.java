package electroblob.wizardry.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;

public class ModelPhoenix extends ModelBase {
	
    ModelRenderer body;
    ModelRenderer rightWing;
    ModelRenderer leftWing;
    ModelRenderer tailFeathers;
    ModelRenderer tail;
    ModelRenderer head;
    ModelRenderer neck;
    ModelRenderer beak;
    ModelRenderer plume;
  
  public ModelPhoenix()
  {
    textureWidth = 64;
    textureHeight = 64;
    
    /* For future reference:
     * - setRotationPoint sets the origin of a part relative to that of its parent.
     * - The first 3 arguments of addBox set the position of a part relative to its rotation point, and the last
     *   3 arguments are the size of the part.
     * (This means that rotation point and position seem to be the wrong way round, since changing the rotation point
     * will move the component without changing which point on the component it rotates about.)
     * - The two integer arguments in the ModelRenderer constructor are the texture offset.
     * - Mirror does nothing unless you set it before addBox.
     * - Rotation is the usual pitch, yaw, roll.
     */
    
      body = new ModelRenderer(this, 0, 34);
      body.addBox(0F, 0F, -3F, 6, 15, 6);
      body.setRotationPoint(-3F, 0F, -5F);
      body.setTextureSize(64, 64);
      body.mirror = true;
      setRotation(body, 0.296706F, 0F, 0F);
      
      rightWing = new ModelRenderer(this, 0, 0);
      rightWing.mirror = true;
      rightWing.addBox(-27F, -27F, 0F, 27, 34, 0);
      rightWing.setRotationPoint(0F, 5F, 0F);
      rightWing.setTextureSize(64, 64);
      setRotation(rightWing, 0.1745329F, 0F, 0F);
      
      leftWing = new ModelRenderer(this, 0, 0);
      leftWing.addBox(0F, -27F, 0F, 27, 34, 0);
      leftWing.setRotationPoint(6F, 5F, 0F);
      leftWing.setTextureSize(64, 64);
      setRotation(leftWing, 0.1745329F, 0F, 0F);
      
      tailFeathers = new ModelRenderer(this, 0, 57);
      tailFeathers.addBox(-5F, 0F, 0F, 10, 7, 0);
      tailFeathers.setRotationPoint(0F, 7F, 1F);
      tailFeathers.setTextureSize(64, 64);
      tailFeathers.mirror = true;
      setRotation(tailFeathers, 0.5235988F, 0F, 0F);
      
      tail = new ModelRenderer(this, 20, 55);
      tail.addBox(-1F, 0F, -1F, 2, 7, 2);
      tail.setRotationPoint(3F, 15F, 2F);
      tail.setTextureSize(64, 64);
      tail.mirror = true;
      setRotation(tail, 0.4014257F, 0F, 0F);
      
      head = new ModelRenderer(this, 24, 34);
      head.addBox(-2F, -4F, -5F, 4, 4, 6);
      head.setRotationPoint(0F, -4F, 0F);
      head.setTextureSize(64, 64);
      head.mirror = true;
      setRotation(head, 0F, 0F, 0F);
      
      neck = new ModelRenderer(this, 24, 44);
      neck.addBox(-1F, -4F, -1F, 2, 4, 2);
      neck.setRotationPoint(0F, 0F, -4F);
      neck.setTextureSize(64, 64);
      neck.mirror = true;
      setRotation(neck, 0.2443461F, 0F, 0F);
      
      beak = new ModelRenderer(this, 32, 44);
      beak.addBox(-0.5F, 4F, -1F, 1, 2, 3);
      beak.setRotationPoint(0F, -5F, -8F);
      beak.setTextureSize(64, 64);
      beak.mirror = true;
      setRotation(beak, 0.2792527F, 0F, 0F);
      
      plume = new ModelRenderer(this, 28, 50);
      plume.addBox(-0.03333334F, 1F, 0F, 0, 5, 5);
      plume.setRotationPoint(0F, -7F, 0F);
      plume.setTextureSize(64, 64);
      plume.mirror = true;
      setRotation(plume, 0F, 0F, 0F);
      
      neck.addChild(head);
      head.addChild(plume);
      head.addChild(beak);
      tail.addChild(tailFeathers);
      body.addChild(tail);
      body.addChild(rightWing);
      body.addChild(leftWing);
  }
  
  public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
  {
    super.render(entity, f, f1, f2, f3, f4, f5);
    setRotationAngles(f, f1, f2, f3, f4, f5, entity);
    
    float f6 = (180F / (float)Math.PI);
    this.neck.rotateAngleX = f4 / (180F / (float)Math.PI);
    this.neck.rotateAngleY = f3 / (180F / (float)Math.PI);
    this.neck.rotateAngleZ = 0.0F;
    this.body.rotateAngleX = 0.3f + MathHelper.cos(f2 * 0.1F) * 0.15F;
    this.body.rotateAngleY = 0.0F;
    this.tail.rotateAngleX = this.body.rotateAngleX * 1.1f;
    this.tailFeathers.rotateAngleX = this.body.rotateAngleX * 1.2f;
    this.rightWing.rotateAngleY = MathHelper.cos(f2 * 0.3F) * (float)Math.PI * 0.15F;
    this.leftWing.rotateAngleY = -this.rightWing.rotateAngleY;
    
    body.render(f5);
    neck.render(f5);
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
