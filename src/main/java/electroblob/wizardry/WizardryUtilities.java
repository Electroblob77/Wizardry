package electroblob.wizardry;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import cpw.mods.fml.common.registry.EntityRegistry;
import electroblob.wizardry.entity.living.EntitySummonedCreature;
import electroblob.wizardry.entity.living.EntityWizard;
import electroblob.wizardry.spell.MindControl;
import electroblob.wizardry.spell.Spell;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityList.EntityEggInfo;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

/** This class contains some useful static methods for use anywhere - items, entities, spells, events, blocks, etc.
 * @see CommonProxy
 * @see WandHelper
 * @since Wizardry 1.0 */
public final class WizardryUtilities {

	/**
	 * Finds the nearest floor level to the given y coord within the range specified at the given x and z coords.
	 * Liquids and other blocks that cannot be built on top of do not count, but stuff like signs does.
	 * (Technically any block is allowed to be the floor according to the code, but seeing as it searches upwards and
	 * non-solid blocks usually need a supporting block, the floor is likely to always be solid).
	 * @param world
	 * @param x The x coordinate to search in
	 * @param y The y coordinate to search from
	 * @param z The z coordinate to search in
	 * @param range The maximum distance from the given y coordinate to search.
	 * @return The y coordinate of the closest floor level, or -1 if there is none. Returns the actual level of the floor
	 * as would be seen in the debug screen when the player is standing on it.
	 * @see WizardryUtilities#getNearestFloorLevelB(World, int, int, int, int)
	 */
	public static int getNearestFloorLevel(World world, int x, int y, int z, int range){
		int yCoord = -2;
		for(int i=y-range;i<=y+range;i++){
			// The last bit determines whether the block found to be a suitable floor is closer than the previous one found.
			// Edit: Changed the first condition from blockExists() since that method includes air blocks!
			if(world.doesBlockHaveSolidTopSurface(world, x, i, z) && (world.isAirBlock(x, i+1, z) || !world.doesBlockHaveSolidTopSurface(world, x, i+1, z))
					&& (i-y < yCoord-y || yCoord == -2)){
				yCoord = i;
			}
		}
		return yCoord + 1;
	}

	/**
	 * Returns whether the block at the given coordinates can be replaced by another one (works as if a block is being placed by a player).
	 * True for air, liquids, vines, tall grass and snow layers but not for flowers, signs etc.
	 * This is a shortcut for world.getBlock(x, y, z).isReplaceable(world, x, y, z);
	 * Not much of a shortcut any more, since block ids have been phased out.
	 * @see WizardryUtilities#canBlockBeReplacedB(World, int, int, int)
	 */
	public static boolean canBlockBeReplaced(World world, int x, int y, int z){
		// Why does isReplaceable have those arguments? I don't know. They aren't used. It's probably for overriding
		// stuff in case these arguments affect whether it is replaceable or not. It is a forge method, after all.
		return world.isAirBlock(x, y, z) || world.getBlock(x, y, z).isReplaceable(world, x, y, z);
	}

	/**
	 * Returns whether the block at the given coordinates can be replaced by another one (works as if a block is being placed by a player)
	 * and is not a liquid.
	 * True for air, vines, tall grass and snow layers but not for flowers, signs etc. or any liquids.
	 * @see WizardryUtilities#canBlockBeReplaced(World, int, int, int)
	 */
	public static boolean canBlockBeReplacedB(World world, int x, int y, int z){
		return (world.isAirBlock(x, y, z) || world.getBlock(x, y, z).isReplaceable(world, x, y, z)) && !world.getBlock(x, y, z).getMaterial().isLiquid();
	}

	/**
	 * Returns whether the block at the given coordinates is unbreakable in survival mode. In vanilla this is true for
	 * bedrock and end portal frame, for example.
	 * This is a shortcut for world.getBlockId(x, y, z).getBlockHardness == -1.0f;
	 * Not much of a shortcut any more, since block ids have been phased out.
	 */
	public static boolean isBlockUnbreakable(World world, int x, int y, int z){
		return world.isAirBlock(x, y, z) ? false : world.getBlock(x, y, z).getBlockHardness(world, x, y, z) == -1.0f;
	}


