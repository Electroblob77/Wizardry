package electroblob.wizardry.registry;

import electroblob.wizardry.Wizardry;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Class responsible for defining, storing and registering all of wizardry's sound events.
 * 
 * @author Electroblob
 * @since Wizardry 2.1
 */
@Mod.EventBusSubscriber
public final class WizardrySounds {

	private WizardrySounds(){} // No instances!

	/** Sound category for all spell-related sounds. This includes inanimate magical entities, but not minions.
	 * @see electroblob.wizardry.util.CustomSoundCategory */
	public static SoundCategory SPELLS;

	private static final List<SoundEvent> sounds = new ArrayList<>();
	
	public static final SoundEvent BLOCK_ARCANE_WORKBENCH_SPELLBIND = createSound("block.arcane_workbench.bind_spell");
	public static final SoundEvent BLOCK_PEDESTAL_ACTIVATE = 		createSound("block.pedestal.activate");
	public static final SoundEvent BLOCK_PEDESTAL_CONQUER = 		createSound("block.pedestal.conquer");
	public static final SoundEvent BLOCK_LECTERN_LOCATE_SPELL = 	createSound("block.lectern.locate_spell");
	public static final SoundEvent BLOCK_RECEPTACLE_IGNITE = 		createSound("block.receptacle.ignite");
	public static final SoundEvent BLOCK_IMBUEMENT_ALTAR_IMBUE = 	createSound("block.imbuement_altar.imbue");

	public static final SoundEvent ITEM_WAND_SWITCH_SPELL = 		createSound("item.wand.switch_spell");
	public static final SoundEvent ITEM_WAND_LEVELUP = 				createSound("item.wand.levelup");
	public static final SoundEvent ITEM_WAND_MELEE = 				createSound("item.wand.melee");
	public static final SoundEvent ITEM_WAND_CHARGEUP = 			createSound("item.wand.chargeup");
	public static final SoundEvent ITEM_ARMOUR_EQUIP_SILK = 		createSound("item.armour.equip_silk");
	public static final SoundEvent ITEM_PURIFYING_ELIXIR_DRINK = 	createSound("item.purifying_elixir.drink");
	public static final SoundEvent ITEM_MANA_FLASK_USE = 			createSound("item.mana_flask.use");
	public static final SoundEvent ITEM_MANA_FLASK_RECHARGE = 		createSound("item.mana_flask.recharge");
	public static final SoundEvent ITEM_FLAMECATCHER_SHOOT = 		createSound("item.flamecatcher.shoot");
	public static final SoundEvent ITEM_FLAMECATCHER_FLAME = 		createSound("item.flamecatcher.flame");

