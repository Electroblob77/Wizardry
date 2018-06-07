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

public class FontOfMana extends Spell {

	public FontOfMana() {
		super(EnumTier.MASTER, 100, EnumElement.HEALING, "font_of_mana", EnumSpellType.UTILITY, 250, EnumAction.bow, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		List<EntityPlayer> targets = WizardryUtilities.getEntitiesWithinRadius(5*blastMultiplier, caster.posX, caster.posY, caster.posZ, world, EntityPlayer.class);
		
		for(EntityPlayer target : targets){
			if(WizardryUtilities.isPlayerAlly(caster, target) || target == caster){
				// Damage multiplier can only ever be 1 or 1.6 for master spells, so there's little point in actually calculating this.
				target.addPotionEffect(new PotionEffect(Wizardry.fontOfMana.id, (int)(600*durationMultiplier), damageMultiplier > 1 ? 1 : 0));
			}
		}
		
		if(world.isRemote){
			for(int i=0;i<100*blastMultiplier;i++){
        		double radius = (1 + world.rand.nextDouble()*4)*blastMultiplier;
        		double angle = world.rand.nextDouble()*Math.PI*2;
        		float hue = world.rand.nextFloat()*0.4f;
				Wizardry.proxy.spawnParticle(EnumParticleType.SPARKLE, world, caster.posX + radius*Math.cos(angle), WizardryUtilities.getEntityFeetPos(caster), caster.posZ + radius*Math.sin(angle),
						0, 0.03, 0, 50, 1, 1-hue, 0.6f+hue);

			}
		}

		world.playSoundAtEntity(caster, "wizardry:heal", 0.7F, world.rand.nextFloat() * 0.4F + 1.0F);
		return true;
	}


}
