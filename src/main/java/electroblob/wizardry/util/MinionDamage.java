package electroblob.wizardry.util;

import electroblob.wizardry.util.MagicDamage.DamageType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

/**
 * DamageSource specifically for summoned creatures. Despite being for melee attacks, this is actually an indirect
 * damage source, because it allows the minion to be the 'projectile'. This class also overrides getDeathMessage to
 * display the minion's name rather than the caster's. <i>The event handler deals with all damage dealt by summoned
 * creatures, so it's very unlikely that this will be needed anywhere else. If for some reason it is, note that
 * knockback has to be removed and re-applied after the damage is dealt.</i>
 * @author Electroblob
 * @since Wizardry 1.2
 * @see IndirectMinionDamage
 */
public class MinionDamage extends IndirectMagicDamage {

	public MinionDamage(String name, Entity minion, Entity caster, DamageType type, boolean isRetaliatory) {
		super(name, minion, caster, type, isRetaliatory);
	}
	
	@Override
	public ITextComponent getDeathMessage(EntityLivingBase victim) {
		ITextComponent itextcomponent = this.damageSourceEntity.getDisplayName();
        String key = "death.attack." + this.damageType;
        return new TextComponentTranslation(key, victim.getDisplayName(), itextcomponent);
	}

}
