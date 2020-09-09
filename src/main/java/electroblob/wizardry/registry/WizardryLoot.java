package electroblob.wizardry.registry;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.loot.RandomSpell;
import electroblob.wizardry.loot.WizardSpell;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.functions.LootFunctionManager;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.Arrays;

/**
 * Class responsible for registering wizardry's loot functions and loot tables. Also handles loot injection and the
 * standard weighting.
 * 
 * @author Electroblob
 * @since Wizardry 1.0
 */
@Mod.EventBusSubscriber
public final class WizardryLoot {

	//public static final String FROM_SPAWNER_NBT_FLAG = "fromSpawner";

	private WizardryLoot(){} // No instances!

	/** Called from the preInit method in the main mod class to register the custom dungeon loot. */
	public static void register(){

		/* Loot tables work as follows: Minecraft goes through each pool in turn. For each pool, it does a certain
		 * number of rolls, which can either be set to always be one number or a random number from a range. Each roll,
		 * it generates one stack of a single random entry in that pool, weighted according to the weights of the
		 * entries. Functions allow properties of that stack (stack size, damage, nbt) to be set, and even allow it to
		 * be replaced dynamically with a completely different item (though there's very little point in doing that as
		 * it could be achieved just as easily with more entries, which makes me think it would be bad practice). You
		 * can also use conditions to control whether an entry or pool is used at all, which is mostly for mob drops
		 * under specific conditions, but one of them is simply a random chance, meaning you could use it to make a pool
		 * that only gets rolled sometimes. All in all, this can get rather confusing, because stackable items can have
		 * 5 stages of randomness applied to them at once: a random chance for the pool, a random number of rolls for
		 * the pool, the weighted random chance of choosing that particular entry, a random chance for that entry, and a
		 * random stack size, and that's before you take functions into account.
		 * 
		 * ...oh, and entries can be entire loot tables in themselves, allowing for potentially infinite levels of
		 * randomness. Yeah. */
 
		// Always registers the loot tables, but only injects the additions into vanilla if the appropriate option is
		// enabled in the config (see WizardryEventHandler).
		LootFunctionManager.registerFunction(new RandomSpell.Serializer());
		LootFunctionManager.registerFunction(new WizardSpell.Serializer());
		LootTableList.register(new ResourceLocation(Wizardry.MODID, "chests/wizard_tower"));
		LootTableList.register(new ResourceLocation(Wizardry.MODID, "chests/obelisk"));
		LootTableList.register(new ResourceLocation(Wizardry.MODID, "chests/shrine"));
		LootTableList.register(new ResourceLocation(Wizardry.MODID, "chests/dungeon_additions"));
		LootTableList.register(new ResourceLocation(Wizardry.MODID, "chests/jungle_dispenser_additions"));
		LootTableList.register(new ResourceLocation(Wizardry.MODID, "subsets/elemental_crystals"));
		LootTableList.register(new ResourceLocation(Wizardry.MODID, "subsets/wizard_armour"));
		LootTableList.register(new ResourceLocation(Wizardry.MODID, "subsets/arcane_tomes"));
		LootTableList.register(new ResourceLocation(Wizardry.MODID, "subsets/wand_upgrades"));
		LootTableList.register(new ResourceLocation(Wizardry.MODID, "subsets/uncommon_artefacts"));
		LootTableList.register(new ResourceLocation(Wizardry.MODID, "subsets/rare_artefacts"));
		LootTableList.register(new ResourceLocation(Wizardry.MODID, "subsets/epic_artefacts"));
		LootTableList.register(new ResourceLocation(Wizardry.MODID, "entities/evil_wizard"));
		LootTableList.register(new ResourceLocation(Wizardry.MODID, "entities/mob_additions"));
		LootTableList.register(new ResourceLocation(Wizardry.MODID, "gameplay/fishing/junk_additions"));
		LootTableList.register(new ResourceLocation(Wizardry.MODID, "gameplay/fishing/treasure_additions"));

	}

	@SubscribeEvent
	public static void onLootTableLoadEvent(LootTableLoadEvent event){
		// General dungeon loot
		if(Arrays.asList(Wizardry.settings.lootInjectionLocations).contains(event.getName())){
			event.getTable().addPool(getAdditive(Wizardry.MODID + ":chests/dungeon_additions", Wizardry.MODID + "_additional_dungeon_loot"));
		}
		// Jungle temple dispensers
		if(event.getName().toString().matches("minecraft:chests/jungle_temple_dispenser")){
			event.getTable().addPool(getAdditive(Wizardry.MODID + ":chests/jungle_dispenser_additions", Wizardry.MODID + "_additional_dispenser_loot"));
		}
		// Mob drops
		// Let's hope mods will play nice and store their entity loot tables under 'entities' or 'entity'
		// If not, packmakers will have to sort it out themselves using the whitelist/blacklist
		if(Arrays.asList(Wizardry.settings.mobLootTableWhitelist).contains(event.getName())){
			event.getTable().addPool(getAdditive(Wizardry.MODID + ":entities/mob_additions", Wizardry.MODID + "_additional_mob_drops"));

		}else if(!Arrays.asList(Wizardry.settings.mobLootTableBlacklist).contains(event.getName())
				&& event.getName().getPath().contains("entities") || event.getName().getPath().contains("entity")){
			// Get the filename of the loot table json, for well-behaved mods this will be the entity name
			String[] split = event.getName().getPath().split("/");
			String entityName = split[split.length - 1];

			EntityEntry entry = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(entityName));
			if(entry == null) return; // If this is true it didn't work :(
			Class entityClass = entry.getEntityClass();

			if(EnumCreatureType.MONSTER.getCreatureClass().isAssignableFrom(entityClass)){
				event.getTable().addPool(getAdditive(Wizardry.MODID + ":entities/mob_additions", Wizardry.MODID + "_additional_mob_drops"));
			}
		}
		// Fishing loot
		// This works slightly differently in that it modifies the existing pools rather than adding new ones
		// This is because you are supposed to only catch one item at a time!
		if(event.getName().toString().matches("minecraft:gameplay/fishing/junk")){
			// The first (and in this case, only) vanilla loot pool is named "main"
			event.getTable().getPool("main").addEntry(getAdditiveEntry(Wizardry.MODID + ":gameplay/fishing/junk_additions", 3));
		}else if(event.getName().toString().matches("minecraft:gameplay/fishing/treasure")){
			event.getTable().getPool("main").addEntry(getAdditiveEntry(Wizardry.MODID + ":gameplay/fishing/treasure_additions", 1));
		}
	}

	private static LootPool getAdditive(String entryName, String poolName){
		return new LootPool(new LootEntry[]{getAdditiveEntry(entryName, 1)}, new LootCondition[0],
				new RandomValueRange(1), new RandomValueRange(0, 1), Wizardry.MODID + "_" + poolName);
	}

	private static LootEntryTable getAdditiveEntry(String name, int weight){
		return new LootEntryTable(new ResourceLocation(name), weight, 0, new LootCondition[0],
				Wizardry.MODID + "_additive_entry");
	}

//	@SubscribeEvent
//	public static void onLivingSpawnEvent(LivingSpawnEvent.SpecialSpawn event){
//		if(event.getSpawner() != null) event.getEntityLiving().getEntityData().setBoolean(FROM_SPAWNER_NBT_FLAG, true);
//	}

}
