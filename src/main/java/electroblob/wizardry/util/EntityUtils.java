package electroblob.wizardry.util;

import com.google.common.collect.Streams;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.entity.living.ISpellCaster;
import electroblob.wizardry.item.ISpellCastingItem;
import electroblob.wizardry.spell.Spell;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Contains useful static methods for retrieving and interacting with players, mobs and other entities. These methods
 * used to be part of {@code WizardryUtilities}.
 * @see BlockUtils
 * @see MagicDamage
 * @see AllyDesignationSystem
 * @author Electroblob
 * @since Wizardry 4.3
 */
public final class EntityUtils {

	private EntityUtils(){} // No instances!

	/** Changed to a constant in wizardry 2.1, since this is a lot more efficient. */
	private static final DataParameter<Boolean> POWERED;

	static {
		// Null is passed in deliberately since POWERED is a static field.
		POWERED = ObfuscationReflectionHelper.getPrivateValue(EntityCreeper.class, null, "field_184714_b");
	}

	/** Stores constant values for attribute modifier operations (and javadoc for what they actually do!) */
	// I'm fed up with remembering these...
	public static final class Operations {

		private Operations(){} // No instances!

		/** Adds the attribute modifier amount to the base value. */
		public static final int ADD = 0;
		/** Multiplies the base value by 1 plus the attribute modifier amount. Multiple modifiers are processed in
		 * parallel, i.e. the calculation is based on the base value and does not depend on previous modifiers. */
		public static final int MULTIPLY_FLAT = 1;
		/** Multiplies the base value by 1 plus the attribute modifier amount. Multiple modifiers are processed in
		 * series, i.e. the calculation is based on the value after previous modifiers are applied, in the order added. */
		public static final int MULTIPLY_CUMULATIVE = 2;
	}

	// Entity retrieval
	// ===============================================================================================================

	/**
	 * Shorthand for {@link EntityUtils#getEntitiesWithinRadius(double, double, double, double, World, Class)}
	 * with EntityLivingBase as the entity type. This is by far the most common use for that method.
	 *
	 * @param radius The search radius
	 * @param x The x coordinate to search around
	 * @param y The y coordinate to search around
	 * @param z The z coordinate to search around
	 * @param world The world to search in
	 */
	public static List<EntityLivingBase> getLivingWithinRadius(double radius, double x, double y, double z, World world){
		return getEntitiesWithinRadius(radius, x, y, z, world, EntityLivingBase.class);
	}

