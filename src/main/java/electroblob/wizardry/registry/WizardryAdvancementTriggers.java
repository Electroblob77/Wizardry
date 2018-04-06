package electroblob.wizardry.registry;

import electroblob.wizardry.util.CustomAdvancementTrigger;
import net.minecraft.advancements.CriteriaTriggers;

/**
 * This class stores a collection of custom advancement triggers, for advancements that cannot be triggered
 * from plain vanilla JSON definitions. It replaces the old WizardryAchievements class.
 *
 * @author 12foo
 * @since Wizardry 4.1.0
 */
public final class WizardryAdvancementTriggers {
	
    public static final CustomAdvancementTrigger armour_set = new CustomAdvancementTrigger("trigger_armour_set");
    public static final CustomAdvancementTrigger jam_wizard = new CustomAdvancementTrigger("trigger_jam_wizard");
    public static final CustomAdvancementTrigger self_destruct = new CustomAdvancementTrigger("trigger_self_destruct");
    public static final CustomAdvancementTrigger all_spells = new CustomAdvancementTrigger("trigger_all_spells");
    public static final CustomAdvancementTrigger element_master = new CustomAdvancementTrigger("trigger_element_master");
    public static final CustomAdvancementTrigger identify_spell = new CustomAdvancementTrigger("trigger_identify_spell");
    public static final CustomAdvancementTrigger elemental = new CustomAdvancementTrigger("trigger_elemental");
    public static final CustomAdvancementTrigger legendary = new CustomAdvancementTrigger("trigger_legendary");
    public static final CustomAdvancementTrigger max_out_wand = new CustomAdvancementTrigger("trigger_max_out_wand");
    public static final CustomAdvancementTrigger special_upgrade = new CustomAdvancementTrigger("trigger_special_upgrade");
    public static final CustomAdvancementTrigger pig_tornado = new CustomAdvancementTrigger("trigger_pig_tornado");
    public static final CustomAdvancementTrigger master = new CustomAdvancementTrigger("trigger_master");
    public static final CustomAdvancementTrigger apprentice = new CustomAdvancementTrigger("trigger_apprentice");
    public static final CustomAdvancementTrigger anger_wizard = new CustomAdvancementTrigger("trigger_anger_wizard");
    public static final CustomAdvancementTrigger buy_master_spell = new CustomAdvancementTrigger("trigger_buy_master_spell");
    public static final CustomAdvancementTrigger wizard_trade = new CustomAdvancementTrigger("trigger_wizard_trade");
    public static final CustomAdvancementTrigger slime_skeleton = new CustomAdvancementTrigger("trigger_slime_skeleton");
    public static final CustomAdvancementTrigger freeze_blaze = new CustomAdvancementTrigger("trigger_freeze_blaze");
    public static final CustomAdvancementTrigger frankenstein = new CustomAdvancementTrigger("trigger_frankenstein");
    public static final CustomAdvancementTrigger charge_creeper = new CustomAdvancementTrigger("trigger_charge_creeper");

	public static void register(){
		
		CriteriaTriggers.register(armour_set);
		CriteriaTriggers.register(jam_wizard);
		CriteriaTriggers.register(self_destruct);
		CriteriaTriggers.register(all_spells);
		CriteriaTriggers.register(element_master);
		CriteriaTriggers.register(identify_spell);
		CriteriaTriggers.register(elemental);
		CriteriaTriggers.register(legendary);
		CriteriaTriggers.register(max_out_wand);
		CriteriaTriggers.register(special_upgrade);
		CriteriaTriggers.register(pig_tornado);
		CriteriaTriggers.register(master);
		CriteriaTriggers.register(apprentice);
		CriteriaTriggers.register(anger_wizard);
		CriteriaTriggers.register(buy_master_spell);
		CriteriaTriggers.register(wizard_trade);
		CriteriaTriggers.register(slime_skeleton);
		CriteriaTriggers.register(freeze_blaze);
		CriteriaTriggers.register(frankenstein);
		CriteriaTriggers.register(charge_creeper);
	}
}