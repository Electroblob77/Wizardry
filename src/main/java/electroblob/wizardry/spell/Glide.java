package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class Glide extends Spell {

	public Glide(){
		super("glide", Tier.ADVANCED, Element.EARTH, SpellType.UTILITY, 5, 0, EnumAction.NONE, true);
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
			double x = caster.posX - 0.25 + world.rand.nextDouble() / 2;
			double y = caster.getEntityBoundingBox().minY + world.rand.nextDouble();
			double z = caster.posZ - 0.25 + world.rand.nextDouble() / 2;
			ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).vel(0, -0.1, 0).lifetime(15).colour(1, 1, 1).spawn(world);
			x = caster.posX - 0.25 + world.rand.nextDouble() / 2;
			y = caster.getEntityBoundingBox().minY + world.rand.nextDouble();
			z = caster.posZ - 0.25 + world.rand.nextDouble() / 2;
			ParticleBuilder.create(Type.LEAF).pos(x, y, z).lifetime(20).spawn(world);
		}

		if(ticksInUse % 24 == 0){
			WizardryUtilities.playSoundAtPlayer(caster, SoundEvents.ITEM_ELYTRA_FLYING, 0.5F, 1.0f);
		}
		
		return true;
	}

}
