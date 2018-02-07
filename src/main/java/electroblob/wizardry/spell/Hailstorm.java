package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.construct.EntityHailstorm;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class Hailstorm extends Spell {

	public Hailstorm(){
		super(Tier.MASTER, 75, Element.ICE, "hailstorm", SpellType.ATTACK, 300, EnumAction.NONE, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		RayTraceResult rayTrace = WizardryUtilities.rayTrace(20 * modifiers.get(WizardryItems.range_upgrade), world,
				caster, false);

		if(rayTrace != null && rayTrace.typeOfHit == RayTraceResult.Type.BLOCK){
			if(!world.isRemote){
				double x = rayTrace.hitVec.xCoord;
				double y = rayTrace.hitVec.yCoord;
				double z = rayTrace.hitVec.zCoord;
				// Moves the entity back towards the caster a bit, so the area of effect is better centred on the
				// position.
				// 3.0d is the distance to move the entity back towards the caster.
				double dx = caster.posX - x;
				double dz = caster.posZ - z;
				double distRatio = 3.0d / Math.sqrt(dx * dx + dz * dz);
				x += dx * distRatio;
				z += dz * distRatio;

				EntityHailstorm hailstorm = new EntityHailstorm(world, x, y + 5, z, caster,
						(int)(120 * modifiers.get(WizardryItems.duration_upgrade)),
						modifiers.get(SpellModifiers.DAMAGE));
				hailstorm.rotationYaw = caster.rotationYawHead;
				world.spawnEntity(hailstorm);
			}
			caster.swingArm(hand);
			WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_ICE, 1.0F, 1.0F);
			return true;
		}
		return false;
	}

}
