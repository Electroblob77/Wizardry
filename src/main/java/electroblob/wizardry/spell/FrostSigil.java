package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.construct.EntityFrostSigil;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class FrostSigil extends Spell {

	public FrostSigil(){
		super(Tier.APPRENTICE, 10, Element.ICE, "frost_sigil", SpellType.ATTACK, 20, EnumAction.NONE, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		RayTraceResult rayTrace = WizardryUtilities.rayTrace(10 * modifiers.get(WizardryItems.range_upgrade), world,
				caster, false);

		if(rayTrace != null && rayTrace.typeOfHit == RayTraceResult.Type.BLOCK && rayTrace.sideHit == EnumFacing.UP){
			if(!world.isRemote){
				double x = rayTrace.hitVec.xCoord;
				double y = rayTrace.hitVec.yCoord;
				double z = rayTrace.hitVec.zCoord;
				EntityFrostSigil frostsigil = new EntityFrostSigil(world, x, y, z, caster,
						modifiers.get(SpellModifiers.DAMAGE));
				world.spawnEntity(frostsigil);
			}
			caster.swingArm(hand);
			WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_ICE, 1.0F, 1.0F);
			return true;
		}
		return false;
	}

}