	/**
	 * Finds the nearest floor level to the given y coord within the range specified at the given x and z coords.
	 * Only works if the block above the floor is actually air and the floor is solid or a liquid.
	 * @param world
	 * @param x The x coordinate to search in
	 * @param y The y coordinate to search from
	 * @param z The z coordinate to search in
	 * @param range The maximum distance from the given y coordinate to search.
	 * @return The y coordinate of the closest floor level, or -1 if there is none. Returns the actual level of the floor
	 * as would be seen in the debug screen when the player is standing on it.
	 * @see WizardryUtilities#getNearestFloorLevel(World, int, int, int, int)
	 */
	public static int getNearestFloorLevelB(World world, int x, int y, int z, int range){
		int yCoord = -2;
		for(int i=y-range;i<=y+range;i++){
			if(world.isAirBlock(x, i+1, z) && (world.getBlock(x, i, z).getMaterial().isLiquid() || world.doesBlockHaveSolidTopSurface(world, x, i, z))
					&& (i-y < yCoord-y || yCoord == -2)){
				// The last bit determines whether the block found to be a suitable floor is closer than the previous one found.
				yCoord = i;
			}
		}
		return yCoord + 1;
	}
	
	/**
	 * Finds the nearest floor level to the given y coord within the range specified at the given x and z coords.
	 * Everything that is not air is treated as floor, even stuff that can't be walked on.
	 * @param world
	 * @param x The x coordinate to search in
	 * @param y The y coordinate to search from
	 * @param z The z coordinate to search in
	 * @param range The maximum distance from the given y coordinate to search.
	 * @return The y coordinate of the closest floor level, or -1 if there is none. Returns the actual level of the floor
	 * as would be seen in the debug screen when the player is standing on it.
	 * @see WizardryUtilities#getNearestFloorLevel(World, int, int, int, int)
	 */
	public static int getNearestFloorLevelC(World world, int x, int y, int z, int range){
		int yCoord = -2;
		for(int i=y-range;i<=y+range;i++){
			if(world.isAirBlock(x, i+1, z) && (i-y < yCoord-y || yCoord == -2)){
				// The last bit determines whether the block found to be a suitable floor is closer than the previous one found.
				yCoord = i;
			}
		}
		return yCoord + 1;
	}

	/**
	 * Gets the block the specified entity is standing on. Uses {@link MathHelper#floor_double(double)} because casting to int will not
	 * return the correct coordinate when x or z is negative.
	 */
	public static Block getBlockEntityIsStandingOn(Entity entity){
		return entity.worldObj.getBlock(MathHelper.floor_double(entity.posX), (int)WizardryUtilities.getEntityFeetPos(entity)-1, MathHelper.floor_double(entity.posZ));
	}

	/**
	 * Shorthand for {@link WizardryUtilities#getEntitiesWithinRadius(double, double, double, double, World, Class)}
	 * with EntityLivingBase as the entity type. This is by far the most common use for that method, which is why this
	 * shorthand exists.
	 * @param radius The search radius
	 * @param x The x coordinate to search around
	 * @param y The y coordinate to search around
	 * @param z The z coordinate to search around
	 * @param world The world to search in
	 */
	public static List<EntityLivingBase> getEntitiesWithinRadius(double radius, double x, double y, double z, World world){
		return getEntitiesWithinRadius(radius, x, y, z, world, EntityLivingBase.class);
	}

	/**
	 * Returns all entities of the specified type within the specified radius of the given coordinates. This is different to using
	 * a raw AABB because a raw AABB will search in a cube volume rather than a sphere.
	 * Note that this does not exclude any entities; if any specific entities are to be excluded this must be
	 * checked when iterating through the list.
	 * @see {@link WizardryUtilities#getEntitiesWithinRadius(double, double, double, double, World)}
	 * @param radius The search radius
	 * @param x The x coordinate to search around
	 * @param y The y coordinate to search around
	 * @param z The z coordinate to search around
	 * @param world The world to search in
	 * @param entityType The class of entity to search for; pass in Entity.class for all entities
	 */
	public static <T extends Entity> List<T> getEntitiesWithinRadius(double radius, double x, double y, double z, World world, Class<T> entityType){
		AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(x - radius, y - radius, z - radius, x + radius, y + radius, z + radius);
		List entityList = world.getEntitiesWithinAABB(entityType, aabb);
		for(int i=0;i<entityList.size();i++){
			if(((Entity)entityList.get(i)).getDistance(x, y, z) > radius){
				entityList.remove(i);
			}
		}
		return entityList;
	}

