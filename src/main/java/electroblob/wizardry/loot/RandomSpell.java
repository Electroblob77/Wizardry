package electroblob.wizardry.loot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang3.ArrayUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.item.ItemScroll;
import electroblob.wizardry.item.ItemSpellBook;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;

/**
 * Loot function that allows spell books and scrolls to select a random spell based on the standard weighting.
 * Automatically discounts continuous spells when the item in question is a scroll. Can be used as-is with no
 * parameters, but several optional parameters are available for those wishing to customise further:
 * <p>
 * - <b>spells</b>: A list of spells to choose from. Defaults to all enabled spells.<br>
 * - <b>ignore_weighting</b>: true to ignore the standard weighting and just pick a completely random spell. Defaults to
 * false.<br>
 * - <b>tiers</b>: A list of tiers to choose from. Defaults to all tiers.<br>
 * - <b>elements</b>: A list of elements to choose from. Defaults to all elements.
 * <p>
 * This class is effectively a loot table-friendly replacement for the standard weighting method in WizardryUtilities
 * which was the basis of all the old loot systems (not counting wizard trades, which were - and still are - completely
 * separate).
 * <p>
 * Since spells are stored as metadata, this <i>could</i> be done by having an entry for each tier and letting it pick a
 * random spell from that tier by setting the metadata to a random value in the range of values that correspond to that
 * tier. However, this creates a two-fold problem: firstly, not all spells are in tier order, and secondly, if spells
 * are added via addon mods the entries would have to be updated manually with the new numbers.
 * <p>
 * (This reasoning is similar to that for the enchant_randomly function in vanilla Minecraft, since you can specify NBT
 * data in loot tables, but using NBT in this way would be incredibly verbose and inflexible, hence the loot function.)
 * 
 * @see WizardryUtilities#getStandardWeightedRandomSpellId(Random, boolean)
 * @author Electroblob
 * @since Wizardry 1.2
 */
/* You could even use the yet more long-winded method of adding each spell as an individual entry, which would be
 * stupidly long but would allow precise control over each individual spell... I'll leave that for pack makers to do if
 * they have the patience! */
public class RandomSpell extends LootFunction {

	private final List<Spell> spells;
	private final boolean ignoreWeighting;
	private final List<Tier> tiers;
	private final List<Element> elements;

	protected RandomSpell(LootCondition[] conditions, List<Spell> spells, boolean ignoreWeighting, List<Tier> tiers,
			List<Element> elements){
		super(conditions);
		this.spells = spells;
		this.ignoreWeighting = ignoreWeighting;
		this.tiers = tiers;
		this.elements = elements;
	}

	@Override
	public ItemStack apply(ItemStack stack, Random random, LootContext context){

		if(!(stack.getItem() instanceof ItemSpellBook) && !(stack.getItem() instanceof ItemScroll)) Wizardry.logger
				.warn("Applying the random_spell loot function to an item that isn't a spell book or scroll.");

		Tier tier;
		Element element;

		if(ignoreWeighting){
			if(tiers == null || tiers.isEmpty()){
				tier = Tier.values()[random.nextInt(Tier.values().length)];
			}else{
				tier = tiers.get(random.nextInt(tiers.size()));
			}
		}else{
			if(tiers == null || tiers.isEmpty()){
				tier = Tier.getWeightedRandomTier(random);
			}else{
				tier = Tier.getWeightedRandomTier(random, tiers.toArray(new Tier[0]));
			}
		}

		// Elements aren't weighted
		if(elements == null || elements.isEmpty()){
			// Element can only be MAGIC if tier is BASIC
			if(tier == Tier.BASIC){
				element = Element.values()[random.nextInt(Element.values().length)];
			}else{
				element = ArrayUtils.removeElement(Element.values(), Element.MAGIC)[random
						.nextInt(Element.values().length)];
			}
		}else{
			// In theory, swapping this line to the commented one should make absolutely no difference.
			element = elements.get(random.nextInt(tiers.size()));
			// element = null;
		}

		// Here's a thought: does randomly selecting the element beforehand (as opposed to leaving it null and letting
		// the spell randomiser use any element) change the overall outcome at all?
		List<Spell> spellsList = Spell.getSpells(new Spell.TierElementFilter(tier, element));
		if(stack.getItem() instanceof ItemScroll) spellsList.retainAll(Spell.getSpells(Spell.nonContinuousSpells));

		// Ensures the tier chosen actually has spells in it, and if not uses BASIC instead. BASIC always has at least
		// the NONE spell since this spell cannot be disabled.
		// NOTE: Commented out for now because it will interfere with the ability to specify tiers and elements.
		// To be honest, I may as well just say that if you disable enough spells to make this important, you deserve
		// less loot!
		/* if(spells.isEmpty()){ spellsList = Spell.getSpells(new Spell.TierElementFilter(EnumTier.BASIC, null));
		 * if(stack.getItem() instanceof ItemScroll) spellsList.retainAll(Spell.getSpells(Spell.nonContinuousSpells));
		 * } */

		if(spells != null && !spells.isEmpty()){
			spellsList.retainAll(spells);
		}

		if(spellsList.isEmpty()){
			Wizardry.logger.warn("Tried to apply the random_spell loot function to an item, but no enabled spells"
					+ "matched the criteria specified. Substituting placeholder (metadata 0) item.");
			stack.setItemDamage(0);
		}else{
			stack.setItemDamage(spellsList.get(random.nextInt(spellsList.size())).id());
		}

		return stack;
	}

