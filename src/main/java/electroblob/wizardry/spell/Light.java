package electroblob.wizardry.spell;

import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.tileentity.TileEntityTimer;
import electroblob.wizardry.util.RayTracer;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class Light extends Spell {

	public Light(){
		super("light", EnumAction.NONE, false);
		addProperties(RANGE, DURATION);
	}

	@Override
	public boolean requiresPacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		double range = getProperty(RANGE).floatValue() * modifiers.get(WizardryItems.range_upgrade);

		RayTraceResult rayTrace = RayTracer.standardBlockRayTrace(world, caster, range, false);

		if(rayTrace != null && rayTrace.typeOfHit == RayTraceResult.Type.BLOCK){

			BlockPos pos = rayTrace.getBlockPos().offset(rayTrace.sideHit);

			if(world.isAirBlock(pos)){

				if(!world.isRemote){
					world.setBlockState(pos, WizardryBlocks.magic_light.getDefaultState());
					if(world.getTileEntity(pos) instanceof TileEntityTimer){
						int lifetime = ItemArtefact.isArtefactActive(caster, WizardryItems.charm_light) ? -1
								: (int)(getProperty(DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade));
						((TileEntityTimer)world.getTileEntity(pos)).setLifetime(lifetime);
					}
				}

				caster.swingArm(hand);
				this.playSound(world, caster, ticksInUse, -1, modifiers);
				return true;
			}
		}else{

			int x = (int)(Math.floor(caster.posX) + caster.getLookVec().x * range);
			int y = (int)(Math.floor(caster.posY) + caster.eyeHeight + caster.getLookVec().y * range);
			int z = (int)(Math.floor(caster.posZ) + caster.getLookVec().z * range);

			BlockPos pos = new BlockPos(x, y, z);

			if(world.isAirBlock(pos)){
				if(!world.isRemote){
					world.setBlockState(pos, WizardryBlocks.magic_light.getDefaultState());
					if(world.getTileEntity(pos) instanceof TileEntityTimer){
						int lifetime = ItemArtefact.isArtefactActive(caster, WizardryItems.charm_light) ? -1
								: (int)(getProperty(DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade));
						((TileEntityTimer)world.getTileEntity(pos)).setLifetime(lifetime);
					}
				}
				caster.swingArm(hand);
				this.playSound(world, caster, ticksInUse, -1, modifiers);
				return true;
			}
		}
		return false;
	}

}
