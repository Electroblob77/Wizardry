package electroblob.wizardry.registry;

import java.util.List;

import com.google.common.collect.Lists;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.EntityArc;
import electroblob.wizardry.entity.EntityMeteor;
import electroblob.wizardry.entity.EntityShield;
import electroblob.wizardry.entity.construct.EntityArrowRain;
import electroblob.wizardry.entity.construct.EntityBlackHole;
import electroblob.wizardry.entity.construct.EntityBlizzard;
import electroblob.wizardry.entity.construct.EntityBubble;
import electroblob.wizardry.entity.construct.EntityDecay;
import electroblob.wizardry.entity.construct.EntityEarthquake;
import electroblob.wizardry.entity.construct.EntityFireRing;
import electroblob.wizardry.entity.construct.EntityFireSigil;
import electroblob.wizardry.entity.construct.EntityForcefield;
import electroblob.wizardry.entity.construct.EntityFrostSigil;
import electroblob.wizardry.entity.construct.EntityHailstorm;
import electroblob.wizardry.entity.construct.EntityHammer;
import electroblob.wizardry.entity.construct.EntityHealAura;
import electroblob.wizardry.entity.construct.EntityIceSpike;
import electroblob.wizardry.entity.construct.EntityLightningPulse;
import electroblob.wizardry.entity.construct.EntityLightningSigil;
import electroblob.wizardry.entity.construct.EntityTornado;
import electroblob.wizardry.entity.living.EntityBlazeMinion;
import electroblob.wizardry.entity.living.EntityDecoy;
import electroblob.wizardry.entity.living.EntityEvilWizard;
import electroblob.wizardry.entity.living.EntityIceGiant;
import electroblob.wizardry.entity.living.EntityIceWraith;
import electroblob.wizardry.entity.living.EntityLightningWraith;
import electroblob.wizardry.entity.living.EntityMagicSlime;
import electroblob.wizardry.entity.living.EntityPhoenix;
import electroblob.wizardry.entity.living.EntityShadowWraith;
import electroblob.wizardry.entity.living.EntitySilverfishMinion;
import electroblob.wizardry.entity.living.EntitySkeletonMinion;
import electroblob.wizardry.entity.living.EntitySpiderMinion;
import electroblob.wizardry.entity.living.EntitySpiritHorse;
import electroblob.wizardry.entity.living.EntitySpiritWolf;
import electroblob.wizardry.entity.living.EntityStormElemental;
import electroblob.wizardry.entity.living.EntityWitherSkeletonMinion;
import electroblob.wizardry.entity.living.EntityWizard;
import electroblob.wizardry.entity.living.EntityZombieMinion;
import electroblob.wizardry.entity.projectile.EntityDarknessOrb;
import electroblob.wizardry.entity.projectile.EntityDart;
import electroblob.wizardry.entity.projectile.EntityFirebolt;
import electroblob.wizardry.entity.projectile.EntityFirebomb;
import electroblob.wizardry.entity.projectile.EntityForceArrow;
import electroblob.wizardry.entity.projectile.EntityForceOrb;
import electroblob.wizardry.entity.projectile.EntityIceCharge;
import electroblob.wizardry.entity.projectile.EntityIceLance;
import electroblob.wizardry.entity.projectile.EntityIceShard;
import electroblob.wizardry.entity.projectile.EntityLightningArrow;
import electroblob.wizardry.entity.projectile.EntityLightningDisc;
import electroblob.wizardry.entity.projectile.EntityMagicMissile;
import electroblob.wizardry.entity.projectile.EntityPoisonBomb;
import electroblob.wizardry.entity.projectile.EntitySmokeBomb;
import electroblob.wizardry.entity.projectile.EntitySpark;
import electroblob.wizardry.entity.projectile.EntitySparkBomb;
import electroblob.wizardry.entity.projectile.EntityThunderbolt;
import electroblob.wizardry.loot.RandomSpell;
import electroblob.wizardry.loot.WizardSpell;
import electroblob.wizardry.tileentity.TileEntityArcaneWorkbench;
import electroblob.wizardry.tileentity.TileEntityMagicLight;
import electroblob.wizardry.tileentity.TileEntityPlayerSave;
import electroblob.wizardry.tileentity.TileEntityStatue;
import electroblob.wizardry.tileentity.TileEntityTimer;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Biomes;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraft.world.storage.loot.functions.LootFunctionManager;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import net.minecraftforge.registries.IForgeRegistry;

