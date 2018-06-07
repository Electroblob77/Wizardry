package electroblob.wizardry.client.renderer;

import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import electroblob.wizardry.client.model.ModelSkeletonMinion;
import electroblob.wizardry.entity.living.EntitySkeletonMinion;

@SideOnly(Side.CLIENT)
public class RenderSkeletonMinion extends RenderBiped
{
    private static final ResourceLocation skeletonTextures = new ResourceLocation("textures/entity/skeleton/skeleton.png");
    private static final ResourceLocation witherSkeletonTextures = new ResourceLocation("textures/entity/skeleton/wither_skeleton.png");

    public RenderSkeletonMinion()
    {
        super(new ModelSkeletonMinion(), 0.5F);
    }

    protected void scaleSkeleton(EntitySkeletonMinion par1EntitySkeletonMinion, float par2)
    {
        if (par1EntitySkeletonMinion.getSkeletonType() == 1)
        {
            GL11.glScalef(1.2F, 1.2F, 1.2F);
        }
    }

    protected void func_82422_c()
    {
        GL11.glTranslatef(0.09375F, 0.1875F, 0.0F);
    }

    protected ResourceLocation func_110860_a(EntitySkeletonMinion par1EntitySkeletonMinion)
    {
        return par1EntitySkeletonMinion.getSkeletonType() == 1 ? witherSkeletonTextures : skeletonTextures;
    }

    protected ResourceLocation func_110856_a(EntityLiving par1EntityLiving)
    {
        return this.func_110860_a((EntitySkeletonMinion)par1EntityLiving);
    }

    /**
     * Allows the render to do any OpenGL state modifications necessary before the model is rendered. Args:
     * entityLiving, partialTickTime
     */
    protected void preRenderCallback(EntityLivingBase par1EntityLivingBase, float par2)
    {
        this.scaleSkeleton((EntitySkeletonMinion)par1EntityLivingBase, par2);
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    protected ResourceLocation getEntityTexture(Entity par1Entity)
    {
        return this.func_110860_a((EntitySkeletonMinion)par1Entity);
    }
}
