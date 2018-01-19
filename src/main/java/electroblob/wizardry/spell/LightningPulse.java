package electroblob.wizardry.spell;

import java.util.List;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.construct.EntityLightningPulse;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumAction;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class LightningPulse extends Spell {

	public LightningPulse() {
		super(Tier.ADVANCED, 25, Element.LIGHTNING, "lightning_pulse", SpellType.ATTACK, 75, EnumAction.NONE, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers) {
		
		if(caster.onGround){
			
			List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(3.0d*modifiers.get(WizardryItems.blast_upgrade), caster.posX, caster.posY, caster.posZ, world);
			
			for(EntityLivingBase target : targets){
				if(WizardryUtilities.isValidTarget(caster, target)){
					// Damage is 4 hearts no matter where the target is.
					target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.SHOCK), 8 * modifiers.get(SpellModifiers.DAMAGE));
					
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
							((EntityPlayerMP)target).connection.sendPacket(new SPacketEntityVelocity(target));
						}
					}
				}
			}
			if(!world.isRemote){
				EntityLightningPulse lightningpulse = new EntityLightningPulse(world, caster.posX, caster.getEntityBoundingBox().minY, caster.posZ, caster, 7, modifiers.get(SpellModifiers.DAMAGE));
				world.spawnEntityInWorld(lightningpulse);
			}
			caster.swingArm(hand);
			WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_LIGHTNING, 1.0f, 1.0f);
			WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_SHOCKWAVE, 2.0f, 1.0f);
			return true;
		}
		return false;
	}


}
