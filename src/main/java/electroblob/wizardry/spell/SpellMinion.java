package electroblob.wizardry.spell;

import java.util.function.Function;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.living.ISummonedCreature;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Generic superclass for all spells which summon minions (i.e. instances of {@link ISummonedCreature}).
 * This allows all the relevant code to be centralised, since these spells all work in the same way. Usually, a simple
 * instantiation of this class is sufficient to create a projectile spell; if something extra needs to be done, such as
 * particle spawning, then methods can be overridden (perhaps using an anonymous class) to add the required functionality.
 * It is encouraged, however, to put extra functionality in the summoned creature class instead whenever possible.
 * <p>
 * By default, this type of spell can be cast by NPCs. {@link Spell#canBeCastByNPCs()}
 * <p>
 * By default, this type of spell does not require a packet to be sent. {@link Spell#doesSpellRequirePacket()}
 * 
 * @author Electroblob
 * @since Wizardry 4.2
 */
// Adding a type parameter to this class seems to work very nicely!
public class SpellMinion<T extends Entity & ISummonedCreature> extends Spell {
	
	/** A factory that creates summoned creature entities. */
	protected final Function<World, T> minionFactory;
	/** The base lifetime of the summoned creature. */
	protected final int baseDuration;
	/** The sound that gets played when this spell is cast. */
	protected final SoundEvent sound;
	
	/** The volume of the sound played when this spell is cast. Defaults to 1. */
	protected float volume = 1;
	/** The pitch of the sound played when this spell is cast. Defaults to 1. */
	protected float pitch = 1;
	/** The pitch variation of the sound played when this spell is cast. Defaults to 0. */
	protected float pitchVariation = 0;
	/** The number of minions to summon. Defaults to 1. */
	protected int quantity = 1;
	/** The maximum number of blocks from the caster in a single direction that the minions can spawn, defaults to 2. */
	protected int range = 2;

	public SpellMinion(String name, Tier tier, Element element, int cost, int cooldown, Function<World, T> minionFactory, int baseDuration, SoundEvent sound){
		this(Wizardry.MODID, name, tier, element, cost, cooldown, minionFactory, baseDuration, sound);
	}

	public SpellMinion(String modID, String name, Tier tier, Element element, int cost, int cooldown, Function<World, T> minionFactory, int baseDuration, SoundEvent sound){
		super(modID, name, tier, element, SpellType.MINION, cost, cooldown, EnumAction.BOW, false);
		this.minionFactory = minionFactory;
		this.baseDuration = baseDuration;
		this.sound = sound;
	}
	
	/**
	 * Sets the sound parameters for this spell.
	 * @param volume 
	 * @param pitch
	 * @param pitchVariation
	 * @return The spell instance, allowing this method to be chained onto the constructor.
	 */
	public SpellMinion<T> soundValues(float volume, float pitch, float pitchVariation) {
		this.volume = volume;
		this.pitch = pitch;
		this.pitchVariation = pitchVariation;
		return this;
	}
	
	/**
	 * Sets the number of minions this spell will summon.
	 * @param quantity The number of minions to summon.
	 * @return The spell instance, allowing this method to be chained onto the constructor.
	 */
	public SpellMinion<T> quantity(int quantity){
		this.quantity = quantity;
		return this;
	}
	
	/**
	 * Sets the range within which minions can spawn.
	 * @param range The maximum number of blocks from the caster that minions can be spawned.
	 * @return The spell instance, allowing this method to be chained onto the constructor.
	 */
	public SpellMinion<T> range(int range){
		this.range = range;
		return this;
	}
	
	@Override public boolean doesSpellRequirePacket(){ return false; }
	
	@Override public boolean canBeCastByNPCs(){ return true; }

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		if(!this.spawnMinions(world, caster, modifiers)) return false;
		WizardryUtilities.playSoundAtPlayer(caster, sound, volume, pitch + pitchVariation * (world.rand.nextFloat() - 0.5f));
		return true;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target,
			SpellModifiers modifiers){

		if(!this.spawnMinions(world, caster, modifiers)) return false;
		caster.playSound(sound, volume, pitch + pitchVariation * (world.rand.nextFloat() - 0.5f));
		return true;
	}
	
	/** Actually spawns the minions. By default, this spawns the number of minions specified by
	 * {@link SpellMinion#quantity} within a number of blocks of the caster specified by {@link SpellMinion#range},
	 * returning false if there is no space to spawn the minions. Returning false from this method causes the spell to
	 * fail. Override to do something special, like spawning mnions in a specific position.
	 * @see SpellMinion#addMinionExtras(Entity, EntityLivingBase, SpellModifiers, int) */
	// Protected since someone might want to extend this class and change the behaviour of this method.
	protected boolean spawnMinions(World world, EntityLivingBase caster, SpellModifiers modifiers){
		
		if(!world.isRemote){
			for(int i=0; i<quantity; i++){
				
				BlockPos pos = WizardryUtilities.findNearbyFloorSpace(caster, range, range*2);
				if(pos == null) return false;
				
				T minion = minionFactory.apply(world);
				
				minion.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
				minion.setCaster(caster);
				minion.setLifetime((int)(baseDuration * modifiers.get(WizardryItems.duration_upgrade)));
				
				this.addMinionExtras(minion, caster, modifiers, i);
				
				world.spawnEntity(minion);
			}
		}
		
		return true;
	}
	
	/**
	 * Called just before each minion is spawned. Does nothing by default, but is provided to allow subclasses to call
	 * extra methods on the summoned entity, for example to add equipment. This method is only called server-side so
	 * cannot be used to spawn particles directly.
	 * @param minion The entity being spawned.
	 * @param caster The caster of this spell.
	 * @param modifiers The modifiers this spell was cast with.
	 * @param alreadySpawned The number of minions already spawned, before this one. Always less than
	 * {@link SpellMinion#quantity}.
	 */
	protected void addMinionExtras(T minion, EntityLivingBase caster, SpellModifiers modifiers, int alreadySpawned){}

}
