package electroblob.wizardry.entity;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityMeteor extends EntityFallingBlock {

	/**
	 * The entity blast multiplier. Only some projectiles cause a blast, which is why this isn't in
	 * EntityMagicProjectile.
	 */
	public float blastMultiplier;

	public EntityMeteor(World world){
		super(world);
		// Superconstructor doesn't call this.
		this.setSize(0.98F, 0.98F);
	}

	public EntityMeteor(World world, double x, double y, double z, float blastMultiplier){
		super(world, x, y, z, WizardryBlocks.meteor.getDefaultState());
		this.motionY = -1.0D;
		this.setFire(200);
		this.blastMultiplier = blastMultiplier;
	}

	@Override
	public double getYOffset(){
		return this.height / 2.0F;
	}

	@Override
	public void onUpdate(){

		if(this.ticksExisted % 16 == 1 && world.isRemote){
			Wizardry.proxy.playMovingSound(this, WizardrySounds.SPELL_LOOP_FIRE, 3.0f, 1.0f, false);
		}

		// You'd think the best way to do this would be to call super and do all the exploding stuff in fall() instead.
		// However, for some reason, fallTile is null on the client side, causing an NPE in super.onUpdate()

		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		++this.fallTime;
		this.motionY -= 0.1d; // 0.03999999910593033D;
		this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
		this.motionX *= 0.9800000190734863D;
		this.motionY *= 0.9800000190734863D;
		this.motionZ *= 0.9800000190734863D;

		if(!this.world.isRemote){

			if(this.onGround){

				this.motionX *= 0.699999988079071D;
				this.motionZ *= 0.699999988079071D;
				this.motionY *= -0.5D;
				this.world.createExplosion(this, this.posX, this.posY, this.posZ, 2.0f * blastMultiplier, true);
				for(int i1 = -3; i1 < 4; i1++){
					for(int j1 = -3; j1 < 4; j1++){
						int y = WizardryUtilities.getNearestFloorLevelB(this.world,
								new BlockPos(this.posX + i1, this.posY, this.posZ + j1), 7);
						// System.out.println(y);
						double dist = this.getDistance((int)this.posX + i1, y, (int)this.posZ + j1);
						// Randomised with weighting so that the nearer the block the more likely it is to be set on
						// fire.
						if(y != -1 && rand.nextInt((int)dist * 2 + 1) < 3 && dist < 4){
							this.world.setBlockState(new BlockPos(this.posX + i1, y, this.posZ + j1),
									Blocks.FIRE.getDefaultState());
						}
					}
				}
				this.setDead();
			}
		}
	}

	@Override
	public void fall(float distance, float damageMultiplier){
		// Don't need to do anything here, the meteor should have already exploded.
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean canRenderOnFire(){
		return true;
	}

	@Override
	public IBlockState getBlock(){
		return WizardryBlocks.meteor.getDefaultState(); // For some reason the superclass version returns null on the
														// client
	}

	@SideOnly(Side.CLIENT)
	@Override
	public int getBrightnessForRender(){
		return 15728880;
	}

	@Override
	public float getBrightness(){
		return 1.0F;
	}

	@Override
	public boolean isInRangeToRenderDist(double distance){
		return true;
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbttagcompound){
		super.readEntityFromNBT(nbttagcompound);
		blastMultiplier = nbttagcompound.getFloat("blastMultiplier");
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbttagcompound){
		super.writeEntityToNBT(nbttagcompound);
		nbttagcompound.setFloat("blastMultiplier", blastMultiplier);
	}

}
