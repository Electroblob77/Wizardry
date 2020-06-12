package electroblob.wizardry.client.audio;

import electroblob.wizardry.item.ISpellCastingItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;

/** A special type of moving sound that stops when the player stops charging up a spell, either by releasing the mouse
 * button or when the charge-up time has elapsed. */
public class MovingSoundSpellCharge extends MovingSoundEntity<EntityLivingBase> {

	public MovingSoundSpellCharge(EntityLivingBase entity, SoundEvent sound, SoundCategory category, float volume, float pitch, boolean repeat){
		super(entity, sound, category, volume, pitch, repeat);
	}

	@Override
	public void update(){

		if(source.isHandActive()){

			ItemStack stack = source.getActiveItemStack();

			if(stack.getItem() instanceof ISpellCastingItem){

				if(source.getItemInUseMaxCount() < ((ISpellCastingItem)stack.getItem()).getCurrentSpell(stack).getChargeup()){
					super.update();
					return;
				}
			}
		}

		this.donePlaying = true;
	}

}
