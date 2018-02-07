package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryParticleType;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class Banish extends Spell {

	public Banish(){
		super(Tier.APPRENTICE, 15, Element.NECROMANCY, "banish", SpellType.ATTACK, 40, EnumAction.NONE, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		Vec3d look = caster.getLookVec();

		RayTraceResult rayTrace = WizardryUtilities.standardEntityRayTrace(world, caster,
				10 * modifiers.get(WizardryItems.range_upgrade));

		if(rayTrace != null && rayTrace.typeOfHit == RayTraceResult.Type.ENTITY
				&& rayTrace.entityHit instanceof EntityLivingBase){

			EntityLivingBase target = (EntityLivingBase)rayTrace.entityHit;

			double radius = (8 + world.rand.nextDouble() * 8) * modifiers.get(WizardryItems.range_upgrade);
			double angle = world.rand.nextDouble() * Math.PI * 2;

			int x = MathHelper.floor(target.posX + Math.sin(angle) * radius);
			int z = MathHelper.floor(target.posZ - Math.cos(angle) * radius);
			int y = WizardryUtilities.getNearestFloorLevel(world,
					new BlockPos(x, (int)caster.getEntityBoundingBox().minY, z), (int)radius);

			if(world.isRemote){
				for(int i = 0; i < 10; i++){
					double dx1 = target.posX;
					double dy1 = target.getEntityBoundingBox().minY + target.height * world.rand.nextFloat();
					double dz1 = target.posZ;
					world.spawnParticle(EnumParticleTypes.PORTAL, dx1, dy1, dz1, world.rand.nextDouble() - 0.5,
							world.rand.nextDouble() - 0.5, world.rand.nextDouble() - 0.5);
				}
			}

			if(y > -1){

				// This means stuff like snow layers is ignored, meaning when on snow-covered ground the caster does
				// not teleport 1 block above the ground.
				if(!world.getBlockState(new BlockPos(x, y, z)).getMaterial().blocksMovement()){
					y--;
				}

				if(world.getBlockState(new BlockPos(x, y + 1, z)).getMaterial().blocksMovement()
						|| world.getBlockState(new BlockPos(x, y + 2, z)).getMaterial().blocksMovement()){
					return false;
				}

				if(!world.isRemote){
					target.setPositionAndUpdate(x + 0.5, y + 1, z + 0.5);
				}

				target.playSound(SoundEvents.ENTITY_ENDERMEN_TELEPORT, 1.0F, 1.0f);
			}
		}

		if(world.isRemote){
			for(int i = 1; i < (int)(25 * modifiers.get(WizardryItems.range_upgrade)); i += 2){
				double x1 = caster.posX + look.xCoord * i / 2 + world.rand.nextFloat() / 5 - 0.1f;
				double y1 = WizardryUtilities.getPlayerEyesPos(caster) - 0.4f + look.yCoord * i / 2
						+ world.rand.nextFloat() / 5 - 0.1f;
				double z1 = caster.posZ + look.zCoord * i / 2 + world.rand.nextFloat() / 5 - 0.1f;

				world.spawnParticle(EnumParticleTypes.PORTAL, x1, y1 - 0.5, z1, 0.0d, 0.0d, 0.0d);
				Wizardry.proxy.spawnParticle(WizardryParticleType.DARK_MAGIC, world, x1, y1, z1, 0.0d, 0.0d, 0.0d, 0,
						0.2f, 0.0f, 0.2f);
			}
		}

		WizardryUtilities.playSoundAtPlayer(caster, SoundEvents.ENTITY_ENDERMEN_TELEPORT, 1.0F, 1.0f);
		caster.swingArm(hand);
		return true;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target,
			SpellModifiers modifiers){

		if(target != null){

			double radius = (8 + world.rand.nextDouble() * 8) * modifiers.get(WizardryItems.range_upgrade);
			double angle = world.rand.nextDouble() * Math.PI * 2;

			int x = MathHelper.floor(target.posX + Math.sin(angle) * radius);
			int z = MathHelper.floor(target.posZ - Math.cos(angle) * radius);
			int y = WizardryUtilities.getNearestFloorLevel(world,
					new BlockPos(x, (int)caster.getEntityBoundingBox().minY, z), (int)radius);

			if(world.isRemote){

				double dx = (target.posX - caster.posX) / caster.getDistanceToEntity(target);
				double dy = (target.posY - caster.posY) / caster.getDistanceToEntity(target);
				double dz = (target.posZ - caster.posZ) / caster.getDistanceToEntity(target);

				for(int i = 1; i < 25; i += 2){

					double x1 = caster.posX + dx * i / 2 + world.rand.nextFloat() / 5 - 0.1f;
					double y1 = caster.posY + caster.getEyeHeight() - 0.4f + dy * i / 2 + world.rand.nextFloat() / 5
							- 0.1f;
					double z1 = caster.posZ + dz * i / 2 + world.rand.nextFloat() / 5 - 0.1f;

					world.spawnParticle(EnumParticleTypes.PORTAL, x1, y1 - 0.5, z1, 0.0d, 0.0d, 0.0d);
					Wizardry.proxy.spawnParticle(WizardryParticleType.DARK_MAGIC, world, x1, y1, z1, 0.0d, 0.0d, 0.0d,
							0, 0.2f, 0.0f, 0.2f);
				}

				for(int i = 0; i < 10; i++){
					double dx1 = target.posX;
					double dy1 = target.getEntityBoundingBox().minY + target.height * world.rand.nextFloat();
					double dz1 = target.posZ;
					world.spawnParticle(EnumParticleTypes.PORTAL, dx1, dy1, dz1, world.rand.nextDouble() - 0.5,
							world.rand.nextDouble() - 0.5, world.rand.nextDouble() - 0.5);
				}
			}

			if(y > -1){

				// This means stuff like snow layers is ignored, meaning when on snow-covered ground the caster does
				// not teleport 1 block above the ground.
				if(!world.getBlockState(new BlockPos(x, y, z)).getMaterial().blocksMovement()){
					y--;
				}

				if(world.getBlockState(new BlockPos(x, y + 1, z)).getMaterial().blocksMovement()
						|| world.getBlockState(new BlockPos(x, y + 2, z)).getMaterial().blocksMovement()){
					return false;
				}

				if(!world.isRemote){
					target.setPositionAndUpdate(x + 0.5, y + 1, z + 0.5);
				}

				target.playSound(SoundEvents.ENTITY_ENDERMEN_TELEPORT, 1.0F, 1.0f);
			}
		}

		caster.playSound(SoundEvents.ENTITY_ENDERMEN_TELEPORT, 1.0F, 1.0f);
		caster.swingArm(hand);
		return true;
	}

	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

}