	// ^ Handy syntax using type parameters here!
	// I *think* that eclipse, for whatever reason, was not showing an 'unchecked' warning here. If you think about it,
	// if this method returns a generic list type I could, in theory, put strings or something in it and then when
	// invoking the method store its result as a List<EntityLivingBase>, which would cause a ClassCastException. Using
	// this handy syntax prevents that from happening. TL;DR: Always use type parameters when available, they're there
	// for a reason. Minecraft is bad in this respect because it doesn't use them in places like World.

	/**
	 * Gets an entity from its UUID. Note that you should check this isn't null.
	 * @param world The world the entity is in
	 * @param id The entity's UUID
	 * @return The Entity that has the given UUID, or null if no such entity exists in the specified world.
	 */
	public static Entity getEntityByUUID(World world, UUID id){
		for(Object object : world.loadedEntityList){
			if(object instanceof Entity){
				Entity entity = (Entity)object;
				// This is a perfect example of where you need to use .equals() and not ==. For most applications,
				// this was unnoticeable until world reload because the UUID instance or entity instance is stored.
				// Fixed now though. Of course, you could alternatively compare the string representations.
				if(entity.getUniqueID().equals(id)){
					return entity;
				}
			}
		}
		return null;
	}

	/**
	 * Does a block ray trace (NOT entities) from an entity's eyes (i.e. properly...)
	 */
	public static MovingObjectPosition rayTrace(double range, World world, EntityLivingBase entity, boolean hitLiquids)
	{
		Vec3 vec3 = Vec3.createVectorHelper(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ);
		Vec3 vec31 = entity.getLookVec();
		Vec3 vec32 = vec3.addVector(vec31.xCoord * range, vec31.yCoord * range, vec31.zCoord * range);
		return world.rayTraceBlocks(vec3, vec32, hitLiquids);
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
	public static MovingObjectPosition standardEntityRayTrace(World world, EntityLivingBase entity, double range){
		double dx = entity.getLookVec().xCoord * range;
		double dy = entity.getLookVec().yCoord * range;
		double dz = entity.getLookVec().zCoord * range;
		HashSet hashset = new HashSet(1);
		hashset.add(entity);
		return WizardryUtilities.tracePath(world, (float)entity.posX, (float)(entity.posY + entity.getEyeHeight()), (float)entity.posZ, (float)(entity.posX + dx), (float)(entity.posY + entity.getEyeHeight() + dy), (float)(entity.posZ + dz), 1.0f, hashset, false);
	}

	/**
	 * Helper method which does a rayTrace for entities from a entity's eye level in the direction they are looking
	 * with a specified range and radius, using the tracePath method. Tidies up the code a bit.
	 * 
	 * @param world
	 * @param entity
	 * @param range
	 * @param borderSize
	 * @return
	 */
	public static MovingObjectPosition standardEntityRayTrace(World world, EntityLivingBase entity, double range, float borderSize){
		double dx = entity.getLookVec().xCoord * range;
		double dy = entity.getLookVec().yCoord * range;
		double dz = entity.getLookVec().zCoord * range;
		HashSet hashset = new HashSet(1);
		hashset.add(entity);
		return WizardryUtilities.tracePath(world, (float)entity.posX, (float)(entity.posY + entity.getEyeHeight()), (float)entity.posZ, (float)(entity.posX + dx), (float)(entity.posY + entity.getEyeHeight() + dy), (float)(entity.posZ + dz), borderSize, hashset, false);
	}

	/**
	 * Method for ray tracing entities (the useless default method doesn't work, despite EnumHitType having an ENTITY field...)
	 * You can also use this for seeking.
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
	 * @return a MovingObjectPosition of either the block hit (no entity hit), the entity hit (hit an entity), or null for nothing hit
	 */
	public static MovingObjectPosition tracePath(World world, float x, float y, float z, float tx, float ty, float tz, float borderSize, HashSet<Entity> excluded, boolean collideablesOnly){

		Vec3 startVec = Vec3.createVectorHelper(x, y, z);
		Vec3 lookVec = Vec3.createVectorHelper(tx-x, ty-y, tz-z);
		Vec3 endVec = Vec3.createVectorHelper(tx, ty, tz);
		float minX = x < tx ? x : tx;
		float minY = y < ty ? y : ty;
		float minZ = z < tz ? z : tz;
		float maxX = x > tx ? x : tx;
		float maxY = y > ty ? y : ty; 
		float maxZ = z > tz ? z : tz;
		AxisAlignedBB bb = AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ).expand(borderSize, borderSize, borderSize);
		List<Entity> allEntities = world.getEntitiesWithinAABBExcludingEntity(null, bb);  
		MovingObjectPosition blockHit = world.rayTraceBlocks(startVec, endVec);
		startVec = Vec3.createVectorHelper(x, y, z);
		endVec = Vec3.createVectorHelper(tx, ty, tz);
		float maxDistance = (float) endVec.distanceTo(startVec);
		if(blockHit!=null)
		{
			maxDistance = (float) blockHit.hitVec.distanceTo(startVec);
		}  
		Entity closestHitEntity = null;
		float closestHit = maxDistance;
		float currentHit = 0.f;
		AxisAlignedBB entityBb;// = ent.getBoundingBox();
		MovingObjectPosition intercept;
		for(Entity ent : allEntities)
		{    
			if((ent.canBeCollidedWith() || !collideablesOnly) && ((excluded != null && !excluded.contains(ent)) || excluded == null))
			{
				float entBorder =  ent.getCollisionBorderSize();
				entityBb = ent.boundingBox;
				if(entityBb!=null)
				{
					entityBb = entityBb.expand(entBorder, entBorder, entBorder);
					intercept = entityBb.calculateIntercept(startVec, endVec);
					if(intercept!=null)
					{
						currentHit = (float) intercept.hitVec.distanceTo(startVec);
						if(currentHit < closestHit || currentHit==0)
						{            
							closestHit = currentHit;
							closestHitEntity = ent;
						}
					} 
				}
			}
		}  
		if(closestHitEntity!=null)
		{
			blockHit = new MovingObjectPosition(closestHitEntity);
		}
		return blockHit;
	}

