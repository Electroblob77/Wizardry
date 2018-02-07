package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.living.EntityShadowWraith;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SummonShadowWraith extends Spell {

	public SummonShadowWraith(){
		super(Tier.MASTER, 100, Element.NECROMANCY, "summon_shadow_wraith", SpellType.MINION, 400, EnumAction.BOW,
				false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		if(!world.isRemote){

			BlockPos pos = WizardryUtilities.findNearbyFloorSpace(caster, 2, 4);
			if(pos == null) return false;

			EntityShadowWraith shadowWraith = new EntityShadowWraith(world, pos.getX() + 0.5, pos.getY(),
					pos.getZ() + 0.5, caster, (int)(600 * modifiers.get(WizardryItems.duration_upgrade)));
			world.spawnEntity(shadowWraith);
		}
		WizardryUtilities.playSoundAtPlayer(caster, SoundEvents.ENTITY_WITHER_AMBIENT, 1.0F,
				world.rand.nextFloat() * 0.2F + 1.0F);
		return true;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public String getDescription(){
		return "\u00A7k" + super.getDescription();
	}

}
