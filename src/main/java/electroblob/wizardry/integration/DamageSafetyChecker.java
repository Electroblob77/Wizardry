package electroblob.wizardry.integration;

import com.google.common.collect.ImmutableSet;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.util.EntityUtils;
import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.Set;

/**
 * This class implements an 'if-all-else-fails' fix for cross-mod infinite looping caused by re-applying damage in
 * attack events (see <a href=https://github.com/Electroblob77/Wizardry/issues/72>GitHub issue #72</a> for details).
 * The methods in this class should <b>only</b> be used when intercepting one of the attack/damage events and
 * dealing damage from within it.
 */
@Mod.EventBusSubscriber
public final class DamageSafetyChecker {

	/** We don't want to tell users to add any of these to the blacklist, as that would be problematic. */
	// NOTE: Make sure this is updated for each new version of Minecraft
	private static final Set<String> VANILLA_DAMAGE_NAMES =  ImmutableSet.of("inFire", "lightningBolt", "onFire",
			"lava", "hotFloor", "inWall", "cramming", "drown", "starve", "cactus", "fall", "flyIntoWall", "outOfWorld",
			"generic", "magic", "wither", "anvil", "fallingBlock", "dragonBreath", "fireworks", "mob", "player", "arrow",
			"thrown", "indirectMagic", "thorns", "explosion", "explosion.player");

	/**
	 * Global counter which is incremented once for each call to
	 * {@link DamageSafetyChecker#attackEntitySafely(Entity, DamageSource, float, String, DamageSource, boolean)}
	 * This allows for detection and avoidance of imminent StackOverflowErrors caused by looping between mods.
	 */
	private static int attacksThisTick = 0;

	/** The number of calls per loaded entity after which damage will be reassigned. */
	// It's a fair bet that if an entity is being damaged 15 times in a single tick, then something is wrong!
	// Based on the crash report in issue #72, there were approximately 38 calls before the error was thrown.
	// The number of calls will vary depending on the stack size and possibly what else is happening at the time.
 	private static final int EXCESSIVE_CALL_THRESHOLD = 15;
	/** The number of calls per loaded entity after which damage will be cancelled entirely. */
	private static final int EXCESSIVE_CALL_LIMIT = 25;

	/**
	 * Attacks the specified target with specified damage source and damage amount, checking for the blacklist and
	 * excessive looping in the process. Under normal circumstances, this method simply calls
	 * {@code target.attackEntityFrom(...)}. If excessive looping is detected, the damage source is substituted for
	 * the given fallback instead, and a warning is printed to the console.
	 * <p></p>
	 * This method should only be used within the attack events (LivingAttackEvent, LivingHurtEvent, LivingDamageEvent
	 * and possibly LivingKnockBackEvent, depending on the circumstances).
	 * @param target The target to apply the damage to.
	 * @param source The source of the damage.
	 * @param damage The amount of damage to be applied.
	 * @param originalSourceName The string identifier for the original damage source (i.e. the one being replaced).
	 *                           This allows wizardry to request that users add it to the blacklist.
	 * @param fallback The fallback damage source for when excessive looping is detected. This <b>must not</b> be the
	 *                 same as the re-applied source, or this method is pointless! Usually it will be more general.
	 * @param knockback True to apply knockback as normal, false to use the knockback-free methods in WizardryUtilities
	 * (see {@link EntityUtils#attackEntityWithoutKnockback(Entity, DamageSource, float)}).
	 */
	public static boolean attackEntitySafely(Entity target, DamageSource source, float damage,
											 String originalSourceName, DamageSource fallback, boolean knockback){

		for(String sourceName : Wizardry.settings.damageSourceBlacklist){
			if(originalSourceName.equals(sourceName)){
				// Blacklist behaviour
				// Same as fallback behaviour, but without the log message
				// No harm in still incrementing the counter
				attacksThisTick++;
				return knockback ? target.attackEntityFrom(fallback, damage)
						: EntityUtils.attackEntityWithoutKnockback(target, fallback, damage);
			}
		}

		if(attacksThisTick > EXCESSIVE_CALL_LIMIT * target.world.loadedEntityList.size()){
			// This should never ever happen unless another mod is intercepting non-entity-based damage and damaging
			// the same target.
			logInterception(originalSourceName, true);
			return false;
		}

		if(attacksThisTick > EXCESSIVE_CALL_THRESHOLD * target.world.loadedEntityList.size()){
			// Sometimes this is unavoidable, it's neither mod's fault but without some kind of forge standard or
			// universal cooperation there's no easy way to prevent it.
			logInterception(originalSourceName, false);
			// Fallback behaviour
			attacksThisTick++;
			return knockback ? target.attackEntityFrom(fallback, damage)
					: EntityUtils.attackEntityWithoutKnockback(target, fallback, damage);

		}else{
			// Normal behaviour
			attacksThisTick++;
			return knockback ? target.attackEntityFrom(source, damage)
					: EntityUtils.attackEntityWithoutKnockback(target, source, damage);
		}
	}

	/**
	 * See {@link DamageSafetyChecker#attackEntitySafely(Entity, DamageSource, float, String, DamageSource, boolean)}.
	 * This version is for when the source being replaced is used as a fallback, i.e. when a damage source is being
	 * replaced for technical reasons (e.g. summoned creatures) rather than as part of a game mechanic (e.g. shadow ward).
	 * This means that if excessive looping is detected, the code will work as if the event was never intercepted.
	 */
	public static boolean attackEntitySafely(Entity target, DamageSource source, float damage, DamageSource originalSource, boolean knockback){
		return attackEntitySafely(target, source, damage, originalSource.getDamageType(), originalSource, knockback);
	}

	/**
	 * See {@link DamageSafetyChecker#attackEntitySafely(Entity, DamageSource, float, String, DamageSource, boolean)}.
	 * Fallback defaults to {@link DamageSource#MAGIC} and knockback defaults to true.
	 */
	public static boolean attackEntitySafely(Entity target, DamageSource source, float damage, String originalSourceName){
		return attackEntitySafely(target, source, damage, originalSourceName, DamageSource.MAGIC, true);
	}

	/** Prints the appropriate message about the damage interception to the console. */
	private static void logInterception(String originalSourceName, boolean aborted){

		if(!Wizardry.settings.compatibilityWarnings) return; // No warnings if they're disabled!

		boolean vanillaName = VANILLA_DAMAGE_NAMES.contains(originalSourceName);

		if(aborted){
			Wizardry.logger.warn("Entity attack excessive call limit reached, aborting entity damage entirely!");
		}else{
			Wizardry.logger.warn("Entity attack excessive call threshold reached, substituting for non-entity-based " +
					"damage to avert a crash.");
		}

		if(vanillaName){
			Wizardry.logger.info("The damage source in question had a vanilla identifier. If you know which mod may " +
					"have caused this, consider asking the author to add a custom identifier so it may be blacklisted. " +
					"You can turn this warning off using the compatibilityWarnings config option. Please do not report " +
					"it to wizardry's author.");
		}else{
			Wizardry.logger.info("To prevent this message and improve efficiency, add \"" + originalSourceName + "\" " +
					"(without quotes) to the damage source blacklist in the config. Please do not report " +
					"this warning unless you have added the damage source to the blacklist already.");
		}
	}

	@SubscribeEvent
	public static void tick(TickEvent event){
		// We actually want this to fire on both sides, because attacks are common code.
		if(event.phase == TickEvent.Phase.START && event.type == TickEvent.Type.WORLD){
			attacksThisTick = 0; // Reset the attack call counter
		}
	}

}
