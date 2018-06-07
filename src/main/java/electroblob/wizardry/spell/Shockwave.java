package electroblob.wizardry.spell;

import java.util.List;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.MagicDamage;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.MagicDamage.DamageType;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumAction;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.world.World;

public class Shockwave extends Spell {

	public Shockwave() {
		super(EnumTier.MASTER, 65, EnumElement.SORCERY, "shockwave", EnumSpellType.ATTACK, 150, EnumAction.bow, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(5.0d*blastMultiplier, caster.posX, caster.posY, caster.posZ, world);
		
		for(EntityLivingBase target : targets){
			if(WizardryUtilities.isValidTarget(caster, target)){
				// Damage increases closer to player up to a maximum of 4 hearts (at 1 block distance).
				float damage = Math.min(8.0f/target.getDistanceToEntity(caster), 8.0f);
				target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.BLAST), damage * damageMultiplier);
				
				if(!world.isRemote){
					
					// Entity speed increases closer to the player to a maximum of 3 (at 1 block distance).
					// This is the entity's speed compared to its distance from the player. Used for a similar triangles
					// based x, y and z speed calculation.
					double velocityFactor = Math.min(5/target.getDistanceSqToEntity(caster), 3.0d);
					
					double dx = target.posX - caster.posX;
					double dy = target.posY + 1 - caster.posY;
					double dz = target.posZ - caster.posZ;
					
					target.motionX = velocityFactor * dx;
					target.motionY = velocityFactor * dy;
					target.motionZ = velocityFactor * dz;

					// Player motion is handled on that player's client so needs packets
					if(target instanceof EntityPlayerMP){
						((EntityPlayerMP)target).playerNetServerHandler.sendPacket(new S12PacketEntityVelocity(target));
					}
				}
			}
		}
		if(world.isRemote){
			
			world.spawnParticle("largeexplode", caster.posX, caster.boundingBox.minY + 0.1, caster.posZ, 0, 0, 0);
			
			double particleX, particleZ;
			for(int i=0;i<40;i++){
				particleX = caster.posX - 1.0d + 2*world.rand.nextDouble();
				particleZ = caster.posZ - 1.0d + 2*world.rand.nextDouble();
				Wizardry.proxy.spawnParticle(EnumParticleType.SPARKLE, world, particleX, WizardryUtilities.getPlayerEyesPos(caster) - 1.5, particleZ,
						particleX - caster.posX, 0, particleZ - caster.posZ, 30, 0.8f, 0.8f, 1.0f);
				particleX = caster.posX - 1.0d + 2*world.rand.nextDouble();
				particleZ = caster.posZ - 1.0d + 2*world.rand.nextDouble();
				Wizardry.proxy.spawnParticle(EnumParticleType.SPARKLE, world, particleX, WizardryUtilities.getPlayerEyesPos(caster) - 1.5, particleZ,
						particleX - caster.posX, 0, particleZ - caster.posZ, 30, 0.9f, 0.9f, 0.9f);
				particleX = caster.posX - 1.0d + 2*world.rand.nextDouble();
				particleZ = caster.posZ - 1.0d + 2*world.rand.nextDouble();
				
				Block block = WizardryUtilities.getBlockEntityIsStandingOn(caster);
				// Player actual eye height is 1.62, client is 1.5 too high, hence the -1.5.
				if(block != null){
					Wizardry.proxy.spawnDigParticle(world, particleX, WizardryUtilities.getPlayerEyesPos(caster) - 1.5, particleZ, particleX - caster.posX, 0, particleZ - caster.posZ,
							block);
				}
			}
		}
		caster.swingItem();
		world.playSoundAtEntity(caster, "wizardry:boom", 1.0f, 0.7f);
		world.playSoundAtEntity(caster, "wizardry:boom", 2.0f, 0.3f);
		return true;
	}


}
