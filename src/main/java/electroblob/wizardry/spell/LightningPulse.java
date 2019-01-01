package electroblob.wizardry.spell;

import java.util.List;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
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
		
	private static final double BASE_RADIUS = 3;
	private static final float BASE_DAMAGE = 8;

	public LightningPulse(){
		super("lightning_pulse", Tier.ADVANCED, Element.LIGHTNING, SpellType.ATTACK, 25, 75, EnumAction.NONE, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}
	
	// TODO: NPC casting support

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		if(caster.onGround){

			List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(
					BASE_RADIUS * modifiers.get(WizardryItems.blast_upgrade), caster.posX, caster.posY, caster.posZ, world);

			for(EntityLivingBase target : targets){
				if(WizardryUtilities.isValidTarget(caster, target)){
					// Base damage is 4 hearts no matter where the target is.
					target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.SHOCK),
							BASE_DAMAGE * modifiers.get(SpellModifiers.POTENCY));

					if(!world.isRemote){

						double dx = target.posX - caster.posX;
						double dz = target.posZ - caster.posZ;
						// Normalises the velocity.
						double vectorLength = MathHelper.sqrt(dx * dx + dz * dz);
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
			
			if(world.isRemote){
				ParticleBuilder.create(Type.LIGHTNING_PULSE).pos(caster.posX, caster.getEntityBoundingBox().minY
						+ WizardryUtilities.ANTI_Z_FIGHTING_OFFSET, caster.posZ)
				.scale(modifiers.get(WizardryItems.blast_upgrade)).spawn(world);
			}
			
			caster.swingArm(hand);
			WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_LIGHTNING, 1, 1);
			WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_SHOCKWAVE, 2, 1);
			return true;
		}
		return false;
	}

}