/**
 * Class responsible for registering all the things that don't have (or need) instances: entities, loot tables, recipes,
 * etc.
 * 
 * @author Electroblob
 * @since Wizardry 1.0
 */
@Mod.EventBusSubscriber
public final class WizardryRegistry {

	// NOTE: In 1.12, recipes have a registry (they can still stay here though since we don't keep references to them)

	/** Called from the preInit method in the main mod class to register the custom dungeon loot. */
	public static void registerLoot(){

		/* Loot tables work as follows: Minecraft goes through each pool in turn. For each pool, it does a certain
		 * number or rolls, which can either be set to always be one number or a random number from a range. Each roll,
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
		 * randomness. Yeah.
		 * 
		 * Translating to the new system: ChestGenHooks.SOME_NAME -> loot table json file ??? -> loot pool (I don't
		 * think it was split up like this before) ChestGenHooks.addItem() -> entry in a loot pool Stack sizes in
		 * WeightedRandomChestContent -> set_count function for entries Weight in WeightedRandomChestContent -> weight
		 * of entries Custom WeightedRandomChestContent implementations -> custom loot functions, but only for
		 * complex/dynamic stuff - things that serve to allow the chance for a category of items (like armour) to be
		 * specified but still have a random chance for which exact item you get should be done with nested loot
		 * tables. */

		// Always registers the loot tables, but only injects the additions into vanilla if the appropriate option is
		// enabled in the config (see WizardryEventHandler).
		LootFunctionManager.registerFunction(new RandomSpell.Serializer());
		LootFunctionManager.registerFunction(new WizardSpell.Serializer());
		LootTableList.register(new ResourceLocation(Wizardry.MODID, "chests/wizard_tower"));
		LootTableList.register(new ResourceLocation(Wizardry.MODID, "chests/dungeon_additions"));
		LootTableList.register(new ResourceLocation(Wizardry.MODID, "subsets/novice_wands"));
		LootTableList.register(new ResourceLocation(Wizardry.MODID, "subsets/wizard_armour"));
		LootTableList.register(new ResourceLocation(Wizardry.MODID, "subsets/arcane_tomes"));
		LootTableList.register(new ResourceLocation(Wizardry.MODID, "subsets/wand_upgrades"));
		LootTableList.register(new ResourceLocation(Wizardry.MODID, "entities/evil_wizard"));
		// TODO: At the moment this is not used anywhere because I can't find a way to add it to all mobs.
		// LootTableList.register(new ResourceLocation(Wizardry.MODID, "entities/mob_additions"));

	}

	/** Called from the preInit method in the main mod class to register all the tile entities. */
	public static void registerTileEntities(){

		GameRegistry.registerTileEntity(TileEntityArcaneWorkbench.class, Wizardry.MODID + "ArcaneWorkbenchTileEntity");
		GameRegistry.registerTileEntity(TileEntityStatue.class, Wizardry.MODID + "PetrifiedStoneTileEntity");
		GameRegistry.registerTileEntity(TileEntityMagicLight.class, Wizardry.MODID + "MagicLightTileEntity");
		GameRegistry.registerTileEntity(TileEntityTimer.class, Wizardry.MODID + "TimerTileEntity");
		GameRegistry.registerTileEntity(TileEntityPlayerSave.class, Wizardry.MODID + "TileEntityPlayerSave");
	}

	/** Not actually the frequency at all; smaller numbers are more frequent. Vanilla uses 3 I think. */
	private static final int LIVING_UPDATE_INTERVAL = 3;
	/** Not actually the frequency at all; smaller numbers are more frequent. */
	private static final int PROJECTILE_UPDATE_INTERVAL = 10;

