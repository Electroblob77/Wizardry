package electroblob.wizardry.registry;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.entity.EntityLevitatingBlock;
import electroblob.wizardry.entity.EntityMeteor;
import electroblob.wizardry.entity.EntityShield;
import electroblob.wizardry.entity.construct.*;
import electroblob.wizardry.entity.living.*;
import electroblob.wizardry.entity.projectile.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Class responsible for registering all of wizardry's entities and their spawning conditions.
 *
 * @author Electroblob
 * @since Wizardry 4.2
 */
@Mod.EventBusSubscriber
public class WizardryEntities {

	private WizardryEntities(){} // No instances!

	/** Most entity trackers fall into one of a few categories, so they are defined here for convenience. This
	 * generally follows the values used in vanilla for each entity type. */
	enum TrackingType {

		LIVING(80, 3, true),
		PROJECTILE(64, 10, true),
		CONSTRUCT(160, 10, false);

		int range;
		int interval;
		boolean trackVelocity;

		TrackingType(int range, int interval, boolean trackVelocity){
			this.range = range;
			this.interval = interval;
			this.trackVelocity = trackVelocity;
		}
	}

	/** Incrementing index for the mod-specific entity network ID. */
	private static int id = 0;

	@SubscribeEvent
	public static void register(RegistryEvent.Register<EntityEntry> event){

		IForgeRegistry<EntityEntry> registry = event.getRegistry();

		// Vanilla summoned creatures
		registry.register(createEntry(EntityZombieMinion.class, 		"zombie_minion", 			TrackingType.LIVING).build());
		registry.register(createEntry(EntityHuskMinion.class, 			"husk_minion", 			TrackingType.LIVING).build());
		registry.register(createEntry(EntitySkeletonMinion.class, 		"skeleton_minion", 		TrackingType.LIVING).build());
		registry.register(createEntry(EntityStrayMinion.class, 			"stray_minion", 			TrackingType.LIVING).build());
		registry.register(createEntry(EntitySpiderMinion.class, 		"spider_minion", 			TrackingType.LIVING).build());
		registry.register(createEntry(EntityBlazeMinion.class, 			"blaze_minion", 			TrackingType.LIVING).build());
		registry.register(createEntry(EntityWitherSkeletonMinion.class, "wither_skeleton_minion", TrackingType.LIVING).build());
		registry.register(createEntry(EntitySilverfishMinion.class, 	"silverfish_minion", 		TrackingType.LIVING).build());
		registry.register(createEntry(EntityVexMinion.class, 			"vex_minion", 			TrackingType.LIVING).build());

		// Custom summoned creatures
		registry.register(createEntry(EntityIceWraith.class, 		"ice_wraith", 		TrackingType.LIVING).egg(0xaafaff, 0x001ce1)
				.spawn(EnumCreatureType.MONSTER, Wizardry.settings.iceWraithSpawnRate, 1, 1, ForgeRegistries.BIOMES.getValuesCollection().stream()
						.filter(b -> !Arrays.asList(Wizardry.settings.mobSpawnBiomeBlacklist).contains(b.getRegistryName())
								&& BiomeDictionary.hasType(b, BiomeDictionary.Type.SNOWY)
								&& !BiomeDictionary.hasType(b, BiomeDictionary.Type.FOREST))
						.collect(Collectors.toSet())).build());

		registry.register(createEntry(EntityLightningWraith.class, 	"lightning_wraith", 	TrackingType.LIVING).egg(0x35424b, 0x27b9d9)
				.spawn(EnumCreatureType.MONSTER, Wizardry.settings.lightningWraithSpawnRate, 1, 1, ForgeRegistries.BIOMES.getValuesCollection().stream()
					.filter(b -> !Arrays.asList(Wizardry.settings.mobSpawnBiomeBlacklist).contains(b.getRegistryName()))
					.collect(Collectors.toSet())).build());

		registry.register(createEntry(EntitySpiritWolf.class, 		"spirit_wolf", 		TrackingType.LIVING).egg(0xbcc2e8, 0x5464c6).build());
		registry.register(createEntry(EntitySpiritHorse.class, 		"spirit_horse", 		TrackingType.LIVING).egg(0x5464c6, 0xbcc2e8).build());
		registry.register(createEntry(EntityPhoenix.class, 			"phoenix", 			TrackingType.LIVING).egg(0xff4900, 0xfde535).build());
		registry.register(createEntry(EntityIceGiant.class, 		"ice_giant", 			TrackingType.LIVING).egg(0x5bacd9, 0xeffaff).build());

		registry.register(createEntry(EntityMagicSlime.class, 		"magic_slime", 		TrackingType.LIVING).build());
		registry.register(createEntry(EntityDecoy.class, 			"decoy", 				TrackingType.LIVING).build());

		// These two are only made of particles, so we can afford a lower update frequency
		registry.register(createEntry(EntityShadowWraith.class, 	"shadow_wraith")		.tracker(80, 10, true).egg(0x11071c, 0x421384).build());
		registry.register(createEntry(EntityStormElemental.class, 	"storm_elemental")	.tracker(80, 10, true).egg(0x162128, 0x135279).build());

		// Other living entities
		registry.register(createEntry(EntityWizard.class, 			"wizard", 			TrackingType.LIVING).egg(0x19295e, 0xee9312).build());
		registry.register(createEntry(EntityEvilWizard.class, 		"evil_wizard", 		TrackingType.LIVING).egg(0x290404, 0xee9312)
				// For reference: 5, 1, 1 are the parameters for the witch in vanilla
				.spawn(EnumCreatureType.MONSTER, Wizardry.settings.evilWizardSpawnRate, 1, 1, ForgeRegistries.BIOMES.getValuesCollection().stream()
						.filter(b -> !Arrays.asList(Wizardry.settings.mobSpawnBiomeBlacklist).contains(b.getRegistryName()))
						.collect(Collectors.toSet())).build());
		registry.register(createEntry(EntityRemnant.class, 			"remnant", 			TrackingType.LIVING).egg(0xe4c7cd, 0x9d2cf3).build());

		// Directed projectiles
		registry.register(createEntry(EntityMagicMissile.class, 	"magic_missile", 		TrackingType.PROJECTILE).build());
		registry.register(createEntry(EntityIceShard.class, 		"ice_shard", 			TrackingType.PROJECTILE).build());
		registry.register(createEntry(EntityLightningArrow.class, 	"lightning_arrow", 	TrackingType.PROJECTILE).build());
		registry.register(createEntry(EntityForceArrow.class, 		"force_arrow", 		TrackingType.PROJECTILE).build());
		registry.register(createEntry(EntityDart.class, 			"dart", 				TrackingType.PROJECTILE).build());
		registry.register(createEntry(EntityIceLance.class, 		"ice_lance", 			TrackingType.PROJECTILE).build());

		// Directionless projectiles
		registry.register(createEntry(EntityFirebomb.class, 		"firebomb", 			TrackingType.PROJECTILE).build());
		registry.register(createEntry(EntityPoisonBomb.class, 		"poison_bomb", 		TrackingType.PROJECTILE).build());
		registry.register(createEntry(EntitySparkBomb.class, 		"spark_bomb", 		TrackingType.PROJECTILE).build());
		registry.register(createEntry(EntitySmokeBomb.class, 		"smoke_bomb", 		TrackingType.PROJECTILE).build());
		registry.register(createEntry(EntityIceCharge.class, 		"ice_charge", 		TrackingType.PROJECTILE).build());
		registry.register(createEntry(EntityForceOrb.class, 		"force_orb", 			TrackingType.PROJECTILE).build());
		registry.register(createEntry(EntitySpark.class, 			"spark", 				TrackingType.PROJECTILE).build());
		registry.register(createEntry(EntityDarknessOrb.class, 		"darkness_orb", 		TrackingType.PROJECTILE).build());
		registry.register(createEntry(EntityFirebolt.class, 		"firebolt", 			TrackingType.PROJECTILE).build());
		registry.register(createEntry(EntityThunderbolt.class, 		"thunderbolt", 		TrackingType.PROJECTILE).build());
		registry.register(createEntry(EntityLightningDisc.class, 	"lightning_disc", 	TrackingType.PROJECTILE).build());
		registry.register(createEntry(EntityEmber.class, 			"ember", 				TrackingType.PROJECTILE).build());
		registry.register(createEntry(EntityMagicFireball.class, 	"magic_fireball", 	TrackingType.PROJECTILE).build());
		registry.register(createEntry(EntityLargeMagicFireball.class, "large_magic_fireball", TrackingType.PROJECTILE).build());
		registry.register(createEntry(EntityIceball.class, 			"iceball", 			TrackingType.PROJECTILE).build());

		// These are effectively projectiles, but since they're bigger and start high up they need updating from further away
		registry.register(createEntry(EntityMeteor.class, 			"meteor")				.tracker(160, 3, true).build());
		registry.register(createEntry(EntityHammer.class, 			"lightning_hammer")	.tracker(160, 3, true).build());
		registry.register(createEntry(EntityLevitatingBlock.class, 	"levitating_block")	.tracker(160, 3, true).build());

		// Constructs
		registry.register(createEntry(EntityBlackHole.class, 		"black_hole", 		TrackingType.CONSTRUCT).build());
		registry.register(createEntry(EntityBlizzard.class, 		"blizzard", 			TrackingType.CONSTRUCT).build());
		registry.register(createEntry(EntityForcefield.class, 		"forcefield", 		TrackingType.CONSTRUCT).build());
		registry.register(createEntry(EntityFireSigil.class, 		"fire_sigil", 		TrackingType.CONSTRUCT).build());
		registry.register(createEntry(EntityFrostSigil.class, 		"frost_sigil", 		TrackingType.CONSTRUCT).build());
		registry.register(createEntry(EntityLightningSigil.class, 	"lightning_sigil", 	TrackingType.CONSTRUCT).build());
		registry.register(createEntry(EntityCombustionRune.class, 	"combustion_rune", 	TrackingType.CONSTRUCT).build());
		registry.register(createEntry(EntityFireRing.class, 		"ring_of_fire", 		TrackingType.CONSTRUCT).build());
		registry.register(createEntry(EntityHealAura.class, 		"healing_aura", 		TrackingType.CONSTRUCT).build());
		registry.register(createEntry(EntityDecay.class, 			"decay", 				TrackingType.CONSTRUCT).build());

		// These ones don't render, currently that makes no difference here but we might as well separate them
		registry.register(createEntry(EntityArrowRain.class, 		"arrow_rain", 		TrackingType.CONSTRUCT).build());
		registry.register(createEntry(EntityEarthquake.class, 		"earthquake", 		TrackingType.CONSTRUCT).build());
		registry.register(createEntry(EntityHailstorm.class, 		"hailstorm", 			TrackingType.CONSTRUCT).build());
		registry.register(createEntry(EntityStormcloud.class, 		"stormcloud", 		TrackingType.CONSTRUCT).build());

		// These ones move, velocity updates are sent if that's not at constant velocity
		registry.register(createEntry(EntityShield.class, 			"shield")				.tracker(160, 10, true).build());
		registry.register(createEntry(EntityBubble.class, 			"bubble")				.tracker(160, 3, false).build());
		registry.register(createEntry(EntityTornado.class, 			"tornado")			.tracker(160, 3, false).build());
		registry.register(createEntry(EntityIceSpike.class, 		"ice_spike")			.tracker(160, 1, true).build());

	}

