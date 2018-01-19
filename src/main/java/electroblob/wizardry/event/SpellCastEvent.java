package electroblob.wizardry.event;

import electroblob.wizardry.spell.Spell;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;

/**
 * [NYI] SpellCastEvent is the parent event class for all spell casting events.<br>
 * <br>
 * This event is fired on the {@link MinecraftForge#EVENT_BUS}.
 *
 * @author Electroblob
 * @since Wizardry 2.1
 */
public abstract class SpellCastEvent extends LivingEvent {

	public SpellCastEvent(EntityLivingBase entity, Spell spell) {
		super(entity);
	}

}
