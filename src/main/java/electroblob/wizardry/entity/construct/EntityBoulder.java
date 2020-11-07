package electroblob.wizardry.entity.construct;

import com.google.common.collect.Lists;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.Boulder;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.List;

public class EntityBoulder extends EntityScaledConstruct {

	private double velX, velZ;
	private int hitsRemaining;
	private boolean soundStarted = false;

	public EntityBoulder(World world){
		super(world);
		setSize(2.375f, 2.375f);
		this.noClip = false;
		this.setNoGravity(false);
		this.stepHeight = 1;
		hitsRemaining = 5;
	}

	public void setHorizontalVelocity(double velX, double velZ){
		this.velX = velX;
		this.velZ = velZ;
	}

	@Override
	public void onUpdate(){

		if(world.isRemote && !soundStarted && onGround){
			soundStarted = true;
			Wizardry.proxy.playMovingSound(this, WizardrySounds.ENTITY_BOULDER_ROLL, WizardrySounds.SPELLS, 1, 1, true);
		}

		super.onUpdate();

		this.motionY -= 0.03999999910593033D; // Gravity

		this.move(MoverType.SELF, velX, motionY, velZ);

		// Entity damage
		List<EntityLivingBase> collided = world.getEntitiesWithinAABB(EntityLivingBase.class, this.getEntityBoundingBox());

		float damage = Spells.boulder.getProperty(Spell.DAMAGE).floatValue() * damageMultiplier;
		float knockback = Spells.boulder.getProperty(Boulder.KNOCKBACK_STRENGTH).floatValue();

		for(EntityLivingBase entity : collided){

			if(!isValidTarget(entity)) break;

			boolean crushBonus = entity.posY < this.posY
					&& entity.getEntityBoundingBox().minX > this.getEntityBoundingBox().minX
					&& entity.getEntityBoundingBox().maxX < this.getEntityBoundingBox().maxX
					&& entity.getEntityBoundingBox().minZ > this.getEntityBoundingBox().minZ
					&& entity.getEntityBoundingBox().maxZ < this.getEntityBoundingBox().maxZ;

			if(EntityUtils.attackEntityWithoutKnockback(entity, MagicDamage.causeIndirectMagicDamage(this,
					getCaster(), DamageType.MAGIC), crushBonus ? damage * 1.5f : damage) && !crushBonus){
				// Only knock back if not crushing
				EntityUtils.applyStandardKnockback(this, entity, knockback);
				entity.motionX += this.motionX;
				entity.motionZ += this.motionZ;
			}
			entity.playSound(WizardrySounds.ENTITY_BOULDER_HIT, 1, 1);
		}

		// Wall smashing
		if(EntityUtils.canDamageBlocks(getCaster(), world) && collidedHorizontally){
			AxisAlignedBB box = getEntityBoundingBox().offset(velX, 0, velZ);
			List<BlockPos> cuboid = Lists.newArrayList(BlockPos.getAllInBox(MathHelper.floor(box.minX), MathHelper.floor(box.minY),
					MathHelper.floor(box.minZ), MathHelper.floor(box.maxX), MathHelper.floor(box.maxY), MathHelper.floor(box.maxZ)));
			smashBlocks(cuboid, true);
		}

		// Trailing particles
		for(int i = 0; i < 10; i++){

			double particleX = this.posX + width * 0.7 * (rand.nextDouble() - 0.5);
			double particleZ = this.posZ + width * 0.7 * (rand.nextDouble() - 0.5);

			IBlockState block = world.getBlockState(new BlockPos(this).down());

			if(block.getBlock() != Blocks.AIR){
				world.spawnParticle(EnumParticleTypes.BLOCK_DUST, particleX, this.posY, particleZ,
						0, 0.2, 0, Block.getStateId(block));
			}
		}
	}

	/**
	 * Attempts to smash the given list of blocks. If the boulder has exhausted its hits, or if any of the blocks are
	 * too hard to be smashed and breakIfTooHard is true, the boulder is destroyed.
	 * @param blocks A list of block positions at which any solid blocks that are weak enough will be destroyed
	 * @param breakIfTooHard True if the boulder should break if any of the blocks are too hard, false if it should stop
	 *                       on those blocks
	 * @return True if something was destroyed (either the boulder, one or more blocks, or both), false otherwise
	 */
	private boolean smashBlocks(List<BlockPos> blocks, boolean breakIfTooHard){

		if(blocks.removeIf(p -> world.getBlockState(p).getBlock().getExplosionResistance(world, p, this, null) > 3
				|| (!world.isRemote && !BlockUtils.canBreakBlock(getCaster(), world, p)))){
			// If any of the blocks were not breakable, the boulder is smashed
			if(breakIfTooHard){
				this.despawn();
			}else{
				return false;
			}

		}else{

			if(!world.isRemote){
				blocks.forEach(p -> world.destroyBlock(p, false));
				if(--hitsRemaining <= 0) this.despawn();
			}else{
				world.playSound(posX, posY, posZ, WizardrySounds.ENTITY_BOULDER_BREAK_BLOCK, SoundCategory.BLOCKS, 1, 1, false);
			}
		}

		shakeNearbyPlayers();
		return true;
	}

