package electroblob.wizardry.spell;

import java.util.List;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public class InvigoratingPresence extends Spell {

	public InvigoratingPresence() {
		super(EnumTier.APPRENTICE, 30, EnumElement.HEALING, "invigorating_presence", EnumSpellType.UTILITY, 60, EnumAction.bow, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		List<EntityPlayer> targets = WizardryUtilities.getEntitiesWithinRadius(5*blastMultiplier, caster.posX, caster.posY, caster.posZ, world, EntityPlayer.class);
		
		for(EntityPlayer target : targets){
			if(WizardryUtilities.isPlayerAlly(caster, target) || target == caster){
				// Strength 2 for 45 seconds.
				target.addPotionEffect(new PotionEffect(Potion.damageBoost.id, (int)(900*durationMultiplier), 1, true));
			}
		}
		
		if(world.isRemote){
			for(int i=0;i<50*blastMultiplier;i++){
        		double radius = (1 + world.rand.nextDouble()*4)*blastMultiplier;
        		double angle = world.rand.nextDouble()*Math.PI*2;
				Wizardry.proxy.spawnParticle(EnumParticleType.SPARKLE, world, caster.posX + radius*Math.cos(angle), WizardryUtilities.getEntityFeetPos(caster), caster.posZ + radius*Math.sin(angle),
						0, 0.03, 0, 50, 1, 0.2f, 0.2f);

			}
		}
		
		world.playSoundAtEntity(caster, "wizardry:aura", 1.0F, world.rand.nextFloat() * 0.2F + 1.0F);
		return true;
	}


}
