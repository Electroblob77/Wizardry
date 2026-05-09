package electroblob.wizardry.util;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.spell.MindControl;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiPredicate;

/**
 * Contains some useful static methods for interacting with the ally designation system. Also handles the friendly fire
 * setting. This was split off from {@code WizardryUtilities} as of wizardry 4.2 in an effort to make the code easier to
 * navigate.
 * <p></p>
 * <b>Addon API:</b> External mods can extend the ally system without modifying wizardry by registering predicates via
 * {@link #registerValidTargetPredicate(BiPredicate)} and {@link #registerAllyPredicate(BiPredicate)}. Predicates are
 * registered once at mod initialisation and called on every ally check with the live entity references, so any
 * dynamic ownership model (NBT, capabilities, in-memory maps) is supported. Predicates run after all built-in
 * wizardry logic and are additive — they can protect additional entity pairs but cannot remove existing protections
 * such as direct ownership or scoreboard teams.
 */
@Mod.EventBusSubscriber
public final class AllyDesignationSystem {

	private AllyDesignationSystem(){} // No instances!

	/**
	 * Predicates registered by external mods for {@link #isValidTarget}. Using {@link CopyOnWriteArrayList} so that
	 * registration from any thread during mod initialisation is safe, while iteration on the server tick thread is
	 * lock-free.
	 */
	private static final List<BiPredicate<Entity, Entity>> validTargetPredicates = new CopyOnWriteArrayList<>();

	/**
	 * Predicates registered by external mods for {@link #isAllied}. See {@link #validTargetPredicates}.
	 */
	private static final List<BiPredicate<EntityLivingBase, EntityLivingBase>> allyPredicates = new CopyOnWriteArrayList<>();

	/**
	 * Registers a predicate that extends the ally check performed by {@link #isValidTarget(Entity, Entity)}.
	 * <p></p>
	 * Call this method <b>once</b> during your mod's initialisation (e.g. {@code FMLInitializationEvent}). The
	 * predicate is stored statically and invoked on every {@code isValidTarget} call, so it must be cheap — avoid
	 * world queries or heavy computation inside the lambda.
	 * <p></p>
	 * <b>Predicate semantics:</b> return {@code true} to declare that {@code target} should be treated as an ally of
	 * {@code attacker} and therefore must not be attacked. Return {@code false} to defer to other registered
	 * predicates or the default result.
	 * <p></p>
	 * <b>Scope:</b> Predicates run after all built-in wizardry rules (direct ownership, scoreboard teams, player ally
	 * list, mind-control, etc.) and only when none of those rules already produced a result. They are therefore
	 * additive — they can protect additional pairs but cannot override existing protections.
	 * <p></p>
	 * <b>Recursion note:</b> {@code isValidTarget} recurses automatically for owned attackers (e.g. a summoned zombie
	 * delegates to its owner). Predicates are only invoked at the root of that recursion (i.e. when the attacker is
	 * not itself an {@link IEntityOwnable}), so each logical query triggers the predicate exactly once regardless of
	 * ownership chain depth.
	 * <p></p>
	 * <b>Example</b> — protecting minions of allied players in an external mod that tracks ownership via a capability:
	 * <pre>{@code
	 * AllyDesignationSystem.registerValidTargetPredicate((attacker, target) -> {
	 *     UUID summonerOfTarget = MyMod.getOwnerUUID(target);   // reads your capability / NBT
	 *     if (summonerOfTarget == null) return false;            // not one of our mobs
	 *     UUID summonerOfAttacker = MyMod.getOwnerUUID(attacker);
	 *     if (summonerOfAttacker == null) return false;
	 *     // Protect if both are owned by the same player, or by players who are wizardry allies
	 *     if (summonerOfTarget.equals(summonerOfAttacker)) return true;
	 *     EntityPlayer ownerA = ...; EntityPlayer ownerB = ...;
	 *     return AllyDesignationSystem.isPlayerAlly(ownerA, ownerB);
	 * });
	 * }</pre>
	 *
	 * @param predicate A {@link BiPredicate} receiving {@code (attacker, target)}. Must be non-null and thread-safe.
	 */
	public static void registerValidTargetPredicate(BiPredicate<Entity, Entity> predicate){
		validTargetPredicates.add(predicate);
	}

