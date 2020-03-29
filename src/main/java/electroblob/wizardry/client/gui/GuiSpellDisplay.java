package electroblob.wizardry.client.gui;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import electroblob.wizardry.Settings;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.client.ClientProxy;
import electroblob.wizardry.client.DrawingUtils;
import electroblob.wizardry.client.MixedFontRenderer;
import electroblob.wizardry.data.SpellGlyphData;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.item.ISpellCastingItem;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.WandHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.Map.Entry;

@Mod.EventBusSubscriber(Side.CLIENT)
public class GuiSpellDisplay {

	private static final ResourceLocation INDEX = new ResourceLocation(Wizardry.MODID, "textures/gui/spell_hud/_index.json");
	
	/** A map which stores all loaded HUD skin objects. This gets wiped on resource pack reload and repopulated with
	 * mappings as specified by {@code _index.json} (these stack between resource packs). The keys in the map correspond
	 * to the keys in {@code _index.json}, and are sorted in that order, with skins belonging to resource packs sorted
	 * from lowest to highest priority. The skins in the base mod will therefore always be first. (It should be noted,
	 * however, that in the gui itself the skins are always sorted in alphabetical order for some reason.) */
	private static final Map<String, Skin> skins = new LinkedHashMap<>(14); // 14 is the number of skins packaged with the mod
	
	private static final Gson gson = new Gson();

	/** Width and height of the spell icon (very unlikely to change!) */
	private static final int SPELL_ICON_SIZE = 32;
	/** Number of ticks the spell switching animation plays for. */
	private static final int SPELL_SWITCH_TIME = 4;
	/** Scale of the next/previous spell names. */
	private static final float SPELL_NAME_SCALE = 0.5f;
	/** Opacity of the next/previous spell names, as a fraction. */
	private static final float SPELL_NAME_OPACITY = 0.3f;

	private static final int HALF_HOTBAR_WIDTH = 97; // Half the width of the hotbar, plus a bit for clearance
	private static final int OFFHAND_SLOT_WIDTH = 29; // Width of the offhand slot plus the gap between it and the hotbar
	
	/** Controls the spell switching animation. Positive when switching to the next spell, negative when switching to
	 * the previous spell. Decremented in magnitude by 1 each tick until it reaches 0 again. */
	private static int switchTimer = 0;
	
	/** 
	 * Starts the spell switching animation.
	 * @param next True to switch to the next spell, false for the previous spell.
	 */
	public static void playSpellSwitchAnimation(boolean next){
		switchTimer = next ? SPELL_SWITCH_TIME : -SPELL_SWITCH_TIME;
	}
	
	/** Returns an unmodifiable set of the string keys for all of the loaded spell HUD skins. */
	public static Set<String> getSkinKeys(){
		return Collections.unmodifiableSet(skins.keySet());
	}
	
	/** Returns an unmodifiable view of the loaded spell HUD skins map. */
	public static Map<String, Skin> getSkins(){
		return Collections.unmodifiableMap(skins);
	}
	
	/** Returns the skin that corresponds to the given key. */
	public static Skin getSkin(String key){
		return skins.get(key);
	}
	
