package electroblob.wizardry;

import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import electroblob.wizardry.entity.EntityArc;
import electroblob.wizardry.entity.EntityFallingGrass;
import electroblob.wizardry.entity.EntityMagicSlime;
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
import electroblob.wizardry.entity.living.EntityPhoenix;
import electroblob.wizardry.entity.living.EntityShadowWraith;
import electroblob.wizardry.entity.living.EntitySilverfishMinion;
import electroblob.wizardry.entity.living.EntitySkeletonMinion;
import electroblob.wizardry.entity.living.EntitySpiderMinion;
import electroblob.wizardry.entity.living.EntitySpiritHorse;
import electroblob.wizardry.entity.living.EntitySpiritWolf;
import electroblob.wizardry.entity.living.EntityStormElemental;
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
import electroblob.wizardry.spell.*;
import electroblob.wizardry.tileentity.TileEntityArcaneWorkbench;
import electroblob.wizardry.tileentity.TileEntityMagicLight;
import electroblob.wizardry.tileentity.TileEntityPlayerSave;
import electroblob.wizardry.tileentity.TileEntityStatue;
import electroblob.wizardry.tileentity.TileEntityTimer;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.WeightedRandomChestContent;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.ChestGenHooks;
import net.minecraftforge.oredict.OreDictionary;

public final class WizardryRegistry {

	// Usage note on ChestGenHooks: First number is minimum, second is maximum. Third is rarity, lower numbers are more rare [bread = 100].
	// i starts at 1 here so that 'none' does not spawn.
	// Earlier problem solved using clever forge hook - extend WeightedRandomChestContent and override generateChestContent.
	// Apparently this seems to be incompatible with roguelike dungeons (any others?), in that it generates the same spell book in
	// all of the chests that have a spell book in the same dungeon (usually, though it has been known to do different ones on
	// different floors). It also appears to be able to generate the 'none' book, which should be impossible... never mind.
	// I don't think that's my fault, or I would have seen a 'none' book in a vanilla dungeon by now.
	// Another curious observation: Recurrent complex's generators also seem to always generate the same book/wand/armour
	// until you restart the game and can generate the 'none' book as well.
	// My best guess as to what's happening is that some mods hook into the dungeon loot differently to others, in that
	// they take all the WeightedRandomChestContent objects once and pick from them each time rather than making new ones
	// for each chest - which is, in theory, a more efficient way of doing things and will speed up world gen a bit, but
	// does of course result in custom chest content implementations behaving strangely. The 'none' book thing remains a
	// mystery, however.
	
