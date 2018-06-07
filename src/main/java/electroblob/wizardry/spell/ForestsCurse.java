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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public class ForestsCurse extends Spell {

	public ForestsCurse() {
		super(EnumTier.MASTER, 75, EnumElement.EARTH, "forests_curse", EnumSpellType.ATTACK, 200, EnumAction.bow, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(5.0d*blastMultiplier, caster.posX, caster.posY, caster.posZ, world);
		
		for(EntityLivingBase target : targets){
			if(WizardryUtilities.isValidTarget(caster, target) && !MagicDamage.isEntityImmune(DamageType.POISON, target)){
				target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.POISON), 4.0f * damageMultiplier);
				target.addPotionEffect(new PotionEffect(Potion.poison.id, (int)(140*durationMultiplier), 2));
				target.addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, (int)(140*durationMultiplier), 2));
				target.addPotionEffect(new PotionEffect(Potion.weakness.id, (int)(140*durationMultiplier), 2));
			}
		}
		
		if(world.isRemote){
			for(int i=0;i<50*blastMultiplier;i++){
        		double radius = (1 + world.rand.nextDouble()*4)*blastMultiplier;
        		double angle = world.rand.nextDouble()*Math.PI*2;
				float brightness = world.rand.nextFloat()/4;
				Wizardry.proxy.spawnParticle(EnumParticleType.DARK_MAGIC, world, caster.posX + radius*Math.cos(angle), WizardryUtilities.getPlayerEyesPos(caster) + 0.5, caster.posZ + radius*Math.sin(angle),
						0, -0.2, 0, 0, 0.05f+brightness, 0.2f+brightness, 0.0f);
				brightness = world.rand.nextFloat()/4;
				Wizardry.proxy.spawnParticle(EnumParticleType.SPARKLE, world, caster.posX + radius*Math.cos(angle), WizardryUtilities.getPlayerEyesPos(caster) + 0.5, caster.posZ + radius*Math.sin(angle),
						0, -0.05, 0, 50, 0.1f+brightness, 0.2f+brightness, 0.0f);
				Wizardry.proxy.spawnParticle(EnumParticleType.LEAF, world, caster.posX + radius*Math.cos(angle), WizardryUtilities.getPlayerEyesPos(caster) + 0.5, caster.posZ + radius*Math.sin(angle), 0, -0.01, 0, 40 + world.rand.nextInt(12));

			}
		}
		
		world.playSoundAtEntity(caster, "mob.wither.spawn", 1.0F, world.rand.nextFloat() * 0.2F + 1.0F);
		return true;
	}


}