	// Normally when extending Gui, you'd have to have an instance to access its methods. However, we're not actually
	// using any of them, so this class may as well not bother and just be a static event handler. Neat!
	@SubscribeEvent
	public static void draw(RenderGameOverlayEvent.Post event){
		
		Minecraft mc = Minecraft.getMinecraft();

		EntityPlayer player = mc.player;

		if(player.isSpectator()) return; // Spectators shouldn't have the spell HUD!

		// If the player has a wand in each hand, only displays for the one in the main hand.

		ItemStack wand = player.getHeldItemMainhand();
		boolean mainHand = true;

		if(!(wand.getItem() instanceof ISpellCastingItem && ((ISpellCastingItem)wand.getItem()).showSpellHUD(player, wand))){
			wand = player.getHeldItemOffhand();
			mainHand = false;
			// If the player isn't holding a spellcasting item that shows the HUD, then nothing else needs to be done.
			if(!(wand.getItem() instanceof ISpellCastingItem && ((ISpellCastingItem)wand.getItem()).showSpellHUD(player, wand))) return;
		}

		int width = event.getResolution().getScaledWidth();
		int height = event.getResolution().getScaledHeight();

		boolean flipX = Wizardry.settings.spellHUDPosition.flipX;
		boolean flipY = Wizardry.settings.spellHUDPosition.flipY;

		if(Wizardry.settings.spellHUDPosition.dynamic){
			// ............. | This bit is true if the wand is on the left, false if it is on the right
			flipX = flipX == ((mainHand ? player.getPrimaryHand() : player.getPrimaryHand().opposite()) == EnumHandSide.LEFT);
		}
		
		Skin skin = skins.get(Wizardry.settings.spellHUDSkin);
		
		if(skin == null){
			
			Wizardry.logger.info("The spell HUD skin '" + Wizardry.settings.spellHUDSkin + "' specified in the config"
					+ " did not match any of the loaded skins; using the default skin as a fallback.");
			
			skin = skins.get(Settings.DEFAULT_HUD_SKIN_KEY);
			
			if(skin == null){
				Wizardry.logger.warn("The default spell HUD skin is missing! A resource pack must have overridden it"
						+ " with an invalid JSON file (default.json), please try again without any resource packs.");
				return;
			}
		}

		GlStateManager.pushMatrix();
		
		// 'Origin' of the spell hud (bottom left corner of the actual texture, always in the corner of the screen)
		int x = flipX ? width : 0;
		int y = flipY ? 0: height;

		// The space available to render the spell HUD
		float xSpace = (float)(width/2 - HALF_HOTBAR_WIDTH);
		if(!player.getHeldItemOffhand().isEmpty()
				// Tests whether the offhand slot is rendered on the same side of the hotbar as the spell HUD
				&& (player.getPrimaryHand() == EnumHandSide.LEFT) == flipX){
			xSpace -= OFFHAND_SLOT_WIDTH;
		}

		// If the skin is at the bottom and the screen width is too small, scale it to avoid the hotbar and offhand
		if(!flipY && skin.getWidth() > xSpace){ // width/2 - 91 - 29 taken from GuiInGame line 547
			float scale = xSpace / skin.getWidth();
			GlStateManager.scale(scale, scale, 1);
			x = MathHelper.ceil(x/scale);
			y = MathHelper.ceil(y/scale);
		}

		Spell spell = WandHelper.getCurrentSpell(wand);
		int cooldown = WandHelper.getCurrentCooldown(wand);
		int maxCooldown = WandHelper.getCurrentMaxCooldown(wand);

		if(event.getType() == RenderGameOverlayEvent.ElementType.TEXT){
			
			float animationProgress = Math.signum(switchTimer) * ((SPELL_SWITCH_TIME - Math.abs(switchTimer) +
					event.getPartialTicks()) / SPELL_SWITCH_TIME);
			
			String prevSpellName = getFormattedSpellName(WandHelper.getPreviousSpell(wand), player, WandHelper.getPreviousCooldown(wand));
			String spellName = getFormattedSpellName(spell, player, cooldown);
			String nextSpellName = getFormattedSpellName(WandHelper.getNextSpell(wand), player, WandHelper.getNextCooldown(wand));

			skin.drawText(x, y, flipX, flipY, prevSpellName, spellName, nextSpellName, animationProgress);

		}else if(event.getType() == RenderGameOverlayEvent.ElementType.HOTBAR){

			boolean discovered = true;

			if(!player.isCreative() && WizardData.get(player) != null){
				discovered = WizardData.get(player).hasSpellBeenDiscovered(spell);
			}
			
			ResourceLocation icon = discovered ? spell.getIcon() : Spells.none.getIcon();

			float progress = 1;
			// Doesn't really matter what progress is when in creative, but we might as well avoid the calculation.
			if(!player.isCreative() && !spell.isContinuous){
				// Subtracted partial tick time to make it smoother
				progress = maxCooldown == 0 ? 1 : (maxCooldown - (float)cooldown + event.getPartialTicks()) / maxCooldown;
			}
			
			skin.drawBackground(x, y, flipX, flipY, icon, progress, player.isCreative());
			
		}

		GlStateManager.popMatrix();
	}
	
