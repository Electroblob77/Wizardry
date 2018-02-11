package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.construct.EntityBlackHole;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class BlackHole extends Spell {

	public BlackHole(){
		super(Tier.MASTER, 150, Element.SORCERY, "black_hole", SpellType.ATTACK, 400, EnumAction.NONE, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		RayTraceResult rayTrace = WizardryUtilities.rayTrace(10 * modifiers.get(WizardryItems.range_upgrade), world,
				caster, false);

		if(rayTrace != null && rayTrace.typeOfHit == RayTraceResult.Type.BLOCK){

			// This demonstrates beautifully the elegance of BlockPos. In 1.7.10 this required a 50-line long switch
			// statement and a flag variable; now it only needs a few lines.
			BlockPos pos = new BlockPos(rayTrace.hitVec).offset(rayTrace.sideHit);

			if(world.isAirBlock(pos)){

				if(!world.isRemote){
					world.spawnEntity(new EntityBlackHole(world, pos.getX() + 0.5, pos.getY() - 1 + 0.5,
							pos.getZ() + 0.5, caster, (int)(600 * modifiers.get(WizardryItems.duration_upgrade)),
							modifiers.get(SpellModifiers.DAMAGE)));
				}

				caster.swingArm(hand);
				WizardryUtilities.playSoundAtPlayer(caster, SoundEvents.ENTITY_WITHER_SPAWN, 2.0f, 0.7f);
				return true;
			}

		}else{
			int x = (int)(Math.floor(caster.posX) + caster.getLookVec().x * 8);
			int y = (int)(Math.floor(caster.posY) + caster.eyeHeight + caster.getLookVec().y * 8);
			int z = (int)(Math.floor(caster.posZ) + caster.getLookVec().z * 8);
			if(!world.isRemote){
				world.spawnEntity(new EntityBlackHole(world, x, y, z, caster,
						(int)(600 * modifiers.get(WizardryItems.duration_upgrade)),
						modifiers.get(SpellModifiers.DAMAGE)));
			}
			caster.swingArm(hand);
			WizardryUtilities.playSoundAtPlayer(caster, SoundEvents.ENTITY_WITHER_SPAWN, 2.0f, 0.7f);
			return true;
		}
		return false;
	}

}
