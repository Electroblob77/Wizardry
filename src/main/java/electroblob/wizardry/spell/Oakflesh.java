package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public class Oakflesh extends Spell {

	public Oakflesh() {
		super(EnumTier.APPRENTICE, 20, EnumElement.HEALING, "oakflesh", EnumSpellType.DEFENCE, 50, EnumAction.bow, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {

		caster.addPotionEffect(new PotionEffect(Potion.resistance.id, (int)(600*durationMultiplier), 1, true));

		if(world.isRemote){
			for(int i=0; i<10; i++){
				double x1 = (double)((float)caster.posX + world.rand.nextFloat()*2 - 1.0F);
				double y1 = (double)((float)WizardryUtilities.getPlayerEyesPos(caster) - 0.5F + world.rand.nextFloat());
				double z1 = (double)((float)caster.posZ + world.rand.nextFloat()*2 - 1.0F);
				Wizardry.proxy.spawnParticle(EnumParticleType.SPARKLE, world, x1, y1, z1, 0, 0.1F, 0, 48 + world.rand.nextInt(12), 0.6f, 0.5f, 0.4f);
			}
		}

		world.playSoundAtEntity(caster, "wizardry:heal", 0.7F, world.rand.nextFloat() * 0.4F + 1.0F);
		return true;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EntityLivingBase target, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier){

		if(!caster.isPotionActive(Potion.resistance)){

			caster.addPotionEffect(new PotionEffect(Potion.resistance.id, (int)(600*durationMultiplier), 1, true));

			if(world.isRemote){
				for(int i=0; i<10; i++){
					double x1 = (double)((float)caster.posX + world.rand.nextFloat()*2 - 1.0F);
					double y1 = (double)((float)caster.posY + caster.getEyeHeight() - 0.5F + world.rand.nextFloat());
					double z1 = (double)((float)caster.posZ + world.rand.nextFloat()*2 - 1.0F);
					Wizardry.proxy.spawnParticle(EnumParticleType.SPARKLE, world, x1, y1, z1, 0, 0.1F, 0, 48 + world.rand.nextInt(12), 0.6f, 0.5f, 0.4f);
				}
			}

			world.playSoundAtEntity(caster, "wizardry:heal", 0.7F, world.rand.nextFloat() * 0.4F + 1.0F);
			return true;
		}

		return false;
	}

	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

}
