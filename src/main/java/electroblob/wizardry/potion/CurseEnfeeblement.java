package electroblob.wizardry.potion;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.WizardryPotions;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.util.FoodStats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.lang.reflect.Field;

@Mod.EventBusSubscriber
public class CurseEnfeeblement extends Curse {

	// Yay more reflection
	private static final Field foodTimer;

	static {
		foodTimer = ObfuscationReflectionHelper.findField(FoodStats.class, "field_75123_d");
		foodTimer.setAccessible(true);
	}

	public CurseEnfeeblement(boolean isBadEffect, int liquiidColour){
		super(isBadEffect, liquiidColour, new ResourceLocation(Wizardry.MODID, "textures/gui/potion_icons/curse_of_enfeeblement.png"));
		// This needs to be here because registerPotionAttributeModifier doesn't like it if the potion has no name yet.
		this.setPotionName("potion." + Wizardry.MODID + ":curse_of_enfeeblement");
		this.registerPotionAttributeModifier(SharedMonsterAttributes.MAX_HEALTH,
				"2e8c378e-3d51-4ba1-b02c-591b5d968a05", -0.2, 1);
	}

	@SubscribeEvent
	public static void onPlayerTickEvent(TickEvent.PlayerTickEvent event){
		// Players are the only entities with natural regeneration
		// This can't be done in performEffect as that method only gets called every 20 ticks or so
		// Don't bother trying to prevent it unless the player is full enough
		if(event.player.isPotionActive(WizardryPotions.curse_of_enfeeblement) && event.player.getFoodStats().getFoodLevel() > 17){
			try{
				// Constantly setting this to zero prevents natural regeneration
				foodTimer.set(event.player.getFoodStats(), 0);
			}catch(IllegalAccessException e){
				Wizardry.logger.error("Error setting player food timer: ", e);
			}
		}
	}

}
