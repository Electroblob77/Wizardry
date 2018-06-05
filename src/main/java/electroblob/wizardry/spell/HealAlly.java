package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class HealAlly extends Spell {

	public HealAlly(){
		super(Tier.APPRENTICE, 10, Element.HEALING, "heal_ally", SpellType.DEFENCE, 20, EnumAction.NONE, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		RayTraceResult rayTrace = WizardryUtilities.standardEntityRayTrace(world, caster,
				10 * modifiers.get(WizardryItems.range_upgrade), 8.0f);

		if(rayTrace != null && rayTrace.entityHit != null && WizardryUtilities.isLiving(rayTrace.entityHit)){
			EntityLivingBase target = (EntityLivingBase)rayTrace.entityHit;
			if(target.getHealth() < target.getMaxHealth()){
				target.heal((int)(5 * modifiers.get(SpellModifiers.DAMAGE)));

				if(world.isRemote){
					for(int i = 0; i < 10; i++){
						double d0 = (double)((float)target.posX + world.rand.nextFloat() * 2 - 1.0F);
						// Apparently the client side spawns the particles 1 block higher than it should... hence the -
						// 0.5F.
						double d1 = (double)((float)target.getEntityBoundingBox().minY + target.height - 0.5f
								+ world.rand.nextFloat());
						double d2 = (double)((float)target.posZ + world.rand.nextFloat() * 2 - 1.0F);
						Wizardry.proxy.spawnParticle(Type.SPARKLE, world, d0, d1, d2, 0, 0.1F, 0,
								48 + world.rand.nextInt(12), 1.0f, 1.0f, 0.3f);
					}
				}

				caster.swingArm(hand);
				target.playSound(WizardrySounds.SPELL_HEAL, 0.7F, world.rand.nextFloat() * 0.4F + 1.0F);
				return true;
			}
		}
		return false;
	}

}