	// Just what benefit does having posY be the eye position on the first person client actually give?

	/**
	 * Gets the y coordinate of the given player's eyes. This is to cover an inconsistency between the value of
	 * EntityPlayer.posY on the first person client and everywhere else; in first person (i.e. when
	 * Minecraft.getMinecraft().thePlayer == player) player.posY is the eye position, but everywhere else it is the
	 * feet position. This is intended for use when spawning particles, since this is the only situation where the
	 * discrepancy is likely to matter.
	 */
	public static double getPlayerEyesPos(EntityPlayer player){
		return Wizardry.proxy.getPlayerEyesPos(player);
	}

	/**
	 * Gets the y coordinate of the given entity's feet. This is to cover an inconsistency between the value of
	 * EntityPlayer.posY on the first person client and everywhere else; in first person (i.e. when
	 * Minecraft.getMinecraft().thePlayer == player) player.posY is the eye position, but everywhere else it is the
	 * feet position. This is intended for use when spawning particles, since this is the only situation where the
	 * discrepancy is likely to matter.
	 */
	public static double getEntityFeetPos(Entity entity){
		if(entity instanceof EntityPlayer){
			// For some reason, EntityPlayer.getEyeHeight() always returns 0.12, so I have replicated the behaviour
			// of the same method from EntityLivingBase and subtracted that instead.
			return getPlayerEyesPos((EntityPlayer)entity) - entity.height*0.85f;
		}else{
			return entity.posY;
		}
	}
	
	/**
	 * Verifies that the given string is a valid string representation of a UUID. More specifically, returns true if and
	 * only if the given string is not null and matches the regular expression:<p>
	 * <center><code>/^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/<p></code></center>
	 * which is the regex equivlent of the standard string representation of a UUID as described in {@link UUID#toString()}.
	 * This method is intended to be used as a check to prevent an {@link IllegalArgumentException} from occurring when
	 * calling {@link UUID#fromString(String)}.
	 * @param string The string to be checked
	 * @return Whether the given string is a valid string representation of a UUID
	 */
	public static boolean verifyUUIDString(String string){
		return string != null && string.matches("/^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$/");
	}

