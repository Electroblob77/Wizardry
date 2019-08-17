package electroblob.wizardry.advancement;

import com.google.common.collect.Streams;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.spell.Spell;
import net.minecraft.util.JsonUtils;

import javax.annotation.Nullable;
import java.util.Arrays;

/** Predicate used by advancement triggers to match spells. */
public class SpellPredicate {

	public static final SpellPredicate ANY = new SpellPredicate();
	private final Spell spell;
	private final Tier[] tiers;
	private final Element[] elements;

	public SpellPredicate(){
		this.spell = null;
		this.tiers = Tier.values();
		this.elements = Element.values();
	}

	public SpellPredicate(@Nullable Spell spell, Tier[] tiers, Element[] elements){
		this.spell = spell;
		this.tiers = tiers;
		this.elements = elements;
	}

	public boolean test(Spell spell){

		if(this.spell != null && spell != this.spell){
			return false;
		}else if(!Arrays.asList(this.tiers).contains(spell.getTier())){
			return false;
		}else if(!Arrays.asList(this.elements).contains(spell.getElement())){
			return false;
		}

		return true;
	}

	public static SpellPredicate deserialize(@Nullable JsonElement element){

		if(element != null && !element.isJsonNull()){

			JsonObject jsonobject = JsonUtils.getJsonObject(element, "spell");

			Spell spell = null;

			if(jsonobject.has("spell")){

				String s = JsonUtils.getString(jsonobject, "spell");
				spell = Spell.get(s);

				if(spell == null){
					throw new JsonSyntaxException("Unknown spell id '" + s + "'");
				}
			}

			Tier[] tiers = Tier.values();

			if(jsonobject.has("tiers")){
				try{
					JsonArray array = JsonUtils.getJsonArray(jsonobject, "tiers");
					tiers = Streams.stream(array)
							.map(je -> Tier.fromName(JsonUtils.getString(je, "element of array tiers")))
							.toArray(Tier[]::new);
				}catch(IllegalArgumentException e){
					throw new JsonSyntaxException("Incorrect spell predicate value", e);
				}
			}

			Element[] elements = Element.values();

			if(jsonobject.has("elements")){
				try{
					JsonArray array = JsonUtils.getJsonArray(jsonobject, "elements");
					elements = Streams.stream(array)
							.map(je -> Element.fromName(JsonUtils.getString(je, "element of array elements")))
							.toArray(Element[]::new);
				}catch(IllegalArgumentException e){
					throw new JsonSyntaxException("Incorrect spell predicate value", e);
				}
			}

			return new SpellPredicate(spell, tiers, elements);

		}else{
			return ANY;
		}
	}

	public static SpellPredicate[] deserializeArray(@Nullable JsonElement element){

		if(element != null && !element.isJsonNull()){

			JsonArray jsonarray = JsonUtils.getJsonArray(element, "spells");
			SpellPredicate[] predicates = new SpellPredicate[jsonarray.size()];

			for(int i = 0; i < predicates.length; ++i){
				predicates[i] = deserialize(jsonarray.get(i));
			}

			return predicates;

		}else{
			return new SpellPredicate[0];
		}
	}
}