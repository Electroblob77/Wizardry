package electroblob.wizardry.client.renderer;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import electroblob.wizardry.entity.living.EntitySpiderMinion;
import net.minecraft.client.renderer.entity.RenderSpider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.util.ResourceLocation;

@SideOnly(Side.CLIENT)
public class RenderSpiderMinion extends RenderSpider
{
    private static final ResourceLocation spiderMinionTextures = new ResourceLocation("textures/entity/spider/cave_spider.png");

    public RenderSpiderMinion()
    {
        this.shadowSize *= 0.7F;
    }

    protected void scaleSpider(EntitySpiderMinion par1EntitySpiderMinion, float par2)
    {
        GL11.glScalef(0.7F, 0.7F, 0.7F);
    }

    protected ResourceLocation getSpiderMinionTextures(EntitySpiderMinion par1EntitySpiderMinion)
    {
        return spiderMinionTextures;
    }

    protected ResourceLocation getSpiderTextures(EntitySpiderMinion par1EntitySpider)
    {
        return this.getSpiderMinionTextures(par1EntitySpider);
    }

    /**
     * Allows the render to do any OpenGL state modifications necessary before the model is rendered. Args:
     * entityLiving, partialTickTime
     */
    protected void preRenderCallback(EntityLivingBase par1EntityLivingBase, float par2)
    {
        this.scaleSpider((EntitySpiderMinion)par1EntityLivingBase, par2);
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    protected ResourceLocation getEntityTexture(Entity par1Entity)
    {
        return this.getSpiderMinionTextures((EntitySpiderMinion)par1Entity);
    }
}