	/**
	 * Registers a predicate that extends the ally check performed by {@link #isAllied(EntityLivingBase, EntityLivingBase)}.
	 * <p></p>
	 * Call this method <b>once</b> during your mod's initialisation (e.g. {@code FMLInitializationEvent}). The
	 * predicate is stored statically and invoked on every {@code isAllied} call.
	 * <p></p>
	 * <b>Predicate semantics:</b> return {@code true} to declare that {@code possibleAlly} should be considered allied
	 * with {@code allyOf}. Returning {@code true} affects any system that calls {@code isAllied}, including healing
	 * AoE spells, {@code EntityRadiantTotem}, and the friendly-fire event handler. Return {@code false} to defer.
	 * <p></p>
	 * <b>Scope:</b> Same additive-only guarantee as {@link #registerValidTargetPredicate} — predicates run after all
	 * built-in rules and cannot remove existing ally relationships.
	 * <p></p>
	 * <b>Example</b> — treating faction members as allies for healing spells:
	 * <pre>{@code
	 * AllyDesignationSystem.registerAllyPredicate((allyOf, possibleAlly) ->
	 *     MyFactionMod.sameFaction(allyOf, possibleAlly)
	 * );
	 * }</pre>
	 *
	 * @param predicate A {@link BiPredicate} receiving {@code (allyOf, possibleAlly)}. Must be non-null and thread-safe.
	 */
	public static void registerAllyPredicate(BiPredicate<EntityLivingBase, EntityLivingBase> predicate){
		allyPredicates.add(predicate);
	}

	/** Set of constants for each of the four friendly fire settings. */
	public enum FriendlyFire {

		ALL("All", false, false),
		ONLY_PLAYERS("Only players", false, true),
		ONLY_OWNED("Only summoned/tamed creatures", true, false),
		NONE("None", true, true);

		/** Constant array storing the names of each of the constants, in the order they are declared. */
		public static final String[] names;

		static {
			names = new String[values().length];
			for(FriendlyFire setting : values()){
				names[setting.ordinal()] = setting.name;
			}
		}

		/** The readable name for this friendly fire setting that will be displayed on the button in the config GUI. */
		public final String name;
		public final boolean blockPlayers;
		public final boolean blockOwned;

		FriendlyFire(String name, boolean blockPlayers, boolean blockOwned){
			this.name = name;
			this.blockPlayers = blockPlayers;
			this.blockOwned = blockOwned;
		}

		/**
		 * Gets a friendly fire setting from its string name (ignoring case), or ALL if the given name is not a valid
		 * setting.
		 */
		public static FriendlyFire fromName(String name){

			for(FriendlyFire setting : values()){
				if(setting.name.equalsIgnoreCase(name)) return setting;
			}

			Wizardry.logger.info("Invalid string for the friendly fire setting. Using default (all) instead.");
			return ALL;
		}

	}