	/** Called from the preInit method in the main mod class to register the custom dungeon loot. */
	public static void registerLoot(){
		
		// The generate loot option only applies to vanilla chests, not wizard tower chests.
		if(Wizardry.generateLoot){
		
			String[] chestTypes = {
					ChestGenHooks.DUNGEON_CHEST,
					ChestGenHooks.PYRAMID_JUNGLE_CHEST,
					ChestGenHooks.PYRAMID_DESERT_CHEST,
					ChestGenHooks.MINESHAFT_CORRIDOR,
					ChestGenHooks.STRONGHOLD_CORRIDOR,
					ChestGenHooks.STRONGHOLD_CROSSING,
					ChestGenHooks.STRONGHOLD_LIBRARY
					};
			
			for(String chest : chestTypes){
				
				ChestGenHooks.addItem(chest, new WeightedRandomSpellBook(new ItemStack(Wizardry.spellBook), 1, 1, Wizardry.SPELL_BOOK_FREQUENCY));
		    	ChestGenHooks.addItem(chest, new WeightedRandomSpellBook(new ItemStack(Wizardry.magicWand), 1, 1, Wizardry.WAND_FREQUENCY));
		    	ChestGenHooks.addItem(chest, new WeightedRandomSpellBook(new ItemStack(Wizardry.wizardHat), 1, 1, Wizardry.ARMOUR_FREQUENCY));
				ChestGenHooks.addItem(chest, new WeightedRandomSpellBook(new ItemStack(Wizardry.scroll), 1, 1, Wizardry.SCROLL_FREQUENCY));
				ChestGenHooks.addItem(chest, new WeightedRandomSpellBook(new ItemStack(Wizardry.condenserUpgrade), 1, 1, Wizardry.WAND_UPGRADE_FREQUENCY));
				ChestGenHooks.addItem(chest, new WeightedRandomChestContent(new ItemStack(Wizardry.magicCrystal), 1, 4, Wizardry.CRYSTAL_FREQUENCY));
				ChestGenHooks.addItem(chest, new WeightedRandomChestContent(new ItemStack(Wizardry.arcaneTome, 1, 1), 1, 1, Wizardry.APPRENTICE_TOME_FREQUENCY));
				ChestGenHooks.addItem(chest, new WeightedRandomChestContent(new ItemStack(Wizardry.arcaneTome, 1, 2), 1, 1, Wizardry.ADVANCED_TOME_FREQUENCY));
				ChestGenHooks.addItem(chest, new WeightedRandomChestContent(new ItemStack(Wizardry.arcaneTome, 1, 3), 1, 1, Wizardry.MASTER_TOME_FREQUENCY));
				ChestGenHooks.addItem(chest, new WeightedRandomChestContent(new ItemStack(Wizardry.armourUpgrade), 1, 1, Wizardry.ARMOUR_UPGRADE_FREQUENCY));
				ChestGenHooks.addItem(chest, new WeightedRandomChestContent(new ItemStack(Wizardry.firebomb), 1, 5, Wizardry.FIREBOMB_FREQUENCY));
				ChestGenHooks.addItem(chest, new WeightedRandomChestContent(new ItemStack(Wizardry.poisonBomb), 1, 5, Wizardry.POISON_BOMB_FREQUENCY));
				ChestGenHooks.addItem(chest, new WeightedRandomChestContent(new ItemStack(Wizardry.smokeBomb), 1, 5, Wizardry.SMOKE_BOMB_FREQUENCY));
				if(Wizardry.discoveryMode) ChestGenHooks.addItem(chest, new WeightedRandomChestContent(new ItemStack(Wizardry.identificationScroll), 1, 1, Wizardry.IDENTIFICATION_SCROLL_FREQUENCY));
	
			}
		}
		
		// Creates a new ChestGenHooks for wizard towers.
		ChestGenHooks chestGen = ChestGenHooks.getInfo(WizardryWorldGenerator.WIZARD_TOWER);
		chestGen.setMax(7);
		chestGen.setMin(3);
		
		// Wizard tower chests use exactly the same things, minus the firebombs, poison bombs and smoke bombs
		ChestGenHooks.addItem(WizardryWorldGenerator.WIZARD_TOWER, new WeightedRandomSpellBook(new ItemStack(Wizardry.spellBook), 1, 1, Wizardry.SPELL_BOOK_FREQUENCY));
    	ChestGenHooks.addItem(WizardryWorldGenerator.WIZARD_TOWER, new WeightedRandomSpellBook(new ItemStack(Wizardry.magicWand), 1, 1, Wizardry.WAND_FREQUENCY));
    	ChestGenHooks.addItem(WizardryWorldGenerator.WIZARD_TOWER, new WeightedRandomSpellBook(new ItemStack(Wizardry.wizardHat), 1, 1, Wizardry.ARMOUR_FREQUENCY));
		ChestGenHooks.addItem(WizardryWorldGenerator.WIZARD_TOWER, new WeightedRandomSpellBook(new ItemStack(Wizardry.scroll), 1, 1, Wizardry.SCROLL_FREQUENCY));
		ChestGenHooks.addItem(WizardryWorldGenerator.WIZARD_TOWER, new WeightedRandomSpellBook(new ItemStack(Wizardry.condenserUpgrade), 1, 1, Wizardry.WAND_UPGRADE_FREQUENCY));
		ChestGenHooks.addItem(WizardryWorldGenerator.WIZARD_TOWER, new WeightedRandomChestContent(new ItemStack(Wizardry.magicCrystal), 1, 4, Wizardry.CRYSTAL_FREQUENCY));
		ChestGenHooks.addItem(WizardryWorldGenerator.WIZARD_TOWER, new WeightedRandomChestContent(new ItemStack(Wizardry.arcaneTome, 1, 1), 1, 1, Wizardry.APPRENTICE_TOME_FREQUENCY));
		ChestGenHooks.addItem(WizardryWorldGenerator.WIZARD_TOWER, new WeightedRandomChestContent(new ItemStack(Wizardry.arcaneTome, 1, 2), 1, 1, Wizardry.ADVANCED_TOME_FREQUENCY));
		ChestGenHooks.addItem(WizardryWorldGenerator.WIZARD_TOWER, new WeightedRandomChestContent(new ItemStack(Wizardry.arcaneTome, 1, 3), 1, 1, Wizardry.MASTER_TOME_FREQUENCY));
		ChestGenHooks.addItem(WizardryWorldGenerator.WIZARD_TOWER, new WeightedRandomChestContent(new ItemStack(Wizardry.armourUpgrade), 1, 1, Wizardry.ARMOUR_UPGRADE_FREQUENCY));
		if(Wizardry.discoveryMode) ChestGenHooks.addItem(WizardryWorldGenerator.WIZARD_TOWER, new WeightedRandomChestContent(new ItemStack(Wizardry.identificationScroll), 1, 1, Wizardry.IDENTIFICATION_SCROLL_FREQUENCY));
		
	}

    /* The name defined here in the regitry is the one Minecraft uses in game for commands like /give, as
     * opposed to the one defined earlier with setBlockName() or setUnlocalizedName() which is the one used by
     * the lang files. Ideally these should both be the same, but I've written the lang file now, so... */
	
	/** Called from the preInit method in the main mod class to register all the blocks. */
	public static void registerBlocks(){
        
        GameRegistry.registerBlock(Wizardry.arcaneWorkbench, "arcane_workbench");
        GameRegistry.registerBlock(Wizardry.crystalOre, "crystal_ore");
        GameRegistry.registerBlock(Wizardry.petrifiedStone, "petrified_stone");
        GameRegistry.registerBlock(Wizardry.iceStatue, "ice_statue");
        GameRegistry.registerBlock(Wizardry.magicLight, "magic_light");
        GameRegistry.registerBlock(Wizardry.crystalFlower, "crystal_flower");
        GameRegistry.registerBlock(Wizardry.snare, "snare");
        GameRegistry.registerBlock(Wizardry.transportationStone, "transportation_stone");
        GameRegistry.registerBlock(Wizardry.spectralBlock, "spectral_block");
        GameRegistry.registerBlock(Wizardry.crystalBlock, "crystal_block");
    	GameRegistry.registerBlock(Wizardry.meteor, "meteor");
    	GameRegistry.registerBlock(Wizardry.vanishingCobweb, "vanishing_cobweb");
	}
	
