package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryParticleType;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class WallOfFrost extends Spell {

	public WallOfFrost(){
		super(Tier.MASTER, 15, Element.ICE, "wall_of_frost", SpellType.UTILITY, 0, EnumAction.NONE, true);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		// IDEA: Use frosted ice instead of ice statue

		Vec3d look = caster.getLookVec();

		RayTraceResult rayTrace = WizardryUtilities.rayTrace(10 * modifiers.get(WizardryItems.range_upgrade), world,
				caster, true);

		if(rayTrace != null && !world.isRemote){

			BlockPos pos = rayTrace.getBlockPos();

			// Stops the ice being placed floating above snow and grass. Directions other than up included for
			// completeness.
			if(WizardryUtilities.canBlockBeReplaced(world, pos)){
				// Moves the blockpos back into the block
				pos = pos.offset(rayTrace.sideHit.getOpposite());
			}

			if(caster.getDistance(pos.getX(), pos.getY(), pos.getZ()) > 2
					&& world.getBlockState(pos).getBlock() != WizardryBlocks.ice_statue){

				pos = pos.offset(rayTrace.sideHit);

				if(WizardryUtilities.canBlockBeReplaced(world, pos)){
					world.setBlockState(pos, WizardryBlocks.ice_statue.getDefaultState());
				}

				// Builds a 2 block high wall if it hits the ground
				if(rayTrace.sideHit == EnumFacing.UP){
					pos = pos.offset(rayTrace.sideHit);

					if(WizardryUtilities.canBlockBeReplaced(world, pos)){
						world.setBlockState(pos, WizardryBlocks.ice_statue.getDefaultState());
					}
				}
			}
		}

		for(int i = 0; i < 20; i++){

			if(world.isRemote){

				double x1 = caster.posX + look.xCoord * i / 2 + world.rand.nextFloat() / 5 - 0.1f;
				double y1 = WizardryUtilities.getPlayerEyesPos(caster) - 0.4f + look.yCoord * i / 2
						+ world.rand.nextFloat() / 5 - 0.1f;
				double z1 = caster.posZ + look.zCoord * i / 2 + world.rand.nextFloat() / 5 - 0.1f;
				Wizardry.proxy.spawnParticle(WizardryParticleType.SPARKLE, world, x1, y1, z1,
						look.xCoord * modifiers.get(WizardryItems.range_upgrade),
						look.yCoord * modifiers.get(WizardryItems.range_upgrade),
						look.zCoord * modifiers.get(WizardryItems.range_upgrade), 8 + world.rand.nextInt(12), 0.4f,
						0.6f, 1.0f);

				x1 = caster.posX + look.xCoord * i / 2 + world.rand.nextFloat() / 5 - 0.1f;
				y1 = WizardryUtilities.getPlayerEyesPos(caster) - 0.4f + look.yCoord * i / 2
						+ world.rand.nextFloat() / 5 - 0.1f;
				z1 = caster.posZ + look.zCoord * i / 2 + world.rand.nextFloat() / 5 - 0.1f;
				Wizardry.proxy.spawnParticle(WizardryParticleType.SPARKLE, world, x1, y1, z1,
						look.xCoord * modifiers.get(WizardryItems.range_upgrade),
						look.yCoord * modifiers.get(WizardryItems.range_upgrade),
						look.zCoord * modifiers.get(WizardryItems.range_upgrade), 8 + world.rand.nextInt(12), 1.0f,
						1.0f, 1.0f);
			}
		}

		if(ticksInUse % 12 == 0){
			if(ticksInUse == 0) WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_ICE, 0.5F, 1.0f);
			WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_LOOP_ICE, 0.5F, 1.0f);
		}

		return true;
	}

}
