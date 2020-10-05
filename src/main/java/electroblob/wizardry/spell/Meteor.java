package electroblob.wizardry.spell;

import electroblob.wizardry.entity.EntityMeteor;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class Meteor extends SpellRay {

	// It doesn't really make sense to have a blast radius when the explosion is measured by strength
	public static final String BLAST_STRENGTH = "blast_strength";

	public Meteor(){
		super("meteor", SpellActions.POINT, false);
		this.soundValues(3, 1, 0);
		this.ignoreLivingEntities(true);
		addProperties(BLAST_STRENGTH);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		if(ItemArtefact.isArtefactActive(caster, WizardryItems.ring_meteor)){

			if(!world.isRemote){

				EntityMeteor meteor = new EntityMeteor(world, caster.posX, caster.posY + caster.getEyeHeight(), caster.posZ,
						modifiers.get(WizardryItems.blast_upgrade), EntityUtils.canDamageBlocks(caster, world));

				Vec3d direction = caster.getLookVec().scale(2 * modifiers.get(WizardryItems.range_upgrade));
				meteor.motionX = direction.x;
				meteor.motionY = direction.y;
				meteor.motionZ = direction.z;

				world.spawnEntity(meteor);
			}

			this.playSound(world, caster, ticksInUse, -1, modifiers);
			return true;

		}else{
			return super.cast(world, caster, hand, ticksInUse, modifiers);
		}
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
						modifiers.get(WizardryItems.blast_upgrade), EntityUtils.canDamageBlocks(caster, world));
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
