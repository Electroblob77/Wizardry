package electroblob.wizardry.integration.conarm;

import baubles.api.BaubleType;
import c4.conarm.common.armor.utils.ArmorHelper;
import c4.conarm.lib.tinkering.TinkersArmor;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.ItemArtefact;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraftforge.fml.common.Loader;

/**
 * This class handles all of wizardry's integration with the <i>Construct's Armory</i> mod. This class contains only the code
 * that requires Construct's Armory to be loaded in order to run. Conversely, all code that requires Construct's Armory to be loaded is
 * located within this class or another class in the package {@code electroblob.wizardry.integration.conarm}.
 *
 * @author TheFlash787
 */
public class WizardryConstructsArmoryIntegration {

    public static final String CONARM_MOD_ID = "conarm";

    private static boolean conarmLoaded;

    public static void init(){
        conarmLoaded = Loader.isModLoaded(CONARM_MOD_ID);
    }

    public static boolean enabled(){
        return Wizardry.settings.constructsArmoryIntegration && conarmLoaded;
    }

    public static void damageArmor(ItemStack itemStack, DamageSource source, int amount, EntityPlayer player){
        if(enabled()){
            ArmorHelper.damageArmor(itemStack, source, amount, player);
        }
    }

    public static boolean isTinkersArmor(ItemStack itemStack){
        if(enabled()){
            return itemStack.getItem() instanceof TinkersArmor;
        }
        return false;
    }
}
