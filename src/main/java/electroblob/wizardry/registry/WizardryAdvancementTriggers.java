package electroblob.wizardry.registry;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.advancement.*;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.util.ResourceLocation;

/**
 * Class responsible for defining, storing and registering all of wizardry's advancement triggers. As of wizardry 4.2,
 * the 'dummy' advancement triggers are being phased out in favour of proper custom triggers with JSON parameters, or
 * where possible, existing triggers in Minecraft.
 *
 * @author 12foo, Electroblob
 * @since Wizardry 4.1.0
 */
public final class WizardryAdvancementTriggers {

	private WizardryAdvancementTriggers(){} // No instances!

    public static final CustomAdvancementTrigger max_out_wand = new CustomAdvancementTrigger("trigger_max_out_wand");
    public static final CustomAdvancementTrigger special_upgrade = new CustomAdvancementTrigger("trigger_special_upgrade");
    public static final CustomAdvancementTrigger anger_wizard = new CustomAdvancementTrigger("trigger_anger_wizard");
    public static final CustomAdvancementTrigger buy_master_spell = new CustomAdvancementTrigger("trigger_buy_master_spell");
  	public static final CustomAdvancementTrigger wizard_trade = new CustomAdvancementTrigger("trigger_wizard_trade");
	public static final CustomAdvancementTrigger spell_failure = new CustomAdvancementTrigger("trigger_spell_failure");
	public static final CustomAdvancementTrigger wand_levelup = new CustomAdvancementTrigger("trigger_wand_levelup");
	public static final CustomAdvancementTrigger restore_imbuement_altar = new CustomAdvancementTrigger("restore_imbuement_altar");

	public static final StructureTrigger visit_structure = new StructureTrigger(new ResourceLocation(Wizardry.MODID, "visit_structure"));
	public static final WizardryContainerTrigger arcane_workbench = new WizardryContainerTrigger(new ResourceLocation(Wizardry.MODID, "arcane_workbench"));
	public static final SpellCastTrigger cast_spell = new SpellCastTrigger(new ResourceLocation(Wizardry.MODID, "cast_spell"));
	public static final SpellDiscoveryTrigger discover_spell = new SpellDiscoveryTrigger(new ResourceLocation(Wizardry.MODID, "discover_spell"));
	public static final WizardryContainerTrigger imbuement_altar = new WizardryContainerTrigger(new ResourceLocation(Wizardry.MODID, "imbuement_altar"));

	public static void register(){

		CriteriaTriggers.register(max_out_wand);
		CriteriaTriggers.register(special_upgrade);
		CriteriaTriggers.register(anger_wizard);
		CriteriaTriggers.register(buy_master_spell);
		CriteriaTriggers.register(wizard_trade);
		CriteriaTriggers.register(spell_failure);
		CriteriaTriggers.register(wand_levelup);
		CriteriaTriggers.register(restore_imbuement_altar);

		CriteriaTriggers.register(visit_structure);
		CriteriaTriggers.register(arcane_workbench);
		CriteriaTriggers.register(cast_spell);
		CriteriaTriggers.register(discover_spell);
		CriteriaTriggers.register(imbuement_altar);
	}
}