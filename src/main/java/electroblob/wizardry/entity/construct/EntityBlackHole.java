package electroblob.wizardry.entity.construct;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.entity.EntityLevitatingBlock;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.BlockUtils;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class EntityBlackHole extends EntityMagicConstruct {

	private static final double SUCTION_STRENGTH = 0.075;
	/** The maximum number of blocks that can be unhooked each tick, reduces lag from excessive numbers of entities. */
	private static final int BLOCK_UNHOOK_LIMIT = 3;

	public int[] randomiser;
	public int[] randomiser2;

	public EntityBlackHole(World world){
		super(world);
		this.width = 6.0f;
		this.height = 3.0f;
		randomiser = new int[30];
		for(int i = 0; i < randomiser.length; i++){
			randomiser[i] = this.rand.nextInt(10);
		}
		randomiser2 = new int[30];
		for(int i = 0; i < randomiser2.length; i++){
			randomiser2[i] = this.rand.nextInt(10);
		}
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbttagcompound){
		super.readEntityFromNBT(nbttagcompound);
		randomiser = nbttagcompound.getIntArray("randomiser");
		randomiser2 = nbttagcompound.getIntArray("randomiser2");
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbttagcompound){
		super.writeEntityToNBT(nbttagcompound);
		nbttagcompound.setIntArray("randomiser", randomiser);
		nbttagcompound.setIntArray("randomiser2", randomiser2);
	}

	public void onUpdate(){

		super.onUpdate();

		// System.out.println("Client side: " + this.world.isRemote + ", Caster: " + this.caster);

		// Particle effect. Finishes 40 ticks before the end so the particles disappear at the same time.
		if(this.ticksExisted + 40 < this.lifetime){
			for(int i = 0; i < 5; i++){
				// this.world.spawnParticle(EnumParticleTypes.PORTAL, this.posX + (this.rand.nextDouble() - 0.5D) *
				// (double)this.width, this.posY + this.rand.nextDouble() * (double)this.height - 0.75D, this.posZ +
				// (this.rand.nextDouble() - 0.5D) * (double)this.width, (this.rand.nextDouble() - 0.5D) * 2.0D,
				// -this.rand.nextDouble(), (this.rand.nextDouble() - 0.5D) * 2.0D);
				this.world.spawnParticle(EnumParticleTypes.PORTAL, this.posX, this.posY, this.posZ,
						(this.rand.nextDouble() - 0.5D) * 4.0D, (this.rand.nextDouble() - 0.5D) * 4.0D - 1,
						(this.rand.nextDouble() - 0.5D) * 4.0D);
			}
		}

		if(this.lifetime - this.ticksExisted == 75){
			this.playSound(WizardrySounds.ENTITY_BLACK_HOLE_VANISH, 1.5f, 1.0f);
		}else if(this.ticksExisted % 80 == 1 && this.ticksExisted + 80 < this.lifetime){
			this.playSound(WizardrySounds.ENTITY_BLACK_HOLE_AMBIENT, 1.5f, 1.0f);
		}

		if(!this.world.isRemote){

			double radius = 6; // TODO: Support for spell properties and modifiers

			boolean suckInBlocks = getCaster() instanceof EntityPlayer && EntityUtils.canDamageBlocks(getCaster(), world)
					&& ItemArtefact.isArtefactActive((EntityPlayer)getCaster(), WizardryItems.charm_black_hole);

			if(suckInBlocks){

				List<BlockPos> sphere = BlockUtils.getBlockSphere(new BlockPos(this), radius);

				int blocksUnhooked = 0;

				for(BlockPos pos : sphere){

					if(rand.nextInt(Math.max(1, (int)this.getDistanceSq(pos) * 3)) == 0){

						if(!BlockUtils.isBlockUnbreakable(world, pos) && !world.isAirBlock(pos)
								&& world.isBlockNormalCube(pos, false)){
							// Checks that the block above is not solid, since this causes the falling block to vanish.
//							&& !world.isBlockNormalCube(pos.up(), false)){

							EntityFallingBlock fallingBlock = new EntityLevitatingBlock(world, pos.getX() + 0.5,
									pos.getY() + 0.5, pos.getZ() + 0.5, world.getBlockState(pos));
//							fallingBlock.noClip = true;
							fallingBlock.fallTime = 1; // Prevent it from trying to delete the block itself
							world.spawnEntity(fallingBlock);
							world.setBlockToAir(pos);

							if(++blocksUnhooked >= BLOCK_UNHOOK_LIMIT) break; // Lag prevention
						}
					}
				}

			}

			List<Entity> targets = EntityUtils.getEntitiesWithinRadius(radius, this.posX, this.posY,
					this.posZ, this.world, Entity.class);

			targets.removeIf(t -> !(t instanceof EntityLivingBase || (suckInBlocks && t instanceof EntityFallingBlock)));

			for(Entity target : targets){

				if(this.isValidTarget(target)){

					// If the target can't be moved, it isn't sucked in but is still damaged if it gets too close
					if(!(target instanceof EntityPlayer && ((getCaster() instanceof EntityPlayer && !Wizardry.settings.playersMoveEachOther)
							|| ItemArtefact.isArtefactActive((EntityPlayer)target, WizardryItems.amulet_anchoring)))){

						EntityUtils.undoGravity(target);
						if(target instanceof EntityLevitatingBlock) ((EntityLevitatingBlock)target).suspend();

						// Sucks the target in
						if(this.posX > target.posX && target.motionX < 1){
							target.motionX += SUCTION_STRENGTH;
						}else if(this.posX < target.posX && target.motionX > -1){
							target.motionX -= SUCTION_STRENGTH;
						}

						if(this.posY > target.posY && target.motionY < 1){
							target.motionY += SUCTION_STRENGTH;
						}else if(this.posY < target.posY && target.motionY > -1){
							target.motionY -= SUCTION_STRENGTH;
						}

						if(this.posZ > target.posZ && target.motionZ < 1){
							target.motionZ += SUCTION_STRENGTH;
						}else if(this.posZ < target.posZ && target.motionZ > -1){
							target.motionZ -= SUCTION_STRENGTH;
						}

						// Player motion is handled on that player's client so needs packets
						if(target instanceof EntityPlayerMP){
							((EntityPlayerMP)target).connection.sendPacket(new SPacketEntityVelocity(target));
						}
					}

					if(this.getDistance(target) <= 2){
						// Damages the target if it is close enough, or destroys it if it's a block
						if(target instanceof EntityFallingBlock){
							target.playSound(WizardrySounds.ENTITY_BLACK_HOLE_BREAK_BLOCK, 0.5f,
									(rand.nextFloat() - rand.nextFloat()) * 0.2f + 1);
							IBlockState state = ((EntityFallingBlock)target).getBlock();
							if(state != null) world.playEvent(2001, new BlockPos(target), Block.getStateId(state));
							target.setDead();

						}else{
							if(this.getCaster() != null){
								target.attackEntityFrom(
										MagicDamage.causeIndirectMagicDamage(this, getCaster(), DamageType.MAGIC),
										2 * damageMultiplier);
							}else{
								target.attackEntityFrom(DamageSource.MAGIC, 2 * damageMultiplier);
							}
						}
					}
				}
			}
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean isInRangeToRenderDist(double distance){
		return true;
	}

	@Override
	public boolean shouldRenderInPass(int pass){
		return pass == 1;
	}

}
