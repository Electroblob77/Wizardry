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
import net.minecraft.world.World;

public class GreaterHeal extends Spell {

	public GreaterHeal() {
		super(EnumTier.ADVANCED, 15, EnumElement.HEALING, "greater_heal", EnumSpellType.DEFENCE, 40, EnumAction.bow, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {

		if(caster.shouldHeal()){
			caster.heal((int)(8*damageMultiplier));
			if(world.isRemote){
				for(int i=0; i<10; i++){
					double dx = (double)((float)caster.posX + world.rand.nextFloat()*2 - 1.0F);
					double dy = (double)((float)WizardryUtilities.getPlayerEyesPos(caster) - 0.5F + world.rand.nextFloat());
					double dz = (double)((float)caster.posZ + world.rand.nextFloat()*2 - 1.0F);
					Wizardry.proxy.spawnParticle(EnumParticleType.SPARKLE, world, dx, dy, dz, 0, 0.1F, 0, 48 + world.rand.nextInt(12), 1.0f, 1.0f, 0.3f);
				}
			}
			world.playSoundAtEntity(caster, "wizardry:heal", 0.7F, world.rand.nextFloat() * 0.4F + 1.0F);
			return true;
		}
		return false;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EntityLivingBase target, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier){
		
		if(caster.getHealth() < caster.getMaxHealth()){
			caster.heal((int)(8*damageMultiplier));
			if(world.isRemote){
				for(int i=0; i<10; i++){
					double dx = (double)((float)caster.posX + world.rand.nextFloat()*2 - 1.0F);
					double dy = (double)((float)caster.posY + caster.getEyeHeight() - 0.5F + world.rand.nextFloat());
					double dz = (double)((float)caster.posZ + world.rand.nextFloat()*2 - 1.0F);
					Wizardry.proxy.spawnParticle(EnumParticleType.SPARKLE, world, dx, dy, dz, 0, 0.1F, 0, 48 + world.rand.nextInt(12), 1.0f, 1.0f, 0.3f);
				}
			}
			world.playSoundAtEntity(caster, "wizardry:heal", 0.7F, world.rand.nextFloat() * 0.4F + 1.0F);
			return true;
		}
		
		return false;
	}

}
