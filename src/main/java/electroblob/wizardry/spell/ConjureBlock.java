package electroblob.wizardry.spell;

import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.tileentity.TileEntityTimer;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ConjureBlock extends SpellRay {
	
	private static final String BLOCK_LIFETIME = "block_lifetime";

	public ConjureBlock(){
		super("conjure_block", false, EnumAction.NONE);
		this.ignoreLivingEntities(true);
		addProperties(BLOCK_LIFETIME);
	}

	@Override
	protected boolean onEntityHit(World world, Entity target, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){
		
		if(caster.isSneaking() && world.getBlockState(pos).getBlock() == WizardryBlocks.spectral_block){

			if(!world.isRemote){
				// Dispelling of blocks
				world.setBlockToAir(pos);
			}else{
				ParticleBuilder.create(Type.FLASH).pos(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5).scale(3)
				.clr(0.75f, 1, 0.85f).spawn(world);
			}
			
			return true;
		}
		
		pos = pos.offset(side);
		
		if(world.isRemote){
			ParticleBuilder.create(Type.FLASH).pos(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5).scale(3)
			.clr(0.75f, 1, 0.85f).spawn(world);
		}
		
		if(WizardryUtilities.canBlockBeReplaced(world, pos)){

			if(!world.isRemote){
				
				world.setBlockState(pos, WizardryBlocks.spectral_block.getDefaultState());
				
				if(world.getTileEntity(pos) instanceof TileEntityTimer){
					((TileEntityTimer)world.getTileEntity(pos)).setLifetime((int)(getProperty(BLOCK_LIFETIME).floatValue()
							* modifiers.get(WizardryItems.duration_upgrade)));
				}
			}

			return true;
		}
		
		return false;
	}

	@Override
	protected boolean onMiss(World world, EntityLivingBase caster, Vec3d origin, Vec3d direction, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

}