	/**
	 * Returns whether the given target can be attacked by the given attacker. It is up to the caller of this method
	 * to work out what this means; it doesn't necessarily mean the target is completely immune (for example, revenge
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
	 * Also note that the friendly fire option is dealt with
	 * in the event handler. This method acts as a sort of wrapper for all the ADS stuff in {@link ExtendedPlayer}; more
	 * details about the ally designation system can be found there.
	 * 
	 * @param attacker The entity that cast the spell originally
	 * @param target The entity being attacked
	 * 
	 * @return False under any of the following circumstances, true otherwise:
	 * <p>
	 * - The target is the attacker (this isn't as stupid as it sounds - anything with an AoE might cause this to be
	 * true, as can summoned creatures)
	 * <p>
	 * - The target and the attacker are both players and the target is an ally of the attacker (but the
	 * attacker need not be an ally of the target)
	 * <p>
	 * - The target is a creature that was summoned/controlled by the attacker or by an ally of the attacker.
	 */
	public static boolean isValidTarget(Entity attacker, Entity target){

		// Don't need to check if either entity is null since instanceof does this anyway.
		
		// Tests whether the target is the attacker
		if(target == attacker) return false;

		// Tests whether the target is a creature that was summoned by the attacker
		if(target instanceof EntitySummonedCreature && ((EntitySummonedCreature)target).getCaster() == attacker){
			return false;
		}

		// Tests whether the target is a creature that was mind controlled by the attacker
		if(target instanceof EntityLiving && ((EntityLivingBase)target).isPotionActive(Wizardry.mindControl)){

			NBTTagCompound entityNBT = target.getEntityData();

			if(entityNBT != null && entityNBT.hasKey(MindControl.NBT_KEY)){
				if(attacker == WizardryUtilities.getEntityByUUID(target.worldObj, UUID.fromString(entityNBT.getString(MindControl.NBT_KEY)))){
					return false;
				}
			}
		}
		
		// Ally section
		if(attacker instanceof EntityPlayer && ExtendedPlayer.get((EntityPlayer)attacker) != null){

			if(target instanceof EntityPlayer){
				// Tests whether the target is an ally of the attacker
				if(ExtendedPlayer.get((EntityPlayer)attacker).isPlayerAlly((EntityPlayer)target)){
					return false;
				}

			}else if(target instanceof EntitySummonedCreature){
				// Tests whether the target is a creature that was summoned by an ally of the attacker
				if(((EntitySummonedCreature)target).getCaster() instanceof EntityPlayer
						&& ExtendedPlayer.get((EntityPlayer)attacker).isPlayerAlly((EntityPlayer)((EntitySummonedCreature)target).getCaster())){
					return false;
				}
				
			}else if(target instanceof EntityLiving && ((EntityLivingBase)target).isPotionActive(Wizardry.mindControl)){
				// Tests whether the target is a creature that was mind controlled by an ally of the attacker
				NBTTagCompound entityNBT = target.getEntityData();
				
				if(entityNBT != null && entityNBT.hasKey(MindControl.NBT_KEY)){
					
					Entity controller = WizardryUtilities.getEntityByUUID(target.worldObj, UUID.fromString(entityNBT.getString(MindControl.NBT_KEY)));
					
					if(controller instanceof EntityPlayer && ExtendedPlayer.get((EntityPlayer)attacker).isPlayerAlly((EntityPlayer)controller)){
						return false;
					}
				}
			}
		}

		return true;
	}

	/** Helper method for testing if the second player is an ally of the first player. Makes the code neater. */
	public static boolean isPlayerAlly(EntityPlayer allyOf, EntityPlayer possibleAlly) {

		ExtendedPlayer properties = ExtendedPlayer.get(allyOf);

		if(properties != null && properties.isPlayerAlly(possibleAlly)) return true;

		return false;
	}

	/**
	 * See {@link WizardryUtilities#getStandardWeightedRandomSpellId(Random, boolean)}. isContinuous defaults to false.
	 */
	public static int getStandardWeightedRandomSpellId(Random random){
		return getStandardWeightedRandomSpellId(random, false);
	}

