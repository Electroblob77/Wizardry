package electroblob.wizardry.client.gui.handbook;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import electroblob.wizardry.client.DrawingUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class Image {

	// Final fields are mandatory, the rest are optional
	private final ResourceLocation location;
	private final int width, height;
	private int textureWidth, textureHeight;
	private int u = 0, v = 0;
	private String caption = "";
	private boolean border = true;
	// Derived fields, not specifically defined in JSON
	private final Set<int[]> instances = new HashSet<>();

	private static final int CAPTION_OFFSET = 4;

	private static final int TEXTURE_INSET_X = 180;
	private static final int BORDER = 1;

	private Image(ResourceLocation location, int width, int height){
		this.location = location;
		this.width = width;
		this.height = height;
	}

	/** Returns the width of the image. */
	int getWidth(){
		return width;
	}

	/** Returns the total height of the image, including caption if it has one. */
	int getHeight(FontRenderer font){
		return caption.isEmpty() ? height : height + CAPTION_OFFSET + font.FONT_HEIGHT;
	}

	/**
	 * Adds an instance of this image to the list.
	 *
	 * @param page The index of the <b>single</b> page this image is on.
	 * @param x    The x-coordinate of the top-left corner of the image, <i>relative</i> to the top-left corner of the GUI.
	 * @param y    The y-coordinate of the top-left corner of the image, <i>relative</i> to the top-left corner of the GUI.
	 */
	void addInstance(int page, int x, int y){
		instances.add(new int[]{page, x, y});
	}

	/** Removes all instances of this image from the list. */
	void clearInstances(){
		instances.clear();
	}

	/**
	 * Draws all instances of this image that are located on the given double-page spread.
	 *
	 * @param font       The font renderer object.
	 * @param doublePage The double-page index of the page to be drawn.
	 * @param left       The x coordinate of the left side of the GUI.
	 * @param top        The y coordinate of the top of the GUI.
	 */
	void draw(FontRenderer font, int doublePage, int left, int top){
		// Images
		for(int[] instance : instances){
			if(GuiWizardHandbook.singleToDoublePage(instance[0]) == doublePage){
				Minecraft.getMinecraft().renderEngine.bindTexture(location);
				GlStateManager.color(1, 1, 1, 1);
				DrawingUtils.drawTexturedRect(left + instance[1], top + instance[2], u, v, width, height, textureWidth, textureHeight);
				font.drawString("\u00A7o" + caption, left + instance[1] +  width/ 2 - font.getStringWidth(caption)/2,
						top + instance[2] + height + CAPTION_OFFSET, GuiWizardHandbook.colours.get("caption"));
			}
		}

		if(border){
			// Borders - do this after all the images are drawn so we only have to bind the handbook texture again once
			Minecraft.getMinecraft().renderEngine.bindTexture(GuiWizardHandbook.texture);
			GlStateManager.color(1, 1, 1, 1);
			for(int[] instance : instances){
				if(GuiWizardHandbook.singleToDoublePage(instance[0]) == doublePage){
					// Math.ceil accounts for odd-numbered image dimensions
					DrawingUtils.drawTexturedFlippedRect(left + instance[1] - BORDER, top + instance[2] - BORDER,
							TEXTURE_INSET_X, GuiWizardHandbook.GUI_HEIGHT, width / 2 + BORDER, height / 2 + BORDER,
							GuiWizardHandbook.TEXTURE_WIDTH, GuiWizardHandbook.TEXTURE_HEIGHT, false, false);
					DrawingUtils.drawTexturedFlippedRect(left + instance[1] + width / 2, top + instance[2] - BORDER,
							TEXTURE_INSET_X, GuiWizardHandbook.GUI_HEIGHT, MathHelper.ceil(width / 2f) + BORDER, height / 2 + BORDER,
							GuiWizardHandbook.TEXTURE_WIDTH, GuiWizardHandbook.TEXTURE_HEIGHT, true, false);
					DrawingUtils.drawTexturedFlippedRect(left + instance[1] - BORDER, top + instance[2] + height / 2,
							TEXTURE_INSET_X, GuiWizardHandbook.GUI_HEIGHT, width / 2 + BORDER, MathHelper.ceil(height / 2f) + BORDER,
							GuiWizardHandbook.TEXTURE_WIDTH, GuiWizardHandbook.TEXTURE_HEIGHT, false, true);
					DrawingUtils.drawTexturedFlippedRect(left + instance[1] + width / 2, top + instance[2] + height / 2,
							TEXTURE_INSET_X, GuiWizardHandbook.GUI_HEIGHT, MathHelper.ceil(width / 2f) + BORDER, MathHelper.ceil(height / 2f) + BORDER,
							GuiWizardHandbook.TEXTURE_WIDTH, GuiWizardHandbook.TEXTURE_HEIGHT, true, true);
				}
			}
		}
	}

	/**
	 * Parses the given JSON object and constructs a new {@code Image} from it, setting all the relevant fields
	 * and references.
	 *
	 * @param json A JSON object representing the image to be constructed. This must contain at least a "location"
	 *             string.
	 * @return The resulting {@code Image} object.
	 * @throws JsonSyntaxException if at any point the JSON object is found to be invalid.
	 */
	static Image fromJson(JsonObject json){

		Image image = new Image(new ResourceLocation(JsonUtils.getString(json, "location")),
				JsonUtils.getInt(json, "width"), JsonUtils.getInt(json, "height"));

		image.u = JsonUtils.getInt(json, "u", 0);
		image.v = JsonUtils.getInt(json, "v", 0);
		image.textureWidth = JsonUtils.getInt(json, "texture_width", image.width);
		image.textureHeight = JsonUtils.getInt(json, "texture_height", image.height);
		image.caption = JsonUtils.getString(json, "caption", "");
		image.border = JsonUtils.getBoolean(json, "border", true);

		return image;
	}

	static void populate(Map<String, Image> map, JsonObject json){

		JsonObject sectionsObject = JsonUtils.getJsonObject(json, "images");

		// Need to iterate over these since we don't know what they're called or how many there are
		for(Map.Entry<String, JsonElement> entry : sectionsObject.entrySet()){

			String key = entry.getKey(); // Find out what each element is called, this will be the sections map key

			Image image = fromJson(entry.getValue().getAsJsonObject());
			map.put(key, image);
		}
	}
}