	/**
	 * Gets the name of the given spell, with formatting added according to its cooldown and whether the given player
	 * has discovered it.
	 * @param spell The spell to get the name of.
	 * @param player The player to test for having discovered the given spell.
	 * @param cooldown The spell's current cooldown.
	 * @return The spell name, with relevant formatting added, for use with the {@link MixedFontRenderer}.
	 */
	private static String getFormattedSpellName(Spell spell, EntityPlayer player, int cooldown){
		
		boolean discovered = true;

		if(!player.isCreative() && WizardData.get(player) != null){
			discovered = WizardData.get(player).hasSpellBeenDiscovered(spell);
		}
		
		// Makes spells greyed out if they are in cooldown or if the player has the arcane jammer effect
		String format = cooldown > 0 || player.isPotionActive(WizardryPotions.arcane_jammer) ? "\u00A78" : spell.getElement().getFormattingCode();
		if(!discovered) format = "\u00A79";
		
		String name = discovered ? spell.getDisplayName() : SpellGlyphData.getGlyphName(spell, player.world);
		name = format + name;
		if(!discovered) name = "#" + name + "#";
		
		return name;
	}
	
	@SubscribeEvent
	public static void onLivingUpdateEvent(LivingUpdateEvent event){
		if(event.getEntity() == Minecraft.getMinecraft().player){ // Makes sure this only gets called once each tick.
			if(switchTimer > 0) switchTimer--;
			else if(switchTimer < 0) switchTimer++;
		}
	}
	
	/** Called from preInit in the main mod class (via the proxies) to initialise the HUD skins, and again on each
	 * resource reload. */
	public static void loadSkins(IResourceManager manager){
		
		try {

			List<IResource> indexFiles = manager.getAllResources(INDEX);
			
			skins.clear(); // Wipes the skins map before repopulating it
			
			for(IResource indexFile : indexFiles){
				
				BufferedReader reader = new BufferedReader(new InputStreamReader(indexFile.getInputStream()));
				
				JsonElement je = gson.fromJson(reader, JsonElement.class);
				JsonObject json = je.getAsJsonObject();
				
				// Need to iterate over these since we don't know what they're called or how many there are
				for(Entry<String, JsonElement> entry : json.entrySet()){
					
					String key = entry.getKey(); // Find out what each element is called, this will be the skins map key
					
					// It's a good idea to use JsonUtils because it produces more helpful error messages (that pack
					// makers should understand).
					
					JsonObject skinData = JsonUtils.getJsonObject(json, key);
					
					String[] splitName = ResourceLocation.splitObjectName(JsonUtils.getString(skinData, "texture"));
					ResourceLocation texture = new ResourceLocation(splitName[0], "textures/" + splitName[1] + ".png");
					
					splitName = ResourceLocation.splitObjectName(JsonUtils.getString(skinData, "metadata"));
					ResourceLocation metadata = new ResourceLocation(splitName[0], "textures/" + splitName[1] + ".json");
					
					// The nice thing about this is it overwrites the existing mapping, and since the index files are in
					// ascending order of resource pack priority, this means resource packs can override existing skins
					// by specifying one with the same key.
					skins.put(key, new Skin(texture, metadata));
				}
			}

		} catch (IOException e){
			// If an exception is thrown, chances are the resource pack did not have a spell_hud folder, so nothing
			// else needs to be done.
			Wizardry.logger.error("Error reading spell HUD skin index file: ", e);
		}
	}
	
	/**
	 * Instances of this class represent individual HUD skins, complete with texture and all necessary metadata. This
	 * class serves to separate the logic behind the spell HUD from its actual rendering.
	 * All information and processing done within this class relates only to the actual drawing; spells and such like
	 * must be queried outside of this class and fed into the methods as appropriate.
	 * 
	 * @author Electroblob
	 * @since Wizardry 4.2
	 */
	public static class Skin {
		
