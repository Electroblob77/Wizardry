package electroblob.wizardry.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.ImmutablePair;

import electroblob.wizardry.CommonProxy;
import electroblob.wizardry.WizardData;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.living.ISummonedCreature;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.spell.MindControl;
import electroblob.wizardry.spell.Spell;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.EntityEquipmentSlot.Type;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * <i>"Where do you put random but useful bits and pieces? {@code WizardryUtilities} of course - the 'stuff that doesn't
 * fit anywhere else' class!"</i>
 * <p>
 * This class contains some useful static methods for use anywhere - items, entities, spells, events, blocks, etc.
 * Broadly speaking, these fall into the following categories:
 * <p>
 * - In-world utilities (position calculating, retrieving entities, etc.)<br>
 * - Raytracing<br>
 * - Drawing utilities (client-only)<br>
 * - NBT and data storage utilities<br>
 * - Interaction with the ally designation system<br>
 * - Loot and weighting utilities
 * 
 * @see CommonProxy
 * @see WandHelper
 * @since Wizardry 1.0
 * @author Electroblob
 */
public final class WizardryUtilities {

	/**
	 * Constant which is simply an array of the four armour slots. (Could've sworn this exists somewhere in vanilla, but
	 * I can't find it anywhere...)
	 */
	public static final EntityEquipmentSlot[] ARMOUR_SLOTS;
	/** Changed to a constant in wizardry 2.1, since this is a lot more efficient. */
	private static final DataParameter<Boolean> POWERED;

	static{
		// The list of slots needs to be mutable.
		List<EntityEquipmentSlot> slots = new ArrayList<EntityEquipmentSlot>(
				Arrays.asList(EntityEquipmentSlot.values()));
		slots.removeIf(slot -> slot.getSlotType() != Type.ARMOR);
		ARMOUR_SLOTS = slots.toArray(new EntityEquipmentSlot[0]);

		// Null is passed in deliberately since POWERED is a static field.
		POWERED = ReflectionHelper.getPrivateValue(EntityCreeper.class, null, "POWERED", "field_184714_b");
	}

	// SECTION Block/Entity/World Utilities
	// ===============================================================================================================

	/**
	 * Returns the actual light level, taking natural light (skylight) and artificial light (block light) into account.
	 * This uses the same logic as mob spawning.
	 * 
	 * @return The light level, from 0 (pitch darkness) to 15 (full daylight/at a torch).
	 */
	public static int getLightLevel(World world, BlockPos pos){
		
		int i = world.getLightFromNeighbors(pos);

        if(world.isThundering()){
            int j = world.getSkylightSubtracted();
            world.setSkylightSubtracted(10);
            i = world.getLightFromNeighbors(pos);
            world.setSkylightSubtracted(j);
        }

        return i;
	}
	
	/**
	 * Returns whether the block at the given coordinates can be replaced by another one (works as if a block is being
	 * placed by a player). True for air, liquids, vines, tall grass and snow layers but not for flowers, signs etc.
	 * This is a shortcut for <code>world.getBlockState(pos).getMaterial().isReplaceable()</code>.
	 * 
	 * @see WizardryUtilities#canBlockBeReplacedB(World, BlockPos)
	 */
	public static boolean canBlockBeReplaced(World world, BlockPos pos){
		return world.isAirBlock(new BlockPos(pos)) || world.getBlockState(pos).getMaterial().isReplaceable();
	}

	/**
	 * Returns whether the block at the given coordinates can be replaced by another one (works as if a block is being
	 * placed by a player) and is not a liquid. True for air, vines, tall grass and snow layers but not for flowers,
	 * signs etc. or any liquids.
	 * 
	 * @see WizardryUtilities#canBlockBeReplaced(World, BlockPos)
	 */
	public static boolean canBlockBeReplacedB(World world, BlockPos pos){
		return canBlockBeReplaced(world, pos) && !world.getBlockState(pos).getMaterial().isLiquid();
	}

	/**
	 * Returns whether the block at the given coordinates is unbreakable in survival mode. In vanilla this is true for
	 * bedrock and end portal frame, for example. This is a shortcut for
	 * world.getBlockState(pos).getBlockHardness(world, pos) == -1.0f. Not much of a shortcut any more, since block ids
	 * have been phased out.
	 */
	public static boolean isBlockUnbreakable(World world, BlockPos pos){
		return world.isAirBlock(new BlockPos(pos)) ? false
				: world.getBlockState(pos).getBlockHardness(world, pos) == -1.0f;
	}

	/**
	 * Finds the nearest floor level to the given y coord within the range specified at the given x and z coords.
	 * Liquids and other blocks that cannot be built on top of do not count, but stuff like signs does. (Technically any
	 * block is allowed to be the floor according to the code, but seeing as it searches upwards and non-solid blocks
	 * usually need a supporting block, the floor is likely to always be solid).
	 * 
	 * @param world
	 * @param x The x coordinate to search in
	 * @param y The y coordinate to search from
	 * @param z The z coordinate to search in
	 * @param range The maximum distance from the given y coordinate to search.
	 * @return The y coordinate of the closest floor level, or -1 if there is none. Returns the actual level of the
	 *         floor as would be seen in the debug screen when the player is standing on it.
	 * @see WizardryUtilities#getNearestFloorLevelB(World, BlockPos, int)
	 */
	public static int getNearestFloorLevel(World world, BlockPos pos, int range){

		int yCoord = -2;
		for(int i = -range; i <= range; i++){
			// The last bit determines whether the block found to be a suitable floor is closer than the previous one
			// found.
			if(world.isSideSolid(pos.up(i), EnumFacing.UP)
					&& (world.isAirBlock(pos.up(i + 1)) || !world.isSideSolid(pos.up(i + 1), EnumFacing.UP))
					&& (i < yCoord - pos.getY() || yCoord == -2)){
				yCoord = pos.getY() + i;
			}
		}
		return yCoord + 1;
	}

