package electroblob.wizardry.client.model;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import electroblob.wizardry.entity.living.EntityIceGiant;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;

@SideOnly(Side.CLIENT)
public class ModelIceGiant extends ModelBase
{
    /** The head model for the iron golem. */
    public ModelRenderer iceGiantHead;

    /** The body model for the iron golem. */
    public ModelRenderer iceGiantBody;

    /** The right arm model for the iron golem. */
    public ModelRenderer iceGiantRightArm;

    /** The left arm model for the iron golem. */
    public ModelRenderer iceGiantLeftArm;

    /** The left leg model for the Iron Golem. */
    public ModelRenderer iceGiantLeftLeg;

    /** The right leg model for the Iron Golem. */
    public ModelRenderer iceGiantRightLeg;
    
    ModelRenderer headSpike1;
    ModelRenderer headSpike2;
    ModelRenderer headSpike3;
    ModelRenderer headSpike4;
    ModelRenderer headSpike5;
    ModelRenderer headSpike6;
    ModelRenderer headSpike7;
    ModelRenderer rightArmSpike1;
    ModelRenderer rightArmSpike2;
    ModelRenderer leftArmSpike1;
    ModelRenderer leftArmSpike2;
    ModelRenderer bodySpike1;
    ModelRenderer bodySpike2;
    ModelRenderer bodySpike3;
    ModelRenderer bodySpike4;
    ModelRenderer bodySpike5;

    public ModelIceGiant()
    {
        this(0.0F);
    }

    public ModelIceGiant(float par1)
    {
        this(par1, -7.0F);
    }

