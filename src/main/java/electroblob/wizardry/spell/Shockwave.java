package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class Shockwave extends SpellAreaEffect {

	public static final String MAX_REPULSION_VELOCITY = "max_repulsion_velocity";
	/** The radius within which maximum damage is dealt and maximum repulsion velocity is applied. */
	private static final double EPICENTRE_RADIUS = 1;

	public Shockwave(){
		super("shockwave", SpellActions.POINT_DOWN, false);
		this.soundValues(2, 0.5f, 0);
		this.alwaysSucceed(true);
		addProperties(DAMAGE, MAX_REPULSION_VELOCITY);
	}

	@Override
	protected boolean affectEntity(World world, Vec3d origin, @Nullable EntityLivingBase caster, EntityLivingBase target, int targetCount, int ticksInUse, SpellModifiers modifiers){

		float radius = getProperty(EFFECT_RADIUS).floatValue() * modifiers.get(WizardryItems.blast_upgrade);

		if(target instanceof EntityPlayer){

			if(!Wizardry.settings.playersMoveEachOther) return false;

			if(ItemArtefact.isArtefactActive((EntityPlayer)target, WizardryItems.amulet_anchoring)){
				if(!world.isRemote && caster instanceof EntityPlayer) ((EntityPlayer)caster).sendStatusMessage(
						new TextComponentTranslation("spell.resist", target.getName(),
								this.getNameForTranslationFormatted()), true);
				return false;
			}
		}

		// Produces a linear profile from 0 at the edge of the radius to 1 at the epicentre radius, then
		// a constant value of 1 within the epicentre radius.
		float proximity = (float)(1 - (Math.max(origin.distanceTo(target.getPositionVector()) - EPICENTRE_RADIUS, 0))/(radius - EPICENTRE_RADIUS));

		// Damage increases closer to player up to a maximum of 4 hearts (at 1 block distance).
		target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.BLAST),
				getProperty(DAMAGE).floatValue() * proximity * modifiers.get(SpellModifiers.POTENCY));

		if(!world.isRemote){

			// Entity speed increases closer to the player to a maximum of 3 (at 1 block distance).
			// This is the entity's speed compared to its distance from the player. Used for a similar triangles
			// based x, y and z speed calculation.
			double velocityFactor = proximity * getProperty(MAX_REPULSION_VELOCITY).floatValue();

			double dx = target.posX - origin.x;
			double dy = target.getEntityBoundingBox().minY + 1 - origin.y;
			double dz = target.posZ - origin.z;

			target.motionX = velocityFactor * dx;
			target.motionY = velocityFactor * dy;
			target.motionZ = velocityFactor * dz;

			// Player motion is handled on that player's client so needs packets
			if(target instanceof EntityPlayerMP){
				((EntityPlayerMP)target).connection.sendPacket(new SPacketEntityVelocity(target));
			}
		}

		return true;
	}

	@Override
	protected void spawnParticleEffect(World world, Vec3d origin, double radius, @Nullable EntityLivingBase caster, SpellModifiers modifiers){

		// Can't put this in affectEntity(...) because it's only called for non-allies, plus here is client-side already
		EntityUtils.getEntitiesWithinRadius(radius, origin.x, origin.y, origin.z, world, EntityPlayer.class)
				.forEach(p -> Wizardry.proxy.shakeScreen(p, 10));

		double particleX, particleZ;

		for(int i = 0; i < 40; i++){

			particleX = origin.x - 1.0d + 2 * world.rand.nextDouble();
			particleZ = origin.z - 1.0d + 2 * world.rand.nextDouble();

			IBlockState block = world.getBlockState(new BlockPos(origin.x, origin.y - 0.5, origin.z));

			if(block != null){
				world.spawnParticle(EnumParticleTypes.BLOCK_DUST, particleX, origin.y,
						particleZ, particleX - origin.x, 0, particleZ - origin.z, Block.getStateId(block));
			}
		}

		ParticleBuilder.create(Type.SPHERE).pos(origin.add(0, 0.1, 0)).scale((float)radius * 0.8f).clr(0.8f, 0.9f, 1).spawn(world);

		world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, origin.x, origin.y + 0.1, origin.z, 0, 0, 0);
	}

}
