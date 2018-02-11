package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryParticleType;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class Glide extends Spell {

	public Glide(){
		super(Tier.ADVANCED, 5, Element.EARTH, "glide", SpellType.UTILITY, 0, EnumAction.NONE, true);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		if(caster.motionY < -0.1 && !caster.isInWater()){
			caster.motionY = -0.1;
			if(Math.abs(caster.motionX) < 0.4 && Math.abs(caster.motionZ) < 0.4){
				caster.addVelocity(caster.getLookVec().x / 8, 0, caster.getLookVec().z / 8);
				// entityplayer.moveEntity(entityplayer.motionX*10, 0, entityplayer.motionZ*10);
			}
			caster.fallDistance = 0.0f;
		}

		if(world.isRemote){
			Wizardry.proxy.spawnParticle(WizardryParticleType.SPARKLE, world,
					caster.posX - 0.25d + world.rand.nextDouble() / 2,
					WizardryUtilities.getPlayerEyesPos(caster) - 1.5f + world.rand.nextDouble(),
					caster.posZ - 0.25d + world.rand.nextDouble() / 2, 0, -0.1F, 0, 15, 1.0f, 1.0f, 1.0f);
			Wizardry.proxy.spawnParticle(WizardryParticleType.LEAF, world,
					caster.posX - 0.25d + world.rand.nextDouble() / 2,
					WizardryUtilities.getPlayerEyesPos(caster) - 1.5f + world.rand.nextDouble(),
					caster.posZ - 0.25d + world.rand.nextDouble() / 2, 0, -0.03, 0, 20);
		}

		if(ticksInUse % 24 == 0){
			WizardryUtilities.playSoundAtPlayer(caster, SoundEvents.ITEM_ELYTRA_FLYING, 0.5F, 1.0f);
		}
		return true;
	}

}
