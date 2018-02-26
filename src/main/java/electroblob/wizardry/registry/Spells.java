package electroblob.wizardry.registry;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.spell.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.fml.common.registry.IForgeRegistry;
import net.minecraftforge.fml.common.registry.RegistryBuilder;

/**
 * Class responsible for defining, storing and registering all of wizardry's spells.
 * 
 * @author Electroblob
 * @since Wizardry 2.1
 */
// In case anyone was wondering, the reason @ObjectHolder is useful within one mod is that it allows you to initialise
// stuff during the registry events (or whenever), whilst still having a final field (which is important, not only
// because it makes the text go bold, but also because it stops anyone fiddling with your fields). "Why would I want to
// initialise things within the registry events?", I hear you ask - well, for one, custom registries don't like it if
// you haven't created the registry before you start calling constructors of classes extending IForgeRegistryEntry.Impl,
// and secondly, you might want to initialise objects based on certain conditions - perhaps a config option, or whether
// another mod is installed. This, presumably, is why everyone at forge is encouraging us to use @ObjectHolder.
@ObjectHolder(Wizardry.MODID)
@Mod.EventBusSubscriber
public final class Spells {

	// This is here because this class is already an event handler.
	@SubscribeEvent
	public static void createRegistry(RegistryEvent.NewRegistry event){

		// Beats me why we need both of these. Surely the type parameter means it already knows?
		RegistryBuilder<Spell> builder = new RegistryBuilder<Spell>();
		builder.setType(Spell.class);
		builder.setName(new ResourceLocation(Wizardry.MODID, "spells"));
		builder.setIDRange(0, 5000); // Is there any penalty for using a larger number?

		Spell.registry = builder.create();
	}

	// Wizardry 1.0 spells

	public static final Spell none = null;
	public static final Spell magic_missile = null;
	public static final Spell ignite = null;
	public static final Spell freeze = null;
	public static final Spell snowball = null;
	public static final Spell arc = null;
	public static final Spell thunderbolt = null;
	public static final Spell summon_zombie = null;
	public static final Spell snare = null;
	public static final Spell dart = null;
	public static final Spell light = null;
	public static final Spell telekinesis = null;
	public static final Spell heal = null;

	public static final Spell fireball = null;
	public static final Spell flame_ray = null;
	public static final Spell firebomb = null;
	public static final Spell fire_sigil = null;
	public static final Spell firebolt = null;
	public static final Spell frost_ray = null;
	public static final Spell summon_snow_golem = null;
	public static final Spell ice_shard = null;
	public static final Spell ice_statue = null;
	public static final Spell frost_sigil = null;
	public static final Spell lightning_ray = null;
	public static final Spell spark_bomb = null;
	public static final Spell homing_spark = null;
	public static final Spell lightning_sigil = null;
	public static final Spell lightning_arrow = null;
	public static final Spell life_drain = null;
	public static final Spell summon_skeleton = null;
	public static final Spell metamorphosis = null;
	public static final Spell wither = null;
	public static final Spell poison = null;
	public static final Spell growth_aura = null;
	public static final Spell bubble = null;
	public static final Spell whirlwind = null;
	public static final Spell poison_bomb = null;
	public static final Spell summon_spirit_wolf = null;
	public static final Spell blink = null;
	public static final Spell agility = null;
	public static final Spell conjure_sword = null;
	public static final Spell conjure_pickaxe = null;
	public static final Spell conjure_bow = null;
	public static final Spell force_arrow = null;
	public static final Spell shield = null;
	public static final Spell replenish_hunger = null;
	public static final Spell cure_effects = null;
	public static final Spell heal_ally = null;

