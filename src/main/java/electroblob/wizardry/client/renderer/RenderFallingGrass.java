package electroblob.wizardry.client.renderer;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAnvil;
import net.minecraft.block.BlockDragonEgg;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderFallingBlock;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class RenderFallingGrass extends RenderFallingBlock {
	
	private RenderBlocks renderBlocks = new RenderBlocks();
	
	@Override
    public void doRender(EntityFallingBlock p_76986_1_, double p_76986_2_, double p_76986_4_, double p_76986_6_, float p_76986_8_, float p_76986_9_)
    {
        World world = p_76986_1_.func_145807_e();
        Block block = Blocks.grass;
        int i = MathHelper.floor_double(p_76986_1_.posX);
        int j = MathHelper.floor_double(p_76986_1_.posY);
        int k = MathHelper.floor_double(p_76986_1_.posZ);

        if (block != null && block != world.getBlock(i, j, k))
        {
            GL11.glPushMatrix();
            GL11.glTranslatef((float)p_76986_2_, (float)p_76986_4_, (float)p_76986_6_);
            this.bindEntityTexture(p_76986_1_);
            GL11.glDisable(GL11.GL_LIGHTING);
            
            this.renderBlocks.setRenderBoundsFromBlock(block);
            this.renderBlockSandFalling(block, world, i, j, k, p_76986_1_.field_145814_a);

            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glPopMatrix();
        }
    }
	
	private void renderBlockSandFalling(Block block, World world, int x, int y, int z, int metadata)
    {
        float f = 0.5F;
        float f1 = 1.0F;
        float f2 = 0.8F;
        float f3 = 0.6F;
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setBrightness(block.getMixedBrightnessForBlock(world, x, y, z));
        tessellator.setColorOpaque_F(f, f, f);
        this.renderBlocks.renderFaceYNeg(block, -0.5D, -0.5D, -0.5D, this.renderBlocks.getBlockIconFromSideAndMetadata(block, 0, metadata));
        
        int colour = world.getBiomeGenForCoords(x, z).getBiomeGrassColor(x, y, z);
        
        float r = (float)(colour >> 16 & 255) / 255.0F;
        float g = (float)(colour >> 8 & 255) / 255.0F;
        float b = (float)(colour & 255) / 255.0F;
        
        tessellator.setColorOpaque_F(f1*r, f1*g, f1*b);
        this.renderBlocks.renderFaceYPos(block, -0.5D, -0.5D, -0.5D, this.renderBlocks.getBlockIconFromSideAndMetadata(block, 1, metadata));
        tessellator.setColorOpaque_F(f2, f2, f2);
        this.renderBlocks.renderFaceZNeg(block, -0.5D, -0.5D, -0.5D, this.renderBlocks.getBlockIconFromSideAndMetadata(block, 2, metadata));
        tessellator.setColorOpaque_F(f2, f2, f2);
        this.renderBlocks.renderFaceZPos(block, -0.5D, -0.5D, -0.5D, this.renderBlocks.getBlockIconFromSideAndMetadata(block, 3, metadata));
        tessellator.setColorOpaque_F(f3, f3, f3);
        this.renderBlocks.renderFaceXNeg(block, -0.5D, -0.5D, -0.5D, this.renderBlocks.getBlockIconFromSideAndMetadata(block, 4, metadata));
        tessellator.setColorOpaque_F(f3, f3, f3);
        this.renderBlocks.renderFaceXPos(block, -0.5D, -0.5D, -0.5D, this.renderBlocks.getBlockIconFromSideAndMetadata(block, 5, metadata));
        tessellator.draw();
    }
}
