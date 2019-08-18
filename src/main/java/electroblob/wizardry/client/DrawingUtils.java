package electroblob.wizardry.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

/**
 * Utility class containing some useful static methods for drawing GUIs. Previously these were spread across the main
 * {@code WizardryUtilities} class and various individual GUI classes.
 * 
 * @author Electroblob
 * @since Wizardry 4.2
 * @see MixedFontRenderer
 */
//@SideOnly(Side.CLIENT)
public final class DrawingUtils {

	/**
	 * The integer colour for black passed into the font renderer methods. This used to be 0 but that's now white for
	 * some reason, so I've made a it a constant in case it changes again.
	 */
	// I think this is actually ever-so-slightly lighter than pure black, but the difference is unnoticeable.
	public static final int BLACK = 1;

	/**
	 * Shorthand for {@link DrawingUtils#drawTexturedRect(int, int, int, int, int, int, int, int)} which draws the
	 * entire texture (u and v are set to 0 and textureWidth and textureHeight are the same as width and height).
	 */
	public static void drawTexturedRect(int x, int y, int width, int height){
		drawTexturedRect(x, y, 0, 0, width, height, width, height);
	}
	
	/**
	 * Draws a textured rectangle, taking the size of the image and the bit needed into
	 * account, unlike {@link net.minecraft.client.gui.Gui#drawTexturedModalRect(int, int, int, int, int, int)
	 * Gui.drawTexturedModalRect(int, int, int, int, int, int)}, which is harcoded for only 256x256 textures. Also handy
	 * for custom potion icons.
	 * 
	 * @param x The x position of the rectangle
	 * @param y The y position of the rectangle
	 * @param u The x position of the top left corner of the section of the image wanted
	 * @param v The y position of the top left corner of the section of the image wanted
	 * @param width The width of the section
	 * @param height The height of the section
	 * @param textureWidth The width of the actual image.
	 * @param textureHeight The height of the actual image.
	 */
	public static void drawTexturedRect(int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight){
		DrawingUtils.drawTexturedFlippedRect(x, y, u, v, width, height, textureWidth, textureHeight, false, false);
	}

	/**
	 * Draws a textured rectangle, taking the size of the image and the bit needed into
	 * account, unlike {@link net.minecraft.client.gui.Gui#drawTexturedModalRect(int, int, int, int, int, int)
	 * Gui.drawTexturedModalRect(int, int, int, int, int, int)}, which is harcoded for only 256x256 textures. Also handy
	 * for custom potion icons. This version allows the texture to additionally be flipped in x and/or y.
	 * 
	 * @param x The x position of the rectangle
	 * @param y The y position of the rectangle
	 * @param u The x position of the top left corner of the section of the image wanted
	 * @param v The y position of the top left corner of the section of the image wanted
	 * @param width The width of the section
	 * @param height The height of the section
	 * @param textureWidth The width of the actual image.
	 * @param textureHeight The height of the actual image.
	 * @param flipX Whether to flip the texture in the x direction.
	 * @param flipY Whether to flip the texture in the y direction.
	 */
	public static void drawTexturedFlippedRect(int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight, boolean flipX, boolean flipY){
	
		float f = 1F / (float)textureWidth;
		float f1 = 1F / (float)textureHeight;
		
		int u1 = flipX ? u + width : u;
		int u2 = flipX ? u : u + width;
		int v1 = flipY ? v + height : v;
		int v2 = flipY ? v : v + height;

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		
		buffer.begin(org.lwjgl.opengl.GL11.GL_QUADS, net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_TEX);

		buffer.pos((double)(x), 		(double)(y + height), 0).tex((double)((float)(u1) * f), (double)((float)(v2) * f1)).endVertex();
		buffer.pos((double)(x + width), (double)(y + height), 0).tex((double)((float)(u2) * f), (double)((float)(v2) * f1)).endVertex();
		buffer.pos((double)(x + width), (double)(y), 		  0).tex((double)((float)(u2) * f), (double)((float)(v1) * f1)).endVertex();
		buffer.pos((double)(x), 		(double)(y), 		  0).tex((double)((float)(u1) * f), (double)((float)(v1) * f1)).endVertex();

		tessellator.draw();
	}

	/**
	 * Draws a textured rectangle, stretching the section of the image to fit the size given.
	 * 
	 * @param x The x position of the rectangle
	 * @param y The y position of the rectangle
	 * @param u The x position of the top left corner of the section of the image wanted, expressed as a fraction of the
	 *        image width
	 * @param v The y position of the top left corner of the section of the image wanted, expressed as a fraction of the
	 *        image width
	 * @param finalWidth The width as rendered
	 * @param finalHeight The height as rendered
	 * @param width The width of the section, expressed as a fraction of the image width
	 * @param height The height of the section, expressed as a fraction of the image width
	 */
	public static void drawTexturedStretchedRect(int x, int y, int u, int v, int finalWidth, int finalHeight, int width,
			int height){
	
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		
		buffer.pos((x), y + finalHeight, 0).tex(u, v + height).endVertex();
		buffer.pos(x + finalWidth, y + finalHeight, 0).tex(u + width, v + height).endVertex();
		buffer.pos(x + finalWidth, (y), 0).tex(u + width, v).endVertex();
		buffer.pos((x), (y), 0).tex(u, v).endVertex();
		
		tessellator.draw();
	}

