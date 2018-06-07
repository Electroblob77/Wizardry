package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.entity.construct.EntityLightningSigil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;

public class LightningSigil extends Spell {

	public LightningSigil() {
		super(EnumTier.APPRENTICE, 10, EnumElement.LIGHTNING, "lightning_sigil", EnumSpellType.ATTACK, 20, EnumAction.none, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		MovingObjectPosition rayTrace = WizardryUtilities.rayTrace(10*rangeMultiplier, world, caster, false);
		
		if(rayTrace != null && rayTrace.typeOfHit == MovingObjectType.BLOCK && rayTrace.sideHit == 1){
			if(!world.isRemote){
				double x = rayTrace.blockX;
				double y = rayTrace.blockY;
				double z = rayTrace.blockZ;
				EntityLightningSigil lightningsigil = new EntityLightningSigil(world, x + 0.5, y+1, z + 0.5, caster, damageMultiplier);
				world.spawnEntityInWorld(lightningsigil);
			}
			caster.swingItem();
			world.playSoundAtEntity(caster, "wizardry:aura", 1.0F, 0.3F);
			return true;
		}
		return false;
	}


}
