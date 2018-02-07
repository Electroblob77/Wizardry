package electroblob.wizardry.entity.living;

import java.util.ArrayList;
import java.util.List;

import electroblob.wizardry.event.SpellCastEvent;
import electroblob.wizardry.event.SpellCastEvent.Source;
import electroblob.wizardry.packet.PacketNPCCastSpell;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.util.EnumHand;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * Entity AI class for use by instances of {@link ISpellCaster}. This deals with pathing, the spell casting itself and
 * the attack cooldown. Also provides an automatic implementation of continuous spell casting using the methods
 * specified in {@code ISpellCaster}; all the entity class needs to do is implement those methods.
 */
public class EntityAIAttackSpell extends EntityAIBase {

	/** The entity the AI instance has been applied to. */
	private final EntityLiving attacker;
	/** The entity the AI instance has been applied to, but as an ISpellCaster. */
	private final ISpellCaster caster;
	/** The tagret to be attacked. */
	private EntityLivingBase target;
	/**
	 * Decremented each tick while greater than 0. When a spell is cast, this is set to that spell's cooldown plus the
	 * base cooldown.
	 */
	private int cooldown;
	/**
	 * The number of ticks between the entity finding a new target and when it first starts attacking, and also the
	 * amount that is added to the spell's cooldown between casting spells.
	 */
	private final int baseCooldown;
	/**
	 * Decremented each tick while greater than 0. When a continuous spell is first cast, this is set to the value of
	 * {@link EntityAIAttackSpell#continuousSpellDuration}.
	 */
	// I think that in this case this is only necessary on the server side. If any inconsistent behaviour
	// occurs, look into syncing this as well.
	private int continuousSpellTimer;
	/** The number of ticks that continuous spells will be cast for before cooling down. */
	private final int continuousSpellDuration;
	/** The speed that the entity should move when attacking. Only used when passed into the navigator. */
	private final double speed;
	private int seeTime;
	private final float maxAttackDistance;

	/**
	 * Creates a new spell attack AI with the given parameters.
	 * 
	 * @param attacker The entity that that uses this AI.
	 * @param speed The speed that the entity should move when attacking. Only used when passed into the navigator.
	 * @param maxDistance The maximum distance the entity should be from its target.
	 * @param baseCooldown The number of ticks between the entity finding a new target and when it first starts
	 *        attacking, and also the amount that is added to the cooldown of the spell that has just been cast.
	 * @param continuousSpellDuration The number of ticks that continuous spells will be cast for before cooling down.
	 */
	public EntityAIAttackSpell(ISpellCaster attacker, double speed, float maxDistance, int baseCooldown,
			int continuousSpellDuration){

		this.cooldown = -1;

		if(!(attacker instanceof EntityLiving)){
			throw new IllegalArgumentException(
					"Tried to create an EntityAICastSpell for an entity that isn't an EntityLiving");
		}else{
			this.caster = attacker;
			this.attacker = (EntityLiving)attacker;
			this.baseCooldown = baseCooldown;
			this.continuousSpellDuration = continuousSpellDuration;
			this.speed = speed;
			this.maxAttackDistance = maxDistance * maxDistance;
			this.setMutexBits(3);
		}
	}

	@Override
	public boolean shouldExecute(){

		EntityLivingBase entitylivingbase = this.attacker.getAttackTarget();

		if(entitylivingbase == null){
			return false;
		}else{
			this.target = entitylivingbase;
			return true;
		}
	}

	@Override
	public boolean continueExecuting(){
		return this.shouldExecute() || !this.attacker.getNavigator().noPath();
	}

	@Override
	public void resetTask(){
		this.target = null;
		this.seeTime = 0;
		this.cooldown = -1;
		this.setContinuousSpellAndNotify(Spells.none, new SpellModifiers());
		this.continuousSpellTimer = 0;
	}

	private void setContinuousSpellAndNotify(Spell spell, SpellModifiers modifiers){
		caster.setContinuousSpell(spell);
		WizardryPacketHandler.net.sendToAllAround(
				new PacketNPCCastSpell.Message(attacker.getEntityId(), target == null ? -1 : target.getEntityId(),
						EnumHand.MAIN_HAND, spell.id(), modifiers),
				// Particles are usually only visible from 16 blocks away, so 128 is more than far enough.
				new TargetPoint(attacker.dimension, attacker.posX, attacker.posY, attacker.posZ, 128));
	}

