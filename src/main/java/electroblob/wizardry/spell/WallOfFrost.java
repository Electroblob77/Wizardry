package electroblob.wizardry.spell;

import electroblob.wizardry.block.BlockStatue;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.tileentity.TileEntityStatue;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WallOfFrost extends SpellRay {

	private static final int BASE_DURATION = 600;
	
	public WallOfFrost(){
		super("wall_of_frost", Tier.MASTER, Element.ICE, SpellType.UTILITY, 15, 0, true, 10, null);
		this.particleVelocity(1);
		this.particleSpacing(0.5);
	}
	
	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){
		// TODO: Temporary solution until I implement a better continuous sound system
		boolean flag = super.cast(world, caster, hand, ticksInUse, modifiers);
		if(flag){
			if(ticksInUse % 12 == 0){
				if(ticksInUse == 0) WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_ICE, 0.5f, 1);
				WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_LOOP_ICE, 0.5f, 1);
			}
		}
		return flag;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target, SpellModifiers modifiers){
		boolean flag = super.cast(world, caster, hand, ticksInUse, target, modifiers);
		if(flag){
			if(ticksInUse % 12 == 0){
				if(ticksInUse == 0) caster.playSound(WizardrySounds.SPELL_ICE, 0.5f, 1);
				caster.playSound(WizardrySounds.SPELL_LOOP_ICE, 0.5f, 1);
			}
		}
		return flag;
	}

	@Override
	protected boolean onEntityHit(World world, Entity target, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		// Wall of frost now freezes entities solid too!
		if(target instanceof EntityLiving && !world.isRemote){
			// Unchecked cast is fine because the block is a static final field
			if(((BlockStatue)WizardryBlocks.ice_statue).convertToStatue((EntityLiving)target,
					(int)(BASE_DURATION * modifiers.get(WizardryItems.duration_upgrade)))){
				
				target.playSound(WizardrySounds.SPELL_FREEZE, 1.0F, world.rand.nextFloat() * 0.4F + 0.8F);
				return true;
			}
		}
		
		return false;
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		
		// IDEA: Use frosted ice instead of ice statue blocks
		
		if(!world.isRemote){

			// Stops the ice being placed floating above snow and grass. Directions other than up included for
			// completeness.
			if(WizardryUtilities.canBlockBeReplaced(world, pos)){
				// Moves the blockpos back into the block
				pos = pos.offset(side.getOpposite());
			}

			if(caster.getDistance(pos.getX(), pos.getY(), pos.getZ()) > 2
					&& world.getBlockState(pos).getBlock() != WizardryBlocks.ice_statue){

				pos = pos.offset(side);
				
				int duration = (int)(BASE_DURATION * modifiers.get(WizardryItems.duration_upgrade));

				if(WizardryUtilities.canBlockBeReplaced(world, pos)){
					
					world.setBlockState(pos, WizardryBlocks.ice_statue.getDefaultState());
					
					if(world.getTileEntity(pos) instanceof TileEntityStatue){
						((TileEntityStatue)world.getTileEntity(pos)).setLifetime(duration);
					}
				}

				// Builds a 2 block high wall if it hits the ground
				if(side == EnumFacing.UP){
					pos = pos.offset(side);

					if(WizardryUtilities.canBlockBeReplaced(world, pos)){
						
						world.setBlockState(pos, WizardryBlocks.ice_statue.getDefaultState());
						
						if(world.getTileEntity(pos) instanceof TileEntityStatue){
							((TileEntityStatue)world.getTileEntity(pos)).setLifetime(duration);
						}
					}
				}
			}
		}
		
		return true;
	}

	@Override
	protected boolean onMiss(World world, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		return true;
	}
	
	@Override
	protected void spawnParticle(World world, double x, double y, double z, double vx, double vy, double vz){
		float brightness = world.rand.nextFloat();
		ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).vel(vx, vy, vz).time(8 + world.rand.nextInt(12))
		.clr(0.4f + 0.6f * brightness, 0.6f + 0.4f*brightness, 1).spawn(world);
		ParticleBuilder.create(Type.SNOW).pos(x, y, z).vel(vx, vy, vz).time(8 + world.rand.nextInt(12)).spawn(world);
	}

}
