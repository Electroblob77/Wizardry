package electroblob.wizardry.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.entity.living.ISpellCaster;
import electroblob.wizardry.item.ItemScroll;
import electroblob.wizardry.item.ItemSpellBook;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.spell.Spell;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunction;

import java.util.List;
import java.util.Random;

/**
 * Loot function that allows spell books to select a random spell from the spells used by the ISpellCaster that dropped
 * them.
 * 
 * @author Electroblob
 * @since Wizardry 1.2
 */
public class WizardSpell extends LootFunction {

	protected WizardSpell(LootCondition[] conditions){
		super(conditions);
	}

	@Override
	public ItemStack apply(ItemStack stack, Random random, LootContext context){

		if(!(stack.getItem() instanceof ItemSpellBook) && !(stack.getItem() instanceof ItemScroll)) Wizardry.logger
				.warn("Applying the wizard_spell loot function to an item that isn't a spell book or scroll.");

		if(context.getLootedEntity() instanceof ISpellCaster){
			List<Spell> spells = ((ISpellCaster)context.getLootedEntity()).getSpells();
			spells.remove(Spells.magic_missile); // Can't drop magic missile
			spells.removeIf(s -> !s.applicableForItem(stack.getItem()));
			if(spells.isEmpty()){
				Wizardry.logger.warn("Tried to apply the wizard_spell loot function to an item, but none of the looted entity's spells were applicable for that item. This is probably a bug!");
			}else{
				stack.setItemDamage(spells.get(random.nextInt(spells.size())).metadata());
			}
		}else{
			Wizardry.logger.warn("Applying the wizard_spell loot function to an entity that isn't a spell caster.");
		}

		return stack;
	}

	public static class Serializer extends LootFunction.Serializer<WizardSpell> {

		public Serializer(){
			super(new ResourceLocation(Wizardry.MODID, "wizard_spell"), WizardSpell.class);
		}

		public void serialize(JsonObject object, WizardSpell function, JsonSerializationContext serializationContext){

		}

		public WizardSpell deserialize(JsonObject object, JsonDeserializationContext deserializationContext,
				LootCondition[] conditions){
			return new WizardSpell(conditions);
		}
	}

}
