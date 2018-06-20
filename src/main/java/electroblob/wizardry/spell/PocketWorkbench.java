package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryGuiHandler;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class PocketWorkbench extends Spell {

	public PocketWorkbench(){
		super("pocket_workbench", Tier.APPRENTICE, Element.SORCERY, SpellType.UTILITY, 30, 40, EnumAction.BOW, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		// TODO: Investigate possible item duplication bug with this spell. So far I have been unable to recreate it.
		if(!world.isRemote){
			caster.openGui(Wizardry.instance, WizardryGuiHandler.PORTABLE_CRAFTING, world, (int)caster.posX,
					(int)caster.posY, (int)caster.posZ);
		}

		WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_CONJURATION, 1, 1);

		return true;
	}

}
