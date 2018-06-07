package electroblob.wizardry.client.renderer;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import electroblob.wizardry.client.model.ModelPhoenix;
import electroblob.wizardry.entity.living.EntityPhoenix;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;

@SideOnly(Side.CLIENT)
public class RenderPhoenix extends RenderLiving
{
    private static final ResourceLocation texture = new ResourceLocation("wizardry:textures/entity/phoenix.png");

    /**
     * not actually sure this is size, is not used as of now, but the model would be recreated if the value changed and
     * it seems a good match for a bat's size
     */
    private int renderedPhoenixSize;

    public RenderPhoenix()
    {
        super(new ModelPhoenix(), 1.0f);
        //this.renderedPhoenixSize = ((ModelPhoenix)this.mainModel).getBatSize();
    }

    public void func_82443_a(EntityPhoenix par1EntityPhoenix, double par2, double par4, double par6, float par8, float par9)
    {
    	/*
        int i = ((ModelPhoenix)this.mainModel).getBatSize();

        if (i != this.renderedPhoenixSize)
        {
            this.renderedPhoenixSize = i;
            this.mainModel = new ModelPhoenix();
        }
    	 */
        super.doRender(par1EntityPhoenix, par2, par4, par6, par8, par9);
        
    }

    protected ResourceLocation getPhoenixTextures(EntityPhoenix par1EntityPhoenix)
    {
        return texture;
    }

    protected void func_82442_a(EntityPhoenix par1EntityPhoenix, float par2)
    {
        GL11.glScalef(1.0F, 1.0F, 1.0F);
    }

    protected void func_82445_a(EntityPhoenix par1EntityPhoenix, double par2, double par4, double par6)
    {
        super.renderLivingAt(par1EntityPhoenix, par2, par4, par6);
    }

    protected void func_82444_a(EntityPhoenix par1EntityPhoenix, float par2, float par3, float par4)
    {
        GL11.glTranslatef(0.0F, -0.1F, 0.0F);
        super.rotateCorpse(par1EntityPhoenix, par2, par3, par4);
    }

    public void doRenderLiving(EntityLiving par1EntityLiving, double par2, double par4, double par6, float par8, float par9)
    {
        this.func_82443_a((EntityPhoenix)par1EntityLiving, par2, par4, par6, par8, par9);
    }

    /**
     * Allows the render to do any OpenGL state modifications necessary before the model is rendered. Args:
     * entityLiving, partialTickTime
     */
    protected void preRenderCallback(EntityLivingBase par1EntityLivingBase, float par2)
    {
        this.func_82442_a((EntityPhoenix)par1EntityLivingBase, par2);
    }

    protected void rotateCorpse(EntityLivingBase par1EntityLivingBase, float par2, float par3, float par4)
    {
        this.func_82444_a((EntityPhoenix)par1EntityLivingBase, par2, par3, par4);
    }

    /**
     * Sets a simple glTranslate on a LivingEntity.
     */
    protected void renderLivingAt(EntityLivingBase par1EntityLivingBase, double par2, double par4, double par6)
    {
        this.func_82445_a((EntityPhoenix)par1EntityLivingBase, par2, par4, par6);
    }

    public void doRenderLiving(EntityLivingBase par1EntityLivingBase, double par2, double par4, double par6, float par8, float par9)
    {
        this.func_82443_a((EntityPhoenix)par1EntityLivingBase, par2, par4, par6, par8, par9);
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    protected ResourceLocation getEntityTexture(Entity par1Entity)
    {
        return this.getPhoenixTextures((EntityPhoenix)par1Entity);
    }

    /**
     * Actually renders the given argument. This is a synthetic bridge method, always casting down its argument and then
     * handing it off to a worker function which does the actual work. In all probabilty, the class Render is generic
     * (Render<T extends Entity) and this method has signature public void doRender(T entity, double d, double d1,
     * double d2, float f, float f1). But JAD is pre 1.5 so doesn't do that.
     */
    public void doRender(Entity par1Entity, double par2, double par4, double par6, float par8, float par9)
    {
    	GL11.glPushMatrix();
    	
    	GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
    	
        this.func_82443_a((EntityPhoenix)par1Entity, par2, par4, par6, par8, par9);

    	GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }
}
