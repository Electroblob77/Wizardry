package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.tileentity.TileEntityTimer;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Cobwebs extends SpellRay {

	private static final int BASE_DURATION = 400;

	public Cobwebs(){
		super("cobwebs", Tier.ADVANCED, Element.EARTH, SpellType.ATTACK, 30, 70, false, 12, SoundEvents.BLOCK_LAVA_EXTINGUISH);
		this.ignoreEntities(true);
	}

	@Override public boolean doesSpellRequirePacket(){ return false; }

	@Override
	protected boolean onEntityHit(World world, Entity target, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		
		boolean flag = false;
		
		pos = pos.offset(side);

		if(world.isAirBlock(pos)){
			if(!world.isRemote){
				world.setBlockState(pos, WizardryBlocks.vanishing_cobweb.getDefaultState());
				if(world.getTileEntity(pos) instanceof TileEntityTimer){
					((TileEntityTimer)world.getTileEntity(pos))
							.setLifetime((int)(BASE_DURATION * modifiers.get(WizardryItems.duration_upgrade)));
				}
			}
			flag = true;
		}

		for(EnumFacing facing : EnumFacing.values()){

			BlockPos pos1 = pos.offset(facing);

			if(world.isAirBlock(pos1)){
				if(!world.isRemote){
					world.setBlockState(pos1, WizardryBlocks.vanishing_cobweb.getDefaultState());
					if(world.getTileEntity(pos1) instanceof TileEntityTimer){
						((TileEntityTimer)world.getTileEntity(pos1))
								.setLifetime((int)(BASE_DURATION * modifiers.get(WizardryItems.duration_upgrade)));
					}
				}
				flag = true;
			}
		}
		
		return flag;
	}

	@Override
	protected boolean onMiss(World world, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

}
