package electroblob.wizardry.spell;

import electroblob.wizardry.entity.construct.EntityForcefield;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;

public class Forcefield extends SpellConstruct<EntityForcefield> {

	public Forcefield(){
		super("forcefield", SpellActions.THRUST, EntityForcefield::new, false);
		addProperties(Spell.EFFECT_RADIUS);
	}

	@Override
	protected void addConstructExtras(EntityForcefield construct, EnumFacing side, EntityLivingBase caster, SpellModifiers modifiers){
		construct.setRadius(getProperty(EFFECT_RADIUS).floatValue() * modifiers.get(WizardryItems.blast_upgrade));
	}
}
