package electroblob.wizardry.client;

import org.lwjgl.input.Keyboard;

import electroblob.wizardry.SpellGlyphData;
import electroblob.wizardry.WizardData;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiSpellBook extends GuiScreen {
	
	private int xSize, ySize;
	private Spell spell;
    
	private static final ResourceLocation texture = new ResourceLocation(Wizardry.MODID, "textures/gui/spellbook.png");
	
	public GuiSpellBook(Spell spell) {
		super();
		xSize = 288;
		ySize = 180;
		this.spell = spell;
	}
	
	/**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int par1, int par2, float par3) {
    	
    	int xPos = this.width/2 - xSize/2;
    	int yPos = this.height/2 - this.ySize/2;
        
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        
        boolean discovered = true;
		if(Wizardry.settings.discoveryMode && !player.capabilities.isCreativeMode && WizardData.get(player) != null
				&& !WizardData.get(player).hasSpellBeenDiscovered(spell)){
			discovered = false;
		}

        // Draws spell illustration on opposite page, underneath the book so it shows through the hole.
		Minecraft.getMinecraft().renderEngine.bindTexture(discovered ? spell.getIcon() : Spells.none.getIcon());
		WizardryUtilities.drawTexturedRect(xPos + 145, yPos + 20, 0, 0, 128, 128, 128, 128);
		
        Minecraft.getMinecraft().renderEngine.bindTexture(texture);
        WizardryUtilities.drawTexturedRect(xPos, yPos, 0, 0, xSize, ySize, xSize, 256);
		
        super.drawScreen(par1, par2, par3);
        
		if(discovered){
			this.fontRendererObj.drawString(spell.getDisplayName(), xPos+17, yPos+14, 0);
			this.fontRendererObj.drawString(spell.type.getDisplayName(), xPos+17, yPos+25, 0x777777);
		}else{
			this.mc.standardGalacticFontRenderer.drawString(SpellGlyphData.getGlyphName(spell, player.worldObj), xPos+17, yPos+14, 0);
			this.mc.standardGalacticFontRenderer.drawString(spell.type.getDisplayName(), xPos+17, yPos+25, 0x777777);
		}
		
    	this.fontRendererObj.drawString("-------------------", xPos+17, yPos+34, 0);
        
        if(spell.tier == Tier.BASIC){
        	// Basic is usually white but this doesn't show up.
        	this.fontRendererObj.drawString("Tier: \u00A77" + Tier.BASIC.getDisplayName(), xPos+17, yPos+44, 0);
        }else{
        	this.fontRendererObj.drawString("Tier: " + spell.tier.getDisplayNameWithFormatting(), xPos+17, yPos+44, 0);
        }
        
        String element = "Element: " + spell.element.getFormattingCode() + spell.element.getDisplayName();
        if(!discovered) element = "Element: ?";
        this.fontRendererObj.drawString(element, xPos+17, yPos+56, 0);
        
        String manaCost = "Mana Cost: " + spell.cost;
        if(spell.isContinuous) manaCost = "Mana Cost: " + spell.cost + "/second";
        if(!discovered) manaCost = "Mana Cost: ?";
        this.fontRendererObj.drawString(manaCost, xPos+17, yPos+68, 0);
        
        if(discovered){
        	this.fontRendererObj.drawSplitString(spell.getDescription(), xPos+17, yPos+82, 118, 0);
        }else{
        	this.mc.standardGalacticFontRenderer.drawSplitString(SpellGlyphData.getGlyphDescription(spell, player.worldObj), xPos+17, yPos+82, 118, 0);
        }
        
        /*
        // Word wrapping
        int charNumber = 0;
        int lineNumber = 0;
        
        while(charNumber < spell.desc.length()){
        	int lineLength = 0;
        	String line;
        	if(spell.desc.length() - charNumber > 22){
	        	for(int i = charNumber; i < charNumber+23; i++){
	        		if(spell.desc.charAt(i) == ' '){
	        			lineLength = i - charNumber;
	        		}
	        	}
	        	line = spell.desc.substring(charNumber, charNumber + lineLength);
        	}else{
            	line = spell.desc.substring(charNumber, spell.desc.length());
            	charNumber = spell.desc.length();
        	}
            this.fontRendererObj.drawString("\u00A7o" + line, xPos+17, yPos+82+10*lineNumber, 0);
            charNumber+=(lineLength+1);
            lineNumber++;
        }
		*/
    }

	public void initGui()
    {
		super.initGui();
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
    }
	
    public void onGuiClosed()
    {
    	super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
    }
}