	public static final SoundEvent ENTITY_BLACK_HOLE_AMBIENT = 		createSound("entity.black_hole.ambient");
	public static final SoundEvent ENTITY_BLACK_HOLE_VANISH = 		createSound("entity.black_hole.vanish");
	public static final SoundEvent ENTITY_BLACK_HOLE_BREAK_BLOCK = 	createSound("entity.black_hole.break_block");
	public static final SoundEvent ENTITY_BLIZZARD_AMBIENT = 		createSound("entity.blizzard.ambient");
	public static final SoundEvent ENTITY_BOULDER_ROLL = 			createSound("entity.boulder.roll");
	public static final SoundEvent ENTITY_BOULDER_LAND = 			createSound("entity.boulder.land");
	public static final SoundEvent ENTITY_BOULDER_HIT = 			createSound("entity.boulder.hit");
	public static final SoundEvent ENTITY_BOULDER_BREAK_BLOCK = 	createSound("entity.boulder.break_block");
	public static final SoundEvent ENTITY_BUBBLE_POP = 				createSound("entity.bubble.pop");
	public static final SoundEvent ENTITY_DECAY_AMBIENT = 			createSound("entity.decay.ambient");
	public static final SoundEvent ENTITY_ENTRAPMENT_AMBIENT = 		createSound("entity.entrapment.ambient");
	public static final SoundEvent ENTITY_ENTRAPMENT_VANISH = 		createSound("entity.entrapment.vanish");
	public static final SoundEvent ENTITY_FIRE_RING_AMBIENT = 		createSound("entity.fire_ring.ambient");
	public static final SoundEvent ENTITY_FIRE_SIGIL_TRIGGER = 		createSound("entity.fire_sigil.trigger");
	public static final SoundEvent ENTITY_FORCEFIELD_AMBIENT = 		createSound("entity.forcefield.ambient");
	public static final SoundEvent ENTITY_FORCEFIELD_DEFLECT = 		createSound("entity.forcefield.deflect");
	public static final SoundEvent ENTITY_FROST_SIGIL_TRIGGER = 	createSound("entity.frost_sigil.trigger");
	public static final SoundEvent ENTITY_HAMMER_ATTACK = 			createSound("entity.hammer.attack");
	public static final SoundEvent ENTITY_HAMMER_EXPLODE = 			createSound("entity.hammer.explode");
	public static final SoundEvent ENTITY_HAMMER_THROW = 			createSound("entity.hammer.throw");
	public static final SoundEvent ENTITY_HAMMER_LAND = 			createSound("entity.hammer.land");
	public static final SoundEvent ENTITY_HEAL_AURA_AMBIENT = 		createSound("entity.heal_aura.ambient");
	public static final SoundEvent ENTITY_ICE_BARRIER_DEFLECT = 	createSound("entity.ice_barrier.deflect");
	public static final SoundEvent ENTITY_ICE_BARRIER_EXTEND = 		createSound("entity.ice_barrier.extend");
	public static final SoundEvent ENTITY_ICE_SPIKE_EXTEND = 		createSound("entity.ice_spike.extend");
	public static final SoundEvent ENTITY_LIGHTNING_SIGIL_TRIGGER = createSound("entity.lightning_sigil.trigger");
	public static final SoundEvent ENTITY_METEOR_FALLING = 			createSound("entity.meteor.falling");
	public static final SoundEvent ENTITY_RADIANT_TOTEM_AMBIENT = 	createSound("entity.radiant_totem.ambient");
	public static final SoundEvent ENTITY_RADIANT_TOTEM_VANISH = 	createSound("entity.radiant_totem.vanish");
	public static final SoundEvent ENTITY_SHIELD_DEFLECT = 			createSound("entity.shield.deflect");
	public static final SoundEvent ENTITY_TORNADO_AMBIENT = 		createSound("entity.tornado.ambient");
	public static final SoundEvent ENTITY_WITHERING_TOTEM_AMBIENT = createSound("entity.withering_totem.ambient");
	public static final SoundEvent ENTITY_WITHERING_TOTEM_EXPLODE = createSound("entity.withering_totem.explode");
	public static final SoundEvent ENTITY_ZOMBIE_SPAWNER_SPAWN = 	createSound("entity.zombie_spawner.spawn");

