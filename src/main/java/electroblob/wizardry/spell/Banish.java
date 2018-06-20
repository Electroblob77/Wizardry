package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class Banish extends SpellRay {

	public Banish(){
		super("banish", Tier.APPRENTICE, Element.NECROMANCY, SpellType.ATTACK, 15, 40, false, 10, SoundEvents.ENTITY_ENDERMEN_TELEPORT);
	}

	@Override
	protected boolean onEntityHit(World world, Entity target, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		
		if(target instanceof EntityLivingBase){

			EntityLivingBase entity = (EntityLivingBase)target;

			double radius = (8 + world.rand.nextDouble() * 8) * modifiers.get(WizardryItems.range_upgrade);
			double angle = world.rand.nextDouble() * Math.PI * 2;

			int x = MathHelper.floor(entity.posX + Math.sin(angle) * radius);
			int z = MathHelper.floor(entity.posZ - Math.cos(angle) * radius);
			int y = WizardryUtilities.getNearestFloorLevel(world,
					new BlockPos(x, (int)caster.getEntityBoundingBox().minY, z), (int)radius);

			if(world.isRemote){
				for(int i=0; i<10; i++){
					double dx1 = entity.posX;
					double dy1 = entity.getEntityBoundingBox().minY + entity.height * world.rand.nextFloat();
					double dz1 = entity.posZ;
					world.spawnParticle(EnumParticleTypes.PORTAL, dx1, dy1, dz1, world.rand.nextDouble() - 0.5,
							world.rand.nextDouble() - 0.5, world.rand.nextDouble() - 0.5);
				}
			}

			if(y > -1){

				// This means stuff like snow layers is ignored, meaning when on snow-covered ground the target does
				// not teleport 1 block above the ground.
				if(!world.getBlockState(new BlockPos(x, y, z)).getMaterial().blocksMovement()){
					y--;
				}

				if(world.getBlockState(new BlockPos(x, y + 1, z)).getMaterial().blocksMovement()
						|| world.getBlockState(new BlockPos(x, y + 2, z)).getMaterial().blocksMovement()){
					return false;
				}

				if(!world.isRemote){
					entity.setPositionAndUpdate(x + 0.5, y + 1, z + 0.5);
				}

				entity.playSound(SoundEvents.ENTITY_ENDERMEN_TELEPORT, 1.0F, 1.0f);
			}
		}
		
		return true;
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onMiss(World world, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		return true;
	}
	
	@Override
	protected void spawnParticle(World world, double x, double y, double z, double vx, double vy, double vz){
		world.spawnParticle(EnumParticleTypes.PORTAL, x, y - 0.5, z, 0, 0, 0);
		ParticleBuilder.create(Type.DARK_MAGIC).pos(x, y, z).colour(0.2f, 0, 0.2f).spawn(world);
	}

}
