package electroblob.wizardry.spell;

import java.util.List;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryParticleType;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumAction;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

public class Shockwave extends Spell {

	public Shockwave(){
		super(Tier.MASTER, 65, Element.SORCERY, "shockwave", SpellType.ATTACK, 150, EnumAction.BOW, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(
				5.0d * modifiers.get(WizardryItems.blast_upgrade), caster.posX, caster.posY, caster.posZ, world);

		for(EntityLivingBase target : targets){
			if(WizardryUtilities.isValidTarget(caster, target)){
				// Damage increases closer to player up to a maximum of 4 hearts (at 1 block distance).
				float damage = Math.min(8.0f / target.getDistanceToEntity(caster), 8.0f);
				target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.BLAST),
						damage * modifiers.get(SpellModifiers.DAMAGE));

				if(!world.isRemote){

					// Entity speed increases closer to the player to a maximum of 3 (at 1 block distance).
					// This is the entity's speed compared to its distance from the player. Used for a similar triangles
					// based x, y and z speed calculation.
					double velocityFactor = Math.min(5 / target.getDistanceSqToEntity(caster), 3.0d);

					double dx = target.posX - caster.posX;
					double dy = target.posY + 1 - caster.posY;
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
				particleX = caster.posX - 1.0d + 2 * world.rand.nextDouble();
				particleZ = caster.posZ - 1.0d + 2 * world.rand.nextDouble();
				Wizardry.proxy.spawnParticle(WizardryParticleType.SPARKLE, world, particleX,
						WizardryUtilities.getPlayerEyesPos(caster) - 1.5, particleZ, particleX - caster.posX, 0,
						particleZ - caster.posZ, 30, 0.8f, 0.8f, 1.0f);
				particleX = caster.posX - 1.0d + 2 * world.rand.nextDouble();
				particleZ = caster.posZ - 1.0d + 2 * world.rand.nextDouble();
				Wizardry.proxy.spawnParticle(WizardryParticleType.SPARKLE, world, particleX,
						WizardryUtilities.getPlayerEyesPos(caster) - 1.5, particleZ, particleX - caster.posX, 0,
						particleZ - caster.posZ, 30, 0.9f, 0.9f, 0.9f);
				particleX = caster.posX - 1.0d + 2 * world.rand.nextDouble();
				particleZ = caster.posZ - 1.0d + 2 * world.rand.nextDouble();

				IBlockState block = WizardryUtilities.getBlockEntityIsStandingOn(caster);

				if(block != null){
					world.spawnParticle(EnumParticleTypes.BLOCK_DUST, particleX, caster.getEntityBoundingBox().minY,
							particleZ, particleX - caster.posX, 0, particleZ - caster.posZ, Block.getStateId(block));
				}
			}

			world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, caster.posX,
					caster.getEntityBoundingBox().minY + 0.1, caster.posZ, 0, 0, 0);

		}
		caster.swingArm(hand);
		WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_SHOCKWAVE, 1.0f, 0.7f);
		WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_SHOCKWAVE, 2.0f, 0.3f);
		return true;
	}

}