    public ModelIceGiant(float par1, float par2)
    {
        short short1 = 128;
        short short2 = 128;
        
        this.iceGiantHead = (new ModelRenderer(this)).setTextureSize(short1, short2);
        this.iceGiantHead.setRotationPoint(0.0F, 0.0F + par2, -1.0F);
        this.iceGiantHead.setTextureOffset(0, 10).addBox(-6.0F, -14.0F, -6.5F, 12, 12, 12, par1);
        
        this.iceGiantBody = (new ModelRenderer(this)).setTextureSize(short1, short2);
        this.iceGiantBody.setRotationPoint(0.0F, 0.0F + par2, 0.0F);
        this.iceGiantBody.setTextureOffset(0, 40).addBox(-9.0F, -2.0F, -6.0F, 18, 12, 11, par1);
        this.iceGiantBody.setTextureOffset(0, 70).addBox(-4.5F, 10.0F, -3.0F, 9, 5, 6, par1 + 0.5F);
        
        this.iceGiantRightArm = (new ModelRenderer(this)).setTextureSize(short1, short2);
        this.iceGiantRightArm.setRotationPoint(0.0F, -7.0F, 0.0F);
        this.iceGiantRightArm.setTextureOffset(60, 21).addBox(-13.0F, -2.5F, -3.0F, 4, 30, 6, par1);
        
        this.iceGiantLeftArm = (new ModelRenderer(this)).setTextureSize(short1, short2);
        this.iceGiantLeftArm.setRotationPoint(0.0F, -7.0F, 0.0F);
        this.iceGiantLeftArm.setTextureOffset(60, 58).addBox(9.0F, -2.5F, -3.0F, 4, 30, 6, par1);
        
        this.iceGiantLeftLeg = (new ModelRenderer(this, 0, 22)).setTextureSize(short1, short2);
        this.iceGiantLeftLeg.setRotationPoint(-4.0F, 18.0F + par2, 0.0F);
        this.iceGiantLeftLeg.setTextureOffset(37, 0).addBox(-3.5F, -3.0F, -3.0F, 6, 16, 5, par1);
        
        this.iceGiantRightLeg = (new ModelRenderer(this, 0, 22)).setTextureSize(short1, short2);
        this.iceGiantRightLeg.mirror = true;
        this.iceGiantRightLeg.setTextureOffset(60, 0).setRotationPoint(5.0F, 18.0F + par2, 0.0F);
        this.iceGiantRightLeg.addBox(-3.5F, -3.0F, -3.0F, 6, 16, 5, par1);
        
        headSpike1 = new ModelRenderer(this, 0, 0).setTextureSize(short1, short2);
        headSpike1.addBox(-4F, -4F, 0F, 4, 4, 4);
        headSpike1.setRotationPoint(-4F, -10F, -5F);
        headSpike1.mirror = true;
        setRotationWithEulerYzx(headSpike1, -0.1047198F, -0.5235988F, 0.9599311F);
        
        headSpike2 = new ModelRenderer(this, 0, 0).setTextureSize(short1, short2);
        headSpike2.addBox(0F, -4F, 0F, 4, 4, 4);
        headSpike2.setRotationPoint(4F, -16F, 0F);
        headSpike2.mirror = true;
        setRotationWithEulerYzx(headSpike2, 0.5585054F, 0.9250245F, -0.5235988F);
        
        headSpike3 = new ModelRenderer(this, 0, 0).setTextureSize(short1, short2);
        headSpike3.addBox(-2F, -2F, -2F, 4, 4, 4);
        headSpike3.setRotationPoint(4F, -13F, 4F);
        headSpike3.mirror = true;
        setRotationWithEulerYzx(headSpike3, 0.7853982F, -1.396263F, 0.7853982F);
        
        headSpike4 = new ModelRenderer(this, 0, 0).setTextureSize(short1, short2);
        headSpike4.addBox(-4F, -4F, 0F, 4, 4, 4);
        headSpike4.setRotationPoint(-4F, -16F, 0F);
        headSpike4.mirror = true;
        setRotationWithEulerYzx(headSpike4, 0.5585054F, -0.9250245F, 0.5235988F);
        
        headSpike5 = new ModelRenderer(this, 0, 0).setTextureSize(short1, short2);
        headSpike5.addBox(0F, -4F, 0F, 4, 4, 4);
        headSpike5.setRotationPoint(4F, -10F, -5F);
        headSpike5.mirror = true;
        setRotationWithEulerYzx(headSpike5, 0.5235988F, -0.9599311F, 0.1047198F);
        
        rightArmSpike1 = new ModelRenderer(this, 0, 0).setTextureSize(short1, short2);
        rightArmSpike1.addBox(-2F, -2F, -2F, 4, 4, 4);
        rightArmSpike1.setRotationPoint(-11F, -8F, 0F);
        rightArmSpike1.mirror = true;
        setRotationWithEulerYzx(rightArmSpike1, 0.7853982F, 1.134464F, 0.9599311F);
        
        headSpike6 = new ModelRenderer(this, 0, 0).setTextureSize(short1, short2);
        headSpike6.addBox(-2F, -2F, -2F, 4, 4, 4);
        headSpike6.setRotationPoint(-4F, -13F, 4F);
        headSpike6.mirror = true;
        setRotationWithEulerYzx(headSpike6, 0.7853982F, -1.745329F, 0.7853982F);
        
        leftArmSpike1 = new ModelRenderer(this, 0, 0).setTextureSize(short1, short2);
        leftArmSpike1.addBox(-2F, -2F, -2F, 4, 4, 4);
        leftArmSpike1.setRotationPoint(11F, -8F, 0F);
        leftArmSpike1.mirror = true;
        setRotationWithEulerYzx(leftArmSpike1, 0.7853982F, -1.134464F, -0.9599311F);
        
        leftArmSpike2 = new ModelRenderer(this, 0, 0).setTextureSize(short1, short2);
        leftArmSpike2.addBox(-2F, -2F, -2F, 4, 4, 4);
        leftArmSpike2.setRotationPoint(12F, -4F, 0F);
        leftArmSpike2.mirror = true;
        setRotationWithEulerYzx(leftArmSpike2, 0.7853982F, 0F, -0.9599311F);
        
        bodySpike1 = new ModelRenderer(this, 32, 69).setTextureSize(short1, short2);
        bodySpike1.addBox(-3F, -3F, -3F, 6, 6, 6);
        bodySpike1.setRotationPoint(-4F, -4F, 3F);
        bodySpike1.mirror = true;
        setRotationWithEulerYzx(bodySpike1, 0.2808018F, 0.8096675F, 0.8339369F);
        
        rightArmSpike2 = new ModelRenderer(this, 0, 0).setTextureSize(short1, short2);
        rightArmSpike2.addBox(-2F, -2F, -2F, 4, 4, 4);
        rightArmSpike2.setRotationPoint(-12F, -4F, 0F);
        rightArmSpike2.mirror = true;
        setRotationWithEulerYzx(rightArmSpike2, 0.7853982F, 0F, 0.9599311F);
        
        bodySpike2 = new ModelRenderer(this, 32, 69).setTextureSize(short1, short2);
        bodySpike2.addBox(-3F, -3F, -3F, 6, 6, 6);
        bodySpike2.setRotationPoint(4F, -4F, 3F);
        bodySpike2.mirror = true;
        setRotationWithEulerYzx(bodySpike2, 0.2808018F, -0.8096757F, -0.8339358F);
        
        bodySpike3 = new ModelRenderer(this, 0, 0).setTextureSize(short1, short2);
        bodySpike3.addBox(-2F, -2F, -2F, 4, 4, 4);
        bodySpike3.setRotationPoint(6F, -2F, -5F);
        bodySpike3.mirror = true;
        setRotationWithEulerYzx(bodySpike3, 1.120006F, -1.347726F, -0.8969422F);
        
        bodySpike4 = new ModelRenderer(this, 32, 69).setTextureSize(short1, short2);
        bodySpike4.addBox(-3F, -3F, -3F, 6, 6, 6);
        bodySpike4.setRotationPoint(0F, -4F, -4F);
        bodySpike4.mirror = true;
        setRotationWithEulerYzx(bodySpike4, 0.7853982F, -1.570796F, 0.9599311F);
        
        headSpike7 = new ModelRenderer(this, 0, 0).setTextureSize(short1, short2);
        headSpike7.addBox(-2F, -2F, -2F, 4, 4, 4);
        headSpike7.setRotationPoint(0F, -20F, 4F);
        headSpike7.mirror = true;
        setRotationWithEulerYzx(headSpike7, 0.7853982F, 1.570796F, 0.7853982F);
        
        bodySpike5 = new ModelRenderer(this, 0, 0).setTextureSize(short1, short2);
        bodySpike5.addBox(-2F, -2F, -2F, 4, 4, 4);
        bodySpike5.setRotationPoint(-6F, -2F, -5F);
        bodySpike5.mirror = true;
        setRotationWithEulerYzx(bodySpike5, 1.120006F, 1.347725F, 0.896934F);
        
        this.convertToChild(this.iceGiantHead, headSpike1);
        this.convertToChild(this.iceGiantHead, headSpike2);
        this.convertToChild(this.iceGiantHead, headSpike3);
        this.convertToChild(this.iceGiantHead, headSpike4);
        this.convertToChild(this.iceGiantHead, headSpike5);
        this.convertToChild(this.iceGiantHead, headSpike6);
        this.convertToChild(this.iceGiantHead, headSpike7);
        this.convertToChild(this.iceGiantRightArm, rightArmSpike1);
        this.convertToChild(this.iceGiantRightArm, rightArmSpike2);
        this.convertToChild(this.iceGiantLeftArm, leftArmSpike1);
        this.convertToChild(this.iceGiantLeftArm, leftArmSpike2);
        this.convertToChild(this.iceGiantBody, bodySpike1);
        this.convertToChild(this.iceGiantBody, bodySpike2);
        this.convertToChild(this.iceGiantBody, bodySpike3);
        this.convertToChild(this.iceGiantBody, bodySpike4);
        this.convertToChild(this.iceGiantBody, bodySpike5);
        
    }

