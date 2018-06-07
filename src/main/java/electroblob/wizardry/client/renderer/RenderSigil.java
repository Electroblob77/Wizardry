package electroblob.wizardry.client.renderer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.entity.construct.EntityHealAura;
import electroblob.wizardry.entity.construct.EntityMagicConstruct;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class RenderSigil extends Render {
	
    private final ResourceLocation texture;
    private float scale = 1.0f;
    private boolean invisibleToEnemies;
    
	public RenderSigil(ResourceLocation texture, float scale, boolean invisibleToEnemies) {
		this.texture = texture;
		this.scale = scale;
		this.invisibleToEnemies = invisibleToEnemies;
	}

	@Override
    public void doRender(Entity entity, double par2, double par4, double par6, float par8, float par9){
		
		// Makes the sigil invisible to enemies of the player that created it
		if(this.invisibleToEnemies && entity instanceof EntityMagicConstruct){
			
			EntityMagicConstruct construct = (EntityMagicConstruct)entity;
			
			if(construct.getCaster() instanceof EntityPlayer
					&& !WizardryUtilities.isPlayerAlly((EntityPlayer)construct.getCaster(), Minecraft.getMinecraft().thePlayer)){
				return;
			}
		}
		
        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_LIGHTING);
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        
        float yOffset = 0;
        
        if(entity.riddenByEntity != null){
        	yOffset = entity.riddenByEntity.height/2;
        }
        
        GL11.glTranslatef((float)par2, (float)par4 + yOffset, (float)par6);
        
        this.bindTexture(texture);
        float f6 = 1.0F;
        float f7 = 0.5F;
        float f8 = 0.5F;
        
        GL11.glRotatef(-90, 1, 0, 0);
        
        // Healing aura rotates slowly
        if(entity instanceof EntityHealAura) GL11.glRotatef(entity.ticksExisted/3.0f, 0, 0, 1);
        
        GL11.glScalef(scale, scale, scale);
        
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        //tessellator.setColorRGBA_I(k1, 128);
        tessellator.setNormal(0.0F, 1.0F, 0.0F);
	    tessellator.addVertexWithUV((double)(0.0F - f7), (double)(0.0F - f8), 0.01, 0, 1);
	    tessellator.addVertexWithUV((double)(f6 - f7), (double)(0.0F - f8), 0.01, 1, 1);
	    tessellator.addVertexWithUV((double)(f6 - f7), (double)(1.0F - f8), 0.01, 1, 0);
	    tessellator.addVertexWithUV((double)(0.0F - f7), (double)(1.0F - f8), 0.01, 0, 0);
        
        tessellator.draw();
        
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        GL11.glPopMatrix();
    }

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return null;
	}

}