	/** Called from the preInit method in the main mod class to register all the items. */
	public static void registerItems(){
		
		GameRegistry.registerItem(Wizardry.magicCrystal, "magic_crystal");

        GameRegistry.registerItem(Wizardry.magicWand, "magic_wand");
        GameRegistry.registerItem(Wizardry.apprenticeWand, "apprentice_wand");
        GameRegistry.registerItem(Wizardry.advancedWand, "advanced_wand");
        GameRegistry.registerItem(Wizardry.masterWand, "master_wand");

        GameRegistry.registerItem(Wizardry.spellBook, "spell_book");
        GameRegistry.registerItem(Wizardry.arcaneTome, "arcane_tome");
        GameRegistry.registerItem(Wizardry.wizardHandbook, "wizard_handbook");
        
        GameRegistry.registerItem(Wizardry.basicFireWand, "basic_fire_wand");
        GameRegistry.registerItem(Wizardry.basicIceWand, "basic_ice_wand");
        GameRegistry.registerItem(Wizardry.basicLightningWand, "basic_lightning_wand");
        GameRegistry.registerItem(Wizardry.basicNecromancyWand, "basic_necromancy_wand");
        GameRegistry.registerItem(Wizardry.basicEarthWand, "basic_earth_wand");
        GameRegistry.registerItem(Wizardry.basicSorceryWand, "basic_sorcery_wand");
        GameRegistry.registerItem(Wizardry.basicHealingWand, "basic_healing_wand");

        GameRegistry.registerItem(Wizardry.apprenticeFireWand, "apprentice_fire_wand");
        GameRegistry.registerItem(Wizardry.apprenticeIceWand, "apprentice_ice_wand");
        GameRegistry.registerItem(Wizardry.apprenticeLightningWand, "apprentice_lightning_wand");
        GameRegistry.registerItem(Wizardry.apprenticeNecromancyWand, "apprentice_necromancy_wand");
        GameRegistry.registerItem(Wizardry.apprenticeEarthWand, "apprentice_earth_wand");
        GameRegistry.registerItem(Wizardry.apprenticeSorceryWand, "apprentice_sorcery_wand");
        GameRegistry.registerItem(Wizardry.apprenticeHealingWand, "apprentice_healing_wand");

        GameRegistry.registerItem(Wizardry.advancedFireWand, "advanced_fire_wand");
        GameRegistry.registerItem(Wizardry.advancedIceWand, "advanced_ice_wand");
        GameRegistry.registerItem(Wizardry.advancedLightningWand, "advanced_lightning_wand");
        GameRegistry.registerItem(Wizardry.advancedNecromancyWand, "advanced_necromancy_wand");
        GameRegistry.registerItem(Wizardry.advancedEarthWand, "advanced_earth_wand");
        GameRegistry.registerItem(Wizardry.advancedSorceryWand, "advanced_sorcery_wand");
        GameRegistry.registerItem(Wizardry.advancedHealingWand, "advanced_healing_wand");

        GameRegistry.registerItem(Wizardry.masterFireWand, "master_fire_wand");
        GameRegistry.registerItem(Wizardry.masterIceWand, "master_ice_wand");
        GameRegistry.registerItem(Wizardry.masterLightningWand, "master_lightning_wand");
        GameRegistry.registerItem(Wizardry.masterNecromancyWand, "master_necromancy_wand");
        GameRegistry.registerItem(Wizardry.masterEarthWand, "master_earth_wand");
        GameRegistry.registerItem(Wizardry.masterSorceryWand, "master_sorcery_wand");
        GameRegistry.registerItem(Wizardry.masterHealingWand, "master_healing_wand");

        GameRegistry.registerItem(Wizardry.spectralSword, "spectral_sword");
        GameRegistry.registerItem(Wizardry.spectralPickaxe, "spectral_pickaxe");
        GameRegistry.registerItem(Wizardry.spectralBow, "spectral_bow");

        GameRegistry.registerItem(Wizardry.manaFlask, "mana_flask");

        GameRegistry.registerItem(Wizardry.storageUpgrade, "storage_upgrade");
        GameRegistry.registerItem(Wizardry.siphonUpgrade, "siphon_upgrade");
        GameRegistry.registerItem(Wizardry.condenserUpgrade, "condenser_upgrade");
        GameRegistry.registerItem(Wizardry.rangeUpgrade, "range_upgrade");
        GameRegistry.registerItem(Wizardry.durationUpgrade, "duration_upgrade");
        GameRegistry.registerItem(Wizardry.cooldownUpgrade, "cooldown_upgrade");
        GameRegistry.registerItem(Wizardry.blastUpgrade, "blast_upgrade");
        GameRegistry.registerItem(Wizardry.attunementUpgrade, "attunement_upgrade");

        GameRegistry.registerItem(Wizardry.flamingAxe, "flaming_axe");
        GameRegistry.registerItem(Wizardry.frostAxe, "frost_axe");

        GameRegistry.registerItem(Wizardry.firebomb, "firebomb");
        GameRegistry.registerItem(Wizardry.poisonBomb, "poison_bomb");
        
        GameRegistry.registerItem(Wizardry.blankScroll, "blank_scroll");
        GameRegistry.registerItem(Wizardry.scroll, "scroll");

        GameRegistry.registerItem(Wizardry.armourUpgrade, "armour_upgrade");
        
        GameRegistry.registerItem(Wizardry.magicSilk, "magic_silk");
        
        GameRegistry.registerItem(Wizardry.wizardHat, "wizard_hat");
        GameRegistry.registerItem(Wizardry.wizardRobe, "wizard_robe");
        GameRegistry.registerItem(Wizardry.wizardLeggings, "wizard_leggings");
        GameRegistry.registerItem(Wizardry.wizardBoots, "wizard_boots");

        GameRegistry.registerItem(Wizardry.wizardHatFire, "wizard_hat_fire");
        GameRegistry.registerItem(Wizardry.wizardRobeFire, "wizard_robe_fire");
        GameRegistry.registerItem(Wizardry.wizardLeggingsFire, "wizard_leggings_fire");
        GameRegistry.registerItem(Wizardry.wizardBootsFire, "wizard_boots_fire");

        GameRegistry.registerItem(Wizardry.wizardHatIce, "wizard_hat_ice");
        GameRegistry.registerItem(Wizardry.wizardRobeIce, "wizard_robe_ice");
        GameRegistry.registerItem(Wizardry.wizardLeggingsIce, "wizard_leggings_ice");
        GameRegistry.registerItem(Wizardry.wizardBootsIce, "wizard_boots_ice");

        GameRegistry.registerItem(Wizardry.wizardHatLightning, "wizard_hat_lightning");
        GameRegistry.registerItem(Wizardry.wizardRobeLightning, "wizard_robe_lightning");
        GameRegistry.registerItem(Wizardry.wizardLeggingsLightning, "wizard_leggings_lightning");
        GameRegistry.registerItem(Wizardry.wizardBootsLightning, "wizard_boots_lightning");

        GameRegistry.registerItem(Wizardry.wizardHatNecromancy, "wizard_hat_necromancy");
        GameRegistry.registerItem(Wizardry.wizardRobeNecromancy, "wizard_robe_necromancy");
        GameRegistry.registerItem(Wizardry.wizardLeggingsNecromancy, "wizard_leggings_necromancy");
        GameRegistry.registerItem(Wizardry.wizardBootsNecromancy, "wizard_boots_necromancy");

        GameRegistry.registerItem(Wizardry.wizardHatEarth, "wizard_hat_earth");
        GameRegistry.registerItem(Wizardry.wizardRobeEarth, "wizard_robe_earth");
        GameRegistry.registerItem(Wizardry.wizardLeggingsEarth, "wizard_leggings_earth");
        GameRegistry.registerItem(Wizardry.wizardBootsEarth, "wizard_boots_earth");
        
        GameRegistry.registerItem(Wizardry.wizardHatSorcery, "wizard_hat_sorcery");
        GameRegistry.registerItem(Wizardry.wizardRobeSorcery, "wizard_robe_sorcery");
        GameRegistry.registerItem(Wizardry.wizardLeggingsSorcery, "wizard_leggings_sorcery");
        GameRegistry.registerItem(Wizardry.wizardBootsSorcery, "wizard_boots_sorcery");

        GameRegistry.registerItem(Wizardry.wizardHatHealing, "wizard_hat_healing");
        GameRegistry.registerItem(Wizardry.wizardRobeHealing, "wizard_robe_healing");
        GameRegistry.registerItem(Wizardry.wizardLeggingsHealing, "wizard_leggings_healing");
        GameRegistry.registerItem(Wizardry.wizardBootsHealing, "wizard_boots_healing");
        
        GameRegistry.registerItem(Wizardry.spawnWizard, "spawn_wizard");

        GameRegistry.registerItem(Wizardry.spectralHelmet, "spectral_helmet");
        GameRegistry.registerItem(Wizardry.spectralChestplate, "spectral_chestplate");
        GameRegistry.registerItem(Wizardry.spectralLeggings, "spectral_leggings");
        GameRegistry.registerItem(Wizardry.spectralBoots, "spectral_boots");
        
        GameRegistry.registerItem(Wizardry.smokeBomb, "smoke_bomb");
        
        GameRegistry.registerItem(Wizardry.identificationScroll, "identification_scroll");
	}
	
