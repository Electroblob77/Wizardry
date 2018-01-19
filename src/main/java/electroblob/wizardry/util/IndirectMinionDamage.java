package electroblob.wizardry.util;

import electroblob.wizardry.util.MagicDamage.DamageType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

/**
 * DamageSource specifically for summoned creatures. This is for ranged attacks and works exactly the same way as a
 * normal indirect damage source, except that it takes the minion as an additional parameter. This is for the sole
 * purpose of displaying the correct death message. <i>The event handler deals with all damage dealt by summoned
 * creatures, so it's very unlikely that this will be needed anywhere else. If for some reason it is, note that
 * knockback has to be removed and re-applied after the damage is dealt.</i>
 * @author Electroblob
 * @since Wizardry 1.2
 * @see MinionDamage
 */
public class IndirectMinionDamage extends IndirectMagicDamage {
	
	private Entity minion;

	public IndirectMinionDamage(String name, Entity projectile, Entity minion, Entity caster, DamageType type, boolean isRetaliatory) {
		super(name, projectile, caster, type, isRetaliatory);
		this.minion = minion;
	}
	
	@Override
	public ITextComponent getDeathMessage(EntityLivingBase victim) {
		ITextComponent itextcomponent = this.minion.getDisplayName();
        String key = "death.attack." + this.damageType;
        return new TextComponentTranslation(key, victim.getDisplayName(), itextcomponent);
	}

}