	@Override
	public void updateTask(){

		// Only executed server side.

		double distanceSq = this.attacker.getDistanceSq(this.target.posX, this.target.getEntityBoundingBox().minY,
				this.target.posZ);
		boolean targetIsVisible = this.attacker.getEntitySenses().canSee(this.target);

		if(targetIsVisible){
			++this.seeTime;
		}else{
			this.seeTime = 0;
		}

		if(distanceSq <= (double)this.maxAttackDistance && this.seeTime >= 20){
			this.attacker.getNavigator().clearPathEntity();
		}else{
			this.attacker.getNavigator().tryMoveToEntityLiving(this.target, this.speed);
		}

		this.attacker.getLookHelper().setLookPositionWithEntity(this.target, 30.0F, 30.0F);

		if(this.continuousSpellTimer > 0){

			this.continuousSpellTimer--;

			// If the target goes out of range or out of sight...
			if(distanceSq > (double)this.maxAttackDistance || !targetIsVisible
			// ...or the spell is cancelled via events...
					|| MinecraftForge.EVENT_BUS
							.post(new SpellCastEvent.Tick(attacker, caster.getContinuousSpell(), caster.getModifiers(),
									Source.NPC, this.continuousSpellDuration - this.continuousSpellTimer))
					// ...or the spell no longer succeeds...
					|| !caster.getContinuousSpell().cast(attacker.world, attacker, EnumHand.MAIN_HAND,
							this.continuousSpellDuration - this.continuousSpellTimer, target, caster.getModifiers())
					// ...or the time has elapsed...
					|| this.continuousSpellTimer == 0){

				// ...reset the continuous spell timer and start the cooldown.
				this.continuousSpellTimer = 0;
				setContinuousSpellAndNotify(Spells.none, new SpellModifiers());
				this.cooldown = this.baseCooldown;
				return;

			}else if(this.continuousSpellDuration - this.continuousSpellTimer == 1){
				// On the first tick, if the spell did succeed, fire SpellCastEvent.Post.
				MinecraftForge.EVENT_BUS.post(new SpellCastEvent.Post(attacker, caster.getContinuousSpell(),
						caster.getModifiers(), Source.NPC));
			}

		}else if(--this.cooldown == 0){

			if(distanceSq > (double)this.maxAttackDistance || !targetIsVisible){
				return;
			}

			double dx = target.posX - attacker.posX;
			double dz = target.posZ - attacker.posZ;

			List<Spell> spells = new ArrayList<Spell>(caster.getSpells());

			if(spells.size() > 0){

				if(!attacker.world.isRemote){

					// New way of choosing a spell; keeps trying until one works or all have been tried

					Spell spell;

					while(!spells.isEmpty()){

						spell = spells.get(attacker.world.rand.nextInt(spells.size()));

						SpellModifiers modifiers = caster.getModifiers();

						if(spell != null && attemptCastSpell(spell, modifiers)){
							// The spell worked, so we're done!
							attacker.rotationYaw = (float)(Math.atan2(dz, dx) * 180.0D / Math.PI) - 90.0F;
							return;
						}else{
							spells.remove(spell);
						}
					}
				}
			}

		}else if(this.cooldown < 0){
			// This should only be reached when the entity first starts attacking. Stops it attacking instantly.
			this.cooldown = this.baseCooldown;
		}
	}

	/** Attempts to cast the given spell (including event firing) and returns true if it succeeded. */
	private boolean attemptCastSpell(Spell spell, SpellModifiers modifiers){

		// If anything stops the spell working at this point, nothing else happens.
		if(MinecraftForge.EVENT_BUS.post(new SpellCastEvent.Pre(attacker, spell, modifiers, Source.NPC))){
			return false;
		}

		if(spell.cast(attacker.world, attacker, EnumHand.MAIN_HAND, 0, target, modifiers)){

			if(spell.isContinuous){
				// -1 because the spell has been cast once already!
				this.continuousSpellTimer = this.continuousSpellDuration - 1;
				setContinuousSpellAndNotify(spell, modifiers);

			}else{

				MinecraftForge.EVENT_BUS.post(new SpellCastEvent.Post(attacker, spell, modifiers, Source.NPC));

				// For now, the cooldown is just added to the constant base cooldown. I think this
				// is a reasonable way of doing things; it's certainly better than before.
				this.cooldown = this.baseCooldown + spell.cooldown;

				if(spell.doesSpellRequirePacket()){
					// Sends a packet to all players in dimension to tell them to spawn particles.
					IMessage msg = new PacketNPCCastSpell.Message(attacker.getEntityId(), target.getEntityId(),
							EnumHand.MAIN_HAND, spell.id(), modifiers);
					WizardryPacketHandler.net.sendToDimension(msg, attacker.world.provider.getDimension());
				}
			}

			return true;
		}

		return false;
	}
}