	/**
	 * Helper method which gets a spell id according to the standard weighting. The tier is a weighted random value;
	 * the actual spell within that tier is completely random. Will not return the id of a spell which has been
	 * disabled in the config. This is for simple stuff like chests and drops; more complex generators like wizard
	 * trades don't use this method.
	 * <p>
	 * For reference, the standard weighting is as follows:
	 * Basic: 60%, Apprentice: 25%, Advanced: 10%, Master: 5%
	 * 
	 * @param random An instance of the Random class
	 * @param nonContinuous Whether the spells must be non-continuous (used for scrolls)
	 * @return A random spell id number
	 */
	public static int getStandardWeightedRandomSpellId(Random random, boolean nonContinuous){

		int randomiser = random.nextInt(20);
		EnumTier tier;

		if(randomiser < 12){
			tier = EnumTier.BASIC;
		}else if(randomiser < 17){
			tier = EnumTier.APPRENTICE;
		}else if(randomiser < 19){
			tier = EnumTier.ADVANCED;
		}else{
			tier = EnumTier.MASTER;
		}

		List<Spell> spells = Spell.getSpells(new Spell.TierElementFilter(tier, null));
		if(nonContinuous) spells.retainAll(Spell.getSpells(Spell.nonContinuousSpells));

		// Ensures the tier chosen actually has spells in it, and if not uses BASIC instead. BASIC always has at least
		// the NONE spell since this spell cannot be disabled.
		if(spells.isEmpty()){
			spells = Spell.getSpells(new Spell.TierElementFilter(EnumTier.BASIC, null));
			if(nonContinuous) spells.retainAll(Spell.getSpells(Spell.nonContinuousSpells));
		}

		// Finds a random spell in the list and returns its id.
		return spells.get(random.nextInt(spells.size())).id();
	}

	/**
	 * Helper method to return the appropriate armour item based on element and slot.
	 * Currently used to iterate through armour for registering charging recipes and for chest generation.
	 * @param slot 0 = head, 1 = body, 2 = legs, 3 = feet
	 */
	public static Item getArmour(EnumElement element, int slot){
		Item armour = null;
		if(element == null){
			switch(slot){
			case 0:
				armour = Wizardry.wizardHat;
				break;
			case 1:
				armour = Wizardry.wizardRobe;
				break;
			case 2:
				armour = Wizardry.wizardLeggings;
				break;
			case 3:
				armour = Wizardry.wizardBoots;
				break;
			default:
				break;
			}
		}else{
			switch(element){
			case FIRE:
				switch(slot){
				case 0:
					armour = Wizardry.wizardHatFire;
					break;
				case 1:
					armour = Wizardry.wizardRobeFire;
					break;
				case 2:
					armour = Wizardry.wizardLeggingsFire;
					break;
				case 3:
					armour = Wizardry.wizardBootsFire;
					break;
				default:
					break;
				}
				break;
			case HEALING:
				switch(slot){
				case 0:
					armour = Wizardry.wizardHatHealing;
					break;
				case 1:
					armour = Wizardry.wizardRobeHealing;
					break;
				case 2:
					armour = Wizardry.wizardLeggingsHealing;
					break;
				case 3:
					armour = Wizardry.wizardBootsHealing;
					break;
				default:
					break;
				}
				break;
			case ICE:
				switch(slot){
				case 0:
					armour = Wizardry.wizardHatIce;
					break;
				case 1:
					armour = Wizardry.wizardRobeIce;
					break;
				case 2:
					armour = Wizardry.wizardLeggingsIce;
					break;
				case 3:
					armour = Wizardry.wizardBootsIce;
					break;
				default:
					break;
				}
				break;
			case LIGHTNING:
				switch(slot){
				case 0:
					armour = Wizardry.wizardHatLightning;
					break;
				case 1:
					armour = Wizardry.wizardRobeLightning;
					break;
				case 2:
					armour = Wizardry.wizardLeggingsLightning;
					break;
				case 3:
					armour = Wizardry.wizardBootsLightning;
					break;
				default:
					break;
				}
				break;
			case NECROMANCY:
				switch(slot){
				case 0:
					armour = Wizardry.wizardHatNecromancy;
					break;
				case 1:
					armour = Wizardry.wizardRobeNecromancy;
					break;
				case 2:
					armour = Wizardry.wizardLeggingsNecromancy;
					break;
				case 3:
					armour = Wizardry.wizardBootsNecromancy;
					break;
				default:
					break;
				}
				break;
			case SORCERY:
				switch(slot){
				case 0:
					armour = Wizardry.wizardHatSorcery;
					break;
				case 1:
					armour = Wizardry.wizardRobeSorcery;
					break;
				case 2:
					armour = Wizardry.wizardLeggingsSorcery;
					break;
				case 3:
					armour = Wizardry.wizardBootsSorcery;
					break;
				default:
					break;
				}
				break;
			case EARTH:
				switch(slot){
				case 0:
					armour = Wizardry.wizardHatEarth;
					break;
				case 1:
					armour = Wizardry.wizardRobeEarth;
					break;
				case 2:
					armour = Wizardry.wizardLeggingsEarth;
					break;
				case 3:
					armour = Wizardry.wizardBootsEarth;
					break;
				default:
					break;
				}
				break;
			default:
				switch(slot){
				case 0:
					armour = Wizardry.wizardHat;
					break;
				case 1:
					armour = Wizardry.wizardRobe;
					break;
				case 2:
					armour = Wizardry.wizardLeggings;
					break;
				case 3:
					armour = Wizardry.wizardBoots;
					break;
				default:
					break;
				}
				break;
			}
		}
		return armour;
	}

