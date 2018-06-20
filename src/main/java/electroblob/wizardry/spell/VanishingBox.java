package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class VanishingBox extends Spell {

	public VanishingBox(){
		super("vanishing_box", Tier.ADVANCED, Element.SORCERY, SpellType.UTILITY, 45, 70, EnumAction.BOW, false);
	}

	@Override public boolean doesSpellRequirePacket(){ return false; }

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		if(!world.isRemote){

			InventoryEnderChest enderchest = caster.getInventoryEnderChest();

			if(enderchest != null){
				caster.displayGUIChest(enderchest);
			}
		}

		WizardryUtilities.playSoundAtPlayer(caster, SoundEvents.BLOCK_ENDERCHEST_OPEN, 1, 1);

		return true;
	}

}
