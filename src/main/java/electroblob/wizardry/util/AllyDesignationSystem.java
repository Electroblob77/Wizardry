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

/**
 * Contains some useful static methods for interacting with the ally designation system. Also handles the friendly fire
 * setting. This was split off from {@link JavaUtils} as of wizardry 4.2 in an effort to make the code easier to
 * navigate.
 */
@Mod.EventBusSubscriber
public final class AllyDesignationSystem {

	private AllyDesignationSystem(){} // No instances!

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

		return true;
	}

	/** Umbrella method that covers both {@link AllyDesignationSystem#isPlayerAlly(EntityPlayer, EntityPlayer)} and
	 * {@link AllyDesignationSystem#isOwnerAlly(EntityPlayer, IEntityOwnable)}, returning true if the second entity is
	 * either owned by the first entity, an ally of the first entity, or owned by an ally of the first entity. This is
	 * generally used to determine targets for healing or other group buffs. */
	public static boolean isAllied(EntityLivingBase allyOf, EntityLivingBase possibleAlly){

		// Owned entities inherit their owner's allies
		if(allyOf instanceof IEntityOwnable){
			Entity owner = ((IEntityOwnable)allyOf).getOwner();
			if(owner instanceof EntityLivingBase && isAllied((EntityLivingBase)owner, possibleAlly)) return true;
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
		return owner instanceof EntityPlayer ? data.isPlayerAlly((EntityPlayer)owner) : data.isPlayerAlly(ownable.getOwnerId());
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
