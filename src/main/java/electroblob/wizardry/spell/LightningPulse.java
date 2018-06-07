package electroblob.wizardry.spell;

import java.util.List;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.MagicDamage;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.MagicDamage.DamageType;
import electroblob.wizardry.entity.construct.EntityLightningPulse;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumAction;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class LightningPulse extends Spell {

	public LightningPulse() {
		super(EnumTier.ADVANCED, 25, EnumElement.LIGHTNING, "lightning_pulse", EnumSpellType.ATTACK, 75, EnumAction.none, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		if(caster.onGround){
			
			List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(3.0d*blastMultiplier, caster.posX, caster.posY, caster.posZ, world);
			
			for(EntityLivingBase target : targets){
				if(WizardryUtilities.isValidTarget(caster, target)){
					// Damage is 4 hearts no matter where the target is.
					target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.SHOCK), 8 * damageMultiplier);
					
					if(!world.isRemote){
						
						double dx = target.posX - caster.posX;
						double dz = target.posZ - caster.posZ;
						// Normalises the velocity.
						double vectorLength = MathHelper.sqrt_double(dx*dx + dz*dz);
						dx /= vectorLength;
						dz /= vectorLength;
						
						target.motionX = 0.8 * dx;
						target.motionY = 0;
						target.motionZ = 0.8 * dz;

						// Player motion is handled on that player's client so needs packets
						if(target instanceof EntityPlayerMP){
							((EntityPlayerMP)target).playerNetServerHandler.sendPacket(new S12PacketEntityVelocity(target));
						}
					}
				}
			}
			if(!world.isRemote){
				EntityLightningPulse lightningpulse = new EntityLightningPulse(world, caster.posX, caster.boundingBox.minY, caster.posZ, caster, 7, damageMultiplier);
				world.spawnEntityInWorld(lightningpulse);
			}
			caster.swingItem();
			world.playSoundAtEntity(caster, "wizardry:electricitya", 1.0f, 1.0f);
			world.playSoundAtEntity(caster, "wizardry:boom", 2.0f, 1.0f);
			return true;
		}
		return false;
	}


}
