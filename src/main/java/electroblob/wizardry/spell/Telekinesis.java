package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class Telekinesis extends SpellRay {

	public Telekinesis(){
		super("telekinesis", SpellActions.POINT, false);
	}

	@Override public boolean requiresPacket(){ return false; }

	@Override
	protected boolean onEntityHit(World world, Entity target, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){
		
		if(target instanceof EntityItem){

			target.motionX = (origin.x - target.posX) / 6;
			target.motionY = (origin.y - target.posY) / 6;
			target.motionZ = (origin.z - target.posZ) / 6;
			return true;

		}else if(target instanceof EntityPlayer && (Wizardry.settings.telekineticDisarmament || !(caster instanceof EntityPlayer))){

			EntityPlayer player = (EntityPlayer)target;
			
			// IDEA: Disarm the offhand if the mainhand is empty or otherwise harmless?

			if(!player.getHeldItemMainhand().isEmpty()){

				if(!world.isRemote){
					EntityItem item = player.entityDropItem(player.getHeldItemMainhand(), 0);
					// Makes the item move towards the caster
					item.motionX = (origin.x - player.posX) / 20;
					item.motionZ = (origin.z - player.posZ) / 20;
				}

				player.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
				
				return true;
			}
		}
		
		return false;
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){
		
		if(caster instanceof EntityPlayer){
			
			IBlockState blockstate = world.getBlockState(pos);
	
			if(blockstate.getBlock().onBlockActivated(world, pos, blockstate, (EntityPlayer)caster, EnumHand.MAIN_HAND,
					side, 0, 0, 0)){
				return true;
			}
		}
		
		return false;
	}

	@Override
	protected boolean onMiss(World world, EntityLivingBase caster, Vec3d origin, Vec3d direction, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

}
