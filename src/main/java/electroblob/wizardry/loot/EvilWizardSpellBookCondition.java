package electroblob.wizardry.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import electroblob.wizardry.Wizardry;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;

import java.util.Random;

/**
 * Loot condition that only passes if the evilWizardsDropSpellBooks config option is enabled, used to allow disabling
 * evil wizards' spell book drops independently of the generic mob loot injection (since evil wizards use their own
 * dedicated loot table rather than the mob loot whitelist/blacklist system).
 *
 * @author Electroblob
 * @since Wizardry 4.3.20
 */
public class EvilWizardSpellBookCondition implements LootCondition {

	@Override
	public boolean testCondition(Random rand, LootContext context){
		return Wizardry.settings.evilWizardsDropSpellBooks;
	}

	public static class Serializer extends LootCondition.Serializer<EvilWizardSpellBookCondition> {

		public Serializer(){
			super(new ResourceLocation(Wizardry.MODID, "evil_wizard_spell_books_enabled"), EvilWizardSpellBookCondition.class);
		}

		@Override
		public void serialize(JsonObject object, EvilWizardSpellBookCondition condition, JsonSerializationContext serializationContext){
			// No parameters to serialize
		}

		@Override
		public EvilWizardSpellBookCondition deserialize(JsonObject object, JsonDeserializationContext deserializationContext){
			return new EvilWizardSpellBookCondition();
		}
	}

}
