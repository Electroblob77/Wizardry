package electroblob.wizardry.client.renderer;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.entity.EntityMeteor;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

@SideOnly(Side.CLIENT)
public class RenderMeteor extends Render
{
    private final RenderBlocks sandRenderBlocks = new RenderBlocks();

    public RenderMeteor()
    {
        this.shadowSize = 0.5F;
    }

    /**
     * The actual render method that is used in doRender
     */
    public void doRenderFallingSand(EntityMeteor par1EntityFallingSand, double par2, double par4, double par6, float par8, float par9)
    {
        World world = par1EntityFallingSand.getWorld();
        Block block = Wizardry.meteor;
        GL11.glPushMatrix();
        GL11.glTranslatef((float)par2, (float)par4 + 0.5f, (float)par6);
        this.bindEntityTexture(par1EntityFallingSand);
        GL11.glDisable(GL11.GL_LIGHTING);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
        
        par1EntityFallingSand.width = 0.98f;
        par1EntityFallingSand.height = 0.98f;
        
        this.sandRenderBlocks.setRenderBoundsFromBlock(block);
        this.sandRenderBlocks.renderBlockSandFalling(block, world, MathHelper.floor_double(par1EntityFallingSand.posX), MathHelper.floor_double(par1EntityFallingSand.posY), MathHelper.floor_double(par1EntityFallingSand.posZ), 0);

        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glPopMatrix();
    }

    protected ResourceLocation getFallingSandTextures(EntityMeteor par1Entity)
    {
        return TextureMap.locationBlocksTexture;
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    protected ResourceLocation getEntityTexture(Entity par1Entity)
    {
        return this.getFallingSandTextures((EntityMeteor)par1Entity);
    }

    /**
     * Actually renders the given argument. This is a synthetic bridge method, always casting down its argument and then
     * handing it off to a worker function which does the actual work. In all probabilty, the class Render is generic
     * (Render<T extends Entity) and this method has signature public void doRender(T entity, double d, double d1,
     * double d2, float f, float f1). But JAD is pre 1.5 so doesn't do that.
     */
    public void doRender(Entity par1Entity, double par2, double par4, double par6, float par8, float par9)
    {
        this.doRenderFallingSand((EntityMeteor)par1Entity, par2, par4, par6, par8, par9);
    }
}