	private void shakeNearbyPlayers(){
		EntityUtils.getEntitiesWithinRadius(10, posX, posY, posZ, world, EntityPlayer.class)
				.forEach(p -> Wizardry.proxy.shakeScreen(p, 8));
	}

	@Override
	public void despawn(){

		if(world.isRemote){

			for(int i = 0; i < 200; i++){
				double x = posX + (rand.nextDouble() - 0.5) * width;
				double y = posY + rand.nextDouble() * height;
				double z = posZ + (rand.nextDouble() - 0.5) * width;
				world.spawnParticle(EnumParticleTypes.BLOCK_DUST, x, y, z, (x - posX) * 0.1,
						(y - posY + height / 2) * 0.1, (z - posZ) * 0.1, Block.getStateId(Blocks.DIRT.getDefaultState()));
			}

			world.playSound(posX, posY, posZ, WizardrySounds.ENTITY_BOULDER_BREAK_BLOCK, SoundCategory.BLOCKS, 1, 1, false);
		}

		super.despawn();
	}

	@Override
	public void move(MoverType type, double x, double y, double z){
		super.move(type, x, y, z);
		this.rotationPitch += Math.toDegrees(MathHelper.sqrt(x*x + y*y + z*z) / (width/2)); // That's how we roll
	}

	@Override
	public void fall(float distance, float damageMultiplier){

		super.fall(distance, damageMultiplier);

		// Floor smashing
		if(EntityUtils.canDamageBlocks(getCaster(), world) && distance > 3){
			AxisAlignedBB box = getEntityBoundingBox().offset(velX, motionY, velZ);
			List<BlockPos> cuboid = Lists.newArrayList(BlockPos.getAllInBox(MathHelper.floor(box.minX), MathHelper.floor(box.minY),
					MathHelper.floor(box.minZ), MathHelper.floor(box.maxX), MathHelper.floor(box.maxY), MathHelper.floor(box.maxZ)));
			if(smashBlocks(cuboid, distance > 8)) return;
			hitsRemaining--;
		}

		if(world.isRemote){

			// Landing particles
			for(int i = 0; i < 40; i++){

				double particleX = this.posX - 1.5 + 3 * rand.nextDouble();
				double particleZ = this.posZ - 1.5 + 3 * rand.nextDouble();
				// Roundabout way of getting a block instance for the block the boulder is standing on (if any).
				IBlockState block = world.getBlockState(new BlockPos(this.posX, this.posY - 2, this.posZ));

				if(block.getBlock() != Blocks.AIR){
					world.spawnParticle(EnumParticleTypes.BLOCK_DUST, particleX, this.posY, particleZ,
							particleX - this.posX, 0, particleZ - this.posZ, Block.getStateId(block));
				}
			}

			// Other landing effects
			if(distance > 1.2){
				world.playSound(posX, posY, posZ, WizardrySounds.ENTITY_BOULDER_LAND, SoundCategory.BLOCKS, Math.min(2, distance / 4), 1, false);
				shakeNearbyPlayers();
//				EntityUtils.getEntitiesWithinRadius(Math.min(12, distance * 2), posX, posY, posZ, world, EntityPlayer.class)
//						.forEach(p -> Wizardry.proxy.shakeScreen(p, Math.min(12, distance * 2)));
			}
		}
	}

	@Override
	public boolean canBeCollidedWith(){
		return true;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(){
		return this.getEntityBoundingBox();
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt){
		super.readEntityFromNBT(nbt);
		velX = nbt.getDouble("velX");
		velZ = nbt.getDouble("velZ");
		hitsRemaining = nbt.getInteger("hitsRemaining");
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt){
		super.writeEntityToNBT(nbt);
		nbt.setDouble("velX", velX);
		nbt.setDouble("velZ", velZ);
		nbt.setInteger("hitsRemaining", hitsRemaining);
	}

	@Override
	public void writeSpawnData(ByteBuf data){
		super.writeSpawnData(data);
		data.writeDouble(velX);
		data.writeDouble(velZ);
	}

	@Override
	public void readSpawnData(ByteBuf data){
		super.readSpawnData(data);
		this.velX = data.readDouble();
		this.velZ = data.readDouble();
	}

}