	public static final Spell summon_blaze = null;
	public static final Spell ring_of_fire = null;
	public static final Spell detonate = null;
	public static final Spell fire_resistance = null;
	public static final Spell fireskin = null;
	public static final Spell flaming_axe = null;
	public static final Spell blizzard = null;
	public static final Spell summon_ice_wraith = null;
	public static final Spell ice_shroud = null;
	public static final Spell ice_charge = null;
	public static final Spell frost_axe = null;
	public static final Spell invoke_weather = null;
	public static final Spell chain_lightning = null;
	public static final Spell lightning_bolt = null;
	public static final Spell summon_lightning_wraith = null;
	public static final Spell static_aura = null;
	public static final Spell lightning_disc = null;
	public static final Spell mind_control = null;
	public static final Spell summon_wither_skeleton = null;
	public static final Spell entrapment = null;
	public static final Spell wither_skull = null;
	public static final Spell darkness_orb = null;
	public static final Spell shadow_ward = null;
	public static final Spell decay = null;
	public static final Spell water_breathing = null;
	public static final Spell tornado = null;
	public static final Spell glide = null;
	public static final Spell summon_spirit_horse = null;
	public static final Spell spider_swarm = null;
	public static final Spell slime = null;
	public static final Spell petrify = null;
	public static final Spell invisibility = null;
	public static final Spell levitation = null;
	public static final Spell force_orb = null;
	public static final Spell transportation = null;
	public static final Spell spectral_pathway = null;
	public static final Spell phase_step = null;
	public static final Spell vanishing_box = null;
	public static final Spell greater_heal = null;
	public static final Spell healing_aura = null;
	public static final Spell forcefield = null;
	public static final Spell ironflesh = null;
	public static final Spell transience = null;

	public static final Spell meteor = null;
	public static final Spell firestorm = null;
	public static final Spell summon_phoenix = null;
	public static final Spell ice_age = null;
	public static final Spell wall_of_frost = null;
	public static final Spell summon_ice_giant = null;
	public static final Spell thunderstorm = null;
	public static final Spell lightning_hammer = null;
	public static final Spell plague_of_darkness = null;
	public static final Spell summon_skeleton_legion = null;
	public static final Spell summon_shadow_wraith = null;
	public static final Spell forests_curse = null;
	public static final Spell flight = null;
	public static final Spell silverfish_swarm = null;
	public static final Spell black_hole = null;
	public static final Spell shockwave = null;
	public static final Spell summon_iron_golem = null;
	public static final Spell arrow_rain = null;
	public static final Spell diamondflesh = null;
	public static final Spell font_of_vitality = null;

	// Wizardry 1.1 spells

	public static final Spell smoke_bomb = null;
	public static final Spell mind_trick = null;
	public static final Spell leap = null;

	public static final Spell pocket_furnace = null;
	public static final Spell intimidate = null;
	public static final Spell banish = null;
	public static final Spell sixth_sense = null;
	public static final Spell darkvision = null;
	public static final Spell clairvoyance = null;
	public static final Spell pocket_workbench = null;
	public static final Spell imbue_weapon = null;
	public static final Spell invigorating_presence = null;
	public static final Spell oakflesh = null;

	public static final Spell greater_fireball = null;
	public static final Spell flaming_weapon = null;
	public static final Spell ice_lance = null;
	public static final Spell freezing_weapon = null;
	public static final Spell ice_spikes = null;
	public static final Spell lightning_pulse = null;
	public static final Spell curse_of_soulbinding = null;
	public static final Spell cobwebs = null;
	public static final Spell decoy = null;
	public static final Spell arcane_jammer = null;
	public static final Spell conjure_armour = null;
	public static final Spell group_heal = null;

	public static final Spell hailstorm = null;
	public static final Spell lightning_web = null;
	public static final Spell summon_storm_elemental = null;
	public static final Spell earthquake = null;
	public static final Spell font_of_mana = null;

