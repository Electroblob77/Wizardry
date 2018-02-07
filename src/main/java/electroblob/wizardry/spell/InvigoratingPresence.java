package electroblob.wizardry.spell;

import java.util.List;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryParticleType;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumAction;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class InvigoratingPresence extends Spell {

	public InvigoratingPresence(){
		super(Tier.APPRENTICE, 30, Element.HEALING, "invigorating_presence", SpellType.UTILITY, 60, EnumAction.BOW,
				false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		List<EntityPlayer> targets = WizardryUtilities.getEntitiesWithinRadius(
				5 * modifiers.get(WizardryItems.blast_upgrade), caster.posX, caster.posY, caster.posZ, world,
				EntityPlayer.class);

		for(EntityPlayer target : targets){
			if(WizardryUtilities.isPlayerAlly(caster, target) || target == caster){
				// Strength 2 for 45 seconds.
				target.addPotionEffect(new PotionEffect(MobEffects.STRENGTH,
						(int)(900 * modifiers.get(WizardryItems.duration_upgrade)), 1, false, false));
			}
		}

		if(world.isRemote){
			for(int i = 0; i < 50 * modifiers.get(WizardryItems.blast_upgrade); i++){
				double radius = (1 + world.rand.nextDouble() * 4) * modifiers.get(WizardryItems.blast_upgrade);
				double angle = world.rand.nextDouble() * Math.PI * 2;
				Wizardry.proxy.spawnParticle(WizardryParticleType.SPARKLE, world,
						caster.posX + radius * Math.cos(angle), caster.getEntityBoundingBox().minY,
						caster.posZ + radius * Math.sin(angle), 0, 0.03, 0, 50, 1, 0.2f, 0.2f);

			}
		}

		WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_CONJURATION, 1.0F,
				world.rand.nextFloat() * 0.2F + 1.0F);
		return true;
	}

}
