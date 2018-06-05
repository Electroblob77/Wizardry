package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.tileentity.TileEntityPlayerSave;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class Snare extends Spell {

	public Snare(){
		super(Tier.BASIC, 10, Element.EARTH, "snare", SpellType.ATTACK, 10, EnumAction.NONE, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		RayTraceResult rayTrace = WizardryUtilities.rayTrace(10 * modifiers.get(WizardryItems.range_upgrade), world,
				caster, true);

		// Gets block the player is looking at and places snare
		if(rayTrace != null && rayTrace.typeOfHit == RayTraceResult.Type.BLOCK){

			BlockPos pos = rayTrace.getBlockPos();

			if(rayTrace.sideHit == EnumFacing.UP && world.isSideSolid(pos, EnumFacing.UP)
					&& WizardryUtilities.canBlockBeReplaced(world, pos.up())){

				if(!world.isRemote){
					world.setBlockState(pos.up(), WizardryBlocks.snare.getDefaultState());
					((TileEntityPlayerSave)world.getTileEntity(pos.up())).setCaster(caster);
				}

				double dx = pos.getX() + 0.5 - caster.posX;
				double dy = pos.getY() + 1.5 - (caster.posY + caster.height / 2);
				double dz = pos.getZ() + 0.5 - caster.posZ;

				if(world.isRemote){
					for(int i = 1; i < 5; i++){
						float brightness = world.rand.nextFloat() / 4;
						Wizardry.proxy.spawnParticle(Type.SPARKLE, world,
								caster.posX + (i * (dx / 5)) + world.rand.nextFloat() / 5,
								WizardryUtilities.getPlayerEyesPos(caster) + (i * (dy / 5))
										+ world.rand.nextFloat() / 5,
								caster.posZ + (i * (dz / 5)) + world.rand.nextFloat() / 5, 0.0d, 0.0d, 0.0d,
								20 + world.rand.nextInt(8), brightness, brightness + 0.1f, 0.0f);
						Wizardry.proxy.spawnParticle(Type.LEAF, world,
								caster.posX + (i * (dx / 5)) + world.rand.nextFloat() / 5,
								WizardryUtilities.getPlayerEyesPos(caster) + (i * (dy / 5))
										+ world.rand.nextFloat() / 5,
								caster.posZ + (i * (dz / 5)) + world.rand.nextFloat() / 5, 0, -0.01, 0,
								40 + world.rand.nextInt(10));
					}
				}

				caster.swingArm(hand);
				WizardryUtilities.playSoundAtPlayer(caster, SoundEvents.BLOCK_GRASS_PLACE, 1.0F,
						world.rand.nextFloat() * 0.4F + 1.2F);
				return true;
			}
		}
		return false;
	}

}
