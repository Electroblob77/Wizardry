package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryGuiHandler;
import electroblob.wizardry.client.GuiPortableCrafting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.item.EnumAction;
import net.minecraft.world.World;

public class PocketWorkbench extends Spell {

	public PocketWorkbench() {
		super(EnumTier.APPRENTICE, 30, EnumElement.SORCERY, "pocket_workbench", EnumSpellType.UTILITY, 40, EnumAction.bow, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {

		// TODO: Investigate possible item duplication bug with this spell. So far I have been unable to recreate it.
		if(!world.isRemote){
			caster.openGui(Wizardry.instance, WizardryGuiHandler.PORTABLE_CRAFTING, world, (int)caster.posX, (int)caster.posY, (int)caster.posZ);
		}

		world.playSoundAtEntity(caster, "wizardry:aura", 1, 1);

		return true;
	}

}