	/** Called from the preInit method in the main mod class to register all the tileentities. */
	public static void registerTileEntities(){

        GameRegistry.registerTileEntity(TileEntityArcaneWorkbench.class, Wizardry.MODID + "ArcaneWorkbenchTileEntity");
        GameRegistry.registerTileEntity(TileEntityStatue.class, Wizardry.MODID + "PetrifiedStoneTileEntity");
        GameRegistry.registerTileEntity(TileEntityMagicLight.class, Wizardry.MODID + "MagicLightTileEntity");
        GameRegistry.registerTileEntity(TileEntityTimer.class, Wizardry.MODID + "TimerTileEntity");
        GameRegistry.registerTileEntity(TileEntityPlayerSave.class, Wizardry.MODID + "TileEntityPlayerSave");
	}
	
	/** Called from the preInit method in the main mod class to register all the entities. */
	public static void registerEntities(Wizardry wizardry){
		
		int id = 0; // Incrementable index for the mod specific entity id.
        
        EntityRegistry.registerModEntity(EntityZombieMinion.class, "Zombie Minion", id++, wizardry, 128, 3, true);
        EntityRegistry.registerModEntity(EntityMagicMissile.class, "Magic Missile", id++, wizardry, 128, 10, true);
        EntityRegistry.registerModEntity(EntityArc.class, "Arc", id++, wizardry, 128, 10, false);
        EntityRegistry.registerModEntity(EntitySkeletonMinion.class, "Skeleton Minion", id++, wizardry, 128, 3, true);
        EntityRegistry.registerModEntity(EntitySparkBomb.class, "Spark Bomb", id++, wizardry, 128, 10, true);
        EntityRegistry.registerModEntity(EntitySpiritWolf.class, "Spirit Wolf", id++, wizardry, 128, 3, true);
        EntityRegistry.registerModEntity(EntityIceShard.class, "Ice Shard", id++, wizardry, 128, 10, true);
        EntityRegistry.registerModEntity(EntityBlazeMinion.class, "Blaze Minion", id++, wizardry, 128, 10, true);
        EntityRegistry.registerModEntity(EntityIceWraith.class, "Ice Wraith", id++, wizardry, 128, 10, true);
        EntityRegistry.registerModEntity(EntityLightningWraith.class, "Lightning Wraith", id++, wizardry, 128, 10, true);
        EntityRegistry.registerModEntity(EntityBlackHole.class, "Black Hole", id++, wizardry, 128, 10, false);
        EntityRegistry.registerModEntity(EntityShield.class, "Shield", id++, wizardry, 128, 1, true);
        EntityRegistry.registerModEntity(EntityMeteor.class, "Meteor", id++, wizardry, 128, 5, true);
        EntityRegistry.registerModEntity(EntityBlizzard.class, "Blizzard", id++, wizardry, 128, 10, false);
        EntityRegistry.registerModEntity(EntityWizard.class, "Wizard", id++, wizardry, 128, 3, true);
        EntityRegistry.registerModEntity(EntityBubble.class, "Bubble", id++, wizardry, 128, 3, false);
        EntityRegistry.registerModEntity(EntityTornado.class, "Tornado", id++, wizardry, 128, 1, false);
        EntityRegistry.registerModEntity(EntityHammer.class, "Lightning Hammer", id++, wizardry, 128, 1, true);
        EntityRegistry.registerModEntity(EntityFirebomb.class, "Firebomb", id++, wizardry, 128, 10, true);
        EntityRegistry.registerModEntity(EntityForceOrb.class, "Force Orb", id++, wizardry, 128, 10, true);
        EntityRegistry.registerModEntity(EntityArrowRain.class, "Arrow Rain", id++, wizardry, 128, 10, false);
        EntityRegistry.registerModEntity(EntitySpark.class, "Spark", id++, wizardry, 128, 10, true);
        EntityRegistry.registerModEntity(EntityShadowWraith.class, "Shadow Wraith", id++, wizardry, 128, 10, true);
        EntityRegistry.registerModEntity(EntityDarknessOrb.class, "Darkness Orb", id++, wizardry, 128, 10, true);
        EntityRegistry.registerModEntity(EntitySpiderMinion.class, "Spider Minion", id++, wizardry, 128, 3, true);
        EntityRegistry.registerModEntity(EntityHealAura.class, "Healing Aura", id++, wizardry, 128, 10, false);
        EntityRegistry.registerModEntity(EntityFireSigil.class, "Fire Sigil", id++, wizardry, 128, 10, false);
        EntityRegistry.registerModEntity(EntityFrostSigil.class, "Frost Sigil", id++, wizardry, 128, 10, false);
        EntityRegistry.registerModEntity(EntityLightningSigil.class, "Lightning Sigil", id++, wizardry, 128, 10, false);
        EntityRegistry.registerModEntity(EntityLightningArrow.class, "Lightning Arrow", id++, wizardry, 128, 10, true);
        EntityRegistry.registerModEntity(EntityFirebolt.class, "Firebolt", id++, wizardry, 128, 10, true);
        EntityRegistry.registerModEntity(EntityPoisonBomb.class, "Poison Bomb", id++, wizardry, 128, 10, true);
        EntityRegistry.registerModEntity(EntityIceCharge.class, "Ice Charge", id++, wizardry, 128, 10, true);
        EntityRegistry.registerModEntity(EntityForceArrow.class, "Force Arrow", id++, wizardry, 128, 10, true);
        EntityRegistry.registerModEntity(EntityDart.class, "Dart", id++, wizardry, 128, 10, true);
        EntityRegistry.registerModEntity(EntityMagicSlime.class, "Magic Slime", id++, wizardry, 128, 3, true);
        EntityRegistry.registerModEntity(EntityForcefield.class, "Forcefield", id++, wizardry, 128, 10, false);
        EntityRegistry.registerModEntity(EntityFireRing.class, "Ring of Fire", id++, wizardry, 128, 10, false);
        EntityRegistry.registerModEntity(EntityLightningDisc.class, "Lightning Disc", id++, wizardry, 128, 10, true);
        EntityRegistry.registerModEntity(EntityThunderbolt.class, "Thunderbolt", id++, wizardry, 128, 10, true);
        EntityRegistry.registerModEntity(EntityIceGiant.class, "Ice Giant", id++, wizardry, 128, 3, true);
        EntityRegistry.registerModEntity(EntitySpiritHorse.class, "Spirit Horse", id++, wizardry, 128, 3, true);
        EntityRegistry.registerModEntity(EntityPhoenix.class, "Phoenix", id++, wizardry, 128, 3, true);
        EntityRegistry.registerModEntity(EntitySilverfishMinion.class, "Silverfish Minion", id++, wizardry, 128, 3, true);
        EntityRegistry.registerModEntity(EntityDecay.class, "Decay", id++, wizardry, 128, 10, false);
        EntityRegistry.registerModEntity(EntityStormElemental.class, "Storm Elemental", id++, wizardry, 128, 10, true);
        EntityRegistry.registerModEntity(EntityEarthquake.class, "Earthquake", id++, wizardry, 128, 10, false);
        EntityRegistry.registerModEntity(EntityFallingGrass.class, "Falling Grass", id++, wizardry, 128, 5, true);
        EntityRegistry.registerModEntity(EntityIceLance.class, "Ice Lance", id++, wizardry, 128, 10, true);
        EntityRegistry.registerModEntity(EntityHailstorm.class, "Hailstorm", id++, wizardry, 128, 10, false);
        EntityRegistry.registerModEntity(EntitySmokeBomb.class, "Smoke Bomb", id++, wizardry, 128, 10, true);
        EntityRegistry.registerModEntity(EntityEvilWizard.class, "Evil Wizard", id++, wizardry, 128, 3, true);
        EntityRegistry.registerModEntity(EntityDecoy.class, "Decoy", id++, wizardry, 128, 3, true);
        EntityRegistry.registerModEntity(EntityIceSpike.class, "Ice Spike", id++, wizardry, 128, 1, true);
        EntityRegistry.registerModEntity(EntityLightningPulse.class, "Lightning Pulse", id++, wizardry, 128, 10, false);

        List<BiomeGenBase> biomes = new ArrayList<BiomeGenBase>();
        for(BiomeGenBase biome : BiomeGenBase.getBiomeGenArray()){
        	if(biome != null){
        		biomes.add(biome);
        	}
        }
        // For reference: 5, 1, 1 are the parameters for the witch in vanilla.
        EntityRegistry.addSpawn(EntityEvilWizard.class, 3, 1, 1, EnumCreatureType.monster, biomes.toArray(new BiomeGenBase[biomes.size()]));

	}
	
