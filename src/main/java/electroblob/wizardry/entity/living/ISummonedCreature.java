package electroblob.wizardry.entity.living;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;

import electroblob.wizardry.WizardData;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.ItemWand;
import electroblob.wizardry.util.IElementalDamage;
import electroblob.wizardry.util.IndirectMinionDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.MinionDamage;
import electroblob.wizardry.util.WizardryParticleType;
import electroblob.wizardry.util.WizardryUtilities;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.EnumHand;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

/**
 * Interface for all summoned creatures. The code for summoned creatures has been overhauled in Wizardry 2.1, and this
 * interface allows summoned creatures to extend vanilla (or indeed modded) entity classes, so
 * <code>EntitySummonedZombie</code> now extends <code>EntityZombie</code>, for example. This change has two major
 * benefits:
 * <p>
 * - There is no longer any need for separate render classes, because summoned creatures are now instances of vanilla
 * types. <i>You don't even need to assign a render class</i> because the supertype should already be assigned the
 * correct one.<br>
 * - Summoned creature classes are now much more robust when it comes to changes between Minecraft versions, since none
 * of the vanilla code needs to be copied.
 * <p>
 * <b>Summoned creatures that do not emulate vanilla entities do not directly implement this interface</b>. Instead,
 * they should extend the abstract base implementation, {@link EntitySummonedCreature}.
 * <p>
 * All damage dealt by ISummonedCreature instances is redirected via
 * {@link ISummonedCreature#onLivingAttackEvent(net.minecraftforge.event.entity.living.LivingAttackEvent)
 * ISummonedCreature.onLivingAttackEvent(LivingAttackEvent)} and replaced by an instance of
 * {@link electroblob.wizardry.util.IElementalDamage IElementalDamage} with the summoner of that creature as the source
 * rather than the creature itself. This means that kills by summoned creatures register as kills for their owner,
 * dropping xp and rare loot if that owner is a player.
 * <p>
 * Though this system is a lot better than the previous system, <i>it is not a perfect solution</i>. The old
 * EntitySummonedCreature class overrode some methods from Entity in order to add shared functionality, but this cannot
 * be done with an interface. To get around this problem, this interface contains 5 delegate methods that do the same
 * things, with the aim of centralising as much code as possible, even though it is not automatically applied.
 * <b>Implementing classes must override the corresponding methods from Entity, and within them, call the appropriate
 * delegate method in this interface.</b> It is impossible to enforce this condition, but the summoned creature will not
 * work properly unless it is adhered to. <i>The position of the delegate method call is unimportant, but by convention
 * it is usually at the start of the calling method, which avoids it being unintentionally skipped by a return
 * statement (except for methods where the result of the delegate method should itself be returned).</i>
 * <p>
 * It is recommended that when implementing this interface, you begin by copying {@link EntitySummonedCreature} to
 * ensure all the relevant methods are duplicated. You can then change the superclass, override any additional methods
 * and add functionality to any that are already overridden. You will always want to override the AI methods at the very
 * least.
 * <p>
 * Due to the limitations of interfaces, some methods that really ought to be protected are public. These are clearly
 * marked as 'Internal, DO NOT CALL'. <b>Don't call them, only implement them.</b>
 * 
 * @since Wizardry 2.1
 * @author Electroblob
 */
/* Quite honestly, this is not what default methods are really for. However, this is modding, and in modding some
 * sacrifices have to be made when it comes to Java style - because adding on to a pre-existing program is not a good
 * way of doing this sort of thing anyway, but we have no choice about that! */
@Mod.EventBusSubscriber
public interface ISummonedCreature extends IEntityAdditionalSpawnData {

	// Remember that ALL fields are static and final in interfaces, even if they don't explicitly state that.
	String NAMEPLATE_TRANSLATION_KEY = "entity." + Wizardry.MODID + ":summonedcreature.nameplate";

	// Setters and getters. The subclass fields that these access should be private.

	/** Sets the lifetime of the summoned creature in ticks. */
	void setLifetime(int ticks);

