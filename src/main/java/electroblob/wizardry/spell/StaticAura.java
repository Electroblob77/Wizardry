package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class StaticAura extends Spell {

	public StaticAura(){
		super(Tier.ADVANCED, 40, Element.LIGHTNING, "static_aura", SpellType.DEFENCE, 250, EnumAction.BOW, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		// Cannot be cast when it has already been cast
		if(!caster.isPotionActive(WizardryPotions.static_aura)){
			if(!world.isRemote){
				caster.addPotionEffect(new PotionEffect(WizardryPotions.static_aura,
						(int)(600 * modifiers.get(WizardryItems.duration_upgrade)), 0));
			}
			WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_SPARK, 1.0F,
					world.rand.nextFloat() * 0.4F + 1.4F);
			return true;
		}
		return false;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target,
			SpellModifiers modifiers){

		if(target != null){
			// Cannot be cast when it has already been cast
			if(!caster.isPotionActive(WizardryPotions.static_aura)){
				if(!world.isRemote){
					caster.addPotionEffect(new PotionEffect(WizardryPotions.static_aura,
							(int)(600 * modifiers.get(WizardryItems.duration_upgrade)), 0));
				}
				caster.playSound(WizardrySounds.SPELL_SPARK, 1.0F, world.rand.nextFloat() * 0.4F + 1.4F);
				return true;
			}
			return false;
		}

		return false;
	}

	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

}
