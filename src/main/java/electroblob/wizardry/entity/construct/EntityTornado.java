package electroblob.wizardry.entity.construct;

import java.util.List;

import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.MagicDamage;
import electroblob.wizardry.MagicDamage.DamageType;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.BiomeDictionary;

public class EntityTornado extends EntityMagicConstruct {

	private double velX, velZ;

	public EntityTornado(World world) {
		super(world);
		this.height = 8.0f;
		this.width = 5.0f;
		this.isImmuneToFire = false;
	}

	public EntityTornado(World world, double x, double y, double z, EntityLivingBase caster, int lifetime, double velX, double velZ, float damageMultiplier) {
		super(world, x, y, z, caster, lifetime, damageMultiplier);
		this.height = 8.0f;
		this.width = 5.0f;
		this.velX = velX;
		this.velZ = velZ;
		this.isImmuneToFire = false;
	}

	public void onUpdate(){

		super.onUpdate();

		if(this.ticksExisted % 120 == 1 && worldObj.isRemote){
			// Repeat is false so that the sound fades out when the tornado does rather than stopping suddenly
			Wizardry.proxy.playMovingSound(this, "wizardry:wind", 1.0f, 1.0f, false);
		}
		
		this.moveEntity(velX, motionY, velZ);
		
		if(this.worldObj.getBlock((int)this.posX, WizardryUtilities.getNearestFloorLevelC(worldObj, (int)this.posX, (int)this.posY + 3, (int)this.posZ, 5) - 1, (int)this.posZ).getMaterial() == Material.lava){
			// Fire tornado!
			this.setFire(5);
		}

		if(!this.worldObj.isRemote){

			List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(4.0d, this.posX, this.posY, this.posZ, this.worldObj);

			for(EntityLivingBase target : targets){

				if(this.isValidTarget(target)){

					double velX = target.motionX;
					double velY = target.motionY;
					double velZ = target.motionZ;

					double dx = this.posX-target.posX > 0? 0.5 - (this.posX-target.posX)/8 : -0.5 - (this.posX-target.posX)/8;
					double dz = this.posZ-target.posZ > 0? 0.5 - (this.posZ-target.posZ)/8 : -0.5 - (this.posZ-target.posZ)/8;
					
					if(this.isBurning()){
						target.setFire(4);
					}
					
					if(this.getCaster() != null){
						target.attackEntityFrom(MagicDamage.causeIndirectEntityMagicDamage(this, getCaster(), DamageType.MAGIC), 1*damageMultiplier);
					}else{
						target.attackEntityFrom(DamageSource.magic, 1*damageMultiplier);
					}

					target.motionX = dx;
					target.motionY = velY+0.2;
					target.motionZ = dz;
					
					// Player motion is handled on that player's client so needs packets
					if(target instanceof EntityPlayerMP){
						((EntityPlayerMP)target).playerNetServerHandler.sendPacket(new S12PacketEntityVelocity(target));
					}

					if(target instanceof EntityPig && target.riddenByEntity instanceof EntityPlayer){
						((EntityPlayer)target.riddenByEntity).triggerAchievement(Wizardry.pigTornado);
					}
				}
			}
		}else{
			for(int i=1; i<10; i++){
				float brightness = rand.nextFloat()*0.7f;
				double yPos = rand.nextDouble()*8;
				
				int blockX = (int)this.posX - 2 + this.rand.nextInt(4);
				int blockZ = (int)this.posZ - 2 + this.rand.nextInt(4);
				int blockY = WizardryUtilities.getNearestFloorLevelC(worldObj, blockX, (int)this.posY + 3, blockZ, 5) - 1;
				
				Block block = this.worldObj.getBlock(blockX, blockY, blockZ);
				int metadata = this.worldObj.getBlockMetadata(blockX, blockY, blockZ);
				
				// If the block it found was air or something it can't pick up, it makes a best guess based on the biome.
				if(!canTornadoPickUpBitsOf(block)){
					block = worldObj.getBiomeGenForCoords(blockX, blockZ).topBlock;
					metadata = 0;
				}
				
				Wizardry.proxy.spawnTornadoParticle(worldObj, this.posX, this.posY + yPos, this.posZ, this.velX, this.velZ, yPos/3 + 0.5d, 100, block, metadata);
				Wizardry.proxy.spawnTornadoParticle(worldObj, this.posX, this.posY + yPos, this.posZ, this.velX, this.velZ, yPos/3 + 0.5d, 100, block, metadata);
				
				// Sometimes spawns leaf particles if the block is leaves
				if(block.getMaterial() == Material.leaves && this.rand.nextInt(3) == 0){
					double yPos1 = rand.nextDouble()*8;
					Wizardry.proxy.spawnParticle(EnumParticleType.LEAF, worldObj, this.posX + (rand.nextDouble()*2-1)*(yPos1/3 + 0.5d), this.posY + yPos1, this.posZ + (rand.nextDouble()*2-1)*(yPos1/3 + 0.5d), 0, -0.05, 0, 40 + rand.nextInt(10));
				}
				
				// Sometimes spawns snow particles if the block is snow
				if(block.getMaterial() == Material.snow || block.getMaterial() == Material.craftedSnow && this.rand.nextInt(3) == 0){
					double yPos1 = rand.nextDouble()*8;
					Wizardry.proxy.spawnParticle(EnumParticleType.SNOW, worldObj, this.posX + (rand.nextDouble()*2-1)*(yPos1/3 + 0.5d), this.posY + yPos1, this.posZ + (rand.nextDouble()*2-1)*(yPos1/3 + 0.5d), 0, -0.02, 0, 40 + rand.nextInt(10));
				}
			}
		}
	}
	
	private static boolean canTornadoPickUpBitsOf(Block block){
		Material material = block.getMaterial();
		return material == Material.craftedSnow
				|| material == Material.ground
				|| material == Material.grass
				|| material == Material.lava
				|| material == Material.sand
				|| material == Material.snow
				|| material == Material.water
				|| material == Material.plants
				|| material == Material.leaves
				|| material == Material.vine;
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
		super.readEntityFromNBT(nbttagcompound);
		velX = nbttagcompound.getDouble("velX");
		velZ = nbttagcompound.getDouble("velZ");
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
		super.writeEntityToNBT(nbttagcompound);
		nbttagcompound.setDouble("velX", velX);
		nbttagcompound.setDouble("velZ", velZ);
	}

	@Override
	public void writeSpawnData(ByteBuf data) {
		super.writeSpawnData(data);
		data.writeDouble(velX);
		data.writeDouble(velZ);
	}

	@Override
	public void readSpawnData(ByteBuf data) {
		super.readSpawnData(data);
		this.velX = data.readDouble();
		this.velZ = data.readDouble();
	}

}