		/** The texture file for this skin. */
		private final ResourceLocation texture;

		/** The display name of the skin in the config menu. */
		private String name;
		/** The description of the skin shown when its button is hovered over in the config menu. */
		private String description;
		
		/** Width of the entire spell HUD. */
		private int width;
		/** Height of the entire spell HUD. */
		private int height;

		/** Whether the entire HUD is flipped when on the right-hand side of the screen. If this is false, the HUD will
		 * still appear on the right-hand side of the screen, but in the same orientation as on the left-hand side. */
		private boolean mirrorX;
		/** Whether the entire HUD is flipped when at the top of the screen. If this is false, the HUD will
		 * still appear at the top of the screen, but in the same orientation as at the bottom. */
		private boolean mirrorY;
		
		/** Distance of the spell icon from the left edge of the screen (or right edge when flipped). */
		private int spellIconInsetX;
		/** Distance of the spell icon from the bottom edge of the screen (or top edge when flipped). */
		private int spellIconInsetY;

		/** Distance of the spell name from the left edge of the screen (or right edge when flipped). */
		private int textInsetX;
		/** Distance of the spell name from the bottom edge of the screen (or the top edge when flipped). */
		private int textInsetY;
		
		/** Horizontal distance between the start of adjacent spell names. */
		private int cascadeOffsetX;
		/** Vertical distance between the start of adjacent spell names. */
		private int cascadeOffsetY;

		/** Distance of the cooldown bar from the left edge of the screen (or right edge when flipped). */
		private int cooldownBarX;
		/** Distance of the cooldown bar from the bottom edge of the screen (or top edge when flipped). */
		private int cooldownBarY;
		/** Length of the cooldown bar. */
		private int cooldownBarLength;
		/** Height of the cooldown bar. */
		private int cooldownBarHeight;

		/** Whether the cooldown bar is flipped horizontally when the HUD is on the right-hand side of the screen. */
		private boolean cooldownBarMirrorX;
		/** Whether the cooldown bar is flipped vertically when the HUD is at the top of the screen. */
		private boolean cooldownBarMirrorY;
		
		/** Whether the cooldown bar progress overlay is shown when the cooldown bar is full (i.e. when progress = 1). */
		private boolean showCooldownWhenFull;
		
		private final Minecraft mc;
		
		/** Creates a new skin with the given texture and reads its values from the given metadata json file. */
		public Skin(ResourceLocation texture, ResourceLocation metadata){
			
			mc = Minecraft.getMinecraft();
			
			this.texture = texture;
			
			try {
				// This time we only want the highest priority file
				IResource metadataFile = Minecraft.getMinecraft().getResourceManager().getResource(metadata);
				BufferedReader reader = new BufferedReader(new InputStreamReader(metadataFile.getInputStream()));
					
				JsonElement je = gson.fromJson(reader, JsonElement.class);
				
				parseJson(je.getAsJsonObject());

			} catch (IOException e){
				// If an exception is thrown, chances are the resource pack did not have a spell_hud folder, so nothing
				// else needs to be done.
				Wizardry.logger.error("Error reading spell HUD skin metadata file: ", e);
			}
		}
		
		/** Returns the display name of this HUD skin, which is shown in the config GUI. */
		public String getName(){
			return name;
		}
		
		/** Returns the description of this HUD skin, which is shown in its tooltip in the config GUI. */
		public String getDescription(){
			return description;
		}
		
		/** Returns the overall width of this spell HUD skin. */
		public int getWidth(){
			return width;
		}
		
		/** Returns the overall height of this spell HUD skin. */
		public int getHeight(){
			return height;
		}
		
