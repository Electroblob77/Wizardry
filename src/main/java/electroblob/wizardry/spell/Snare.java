package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.tileentity.TileEntityPlayerSave;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Snare extends SpellRay {

	public Snare(){
		super("snare", Tier.BASIC, Element.EARTH, SpellType.ATTACK, 10, 10, false, 10, SoundEvents.BLOCK_GRASS_PLACE);
		this.soundValues(1, 1.4f, 0.4f);
		this.ignoreEntities(true);
	}

	@Override
	protected boolean onEntityHit(World world, Entity target, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		
		if(side == EnumFacing.UP && world.isSideSolid(pos, EnumFacing.UP)
				&& WizardryUtilities.canBlockBeReplaced(world, pos.up())){

			if(!world.isRemote){
				world.setBlockState(pos.up(), WizardryBlocks.snare.getDefaultState());
				((TileEntityPlayerSave)world.getTileEntity(pos.up())).setCaster(caster);
			}

			return true;
		}
		
		return false;
	}

	@Override
	protected boolean onMiss(World world, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		return false;
	}
	
	@Override
	protected void spawnParticle(World world, double x, double y, double z, double vx, double vy, double vz){
		float brightness = world.rand.nextFloat() * 0.25f;
		ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).time(20 + world.rand.nextInt(8))
		.clr(brightness, brightness + 0.1f, 0).spawn(world);
		ParticleBuilder.create(Type.LEAF).pos(x, y, z).vel(0, -0.01, 0).time(40 + world.rand.nextInt(10)).spawn(world);
	}

}
