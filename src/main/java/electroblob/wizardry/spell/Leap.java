package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

public class Leap extends Spell {

	public Leap() {
		super(Tier.BASIC, 10, Element.EARTH, "leap", SpellType.UTILITY, 20, EnumAction.NONE, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers) {
		
		if(caster.onGround){
			
			caster.motionY = 0.65 * modifiers.get(SpellModifiers.DAMAGE);
			caster.addVelocity(caster.getLookVec().xCoord*0.3, 0, caster.getLookVec().zCoord*0.3);
			
			if(world.isRemote){
				for(int i=0; i<10; i++){
					double x = (double)(caster.posX + world.rand.nextFloat() - 0.5F);
					double y = (double)(caster.getEntityBoundingBox().minY);
					double z = (double)(caster.posZ + world.rand.nextFloat() - 0.5F);
					world.spawnParticle(EnumParticleTypes.CLOUD, x, y, z, 0, 0, 0);
				}
			}
			
			WizardryUtilities.playSoundAtPlayer(caster, SoundEvents.ENTITY_ENDERDRAGON_FLAP, 0.5F, 1.0f);
			caster.swingArm(hand);
			return true;
		}
		
		return false;
	}


}
