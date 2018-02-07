package electroblob.wizardry.util;

import electroblob.wizardry.util.MagicDamage.DamageType;
import net.minecraft.entity.Entity;
import net.minecraft.util.EntityDamageSourceIndirect;

public class IndirectMagicDamage extends EntityDamageSourceIndirect implements IElementalDamage {

	private final DamageType type;
	private final boolean isRetaliatory;

	public IndirectMagicDamage(String name, Entity magic, Entity caster, DamageType type, boolean isRetaliatory){
		super(name, magic, caster);
		this.type = type;
		this.isRetaliatory = isRetaliatory;
	}

	@Override
	public DamageType getType(){
		return type;
	}

	@Override
	public boolean isRetaliatory(){
		return isRetaliatory;
	}

}
