package electroblob.wizardry.entity.construct;

import java.util.List;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.MagicDamage;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.MagicDamage.DamageType;
import electroblob.wizardry.entity.EntityFallingGrass;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;

public class EntityEarthquake extends EntityMagicConstruct {

	public EntityEarthquake(World world){
		super(world);

		this.height = 1.0f;
		this.width = 1.0f;
	}

	public EntityEarthquake(World world, double x, double y, double z, EntityLivingBase caster, int lifetime,
			float damageMultiplier) {
		super(world, x, y, z, caster, lifetime, damageMultiplier);

		this.height = 1.0f;
		this.width = 1.0f;
	}

	public void onUpdate(){

		super.onUpdate();

		if(!worldObj.isRemote){

			double speed = 0.4;

			// The further the earthquake is going to spread, the finer the angle increments.
			for(double angle=0; angle < 2*Math.PI; angle+=Math.PI/(lifetime*1.5)){

				// Calculates coordinates for the block to be moved. The radius increases with time. The +1.5 is to leave
				// blocks in the centre untouched.
				int x = this.posX < 0 ? (int)(this.posX + ((this.ticksExisted*speed)+1.5)*Math.sin(angle) - 1) : (int)(this.posX + ((this.ticksExisted*speed)+1.5)*Math.sin(angle));
				int y = (int)(this.posY - 0.5);
				int z = this.posZ < 0 ? (int)(this.posZ + ((this.ticksExisted*speed)+1.5)*Math.cos(angle) - 1) : (int)(this.posZ + ((this.ticksExisted*speed)+1.5)*Math.cos(angle));


				if(!WizardryUtilities.isBlockUnbreakable(worldObj, x, y, z) && !worldObj.isAirBlock(x, y, z) && worldObj.isBlockNormalCubeDefault(x, y, z, false)
						// Checks that the block above is not solid, since this causes the falling sand to vanish.
						&& !worldObj.isBlockNormalCubeDefault(x, y+1, z, false)){

					// Falling blocks do the setting block to air themselves.
					// EntityFallingGrass fixes the problem with the biome colour not being applied.
					EntityFallingBlock fallingblock = worldObj.getBlock(x, y, z) == Blocks.grass ?
							new EntityFallingGrass(worldObj, x+0.5, y+0.5, z+0.5, worldObj.getBlock(x, y, z), worldObj.getBlockMetadata(x, y, z))
							: new EntityFallingBlock(worldObj, x+0.5, y+0.5, z+0.5, worldObj.getBlock(x, y, z), worldObj.getBlockMetadata(x, y, z));
							fallingblock.motionY = 0.3;
							worldObj.spawnEntityInWorld(fallingblock);
				}
			}

			List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius((this.ticksExisted*speed)+1.5, this.posX, this.posY, this.posZ, worldObj);
			
			// In this particular instance, the caster is completely unaffected because they will always be in the centre.
			targets.remove(this.getCaster());

			for(EntityLivingBase target : targets){

				// Searches in a 1 wide ring.
				if(this.getDistanceToEntity(target) > (this.ticksExisted*speed)+0.5 && target.posY < this.posY + 1  && target.posY > this.posY - 1){

					// Knockback must be removed in this instance, or the target will fall into the floor.
					double motionX = target.motionX;
					double motionZ = target.motionZ;

					if(this.isValidTarget(target)){
						target.attackEntityFrom(MagicDamage.causeIndirectEntityMagicDamage(this, this.getCaster(), DamageType.BLAST), 10*this.damageMultiplier);
						target.addPotionEffect(new PotionEffect(Potion.weakness.id, 400, 1));
					}

					// All targets are thrown, even those immune to the damage, so they don't fall into the ground.
					target.motionX = motionX;
					target.motionY = 0.8; // Throws target into the air.
					target.motionZ = motionZ;
					
					// Player motion is handled on that player's client so needs packets
					if(target instanceof EntityPlayerMP){
						((EntityPlayerMP)target).playerNetServerHandler.sendPacket(new S12PacketEntityVelocity(target));
					}
				}
			}
		}
	}

}
