package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.construct.EntityHammer;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class LightningHammer extends Spell {

	public LightningHammer(){
		super(Tier.MASTER, 100, Element.LIGHTNING, "lightning_hammer", SpellType.ATTACK, 300, EnumAction.BOW, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		RayTraceResult rayTrace = WizardryUtilities.rayTrace(40 * modifiers.get(WizardryItems.range_upgrade), world,
				caster, false);

		if(rayTrace != null && rayTrace.typeOfHit == RayTraceResult.Type.BLOCK){

			BlockPos pos = rayTrace.getBlockPos();

			// Not sure why it is +1 but it has to be to work properly.
			if(world.canBlockSeeSky(pos.up())){

				if(!world.isRemote){

					EntityHammer hammer = new EntityHammer(world, pos.getX() + 0.5, pos.getY() + 50, pos.getZ() + 0.5,
							caster, (int)(600 * modifiers.get(WizardryItems.duration_upgrade)),
							modifiers.get(SpellModifiers.DAMAGE));

					hammer.motionX = 0;
					hammer.motionY = -2;
					hammer.motionZ = 0;

					world.spawnEntity(hammer);
				}

				caster.swingArm(hand);
				WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_SUMMONING, 3.0f, 1.0f);
				return true;
			}
		}
		return false;
	}

}
