package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.construct.EntityDecay;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class Decay extends Spell {

	public Decay(){
		super(Tier.ADVANCED, 50, Element.NECROMANCY, "decay", SpellType.ATTACK, 200, EnumAction.NONE, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		RayTraceResult rayTrace = WizardryUtilities.rayTrace(12 * modifiers.get(WizardryItems.range_upgrade), world,
				caster, false);

		if(rayTrace != null && rayTrace.typeOfHit == RayTraceResult.Type.BLOCK){

			BlockPos pos = rayTrace.getBlockPos();

			if(world.getBlockState(pos.up()).isNormalCube()) return false;

			if(!world.isRemote){

				world.spawnEntity(new EntityDecay(world, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, caster));

				for(int i = 0; i < 5; i++){
					BlockPos pos1 = WizardryUtilities.findNearbyFloorSpace(caster, 2, 6);
					if(pos1 == null) break;
					world.spawnEntity(
							new EntityDecay(world, pos1.getX() + 0.5, pos1.getY(), pos1.getZ() + 0.5, caster));
				}
			}

			WizardryUtilities.playSoundAtPlayer(caster, SoundEvents.ENTITY_WITHER_SHOOT, 1.0F,
					world.rand.nextFloat() * 0.2F + 1.0F);
			caster.swingArm(hand);
			return true;
		}

		return false;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target,
			SpellModifiers modifiers){

		if(target != null){

			int x = MathHelper.floor(target.posX);
			int y = (int)(int)target.getEntityBoundingBox().minY;
			int z = MathHelper.floor(target.posZ);

			if(world.getBlockState(new BlockPos(x, y, z)).isNormalCube()) return false;

			if(!world.isRemote){

				world.spawnEntity(new EntityDecay(world, x + 0.5, y + 1, z + 0.5, caster));

				for(int i = 0; i < 5; i++){
					BlockPos pos = WizardryUtilities.findNearbyFloorSpace(caster, 2, 6);
					if(pos == null) break;
					world.spawnEntity(new EntityDecay(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, caster));
				}
			}

			caster.playSound(SoundEvents.ENTITY_WITHER_SHOOT, 1.0F, world.rand.nextFloat() * 0.2F + 1.0F);
			caster.swingArm(hand);
			return true;
		}

		return false;
	}

	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

}
