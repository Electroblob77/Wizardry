package electroblob.wizardry.client.renderer;

import electroblob.wizardry.tileentity.TileEntityStatue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public class RenderStatue extends TileEntitySpecialRenderer<TileEntityStatue> {
	
	private int destroyStage = 0; // Gets set each time a statue is rendered to allow access from the layer renderer

	@Override
	public void renderTileEntityAt(TileEntityStatue statue, double x, double y, double z, float partialTicks, int destroyStage){

		// Multiblock support for the breaking animation. The chest has its own way of doing this in
		// TileEntityRendererDispatcher, but I don't have access to that.
		if(statue.position != 1 && destroyStage >= 0){
			TileEntity tileentity = statue.getWorld().getTileEntity(statue.getPos().down(statue.position-1));
			//System.out.println(tileentity);
			if(tileentity instanceof TileEntityStatue){
				// If this is the block breaking animation pass and this isn't the bottom block, divert the call to
				// the bottom block.
				this.renderTileEntityAt((TileEntityStatue)tileentity, x, y - (statue.position-1), z, partialTicks, destroyStage);
			}
		}

		if(statue.creature != null && statue.position == 1){
			
			this.destroyStage = destroyStage;
			
			GlStateManager.pushMatrix();
			// The next line makes stuff render in the same place relative to the world wherever the player is.
			GlStateManager.translate((float)x + 0.5F, (float)y, (float)z + 0.5F);
			GlStateManager.enableLighting();
			
			float yaw = statue.creature.prevRotationYaw;
			int i = statue.creature.getBrightnessForRender(0);

			int j = i % 65536;
			int k = i / 65536;
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)j / 1.0F, (float)k / 1.0F);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

			GlStateManager.rotate(-yaw, 0F, 1F, 0F);
			// Stops the normal model from rendering.
			if(!statue.isIce) statue.creature.setInvisible(true);
			// Setting the last parameter to true prevents the debug bounding box from rendering.
			// For some reason, passing in the partialTicks causes the entity to spin round really fast
			Minecraft.getMinecraft().getRenderManager().doRenderEntity(statue.creature, 0, 0, 0, 0, 0, true);
			if(!statue.isIce) statue.creature.setInvisible(false);
			
			GlStateManager.popMatrix();

		}
	}
	
	public ResourceLocation getBlockBreakingTexture(){
		return destroyStage < 0 ? null : DESTROY_STAGES[destroyStage];
	}

}
