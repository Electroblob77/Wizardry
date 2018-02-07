package electroblob.wizardry.spell;

import electroblob.wizardry.WizardData;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.EntityShield;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumAction;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class Shield extends Spell {

	public Shield(){
		super(Tier.APPRENTICE, 5, Element.HEALING, "shield", SpellType.DEFENCE, 0, EnumAction.BLOCK, true);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		caster.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 10, 0, false, false));

		if(WizardData.get(caster).shield == null){
			WizardData.get(caster).shield = new EntityShield(world, caster);
			if(!world.isRemote){
				world.spawnEntity(WizardData.get(caster).shield);
			}
		}
		if(ticksInUse == 0){
			WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_CONJURATION, 1.0f, 1.0f);
		}
		return true;
	}

}
