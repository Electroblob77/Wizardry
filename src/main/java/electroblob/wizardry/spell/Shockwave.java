package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.*;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import java.util.List;

public class Shockwave extends Spell {

	public static final String MAX_REPULSION_VELOCITY = "max_repulsion_velocity";
	/** The radius within which maximum damage is dealt and maximum repulsion velocity is applied. */
	private static final double EPICENTRE_RADIUS = 1;

	public Shockwave(){
		super("shockwave", SpellActions.POINT_DOWN, false);
		this.soundValues(2, 0.5f, 0);
		addProperties(BLAST_RADIUS, DAMAGE, MAX_REPULSION_VELOCITY);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		double radius = getProperty(BLAST_RADIUS).floatValue() * modifiers.get(WizardryItems.blast_upgrade);

		List<EntityLivingBase> targets = EntityUtils.getLivingWithinRadius(radius, caster.posX, caster.posY, caster.posZ, world);

		for(EntityLivingBase target : targets){

			if(target instanceof EntityPlayer){

				Wizardry.proxy.shakeScreen((EntityPlayer)target, 10);

				if(!Wizardry.settings.playersMoveEachOther) continue;

				if(ItemArtefact.isArtefactActive((EntityPlayer)target, WizardryItems.amulet_anchoring)){
					if(!world.isRemote) caster.sendStatusMessage(new TextComponentTranslation("spell.resist",
							target.getName(), this.getNameForTranslationFormatted()), true);
					continue;
				}
			}

			if(AllyDesignationSystem.isValidTarget(caster, target)){

				// Produces a linear profile from 0 at the edge of the radius to 1 at the epicentre radius, then
				// a constant value of 1 within the epicentre radius.
				float proximity = (float)(1 - (Math.max(target.getDistance(caster) - EPICENTRE_RADIUS, 0))/(radius - EPICENTRE_RADIUS));

				// Damage increases closer to player up to a maximum of 4 hearts (at 1 block distance).
				target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.BLAST),
						getProperty(DAMAGE).floatValue() * proximity * modifiers.get(SpellModifiers.POTENCY));

				if(!world.isRemote){

					// Entity speed increases closer to the player to a maximum of 3 (at 1 block distance).
					// This is the entity's speed compared to its distance from the player. Used for a similar triangles
					// based x, y and z speed calculation.
					double velocityFactor = proximity * getProperty(MAX_REPULSION_VELOCITY).floatValue();

					double dx = target.posX - caster.posX;
					double dy = target.getEntityBoundingBox().minY + 1 - caster.posY;
					double dz = target.posZ - caster.posZ;

					target.motionX = velocityFactor * dx;
					target.motionY = velocityFactor * dy;
					target.motionZ = velocityFactor * dz;

					// Player motion is handled on that player's client so needs packets
					if(target instanceof EntityPlayerMP){
						((EntityPlayerMP)target).connection.sendPacket(new SPacketEntityVelocity(target));
					}
				}
			}
		}
		
		if(world.isRemote){

			double particleX, particleZ;
			
			for(int i = 0; i < 40; i++){

//				particleX = caster.posX - 1.0d + 2 * world.rand.nextDouble();
//				particleZ = caster.posZ - 1.0d + 2 * world.rand.nextDouble();
//				ParticleBuilder.create(Type.SPARKLE).pos(particleX, caster.getEntityBoundingBox().minY, particleZ)
//				.vel(particleX - caster.posX, 0, particleZ - caster.posZ).time(30).clr(0.8f, 0.8f, 1).spawn(world);
//
//				particleX = caster.posX - 1.0d + 2 * world.rand.nextDouble();
//				particleZ = caster.posZ - 1.0d + 2 * world.rand.nextDouble();
//				ParticleBuilder.create(Type.SPARKLE).pos(particleX, caster.getEntityBoundingBox().minY, particleZ)
//				.vel(particleX - caster.posX, 0, particleZ - caster.posZ).time(30).clr(0.9f, 0.9f, 0.9f).spawn(world);

				particleX = caster.posX - 1.0d + 2 * world.rand.nextDouble();
				particleZ = caster.posZ - 1.0d + 2 * world.rand.nextDouble();
				IBlockState block = BlockUtils.getBlockEntityIsStandingOn(caster);

				if(block != null){
					world.spawnParticle(EnumParticleTypes.BLOCK_DUST, particleX, caster.getEntityBoundingBox().minY,
							particleZ, particleX - caster.posX, 0, particleZ - caster.posZ, Block.getStateId(block));
				}
			}

			ParticleBuilder.create(Type.SPHERE)
					.pos(caster.posX, caster.getEntityBoundingBox().minY + 0.1, caster.posZ)
					.scale((float)radius * 0.8f)
					.clr(0.8f, 0.9f, 1)
					.spawn(world);

			world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, caster.posX,
					caster.getEntityBoundingBox().minY + 0.1, caster.posZ, 0, 0, 0);

		}
		
		caster.swingArm(hand);
		playSound(world, caster, ticksInUse, -1, modifiers);
		return true;
	}

}
