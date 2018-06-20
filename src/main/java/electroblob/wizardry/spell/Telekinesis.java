package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardrySounds;
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
import net.minecraft.world.World;

public class Telekinesis extends SpellRay {

	public Telekinesis(){
		super("telekinesis", Tier.BASIC, Element.SORCERY, SpellType.UTILITY, 5, 5, false, 8, WizardrySounds.SPELL_CONJURATION);
	}

	@Override public boolean doesSpellRequirePacket(){ return false; }

	@Override
	protected boolean onEntityHit(World world, Entity target, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		
		if(target instanceof EntityItem){

			target.motionX = (caster.posX - target.posX) / 6;
			target.motionY = (caster.posY + caster.getEyeHeight() - target.posY) / 6;
			target.motionZ = (caster.posZ - target.posZ) / 6;
			return true;

		}else if(target instanceof EntityPlayer && (Wizardry.settings.telekineticDisarmament || !(caster instanceof EntityPlayer))){

			EntityPlayer player = (EntityPlayer)target;
			
			// IDEA: Disarm the offhand if the mainhand is empty or otherwise harmless?

			if(!player.getHeldItemMainhand().isEmpty()){

				if(!world.isRemote){
					EntityItem item = player.entityDropItem(player.getHeldItemMainhand(), 0);
					// Makes the item move towards the caster
					item.motionX = (caster.posX - player.posX) / 20;
					item.motionZ = (caster.posZ - player.posZ) / 20;
				}

				player.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
				
				return true;
			}
		}
		
		return false;
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		
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
	protected boolean onMiss(World world, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

}
