package electroblob.wizardry.registry;

import org.apache.commons.lang3.tuple.Triple;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.construct.EntityBlackHole;
import electroblob.wizardry.entity.construct.EntityBlizzard;
import electroblob.wizardry.entity.construct.EntityFireRing;
import electroblob.wizardry.entity.construct.EntityFireSigil;
import electroblob.wizardry.entity.construct.EntityForcefield;
import electroblob.wizardry.entity.construct.EntityFrostSigil;
import electroblob.wizardry.entity.construct.EntityHealAura;
import electroblob.wizardry.entity.construct.EntityLightningSigil;
import electroblob.wizardry.entity.living.EntityBlazeMinion;
import electroblob.wizardry.entity.living.EntityIceGiant;
import electroblob.wizardry.entity.living.EntityIceWraith;
import electroblob.wizardry.entity.living.EntityLightningWraith;
import electroblob.wizardry.entity.living.EntityPhoenix;
import electroblob.wizardry.entity.living.EntitySilverfishMinion;
import electroblob.wizardry.entity.living.EntitySpiderMinion;
import electroblob.wizardry.entity.living.EntityStormElemental;
import electroblob.wizardry.entity.living.EntityWitherSkeletonMinion;
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
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

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

		registry.register(new None());
		registry.register(new SpellArrow("magic_missile", Tier.BASIC, Element.MAGIC, 5, 10, EntityMagicMissile::new, 2, WizardrySounds.SPELL_MAGIC).soundValues(1, 1.4f, 0.4f));
		registry.register(new Ignite());
		registry.register(new Freeze());
		registry.register(new Snowball());
		registry.register(new Arc());
		registry.register(new SpellProjectile("thunderbolt", Tier.BASIC, Element.LIGHTNING, 10, 15, EntityThunderbolt::new, 2.5f, WizardrySounds.SPELL_ICE).soundValues(0.8f, 0.9f, 0.2f));
		registry.register(new SpellMinion<>("summon_zombie", Tier.BASIC, Element.NECROMANCY, 10, 40, EntityZombieMinion::new, 600, WizardrySounds.SPELL_SUMMONING).soundValues(7, 0.6f, 0));
		registry.register(new Snare());
		registry.register(new SpellArrow("dart", Tier.BASIC, Element.EARTH, 5, 10, EntityDart::new, 2, SoundEvents.ENTITY_ARROW_SHOOT).soundValues(0.5f, 0.4f, 0.2f));
		registry.register(new Light());
		registry.register(new Telekinesis());
		registry.register(new Heal());

		registry.register(new Fireball());
		registry.register(new FlameRay());
		registry.register(new SpellProjectile("firebomb", Tier.APPRENTICE, Element.FIRE, 15, 25, EntityFirebomb::new, 1.5f, SoundEvents.ENTITY_SNOWBALL_THROW).soundValues(0.5f, 0.4f, 0.2f));
		registry.register(new SpellConstructRanged<>("fire_sigil", Tier.APPRENTICE, Element.FIRE, SpellType.CONSTRUCT, 10, 20, EntityFireSigil::new, -1, 10, SoundEvents.ITEM_FLINTANDSTEEL_USE).floor(true));
		registry.register(new SpellProjectile("firebolt", Tier.APPRENTICE, Element.FIRE, 10, 10, EntityFirebolt::new, 2.5f, SoundEvents.ENTITY_BLAZE_SHOOT));
		registry.register(new FrostRay());
		registry.register(new SummonSnowGolem());
		registry.register(new SpellArrow("ice_shard", Tier.APPRENTICE, Element.ICE, 10, 10, EntityIceShard::new, 2, WizardrySounds.SPELL_ICE).soundValues(1, 1.6f, 0.4f));
		registry.register(new IceStatue());
		registry.register(new SpellConstructRanged<>("frost_sigil", Tier.APPRENTICE, Element.ICE, SpellType.CONSTRUCT, 10, 20, EntityFrostSigil::new, -1, 10, WizardrySounds.SPELL_ICE).floor(true));
		registry.register(new LightningRay());
		registry.register(new SpellProjectile("spark_bomb", Tier.APPRENTICE, Element.LIGHTNING, 15, 25, EntitySparkBomb::new, 1.5f, SoundEvents.ENTITY_SNOWBALL_THROW).soundValues(0.5f, 0.4f, 0.2f));
		registry.register(new SpellProjectile("homing_spark", Tier.APPRENTICE, Element.LIGHTNING, 10, 20, EntitySpark::new, 0.5f, WizardrySounds.SPELL_CONJURATION).soundValues(1.0f, 0.4f, 0.2f));
		registry.register(new SpellConstructRanged<>("lightning_sigil", Tier.APPRENTICE, Element.LIGHTNING, SpellType.CONSTRUCT, 10, 20, EntityLightningSigil::new, -1, 10, WizardrySounds.SPELL_CONJURATION).floor(true));
		registry.register(new SpellArrow("lightning_arrow", Tier.APPRENTICE, Element.LIGHTNING, 15, 20, EntityLightningArrow::new, 2, WizardrySounds.SPELL_LIGHTNING).soundValues(1, 1.45f, 0.3f));
		registry.register(new LifeDrain());
		registry.register(new SummonSkeleton());
		registry.register(new Metamorphosis());
		registry.register(new Wither());
		registry.register(new Poison());
		registry.register(new GrowthAura());
		registry.register(new Bubble());
		registry.register(new Whirlwind());
		registry.register(new SpellProjectile("poison_bomb", Tier.APPRENTICE, Element.EARTH, 15, 25, EntityPoisonBomb::new, 1.5f, SoundEvents.ENTITY_SNOWBALL_THROW).soundValues(0.5f, 0.4f, 0.2f));
		registry.register(new SummonSpiritWolf());
		registry.register(new Blink());
		registry.register(new SpellBuff("agility", Tier.APPRENTICE, Element.SORCERY, SpellType.BUFF, 20, 40, WizardrySounds.SPELL_HEAL, 0.4f, 1.0f, 0.8f, Triple.of(MobEffects.SPEED, 600, 1), Triple.of(MobEffects.JUMP_BOOST, 600, 1)).soundValues(0.7f, 1.2f, 0.4f));
		registry.register(new SpellConjuration("conjure_sword", Tier.APPRENTICE, Element.SORCERY, SpellType.UTILITY, 25, 50, WizardryItems.spectral_sword, WizardrySounds.SPELL_CONJURATION));
		registry.register(new SpellConjuration("conjure_pickaxe", Tier.APPRENTICE, Element.SORCERY, SpellType.UTILITY, 25, 50, WizardryItems.spectral_pickaxe, WizardrySounds.SPELL_CONJURATION));
		registry.register(new SpellConjuration("conjure_bow", Tier.APPRENTICE, Element.SORCERY, SpellType.UTILITY, 40, 50, WizardryItems.spectral_bow, WizardrySounds.SPELL_CONJURATION));
		registry.register(new SpellArrow("force_arrow", Tier.APPRENTICE, Element.SORCERY, 15, 20, EntityForceArrow::new, 1, WizardrySounds.SPELL_FORCE).soundValues(1, 1.3f, 0.2f));
		registry.register(new Shield());
		registry.register(new ReplenishHunger());
		registry.register(new CureEffects());
		registry.register(new HealAlly());

		registry.register(new SpellMinion<>("summon_blaze", Tier.ADVANCED, Element.FIRE, 40, 200, EntityBlazeMinion::new, 600, SoundEvents.ENTITY_WITHER_AMBIENT).soundValues(1, 1.1f, 0.2f));
		registry.register(new SpellConstruct<>("ring_of_fire", Tier.ADVANCED, Element.FIRE, SpellType.CONSTRUCT, 30, 100, EnumAction.BOW, EntityFireRing::new, 600, SoundEvents.ENTITY_BLAZE_SHOOT));
		registry.register(new Detonate());
		registry.register(new SpellBuff("fire_resistance", Tier.ADVANCED, Element.FIRE, SpellType.DEFENCE, 20, 80, WizardrySounds.SPELL_HEAL, 1, 0.5f, 0, Triple.of(MobEffects.FIRE_RESISTANCE, 600, 0)).soundValues(0.7f, 1.2f, 0.4f));
		registry.register(new SpellBuff("fireskin", Tier.ADVANCED, Element.FIRE, SpellType.DEFENCE, 40, 250, SoundEvents.ENTITY_BLAZE_SHOOT, 1, 0.5f, 0, Triple.of(WizardryPotions.fireskin, 600, 0)));
		registry.register(new FlamingAxe());
		registry.register(new SpellConstructRanged<>("blizzard", Tier.ADVANCED, Element.ICE, SpellType.CONSTRUCT, 40, 100, EntityBlizzard::new, 600, 20, WizardrySounds.SPELL_ICE));
		registry.register(new SpellMinion<>("summon_ice_wraith", Tier.ADVANCED, Element.ICE, 40, 200, EntityIceWraith::new, 600, SoundEvents.ENTITY_WITHER_AMBIENT).soundValues(1, 1.1f, 0.2f));
		registry.register(new SpellBuff("ice_shroud", Tier.ADVANCED, Element.ICE, SpellType.DEFENCE, 40, 250, WizardrySounds.SPELL_ICE, 0.3f, 0.5f, 1, Triple.of(WizardryPotions.ice_shroud, 600, 0)).soundValues(1, 1.6f, 0.4f));
		registry.register(new SpellProjectile("ice_charge", Tier.ADVANCED, Element.ICE, 20, 30, EntityIceCharge::new, 1.5f, WizardrySounds.SPELL_ICE).soundValues(1, 1.6f, 0.4f));
		registry.register(new FrostAxe());
		registry.register(new InvokeWeather());
		registry.register(new ChainLightning());
		registry.register(new LightningBolt());
		registry.register(new SpellMinion<>("summon_lightning_wraith", Tier.ADVANCED, Element.LIGHTNING, 40, 200, EntityLightningWraith::new, 600, SoundEvents.ENTITY_WITHER_AMBIENT).soundValues(1, 1.1f, 0.2f));
		registry.register(new SpellBuff("static_aura", Tier.ADVANCED, Element.LIGHTNING, SpellType.DEFENCE, 40, 250, WizardrySounds.SPELL_SPARK, 0, 0.5f, 0.7f, Triple.of(WizardryPotions.static_aura, 600, 0)).soundValues(1, 1.6f, 0.4f));
		registry.register(new SpellProjectile("lightning_disc", Tier.ADVANCED, Element.LIGHTNING, 25, 60, EntityLightningDisc::new, 1.2f, WizardrySounds.SPELL_LIGHTNING).soundValues(1, 0.95f, 0.3f));
		registry.register(new MindControl());
		registry.register(new SpellMinion<>("summon_wither_skeleton", Tier.ADVANCED, Element.NECROMANCY, 35, 150, EntityWitherSkeletonMinion::new, 600, WizardrySounds.SPELL_SUMMONING).soundValues(7, 0.6f, 0));
		registry.register(new Entrapment());
		registry.register(new WitherSkull());
		registry.register(new SpellProjectile("darkness_orb", Tier.ADVANCED, Element.NECROMANCY, 20, 20, EntityDarknessOrb::new, 0.5f, SoundEvents.ENTITY_WITHER_SHOOT).soundValues(0.5f, 0.4f, 0.2f));
		registry.register(new ShadowWard());
		registry.register(new Decay());
		registry.register(new SpellBuff("water_breathing", Tier.ADVANCED, Element.EARTH, SpellType.BUFF, 30, 250, WizardrySounds.SPELL_HEAL, 0.3f, 0.3f, 1, Triple.of(MobEffects.WATER_BREATHING, 1200, 0)){ @Override public boolean canBeCastByNPCs(){ return false; } }.soundValues(0.7f, 1.2f, 0.4f));
		registry.register(new Tornado());
		registry.register(new Glide());
		registry.register(new SummonSpiritHorse());
		registry.register(new SpellMinion<>("spider_swarm", Tier.ADVANCED, Element.EARTH, 45, 200, EntitySpiderMinion::new, 600, SoundEvents.BLOCK_FIRE_EXTINGUISH).soundValues(1, 1.1f, 0.1f).quantity(5).range(3));
		registry.register(new Slime());
		registry.register(new Petrify());
		registry.register(new SpellBuff("invisibility", Tier.ADVANCED, Element.SORCERY, SpellType.BUFF, 35, 200, WizardrySounds.SPELL_HEAL, 0.7f, 1, 1, Triple.of(MobEffects.INVISIBILITY, 600, 0)).soundValues(0.7f, 1.2f, 0.4f));
		registry.register(new Levitation());
		registry.register(new SpellProjectile("force_orb", Tier.ADVANCED, Element.SORCERY, 20, 20, EntityForceOrb::new, 1.5f, SoundEvents.ENTITY_SNOWBALL_THROW).soundValues(0.5f, 0.4f, 0.2f));
		registry.register(new Transportation());
		registry.register(new SpectralPathway());
		registry.register(new PhaseStep());
		registry.register(new VanishingBox());
		registry.register(new GreaterHeal());
		registry.register(new SpellConstruct<>("healing_aura", Tier.ADVANCED, Element.HEALING, SpellType.CONSTRUCT, 35, 150, EnumAction.BOW, EntityHealAura::new, 600, null));
		registry.register(new SpellConstruct<>("forcefield", Tier.ADVANCED, Element.HEALING, SpellType.DEFENCE, 45, 200, EnumAction.BOW, EntityForcefield::new, 600, WizardrySounds.SPELL_CONJURATION_LARGE));
		registry.register(new SpellBuff("ironflesh", Tier.ADVANCED, Element.HEALING, SpellType.DEFENCE, 30, 100, WizardrySounds.SPELL_HEAL, 0.4f, 0.5f, 0.6f, Triple.of(MobEffects.RESISTANCE, 600, 2)).soundValues(0.7f, 1.2f, 0.4f));
		registry.register(new Transience());

		registry.register(new Meteor());
		registry.register(new Firestorm());
		registry.register(new SpellMinion<>("summon_phoenix", Tier.MASTER, Element.FIRE, 150, 400, EntityPhoenix::new, 600, SoundEvents.ENTITY_WITHER_AMBIENT).soundValues(1, 1.1f, 0.1f));
		registry.register(new IceAge());
		registry.register(new WallOfFrost());
		registry.register(new SpellMinion<>("summon_ice_giant", Tier.MASTER, Element.ICE, 100, 400, EntityIceGiant::new, 600, WizardrySounds.SPELL_ICE).soundValues(1, 0.15f, 0.1f));
		registry.register(new Thunderstorm());
		registry.register(new LightningHammer());
		registry.register(new PlagueOfDarkness());
		registry.register(new SummonSkeletonLegion());
		registry.register(new SummonShadowWraith());
		registry.register(new ForestsCurse());
		registry.register(new Flight());
		registry.register(new SpellMinion<>("silverfish_swarm", Tier.MASTER, Element.EARTH, 80, 300, EntitySilverfishMinion::new, 600, SoundEvents.BLOCK_FIRE_EXTINGUISH).soundValues(1, 1.1f, 0.1f).quantity(20).range(3));
		registry.register(new SpellConstructRanged<>("black_hole", Tier.MASTER, Element.SORCERY, SpellType.CONSTRUCT, 150, 400, EntityBlackHole::new, 600, 10, SoundEvents.ENTITY_WITHER_SPAWN).soundValues(2, 0.7f, 0));
		registry.register(new Shockwave());
		registry.register(new SummonIronGolem());
		registry.register(new ArrowRain());
		registry.register(new SpellBuff("diamondflesh", Tier.MASTER, Element.HEALING, SpellType.DEFENCE, 100, 300, WizardrySounds.SPELL_HEAL, 0.1f, 0.7f, 1, Triple.of(MobEffects.RESISTANCE, 600, 5)).soundValues(0.7f, 1.2f, 0.4f));
		registry.register(new SpellBuff("font_of_vitality", Tier.MASTER, Element.HEALING, SpellType.DEFENCE, 75, 300, WizardrySounds.SPELL_HEAL, 1, 0.8f, 0.3f, Triple.of(MobEffects.ABSORPTION, 1200, 1), Triple.of(MobEffects.REGENERATION, 300, 1)).soundValues(0.7f, 1.2f, 0.4f));

		// Wizardry 1.1 spells

		registry.register(new SpellProjectile("smoke_bomb", Tier.BASIC, Element.FIRE, 10, 20, EntitySmokeBomb::new, 1.5f, SoundEvents.ENTITY_SNOWBALL_THROW).soundValues(0.5f, 0.4f, 0.2f));
		registry.register(new MindTrick());
		registry.register(new Leap());

		registry.register(new PocketFurnace());
		registry.register(new Intimidate());
		registry.register(new Banish());
		registry.register(new SixthSense());
		registry.register(new SpellBuff("darkvision", Tier.APPRENTICE, Element.EARTH, SpellType.BUFF, 20, 40, WizardrySounds.SPELL_HEAL, 0, 0.4f, 0.7f, Triple.of(MobEffects.NIGHT_VISION, 900, 0)){ @Override public boolean canBeCastByNPCs(){ return false; } }.soundValues(0.7f, 1.2f, 0.4f));
		registry.register(new Clairvoyance());
		registry.register(new PocketWorkbench());
		registry.register(new ImbueWeapon());
		registry.register(new InvigoratingPresence());
		registry.register(new SpellBuff("oakflesh", Tier.ADVANCED, Element.HEALING, SpellType.DEFENCE, 20, 50, WizardrySounds.SPELL_HEAL, 0.6f, 0.5f, 0.4f, Triple.of(MobEffects.RESISTANCE, 600, 1)).soundValues(0.7f, 1.2f, 0.4f));

		registry.register(new GreaterFireball());
		registry.register(new FlamingWeapon());
		registry.register(new SpellArrow("ice_lance", Tier.ADVANCED, Element.ICE, 20, 20, EntityIceLance::new, 2, WizardrySounds.SPELL_ICE).soundValues(1, 1, 0.4f));
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
		registry.register(new SpellMinion<>("summon_storm_elemental", Tier.MASTER, Element.LIGHTNING, 100, 400, EntityStormElemental::new, 600, SoundEvents.ENTITY_WITHER_AMBIENT).soundValues(1, 1.1f, 0.1f));
		registry.register(new Earthquake());
		registry.register(new FontOfMana());
	}

}