	/**
	 * Finds the nearest floor level to the given y coord within the range specified at the given x and z coords. Only
	 * works if the block above the floor is actually air and the floor is solid or a liquid.
	 * 
	 * @param world
	 * @param x The x coordinate to search in
	 * @param y The y coordinate to search from
	 * @param z The z coordinate to search in
	 * @param range The maximum distance from the given y coordinate to search.
	 * @return The y coordinate of the closest floor level, or -1 if there is none. Returns the actual level of the
	 *         floor as would be seen in the debug screen when the player is standing on it.
	 * @see WizardryUtilities#getNearestFloorLevel(World, BlockPos, int)
	 */
	public static int getNearestFloorLevelB(World world, BlockPos pos, int range){
		int yCoord = -2;
		for(int i = -range; i <= range; i++){
			if(world.isAirBlock(new BlockPos(pos.up(i + 1)))
					&& (world.getBlockState(pos.up(i)).getMaterial().isLiquid()
							|| world.isSideSolid(pos.up(i), EnumFacing.UP))
					&& (i < yCoord - pos.getY() || yCoord == -2)){
				// The last bit determines whether the block found to be a suitable floor is closer than the previous
				// one found.
				yCoord = pos.getY() + i;
			}
		}
		return yCoord + 1;
	}

	/**
	 * Finds the nearest floor level to the given y coord within the range specified at the given x and z coords.
	 * Everything that is not air is treated as floor, even stuff that can't be walked on.
	 * 
	 * @param world
	 * @param x The x coordinate to search in
	 * @param y The y coordinate to search from
	 * @param z The z coordinate to search in
	 * @param range The maximum distance from the given y coordinate to search.
	 * @return The y coordinate of the closest floor level, or -1 if there is none. Returns the actual level of the
	 *         floor as would be seen in the debug screen when the player is standing on it.
	 * @see WizardryUtilities#getNearestFloorLevel(World, BlockPos, int)
	 */
	public static int getNearestFloorLevelC(World world, BlockPos pos, int range){
		int yCoord = -2;
		for(int i = -range; i <= range; i++){
			if(world.isAirBlock(new BlockPos(pos.up(i + 1))) && (i < yCoord - pos.getY() || yCoord == -2)){
				// The last bit determines whether the block found to be a suitable floor is closer than the previous
				// one found.
				yCoord = pos.getY() + i;
			}
		}
		return yCoord + 1;
	}

	/**
	 * Gets a random position on the ground near the given entity within the specified horizontal and vertical ranges.
	 * Used to find a position to spawn entities in summoning spells.
	 * 
	 * @param entity The entity around which to search.
	 * @param horizontalRange The maximum number of blocks on the x or z axis the returned position can be from the
	 *        given entity. <i>The number of operations performed by this method is proportional to the square of this
	 *        parameter, so for performance reasons it is recommended that it does not exceed around 10.</i>
	 * @param verticalRange The maximum number of blocks on the y axis the returned position can be from the given
	 *        entity.
	 * @return A BlockPos with the coordinates of the block directly above the ground at the position found, or null if
	 *         none were found within range. Importantly, since this method checks <i>all possible</i> positions within
	 *         range (i.e. randomness only occurs when deciding between the possible positions), if it returns null once
	 *         then it will always return null given the same circumstances and parameters. What this means is that you
	 *         can (and should) immediately stop trying to cast a summoning spell if this returns null.
	 */
	@Nullable
	public static BlockPos findNearbyFloorSpace(Entity entity, int horizontalRange, int verticalRange){

		World world = entity.world;
		BlockPos origin = new BlockPos(entity);
		return findNearbyFloorSpace(world, origin, horizontalRange, verticalRange);
	}
	
	/**
	 * Gets a random position on the ground near the given BlockPos within the specified horizontal and vertical ranges.
	 * Used to find a position to spawn entities in summoning spells.
	 * 
	 * @param world The world in which to search.
	 * @param origin The BlockPos around which to search.
	 * @param horizontalRange The maximum number of blocks on the x or z axis the returned position can be from the
	 *        given position. <i>The number of operations performed by this method is proportional to the square of this
	 *        parameter, so for performance reasons it is recommended that it does not exceed around 10.</i>
	 * @param verticalRange The maximum number of blocks on the y axis the returned position can be from the given
	 *        position.
	 * @return A BlockPos with the coordinates of the block directly above the ground at the position found, or null if
	 *         none were found within range. Importantly, since this method checks <i>all possible</i> positions within
	 *         range (i.e. randomness only occurs when deciding between the possible positions), if it returns null once
	 *         then it will always return null given the same circumstances and parameters. What this means is that you
	 *         can (and should) immediately stop trying to cast a summoning spell if this returns null.
	 */
	@Nullable
	public static BlockPos findNearbyFloorSpace(World world, BlockPos origin, int horizontalRange, int verticalRange){
		
		List<BlockPos> possibleLocations = new ArrayList<BlockPos>();

		for(int x = -horizontalRange; x <= horizontalRange; x++){
			for(int z = -horizontalRange; z <= horizontalRange; z++){
				int y = WizardryUtilities.getNearestFloorLevel(world, origin.add(x, 0, z), verticalRange);
				if(y > -1) possibleLocations.add(new BlockPos(origin.getX() + x, y, origin.getZ() + z));
			}
		}

		if(possibleLocations.isEmpty()){
			return null;
		}else{
			return possibleLocations.get(world.rand.nextInt(possibleLocations.size()));
		}
	}

	/**
	 * Gets the blockstate of the block the specified entity is standing on. Uses
	 * {@link MathHelper#floor_double(double)} because casting to int will not return the correct coordinate when x or z
	 * is negative.
	 */
	public static IBlockState getBlockEntityIsStandingOn(Entity entity){
		BlockPos pos = new BlockPos(MathHelper.floor(entity.posX), (int)entity.getEntityBoundingBox().minY - 1,
				MathHelper.floor(entity.posZ));
		return entity.world.getBlockState(pos);
	}