	/**
	 * Private helper method that simplifies the parts of an {@link EntityEntry} that are common to all entities.
	 * This automatically assigns a network id, and accepts a {@link TrackingType} for automatic tracker assignment.
	 * @param entityClass The entity class to use.
	 * @param name The name of the entity. This will form the path of a {@code ResourceLocation} with domain
	 * 		       {@code ebwizardry}, which in turn will be used as both the registry name and the 'command' name.
	 * @param tracking The {@link TrackingType} to use for this entity.
	 * @param <T> The type of entity.
	 * @return The (part-built) builder instance, allowing other builder methods to be added as necessary.
	 */
	private static <T extends Entity> EntityEntryBuilder<T> createEntry(Class<T> entityClass, String name, TrackingType tracking){
		return createEntry(entityClass, name).tracker(tracking.range, tracking.interval, tracking.trackVelocity);
	}

	/**
	 * Private helper method that simplifies the parts of an {@link EntityEntry} that are common to all entities.
	 * This automatically assigns a network id.
	 * @param entityClass The entity class to use.
	 * @param name The name of the entity. This will form the path of a {@code ResourceLocation} with domain
	 * 		       {@code ebwizardry}, which in turn will be used as both the registry name and the 'command' name.
	 * @param <T> The type of entity.
	 * @return The (part-built) builder instance, allowing other builder methods to be added as necessary.
	 */
	private static <T extends Entity> EntityEntryBuilder<T> createEntry(Class<T> entityClass, String name){
		ResourceLocation registryName = new ResourceLocation(Wizardry.MODID, name);
		return EntityEntryBuilder.<T>create().entity(entityClass).id(registryName, id++).name(registryName.toString());
	}

}