	/** Called from the preInit method in the main mod class to register all the entities. */
	public static void registerEntities(){

		int id = 0; // Incrementable index for the mod specific entity id.

		registerEntity(EntityZombieMinion.class, "zombie_minion", id++, 128, LIVING_UPDATE_INTERVAL, true);
		registerEntity(EntityMagicMissile.class, "magic_missile", id++, 128, PROJECTILE_UPDATE_INTERVAL, true);
		// TODO: This should be a particle
		registerEntity(EntityArc.class, "arc", id++, 128, PROJECTILE_UPDATE_INTERVAL, false);
		registerEntity(EntitySkeletonMinion.class, "skeleton_minion", id++, 128, LIVING_UPDATE_INTERVAL, true);
		registerEntity(EntitySparkBomb.class, "spark_bomb", id++, 128, PROJECTILE_UPDATE_INTERVAL, true);
		registerEntity(EntitySpiritWolf.class, "spirit_wolf", id++, 128, LIVING_UPDATE_INTERVAL, true);
		registerEntity(EntityIceShard.class, "ice_shard", id++, 128, PROJECTILE_UPDATE_INTERVAL, true);
		registerEntity(EntityBlazeMinion.class, "blaze_minion", id++, 128, LIVING_UPDATE_INTERVAL, true);
		registerEntity(EntityIceWraith.class, "ice_wraith", id++, 128, LIVING_UPDATE_INTERVAL, true);
		registerEntity(EntityLightningWraith.class, "lightning_wraith", id++, 128, LIVING_UPDATE_INTERVAL, true);
		registerEntity(EntityBlackHole.class, "black_hole", id++, 128, PROJECTILE_UPDATE_INTERVAL, false);
		registerEntity(EntityShield.class, "shield", id++, 128, 1, true);
		registerEntity(EntityMeteor.class, "meteor", id++, 128, 5, true);
		registerEntity(EntityBlizzard.class, "blizzard", id++, 128, PROJECTILE_UPDATE_INTERVAL, false);
		registerEntityAndEgg(EntityWizard.class, "wizard", id++, 128, LIVING_UPDATE_INTERVAL, true, 0x19295e, 0xee9312);
		registerEntity(EntityBubble.class, "bubble", id++, 128, 3, false);
		registerEntity(EntityTornado.class, "tornado", id++, 128, 1, false);
		registerEntity(EntityHammer.class, "lightning_hammer", id++, 128, 1, true);
		registerEntity(EntityFirebomb.class, "firebomb", id++, 128, PROJECTILE_UPDATE_INTERVAL, true);
		registerEntity(EntityForceOrb.class, "force_orb", id++, 128, PROJECTILE_UPDATE_INTERVAL, true);
		registerEntity(EntityArrowRain.class, "arrow_rain", id++, 128, PROJECTILE_UPDATE_INTERVAL, false);
		registerEntity(EntitySpark.class, "spark", id++, 128, PROJECTILE_UPDATE_INTERVAL, true);
		registerEntity(EntityShadowWraith.class, "shadow_wraith", id++, 128, PROJECTILE_UPDATE_INTERVAL, true);
		registerEntity(EntityDarknessOrb.class, "darkness_orb", id++, 128, PROJECTILE_UPDATE_INTERVAL, true);
		registerEntity(EntitySpiderMinion.class, "spider_minion", id++, 128, LIVING_UPDATE_INTERVAL, true);
		registerEntity(EntityHealAura.class, "healing_aura", id++, 128, PROJECTILE_UPDATE_INTERVAL, false);
		registerEntity(EntityFireSigil.class, "fire_sigil", id++, 128, PROJECTILE_UPDATE_INTERVAL, false);
		registerEntity(EntityFrostSigil.class, "frost_sigil", id++, 128, PROJECTILE_UPDATE_INTERVAL, false);
		registerEntity(EntityLightningSigil.class, "lightning_sigil", id++, 128, PROJECTILE_UPDATE_INTERVAL, false);
		registerEntity(EntityLightningArrow.class, "lightning_arrow", id++, 128, PROJECTILE_UPDATE_INTERVAL, true);
		registerEntity(EntityFirebolt.class, "firebolt", id++, 128, PROJECTILE_UPDATE_INTERVAL, true);
		registerEntity(EntityPoisonBomb.class, "poison_bomb", id++, 128, PROJECTILE_UPDATE_INTERVAL, true);
		registerEntity(EntityIceCharge.class, "ice_charge", id++, 128, PROJECTILE_UPDATE_INTERVAL, true);
		registerEntity(EntityForceArrow.class, "force_arrow", id++, 128, PROJECTILE_UPDATE_INTERVAL, true);
		registerEntity(EntityDart.class, "dart", id++, 128, PROJECTILE_UPDATE_INTERVAL, true);
		registerEntity(EntityMagicSlime.class, "magic_slime", id++, 128, LIVING_UPDATE_INTERVAL, true);
		registerEntity(EntityForcefield.class, "forcefield", id++, 128, PROJECTILE_UPDATE_INTERVAL, false);
		registerEntity(EntityFireRing.class, "ring_of_fire", id++, 128, PROJECTILE_UPDATE_INTERVAL, false);
		registerEntity(EntityLightningDisc.class, "lightning_disc", id++, 128, PROJECTILE_UPDATE_INTERVAL, true);
		registerEntity(EntityThunderbolt.class, "thunderbolt", id++, 128, PROJECTILE_UPDATE_INTERVAL, true);
		registerEntity(EntityIceGiant.class, "ice_giant", id++, 128, LIVING_UPDATE_INTERVAL, true);
		registerEntity(EntitySpiritHorse.class, "spirit_horse", id++, 128, LIVING_UPDATE_INTERVAL, true);
		registerEntity(EntityPhoenix.class, "phoenix", id++, 128, LIVING_UPDATE_INTERVAL, true);
		registerEntity(EntitySilverfishMinion.class, "silverfish_minion", id++, 128, LIVING_UPDATE_INTERVAL, true);
		registerEntity(EntityDecay.class, "decay", id++, 128, PROJECTILE_UPDATE_INTERVAL, false);
		registerEntity(EntityStormElemental.class, "storm_elemental", id++, 128, PROJECTILE_UPDATE_INTERVAL, true);
		registerEntity(EntityEarthquake.class, "earthquake", id++, 128, PROJECTILE_UPDATE_INTERVAL, false);
		registerEntity(EntityIceLance.class, "ice_lance", id++, 128, PROJECTILE_UPDATE_INTERVAL, true);
		registerEntity(EntityHailstorm.class, "hailstorm", id++, 128, PROJECTILE_UPDATE_INTERVAL, false);
		registerEntity(EntitySmokeBomb.class, "smoke_bomb", id++, 128, PROJECTILE_UPDATE_INTERVAL, true);
		registerEntityAndEgg(EntityEvilWizard.class, "evil_wizard", id++, 128, LIVING_UPDATE_INTERVAL, true, 0x290404, 0xee9312);
		registerEntity(EntityDecoy.class, "decoy", id++, 128, LIVING_UPDATE_INTERVAL, true);
		registerEntity(EntityIceSpike.class, "ice_spike", id++, 128, 1, true);
		// TODO: This should be a particle.
		registerEntity(EntityLightningPulse.class, "lightning_pulse", id++, 128, PROJECTILE_UPDATE_INTERVAL, false);
		registerEntity(EntityWitherSkeletonMinion.class, "wither_skeleton_minion", id++, 128, LIVING_UPDATE_INTERVAL, true);

		// TODO: May need fixing
		List<Biome> biomes = Lists.newArrayList();
		for (Biome biome : ForgeRegistries.BIOMES.getValuesCollection()) {
			biomes.add(biome);
		}
		biomes.remove(Biomes.MUSHROOM_ISLAND);
		biomes.remove(Biomes.MUSHROOM_ISLAND_SHORE);
		// For reference: 5, 1, 1 are the parameters for the witch in vanilla.
		EntityRegistry.addSpawn(EntityEvilWizard.class, 3, 1, 1, EnumCreatureType.MONSTER, biomes.toArray(new Biome[biomes.size()]));

	}
	
