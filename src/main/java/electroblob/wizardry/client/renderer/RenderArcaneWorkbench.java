package electroblob.wizardry.client.renderer;

import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import electroblob.wizardry.tileentity.ContainerArcaneWorkbench;
import electroblob.wizardry.tileentity.TileEntityArcaneWorkbench;

public class RenderArcaneWorkbench extends TileEntitySpecialRenderer
{
	private final RenderBlocks renderBlocksInstance = new RenderBlocks();
	private static final ResourceLocation runeTexture = new ResourceLocation("wizardry:textures/entity/rune.png");
	
	public RenderArcaneWorkbench(){
	}
	
	@Override
	public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float f) {

        GL11.glPushMatrix();
        // It seems that these next two lines make stuff render in the same place relative to the world wherever
        // the player is.
		GL11.glTranslatef((float)x + 0.5F, (float)y +1.5F, (float)z + 0.5F);
		GL11.glRotatef(180, 0F, 0F, 1F);
        GL11.glPushMatrix();
        double angle = 0.0d;
        if(x < -0.5){
        	angle = Math.toDegrees(Math.atan((z+0.5)/(x+0.5))) + 180;
        }else{
        	angle = Math.toDegrees(Math.atan((z+0.5)/(x+0.5)));
        }
		this.renderEffect((TileEntityArcaneWorkbench)tileentity);
		this.renderWand((TileEntityArcaneWorkbench)tileentity, angle);
        GL11.glPopMatrix();
        GL11.glPopMatrix();
	}
	
	private void renderEffect(TileEntityArcaneWorkbench tileentity) {

        ItemStack itemstack = tileentity.getStackInSlot(ContainerArcaneWorkbench.WAND_SLOT);
        
        if(itemstack != null){
        	GL11.glPushMatrix();
            GL11.glDisable(GL11.GL_LIGHTING);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
	        GL11.glEnable(GL11.GL_BLEND);
	        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA); //This line fixes the weird brightness bug.
	        GL11.glRotatef(tileentity.timer, 0.0f, 1.0f, 0.0f);
        	GL11.glTranslatef(0.0f, 0.65f, 0.0f);
			Tessellator tessellator = Tessellator.instance;
			bindTexture(runeTexture);
			
			tessellator.startDrawingQuads();
			tessellator.addVertexWithUV(-0.5f, 0, -0.5f, 0, 0);
			tessellator.addVertexWithUV(0.5f, 0, -0.5f, 1, 0);
			tessellator.addVertexWithUV(0.5f, 0, 0.5f, 1, 1);
			tessellator.addVertexWithUV(-0.5f, 0, 0.5f, 0, 1);
			tessellator.draw();
			
	        GL11.glDisable(GL11.GL_BLEND);
	        GL11.glEnable(GL11.GL_LIGHTING);
	        GL11.glPopMatrix();
        }
	}

	/**
	 * Renders the wand on the workbench as 3D on the model. Currently doesn't do much on 'fast' graphics!
	 * @param tileentity The instance of the workbench tile entity
	 */
	
    private void renderWand(TileEntityArcaneWorkbench tileentity, double viewAngle)
    {
        ItemStack itemstack = tileentity.getStackInSlot(ContainerArcaneWorkbench.WAND_SLOT);
        
        if (itemstack != null)
        {
            EntityItem entityitem = new EntityItem(tileentity.getWorldObj(), 0.0d, 0.0d, 0.0d, itemstack);
            entityitem.hoverStart = 0.0F;
            GL11.glPushMatrix();
            GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
            
            GL11.glRotated(180, 0, 1, 0);
            // View angle is negated because of the 180 flip.
            GL11.glRotated(-viewAngle-90, 0, 0, 1);
            // Does the floaty thing
        	GL11.glTranslatef(0.0F, -0.25F, (float)tileentity.yOffset/5000.0F - 0.55f);
            GL11.glScalef(1.5F, 1.5F, 1.5F);
            RenderItem.renderInFrame = true; // Not too sure what this is
            RenderManager.instance.renderEntityWithPosYaw(entityitem, 0.0d, 0.0d, 0.0d, 0.0F, 0.0F);
            RenderItem.renderInFrame = false;
            GL11.glPopMatrix();
        }
    }
}
