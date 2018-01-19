package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.construct.EntityLightningSigil;
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

public class LightningSigil extends Spell {

	public LightningSigil() {
		super(Tier.APPRENTICE, 10, Element.LIGHTNING, "lightning_sigil", SpellType.ATTACK, 20, EnumAction.NONE, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers) {
		
		RayTraceResult rayTrace = WizardryUtilities.rayTrace(10*modifiers.get(WizardryItems.range_upgrade), world, caster, false);
		
		if(rayTrace != null && rayTrace.typeOfHit == RayTraceResult.Type.BLOCK && rayTrace.sideHit == EnumFacing.UP){
			
			if(!world.isRemote){
				double x = rayTrace.hitVec.xCoord;
				double y = rayTrace.hitVec.yCoord;
				double z = rayTrace.hitVec.zCoord;
				EntityLightningSigil lightningsigil = new EntityLightningSigil(world, x, y, z, caster, modifiers.get(SpellModifiers.DAMAGE));
				world.spawnEntityInWorld(lightningsigil);
			}
			
			caster.swingArm(hand);
			WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_CONJURATION, 1.0F, 0.3F);
			return true;
		}
		return false;
	}


}
