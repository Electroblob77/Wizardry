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
 * Loot condition that only passes if discovery mode is enabled, used to keep scrolls of identification out of loot
 * tables when the discoveryMode config option is disabled (since in that case they can't be discovered anyway and
 * would be useless/unobtainable by other means).
 *
 * @author Electroblob
 * @since Wizardry 4.3.20
 */
public class DiscoveryModeCondition implements LootCondition {

	@Override
	public boolean testCondition(Random rand, LootContext context){
		return Wizardry.settings.discoveryMode;
	}

	public static class Serializer extends LootCondition.Serializer<DiscoveryModeCondition> {

		public Serializer(){
			super(new ResourceLocation(Wizardry.MODID, "discovery_mode"), DiscoveryModeCondition.class);
		}

		@Override
		public void serialize(JsonObject object, DiscoveryModeCondition condition, JsonSerializationContext serializationContext){
			// No parameters to serialize
		}

		@Override
		public DiscoveryModeCondition deserialize(JsonObject object, JsonDeserializationContext deserializationContext){
			return new DiscoveryModeCondition();
		}
	}

}