	/**
	 * Returns all entities of the specified type within the specified radius of the given coordinates. This is
	 * different to using a raw AABB because a raw AABB will search in a cube volume rather than a sphere. Note that
	 * this does not exclude any entities; if any specific entities are to be excluded this must be checked when
	 * iterating through the list.
	 *
	 * @see EntityUtils#getLivingWithinRadius(double, double, double, double, World)
	 * @param radius The search radius
	 * @param x The x coordinate to search around
	 * @param y The y coordinate to search around
	 * @param z The z coordinate to search around
	 * @param world The world to search in
	 * @param entityType The class of entity to search for; pass in Entity.class for all entities
	 */
	public static <T extends Entity> List<T> getEntitiesWithinRadius(double radius, double x, double y, double z, World world, Class<T> entityType){
		AxisAlignedBB aabb = new AxisAlignedBB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius);
		List<T> entityList = world.getEntitiesWithinAABB(entityType, aabb);
		for(int i = 0; i < entityList.size(); i++){
			if(entityList.get(i).getDistance(x, y, z) > radius){
				entityList.remove(i);
				break;
			}
		}
		return entityList;
	}

	/**
	 * Gets an entity from its UUID. If the UUID is known to belong to an {@code EntityPlayer}, use the more efficient
	 * {@link World#getPlayerEntityByUUID(UUID)} instead.
	 *
	 * @param world The world the entity is in
	 * @param id The entity's UUID
	 * @return The Entity that has the given UUID, or null if no such entity exists in the specified world.
	 */
	@Nullable
	public static Entity getEntityByUUID(World world, @Nullable UUID id){

		if(id == null) return null; // It would return null eventually but there's no point even looking

		for(Entity entity : world.loadedEntityList){
			// This is a perfect example of where you need to use .equals() and not ==. For most applications,
			// this was unnoticeable until world reload because the UUID instance or entity instance is stored.
			// Fixed now though.
			if(entity != null && entity.getUniqueID() != null && entity.getUniqueID().equals(id)){
				return entity;
			}
		}
		return null;
	}

	/**
	 * Returns the entity riding the given entity, or null if there is none. Allows for neater code now that entities
	 * have a list of passengers, because it is necessary to check that the list is not empty first.
	 */
	@Nullable
	public static Entity getRider(Entity entity){
		return !entity.getPassengers().isEmpty() ? entity.getPassengers().get(0) : null;
	}

	// Motion
	// ===============================================================================================================

	/**
	 * Undoes 1 tick's worth of velocity change due to gravity for the given entity. If the entity has no gravity,
	 * this method does nothing. This method is intended to be used in situations where entity gravity needs to be
	 * turned on and off and it is not practical to use {@link Entity#setNoGravity(boolean)}, usually if there is no
	 * easy way to get a reference to the entity to turn gravity back on.
	 *
	 * @param entity The entity to undo gravity for.
	 */
	public static void undoGravity(Entity entity){
		if(!entity.hasNoGravity()){
			double gravity = 0.04;
			if(entity instanceof EntityThrowable) gravity = 0.03;
			else if(entity instanceof EntityArrow) gravity = 0.05;
			else if(entity instanceof EntityLivingBase) gravity = 0.08;
			entity.motionY += gravity;
		}
	}

	/**
	 * Applies the standard (non-enchanted) amount of knockback to the given target, using the same calculation and
	 * strength value (0.4) as {@link EntityLivingBase#attackEntityFrom(DamageSource, float)}. Use in conjunction with
	 * {@link EntityUtils#attackEntityWithoutKnockback(Entity, DamageSource, float)} to change the source of
	 * knockback for an attack.
	 *
	 * @param attacker The entity that caused the knockback; the target will be pushed away from this entity
	 * @param target The entity to be knocked back
	 */
	public static void applyStandardKnockback(Entity attacker, EntityLivingBase target){
		applyStandardKnockback(attacker, target, 0.4f);
	}

	/**
	 * Applies the standard knockback calculation to the given target, using the same calculation as
	 * {@link EntityLivingBase#attackEntityFrom(DamageSource, float)}.
	 *
	 * @param attacker The entity that caused the knockback; the target will be pushed away from this entity
	 * @param target The entity to be knocked back
	 * @param strength The strength of the knockback
	 */
	public static void applyStandardKnockback(Entity attacker, EntityLivingBase target, float strength){
		double dx = attacker.posX - target.posX;
		double dz;
		for(dz = attacker.posZ - target.posZ; dx * dx + dz * dz < 1.0E-4D; dz = (Math.random() - Math.random())
				* 0.01D){
			dx = (Math.random() - Math.random()) * 0.01D;
		}
		target.knockBack(attacker, strength, dx, dz);
	}

	/**
	 * Finds the nearest space to the specified position that the given entity can teleport to without being inside one
	 * or more solid blocks. The search volume is twice the size of the entity's bounding box (meaning that when
	 * teleported to the returned position, the original destination remains within the entity's bounding box).
	 * @param entity The entity being teleported
	 * @param destination The target position to search around
	 * @param accountForPassengers True to take passengers into account when searching for a space, false to ignore them
	 * @return The resulting position, or null if no space was found.
	 */
	public static Vec3d findSpaceForTeleport(Entity entity, Vec3d destination, boolean accountForPassengers){

		World world = entity.world;
		AxisAlignedBB box = entity.getEntityBoundingBox();

		if(accountForPassengers){
			for(Entity passenger : entity.getPassengers()){
				box = box.union(passenger.getEntityBoundingBox());
			}
		}

		box = box.offset(destination.subtract(entity.posX, entity.posY, entity.posZ));

		// All the parameters of this method are INCLUSIVE, so even the max coordinates should be rounded down
		Iterable<BlockPos> cuboid = BlockPos.getAllInBox(MathHelper.floor(box.minX), MathHelper.floor(box.minY),
				MathHelper.floor(box.minZ), MathHelper.floor(box.maxX), MathHelper.floor(box.maxY), MathHelper.floor(box.maxZ));

		if(Streams.stream(cuboid).noneMatch(b -> world.collidesWithAnyBlock(new AxisAlignedBB(b)))){
			// Nothing in the way
			return destination;

		}else{
			// Nearby position search
			double dx = box.maxX - box.minX;
			double dy = box.maxY - box.minY;
			double dz = box.maxZ - box.minZ;

			// Minimum space required is (nx + px) blocks * (ny + py) blocks * (nz + pz) blocks
			int nx = MathHelper.ceil(dx) / 2;
			int px = MathHelper.ceil(dx) - nx;
			int ny = MathHelper.ceil(dy) / 2;
			int py = MathHelper.ceil(dy) - ny;
			int nz = MathHelper.ceil(dz) / 2;
			int pz = MathHelper.ceil(dz) - nz;

			// Check all the blocks in and around the bounding box...
			List<BlockPos> nearby = Streams.stream(BlockPos.getAllInBox(MathHelper.floor(box.minX) - 1,
					MathHelper.floor(box.minY) - 1, MathHelper.floor(box.minZ) - 1,
					MathHelper.floor(box.maxX) + 1, MathHelper.floor(box.maxY) + 1,
					MathHelper.floor(box.maxZ) + 1)).collect(Collectors.toList());

			// ... but only return positions actually inside the box
			List<BlockPos> possiblePositions = Streams.stream(cuboid).collect(Collectors.toList());

			// Rather than iterate over each position and check if the box fits, find all solid blocks and cut out all
			// positions whose corresponding box would include them - this is waaay more efficient!
			while(!nearby.isEmpty()){

				BlockPos pos = nearby.remove(0);

				if(world.collidesWithAnyBlock(new AxisAlignedBB(pos))){
					Predicate<BlockPos> nearSolidBlock = b -> b.getX() >= pos.getX() - nx && b.getX() <= pos.getX() + px
														   && b.getY() >= pos.getY() - ny && b.getY() <= pos.getY() + py
														   && b.getZ() >= pos.getZ() - nz && b.getZ() <= pos.getZ() + pz;
					nearby.removeIf(nearSolidBlock);
					possiblePositions.removeIf(nearSolidBlock);
				}
			}

			if(possiblePositions.isEmpty()) return null; // No space nearby

			BlockPos nearest = possiblePositions.stream().min(Comparator.comparingDouble(b -> destination.squareDistanceTo(
					b.getX() + 0.5, b.getY() + 0.5, b.getZ() + 0.5))).get(); // The list can't be empty

			return GeometryUtils.getFaceCentre(nearest, EnumFacing.DOWN);
		}
	}

	// Damage
	// ===============================================================================================================

	/**
	 * Attacks the given entity with the given damage source and amount, but preserving the entity's original velocity
	 * instead of applying knockback, as would happen with
	 * {@link EntityLivingBase#attackEntityFrom(DamageSource, float)} <i>(More accurately, calls that method as normal
	 * and then resets the entity's velocity to what it was before).</i> Handy for when you need to damage an entity
	 * repeatedly in a short space of time.
	 *
	 * @param entity The entity to attack
	 * @param source The source of the damage
	 * @param amount The amount of damage to apply
	 * @return True if the attack succeeded, false if not.
	 */
	public static boolean attackEntityWithoutKnockback(Entity entity, DamageSource source, float amount){
		double vx = entity.motionX;
		double vy = entity.motionY;
		double vz = entity.motionZ;
		boolean succeeded = entity.attackEntityFrom(source, amount);
		entity.motionX = vx;
		entity.motionY = vy;
		entity.motionZ = vz;
		return succeeded;
	}

	/**
	 * Returns whether the given {@link DamageSource} is melee damage. This method makes a best guess as to whether
	 * the damage was from a melee attack; there is no way of testing this properly.
	 * @param source The damage source to be tested.
	 * @return True if the given damage source is melee damage, false otherwise.
	 */
	public static boolean isMeleeDamage(DamageSource source){

		// With the exception of minions, melee damage always has the same entity for immediate/true source
		if(!(source instanceof MinionDamage) && source.getImmediateSource() != source.getTrueSource()) return false;
		if(source.isProjectile()) return false; // Projectile damage obviously isn't melee damage
		if(source.isUnblockable()) return false; // Melee damage should always be blockable
		if(!(source instanceof MinionDamage) && source instanceof IElementalDamage) return false;
		if(!(source.getTrueSource() instanceof EntityLivingBase)) return false; // Only living things can melee!

		if(source.getTrueSource() instanceof EntityPlayer && source.getDamageLocation() != null
				&& source.getDamageLocation().distanceTo(source.getTrueSource().getPositionVector()) > ((EntityLivingBase)source
				.getTrueSource()).getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue()){
			return false; // Out of melee reach for players
		}

		// If it got through all that, chances are it's melee damage
		return true;
	}

	// Boolean checks
	// ===============================================================================================================

	/**
	 * Returns true if the given entity is an EntityLivingBase and not an armour stand; makes the code a bit neater.
	 * This was added because armour stands are a subclass of EntityLivingBase, but shouldn't necessarily be treated
	 * as living entities - this depends on the situation. <i>The given entity can safely be cast to EntityLivingBase
	 * if this method returns true.</i>
	 */
	// In my opinion, it's a bad design choice to have armour stands extend EntityLivingBase directly - it would be
	// better to make a parent class which is extended by both armour stands and EntityLivingBase and contains only
	// the code required by both.
	public static boolean isLiving(Entity entity){
		return entity instanceof EntityLivingBase && !(entity instanceof EntityArmorStand);
	}

	/**
	 * Checks if the given player is opped on the given server. If the server is a singleplayer or LAN server, this
	 * means they have cheats enabled.
	 */
	public static boolean isPlayerOp(EntityPlayer player, MinecraftServer server){
		return server.getPlayerList().getOppedPlayers().getEntry(player.getGameProfile()) != null;
	}

	/** Checks that the given entity is allowed to damage blocks in the given world. If the entity is a player or null,
	 * this checks the player block damage config setting, otherwise it posts a mob griefing event and returns the result. */
	public static boolean canDamageBlocks(@Nullable EntityLivingBase entity, World world){
		// TODO: Dispenser griefing!
		if(entity == null || entity instanceof EntityPlayer) return Wizardry.settings.playerBlockDamage;
		return ForgeEventFactory.getMobGriefingEvent(world, entity);
	}

	/**
	 * Returns true if the given caster is currently casting the given spell by any means. This method is intended to
	 * eliminate the long and cumbersome wand use checking in event handlers, which often missed out spells cast by
	 * means other than wands.
	 * @param caster The potential spell caster, which may be a player or an {@link ISpellCaster}. Any other entity will
	 * cause this method to always return false.
	 * @param spell The spell to check for. The spell must be continuous or this method will always return false.
	 * @return True if the caster is currently casting the given spell through any means, false otherwise.
	 */
	// The reason this is a boolean check is that actually returning a spell presents a problem: players can cast two
	// continuous spells at once, one via commands and one via an item, so which do you choose? Since the main point was
	// to check for specific spells, it seems more useful to do it this way.
	public static boolean isCasting(EntityLivingBase caster, Spell spell){

		if(!spell.isContinuous) return false;

		if(caster instanceof EntityPlayer){

			WizardData data = WizardData.get((EntityPlayer)caster);

			if(data != null && data.currentlyCasting() == spell) return true;

			if(caster.isHandActive() && caster.getItemInUseMaxCount() >= spell.getChargeup()){

				ItemStack stack = caster.getHeldItem(caster.getActiveHand());

				if(stack.getItem() instanceof ISpellCastingItem && ((ISpellCastingItem)stack.getItem()).getCurrentSpell(stack) == spell){
					// Don't do this, it interferes with stuff! We effectively already tested this with caster.isHandActive() anyway
//						&& ((ISpellCastingItem)stack.getItem()).canCast(stack, spell, (EntityPlayer)caster,
//						EnumHand.MAIN_HAND, 0, new SpellModifiers())){
					return true;
				}
			}

		}else if(caster instanceof ISpellCaster){
			if(((ISpellCaster)caster).getContinuousSpell() == spell) return true;
		}

		return false;
	}

	// Misc
	// ===============================================================================================================

	/** Returns the default aiming arror used by skeletons for the given difficulty. For reference, these are: Easy - 10,
	 * Normal - 6, Hard - 2, Peaceful - 10 (rarely used). */
	public static int getDefaultAimingError(EnumDifficulty difficulty){
		switch(difficulty){
			case EASY: return 10;
			case NORMAL: return 6;
			case HARD: return 2;
			default: return 10; // Peaceful counts as easy; the only time this is used is when a player attacks a (good) wizard.
		}
	}

	/**
	 * Turns the given creeper into a charged creeper. In 1.10, this requires reflection since the DataManager keys are
	 * private. (You <i>could</i> call {@link EntityCreeper#onStruckByLightning(EntityLightningBolt)} and then heal it
	 * and extinguish it, but that's a bit awkward, and it'll trigger events and stuff...)
	 */
	// The reflection here only gets done once to initialise the POWERED field, so it's not a performance issue at all.
	public static void chargeCreeper(EntityCreeper creeper){
		creeper.getDataManager().set(POWERED, true);
	}

	// No point allowing anything other than players for these methods since other entities can use Entity#playSound.

	/**
	 * Shortcut for
	 * {@link World#playSound(EntityPlayer, double, double, double, SoundEvent, SoundCategory, float, float)} where the
	 * player is null but the x, y and z coordinates are those of the passed in player. Use in preference to
	 * {@link EntityPlayer#playSound(SoundEvent, float, float)} if there are client-server discrepancies.
	 */
	public static void playSoundAtPlayer(EntityPlayer player, SoundEvent sound, SoundCategory category, float volume,
			float pitch){
		player.world.playSound(null, player.posX, player.posY, player.posZ, sound, category, volume, pitch);
	}

	/**
	 * See {@link EntityUtils#playSoundAtPlayer(EntityPlayer, SoundEvent, SoundCategory, float, float)}. Category
	 * defaults to {@link SoundCategory#PLAYERS}.
	 */
	public static void playSoundAtPlayer(EntityPlayer player, SoundEvent sound, float volume, float pitch){
		player.world.playSound(null, player.posX, player.posY, player.posZ, sound, SoundCategory.PLAYERS, volume, pitch);
	}

}
