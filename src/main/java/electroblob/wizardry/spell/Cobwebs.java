package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.tileentity.TileEntityTimer;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.List;

public class Cobwebs extends SpellRay {

	public Cobwebs(){
		super("cobwebs", false, SpellActions.POINT);
		this.ignoreLivingEntities(true);
		addProperties(EFFECT_RADIUS, DURATION);
	}

	@Override public boolean requiresPacket(){ return false; }

	@Override
	protected boolean onEntityHit(World world, Entity target, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){
		
		boolean flag = false;
		
		pos = pos.offset(side);

		int blastUpgradeCount = (int)((modifiers.get(WizardryItems.blast_upgrade) - 1) / Constants.RANGE_INCREASE_PER_LEVEL + 0.5f);

		float radius = getProperty(EFFECT_RADIUS).floatValue() + 0.73f * blastUpgradeCount;

		List<BlockPos> sphere = WizardryUtilities.getBlockSphere(pos, radius * modifiers.get(WizardryItems.blast_upgrade));

		for(BlockPos pos1 : sphere){

			if(world.isAirBlock(pos1)){
				if(!world.isRemote){
					world.setBlockState(pos1, WizardryBlocks.vanishing_cobweb.getDefaultState());
					if(world.getTileEntity(pos1) instanceof TileEntityTimer){
						((TileEntityTimer)world.getTileEntity(pos1))
								.setLifetime((int)(getProperty(DURATION).doubleValue()
										* modifiers.get(WizardryItems.duration_upgrade)));
					}
				}
				flag = true;
			}
		}
		
		return flag;
	}

	@Override
	protected boolean onMiss(World world, EntityLivingBase caster, Vec3d origin, Vec3d direction, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

}