	/**
	 * Shorthand for {@link WizardryUtilities#getEntitiesWithinRadius(double, double, double, double, World, Class)}
	 * with EntityLivingBase as the entity type. This is by far the most common use for that method, which is why this
	 * shorthand exists.
	 * 
	 * @param radius The search radius
	 * @param x The x coordinate to search around
	 * @param y The y coordinate to search around
	 * @param z The z coordinate to search around
	 * @param world The world to search in
	 */
	public static List<EntityLivingBase> getEntitiesWithinRadius(double radius, double x, double y, double z,
			World world){
		return getEntitiesWithinRadius(radius, x, y, z, world, EntityLivingBase.class);
	}

	/**
	 * Returns all entities of the specified type within the specified radius of the given coordinates. This is
	 * different to using a raw AABB because a raw AABB will search in a cube volume rather than a sphere. Note that
	 * this does not exclude any entities; if any specific entities are to be excluded this must be checked when
	 * iterating through the list.
	 * 
	 * @see {@link WizardryUtilities#getEntitiesWithinRadius(double, double, double, double, World)}
	 * @param radius The search radius
	 * @param x The x coordinate to search around
	 * @param y The y coordinate to search around
	 * @param z The z coordinate to search around
	 * @param world The world to search in
	 * @param entityType The class of entity to search for; pass in Entity.class for all entities
	 */
	public static <T extends Entity> List<T> getEntitiesWithinRadius(double radius, double x, double y, double z,
			World world, Class<T> entityType){
		AxisAlignedBB aabb = new AxisAlignedBB(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius);
		List<T> entityList = world.getEntitiesWithinAABB(entityType, aabb);
		for(int i = 0; i < entityList.size(); i++){
			if(entityList.get(i).getDistance(x, y, z) > radius){
				entityList.remove(i);
			}
		}
		return entityList;
	}