		/** Actually reads the metadata values for this skin from the json file. */
		private void parseJson(JsonObject json){
			
			// For now, all the keys must be present for the metadata file to work (the only ones that could reasonably
			// have a default anyway are the mirror values).
			
			name = JsonUtils.getString(json, "name");
			description = JsonUtils.getString(json, "description");
			
			width = JsonUtils.getInt(json, "width");
			if(width > 128) Wizardry.logger.warn("The width of the spell HUD skin " + name + " exceeds 128, this may cause it to render strangely.");
			height = JsonUtils.getInt(json, "height");
			
			JsonObject mirror = JsonUtils.getJsonObject(json, "mirror");
			mirrorX = JsonUtils.getBoolean(mirror, "x");
			mirrorY = JsonUtils.getBoolean(mirror, "y");

			JsonObject spellIconInset = JsonUtils.getJsonObject(json, "spell_icon_inset");
			spellIconInsetX = JsonUtils.getInt(spellIconInset, "x");
			spellIconInsetY = JsonUtils.getInt(spellIconInset, "y");
			
			JsonObject textInset = JsonUtils.getJsonObject(json, "text_inset");
			textInsetX = JsonUtils.getInt(textInset, "x");
			textInsetY = JsonUtils.getInt(textInset, "y");
			
			JsonObject cascadeOffset = JsonUtils.getJsonObject(json, "spell_cascade_offset");
			cascadeOffsetX = JsonUtils.getInt(cascadeOffset, "x");
			cascadeOffsetY = JsonUtils.getInt(cascadeOffset, "y");

			JsonObject cooldownBar = JsonUtils.getJsonObject(json, "cooldown_bar");
			cooldownBarX = JsonUtils.getInt(cooldownBar, "x");
			cooldownBarY = JsonUtils.getInt(cooldownBar, "y");
			cooldownBarLength = JsonUtils.getInt(cooldownBar, "length");
			cooldownBarHeight = JsonUtils.getInt(cooldownBar, "height");
			
			JsonObject cooldownBarMirror = JsonUtils.getJsonObject(cooldownBar, "mirror");
			cooldownBarMirrorX = JsonUtils.getBoolean(cooldownBarMirror, "x");
			cooldownBarMirrorY = JsonUtils.getBoolean(cooldownBarMirror, "y");

			showCooldownWhenFull = JsonUtils.getBoolean(cooldownBar, "show_when_full");
			
		}
		
		// The idea of these methods is that everything in here relates only to the actual drawing of the HUD. In other
		// words, all processing of which spells to draw and so on is done outside of here. This means that the config
		// GUI can easily display its preview without having a player or wand stack object to query.
		
