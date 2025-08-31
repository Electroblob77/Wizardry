package electroblob.wizardry.potion;

import electroblob.wizardry.registry.WizardrySounds;
import net.minecraft.init.SoundEvents;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PotionFireskin extends PotionMagicEffectParticles {

	public PotionFireskin(boolean isBadEffect, int liquidColour, ResourceLocation texture) {
		super(isBadEffect, liquidColour, texture);
		this.setBeneficial();
	}

	@Override
	public void spawnCustomParticle(World world, double x, double y, double z){
		if(world.isRemote){
			// Spawn ambient fire particles around the entity
			world.spawnParticle(EnumParticleTypes.FLAME, x, y, z, 0, 0, 0);
		}
	}

	@Override
	public boolean isReady(int duration, int amplifier) {
		return true;
	}

	@Override
	public void performEffect(EntityLivingBase entitylivingbase, int strength){

		// Original fireskin behavior: extinguish the entity to prevent fire damage from melee attacks
		entitylivingbase.extinguish();

		// Server-side cobweb burning logic
		World world = entitylivingbase.world;
		if(!world.isRemote){

			AxisAlignedBB entityBox = entitylivingbase.getEntityBoundingBox();

			// Expand the bounding box slightly to catch cobwebs the entity is touching
			entityBox = entityBox.expand(0.1, 0.1, 0.1);

			BlockPos minPos = new BlockPos(entityBox.minX, entityBox.minY, entityBox.minZ);
			BlockPos maxPos = new BlockPos(entityBox.maxX, entityBox.maxY, entityBox.maxZ);

			boolean burnedCobweb = false;

			// Check all blocks in the entity's bounding box
			for(int x = minPos.getX(); x <= maxPos.getX(); x++){
				for(int y = minPos.getY(); y <= maxPos.getY(); y++){
					for(int z = minPos.getZ(); z <= maxPos.getZ(); z++){

						BlockPos pos = new BlockPos(x, y, z);

						if(world.getBlockState(pos).getBlock() == Blocks.WEB){
							// Burn the cobweb
							world.setBlockToAir(pos);
							burnedCobweb = true;

							// Add visual effects - spawn particles on client side
							if(world.rand.nextFloat() < 0.7f){ // 70% chance to spawn particles for better visibility
								float offsetX = world.rand.nextFloat() * 0.1f - 0.05f;
								float offsetY = world.rand.nextFloat() * 0.1f + 0.05f;
								float offsetZ = world.rand.nextFloat() * 0.1f - 0.05f;

								// Main flame particle rising up
								world.spawnParticle(EnumParticleTypes.FLAME,
									pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
									0, 0.1, 0);

								// Two additional flame particles with random movement
								world.spawnParticle(EnumParticleTypes.FLAME,
									pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
									offsetX, offsetY, offsetZ);

								world.spawnParticle(EnumParticleTypes.FLAME,
									pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
									offsetX * -0.5f, offsetY * 0.8f, offsetZ * -0.5f);
							}
						}
					}
				}
			}

			// Play the fire extinguish sound only when cobwebs were actually burned
			if(burnedCobweb){
				world.playSound(null, entitylivingbase.getPosition(), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.6f, 0.8f + world.rand.nextFloat() * 0.4f);
			}
		}
	}
}
