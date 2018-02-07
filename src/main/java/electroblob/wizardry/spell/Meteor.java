package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.EntityMeteor;
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

public class Meteor extends Spell {

	public Meteor(){
		super(Tier.MASTER, 100, Element.FIRE, "meteor", SpellType.ATTACK, 200, EnumAction.NONE, false);
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
					EntityMeteor meteor = new EntityMeteor(world, pos.getX(), pos.getY() + 50, pos.getZ(),
							modifiers.get(WizardryItems.blast_upgrade));
					world.spawnEntity(meteor);
				}

				caster.swingArm(hand);
				WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_SUMMONING, 3.0f, 1.0f);
				return true;
			}
		}
		return false;
	}

}
