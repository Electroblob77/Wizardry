package electroblob.wizardry.client.gui.handbook;

import com.google.common.collect.Streams;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import electroblob.wizardry.client.DrawingUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;

import java.util.*;

class CraftingRecipe {

	static final int BORDER = 7;
	static final int TEXTURE_INSET_X = 40, TEXTURE_INSET_Y = 190;
	static final int WIDTH = 121, HEIGHT = 66;

	// Final fields are mandatory, the rest are optional
	private final ResourceLocation[] locations;
	// Derived fields, not specifically defined in JSON
	private List<IRecipe> recipes;
	private final Set<int[]> instances = new HashSet<>();

	private CraftingRecipe(ResourceLocation[] locations){
		this.locations = locations;
	}

	/**
	 * Adds an instance of this recipe to the list.
	 *
	 * @param page The index of the <b>single</b> page this image is on.
	 * @param x    The x-coordinate of the top-left corner of the image, <i>relative</i> to the top-left corner of the GUI.
	 * @param y    The y-coordinate of the top-left corner of the image, <i>relative</i> to the top-left corner of the GUI.
	 */
	void addInstance(int page, int x, int y){
		instances.add(new int[]{page, x, y});
	}

	/** Removes all instances of this recipe from the list. */
	void clearInstances(){
		instances.clear();
	}

	/** Called on GUI open to load the actual recipe object from the registry. This cannot be done on JSON load since
	 * the recipes aren't necessarily loaded at that point. */
	void load(){

		recipes = new ArrayList<>(locations.length);

		for(ResourceLocation location : locations){

			IRecipe recipe = CraftingManager.getRecipe(location);
			if(recipe == null) throw new JsonSyntaxException("No such recipe: " + location);
			recipes.add(recipe);
		}
	}

	/**
	 * Draws all instances of this recipe that are located on the given double-page spread.
	 *
	 * @param font         The font renderer object.
	 * @param itemRenderer The item renderer object.
	 * @param doublePage   The double-page index of the page to be drawn.
	 * @param left         The x coordinate of the left side of the GUI.
	 * @param top          The y coordinate of the top of the GUI.
	 */
	void draw(FontRenderer font, RenderItem itemRenderer, int doublePage, int left, int top){

		int index = (int)(Minecraft.getSystemTime() % Integer.MAX_VALUE)/2000;

		for(int[] instance : instances){
			if(GuiWizardHandbook.singleToDoublePage(instance[0]) == doublePage){
				renderCraftingRecipe(font, itemRenderer, left + instance[1], top + instance[2], recipes.get(index % recipes.size()));
			}
		}
	}

	/**
	 * Draws the tooltips for all instances of this recipe that are located on the given double-page spread. This has to
	 * be done separately so that the tooltips are on top of everything else.
	 *
	 * @param itemRenderer The item renderer object.
	 * @param doublePage   The double-page index of the page to be drawn.
	 * @param left         The x coordinate of the left side of the GUI.
	 * @param top          The y coordinate of the top of the GUI.
	 */
	void drawTooltips(GuiWizardHandbook gui, FontRenderer font, RenderItem itemRenderer, int doublePage, int left, int top, int mouseX, int mouseY){

		int index = (int)(Minecraft.getSystemTime() % Integer.MAX_VALUE)/2000;

		for(int[] instance : instances){
			if(GuiWizardHandbook.singleToDoublePage(instance[0]) == doublePage){
				renderCraftingTooltips(gui, itemRenderer, left + instance[1], top + instance[2], mouseX, mouseY, recipes.get(index % recipes.size()));
			}
		}
	}

	/**
	 * Parses the given JSON object and constructs a new {@code Image} from it, setting all the relevant fields
	 * and references.
	 *
	 * @param json A JSON object representing the image to be constructed. This must contain at least a "locations"
	 *             string.
	 * @return The resulting {@code Image} object.
	 * @throws JsonSyntaxException if at any point the JSON object is found to be invalid.
	 */
	static CraftingRecipe fromJson(JsonObject json){

		ResourceLocation[] locations = Streams.stream(JsonUtils.getJsonArray(json, "locations"))
				.map(je -> new ResourceLocation(je.getAsString())).toArray(ResourceLocation[]::new);
		return new CraftingRecipe(locations);
	}