	/**
	 * Returns the lifetime of the summoned creature in ticks. Allows primarily for duration multiplier support, but
	 * also for example the skeleton legion spell which lasts for 60 seconds instead of the usual 30. Syncing and saving
	 * is done automatically. As of Wizardry 2.1, despawning is handled in ISummonedCreature; see
	 * {@link ISummonedCreature#onDespawn()} for details.
	 */
	int getLifetime();

	/**
	 * Sets the WeakReference object which refers to the owner of this summoned creature. Internal, don't call unless
	 * you know what you are doing.
	 */
	void setCasterReference(WeakReference<EntityLivingBase> reference);

	/**
	 * Returns a WeakReference object which refers to the owner of this summoned creature. Subclasses should store this
	 * as a private field. This may be null; as such it is preferable to use {@link ISummonedCreature#getCaster()} to
	 * get the caster object itself.
	 */
	@Nullable
	WeakReference<EntityLivingBase> getCasterReference();

	/** Internal, DO NOT CALL. */
	void setCasterUUID(UUID uuid);

	/** Internal, DO NOT CALL. This is for loading purposes only and is not usually synchronised. */
	UUID getCasterUUID();

	/**
	 * Returns the EntityLivingBase that summoned this creature, or null if it no longer exists. Cases where the entity
	 * may no longer exist are: entity died or was deleted, mob despawned, player logged out, entity teleported to
	 * another dimension, or this creature simply had no caster in the first place. <i>This is the correct method to use
	 * to get the owner of this summoned creature.
	 */
	@Nullable
	default EntityLivingBase getCaster(){
		return getCasterReference() == null ? null : getCasterReference().get();
	}

	// Miscellaneous

	/**
	 * Called by the server when constructing the spawn packet. Data should be added to the provided stream.
	 * <b>Implementors must call super when overriding.</b>
	 *
	 * @param buffer The packet data stream
	 */
	@Override
	default void writeSpawnData(ByteBuf buffer){
		buffer.writeInt(getCaster() != null ? getCaster().getEntityId() : -1);
		buffer.writeInt(getLifetime());
	}

	/**
	 * Called by the client when it receives a Entity spawn packet. Data should be read out of the stream in the same
	 * way as it was written. <b>Implementors must call super when overriding.</b>
	 *
	 * @param additionalData The packet data stream
	 */
	@Override
	default void readSpawnData(ByteBuf buffer){
		int id = buffer.readInt();
		// We're on the client side here, so we can safely use Minecraft.getMinecraft().world via proxies.
		if(id > -1) setCasterReference(
				new WeakReference<EntityLivingBase>((EntityLivingBase)Wizardry.proxy.getTheWorld().getEntityByID(id)));
		setLifetime(buffer.readInt());
	}

	/**
	 * Shorthand for {@link WizardryUtilities#isValidTarget(Entity, Entity)}, with the owner of this creature as the
	 * attacker. Also allows implementors to override it if they wish to do so.
	 */
	default boolean isValidTarget(Entity target){
		return WizardryUtilities.isValidTarget(this.getCaster(), target);
	}

	/**
	 * Returns a entity selector to be passed into AI methods. Normally, this should not be overridden, but it is
	 * possible for implementors to override this in order to do something special when selecting a target.
	 */
	default Predicate<Entity> getTargetSelector(){

		return new Predicate<Entity>(){

			public boolean apply(Entity entity){
				// TODO: Backport invisibility check (also in wizards)
				// If the target is valid and not invisible...
				if(!entity.isInvisible() && isValidTarget(entity)){

					// ... and is a player, they can be attacked, since players can't be in the whitelist or the
					// blacklist.
					if(entity instanceof EntityPlayer) return true;

					// ... and is a mob, a summoned creature, a wizard ...
					if((entity instanceof IMob || entity instanceof ISummonedCreature
							|| (entity instanceof EntityWizard && !(getCaster() instanceof EntityWizard))
					// ... or in the whitelist ...
							|| Arrays.asList(Wizardry.settings.summonedCreatureTargetsWhitelist)
									.contains(EntityList.getKey(entity.getClass()).toString().toLowerCase(Locale.ROOT)))
							// ... and isn't in the blacklist ...
							&& !Arrays.asList(Wizardry.settings.summonedCreatureTargetsBlacklist)
									.contains(EntityList.getKey(entity.getClass()).toString().toLowerCase(Locale.ROOT))){
						// ... it can be attacked.
						return true;
					}
				}

				return false;
			}
		};
	}

