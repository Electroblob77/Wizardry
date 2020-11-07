package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryGuiHandler;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class PocketWorkbench extends Spell {

	public PocketWorkbench(){
		super("pocket_workbench", SpellActions.IMBUE, false);
	}

	@Override
	public boolean requiresPacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		if(!world.isRemote){
			caster.openGui(Wizardry.instance, WizardryGuiHandler.PORTABLE_CRAFTING, world, (int)caster.posX,
					(int)caster.posY, (int)caster.posZ);
		}

		this.playSound(world, caster, ticksInUse, -1, modifiers);

		return true;
	}

}
