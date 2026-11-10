package electroblob.wizardry.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import electroblob.wizardry.Wizardry;
import net.minecraft.advancements.Advancement;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.Random;

/**
 * Loot condition that biases artefact loot entries away from artefacts the player has already obtained, so that
 * players do not have to rely entirely on luck to find every artefact (see the {@code random_spell} loot function's
 * {@code undiscovered_bias} parameter, which does the same thing for spell books and scrolls). Unlike spells,
 * artefacts don't have a central registry or per-player discovery tracking, so this piggybacks on the
 * {@code ebwizardry:all_artefacts} advancement's criteria (one per artefact) to work out whether the given artefact
 * has already been obtained.
 * <p></p>
 * This is a condition rather than a loot function because, unlike spell books and scrolls, each artefact is already
 * a distinct item occupying its own loot table entry; this condition is therefore applied to each individual
 * artefact entry and references that same artefact explicitly, rather than picking from a pool of candidates.
 * <p></p>
 * Addon-injected artefact entries are unaffected unless they add this condition themselves, since it has to be
 * added per-entry.
 *
 * @author WinDanesz
 * @since Wizardry 4.3.20 (fork)
 */
public class UndiscoveredArtefactCondition implements LootCondition {

	private final String artefact;
	private final float bias;

	public UndiscoveredArtefactCondition(String artefact, float bias){
		this.artefact = artefact;
		this.bias = bias;
	}

	@Override
	public boolean testCondition(Random rand, LootContext context){

		if(bias <= 0) return true;

		// This method is badly-named, loot chests pass a player through too, not just mobs
		EntityPlayer player = (EntityPlayer)context.getKillerPlayer();

		if(!(player instanceof EntityPlayerMP)) return true;

		Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(artefact));

		if(item == null){
			Wizardry.logger.warn("Couldn't locate artefact '{}' referenced in an undiscovered_bias loot condition!", artefact);
			return true;
		}

		if(!hasBeenObtained((EntityPlayerMP)player, item)) return true;

		// Already obtained: reject with probability equal to the bias, to favour other (undiscovered) entries
		// in the same pool without ever guaranteeing this entry can't come up at all
		return rand.nextFloat() >= bias;
	}

	/** Returns whether the given player has already obtained the given artefact, using the per-artefact criteria
	 * of the {@code ebwizardry:all_artefacts} advancement as a (imperfect but low-maintenance) stand-in for proper
	 * discovery tracking. Artefacts with no corresponding criterion (e.g. from addons that haven't registered one)
	 * are treated as not yet obtained, so they're never penalised by this condition. */
	private static boolean hasBeenObtained(EntityPlayerMP player, Item item){

		Advancement advancement = player.getServer().getAdvancementManager()
				.getAdvancement(new ResourceLocation(Wizardry.MODID, "all_artefacts"));

		if(advancement == null) return false;

		String criterion = item.getRegistryName().getPath();

		if(!advancement.getCriteria().containsKey(criterion)) return false;

		return player.getAdvancements().getProgress(advancement).getCriterionProgress(criterion).isObtained();
	}

	public static class Serializer extends LootCondition.Serializer<UndiscoveredArtefactCondition> {

		public Serializer(){
			super(new ResourceLocation(Wizardry.MODID, "undiscovered_bias"), UndiscoveredArtefactCondition.class);
		}

		@Override
		public void serialize(JsonObject object, UndiscoveredArtefactCondition condition, JsonSerializationContext serializationContext){
			object.addProperty("artefact", condition.artefact);
			object.addProperty("bias", condition.bias);
		}

		@Override
		public UndiscoveredArtefactCondition deserialize(JsonObject object, JsonDeserializationContext deserializationContext){
			String artefact = JsonUtils.getString(object, "artefact");
			float bias = JsonUtils.getFloat(object, "bias", 0);
			return new UndiscoveredArtefactCondition(artefact, bias);
		}
	}

}