	/** Static instance of the none spell, used for various purposes. */
	public static Spell none;
	/** Static instance of the magic missile spell, used for crafting spell books and by wizards. */
	public static Spell magicMissile;
	/** Static instance of the transportation spell, used for chat messages. */
	public static Spell transportation;
	/** Static instance of the clairvoyance spell, used for chat messages. */
	public static Spell clairvoyance;

	/** Called from the preInit method in the main mod class to register all the spells in wizardry. */
	public static void registerSpells(){
		
		// Wizardry 1.0 spells
		
		Spell.register(none = new None());
		Spell.register(magicMissile = new MagicMissile());
		Spell.register(new Ignite());
		Spell.register(new Freeze());
		Spell.register(new Snowball());
		Spell.register(new Arc());
		Spell.register(new Thunderbolt());
		Spell.register(new SummonZombie());
		Spell.register(new Snare());
		Spell.register(new Dart());
		Spell.register(new Light());
		Spell.register(new Telekinesis());
		Spell.register(new Heal());
		
		Spell.register(new Fireball());
		Spell.register(new FlameRay());
		Spell.register(new Firebomb());
		Spell.register(new FireSigil());
		Spell.register(new Firebolt());
		Spell.register(new FrostRay());
		Spell.register(new SummonSnowGolem());
		Spell.register(new IceShard());
		Spell.register(new IceStatue());
		Spell.register(new FrostSigil());
		Spell.register(new LightningRay());
		Spell.register(new SparkBomb());
		Spell.register(new HomingSpark());
		Spell.register(new LightningSigil());
		Spell.register(new LightningArrow());
		Spell.register(new LifeDrain());
		Spell.register(new SummonSkeleton());
		Spell.register(new Metamorphosis());
		Spell.register(new Wither());
		Spell.register(new Poison());
		Spell.register(new GrowthAura());
		Spell.register(new Bubble());
		Spell.register(new Whirlwind());
		Spell.register(new PoisonBomb());
		Spell.register(new SummonSpiritWolf());
		Spell.register(new Blink());
		Spell.register(new Agility());
		Spell.register(new ConjureSword());
		Spell.register(new ConjurePickaxe());
		Spell.register(new ConjureBow());
		Spell.register(new ForceArrow());
		Spell.register(new Shield());
		Spell.register(new ReplenishHunger());
		Spell.register(new CureEffects());
		Spell.register(new HealAlly());
	
		Spell.register(new SummonBlaze());
		Spell.register(new RingOfFire());
		Spell.register(new Detonate());
		Spell.register(new FireResistance());
		Spell.register(new Fireskin());
		Spell.register(new FlamingAxe());
		Spell.register(new Blizzard());
		Spell.register(new SummonIceWraith());
		Spell.register(new IceShroud());
		Spell.register(new IceCharge());
		Spell.register(new FrostAxe());
		Spell.register(new InvokeWeather());
		Spell.register(new ChainLightning());
		Spell.register(new LightningBolt());
		Spell.register(new SummonLightningWraith());
		Spell.register(new StaticAura());
		Spell.register(new LightningDisc());
		Spell.register(new MindControl());
		Spell.register(new SummonWitherSkeleton());
		Spell.register(new Entrapment());
		Spell.register(new WitherSkull());
		Spell.register(new DarknessOrb());
		Spell.register(new ShadowWard());
		Spell.register(new Decay());
		Spell.register(new WaterBreathing());
		Spell.register(new Tornado());
		Spell.register(new Glide());
		Spell.register(new SummonSpiritHorse());
		Spell.register(new SpiderSwarm());
		Spell.register(new Slime());
		Spell.register(new Petrify());
		Spell.register(new Invisibility());
		Spell.register(new Levitation());
		Spell.register(new ForceOrb());
		Spell.register(transportation = new Transportation());
		Spell.register(new SpectralPathway());
		Spell.register(new PhaseStep());
		Spell.register(new VanishingBox());
		Spell.register(new GreaterHeal());
		Spell.register(new HealingAura());
		Spell.register(new Forcefield());
		Spell.register(new Ironflesh());
		Spell.register(new Transience());
	
		Spell.register(new Meteor());
		Spell.register(new Firestorm());
		Spell.register(new SummonPhoenix());
		Spell.register(new IceAge());
		Spell.register(new WallOfFrost());
		Spell.register(new SummonIceGiant());
		Spell.register(new Thunderstorm());
		Spell.register(new LightningHammer());
		Spell.register(new PlagueOfDarkness());
		Spell.register(new SummonSkeletonLegion());
		Spell.register(new SummonShadowWraith());
		Spell.register(new ForestsCurse());
		Spell.register(new Flight());
		Spell.register(new SilverfishSwarm());
		Spell.register(new BlackHole());
		Spell.register(new Shockwave());
		Spell.register(new SummonIronGolem());
		Spell.register(new ArrowRain());
		Spell.register(new Diamondflesh());
		Spell.register(new FontOfVitality());

		// Wizardry 1.1 spells

		Spell.register(new SmokeBomb());
		Spell.register(new MindTrick());
		Spell.register(new Leap());
		
		Spell.register(new PocketFurnace());
		Spell.register(new Intimidate());
		Spell.register(new Banish());
		Spell.register(new SixthSense());
		Spell.register(new Darkvision());
		Spell.register(clairvoyance = new Clairvoyance());
		Spell.register(new PocketWorkbench());
		Spell.register(new ImbueWeapon());
		Spell.register(new InvigoratingPresence());
		Spell.register(new Oakflesh());
		
		Spell.register(new GreaterFireball());
		Spell.register(new FlamingWeapon());
		Spell.register(new IceLance());
		Spell.register(new FreezingWeapon());
		Spell.register(new IceSpikes());
		Spell.register(new LightningPulse());
		Spell.register(new CurseOfSoulbinding());
		Spell.register(new Cobwebs());
		Spell.register(new Decoy());
		Spell.register(new ArcaneJammer());
		Spell.register(new ConjureArmour());
		Spell.register(new GroupHeal());

		Spell.register(new Hailstorm());
		Spell.register(new LightningWeb());
		Spell.register(new SummonStormElemental());
		Spell.register(new Earthquake());
		Spell.register(new FontOfMana());
	}