	/**
	 * Called when this creature has existed for 1 tick, effectively when it has just been spawned. Normally used to add
	 * particles, sounds, etc.
	 */
	void onSpawn();

	/**
	 * Called when this summoned creature vanishes. Normally used to add particles, sounds, etc.
	 */
	void onDespawn();

	/** Whether this creature should spawn a subtle black swirl particle effect while alive. */
	boolean hasParticleEffect();

	/**
	 * Called from the event handler after the damage change is applied. Does nothing by default, but can be overridden
	 * to do something when a successful attack is made. This was added because the event-based damage source system can
	 * cause parts of attackEntityAsMob not to fire, since attackEntityFrom is intercepted and canceled.
	 * <p>
	 * Usage examples: {@link EntitySliverfishMinion} uses this to summon more silverfish if the target is killed,
	 * {@link EntitySkeletonMinion} and {@link EntitySpiderMinion} use this to add potion effects to the target.
	 */
	default void onSuccessfulAttack(EntityLivingBase target){
	};

	// Delegates

	/**
	 * Implementors should call this from writeEntityToNBT. Can be overridden as long as super is called, but there's
	 * very little point in doing that since anything extra could just be added to writeEntityToNBT anyway.
	 */
	default void writeNBTDelegate(NBTTagCompound tagcompound){
		if(this.getCaster() != null){
			tagcompound.setUniqueId("casterUUID", this.getCaster().getUniqueID());
		}
		tagcompound.setInteger("lifetime", getLifetime());
	}

	/**
	 * Implementors should call this from readEntityFromNBT. Can be overridden as long as super is called, but there's
	 * very little point in doing that since anything extra could just be added to readEntityFromNBT anyway.
	 */
	default void readNBTDelegate(NBTTagCompound tagcompound){
		this.setCasterUUID(tagcompound.getUniqueId("casterUUID"));
		this.setLifetime(tagcompound.getInteger("lifetime"));
	}

	/**
	 * Implementors should call this from setRevengeTarget, and call super.setRevengeTarget if and only if this method
	 * returns <b>true</b>.
	 */
	default boolean shouldRevengeTarget(EntityLivingBase entity){
		// Allows the config to prevent minions from revenge-targeting their owners.
		return entity != this.getCaster() || Wizardry.settings.minionRevengeTargeting;
	}

	/**
	 * Implementors should call this from onUpdate. Can be overridden as long as super is called, but there's very
	 * little point in doing that since anything extra could just be added to onUpdate anyway.
	 */
	default void updateDelegate(){

		if(!(this instanceof Entity))
			throw new ClassCastException("Implementations of ISummonedCreature must extend Entity!");

		Entity thisEntity = ((Entity)this);

		if(this.getCaster() == null && this.getCasterUUID() != null){
			Entity entity = WizardryUtilities.getEntityByUUID(thisEntity.world, getCasterUUID());
			if(entity instanceof EntityLivingBase){
				this.setCasterReference(new WeakReference<EntityLivingBase>((EntityLivingBase)entity));
			}
		}

		if(thisEntity.ticksExisted == 1){
			this.onSpawn();
		}

		if(thisEntity.ticksExisted > this.getLifetime() && this.getLifetime() != -1){
			this.onDespawn();
			thisEntity.setDead();
		}

		if(this.hasParticleEffect() && thisEntity.world.isRemote && thisEntity.world.rand.nextInt(8) == 0)
			Wizardry.proxy.spawnParticle(WizardryParticleType.DARK_MAGIC, thisEntity.world, thisEntity.posX,
					thisEntity.posY + thisEntity.world.rand.nextDouble() * 1.5, thisEntity.posZ, 0.0d, 0.0d, 0.0d, 0,
					0.1f, 0.0f, 0.0f);

	}