	/**
	 * Gets an entity from its UUID. Note that you should check this isn't null. If the UUID is known to belong to an
	 * EntityPlayer, use the more efficient {@link World#getPlayerEntityByUUID(UUID)} instead.
	 * 
	 * @param world The world the entity is in
	 * @param id The entity's UUID
	 * @return The Entity that has the given UUID, or null if no such entity exists in the specified world.
	 */
	@Nullable
	public static Entity getEntityByUUID(World world, UUID id){

		for(Entity entity : world.loadedEntityList){
			// This is a perfect example of where you need to use .equals() and not ==. For most applications,
			// this was unnoticeable until world reload because the UUID instance or entity instance is stored.
			// Fixed now though.
			if(entity.getUniqueID().equals(id)){
				return entity;
			}
		}
		return null;
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
	 * See {@link WizardryUtilities#playSoundAtPlayer(EntityPlayer, SoundEvent, SoundCategory, float, float)}. Category
	 * defaults to {@link SoundCategory#PLAYERS}.
	 */
	public static void playSoundAtPlayer(EntityPlayer player, SoundEvent sound, float volume, float pitch){
		player.world.playSound(null, player.posX, player.posY, player.posZ, sound, SoundCategory.PLAYERS, volume, pitch);
	}

	/**
	 * Returns the entity riding the given entity, or null if there is none. Allows for neater code now that entities
	 * have a list of passengers, because it is necessary to check that the list is not null or empty first.
	 */
	@Nullable
	public static Entity getRider(Entity entity){
		return entity.getPassengers() != null && !entity.getPassengers().isEmpty() ? entity.getPassengers().get(0)
				: null;
	}

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
	 * Applies the standard (non-enchanted) amount of knockback to the given target, using the same calculation as
	 * {@link EntityLivingBase#attackEntityFrom(DamageSource, float)}. Use in conjunction with
	 * {@link WizardryUtilities#attackEntityWithoutKnockback(Entity, DamageSource, float)} to change the source of
	 * knockback for an attack.
	 * 
	 * @param attacker The entity that caused the knockback; the target will be pushed away from this entity.
	 * @param target The entity to be knocked back.
	 */
	public static void applyStandardKnockback(Entity attacker, EntityLivingBase target){
		double dx = attacker.posX - target.posX;
		double dz;
		for(dz = attacker.posZ - target.posZ; dx * dx + dz * dz < 1.0E-4D; dz = (Math.random() - Math.random())
				* 0.01D){
			dx = (Math.random() - Math.random()) * 0.01D;
		}
		// The first argument is never used.
		target.knockBack(null, 0.4f, dx, dz);
	}

	/**
	 * Returns a list of the itemstacks in the given player's hotbar. Defined here for convenience and to centralise the
	 * (unfortunately unavoidable) use of hardcoded numbers to reference the inventory slots. The returned list is a
	 * modifiable copy of part of the player's inventory stack list; as such, changes to the list are <b>not</b> written
	 * through to the player's inventory. However, the ItemStack instances themselves are not copied, so changes to any
	 * of their fields (size, metadata...) will change those in the player's inventory.
	 * 
	 * @since Wizardry 1.2
	 */
	public static List<ItemStack> getHotbar(EntityPlayer player){
		NonNullList<ItemStack> hotbar = NonNullList.create();
		hotbar.addAll(player.inventory.mainInventory.subList(0, 9));
		return hotbar;
	}

	/**
	 * Returns a list of the itemstacks in the given player's hotbar and offhand, sorted into the following order: main
	 * hand, offhand, rest of hotbar left-to-right. The returned list is a modifiable shallow copy of part of the player's
	 * inventory stack list; as such, changes to the list are <b>not</b> written through to the player's inventory.
	 * However, the ItemStack instances themselves are not copied, so changes to any of their fields (size, metadata...)
	 * will change those in the player's inventory.
	 * 
	 * @since Wizardry 1.2
	 */
	public static List<ItemStack> getPrioritisedHotbarAndOffhand(EntityPlayer player){
		List<ItemStack> hotbar = WizardryUtilities.getHotbar(player);
		// Adds the offhand item to the beginning of the list so it is processed before the hotbar
		hotbar.add(0, player.getHeldItemOffhand());
		// Moves the item in the main hand to the beginning of the list so it is processed first
		hotbar.remove(player.getHeldItemMainhand());
		hotbar.add(0, player.getHeldItemMainhand());
		return hotbar;
	}

	/**
	 * Tests whether the specified player has any of the specified item in their entire inventory, including armour
	 * slots and offhand.
	 */
	public static boolean doesPlayerHaveItem(EntityPlayer player, Item item){

		for(ItemStack stack : player.inventory.mainInventory){
			if(stack.getItem() == item){
				return true;
			}
		}

		for(ItemStack stack : player.inventory.armorInventory){
			if(stack.getItem() == item){
				return true;
			}
		}

		for(ItemStack stack : player.inventory.offHandInventory){
			if(stack.getItem() == item){
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks if the given player is opped on the given server. If the server is a singleplayer or LAN server, this
	 * means they have cheats enabled.
	 */
	public static boolean isPlayerOp(EntityPlayer player, MinecraftServer server){
		return server.getPlayerList().getOppedPlayers().getEntry(player.getGameProfile()) != null;
	}

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
	 * Turns the given creeper into a charged creeper. In 1.10, this requires reflection since the DataManager keys are
	 * private. (You <i>could</i> call {@link EntityCreeper#onStruckByLightning(...)} and then heal it and extinguish
	 * it, but that's a bit awkward.)
	 */
	public static void chargeCreeper(EntityCreeper creeper){
		creeper.getDataManager().set(POWERED, true);
	}

	// SECTION Raytracing
	// ===============================================================================================================

	/**
	 * Does a block ray trace (NOT entities) from an entity's eyes (i.e. properly...)
	 */
	@Nullable
	public static RayTraceResult standardBlockRayTrace(World world, EntityLivingBase entity, double range, boolean hitLiquids){

		Vec3d start = new Vec3d(entity.posX, entity.getEntityBoundingBox().minY + entity.getEyeHeight(), entity.posZ);
		Vec3d look = entity.getLookVec();
		Vec3d end = start.addVector(look.x * range, look.y * range, look.z * range);
		return world.rayTraceBlocks(start, end, hitLiquids);
	}

	/**
	 * Helper method which does a rayTrace for entities from an entity's eye level in the direction they are looking
	 * with a specified range, using the tracePath method. Tidies up the code a bit. Border size defaults to 1.
	 * 
	 * @param world
	 * @param entity
	 * @param range
	 * @return
	 */
	@Nullable
	public static RayTraceResult standardEntityRayTrace(World world, EntityLivingBase entity, double range, boolean hitLiquids){
		return standardEntityRayTrace(world, entity, range, 1, hitLiquids);
	}

	/**
	 * Helper method which does a rayTrace for entities from a entity's eye level in the direction they are looking with
	 * a specified range and radius, using the tracePath method. Tidies up the code a bit.
	 * 
	 * @param world
	 * @param entity
	 * @param range
	 * @param borderSize
	 * @return
	 */
	@Nullable
	public static RayTraceResult standardEntityRayTrace(World world, EntityLivingBase entity, double range, float borderSize, boolean hitLiquids){
		double dx = entity.getLookVec().x * range;
		double dy = entity.getLookVec().y * range;
		double dz = entity.getLookVec().z * range;
		HashSet<Entity> hashset = new HashSet<Entity>(1);
		hashset.add(entity);
		return WizardryUtilities.tracePath(world, (float)entity.posX,
				(float)(entity.getEntityBoundingBox().minY + entity.getEyeHeight()), (float)entity.posZ,
				(float)(entity.posX + dx), (float)(entity.posY + entity.getEyeHeight() + dy), (float)(entity.posZ + dz),
				borderSize, hashset, false, hitLiquids);
	}

	/**
	 * Method for ray tracing entities. You can also use this for seeking.
	 * 
	 * @param world
	 * @param x startX
	 * @param y startY
	 * @param z startZ
	 * @param tx endX
	 * @param ty endY
	 * @param tz endZ
	 * @param borderSize extra area to examine around line for entities
	 * @param excluded any excluded entities (the player, etc)
	 * @return a RayTraceResult of either the block hit (no entity hit), the entity hit (hit an entity), or null for
	 *         nothing hit
	 */
	@Nullable
	public static RayTraceResult tracePath(World world, float x, float y, float z, float tx, float ty, float tz,
			float borderSize, HashSet<Entity> excluded, boolean collideablesOnly, boolean hitLiquids){

		Vec3d startVec = new Vec3d(x, y, z);
		// Vec3d lookVec = new Vec3d(tx-x, ty-y, tz-z);
		Vec3d endVec = new Vec3d(tx, ty, tz);
		float minX = x < tx ? x : tx;
		float minY = y < ty ? y : ty;
		float minZ = z < tz ? z : tz;
		float maxX = x > tx ? x : tx;
		float maxY = y > ty ? y : ty;
		float maxZ = z > tz ? z : tz;
		AxisAlignedBB bb = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ).grow(borderSize, borderSize, borderSize);
		List<Entity> allEntities = world.getEntitiesWithinAABBExcludingEntity(null, bb);
		RayTraceResult blockHit = world.rayTraceBlocks(startVec, endVec, hitLiquids);
		startVec = new Vec3d(x, y, z);
		endVec = new Vec3d(tx, ty, tz);
		float maxDistance = (float)endVec.distanceTo(startVec);
		if(blockHit != null){
			maxDistance = (float)blockHit.hitVec.distanceTo(startVec);
		}
		Entity closestHitEntity = null;
		float closestHit = maxDistance;
		float currentHit = 0.f;
		AxisAlignedBB entityBb;// = ent.getBoundingBox();
		RayTraceResult intercept;
		for(Entity ent : allEntities){
			if((ent.canBeCollidedWith() || !collideablesOnly)
					&& ((excluded != null && !excluded.contains(ent)) || excluded == null)){
				float entBorder = ent.getCollisionBorderSize();
				entityBb = ent.getEntityBoundingBox();
				if(entityBb != null){
					entityBb = entityBb.grow(entBorder, entBorder, entBorder);
					intercept = entityBb.calculateIntercept(startVec, endVec);
					if(intercept != null){
						currentHit = (float)intercept.hitVec.distanceTo(startVec);
						if(currentHit < closestHit || currentHit == 0){
							closestHit = currentHit;
							closestHitEntity = ent;
						}
					}
				}
			}
		}
		if(closestHitEntity != null){
			blockHit = new RayTraceResult(closestHitEntity);
		}
		return blockHit;
	}

	// SECTION Rendering and GUIs
	// ===============================================================================================================

	// Doesn't seem right to put this in the proxies since it should only ever be called from client-side code, and I'm
	// not about to make a whole separate utilities class just for one method. Fully qualified names it is!
	/**
	 * <b>[Client-side only]</b> Draws a textured rectangle, taking the size of the image and the bit needed into
	 * account, unlike {@link net.minecraft.client.gui.Gui#drawTexturedModalRect(int, int, int, int, int, int)
	 * Gui.drawTexturedModalRect(int, int, int, int, int, int)}, which is harcoded for only 256x256 textures. Also handy
	 * for custom potion icons.
	 * 
	 * @param x The x position of the rectangle
	 * @param y The y position of the rectangle
	 * @param u The x position of the top left corner of the section of the image wanted
	 * @param v The y position of the top left corner of the section of the image wanted
	 * @param width The width of the section
	 * @param height The height of the section
	 * @param textureWidth The width of the actual image.
	 * @param textureHeight The height of the actual image.
	 */
	@SideOnly(Side.CLIENT)
	public static void drawTexturedRect(int x, int y, int u, int v, int width, int height, int textureWidth,
			int textureHeight){

		float f = 1F / (float)textureWidth;
		float f1 = 1F / (float)textureHeight;

		// Essentially the same as getting the tessellator. For most code, you'll want the tessellator AND the
		// vertexbuffer
		// stored in local variables.
		BufferBuilder buffer = net.minecraft.client.renderer.Tessellator.getInstance()
				.getBuffer();
		// Equivalent of tessellator.startDrawingQuads()
		buffer.begin(org.lwjgl.opengl.GL11.GL_QUADS,
				net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_TEX);
		// Equivalent of tessellator.addVertex()
		buffer.pos((double)(x), (double)(y + height), 0)
				.tex((double)((float)(u) * f), (double)((float)(v + height) * f1)).endVertex();
		buffer.pos((double)(x + width), (double)(y + height), 0)
				.tex((double)((float)(u + width) * f), (double)((float)(v + height) * f1)).endVertex();
		buffer.pos((double)(x + width), (double)(y), 0).tex((double)((float)(u + width) * f), (double)((float)(v) * f1))
				.endVertex();
		buffer.pos((double)(x), (double)(y), 0).tex((double)((float)(u) * f), (double)((float)(v) * f1)).endVertex();
		// Exactly the same as before.
		net.minecraft.client.renderer.Tessellator.getInstance().draw();
	}

	/**
	 * Shorthand for {@link WizardryUtilities#drawTexturedRect(int, int, int, int, int, int, int, int)} which draws the
	 * entire texture (u and v are set to 0 and textureWidth and textureHeight are the same as width and height).
	 */
	@SideOnly(Side.CLIENT)
	public static void drawTexturedRect(int x, int y, int width, int height){
		drawTexturedRect(x, y, 0, 0, width, height, width, height);
	}

	// SECTION NBT and Data Storage
	// ===============================================================================================================

	/**
	 * Verifies that the given string is a valid string representation of a UUID. More specifically, returns true if and
	 * only if the given string is not null and matches the regular expression:
	 * <p>
	 * <center><code>/^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/<p></code></center>
	 * which is the regex equivalent of the standard string representation of a UUID as described in
	 * {@link UUID#toString()}. This method is intended to be used as a check to prevent an
	 * {@link IllegalArgumentException} from occurring when calling {@link UUID#fromString(String)}.
	 * 
	 * @param string The string to be checked
	 * @return Whether the given string is a valid string representation of a UUID
	 * @deprecated UUIDs can now be stored in NBT directly; use that in preference to storing them as strings.
	 */
	@Deprecated
	public static boolean verifyUUIDString(String string){
		return string != null
				&& string.matches("/^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/");
	}

	/**
	 * Generic method that stores any Map to an NBTTagList, given two functions that convert the key and value types in
	 * that map to subclasses of NBTBase. For what it's worth, there is very little point in using this unless you can
	 * use something more concise than an anonymous class to do the conversion. A lambda expression, or better, a method
	 * reference, would fit nicely. For example, take ExtendedPlayer's use of this to store conjured item durations:
	 * <p>
	 * <code>properties.setTag("conjuredItems", WizardryUtilities.mapToNBT(this.conjuredItemDurations,
	 * item -> new NBTTagInt(Item.getIdFromItem((Item)item)), NBTTagInt::new));</code>
	 * <p>
	 * This is a lot nicer than simply iterating through the map, because for that you need to use the entry list, which
	 * introduces local variables that aren't really necessary. Notice that, since the values V in the map are simply
	 * Integer objects, a simple constructor reference to NBTTagInt::new can be used instead of a lambda expression (the
	 * Integer is auto-unboxed to int).
	 * 
	 * @param <K> The type of key stored in the given Map.
	 * @param <V> The type of value stored in the given Map.
	 * @param <L> The subtype of NBTBase that the keys (of type K) will be converted to.
	 * @param <W> The subtype of NBTBase that the values (of type V) will be converted to.
	 * @param map The Map to be stored.
	 * @param keyFunction A Function that converts the keys in the map to NBT objects that can be stored.
	 * @param valueFunction A Function that converts the values in the map to NBT objects that can be stored.
	 * @param keyTagName The tag name to use for the key tags.
	 * @param valueTagName The tag name to use for the value tags.
	 * @return An NBTTagList that represents the given Map.
	 */
	public static <K, V, L extends NBTBase, W extends NBTBase> NBTTagList mapToNBT(Map<K, V> map,
			Function<K, L> keyFunction, Function<V, W> valueFunction, String keyTagName, String valueTagName){

		NBTTagList tagList = new NBTTagList();

		for(Entry<K, V> entry : map.entrySet()){
			NBTTagCompound mapping = new NBTTagCompound();
			mapping.setTag(keyTagName, keyFunction.apply(entry.getKey()));
			mapping.setTag(valueTagName, valueFunction.apply(entry.getValue()));
			tagList.appendTag(mapping);
		}

		return tagList;
	}

	/**
	 * See {@link WizardryUtilities#mapToNBT(Map, Function, Function, String, String)}; this version is for when the
	 * names of the individual key/value tags are unimportant (they default to "key" and "value" respectively).
	 */
	public static <K, V, L extends NBTBase, W extends NBTBase> NBTTagList mapToNBT(Map<K, V> map,
			Function<K, L> keyFunction, Function<V, W> valueFunction){
		return mapToNBT(map, keyFunction, valueFunction, "key", "value");
	}

	/**
	 * Generic method that reads a Map from an NBTTagList, given two functions that convert the key and value tag types
	 * into the key and value types in the returned map. The given NBTTagList remains unchanged after calling this
	 * method.
	 * 
	 * @param <K> The type of key stored in the returned Map.
	 * @param <V> The type of value stored in the returned Map.
	 * @param <L> The subtype of NBTBase that the keys are stored as.
	 * @param <W> The subtype of NBTBase that the values are stored as.
	 * @param tagList The NBTTagList to be converted. This <b>must</b> be a list of compound tags.
	 * @param keyFunction A Function that converts the generic NBTBase tags in the list to keys of type K for the map.
	 * @param valueFunction A Function that converts the generic NBTBase tags in the list to values of type V for the
	 *        map.
	 * @param keyTagName The tag name used for the key tags.
	 * @param valueTagName The tag name used for the value tags.
	 * @return A Map containing the keys and values stored in the given NBTTagList. Can be empty, but not null.
	 * @throws ClassCastException If the tags are not of the expected type.
	 * @see WizardryUtilities#mapToNBT(Map, Function, Function, String, String)
	 */
	@SuppressWarnings("unchecked") // Intentional, because throwing an exception is appropriate here.
	public static <K, V, L extends NBTBase, W extends NBTBase> Map<K, V> NBTToMap(NBTTagList tagList,
			Function<L, K> keyFunction, Function<W, V> valueFunction, String keyTagName, String valueTagName){

		Map<K, V> map = new HashMap<K, V>();

		for(int i = 0; i < tagList.tagCount(); i++){
			NBTTagCompound mapping = tagList.getCompoundTagAt(i);
			NBTBase keyTag = mapping.getTag(keyTagName);
			NBTBase valueTag = mapping.getTag(valueTagName);
			K key = null;
			try{
				key = keyFunction.apply((L)keyTag);
			}catch (ClassCastException e){
				Wizardry.logger.error(
						"Error when reading map from NBT: unexpected tag type " + NBTBase.NBT_TYPES[keyTag.getId()], e);
			}
			V value = null;
			try{
				value = valueFunction.apply((W)valueTag);
			}catch (ClassCastException e){
				Wizardry.logger.error(
						"Error when reading map from NBT: unexpected tag type " + NBTBase.NBT_TYPES[valueTag.getId()],
						e);
			}
			map.put(key, value);
		}

		return map;
	}

	/**
	 * See {@link WizardryUtilities#NBTToMap(NBTTagList, Function, Function, String, String)}; this version is for when
	 * the names of the individual key/value tags are unimportant (they default to "key" and "value" respectively).
	 */
	public static <K, V, L extends NBTBase, W extends NBTBase> Map<K, V> NBTToMap(NBTTagList tagList,
			Function<L, K> keyFunction, Function<W, V> valueFunction){
		return NBTToMap(tagList, keyFunction, valueFunction, "key", "value");
	}

	/**
	 * Generic method that stores any Collection to an NBTTagList, given a function that converts the elements in that
	 * collection to subclasses of NBTBase. For what it's worth, there is very little point in using this unless you can
	 * use something more concise than an anonymous class to do the conversion. A lambda expression, or better, a method
	 * reference, would fit nicely.
	 * 
	 * @param <E> The type of element stored in the given Collection.
	 * @param <T> The NBT tag type that the elements will be converted to.
	 * @param list The Collection to be stored.
	 * @param function A Function that converts the elements in the collection to NBT objects that can be stored.
	 * @return An NBTTagList that represents the given Collection.
	 */
	public static <E, T extends NBTBase> NBTTagList listToNBT(Collection<E> list, Function<E, T> function){

		NBTTagList tagList = new NBTTagList();
		// If the collection is ordered, it will preserve the order, even though we don't know what type it is yet.
		for(E element : list){
			tagList.appendTag(function.apply(element));
		}

		return tagList;
	}

	/**
	 * Generic method that reads a Collection from an NBTTagList, given a function that converts the element tag types
	 * to the element types in the returned collection. The given NBTTagList remains unchanged after calling this
	 * method. Unless the target variable for this method is of type Collection<E>, you will need to create a new
	 * collection containing the elements in the returned collection via that collection's constructor (e.g. {@code new
	 * HashSet<E>(collection)}). <i>Although this method returns a Collection rather than any of its subtypes, it uses
	 * an ArrayList internally to guarantee the order of the elements in the returned collection is the same as the
	 * order in which they were stored. As such, you may safely cast to List should you wish.</i>
	 * 
	 * @param <E> The type of element stored in the returned Collection.
	 * @param <T> The subtype of NBTBase that the elements are stored as.
	 * @param tagList The NBTTagList to be converted.
	 * @param function A Function that converts the generic NBTBase tags in the list to elements for the collection.
	 *        Chances are you will need to cast the NBTBase tag to whichever NBT tag type you are expecting in order to
	 *        access the appropriate getter method.
	 * @return A Collection containing the elements stored in the given NBTTagList. Can be empty, but not null.
	 * @throws ClassCastException If the tags are not of the expected type.
	 */
	@SuppressWarnings("unchecked") // Intentional, because throwing an exception is appropriate here.
	public static <E, T extends NBTBase> Collection<E> NBTToList(NBTTagList tagList, Function<T, E> function){
		// Uses an ArrayList to guarantee iteration order, and also to permit duplicate elements (which are
		// perfectly reasonable in this context).
		Collection<E> list = new ArrayList<E>();
		// The original tag list should remain unchanged, hence the copy.
		NBTTagList tagList2 = (NBTTagList)tagList.copy();

		while(!tagList2.hasNoTags()){
			NBTBase tag = tagList2.removeTag(0);
			// Why oh why is NBTTagList not parametrised? It even has a tagType field, so it must know!
			try{
				list.add(function.apply((T)tag));
			}catch (ClassCastException e){
				Wizardry.logger.error(
						"Error when reading list from NBT: unexpected tag type " + NBTBase.NBT_TYPES[tag.getId()], e);
			}
		}

		return list;

	}
	
	/** Removes the UUID with the given key from the given NBT tag, if any. Why this doesn't exist in vanilla I have
	 * no idea. */
	public static void removeUniqueId(NBTTagCompound tag, String key){
		tag.removeTag(key + "Most");
		tag.removeTag(key + "Least");
	}

	// TODO: Backport: It has recently become apparent that storing UUIDs as strings is not good practice, so backport
	// these two
	// methods to 1.7.10 and replace tag.setUniqueId and tag.getUniqueId with their respective contents from 1.10.2.

	/**
	 * Returns an NBTTagCompound which contains only the given UUID, stored using
	 * {@link NBTTagCompound#setUniqueId(String, UUID)}. Allows for neater storage to NBTTagLists.
	 */
	public static NBTTagCompound UUIDtoTagCompound(UUID id){
		NBTTagCompound tag = new NBTTagCompound();
		tag.setUniqueId("uuid", id);
		return tag;
	}

	/**
	 * Wrapper for {@link NBTTagCompound#getUniqueId(String)} which converts an NBTTagCompound directly to a UUID.
	 * Intended to be used as the inverse of {@link WizardryUtilities#UUIDtoTagCompound(UUID)}.
	 */
	public static UUID tagCompoundToUUID(NBTTagCompound tag){
		return tag.getUniqueId("uuid");
	}

	// SECTION Ally Designation System
	// ===============================================================================================================

	/**
	 * Returns whether the given target can be attacked by the given attacker. It is up to the caller of this method to
	 * work out what this means; it doesn't necessarily mean the target is completely immune (for example, revenge
	 * targeting might reasonably bypass this). This method is intended for use where the damage is indirect and/or
	 * unavoidable; direct attacks should not check this method. Currently this means the following situations check
	 * this method:
	 * <p>
	 * - AI targeting for summoned creatures<br>
	 * - AI targeting for mind-controlled creatures<br>
	 * - Constructs with an area of effect<br>
	 * - Instantaneous spells with an area of effect around the caster (e.g. forest's curse, thunderstorm)<br>
	 * - Any lightning chaining effects<br>
	 * - Any projectiles which seek targets
	 * <p>
	 * Also note that the friendly fire option is dealt with in the event handler. This method acts as a sort of wrapper
	 * for all the ADS stuff in {@link WizardData}; more details about the ally designation system can be found there.
	 * 
	 * @param attacker The entity that cast the spell originally
	 * @param target The entity being attacked
	 * 
	 * @return False under any of the following circumstances, true otherwise:
	 *         <p>
	 *         - The target is null
	 *         <p>
	 *         - The target is the attacker (this isn't as stupid as it sounds - anything with an AoE might cause this
	 *         to be true, as can summoned creatures)
	 *         <p>
	 *         - The target and the attacker are both players and the target is an ally of the attacker (but the
	 *         attacker need not be an ally of the target)
	 *         <p>
	 *         - The target is a creature that was summoned/controlled by the attacker or by an ally of the attacker.
	 *         <p>
	 *         <i>As of wizardry 4.1.2, this method now returns <b>true</b> instead of false if the attacker is null. This
	 *         is because in the vast majority of cases, it makes more sense this way: if a construct has no caster, it
	 *         should affect all entities; if a minion has no caster it should target all entities; etc.</i>
	 */
	public static boolean isValidTarget(Entity attacker, Entity target){

		// Always return true if the attacker is null
		if(attacker == null) return true;
		
		// Always return false if the target is null
		if(target == null) return false;

		// Tests whether the target is the attacker
		if(target == attacker) return false;

		// Tests whether the target is a creature that was summoned by the attacker
		if(target instanceof ISummonedCreature && ((ISummonedCreature)target).getCaster() == attacker){
			return false;
		}

		// Tests whether the target is a creature that was mind controlled by the attacker
		if(target instanceof EntityLiving && ((EntityLivingBase)target).isPotionActive(WizardryPotions.mind_control)){

			NBTTagCompound entityNBT = target.getEntityData();

			if(entityNBT != null && entityNBT.hasUniqueId(MindControl.NBT_KEY)){
				if(attacker == WizardryUtilities.getEntityByUUID(target.world,
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

			}else if(target instanceof ISummonedCreature){
				// Tests whether the target is a creature that was summoned by an ally of the attacker
				if(((ISummonedCreature)target).getCaster() instanceof EntityPlayer && WizardData.get((EntityPlayer)attacker)
								.isPlayerAlly((EntityPlayer)((ISummonedCreature)target).getCaster())){
					return false;
				}

			}else if(target instanceof EntityLiving && ((EntityLivingBase)target).isPotionActive(WizardryPotions.mind_control)){
				// Tests whether the target is a creature that was mind controlled by an ally of the attacker
				NBTTagCompound entityNBT = target.getEntityData();

				if(entityNBT != null && entityNBT.hasKey(MindControl.NBT_KEY)){

					Entity controller = WizardryUtilities.getEntityByUUID(target.world, entityNBT.getUniqueId(MindControl.NBT_KEY));

					if(controller instanceof EntityPlayer && WizardData.get((EntityPlayer)attacker).isPlayerAlly((EntityPlayer)controller)){
						return false;
					}
				}
			}
		}

		return true;
	}

	/** Helper method for testing if the second player is an ally of the first player. Makes the code neater. */
	public static boolean isPlayerAlly(EntityPlayer allyOf, EntityPlayer possibleAlly){

		WizardData properties = WizardData.get(allyOf);

		if(properties != null && properties.isPlayerAlly(possibleAlly)) return true;

		return false;
	}

	// SECTION Loot and Weighting
	// ===============================================================================================================

	/**
	 * See {@link WizardryUtilities#getStandardWeightedRandomSpellId(Random, boolean)}. nonContinuous defaults to false.
	 */
	public static int getStandardWeightedRandomSpellId(Random random){
		return getStandardWeightedRandomSpellId(random, false);
	}

	/**
	 * Helper method which gets a spell id according to the standard weighting. The tier is a weighted random value; the
	 * actual spell within that tier is completely random. Will not return the id of a spell which has been disabled in
	 * the config. This is for simple stuff like chests and drops; more complex generators like wizard trades don't use
	 * this method.
	 * <p>
	 * For reference, the standard weighting is as follows: Basic: 60%, Apprentice: 25%, Advanced: 10%, Master: 5%
	 * 
	 * @param random An instance of {@link Random} to use for RNG
	 * @param nonContinuous Whether the spells must be non-continuous (used for scrolls)
	 * @return A random spell id number
	 */
	public static int getStandardWeightedRandomSpellId(Random random, boolean nonContinuous){

		Tier tier = Tier.getWeightedRandomTier(random);

		List<Spell> spells = Spell.getSpells(new Spell.TierElementFilter(tier, null));
		if(nonContinuous) spells.retainAll(Spell.getSpells(Spell.nonContinuousSpells));

		// Ensures the tier chosen actually has spells in it, and if not uses BASIC instead.
		if(spells.isEmpty()){
			spells = Spell.getSpells(new Spell.TierElementFilter(Tier.BASIC, null));
			if(nonContinuous) spells.retainAll(Spell.getSpells(Spell.nonContinuousSpells));
		}

		// Finds a random spell in the list and returns its id.
		return spells.get(random.nextInt(spells.size())).id();
	}

	// TODO: These methods need a rethink. What are we trying to achieve with them? Should each use case look in the
	// same pool of items? For example, might we (or someone else) want to have a wand which can generate in chests, but
	// is not used by wizards?

	// I reckon this should be strictly for cases where we only ever want the standard wand set, i.e. wizards' gear,
	// etc.

	/**
	 * Helper method to return the appropriate armour item based on element and slot. As of Wizardry 2.1, this uses the
	 * immutable map stored in {@link WizardryItems#ARMOUR_MAP}. Currently used to iterate through armour for
	 * registering charging recipes and for chest generation.
	 * 
	 * @param element The EnumElement of the armour required. Null will be converted to {@link Element#MAGIC}.
	 * @param slot EntityEquipmentSlot of the armour piece required
	 * @return The armour item which corresponds to the given element and slot, or null if no such item exists.
	 * @throws IllegalArgumentException if the given slot is not an armour slot.
	 */
	public static Item getArmour(Element element, EntityEquipmentSlot slot){
		if(slot == null || slot.getSlotType() != Type.ARMOR)
			throw new IllegalArgumentException("Must be a valid armour slot");
		if(element == null) element = Element.MAGIC;
		return WizardryItems.ARMOUR_MAP.get(ImmutablePair.of(slot, element));
	}

	/**
	 * Helper method to return the appropriate wand based on tier and element.As of Wizardry 2.1, this uses the
	 * immutable map stored in {@link WizardryItems#WAND_MAP}. Currently used in the packet handler for upgrading wands,
	 * for chest generation and to iterate through wands for charging recipes.
	 * 
	 * @param tier The tier of the wand required.
	 * @param element The element of the wand required. Null will be converted to {@link Element#MAGIC}.
	 * @return The wand item which corresponds to the given element and slot, or null if no such item exists.
	 * @throws NullPointerException if the given tier is null.
	 */
	public static Item getWand(Tier tier, Element element){
		if(tier == null) throw new NullPointerException("The given tier cannot be null.");
		if(element == null) element = Element.MAGIC;
		return WizardryItems.WAND_MAP.get(ImmutablePair.of(tier, element));
	}
}
