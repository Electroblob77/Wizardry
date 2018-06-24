package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class PhaseStep extends Spell {

	public PhaseStep(){
		super("phase_step", Tier.ADVANCED, Element.SORCERY, SpellType.UTILITY, 35, 40, EnumAction.NONE, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		// Phase step does not gain range from range multiplier, instead it increases the thickness
		// of the wall you can teleport through.
		RayTraceResult rayTrace = WizardryUtilities.standardBlockRayTrace(world, caster, 5, false);

		if(rayTrace != null && rayTrace.typeOfHit == RayTraceResult.Type.BLOCK){

			BlockPos pos = new BlockPos(rayTrace.getBlockPos().getX(), (int)caster.posY, rayTrace.getBlockPos().getZ());

			// The maximum wall thickness as determined by the range multiplier. The + 0.5f is so that
			// weird float processing doesn't incorrectly round it down.
			int maxThickness = 1 + (int)((modifiers.get(WizardryItems.range_upgrade) - 1) / Constants.RANGE_INCREASE_PER_LEVEL + 0.5f);

			if(rayTrace.sideHit.getAxis().isHorizontal()){

				// i represents how far the player needs to teleport to get through the wall
				for(int i = 0; i <= maxThickness; i++){

					BlockPos pos1 = pos.offset(rayTrace.sideHit.getOpposite(), i);

					// Prevents the player from teleporting through unbreakable blocks, so they cannot cheat in other
					// mods' mazes and dungeons.
					if((WizardryUtilities.isBlockUnbreakable(world, pos1) || WizardryUtilities.isBlockUnbreakable(world, pos1.up()))
							&& !Wizardry.settings.teleportThroughUnbreakableBlocks)
						return false;

					if(!world.getBlockState(pos1).getMaterial().blocksMovement()
							&& !world.getBlockState(pos1.up()).getMaterial().blocksMovement()){

						if(!world.isRemote){
							caster.setPositionAndUpdate(pos1.getX() + 0.5, caster.posY, pos1.getZ() + 0.5);
						}

						caster.swingArm(hand);
						WizardryUtilities.playSoundAtPlayer(caster, SoundEvents.ENTITY_ENDERMEN_TELEPORT, 1.0F, 1.0f);
						return true;
					}
				}
			}
		}

		// This is here because the conditions are false on the client for whatever reason. (see the Javadoc for cast()
		// for an explanation)
		if(world.isRemote){
			for(int i = 0; i < 10; i++){
				double dx1 = caster.posX;
				double dy1 = caster.getEntityBoundingBox().minY + 2 * world.rand.nextFloat();
				double dz1 = caster.posZ;
				world.spawnParticle(EnumParticleTypes.PORTAL, dx1, dy1, dz1, world.rand.nextDouble() - 0.5,
						world.rand.nextDouble() - 0.5, world.rand.nextDouble() - 0.5);
			}
			
			// Can't be bothered to route this through the proxies!
			electroblob.wizardry.client.WizardryClientEventHandler.playBlinkEffect();
		}

		return false;
	}

}
