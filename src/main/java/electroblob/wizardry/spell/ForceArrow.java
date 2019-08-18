package electroblob.wizardry.spell;

import electroblob.wizardry.entity.projectile.EntityForceArrow;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLivingBase;

import javax.annotation.Nullable;

public class ForceArrow extends SpellArrow<EntityForceArrow> {

	public ForceArrow(){
		super("force_arrow", EntityForceArrow::new);
		this.addProperties(Spell.DAMAGE);
		this.soundValues(1, 1.3f, 0.2f);
	}

	@Override
	protected void addArrowExtras(EntityForceArrow arrow, @Nullable EntityLivingBase caster, SpellModifiers modifiers){
		arrow.setMana((int)(this.getCost() * modifiers.get(SpellModifiers.COST)));
	}
}