	static void populate(Map<String, CraftingRecipe> map, JsonObject json){

		JsonObject sectionsObject = JsonUtils.getJsonObject(json, "recipes");

		// Need to iterate over these since we don't know what they're called or how many there are
		for(Map.Entry<String, JsonElement> entry : sectionsObject.entrySet()){

			String key = entry.getKey(); // Find out what each element is called, this will be the sections map key

			CraftingRecipe recipe = fromJson(entry.getValue().getAsJsonObject());
			map.put(key, recipe);
		}
	}

	private static void renderCraftingRecipe(FontRenderer font, RenderItem itemRenderer, int x, int y, IRecipe recipe){

		ItemStack result = recipe.getRecipeOutput();

		GlStateManager.color(1, 1, 1, 1);
		Minecraft.getMinecraft().renderEngine.bindTexture(GuiWizardHandbook.texture);

		DrawingUtils.drawTexturedRect(x, y, TEXTURE_INSET_X, TEXTURE_INSET_Y, WIDTH, HEIGHT, GuiWizardHandbook.TEXTURE_WIDTH, GuiWizardHandbook.TEXTURE_HEIGHT);

		GlStateManager.pushMatrix();
		RenderHelper.enableGUIStandardItemLighting();
		GlStateManager.disableLighting();
		GlStateManager.enableRescaleNormal();
		GlStateManager.enableColorMaterial();
		itemRenderer.zLevel = 100.0F;

		int index = (int)(Minecraft.getSystemTime() % Integer.MAX_VALUE)/2000;

		int i = 0;

		for(Ingredient ingredient : recipe.getIngredients()){

			if(ingredient != Ingredient.EMPTY){
				ItemStack stack = ingredient.getMatchingStacks()[index % ingredient.getMatchingStacks().length];
				if(!stack.isEmpty()){
					itemRenderer.renderItemAndEffectIntoGUI(stack, x + BORDER + 18 * (i%3), y + BORDER + 18 * (i/3));
					itemRenderer.renderItemOverlays(font, stack, x + BORDER + 18 * (i%3), y + BORDER + 18 * (i/3));
				}
			}

			i++;
		}

		if(!result.isEmpty()){
			itemRenderer.renderItemAndEffectIntoGUI(result, x + BORDER + 86, y + BORDER + 18);
			itemRenderer.renderItemOverlays(font, result, x + BORDER + 86, y + BORDER + 18);
		}

		GlStateManager.popMatrix();
		GlStateManager.enableDepth();
		GlStateManager.disableColorMaterial();
		itemRenderer.zLevel = 0.0F;
		RenderHelper.disableStandardItemLighting();

	}

	private static void renderCraftingTooltips(GuiWizardHandbook gui, RenderItem itemRenderer, int x, int y, int mouseX, int mouseY, IRecipe recipe){

		ItemStack result = recipe.getRecipeOutput();

		GlStateManager.pushMatrix();
		RenderHelper.enableGUIStandardItemLighting();
		GlStateManager.disableLighting();
		GlStateManager.enableRescaleNormal();
		GlStateManager.enableColorMaterial();
		itemRenderer.zLevel = 0.0F;

		int index = (int)(Minecraft.getSystemTime() % Integer.MAX_VALUE)/2000;

		int i = 0;

		for(Ingredient ingredient : recipe.getIngredients()){

			if(ingredient != Ingredient.EMPTY){
				ItemStack stack = ingredient.getMatchingStacks()[index % ingredient.getMatchingStacks().length];
				if(!stack.isEmpty() && isPointInRegion(x + BORDER + 18 * (i%3), y + BORDER + 18 * (i/3), 16, 16, mouseX, mouseY)){
					gui.renderToolTip(stack, mouseX, mouseY);
				}
			}

			i++;
		}

		if(!result.isEmpty() && isPointInRegion(x + BORDER + 86, y + BORDER + 18, 16, 16, mouseX, mouseY)){
			gui.renderToolTip(result, mouseX, mouseY);
		}

		GlStateManager.popMatrix();
		GlStateManager.enableDepth();
		GlStateManager.disableColorMaterial();
		RenderHelper.disableStandardItemLighting();

	}

	private static boolean isPointInRegion(int left, int top, int width, int height, int mouseX, int mouseY){
		return mouseX >= left - 1 && mouseX < left + width + 1 && mouseY >= top - 1 && mouseY < top + height + 1;
	}

}