	/**
	 * Helper method to return the appropriate wand based on tier and element.
	 * Currently used in the packet handler for upgrading wands, for chest generation and to iterate through
	 * wands for charging recipes.
	 */
	public static Item getWand(EnumTier tier, EnumElement element){
		Item wand = null;
		if(element == null){
			switch(tier){
			case BASIC:
				wand = Wizardry.magicWand;
				break;
			case APPRENTICE:
				wand = Wizardry.apprenticeWand;
				break;
			case ADVANCED:
				wand = Wizardry.advancedWand;
				break;
			case MASTER:
				wand = Wizardry.masterWand;
				break;
			default:
				break;
			}
		}else{
			switch(element){
			case FIRE:
				switch(tier){
				case BASIC:
					wand = Wizardry.basicFireWand;
					break;
				case APPRENTICE:
					wand = Wizardry.apprenticeFireWand;
					break;
				case ADVANCED:
					wand = Wizardry.advancedFireWand;
					break;
				case MASTER:
					wand = Wizardry.masterFireWand;
					break;
				default:
					break;
				}
				break;
			case HEALING:
				switch(tier){
				case BASIC:
					wand = Wizardry.basicHealingWand;
					break;
				case APPRENTICE:
					wand = Wizardry.apprenticeHealingWand;
					break;
				case ADVANCED:
					wand = Wizardry.advancedHealingWand;
					break;
				case MASTER:
					wand = Wizardry.masterHealingWand;
					break;
				default:
					break;
				}
				break;
			case ICE:
				switch(tier){
				case BASIC:
					wand = Wizardry.basicIceWand;
					break;
				case APPRENTICE:
					wand = Wizardry.apprenticeIceWand;
					break;
				case ADVANCED:
					wand = Wizardry.advancedIceWand;
					break;
				case MASTER:
					wand = Wizardry.masterIceWand;
					break;
				default:
					break;
				}
				break;
			case LIGHTNING:
				switch(tier){
				case BASIC:
					wand = Wizardry.basicLightningWand;
					break;
				case APPRENTICE:
					wand = Wizardry.apprenticeLightningWand;
					break;
				case ADVANCED:
					wand = Wizardry.advancedLightningWand;
					break;
				case MASTER:
					wand = Wizardry.masterLightningWand;
					break;
				default:
					break;
				}
				break;
			case NECROMANCY:
				switch(tier){
				case BASIC:
					wand = Wizardry.basicNecromancyWand;
					break;
				case APPRENTICE:
					wand = Wizardry.apprenticeNecromancyWand;
					break;
				case ADVANCED:
					wand = Wizardry.advancedNecromancyWand;
					break;
				case MASTER:
					wand = Wizardry.masterNecromancyWand;
					break;
				default:
					break;
				}
				break;
			case SORCERY:
				switch(tier){
				case BASIC:
					wand = Wizardry.basicSorceryWand;
					break;
				case APPRENTICE:
					wand = Wizardry.apprenticeSorceryWand;
					break;
				case ADVANCED:
					wand = Wizardry.advancedSorceryWand;
					break;
				case MASTER:
					wand = Wizardry.masterSorceryWand;
					break;
				default:
					break;
				}
				break;
			case EARTH:
				switch(tier){
				case BASIC:
					wand = Wizardry.basicEarthWand;
					break;
				case APPRENTICE:
					wand = Wizardry.apprenticeEarthWand;
					break;
				case ADVANCED:
					wand = Wizardry.advancedEarthWand;
					break;
				case MASTER:
					wand = Wizardry.masterEarthWand;
					break;
				default:
					break;
				}
				break;
			default:
				switch(tier){
				case BASIC:
					wand = Wizardry.magicWand;
					break;
				case APPRENTICE:
					wand = Wizardry.apprenticeWand;
					break;
				case ADVANCED:
					wand = Wizardry.advancedWand;
					break;
				case MASTER:
					wand = Wizardry.masterWand;
					break;
				default:
					break;
				}
				break;
			}
		}
		return wand;
	}

}
