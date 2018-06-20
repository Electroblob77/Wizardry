package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.construct.EntityTornado;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.EnumAction;

public class Tornado extends SpellConstruct<EntityTornado> {

	public Tornado(){
		super("tornado", Tier.ADVANCED, Element.EARTH, SpellType.ATTACK, 35, 80, EnumAction.NONE, EntityTornado::new, 200, WizardrySounds.SPELL_ICE);
	}

	@Override
	protected void addConstructExtras(EntityTornado construct, EntityLivingBase caster, SpellModifiers modifiers){
		construct.setHorizontalVelocity(caster.getLookVec().x / 3, caster.getLookVec().z / 3);
	}

}
