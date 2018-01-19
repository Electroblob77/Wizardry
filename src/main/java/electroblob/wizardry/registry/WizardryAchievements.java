package electroblob.wizardry.registry;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.stats.Achievement;
import net.minecraftforge.common.AchievementPage;

/**
 * Class responsible for defining, storing and registering all of wizardry's achievements (achievements don't have a
 * registry, but it make sense to do them the same way as everything else).
 * @author Electroblob
 * @since Wizardry 2.1
 */
public final class WizardryAchievements {

	public static final Achievement crystal = new Achievement("crystal", "crystal", -1, -1, WizardryItems.magic_crystal, null).registerStat();
	public static final Achievement arcane_initiate = new Achievement("arcane_initiate", "arcane_initiate", 2, -1, WizardryItems.magic_wand, crystal).registerStat();
	public static final Achievement apprentice = new Achievement("apprentice", "apprentice", 2, 3, WizardryItems.apprentice_wand, arcane_initiate).registerStat();
	public static final Achievement master = new Achievement("master", "master", 3, 6, WizardryItems.master_wand, apprentice).registerStat();
	public static final Achievement all_spells = new Achievement("all_spells", "all_spells", 5, 7, WizardryItems.wizard_handbook, master).registerStat().setSpecial();
	public static final Achievement wizard_trade = new Achievement("wizard_trade", "wizard_trade", 2, -5, Items.EMERALD, arcane_initiate).registerStat();
	public static final Achievement buy_master_spell = new Achievement("buy_master_spell", "buy_master_spell", 5, -5, WizardryItems.spell_book, wizard_trade).registerStat().setSpecial();
	public static final Achievement freeze_blaze = new Achievement("freeze_blaze", "freeze_blaze", -1, 3, Blocks.ICE, apprentice).registerStat();
	public static final Achievement charge_creeper = new Achievement("charge_creeper", "charge_creeper", -1, 1, Items.GUNPOWDER, arcane_initiate).registerStat();
	public static final Achievement frankenstein = new Achievement("frankenstein", "frankenstein", -3, 1, WizardryItems.advanced_lightning_wand, charge_creeper).registerStat().setSpecial();
	public static final Achievement special_upgrade = new Achievement("special_upgrade", "special_upgrade", 4, 1, WizardryItems.condenser_upgrade, arcane_initiate).registerStat();
	public static final Achievement craft_flask = new Achievement("craft_flask", "craft_flask", 4, -2, WizardryItems.mana_flask, arcane_initiate).registerStat();
	public static final Achievement elemental = new Achievement("elemental", "elemental", 6, -1, WizardryItems.basic_fire_wand, arcane_initiate).registerStat();
	public static final Achievement armour_set = new Achievement("armour_set", "armour_set", 0, -4, WizardryItems.wizard_hat, arcane_initiate).registerStat();
	public static final Achievement legendary = new Achievement("legendary", "legendary", -2, -5, WizardryItems.armour_upgrade, armour_set).registerStat().setSpecial();
	public static final Achievement self_destruct = new Achievement("self_destruct", "self_destruct", 1, 0, Blocks.PUMPKIN, arcane_initiate).registerStat();
	public static final Achievement pig_tornado = new Achievement("pig_tornado", "pig_tornado", 6, 3, Items.SADDLE, apprentice).registerStat().setSpecial();
	public static final Achievement jam_wizard = new Achievement("jam_wizard", "jam_wizard", 4, 4, Blocks.WEB, apprentice).registerStat();
	public static final Achievement slime_skeleton = new Achievement("slime_skeleton", "slime_skeleton", 1, 5, Items.SLIME_BALL, apprentice).registerStat();
	public static final Achievement anger_wizard = new Achievement("anger_wizard", "anger_wizard", 1, -7, Items.IRON_SWORD, wizard_trade).registerStat();
	public static final Achievement defeat_evil_wizard = new Achievement("defeat_evil_wizard", "defeat_evil_wizard", 4, -7, WizardryItems.wizard_boots_necromancy, wizard_trade).registerStat();
	public static final Achievement max_out_wand = new Achievement("max_out_wand", "max_out_wand", 7, 1, WizardryItems.arcane_tome, special_upgrade).registerStat().setSpecial();
	public static final Achievement element_master = new Achievement("element_master", "element_master", 7, -3, WizardryItems.master_ice_wand, elemental).registerStat().setSpecial();
	public static final Achievement identify_spell = new Achievement("identify_spell", "identify_spell", -2, -3, WizardryItems.identification_scroll, arcane_initiate).registerStat();
	
	private static final Achievement[] ACHIEVEMENTS_LIST = {crystal, arcane_initiate, apprentice, master, all_spells, wizard_trade, buy_master_spell, freeze_blaze, charge_creeper,
	frankenstein, special_upgrade, craft_flask, elemental, armour_set, legendary, self_destruct, pig_tornado, jam_wizard, slime_skeleton, anger_wizard, defeat_evil_wizard,
	max_out_wand, element_master, identify_spell};
	
	public static final AchievementPage WIZARDRY_ACHIEVEMENT_PAGE = new AchievementPage("Wizardry", ACHIEVEMENTS_LIST);
	
}