	/**
	 * Implementors should call this from processInteract, and call super.processInteract if and only if this method
	 * returns <b>false</b>.
	 */
	default boolean interactDelegate(EntityPlayer player, EnumHand hand){

		ItemStack stack = player.getHeldItem(hand);

		WizardData properties = WizardData.get(player);
		// Selects one of the player's minions.
		if(player.isSneaking() && stack.getItem() instanceof ItemWand){

			if(!player.world.isRemote && properties != null && this.getCaster() == player){

				if(properties.selectedMinion != null && properties.selectedMinion.get() == this){
					// Deselects the selected minion if right-clicked again
					properties.selectedMinion = null;
				}else{
					// Selects this minion
					properties.selectedMinion = new WeakReference<ISummonedCreature>(this);
				}
				properties.sync();
			}
			return true;
		}

		return false;
	}

	// Damage system

	@SubscribeEvent
	public static void onLivingAttackEvent(LivingAttackEvent event){

		// Rather than bother overriding entire attack methods in ISummonedCreature implementations, it's easier (and
		// more robust) to use LivingAttackEvent to modify the damage source.
		if(event.getSource().getTrueSource() instanceof ISummonedCreature){

			EntityLivingBase summoner = ((ISummonedCreature)event.getSource().getTrueSource()).getCaster();

			if(summoner != null){

				event.setCanceled(true);
				DamageSource newSource = event.getSource();
				// Copies over the original DamageType if appropriate.
				DamageType type = event.getSource() instanceof IElementalDamage
						? ((IElementalDamage)event.getSource()).getType()
						: DamageType.MAGIC;
				// Copies over the original isRetaliatory flag if appropriate.
				boolean isRetaliatory = event.getSource() instanceof IElementalDamage
						&& ((IElementalDamage)event.getSource()).isRetaliatory();

				// All summoned creatures are classified as magic, so it makes sense to do it this way.
				if(event.getSource() instanceof EntityDamageSourceIndirect){
					newSource = new IndirectMinionDamage(event.getSource().damageType,
							event.getSource().getImmediateSource(), event.getSource().getTrueSource(), summoner, type,
							isRetaliatory);
				}else if(event.getSource() instanceof EntityDamageSource){
					// Name is copied over so it uses the appropriate vanilla death message
					newSource = new MinionDamage(event.getSource().damageType, event.getSource().getTrueSource(), summoner,
							type, isRetaliatory);
				}

				// Copy over any relevant 'attributes' the original DamageSource might have had.
				if(event.getSource().isExplosion()) newSource.setExplosion();
				if(event.getSource().isFireDamage()) newSource.setFireDamage();
				if(event.getSource().isProjectile()) newSource.setProjectile();

				// For some reason Minecraft calculates knockback relative to DamageSource#getTrueSource. In vanilla this
				// is unnoticeable, but it looks a bit weird with summoned creatures involved - so this fixes that.
				if(WizardryUtilities.attackEntityWithoutKnockback(event.getEntity(), newSource, event.getAmount())){
					// Using event.getSource().getTrueSource() as this means the target is knocked back from the minion
					WizardryUtilities.applyStandardKnockback(event.getSource().getTrueSource(), event.getEntityLiving());
					((ISummonedCreature)event.getSource().getTrueSource()).onSuccessfulAttack(event.getEntityLiving());
					// If the target revenge-targeted the summoner, make it revenge-target the minion instead
					// (if it didn't revenge-target, do nothing)
					if(event.getEntityLiving().getRevengeTarget() == summoner
							&& event.getSource().getTrueSource() instanceof EntityLivingBase){
						event.getEntityLiving().setRevengeTarget((EntityLivingBase)event.getSource().getTrueSource());
					}
				}

			}
		}
	}

}
