package electroblob.wizardry.spell;

import electroblob.wizardry.block.BlockThorns;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.tileentity.TileEntityThorns;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ForestOfThorns extends Spell {

	public ForestOfThorns(){
		super("forest_of_thorns", SpellActions.SUMMON, false);
		addProperties(EFFECT_RADIUS, DURATION, DAMAGE);
	}

	@Override public boolean requiresPacket(){ return false; }
	@Override public boolean canBeCastBy(EntityLiving npc, boolean override){ return true; }
	@Override public boolean canBeCastBy(TileEntityDispenser dispenser){ return true; }

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){
		if(!summonThorns(world, caster, caster.getPosition(), modifiers)) return false;
		this.playSound(world, caster, ticksInUse, -1, modifiers);
		return true;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target, SpellModifiers modifiers){
		if(!summonThorns(world, caster, caster.getPosition(), modifiers)) return false;
		this.playSound(world, caster, ticksInUse, -1, modifiers);
		return true;
	}

	@Override
	public boolean cast(World world, double x, double y, double z, EnumFacing direction, int ticksInUse, int duration, SpellModifiers modifiers){
		if(!summonThorns(world, null, new BlockPos(x, y, z).offset(direction), modifiers)) return false;
		this.playSound(world, x, y, z, ticksInUse, duration, modifiers);
		return true;
	}

	private boolean summonThorns(World world, @Nullable EntityLivingBase caster, BlockPos origin, SpellModifiers modifiers){

		if(!world.isRemote){

			double radius = getProperty(EFFECT_RADIUS).doubleValue() * modifiers.get(WizardryItems.blast_upgrade);

			List<BlockPos> ring = new ArrayList<>((int)(7 * radius)); // 7 is a bit more than 2 pi

			for(int x = -(int)radius; x <= radius; x++){

				for(int z = -(int)radius; z <= radius; z++){

					double distance = MathHelper.sqrt(x*x + z*z);

					if(distance > radius || distance < radius - 1.5) continue;

					Integer y = BlockUtils.getNearestSurface(world, origin.add(x, 0, z), EnumFacing.UP, (int)radius, true, BlockUtils.SurfaceCriteria.BUILDABLE);
					if(y != null) ring.add(new BlockPos(origin.getX() + x, y, origin.getZ() + z));
				}
			}

			if(ring.isEmpty()) return false;

			// Because we're always using EnumFacing.UP in the code above, we can be sure that pos is the block above the floor
			for(BlockPos pos : ring){

				if(BlockUtils.canBlockBeReplaced(world, pos) && BlockUtils.canBlockBeReplaced(world, pos.up())){

					((BlockThorns)WizardryBlocks.thorns).placeAt(world, pos, 3);

					TileEntity tileentity = world.getTileEntity(pos);

					if(tileentity instanceof TileEntityThorns){

						((TileEntityThorns)tileentity).setLifetime((int)(getProperty(DURATION).floatValue()
								* modifiers.get(WizardryItems.duration_upgrade)));

						if(caster != null) ((TileEntityThorns)tileentity).setCaster(caster);
						((TileEntityThorns)tileentity).damageMultiplier = modifiers.get(SpellModifiers.POTENCY);

						((TileEntityThorns)tileentity).sync();
					}
				}
			}
		}

		return true;
	}
}
