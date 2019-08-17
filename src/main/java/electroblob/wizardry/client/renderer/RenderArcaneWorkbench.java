package electroblob.wizardry.client.renderer;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.tileentity.ContainerArcaneWorkbench;
import electroblob.wizardry.tileentity.TileEntityArcaneWorkbench;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

public class RenderArcaneWorkbench extends TileEntitySpecialRenderer<TileEntityArcaneWorkbench> {

	private static final ResourceLocation runeTexture = new ResourceLocation(Wizardry.MODID, "textures/entity/rune.png");

	public RenderArcaneWorkbench(){}

	@Override
	public void render(TileEntityArcaneWorkbench tileentity, double x, double y, double z, float partialTicks, int destroyStage, float alpha){
		
		GlStateManager.pushMatrix();
		// This line makes stuff render in the same place relative to the world wherever the player is.
		GlStateManager.translate((float)x + 0.5F, (float)y + 1.5F, (float)z + 0.5F);
		GlStateManager.rotate(180, 0F, 0F, 1F);
		GlStateManager.pushMatrix();
		double angle;
		if(x < -0.5){
			angle = Math.toDegrees(Math.atan((z + 0.5) / (x + 0.5))) + 180;
		}else{
			angle = Math.toDegrees(Math.atan((z + 0.5) / (x + 0.5)));
		}
		this.renderEffect(tileentity, partialTicks);
		this.renderWand(tileentity, angle, partialTicks);
		GlStateManager.popMatrix();
		GlStateManager.popMatrix();
	}

	private void renderEffect(TileEntityArcaneWorkbench tileentity, float partialTicks){

		ItemStack itemstack = tileentity.getStackInSlot(ContainerArcaneWorkbench.CENTRE_SLOT);

		if(!itemstack.isEmpty()){
			GlStateManager.pushMatrix();
			GlStateManager.disableLighting();
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f);
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA); // This line fixes the weird brightness bug.
			GlStateManager.rotate(tileentity.timer + partialTicks, 0.0f, 1.0f, 0.0f);
			GlStateManager.translate(0.0f, 0.65f, 0.0f);
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder buffer = tessellator.getBuffer();
			bindTexture(runeTexture);

			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			buffer.pos(-0.5f, 0, -0.5f).tex(0, 0).endVertex();
			buffer.pos(0.5f, 0, -0.5f).tex(1, 0).endVertex();
			buffer.pos(0.5f, 0, 0.5f).tex(1, 1).endVertex();
			buffer.pos(-0.5f, 0, 0.5f).tex(0, 1).endVertex();
			tessellator.draw();

			GlStateManager.disableBlend();
			GlStateManager.enableLighting();
			GlStateManager.popMatrix();
		}
	}

	/**
	 * Renders the wand on the workbench as 3D on the model. Currently doesn't do much on 'fast' graphics!
	 * 
	 * @param tileentity The instance of the workbench tile entity
	 */
	private void renderWand(TileEntityArcaneWorkbench tileentity, double viewAngle, float partialTicks){

		ItemStack stack = tileentity.getStackInSlot(ContainerArcaneWorkbench.CENTRE_SLOT);

		if(!stack.isEmpty()){

			GlStateManager.pushMatrix();
			//GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);

			GlStateManager.rotate(180, 0, 1, 0);
			GlStateManager.rotate((float)(viewAngle - 90f), 0, 0, 1);
			// Does the floaty thing		<- #bestcommentever
			GlStateManager.translate(0.0F, 0.0F, 0.56f + 0.05f * MathHelper.sin((tileentity.timer + partialTicks)/15));//(float)tileentity.yOffset / 5000.0F + 0.55f);
			GlStateManager.scale(0.75F, 0.75F, 0.75F);
			// This is what the item frame uses so it's definitely what we want.
			Minecraft.getMinecraft().getRenderItem().renderItem(stack, TransformType.FIXED);
			GlStateManager.popMatrix();
		}
	}
}
