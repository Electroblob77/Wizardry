package electroblob.wizardry;

import electroblob.wizardry.MagicDamage.DamageType;
import net.minecraft.entity.Entity;
import net.minecraft.util.EntityDamageSourceIndirect;

public class IndirectMagicDamage extends EntityDamageSourceIndirect implements IElementalDamage {
	
	private final DamageType type;

	public IndirectMagicDamage(String name, Entity magic, Entity caster, DamageType type) {
		super(name, magic, caster);
		this.type = type;
	}

	@Override
	public DamageType getType(){
		return type;
	}

}
