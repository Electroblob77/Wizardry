package electroblob.wizardry.event;

import electroblob.wizardry.spell.Spell;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Allows for developers to hook into EntityUtils.isCasting to allow for custom spell casting effects to be considered as casting (for sound loops and other checks)
 * Set the result to ALLOW to enable a casting effect
 */
@Event.HasResult
public class IsCastingEvent extends LivingEvent {

	private final Spell spell;

	public IsCastingEvent(EntityLivingBase entity, Spell spell) {
		super(entity);
		this.spell = spell;
	}

	public Spell getSpell() {
		return this.spell;
	}

}
