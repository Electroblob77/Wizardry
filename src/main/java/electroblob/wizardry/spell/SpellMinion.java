package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.entity.living.ISummonedCreature;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import electroblob.wizardry.util.WizardryUtilities.Operations;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * Generic superclass for all spells which summon minions (i.e. instances of {@link ISummonedCreature}).
 * This allows all the relevant code to be centralised, since these spells all work in the same way. Usually, a simple
 * instantiation of this class is sufficient to create a minion spell; if something extra needs to be done, such as
 * particle spawning, then methods can be overridden (perhaps using an anonymous class) to add the required functionality.
 * It is encouraged, however, to put extra functionality in the summoned creature class instead whenever possible.
 * <p></p>
 * Properties added by this type of spell: {@link SpellMinion#MINION_LIFETIME}
 * <p></p>
 * By default, this type of spell can be cast by NPCs. {@link Spell#canBeCastBy(EntityLiving, boolean)}
 * <p></p>
 * By default, this type of spell can be cast by dispensers. {@link Spell#canBeCastBy(TileEntityDispenser)}
 * <p></p>
 * By default, this type of spell does not require a packet to be sent. {@link Spell#requiresPacket()}
 * 
 * @author Electroblob
 * @since Wizardry 4.2
 */
public class SpellMinion<T extends EntityLiving & ISummonedCreature> extends Spell {

	public static final String MINION_LIFETIME = "minion_lifetime";
	public static final String MINION_COUNT = "minion_count";
	public static final String SUMMON_RADIUS = "summon_radius";

	/** The string identifier for the minion health spell modifier, which doubles as the identifier for the
	 * entity attribute modifier. */
	public static final String HEALTH_MODIFIER = "minion_health";
	/** The string identifier for the potency attribute modifier. */
	private static final String POTENCY_ATTRIBUTE_MODIFIER = "potency";
	
	/** A factory that creates summoned creature entities. */
	protected final Function<World, T> minionFactory;
	/** Whether the minions are spawned in mid-air. Defaults to false. */
	protected boolean flying = false;

	public SpellMinion(String name, Function<World, T> minionFactory){
		this(Wizardry.MODID, name, minionFactory);
	}

	public SpellMinion(String modID, String name, Function<World, T> minionFactory){
		super(modID, name, EnumAction.BOW, false);
		this.minionFactory = minionFactory;
		addProperties(MINION_LIFETIME, MINION_COUNT, SUMMON_RADIUS);
	}

	/**
	 * Sets whether the minions are spawned in mid-air.
	 * @param flying True to spawn the minions in mid-air, false to spawn them on the ground.
	 * @return The spell instance, allowing this method to be chained onto the constructor.
	 */
	public SpellMinion<T> flying(boolean flying){
		this.flying = flying;
		return this;
	}
	
	@Override public boolean requiresPacket(){ return false; }
	
	@Override public boolean canBeCastBy(EntityLiving npc, boolean override){ return true; }
	
	@Override public boolean canBeCastBy(TileEntityDispenser dispenser) { return true; }

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		if(!this.spawnMinions(world, caster, modifiers)) return false;
		this.playSound(world, caster, ticksInUse, -1, modifiers);
		return true;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target,
			SpellModifiers modifiers){

		if(!this.spawnMinions(world, caster, modifiers)) return false;
		this.playSound(world, caster, ticksInUse, -1, modifiers);
		return true;
	}
	
	@Override
	public boolean cast(World world, double x, double y, double z, EnumFacing direction, int ticksInUse, int duration, SpellModifiers modifiers){
		
		BlockPos pos = new BlockPos(x, y, z);
		
		// In this case it looks nice to have them all explode out from one position! (It also makes the code simpler...)
		if(!world.isRemote){
			for(int i=0; i<getProperty(MINION_COUNT).intValue(); i++){

				T minion = minionFactory.apply(world);

				// In this case we don't care whether the minions can fly or not.
				minion.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
				minion.setLifetime((int)(getProperty(MINION_LIFETIME).floatValue() * modifiers.get(WizardryItems.duration_upgrade)));
				this.addMinionExtras(minion, pos, null, modifiers, i);

				world.spawnEntity(minion);
			}
		}
		// This MUST be the coordinates of the actual dispenser, so we need to offset it
		this.playSound(world, x - direction.getXOffset(), y - direction.getYOffset(), z - direction.getZOffset(), ticksInUse, duration, modifiers);

		return true;
	}
	
	/**
	 * Actually spawns the minions. By default, this spawns the number of minions specified by the
	 * {@link SpellMinion#MINION_COUNT} property within a number of blocks of the caster specified by the property
	 * {@link SpellMinion#SUMMON_RADIUS}, returning false if there is no space to spawn the minions. Override to do
	 * something special, like spawning minions in a specific position.
	 * 
	 * @param world The world in which to spawn the minions.
	 * @param caster The entity that cast this spell, or null if it was cast by a dispenser.
	 * @param modifiers The spell modifiers this spell was cast with.
	 * @return False to cause the spell to fail, true to allow it to continue.
	 * 
	 * @see SpellMinion#addMinionExtras(EntityLiving, BlockPos, EntityLivingBase, SpellModifiers, int)
	 */
	// Protected since someone might want to extend this class and change the behaviour of this method.
	protected boolean spawnMinions(World world, EntityLivingBase caster, SpellModifiers modifiers){
		
		if(!world.isRemote){
			for(int i=0; i<getProperty(MINION_COUNT).intValue(); i++){

				int range = getProperty(SUMMON_RADIUS).intValue();

				// Try and find a nearby floor space
				BlockPos pos = WizardryUtilities.findNearbyFloorSpace(caster, range, range*2);

				if(flying){
					if(pos != null){
						// Make sure the flying entity spawns above the ground
						pos = pos.up(2); // Adding 2 will suffice, it's not exactly a game-changer...
					}else{
						// If there was no floor around to spawn them on, just pick any spot in mid-air
						pos = caster.getPosition().north(world.rand.nextInt(range*2) - range)
								.east(world.rand.nextInt(range*2) - range);
					}
				}else{
					// If there was no floor around and the entity isn't a flying one, the spell fails.
					// As per the javadoc for findNearbyFloorSpace, there's no point trying the rest of the minions.
					if(pos == null) return false;
				}
				
				T minion = createMinion(world, caster, modifiers);
				
				minion.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
				minion.setCaster(caster);
				// Modifier implementation
				// Attribute modifiers are pretty opaque, see https://minecraft.gamepedia.com/Attribute#Modifiers
				minion.setLifetime((int)(getProperty(MINION_LIFETIME).floatValue() * modifiers.get(WizardryItems.duration_upgrade)));
				IAttributeInstance attribute = minion.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE);
				if(attribute != null) attribute.applyModifier( // Apparently some things don't have an attack damage
						new AttributeModifier(POTENCY_ATTRIBUTE_MODIFIER, modifiers.get(SpellModifiers.POTENCY) - 1, Operations.MULTIPLY_CUMULATIVE));
				// This is only used for artefacts, but it's a nice example of custom spell modifiers
				minion.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).applyModifier(
						new AttributeModifier(HEALTH_MODIFIER, modifiers.get(HEALTH_MODIFIER) - 1, Operations.MULTIPLY_CUMULATIVE));
				minion.setHealth(minion.getMaxHealth()); // Need to set this because we may have just modified the value

				this.addMinionExtras(minion, pos, caster, modifiers, i);
				
				world.spawnEntity(minion);
			}
		}
		
		return true;
	}

	/**
	 * Creates and returns a new instance of this spell's minion entity. By default, this simply calls the apply
	 * method in {@link SpellMinion#minionFactory}. Override to add logic for changing the type of entity summoned.
	 *
	 * @param world The world in which to spawn the minion.
	 * @param caster The entity that cast this spell, or null if it was cast by a dispenser.
	 * @param modifiers The spell modifiers this spell was cast with.
	 * @return The resulting minion entity.
	 */
	protected T createMinion(World world, @Nullable EntityLivingBase caster, SpellModifiers modifiers){
		return minionFactory.apply(world);
	}
	
	/**
	 * Called just before each minion is spawned. Calls {@link EntityLiving#onInitialSpawn(DifficultyInstance, IEntityLivingData)}
	 * by default, but subclasses can override to call extra methods on the summoned entity, for example to add
	 * special equipment. This method is only called server-side so cannot be used to spawn particles directly.
	 * @param minion The entity being spawned.
	 * @param pos The position at which the entity was spawned.
	 * @param caster The caster of this spell, or null if it was cast by a dispenser.
	 * @param modifiers The modifiers this spell was cast with.
	 * @param alreadySpawned The number of minions already spawned, before this one. Always less than the property
	 * {@link SpellMinion#MINION_COUNT}.
	 */
	protected void addMinionExtras(T minion, BlockPos pos, @Nullable EntityLivingBase caster, SpellModifiers modifiers, int alreadySpawned){
		minion.onInitialSpawn(minion.world.getDifficultyForLocation(pos), null);
	}

}