	/**
	 * Mixes the two given opaque colours in the proportion specified.
	 * @param colour1 The first colour to mix, as a 6-digit hexadecimal.
	 * @param colour2 The second colour to mix, as a 6-digit hexadecimal.
	 * @param proportion The proportion of the second colour; will be clamped to between 0 and 1.
	 * @return The resulting colour, as a 6-digit hexadecimal.
	 */
	public static int mix(int colour1, int colour2, float proportion){

		proportion = MathHelper.clamp(proportion, 0, 1);

		int r1 = colour1 >> 16 & 255;
		int g1 = colour1 >> 8 & 255;
		int b1 = colour1 & 255;
		int r2 = colour2 >> 16 & 255;
		int g2 = colour2 >> 8 & 255;
		int b2 = colour2 & 255;

		int r = (int)(r1 + (r2-r1) * proportion);
		int g = (int)(g1 + (g2-g1) * proportion);
		int b = (int)(b1 + (b2-b1) * proportion);

		return (r << 16) + (g << 8) + b;
	}

	/**
	 * Makes the given opaque colour translucent with the given opacity.
	 * @param colour An integer colour code, should be a 6-digit hexadecimal (i.e. opaque).
	 * @param opacity The opacity to apply to the given colour, as a fraction between 0 and 1.
	 * @return The resulting integer colour code, which will be an 8-digit hexadecimal.
	 */
	public static int makeTranslucent(int colour, float opacity){
		return colour + ((int)(0xff * opacity * 0x01000000));
	}

	/**
	 * Draws the given string at the given position, scaling it if it does not fit within the given width.
	 * @param font A {@code FontRenderer} object.
	 * @param text The text to display.
	 * @param x The x position of the top-left corner of the text.
	 * @param y The y position of the top-left corner of the text.
	 * @param scale The scale that the text should normally be if it does not exceed the maximum width.
	 * @param colour The colour to render the text in, supports translucency.
	 * @param width The maximum width of the text. <i>This is not scaled; you should pass in the width of the actual
	 * area of the screen in which the text needs to fit, regardless of the scale parameter.</i>
	 * @param centre Whether to adjust the y position such that the centre of the text lines up with where its centre
	 * would be if it was not scaled (automatically or manually).
	 * @param alignR True to right-align the text, false for normal left alignment.
	 */
	public static void drawScaledStringToWidth(FontRenderer font, String text, float x, float y, float scale, int colour, float width, boolean centre, boolean alignR){
		
		float textWidth = font.getStringWidth(text) * scale;
		float textHeight = font.FONT_HEIGHT * scale;
		
		if(textWidth > width){
			scale *= width/textWidth;
		}else if(alignR){ // Alignment makes no difference if the string fills the entire width
			x += width - textWidth;
		}
		
		if(centre) y += (font.FONT_HEIGHT - textHeight)/2;
		
		DrawingUtils.drawScaledTranslucentString(font, text, x, y, scale, colour);
	}

	/** Draws the given string at the given position, scaling the text by the specified factor. Also enables blending to
	 * render text in semitransparent colours (e.g. 0x88ffffff). */
	public static void drawScaledTranslucentString(FontRenderer font, String text, float x, float y, float scale, int colour){
		
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.scale(scale, scale, scale);
		// Because we scaled the entire rendering space, the coordinates have to be scaled inversely
		x /= scale;
		y /= scale;
		font.drawStringWithShadow(text, x, y, colour);
		GlStateManager.disableBlend();
		GlStateManager.popMatrix();
	}

	/**
	 * Draws an itemstack and (optionally) its tooltip, directly. Mainly intended for use outside of GUI classes, since
	 * most of the GL state changes done in this method (which are the main reason it exists at all) are already done
	 * when drawing a GUI.
	 * 
	 * @param gui An instance of a GUI class.
	 * @param stack The itemstack to draw.
	 * @param x The x position of the left-hand edge of the itemstack.
	 * @param y The y position of the top edge of the itemstack.
	 * @param mouseX The x position of the mouse, used for tooltip positioning.
	 * @param mouseY The y position of the mouse, used for tooltip positioning.
	 * @param tooltip Whether to draw the tooltip.
	 */
	public static void drawItemAndTooltip(GuiContainer gui, ItemStack stack, int x, int y, int mouseX, int mouseY, boolean tooltip){
	
		RenderItem renderItem = Minecraft.getMinecraft().getRenderItem();
		GlStateManager.pushMatrix();
		RenderHelper.enableGUIStandardItemLighting();
		GlStateManager.disableLighting();
		GlStateManager.enableRescaleNormal();
		GlStateManager.enableColorMaterial();
		GlStateManager.enableLighting();
		renderItem.zLevel = 100.0F;
	
		if(!stack.isEmpty()){
			renderItem.renderItemAndEffectIntoGUI(stack, x, y);
			renderItem.renderItemOverlays(Minecraft.getMinecraft().fontRenderer, stack, x, y);
	
			if(tooltip){
				gui.drawHoveringText(gui.getItemToolTip(stack), mouseX + gui.getXSize()/2 - gui.width/2,
						mouseY + gui.getYSize()/2 - gui.height/2);
			}
		}
	
		GlStateManager.popMatrix();
		GlStateManager.enableLighting();
		GlStateManager.enableDepth();
		RenderHelper.enableStandardItemLighting();
	}

}