    /**
     * Sets the models various rotation angles then renders the model.
     */
    public void render(Entity par1Entity, float par2, float par3, float par4, float par5, float par6, float par7)
    {
        this.setRotationAngles(par2, par3, par4, par5, par6, par7, par1Entity);
        this.iceGiantHead.render(par7);
        this.iceGiantBody.render(par7);
        this.iceGiantLeftLeg.render(par7);
        this.iceGiantRightLeg.render(par7);
        this.iceGiantRightArm.render(par7);
        this.iceGiantLeftArm.render(par7);
    }

    /**
     * Sets the model's various rotation angles. For bipeds, par1 and par2 are used for animating the movement of arms
     * and legs, where par1 represents the time(so that arms and legs swing back and forth) and par2 represents how
     * "far" arms and legs can swing at most.
     */
    public void setRotationAngles(float par1, float par2, float par3, float par4, float par5, float par6, Entity par7Entity)
    {
        this.iceGiantHead.rotateAngleY = par4 / (180F / (float)Math.PI);
        this.iceGiantHead.rotateAngleX = par5 / (180F / (float)Math.PI);
        this.iceGiantLeftLeg.rotateAngleX = -1.5F * this.func_78172_a(par1, 13.0F) * par2;
        this.iceGiantRightLeg.rotateAngleX = 1.5F * this.func_78172_a(par1, 13.0F) * par2;
        this.iceGiantLeftLeg.rotateAngleY = 0.0F;
        this.iceGiantRightLeg.rotateAngleY = 0.0F;
    }