	@SubscribeEvent
	public static void register(RegistryEvent.Register<Spell> event){

		IForgeRegistry<Spell> registry = event.getRegistry();

		// event.getRegistry should always equal Spell.registry.
		registry.register(new None());
		registry.register(new MagicMissile());
		registry.register(new Ignite());
		registry.register(new Freeze());
		registry.register(new Snowball());
		registry.register(new Arc());
		registry.register(new Thunderbolt());
		registry.register(new SummonZombie());
		registry.register(new Snare());
		registry.register(new Dart());
		registry.register(new Light());
		registry.register(new Telekinesis());
		registry.register(new Heal());

		registry.register(new Fireball());
		registry.register(new FlameRay());
		registry.register(new Firebomb());
		registry.register(new FireSigil());
		registry.register(new Firebolt());
		registry.register(new FrostRay());
		registry.register(new SummonSnowGolem());
		registry.register(new IceShard());
		registry.register(new IceStatue());
		registry.register(new FrostSigil());
		registry.register(new LightningRay());
		registry.register(new SparkBomb());
		registry.register(new HomingSpark());
		registry.register(new LightningSigil());
		registry.register(new LightningArrow());
		registry.register(new LifeDrain());
		registry.register(new SummonSkeleton());
		registry.register(new Metamorphosis());
		registry.register(new Wither());
		registry.register(new Poison());
		registry.register(new GrowthAura());
		registry.register(new Bubble());
		registry.register(new Whirlwind());
		registry.register(new PoisonBomb());
		registry.register(new SummonSpiritWolf());
		registry.register(new Blink());
		registry.register(new Agility());
		registry.register(new ConjureSword());
		registry.register(new ConjurePickaxe());
		registry.register(new ConjureBow());
		registry.register(new ForceArrow());
		registry.register(new Shield());
		registry.register(new ReplenishHunger());
		registry.register(new CureEffects());
		registry.register(new HealAlly());

		registry.register(new SummonBlaze());
		registry.register(new RingOfFire());
		registry.register(new Detonate());
		registry.register(new FireResistance());
		registry.register(new Fireskin());
		registry.register(new FlamingAxe());
		registry.register(new Blizzard());
		registry.register(new SummonIceWraith());
		registry.register(new IceShroud());
		registry.register(new IceCharge());
		registry.register(new FrostAxe());
		registry.register(new InvokeWeather());
		registry.register(new ChainLightning());
		registry.register(new LightningBolt());
		registry.register(new SummonLightningWraith());
		registry.register(new StaticAura());
		registry.register(new LightningDisc());
		registry.register(new MindControl());
		registry.register(new SummonWitherSkeleton());
		registry.register(new Entrapment());
		registry.register(new WitherSkull());
		registry.register(new DarknessOrb());
		registry.register(new ShadowWard());
		registry.register(new Decay());
		registry.register(new WaterBreathing());
		registry.register(new Tornado());
		registry.register(new Glide());
		registry.register(new SummonSpiritHorse());
		registry.register(new SpiderSwarm());
		registry.register(new Slime());
		registry.register(new Petrify());
		registry.register(new Invisibility());
		registry.register(new Levitation());
		registry.register(new ForceOrb());
		registry.register(new Transportation());
		registry.register(new SpectralPathway());
		registry.register(new PhaseStep());
		registry.register(new VanishingBox());
		registry.register(new GreaterHeal());
		registry.register(new HealingAura());
		registry.register(new Forcefield());
		registry.register(new Ironflesh());
		registry.register(new Transience());

		registry.register(new Meteor());
		registry.register(new Firestorm());
		registry.register(new SummonPhoenix());
		registry.register(new IceAge());
		registry.register(new WallOfFrost());
		registry.register(new SummonIceGiant());
		registry.register(new Thunderstorm());
		registry.register(new LightningHammer());
		registry.register(new PlagueOfDarkness());
		registry.register(new SummonSkeletonLegion());
		registry.register(new SummonShadowWraith());
		registry.register(new ForestsCurse());
		registry.register(new Flight());
		registry.register(new SilverfishSwarm());
		registry.register(new BlackHole());
		registry.register(new Shockwave());
		registry.register(new SummonIronGolem());
		registry.register(new ArrowRain());
		registry.register(new Diamondflesh());
		registry.register(new FontOfVitality());

		// Wizardry 1.1 spells

		registry.register(new SmokeBomb());
		registry.register(new MindTrick());
		registry.register(new Leap());

		registry.register(new PocketFurnace());
		registry.register(new Intimidate());
		registry.register(new Banish());
		registry.register(new SixthSense());
		registry.register(new Darkvision());
		registry.register(new Clairvoyance());
		registry.register(new PocketWorkbench());
		registry.register(new ImbueWeapon());
		registry.register(new InvigoratingPresence());
		registry.register(new Oakflesh());

		registry.register(new GreaterFireball());
		registry.register(new FlamingWeapon());
		registry.register(new IceLance());
		registry.register(new FreezingWeapon());
		registry.register(new IceSpikes());
		registry.register(new LightningPulse());
		registry.register(new CurseOfSoulbinding());
		registry.register(new Cobwebs());
		registry.register(new Decoy());
		registry.register(new ArcaneJammer());
		registry.register(new ConjureArmour());
		registry.register(new GroupHeal());

		registry.register(new Hailstorm());
		registry.register(new LightningWeb());
		registry.register(new SummonStormElemental());
		registry.register(new Earthquake());
		registry.register(new FontOfMana());
	}

}