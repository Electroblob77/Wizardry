package electroblob.wizardry.client;

import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;

/** Font renderer that renders parts of strings surrounded by '#' (without quotes) in the SGA instead of normal text.
 * @since Wizardry 1.1 */
@SideOnly(Side.CLIENT)
public class MixedFontRenderer extends FontRenderer {
	
	public MixedFontRenderer(GameSettings p_i1035_1_, ResourceLocation p_i1035_2_, TextureManager p_i1035_3_,
			boolean p_i1035_4_) {
		super(p_i1035_1_, p_i1035_2_, p_i1035_3_, p_i1035_4_);
	}
	
	@Override
    public int drawString(String string, int x, int y, int colour, boolean shadow){
		
		int l = 0;
		
		boolean sga = false;
		
		while(string.indexOf('#') > -1){
			
			String section = string.substring(0, string.indexOf('#'));
			
			if(sga){
	        	l += Minecraft.getMinecraft().standardGalacticFontRenderer.drawString(section, x, y, colour, shadow);
				x += Minecraft.getMinecraft().standardGalacticFontRenderer.getStringWidth(section);
	        }else{
	        	l += Minecraft.getMinecraft().fontRenderer.drawString(section, x, y, colour, shadow);
				x += Minecraft.getMinecraft().fontRenderer.getStringWidth(section);
	        }
			
			string = string.substring(string.indexOf('#') + 1);
			sga = !sga;
		}
		
		if(sga){
        	l += Minecraft.getMinecraft().standardGalacticFontRenderer.drawString(string, x, y, colour, shadow);
        }else{
        	l += Minecraft.getMinecraft().fontRenderer.drawString(string, x, y, colour, shadow);
        }
		
        return l;
    }
	
	@Override
	public int getStringWidth(String string){
		
		int l = 0;
		
		boolean sga = false;
		
		while(string.indexOf('#') > -1){
			
			String section = string.substring(0, string.indexOf('#'));
			
			if(sga){
	        	l += Minecraft.getMinecraft().standardGalacticFontRenderer.getStringWidth(section);
	        }else{
	        	l += Minecraft.getMinecraft().fontRenderer.getStringWidth(section);
	        }
			
			string = string.substring(string.indexOf('#') + 1);
			sga = !sga;
		}
		
		if(sga){
        	l += Minecraft.getMinecraft().standardGalacticFontRenderer.getStringWidth(string);
        }else{
        	l += Minecraft.getMinecraft().fontRenderer.getStringWidth(string);
        }
		
        return l;
	}
	
	// This doesn't work the same way yet
	@Override
    public void drawSplitString(String string, int x, int y, int width, int colour){
		
		if(string.contains("#")){
        	Minecraft.getMinecraft().standardGalacticFontRenderer.drawSplitString(string.substring(1), x, y, width, colour);
        }else{
        	Minecraft.getMinecraft().fontRenderer.drawSplitString(string, x, y, width, colour);
        }
    }

}
