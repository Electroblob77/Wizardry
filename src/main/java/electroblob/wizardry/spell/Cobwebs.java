package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.tileentity.TileEntityTimer;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class Cobwebs extends Spell {

	private static final int baseDuration = 400;

	public Cobwebs(){
		super(Tier.ADVANCED, 30, Element.EARTH, "cobwebs", SpellType.ATTACK, 70, EnumAction.NONE, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		RayTraceResult rayTrace = WizardryUtilities.rayTrace(12 * modifiers.get(WizardryItems.range_upgrade), world,
				caster, true);

		if(rayTrace != null && rayTrace.typeOfHit == RayTraceResult.Type.BLOCK){

			boolean flag = false;

			BlockPos pos = rayTrace.getBlockPos().offset(rayTrace.sideHit);

			if(world.isAirBlock(pos)){
				if(!world.isRemote){
					world.setBlockState(pos, WizardryBlocks.vanishing_cobweb.getDefaultState());
					if(world.getTileEntity(pos) instanceof TileEntityTimer){
						((TileEntityTimer)world.getTileEntity(pos))
								.setLifetime((int)(baseDuration * modifiers.get(WizardryItems.duration_upgrade)));
					}
				}
				flag = true;
			}

			for(EnumFacing side : EnumFacing.values()){

				BlockPos pos1 = pos.offset(side);

				if(world.isAirBlock(pos1)){
					if(!world.isRemote){
						world.setBlockState(pos1, WizardryBlocks.vanishing_cobweb.getDefaultState());
						if(world.getTileEntity(pos1) instanceof TileEntityTimer){
							((TileEntityTimer)world.getTileEntity(pos1))
									.setLifetime((int)(baseDuration * modifiers.get(WizardryItems.duration_upgrade)));
						}
					}
					flag = true;
				}
			}

			if(flag){
				caster.swingArm(hand);
				WizardryUtilities.playSoundAtPlayer(caster, SoundEvents.BLOCK_LAVA_EXTINGUISH, 1.0f, 1.0f);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target,
			SpellModifiers modifiers){

		if(target != null){

			int x = MathHelper.floor(target.posX);
			int y = (int)target.getEntityBoundingBox().minY;
			int z = MathHelper.floor(target.posZ);

			boolean flag = false;

			BlockPos pos = new BlockPos(x, y, z);

			if(world.isAirBlock(pos)){
				if(!world.isRemote){
					world.setBlockState(pos, WizardryBlocks.vanishing_cobweb.getDefaultState());
					if(world.getTileEntity(pos) instanceof TileEntityTimer){
						((TileEntityTimer)world.getTileEntity(pos))
								.setLifetime((int)(baseDuration * modifiers.get(WizardryItems.duration_upgrade)));
					}
				}
				flag = true;
			}

			for(EnumFacing side : EnumFacing.values()){

				BlockPos pos1 = pos.offset(side);

				if(world.isAirBlock(pos1)){
					if(!world.isRemote){
						world.setBlockState(pos1, WizardryBlocks.vanishing_cobweb.getDefaultState());
						if(world.getTileEntity(pos1) instanceof TileEntityTimer){
							((TileEntityTimer)world.getTileEntity(pos1))
									.setLifetime((int)(baseDuration * modifiers.get(WizardryItems.duration_upgrade)));
						}
					}
					flag = true;
				}
			}

			if(flag){
				caster.swingArm(hand);
				caster.playSound(SoundEvents.BLOCK_LAVA_EXTINGUISH, 1.0f, 1.0f);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

}