	/** Called from the init method in the main mod class to register all the recipes. */
	public static void registerRecipes(){

        ItemStack ironIngotStack = new ItemStack(Items.iron_ingot);
        ItemStack magicCrystalStack = new ItemStack(Wizardry.magicCrystal);
        ItemStack magicWandStack = new ItemStack(Wizardry.magicWand, 1, EnumTier.BASIC.maxCharge);
        ItemStack goldNuggetStack = new ItemStack(Items.gold_nugget);
        ItemStack stickStack = new ItemStack(Items.stick);
        ItemStack bookStack = new ItemStack(Items.book);
        ItemStack stringStack = new ItemStack(Items.string);
        ItemStack spellBookStack = new ItemStack(Wizardry.spellBook, 1, WizardryRegistry.magicMissile.id());
        ItemStack arcaneWorkbenchStack = new ItemStack(Wizardry.arcaneWorkbench);
        ItemStack stoneStack = new ItemStack(Blocks.stone);
        ItemStack lapisBlockStack = new ItemStack(Blocks.lapis_block);
        ItemStack purpleCarpetStack = new ItemStack(Blocks.carpet, 1, 10);
        ItemStack wizardHandbookStack = new ItemStack(Wizardry.wizardHandbook);
        ItemStack crystalFlowerStack = new ItemStack(Wizardry.crystalFlower);
        ItemStack magicCrystalStack1 = new ItemStack(Wizardry.magicCrystal, 2);
        ItemStack magicCrystalStack2 = new ItemStack(Wizardry.magicCrystal, 9);
        ItemStack crystalBlockStack = new ItemStack(Wizardry.crystalBlock);
        ItemStack manaFlaskStack = new ItemStack(Wizardry.manaFlask);
        ItemStack bottleStack = new ItemStack(Items.glass_bottle);
        ItemStack gunpowderStack = new ItemStack(Items.gunpowder);
        ItemStack blazePowderStack = new ItemStack(Items.blaze_powder);
        ItemStack spiderEyeStack = new ItemStack(Items.spider_eye);
        // Coal or charcoal is equally fine, hence the wildcard value
        ItemStack coalStack = new ItemStack(Items.coal, 1, OreDictionary.WILDCARD_VALUE);
        ItemStack firebombStack = new ItemStack(Wizardry.firebomb, 3);
        ItemStack poisonBombStack = new ItemStack(Wizardry.poisonBomb, 3);
        ItemStack smokeBombStack = new ItemStack(Wizardry.smokeBomb, 3);
        ItemStack transportationStoneStack = new ItemStack(Wizardry.transportationStone, 2);
        ItemStack silkStack = new ItemStack(Wizardry.magicSilk);
        ItemStack silkStack1 = new ItemStack(Wizardry.magicSilk, 2);
        ItemStack hatStack = new ItemStack(Wizardry.wizardHat);
        ItemStack robeStack = new ItemStack(Wizardry.wizardRobe);
        ItemStack leggingsStack = new ItemStack(Wizardry.wizardLeggings);
        ItemStack bootsStack = new ItemStack(Wizardry.wizardBoots);
        ItemStack scrollStack = new ItemStack(Wizardry.blankScroll);
        ItemStack paperStack = new ItemStack(Items.paper);
        
        GameRegistry.addRecipe(magicWandStack, "  x", " y ", "z  ", 'x', magicCrystalStack, 'y', stickStack, 'z', goldNuggetStack);
        GameRegistry.addRecipe(spellBookStack, " x ", "xyx", " x ", 'x', magicCrystalStack, 'y', bookStack);
        GameRegistry.addRecipe(arcaneWorkbenchStack, "vwv", "xyx", "zzz", 'v', goldNuggetStack, 'w', purpleCarpetStack, 'x', magicCrystalStack, 'y', lapisBlockStack, 'z', stoneStack);
        GameRegistry.addRecipe(manaFlaskStack, "yyy", "yxy", "yyy", 'x', bottleStack, 'y', magicCrystalStack);
        GameRegistry.addRecipe(transportationStoneStack, " x ", "xyx", " x ", 'x', stoneStack, 'y', magicCrystalStack);
        GameRegistry.addRecipe(hatStack, "yyy", "y y", 'y', silkStack);
        GameRegistry.addRecipe(robeStack, "y y", "yyy", "yyy", 'y', silkStack);
        GameRegistry.addRecipe(leggingsStack, "yyy", "y y", "y y", 'y', silkStack);
        GameRegistry.addRecipe(bootsStack, "y y", "y y", 'y', silkStack);
        GameRegistry.addRecipe(silkStack1, " x ", "xyx", " x ", 'x', stringStack, 'y', magicCrystalStack);
        GameRegistry.addRecipe(crystalBlockStack, "zzz", "zzz", "zzz", 'z', magicCrystalStack);

        GameRegistry.addShapelessRecipe(wizardHandbookStack, bookStack, magicCrystalStack);
        GameRegistry.addShapelessRecipe(magicCrystalStack1, crystalFlowerStack);
        GameRegistry.addShapelessRecipe(magicCrystalStack2, crystalBlockStack);
        if(Wizardry.firebombIsCraftable) GameRegistry.addShapelessRecipe(firebombStack, bottleStack, gunpowderStack, blazePowderStack, blazePowderStack);
        if(Wizardry.poisonBombIsCraftable) GameRegistry.addShapelessRecipe(poisonBombStack, bottleStack, gunpowderStack, spiderEyeStack, spiderEyeStack);
        if(Wizardry.smokeBombIsCraftable) GameRegistry.addShapelessRecipe(smokeBombStack, bottleStack, gunpowderStack, coalStack, coalStack);
        if(Wizardry.useAlternateScrollRecipe){
            GameRegistry.addShapelessRecipe(scrollStack, paperStack, stringStack, magicCrystalStack);
        }else{
            GameRegistry.addShapelessRecipe(scrollStack, paperStack, stringStack);
        }
        
        
        // Mana flask recipes
        ItemStack miscWandStack;
        
        for(int i=0; i<EnumElement.values().length; i++){
        	for(int j=0; j<EnumTier.values().length; j++){
        		miscWandStack = new ItemStack(WizardryUtilities.getWand(EnumTier.values()[j], EnumElement.values()[i]), 1, OreDictionary.WILDCARD_VALUE);
        		GameRegistry.addShapelessRecipe(miscWandStack, miscWandStack, manaFlaskStack);
        	}
        }
        
        ItemStack miscArmourStack;
        
        for(int i=0; i<EnumElement.values().length; i++){
        	for(int j=0; j<4; j++){
	        	miscArmourStack = new ItemStack(WizardryUtilities.getArmour(EnumElement.values()[i], j), 1, OreDictionary.WILDCARD_VALUE);
	        	GameRegistry.addShapelessRecipe(miscArmourStack, miscArmourStack, manaFlaskStack);
        	}
        }
	}

}
