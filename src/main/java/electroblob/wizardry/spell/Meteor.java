package electroblob.wizardry.spell;

import electroblob.wizardry.entity.EntityMeteor;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class Meteor extends SpellRay {

	// It doesn't really make sense to have a blast radius when the explosion is measured by strength
	public static final String BLAST_STRENGTH = "blast_strength";

	public Meteor(){
		super("meteor", false, EnumAction.NONE);
		this.soundValues(3, 1, 0);
		this.ignoreLivingEntities(true);
		addProperties(BLAST_STRENGTH);
	}

	@Override public boolean requiresPacket(){ return false; }

	@Override
	protected boolean onEntityHit(World world, Entity target, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){
		
		if(world.canBlockSeeSky(pos.up())){

			if(!world.isRemote){
				EntityMeteor meteor = new EntityMeteor(world, pos.getX(), pos.getY() + 50, pos.getZ(),
						modifiers.get(WizardryItems.blast_upgrade), WizardryUtilities.canDamageBlocks(caster, world));
				world.spawnEntity(meteor);
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