	/**
	 * Returns whether the given target can be attacked by the given attacker. It is up to the caller of this method to
	 * work out what this means; it doesn't necessarily mean the target is completely immune (for example, revenge
	 * targeting might reasonably bypass this). This method is intended for use where the damage is indirect and/or
	 * unavoidable; direct attacks should not check this method. Currently this means the following situations check
	 * this method:
	 * <p></p>
	 * - AI targeting for summoned creatures<br>
	 * - AI targeting for mind-controlled creatures<br>
	 * - Constructs with an area of effect<br>
	 * - Instantaneous spells with an area of effect around the caster (e.g. forest's curse, thunderstorm)<br>
	 * - Any lightning chaining effects<br>
	 * - Any projectiles which seek targets
	 * <p></p>
	 * Also note that the friendly fire option is dealt with in the event handler. This method acts as a sort of wrapper
	 * for all the AllyDesignationSystem stuff in {@link WizardData}; more details about the ally designation system can be found there.
	 *
	 * @param attacker The entity that cast the spell originally
	 * @param target The entity being attacked
	 *
	 * @return False under any of the following circumstances, true otherwise:
	 *         <p></p>
	 *         - The target is null
	 *         <p></p>
	 *         - The target is the attacker (this isn't as stupid as it sounds - anything with an AoE might cause this
	 *         to be true, as can summoned creatures)
	 *         <p></p>
	 *         - The target and the attacker are both players and the target is an ally of the attacker (but the
	 *         attacker need not be an ally of the target)
	 *         <p></p>
	 *         - The target is a creature that was summoned/controlled by the attacker or by an ally of the attacker.
	 *         <p></p>
	 *         - The target is a creature that was tamed by the attacker or by an ally of the attacker
	 *         (see {@link net.minecraft.entity.IEntityOwnable}).
	 *         <p></p>
	 *         <i>As of wizardry 4.1.2, this method now returns <b>true</b> instead of false if the attacker is null. This
	 *         is because in the vast majority of cases, it makes more sense this way: if a construct has no caster, it
	 *         should affect all entities; if a minion has no caster it should target all entities; etc.</i>
	 */
	public static boolean isValidTarget(Entity attacker, Entity target){

		// Owned entities inherit their owner's allies
		if(attacker instanceof IEntityOwnable && !isValidTarget(((IEntityOwnable)attacker).getOwner(), target)) return false;

		// Always return false if the target is null
		if(target == null) return false;

		// Always return true if the attacker is null - this must be after the target null check!
		if(attacker == null) return true;

		// Teammates are allies
		if(attacker.isOnSameTeam(target)) return false;

		// Tests whether the target is the attacker
		if(target == attacker) return false;

		// I really shouldn't need to do this, but fake players seem to break stuff...
		if(target instanceof FakePlayer) return false;

		// Use a positive check for these rather than a negative check for monsters, because we only want mobs
		// that are definitely passive
		if(Wizardry.settings.passiveMobsAreAllies && (target.isCreatureType(EnumCreatureType.AMBIENT, false)
				|| target.isCreatureType(EnumCreatureType.CREATURE, false)
				|| target.isCreatureType(EnumCreatureType.WATER_CREATURE, false))){
			return false;
		}

		// Tests whether the target is a creature that was summoned by the attacker
//		if(target instanceof ISummonedCreature && ((ISummonedCreature)target).getCaster() == attacker){
//			return false;
//		}

		// Tests whether the target is a creature that was summoned/tamed (or is otherwise owned) by the attacker
		if(target instanceof IEntityOwnable && ((IEntityOwnable)target).getOwner() == attacker){
			return false;
		}

		// Tests whether the target is a creature that was summoned/tamed (or is otherwise owned) by the attacker
		if(target instanceof IEntityOwnable && attacker instanceof EntityLiving && !(((EntityLiving)attacker).getRevengeTarget() == ((IEntityOwnable)target).getOwner() || ((EntityLiving)attacker).getAttackTarget() == ((IEntityOwnable)target).getOwner())){
			return false;
		}

		// Tests whether the target is a creature that was mind controlled by the attacker
		if(target instanceof EntityLiving && ((EntityLivingBase)target).isPotionActive(WizardryPotions.mind_control)){

			NBTTagCompound entityNBT = target.getEntityData();

			if(entityNBT != null && entityNBT.hasUniqueId(MindControl.NBT_KEY)){
				if(attacker == EntityUtils.getEntityByUUID(target.world,
						entityNBT.getUniqueId(MindControl.NBT_KEY))){
					return false;
				}
			}
		}

		// Ally section
		if(attacker instanceof EntityPlayer && WizardData.get((EntityPlayer)attacker) != null){

			if(target instanceof EntityPlayer){
				// Tests whether the target is an ally of the attacker
				if(WizardData.get((EntityPlayer)attacker).isPlayerAlly((EntityPlayer)target)){
					return false;
				}

//			}else if(target instanceof ISummonedCreature){
//				// Tests whether the target is a creature that was summoned by an ally of the attacker
//				if(((ISummonedCreature)target).getCaster() instanceof EntityPlayer && WizardData.get((EntityPlayer)attacker)
//								.isPlayerAlly((EntityPlayer)((ISummonedCreature)target).getCaster())){
//					return false;
//				}

			}else if(target instanceof IEntityOwnable){
				// Tests whether the target is a creature that was summoned/tamed by an ally of the attacker
				if(isOwnerAlly((EntityPlayer)attacker, (IEntityOwnable)target)) return false;

			}else if(target instanceof EntityLiving && ((EntityLivingBase)target).isPotionActive(WizardryPotions.mind_control)){
				// Tests whether the target is a creature that was mind controlled by an ally of the attacker
				NBTTagCompound entityNBT = target.getEntityData();

				if(entityNBT != null && entityNBT.hasKey(MindControl.NBT_KEY)){

					Entity controller = EntityUtils.getEntityByUUID(target.world, entityNBT.getUniqueId(MindControl.NBT_KEY));

					if(controller instanceof EntityPlayer && WizardData.get((EntityPlayer)attacker).isPlayerAlly((EntityPlayer)controller)){
						return false;
					}
				}
			}
		}

		// Addon predicate registry — only evaluated at the root of the ownership recursion (i.e. when the attacker is
		// not itself an IEntityOwnable). Owned attackers already delegate upward via the recursive call at the top of
		// this method, so skipping them here ensures each logical query fires the predicates exactly once.
		if(!validTargetPredicates.isEmpty() && !(attacker instanceof IEntityOwnable)){
			for(BiPredicate<Entity, Entity> predicate : validTargetPredicates){
				if(predicate.test(attacker, target)) return false;
			}
		}

		return true;
	}