	public static final SoundEvent ENTITY_EVIL_WIZARD_AMBIENT = 	createSound("entity.evil_wizard.ambient");
	public static final SoundEvent ENTITY_EVIL_WIZARD_HURT = 		createSound("entity.evil_wizard.hurt");
	public static final SoundEvent ENTITY_EVIL_WIZARD_DEATH = 		createSound("entity.evil_wizard.death");
	public static final SoundEvent ENTITY_ICE_GIANT_ATTACK = 		createSound("entity.ice_giant.attack");
	public static final SoundEvent ENTITY_ICE_GIANT_DESPAWN = 		createSound("entity.ice_giant.despawn");
	public static final SoundEvent ENTITY_ICE_WRAITH_AMBIENT = 		createSound("entity.ice_wraith.ambient");
	public static final SoundEvent ENTITY_MAGIC_SLIME_ATTACK = 		createSound("entity.magic_slime.attack");
	public static final SoundEvent ENTITY_MAGIC_SLIME_EXPLODE = 	createSound("entity.magic_slime.explode");
	public static final SoundEvent ENTITY_MAGIC_SLIME_SPLAT = 		createSound("entity.magic_slime.splat");
	public static final SoundEvent ENTITY_PHOENIX_AMBIENT = 		createSound("entity.phoenix.ambient");
	public static final SoundEvent ENTITY_PHOENIX_BURN = 			createSound("entity.phoenix.burn");
	public static final SoundEvent ENTITY_PHOENIX_FLAP = 			createSound("entity.phoenix.flap");
	public static final SoundEvent ENTITY_PHOENIX_HURT = 			createSound("entity.phoenix.hurt");
	public static final SoundEvent ENTITY_PHOENIX_DEATH = 			createSound("entity.phoenix.death");
	public static final SoundEvent ENTITY_REMNANT_AMBIENT = 		createSound("entity.remnant.ambient");
	public static final SoundEvent ENTITY_REMNANT_HURT = 			createSound("entity.remnant.hurt");
	public static final SoundEvent ENTITY_REMNANT_DEATH = 			createSound("entity.remnant.death");
	public static final SoundEvent ENTITY_SHADOW_WRAITH_AMBIENT = 	createSound("entity.shadow_wraith.ambient");
	public static final SoundEvent ENTITY_SHADOW_WRAITH_NOISE = 	createSound("entity.shadow_wraith.noise");
	public static final SoundEvent ENTITY_SHADOW_WRAITH_HURT = 		createSound("entity.shadow_wraith.hurt");
	public static final SoundEvent ENTITY_SHADOW_WRAITH_DEATH = 	createSound("entity.shadow_wraith.death");
	public static final SoundEvent ENTITY_SPIRIT_HORSE_VANISH = 	createSound("entity.spirit_horse.vanish");
	public static final SoundEvent ENTITY_SPIRIT_WOLF_VANISH = 		createSound("entity.spirit_wolf.vanish");
	public static final SoundEvent ENTITY_STORM_ELEMENTAL_AMBIENT = createSound("entity.storm_elemental.ambient");
	public static final SoundEvent ENTITY_STORM_ELEMENTAL_BURN = 	createSound("entity.storm_elemental.burn");
	public static final SoundEvent ENTITY_STORM_ELEMENTAL_WIND = 	createSound("entity.storm_elemental.wind");
	public static final SoundEvent ENTITY_STORM_ELEMENTAL_HURT = 	createSound("entity.storm_elemental.hurt");
	public static final SoundEvent ENTITY_STORM_ELEMENTAL_DEATH = 	createSound("entity.storm_elemental.death");
	public static final SoundEvent ENTITY_STORMCLOUD_THUNDER = 		createSound("entity.stormcloud.thunder");
	public static final SoundEvent ENTITY_STORMCLOUD_ATTACK = 		createSound("entity.stormcloud.attack");
	public static final SoundEvent ENTITY_WIZARD_YES = 				createSound("entity.wizard.yes");
	public static final SoundEvent ENTITY_WIZARD_NO = 				createSound("entity.wizard.no");
	public static final SoundEvent ENTITY_WIZARD_AMBIENT = 			createSound("entity.wizard.ambient");
	public static final SoundEvent ENTITY_WIZARD_TRADING = 			createSound("entity.wizard.trading");
	public static final SoundEvent ENTITY_WIZARD_HURT = 			createSound("entity.wizard.hurt");
	public static final SoundEvent ENTITY_WIZARD_DEATH = 			createSound("entity.wizard.death");
	public static final SoundEvent ENTITY_WIZARD_HOHOHO = 			createSound("entity.wizard.hohoho");