	/** Private helper method for registering entities; keeps things neater. For some reason, Forge 1.11.2 wants a
	 * ResourceLocation and a string name... probably because it's transitioning to the registry system. */
	private static void registerEntity(Class<? extends Entity> entityClass, String name, int id, int trackingRange, int updateFrequency, boolean sendsVelocityUpdates){
		ResourceLocation registryName = new ResourceLocation(Wizardry.MODID, name);
		EntityRegistry.registerModEntity(registryName, entityClass, registryName.toString(), id, Wizardry.instance, trackingRange, updateFrequency, sendsVelocityUpdates);
	}
	
	/** Private helper method for registering entities with eggs; keeps things neater. For some reason, Forge 1.11.2
	 * wants a ResourceLocation and a string name... probably because it's transitioning to the registry system. */
	private static void registerEntityAndEgg(Class<? extends Entity> entityClass, String name, int id, int trackingRange, int updateFrequency, boolean sendsVelocityUpdates, int eggColour, int spotColour){
		ResourceLocation registryName = new ResourceLocation(Wizardry.MODID, name);
		EntityRegistry.registerModEntity(registryName, entityClass, registryName.toString(), id, Wizardry.instance, trackingRange, updateFrequency, sendsVelocityUpdates);
		EntityRegistry.registerEgg(registryName, eggColour, spotColour);
	}

