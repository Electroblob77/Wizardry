package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.entity.living.ISpellCaster;
import electroblob.wizardry.integration.DamageSafetyChecker;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.EnumAction;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.function.BiFunction;

/**
 * Similar to {@link electroblob.wizardry.spell.SpellProjectile}, but for any {@link EntityThrowable}.
 * This allows all the relevant code to be centralised, since these spells all work in the same way. Usually, a simple
 * instantiation of this class is sufficient to create a projectile spell; if something extra needs to be done, such as
 * particle spawning, then methods can be overridden (perhaps using an anonymous class) to add the required functionality.
 * <p></p>
 * <i>N.B. It is advised that this class is only used where the projectile to be launched belongs to vanilla Minecraft
 * or another mod; no guarantees are made as to the behaviour of such projectiles!</i>
 * <p></p>
 * Properties added by this type of spell: {@link Spell#RANGE}
 * <p></p>
 * By default, this type of spell can be cast by NPCs. {@link Spell#canBeCastBy(EntityLiving, boolean)}
 * <p></p>
 * By default, this type of spell cannot be cast by dispensers. {@link Spell#canBeCastBy(TileEntityDispenser)}
 * <p></p>
 * By default, this type of spell does not require a packet to be sent. {@link Spell#requiresPacket()}
 *
 * @author Electroblob
 * @since Wizardry 4.2.8
 */
// TODO: Use events to make these projectiles seek targets when the caster is wearing a ring of attraction (is this possible?)
@EventBusSubscriber
public class SpellThrowable<T extends EntityThrowable> extends Spell {

	/** NBT key for storing a damage modifier in an external entity (i.e. from vanilla or another mod). Entities with a
	 * float value stored under this key will have their damage dealt multiplied by that value. This is not just for
	 * projectiles; it will work for any entity that uses a damage source with itself as the immediate source. */
	public static final String DAMAGE_MODIFIER_NBT_KEY = Wizardry.MODID + "DamageModifier";

	private static final float LAUNCH_Y_OFFSET = 0.1f;

	protected final BiFunction<World, EntityLivingBase, T> projectileFactory;

	public SpellThrowable(String name, BiFunction<World, EntityLivingBase, T> projectileFactory){
		this(Wizardry.MODID, name, projectileFactory);
	}

	public SpellThrowable(String modID, String name, BiFunction<World, EntityLivingBase, T> projectileFactory){
		super(modID, name, EnumAction.NONE, false);
		this.projectileFactory = projectileFactory;
		addProperties(RANGE);
		this.npcSelector((e, o) -> true);
	}

	@Override
	public boolean requiresPacket(){
		return false;
	}

	/** Trajectory calculation - see {@link SpellProjectile} for a more detailed explanation */
	protected float calculateVelocity(SpellModifiers modifiers, float launchHeight){
		float g = 0.03f;
		float range = getProperty(RANGE).floatValue() * modifiers.get(WizardryItems.range_upgrade);
		return range / MathHelper.sqrt(2 * launchHeight/g);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		if(!world.isRemote){
			float velocity = calculateVelocity(modifiers, caster.getEyeHeight() - LAUNCH_Y_OFFSET);
			T projectile = projectileFactory.apply(world, caster);
			projectile.shoot(caster, caster.rotationPitch, caster.rotationYaw, 0.0f, velocity, 1.0f);
			projectile.getEntityData().setFloat(DAMAGE_MODIFIER_NBT_KEY, modifiers.get(SpellModifiers.POTENCY));
			addProjectileExtras(projectile, caster, modifiers);
			world.spawnEntity(projectile);
		}

		this.playSound(world, caster, ticksInUse, -1, modifiers);
		caster.swingArm(hand);
		return true;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target, SpellModifiers modifiers){

		if(target != null){

			if(!world.isRemote){
				float velocity = calculateVelocity(modifiers, caster.getEyeHeight() - LAUNCH_Y_OFFSET);
				T projectile = projectileFactory.apply(world, caster);
				int aimingError = caster instanceof ISpellCaster ? ((ISpellCaster)caster).getAimingError(world.getDifficulty())
						: EntityUtils.getDefaultAimingError(world.getDifficulty());
				aim(projectile, caster, target, velocity, aimingError);
				addProjectileExtras(projectile, caster, modifiers);
				world.spawnEntity(projectile);
			}

			this.playSound(world, caster, ticksInUse, -1, modifiers);
			caster.swingArm(hand);
			return true;
		}

		return false;
	}

	// Copied from EntityMagicProjectile (ugh what a mess)
	private void aim(T throwable, EntityLivingBase caster, Entity target, float speed, float aimingError){

		throwable.ignoreEntity = caster;

		throwable.posY = caster.getEntityBoundingBox().minY + (double)caster.getEyeHeight() - LAUNCH_Y_OFFSET;
		double dx = target.posX - caster.posX;
		double dy = !throwable.hasNoGravity() ? target.getEntityBoundingBox().minY + (double)(target.height / 3.0f) - throwable.posY
				: target.getEntityBoundingBox().minY + (double)(target.height / 2.0f) - throwable.posY;
		double dz = target.posZ - caster.posZ;
		double horizontalDistance = MathHelper.sqrt(dx * dx + dz * dz);

		if(horizontalDistance >= 1.0E-7D){

			double dxNormalised = dx / horizontalDistance;
			double dzNormalised = dz / horizontalDistance;
			throwable.setPosition(caster.posX + dxNormalised, throwable.posY, caster.posZ + dzNormalised);

			// Depends on the horizontal distance between the two entities and accounts for bullet drop,
			// but of course if gravity is ignored throwable should be 0 since there is no bullet drop.
			float bulletDropCompensation = !throwable.hasNoGravity() ? (float)horizontalDistance * 0.2f : 0;
			// It turns out that throwable method normalises the input (x, y, z) anyway
			throwable.shoot(dx, dy + (double)bulletDropCompensation, dz, speed, aimingError);
		}
	}

	/**
	 * Does nothing by default, but can be overridden to call extra methods or set additional fields on the launched
	 * projectile.
	 */
	protected void addProjectileExtras(T projectile, EntityLivingBase caster, SpellModifiers modifiers){}

	@SubscribeEvent
	public static void onLivingAttackEvent(LivingAttackEvent event){

		// TODO: Annoyingly, wither skulls apply 'direct' damage from their shooter so they won't be recognised here
		if(!(event.getSource() instanceof MagicDamage)){ // Prevent infinite looping

			float damageModifier = event.getSource().getImmediateSource().getEntityData().getFloat(DAMAGE_MODIFIER_NBT_KEY);

			// Really, we just want to increase the damage without modifying the source, but that would cause an
			// infinite loop so we need some way of identifying it - the easiest way is to use MagicDamage, which fits
			// quite nicely since we can only get here if the entity was from a spell anyway
			if(damageModifier > 0){

				Entity projectile = event.getSource().getImmediateSource();
				Entity shooter = event.getSource().getTrueSource();

				DamageSource newSource = shooter == projectile
						? MagicDamage.causeDirectMagicDamage(projectile, DamageType.MAGIC)
						: MagicDamage.causeIndirectMagicDamage(projectile, shooter, DamageType.MAGIC);

				// Copy over any relevant 'attributes' the original DamageSource might have had.
				if(event.getSource().isExplosion()) newSource.setExplosion();
				if(event.getSource().isFireDamage()) newSource.setFireDamage();
				if(event.getSource().isProjectile()) newSource.setProjectile();

				DamageSafetyChecker.attackEntitySafely(event.getEntity(), newSource,
						event.getAmount() * damageModifier, event.getSource(), true);
			}
		}
	}

}