	public static class Serializer extends LootFunction.Serializer<RandomSpell> {

		public Serializer(){
			super(new ResourceLocation(Wizardry.MODID, "random_spell"), RandomSpell.class);
		}

		public void serialize(JsonObject object, RandomSpell function, JsonSerializationContext serializationContext){

			if(function.spells != null && !function.spells.isEmpty()){

				JsonArray jsonarray = new JsonArray();

				for(Spell spell : function.spells){
					jsonarray.add(new JsonPrimitive(spell.getRegistryName().toString()));
				}

				object.add("spells", jsonarray);
			}

			object.addProperty("ignore_weighting", function.ignoreWeighting);

			if(function.tiers != null && !function.tiers.isEmpty()){

				JsonArray jsonarray = new JsonArray();

				for(Tier tier : function.tiers){
					jsonarray.add(new JsonPrimitive(tier.getUnlocalisedName()));
				}

				object.add("tiers", jsonarray);
			}

			if(function.elements != null && !function.elements.isEmpty()){

				JsonArray jsonarray = new JsonArray();

				for(Element element : function.elements){
					jsonarray.add(new JsonPrimitive(element.getUnlocalisedName()));
				}

				object.add("elements", jsonarray);
			}
		}

		public RandomSpell deserialize(JsonObject object, JsonDeserializationContext deserializationContext,
				LootCondition[] conditions){

			List<Spell> spells = null;
			List<Tier> tiers = null;
			List<Element> elements = null;

			if(object.has("spells")){

				// Vanilla Minecraft calls Lists.<T>newArrayList() here, which is completely pointless.
				spells = new ArrayList<Spell>();

				// Importantly, it is necessary to specify a default (the new JsonArray) here because otherwise the
				// parameter will be mandatory, and the game will crash if it isn't present.
				for(JsonElement element : JsonUtils.getJsonArray(object, "spells", new JsonArray())){

					String string = JsonUtils.getString(element, "spell");

					Spell spell = Spell.get(string);

					if(spell == null){
						throw new JsonSyntaxException("Unknown spell \'" + string + "\'");
					}

					spells.add(spell);
				}
			}

			boolean ignoreWeighting = JsonUtils.getBoolean(object, "ignore_weighting", false);

			if(object.has("tiers")){

				// Vanilla Minecraft calls Lists.<T>newArrayList() here, which is completely pointless.
				tiers = new ArrayList<Tier>();

				jsonarray: for(JsonElement element : JsonUtils.getJsonArray(object, "tiers", new JsonArray())){

					String string = JsonUtils.getString(element, "tier");

					// IDEA: If it is necessary to get tiers by name elsewhere in the future, move this to EnumTier.
					for(Tier tier : Tier.values()){
						if(tier.getUnlocalisedName().equals(string)){
							tiers.add(tier);
							continue jsonarray;
						}
					}
					// If the string does not match any of the tiers, throws an exception.
					throw new JsonSyntaxException("Unknown tier \'" + string + "\'");
				}
			}

			if(object.has("elements")){

				// Vanilla Minecraft calls Lists.<T>newArrayList() here, which is completely pointless.
				elements = new ArrayList<Element>();

				jsonarray: for(JsonElement jelement : JsonUtils.getJsonArray(object, "elements", new JsonArray())){

					String string = JsonUtils.getString(jelement, "element");

					// IDEA: If it is necessary to get elements by name elsewhere in the future, move this to
					// EnumElement.
					for(Element element : Element.values()){
						if(element.getUnlocalisedName().equals(string)){
							elements.add(element);
							continue jsonarray;
						}
					}
					// If the string does not match any of the elements, throws an exception.
					throw new JsonSyntaxException("Unknown element \'" + string + "\'");
				}
			}

			return new RandomSpell(conditions, spells, ignoreWeighting, tiers, elements);
		}
	}

}
