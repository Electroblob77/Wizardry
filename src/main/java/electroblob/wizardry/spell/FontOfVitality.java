package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public class FontOfVitality extends Spell {

	public FontOfVitality() {
		super(EnumTier.MASTER, 75, EnumElement.HEALING, "font_of_vitality", EnumSpellType.DEFENCE, 300, EnumAction.bow, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {

		caster.addPotionEffect(new PotionEffect(Potion.field_76444_x.id, (int)(1200*durationMultiplier), 1, true));
		caster.addPotionEffect(new PotionEffect(Potion.regeneration.id, (int)(300*durationMultiplier), 1, true));

		if(world.isRemote){
			for(int i=0; i<10; i++){
				double x1 = (double)((float)caster.posX + world.rand.nextFloat()*2 - 1.0F);
				double y1 = (double)((float)WizardryUtilities.getPlayerEyesPos(caster) - 0.5F + world.rand.nextFloat());
				double z1 = (double)((float)caster.posZ + world.rand.nextFloat()*2 - 1.0F);

				Wizardry.proxy.spawnParticle(EnumParticleType.SPARKLE, world, x1, y1, z1, 0, 0.1F, 0, 48 + world.rand.nextInt(12), 1.0f, 0.6f, 0.7f);

				x1 = (double)((float)caster.posX + world.rand.nextFloat()*2 - 1.0F);
				y1 = (double)((float)WizardryUtilities.getPlayerEyesPos(caster) - 0.5F + world.rand.nextFloat());
				z1 = (double)((float)caster.posZ + world.rand.nextFloat()*2 - 1.0F);

				Wizardry.proxy.spawnParticle(EnumParticleType.SPARKLE, world, x1, y1, z1, 0, 0.1F, 0, 48 + world.rand.nextInt(12), 1.0f, 0.8f, 0.3f);

			}
		}
		
		world.playSoundAtEntity(caster, "wizardry:heal", 0.7F, world.rand.nextFloat() * 0.4F + 1.0F);
		return true;
	}


}