    /**
     * Used for easily adding entity-dependent animations. The second and third float params here are the same second
     * and third as in the setRotationAngles method.
     */
    public void setLivingAnimations(EntityLivingBase par1EntityLivingBase, float par2, float par3, float par4)
    {
        EntityIceGiant entityicegiant = (EntityIceGiant)par1EntityLivingBase;
        int i = entityicegiant.getAttackTimer();

        if (i > 0)
        {
            this.iceGiantRightArm.rotateAngleX = -2.0F + 1.5F * this.func_78172_a((float)i - par4, 10.0F);
            this.iceGiantLeftArm.rotateAngleX = -2.0F + 1.5F * this.func_78172_a((float)i - par4, 10.0F);
        }
        else
        {
            this.iceGiantRightArm.rotateAngleX = (-0.2F + 1.5F * this.func_78172_a(par2, 13.0F)) * par3;
            this.iceGiantLeftArm.rotateAngleX = (-0.2F - 1.5F * this.func_78172_a(par2, 13.0F)) * par3;
        }
    }

    private float func_78172_a(float par1, float par2)
    {
        return (Math.abs(par1 % par2 - par2 * 0.5F) - par2 * 0.25F) / (par2 * 0.25F);
    }
    
    /** This is really useful for converting the source from a Techne model export
    * which will have absolute rotation points that need to be converted before
    * creating the addChild() relationship. [Courtesy of jabelar] */
    protected void convertToChild(ModelRenderer parent, ModelRenderer child)
    {
       // move child rotation point to be relative to parent
       child.rotationPointX -= parent.rotationPointX;
       child.rotationPointY -= parent.rotationPointY;
       child.rotationPointZ -= parent.rotationPointZ;
       // make rotations relative to parent
       child.rotateAngleX -= parent.rotateAngleX;
       child.rotateAngleY -= parent.rotateAngleY;
       child.rotateAngleZ -= parent.rotateAngleZ;
       // create relationship
       parent.addChild(child);
    }
    
    /** Fixes the Techne rotation order bug. [Courtesy of tprk77] */
    private Vector3f ConvertEulerYzxToZyx(Vector3f eulerYzx) {
        // Create a matrix from YZX ordered Euler angles
        float a = MathHelper.cos(eulerYzx.x);
        float b = MathHelper.sin(eulerYzx.x);
        float c = MathHelper.cos(eulerYzx.y);
        float d = MathHelper.sin(eulerYzx.y);
        float e = MathHelper.cos(eulerYzx.z);
        float f = MathHelper.sin(eulerYzx.z);
        Matrix4f matrix = new Matrix4f();
        matrix.m00 = c * e;
        matrix.m01 = b * d - a * c * f;
        matrix.m02 = b * c * f + a * d;
        matrix.m10 = f;
        matrix.m11 = a * e;
        matrix.m12 = -b * e;
        matrix.m20 = -d * e;
        matrix.m21 = a * d * f + b * c;
        matrix.m22 = a * c - b * d * f;
        matrix.m33 = 1.0F;
        // Create ZYX ordered Euler angles from the matrix
        Vector3f eulerZyx = new Vector3f();
        eulerZyx.y = (float) Math.asin(MathHelper.clamp_float(-matrix.m20, -1, 1));
        if (MathHelper.abs(matrix.m20) < 0.99999) {
            eulerZyx.x = (float) Math.atan2(matrix.m21, matrix.m22);
            eulerZyx.z = (float) Math.atan2(matrix.m10, matrix.m00);
        } else {
            eulerZyx.x = 0.0F;
            eulerZyx.z = (float) Math.atan2(-matrix.m01, matrix.m11);
        }
        return eulerZyx;
    }
    
    private void setRotationWithEulerYzx(ModelRenderer model, float x, float y, float z) {
        Vector3f eulerYzx = new Vector3f(x, y, z);
        Vector3f eulerZyx = ConvertEulerYzxToZyx(eulerYzx);
        model.rotateAngleX = eulerZyx.x;
        model.rotateAngleY = eulerZyx.y;
        model.rotateAngleZ = eulerZyx.z;
    }
}