	/** Umbrella method that covers both {@link AllyDesignationSystem#isPlayerAlly(EntityPlayer, EntityPlayer)} and
	 * {@link AllyDesignationSystem#isOwnerAlly(EntityPlayer, IEntityOwnable)}, returning true if the second entity is
	 * either owned by the first entity, an ally of the first entity, or owned by an ally of the first entity. This is
	 * generally used to determine targets for healing or other group buffs. */
	public static boolean isAllied(EntityLivingBase allyOf, EntityLivingBase possibleAlly){

        if (allyOf == null || possibleAlly == null) {
            return false;
        }

		// Owned entities inherit their owner's allies
		if(allyOf instanceof IEntityOwnable){
			Entity owner = ((IEntityOwnable)allyOf).getOwner();
			if(owner instanceof EntityLivingBase && (owner == possibleAlly || isAllied((EntityLivingBase)owner, possibleAlly))) return true;
		}

		if(allyOf instanceof EntityPlayer && possibleAlly instanceof EntityPlayer
				&& isPlayerAlly((EntityPlayer)allyOf, (EntityPlayer)possibleAlly)){
			return true;
		}

		if(possibleAlly instanceof IEntityOwnable){
			IEntityOwnable pet = (IEntityOwnable)possibleAlly;
			if(pet.getOwner() == allyOf) return true;
			if(allyOf instanceof EntityPlayer && isOwnerAlly((EntityPlayer)allyOf, pet)) return true;
		}

		if(possibleAlly.isOnSameTeam(allyOf)) {
			return true;
		}

		// Check if the possibleAlly is mind controlled by the allyOf entity
		if(possibleAlly instanceof EntityLiving && possibleAlly.isPotionActive(WizardryPotions.mind_control)){
			NBTTagCompound entityNBT = possibleAlly.getEntityData();

			if(entityNBT != null && entityNBT.hasUniqueId(MindControl.NBT_KEY)){
				Entity controller = EntityUtils.getEntityByUUID(possibleAlly.world, entityNBT.getUniqueId(MindControl.NBT_KEY));
				return controller == allyOf;
			}
		}

		// Addon predicate registry — runs after all built-in rules; additive only.
		if(!allyPredicates.isEmpty()){
			for(BiPredicate<EntityLivingBase, EntityLivingBase> predicate : allyPredicates){
				if(predicate.test(allyOf, possibleAlly)) return true;
			}
		}

		return false;
	}

	/** Helper method for testing if the second player is an ally of the first player. Makes the code neater.
	 * @see AllyDesignationSystem#isOwnerAlly(EntityPlayer, IEntityOwnable) */
	public static boolean isPlayerAlly(EntityPlayer allyOf, EntityPlayer possibleAlly){
		WizardData data = WizardData.get(allyOf);
		return data != null && data.isPlayerAlly(possibleAlly);
	}

	/** Helper method for testing if the given {@link net.minecraft.entity.IEntityOwnable}'s owner is an ally of the
	 * given player. This works even when the owner is not logged in, though it may not correctly respect teams when
	 * that is the case. */
	public static boolean isOwnerAlly(EntityPlayer allyOf, IEntityOwnable ownable){
		WizardData data = WizardData.get(allyOf);
		if(data == null) return false;
		Entity owner = ownable.getOwner();
		if(owner == null) return data.isPlayerAlly(ownable.getOwnerId()); // offline owner
		if(owner instanceof EntityPlayer) return data.isPlayerAlly((EntityPlayer)owner);
		if(owner instanceof IEntityOwnable) return isOwnerAlly(allyOf, (IEntityOwnable)owner); // recurse
		return false;
	}

	@SubscribeEvent
	public static void onLivingAttackEvent(LivingAttackEvent event){

		if(event.getSource() != null && event.getSource().getTrueSource() instanceof EntityPlayer
				&& event.getSource() instanceof IElementalDamage){

			if(event.getEntity() instanceof EntityPlayer){
				// Prevents any magic damage to allied players if friendly fire is disabled for players
				if(Wizardry.settings.friendlyFire.blockPlayers && isPlayerAlly((EntityPlayer)event.getSource().getTrueSource(), (EntityPlayer)event.getEntity())){
					event.setCanceled(true);
				}
			}else{
				// Prevents any magic damage to entities owned by allied players if friendly fire is disabled for owned creatures
				// Since we're dealing with players separately we might as well just use isAllied
				if(Wizardry.settings.friendlyFire.blockOwned && isAllied((EntityPlayer)event.getSource().getTrueSource(), event.getEntityLiving())){
					event.setCanceled(true);
				}
			}
		}
	}
}