	/** Called from the init method in the main mod class to register all the recipes. */
	@SubscribeEvent
	public static void registerRecipes(RegistryEvent.Register<IRecipe> event){
		IForgeRegistry<IRecipe> registry = event.getRegistry();

		ItemStack magicCrystalStack = new ItemStack(WizardryItems.magic_crystal);
		ItemStack magicWandStack = new ItemStack(WizardryItems.magic_wand, 1, Tier.BASIC.maxCharge);
		ItemStack goldNuggetStack = new ItemStack(Items.GOLD_NUGGET);
		ItemStack stickStack = new ItemStack(Items.STICK);
		ItemStack bookStack = new ItemStack(Items.BOOK);
		ItemStack spellBookStack = new ItemStack(WizardryItems.spell_book, 1, Spells.magic_missile.id());
		Ingredient crystalFlowerStack = Ingredient.fromStacks(new ItemStack(WizardryBlocks.crystal_flower));
		ItemStack magicCrystalStack1 = new ItemStack(WizardryItems.magic_crystal, 2);
		ItemStack magicCrystalStack2 = new ItemStack(WizardryItems.magic_crystal, 9);
		Ingredient crystalBlockStack = Ingredient.fromStacks(new ItemStack(WizardryBlocks.crystal_block));
		ItemStack manaFlaskStack = new ItemStack(WizardryItems.mana_flask);
		Ingredient bottleStack = Ingredient.fromStacks(new ItemStack(Items.GLASS_BOTTLE));
		Ingredient gunpowderStack = Ingredient.fromStacks(new ItemStack(Items.GUNPOWDER));
		Ingredient blazePowderStack = Ingredient.fromStacks(new ItemStack(Items.BLAZE_POWDER));
		Ingredient spiderEyeStack = Ingredient.fromStacks(new ItemStack(Items.SPIDER_EYE));
		// Coal or charcoal is equally fine, hence the wildcard value
		Ingredient coalStack = Ingredient.fromStacks(new ItemStack(Items.COAL, 1, OreDictionary.WILDCARD_VALUE));
		ItemStack firebombStack = new ItemStack(WizardryItems.firebomb, 3);
		ItemStack poisonBombStack = new ItemStack(WizardryItems.poison_bomb, 3);
		ItemStack smokeBombStack = new ItemStack(WizardryItems.smoke_bomb, 3);
		ItemStack scrollStack = new ItemStack(WizardryItems.blank_scroll);
		Ingredient paperStack = Ingredient.fromStacks(new ItemStack(Items.PAPER));
		Ingredient stringStack = Ingredient.fromStacks(new ItemStack(Items.STRING));

		registry.register(new ShapedOreRecipe(null, magicWandStack, "  x", " y ", "z  ", 'x', magicCrystalStack, 'y', stickStack, 'z', goldNuggetStack).setRegistryName(new ResourceLocation(Wizardry.MODID, "recipes/magic_wand")));
		registry.register(new ShapedOreRecipe(null, spellBookStack, " x ", "xyx", " x ", 'x', magicCrystalStack, 'y', bookStack).setRegistryName(new ResourceLocation(Wizardry.MODID, "recipes/spellbook")));

		registry.register(new ShapelessOreRecipe(null, magicCrystalStack1, crystalFlowerStack).setRegistryName(new ResourceLocation(Wizardry.MODID, "recipes/magic_crystal_1")));
		registry.register(new ShapelessOreRecipe(null, magicCrystalStack2, crystalBlockStack).setRegistryName(new ResourceLocation(Wizardry.MODID, "recipes/magic_crystal_2")));

		if(Wizardry.settings.firebombIsCraftable) registry.register(new ShapelessOreRecipe(null, firebombStack, bottleStack, gunpowderStack, blazePowderStack, blazePowderStack).setRegistryName(new ResourceLocation(Wizardry.MODID, "recipes/fire_bomb")));
		if(Wizardry.settings.poisonBombIsCraftable) registry.register(new ShapelessOreRecipe(null, poisonBombStack, bottleStack, gunpowderStack, spiderEyeStack, spiderEyeStack).setRegistryName(new ResourceLocation(Wizardry.MODID, "recipes/poison_bomb")));
		if(Wizardry.settings.smokeBombIsCraftable) registry.register(new ShapelessOreRecipe(null, smokeBombStack, bottleStack, gunpowderStack, coalStack, coalStack).setRegistryName(new ResourceLocation(Wizardry.MODID, "recipes/smoke_bomb")));

		if(Wizardry.settings.useAlternateScrollRecipe){
			registry.register(new ShapelessOreRecipe(null, scrollStack, paperStack, stringStack, magicCrystalStack).setRegistryName(new ResourceLocation(Wizardry.MODID, "recipes/blank_scroll")));
		}else{
			registry.register(new ShapelessOreRecipe(null, scrollStack, paperStack, stringStack).setRegistryName(new ResourceLocation(Wizardry.MODID, "recipes/blank_scroll")));
		}

		// Mana flask recipes
		ItemStack miscWandStack;

		for(Element element : Element.values()){
			for(Tier tier : Tier.values()){
				miscWandStack = new ItemStack(WizardryUtilities.getWand(tier, element), 1, OreDictionary.WILDCARD_VALUE);
				registry.register(new ShapelessOreRecipe(null, miscWandStack, miscWandStack, manaFlaskStack).setRegistryName(new ResourceLocation(Wizardry.MODID, "recipes/flask_wand_" + element.getUnlocalisedName() + "_" + tier.getUnlocalisedName())));
			}
		}


		ItemStack miscArmourStack;

		for(Element element : Element.values()){
			for(EntityEquipmentSlot slot : WizardryUtilities.ARMOUR_SLOTS){
				miscArmourStack = new ItemStack(WizardryUtilities.getArmour(element, slot), 1, OreDictionary.WILDCARD_VALUE);
				registry.register(new ShapelessOreRecipe(null, miscArmourStack, miscArmourStack, manaFlaskStack).setRegistryName(new ResourceLocation(Wizardry.MODID, "recipes/flask_armour_" + element.getUnlocalisedName() + "_" + slot.getName())));
			}
		}
	}

}