	public static final SoundEvent ENTITY_DARKNESS_ORB_HIT = 		createSound("entity.darkness_orb.hit");
	public static final SoundEvent ENTITY_DART_HIT = 				createSound("entity.dart.hit");
	public static final SoundEvent ENTITY_DART_HIT_BLOCK = 			createSound("entity.dart.hit_block");
	public static final SoundEvent ENTITY_FIREBOLT_HIT = 			createSound("entity.firebolt.hit");
	public static final SoundEvent ENTITY_FIREBOMB_THROW = 			createSound("entity.firebomb.throw");
	public static final SoundEvent ENTITY_FIREBOMB_SMASH = 			createSound("entity.firebomb.smash");
	public static final SoundEvent ENTITY_FIREBOMB_FIRE = 			createSound("entity.firebomb.fire");
	public static final SoundEvent ENTITY_FLAMECATCHER_ARROW_HIT = 	createSound("entity.flamecatcher_arrow.hit");
	public static final SoundEvent ENTITY_FORCE_ARROW_HIT = 		createSound("entity.force_arrow.hit");
	public static final SoundEvent ENTITY_FORCE_ORB_HIT = 			createSound("entity.force_orb.hit");
	public static final SoundEvent ENTITY_FORCE_ORB_HIT_BLOCK = 	createSound("entity.force_orb.hit_block");
	public static final SoundEvent ENTITY_ICEBALL_HIT = 			createSound("entity.iceball.hit");
	public static final SoundEvent ENTITY_ICE_CHARGE_SMASH = 		createSound("entity.ice_charge.smash");
	public static final SoundEvent ENTITY_ICE_CHARGE_ICE = 			createSound("entity.ice_charge.ice");
	public static final SoundEvent ENTITY_ICE_LANCE_SMASH = 		createSound("entity.ice_lance.smash");
	public static final SoundEvent ENTITY_ICE_LANCE_HIT = 			createSound("entity.ice_lance.hit");
	public static final SoundEvent ENTITY_ICE_SHARD_SMASH = 		createSound("entity.ice_shard.smash");
	public static final SoundEvent ENTITY_ICE_SHARD_HIT = 			createSound("entity.ice_shard.hit");
	public static final SoundEvent ENTITY_LIGHTNING_ARROW_HIT = 	createSound("entity.lightning_arrow.hit");
	public static final SoundEvent ENTITY_LIGHTNING_DISC_HIT = 		createSound("entity.lightning_disc.hit");
//	public static final SoundEvent ENTITY_MAGIC_FIREBALL_HIT = 		createSound("entity.magic_fireball.hit");
	public static final SoundEvent ENTITY_MAGIC_MISSILE_HIT = 		createSound("entity.magic_missile.hit");
	public static final SoundEvent ENTITY_POISON_BOMB_THROW = 		createSound("entity.poison_bomb.throw");
	public static final SoundEvent ENTITY_POISON_BOMB_SMASH = 		createSound("entity.poison_bomb.smash");
	public static final SoundEvent ENTITY_POISON_BOMB_POISON = 		createSound("entity.poison_bomb.poison");
	public static final SoundEvent ENTITY_SMOKE_BOMB_THROW = 		createSound("entity.smoke_bomb.throw");
	public static final SoundEvent ENTITY_SMOKE_BOMB_SMASH = 		createSound("entity.smoke_bomb.smash");
	public static final SoundEvent ENTITY_SMOKE_BOMB_SMOKE = 		createSound("entity.smoke_bomb.smoke");
	public static final SoundEvent ENTITY_HOMING_SPARK_HIT = 		createSound("entity.homing_spark.hit");
	public static final SoundEvent ENTITY_SPARK_BOMB_THROW = 		createSound("entity.spark_bomb.throw");
	public static final SoundEvent ENTITY_SPARK_BOMB_HIT = 			createSound("entity.spark_bomb.hit");
	public static final SoundEvent ENTITY_SPARK_BOMB_HIT_BLOCK = 	createSound("entity.spark_bomb.hit_block");
	public static final SoundEvent ENTITY_SPARK_BOMB_CHAIN = 		createSound("entity.spark_bomb.chain");
	public static final SoundEvent ENTITY_THUNDERBOLT_HIT = 		createSound("entity.thunderbolt.hit");

	public static final SoundEvent SPELL_STATIC_AURA_RETALIATE = 	createSound("spell.static_aura.retaliate");
	public static final SoundEvent SPELL_CURSE_OF_SOULBINDING_RETALIATE = createSound("spell.curse_of_soulbinding.retaliate");
	public static final SoundEvent SPELL_TRANSPORTATION_TRAVEL = 	createSound("spell.transportation.travel");

	public static final SoundEvent MISC_DISCOVER_SPELL = 			createSound("misc.discover_spell");
	public static final SoundEvent MISC_BOOK_OPEN = 				createSound("misc.book_open");
	public static final SoundEvent MISC_PAGE_TURN = 				createSound("misc.page_turn");
	public static final SoundEvent MISC_FREEZE = 					createSound("misc.freeze");
	public static final SoundEvent MISC_SPELL_FAIL = 				createSound("misc.spell_fail");

	// Trick borrowed from the Twilight Forest, makes things neater.

	/** Overload for {@link WizardrySounds#createSound(String, String)} which assigns wizardry's mod ID automatically. */
	public static SoundEvent createSound(String name){
		return createSound(Wizardry.MODID, name);
	}

	/** Creates a sound with the given name, to be read from {@code assets/[modID]/sounds.json}. The sound will be
	 * registered automatically later. */
	public static SoundEvent createSound(String modID, String name){
		// All the setRegistryName methods delegate to this one, it doesn't matter which you use.
		SoundEvent sound = new SoundEvent(new ResourceLocation(modID, name)).setRegistryName(name);
		sounds.add(sound);
		return sound;
	}

	// For some reason, sound events seem to work even when they aren't registered, without even so much as a warning.
	
	@SubscribeEvent
	public static void register(RegistryEvent.Register<SoundEvent> event){
		event.getRegistry().registerAll(sounds.toArray(new SoundEvent[0]));
	}

}