		/**
		 * Draws the background layer of this HUD skin at the given position with the given orientations, with the given
		 * spell icon and cooldown bar progress.
		 * 
		 * @param x The x-coordinate of the corner of the spell HUD. The bottom left corner <i>of the actual texture</i>
		 * will always be at this position unless mirrorX/Y is false, so for example if flipX is false and flipY is true,
		 * this will be the corner of the HUD that is closest to the top left corner of the screen.
		 * @param y The y-coordinate of the corner of the spell HUD; see above.
		 * @param flipX Whether to flip the HUD horizontally.
		 * @param flipY Whether to flip the HUD vertically.
		 * @param icon A {@code ResourceLocation} corresponding to the icon of the selected spell.
		 * @param cooldownBarProgress The fraction of the cooldown bar to draw; must be between 0 and 1 (inclusive).
		 * @param creativeMode True to draw the creative mode HUD, false for the survival mode version.
		 */
		public void drawBackground(int x, int y, boolean flipX, boolean flipY, ResourceLocation icon, float cooldownBarProgress, boolean creativeMode){
			
			// Moves the origin if the HUD does not mirror; neatens the rest of the code.
			if(flipX && !mirrorX) x -= width;
			if(flipY && !mirrorY) y += height;
			
			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
			GlStateManager.color(1, 1, 1);

			// Spell illustration - this is now done first so it is behind the HUD texture
			mc.renderEngine.bindTexture(icon);

			int x1 = flipX && mirrorX ? x - spellIconInsetX - SPELL_ICON_SIZE : x + spellIconInsetX;
			// y is upside-down so this is the other way round
			int y1 = flipY && mirrorY ? y + spellIconInsetY : y - spellIconInsetY - SPELL_ICON_SIZE;
			
			DrawingUtils.drawTexturedRect(x1, y1, 0, 0, SPELL_ICON_SIZE, SPELL_ICON_SIZE, SPELL_ICON_SIZE, SPELL_ICON_SIZE);

			// Background of spell hud
			mc.renderEngine.bindTexture(texture);
			
			x1 = flipX && mirrorX ? x - width : x;
			y1 = flipY && mirrorY ? y : y - height;
			// The 128 here is a uv value, not a dimension, and hence is left as a hardcoded number.
			// TODO: Since the HUD is wider than it is tall, perhaps the creative mode texture should be in the bottom half instead of the right half?
			DrawingUtils.drawTexturedFlippedRect(x1, y1, creativeMode ? 128 : 0, 0, width, height, 256, 256, flipX && mirrorX, flipY && mirrorY);

			// Cooldown bar
			if(!creativeMode && cooldownBarProgress > 0 && (showCooldownWhenFull || cooldownBarProgress < 1)){

				int l = (int)(cooldownBarProgress * cooldownBarLength);
				
				x1 = flipX && mirrorX ? x - cooldownBarX - (cooldownBarMirrorX ? l : cooldownBarLength) : x + cooldownBarX;
				y1 = flipY && mirrorY ? y + cooldownBarY : y - cooldownBarY - cooldownBarHeight;
				
				int u = cooldownBarX; // This doesn't change, even when cooldownBarMirrorX is true, because it should
				int v = height;		  // always start with the left-hand in the actual texture file
				
				DrawingUtils.drawTexturedFlippedRect(x1, y1, u, v, l, cooldownBarHeight, 256, 256, flipX && cooldownBarMirrorX, flipY && cooldownBarMirrorY);
			}

			GlStateManager.popMatrix();
			
			// Blend needs to be left enabled here because otherwise the hotbar becomes opaque
		}
		
