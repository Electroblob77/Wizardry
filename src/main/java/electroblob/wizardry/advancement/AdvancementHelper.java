package electroblob.wizardry.advancement;

import electroblob.wizardry.Wizardry;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementManager;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;

/**
 * This is a provisory class for the transition to 1.12
 * It contains an enum with all the advancements of this mod
 * and an helper to grant advancements on the server side only (like the command)
 * @author Corail31
 * @since Wizardry 4.1
 */
public class AdvancementHelper {
	public enum EnumAdvancement {
		crystal,
		arcane_initiate,
		apprentice,
		master,
		all_spells,
		wizard_trade,
		buy_master_spell,
		freeze_blaze,
		charge_creeper,
		frankenstein,
		special_upgrade,
		craft_flask,
		elemental,
		armour_set,
		legendary,
		self_destruct,
		pig_tornado,
		jam_wizard,
		slime_skeleton,
		anger_wizard,
		defeat_evil_wizard,
		max_out_wand,
		element_master,
		identify_spell;
	}
	
	public static boolean grantAdvancement(EntityPlayer player, EnumAdvancement advancementName) {
		if (player == null) { return false; }
		if (player.world.isRemote) { return true; }
		EntityPlayerMP player_mp = player.getServer().getPlayerList().getPlayerByUUID(player.getUniqueID());
		AdvancementManager am = player_mp.getServerWorld().getAdvancementManager();
		Advancement advancement = am.getAdvancement(new ResourceLocation(Wizardry.MODID, advancementName.name()));
		if (advancement == null) { return false; }
		AdvancementProgress advancementprogress = player_mp.getAdvancements().getProgress(advancement);
		if (!advancementprogress.isDone()) {
			for (String criteria : advancementprogress.getRemaningCriteria()) {
				player_mp.getAdvancements().grantCriterion(advancement, criteria);
			}
		}
		return true;
	}
}