		/**
		 * Draws the text layer of this HUD skin at the given position with the given orientations, with the given
		 * spell name strings.
		 * 
		 * @param x The x-coordinate of the corner of the spell HUD. The bottom left corner <i>of the actual texture</i>
		 * will always be at this position, so for example if flipX is false and flipY is true, this will be the corner
		 * of the HUD that is closest to the top left corner of the screen.
		 * @param y The y-coordinate of the corner of the spell HUD; see above.
		 * @param flipX Whether to flip the HUD horizontally.
		 * @param flipY Whether to flip the HUD vertically.
		 * @param prevSpellName The name of the previous spell. This string will be drawn <i>directly</i> using the
		 * {@link MixedFontRenderer}; as such it should be supplied with formatting codes and # characters already
		 * appended.
		 * @param spellName The name of the currently selected spell; see above.
		 * @param nextSpellName The name of the next spell; see above.
		 * @param animationProgress The progress of the spell switching animation, as a fraction between 0 and 1
		 * (inclusive). Positive values indicate switching forwards, negative values indicate switching backwards, and
		 * a value of zero indicates that the spell is not currently being switched.
		 */
		public void drawText(int x, int y, boolean flipX, boolean flipY, String prevSpellName, String spellName, String nextSpellName, float animationProgress){
			
			// Moves the origin if the HUD does not mirror; neatens the rest of the code.
			if(flipX && !mirrorX) x -= width;
			if(flipY && !mirrorY) y += height;
			
			FontRenderer font = ClientProxy.mixedFontRenderer; // On this occasion we're client-side so this is OK
			
			// Position of the selected spell name in normal display, also used for interpolation when animating
			int x1 = flipX && mirrorX ? x - width : x + textInsetX;
			// The text is an odd number of pixels high so we need to subtract an extra 1 when not flipped
			int y1 = flipY && mirrorY ? y + textInsetY - font.FONT_HEIGHT/2 + 2 : y - textInsetY - font.FONT_HEIGHT/2 - 1;
			
			int maxWidth = width - textInsetX; // Maximum width of the text
			
			if(animationProgress == 0){ // Normal display
				
				float xPrev = flipX && mirrorX ? x - width : x + textInsetX - (flipY ? -1 : 1) * cascadeOffsetX;
				float xNext = flipX && mirrorX ? x - width : x + textInsetX + (flipY ? -1 : 1) * cascadeOffsetX;
				// Don't ask me why adding 1 to this makes it look more even, it just does!
				float yPrev = y1 - (cascadeOffsetY + 1); // No need to account for flipY because previous is always above.
				float yNext = y1 + cascadeOffsetY; // No need to account for flipY because next is always below.
				float maxWidthPrev = maxWidth + (flipY ? -1 : 1) * cascadeOffsetX;
				float maxWidthNext = maxWidth - (flipY ? -1 : 1) * cascadeOffsetX;
				int nextPrevClr = DrawingUtils.makeTranslucent(0xffffff, SPELL_NAME_OPACITY);
				
				DrawingUtils.drawScaledStringToWidth(font, prevSpellName, xPrev, yPrev, SPELL_NAME_SCALE, nextPrevClr, maxWidthPrev, true, flipX && mirrorX);
				DrawingUtils.drawScaledStringToWidth(font, spellName, x1, y1, 1, 0xffffffff, maxWidth, true, flipX && mirrorX);
				DrawingUtils.drawScaledStringToWidth(font, nextSpellName, xNext, yNext, SPELL_NAME_SCALE, nextPrevClr, maxWidthNext, true, flipX && mirrorX);
				
			}else{ // Switching spells
				
				boolean reverse = animationProgress < 0;
				if(reverse) animationProgress = 1 - Math.abs(animationProgress); // Simplest way of reversing the animation
				
				float xPrev = flipX && mirrorX ? x - width : x + textInsetX - (flipY ? -1 : 1) * cascadeOffsetX * animationProgress;
				float xNext = flipX && mirrorX ? x - width : x + textInsetX + (flipY ? -1 : 1) * cascadeOffsetX * (1 - animationProgress);
				float yPrev = y1 - (cascadeOffsetY + 1) * animationProgress; // No need to account for flipY because previous is always above.
				float yNext = y1 + cascadeOffsetY * (1 - animationProgress); // No need to account for flipY because next is always below.
				float maxWidthPrev = maxWidth + (flipY ? -1 : 1) * cascadeOffsetX * animationProgress;
				float maxWidthNext = maxWidth - (flipY ? -1 : 1) * cascadeOffsetX * (1 - animationProgress);
				float scalePrev = SPELL_NAME_SCALE + (1 - SPELL_NAME_SCALE) * (1 - animationProgress);
				float scaleNext = SPELL_NAME_SCALE + (1 - SPELL_NAME_SCALE) * (animationProgress);
				int clrPrev = DrawingUtils.makeTranslucent(0xffffff, SPELL_NAME_OPACITY + (1 - SPELL_NAME_OPACITY) * (1 - animationProgress));
				int clrNext = DrawingUtils.makeTranslucent(0xffffff, SPELL_NAME_OPACITY + (1 - SPELL_NAME_OPACITY) * animationProgress);
				
				if(reverse){ // Switching to previous spell
					
					// Only renders the next spell and the current one
					DrawingUtils.drawScaledStringToWidth(font, spellName, xPrev, yPrev, scalePrev, clrPrev, maxWidthPrev, true, flipX && mirrorX);
					DrawingUtils.drawScaledStringToWidth(font, nextSpellName, xNext, yNext, scaleNext, clrNext, maxWidthNext, true, flipX && mirrorX);
					
				}else{ // Switching to next spell
					
					// Only renders the previous spell and the current one
					DrawingUtils.drawScaledStringToWidth(font, prevSpellName, xPrev, yPrev, scalePrev, clrPrev, maxWidthPrev, true, flipX && mirrorX);
					DrawingUtils.drawScaledStringToWidth(font, spellName, xNext, yNext, scaleNext, clrNext, maxWidthNext, true, flipX && mirrorX);
					
				}
			}
		}
		
	}

}
