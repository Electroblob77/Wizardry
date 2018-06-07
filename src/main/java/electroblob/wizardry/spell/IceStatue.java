package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.tileentity.TileEntityStatue;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class IceStatue extends Spell {

	private static final int baseDuration = 400;

	public IceStatue() {
		super(EnumTier.APPRENTICE, 15, EnumElement.ICE, "ice_statue", EnumSpellType.ATTACK, 40, EnumAction.none, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {

		Vec3 look = caster.getLookVec();

		MovingObjectPosition rayTrace = WizardryUtilities.standardEntityRayTrace(world, caster, 10*rangeMultiplier);

		if(rayTrace != null && rayTrace.typeOfHit == MovingObjectType.ENTITY && rayTrace.entityHit instanceof EntityLiving && !world.isRemote){

			EntityLiving target = (EntityLiving) rayTrace.entityHit;

			int x = (int)Math.floor(target.posX);
			int y = (int)Math.floor(target.posY);
			int z = (int)Math.floor(target.posZ);

			if(target.isBurning()){
				target.extinguish();
			}
			
			// Stops the entity looking red while frozen and the resulting z-fighting
			target.hurtTime = 0;

			if(target instanceof EntityBlaze) caster.addStat(Wizardry.freezeBlaze, 1);

			// Short mobs such as spiders and pigs
			if((target.height < 1.2 || target.isChild()) && WizardryUtilities.canBlockBeReplaced(world, x, y, z)){
				world.setBlock(x, y, z, Wizardry.iceStatue);
				if(world.getTileEntity(x, y, z) instanceof TileEntityStatue){
					((TileEntityStatue)world.getTileEntity(x, y, z)).setCreatureAndPart(target, 1, 1);
					((TileEntityStatue)world.getTileEntity(x, y, z)).setLifetime((int)(baseDuration*durationMultiplier));
				}
				target.setDead();
				world.playSoundAtEntity(target, "wizardry:freeze", 1.0F, world.rand.nextFloat() * 0.4F + 0.8F);
			}
			// Normal sized mobs like zombies and skeletons
			else if(target.height < 2.5 && WizardryUtilities.canBlockBeReplaced(world, x, y, z) && WizardryUtilities.canBlockBeReplaced(world, x, y+1, z)){
				world.setBlock(x, y, z, Wizardry.iceStatue);
				if(world.getTileEntity(x, y, z) instanceof TileEntityStatue){
					((TileEntityStatue)world.getTileEntity(x, y, z)).setCreatureAndPart(target, 1, 2);
					((TileEntityStatue)world.getTileEntity(x, y, z)).setLifetime((int)(baseDuration*durationMultiplier));
				}

				world.setBlock(x, y+1, z, Wizardry.iceStatue);
				if(world.getTileEntity(x, y+1, z) instanceof TileEntityStatue){
					((TileEntityStatue)world.getTileEntity(x, y+1, z)).setCreatureAndPart(target, 2, 2);
				}
				target.setDead();
				world.playSoundAtEntity(target, "wizardry:freeze", 1.0F, world.rand.nextFloat() * 0.4F + 0.8F);
			}
			// Tall mobs like endermen and iron golems
			else if(WizardryUtilities.canBlockBeReplaced(world, x, y, z) && WizardryUtilities.canBlockBeReplaced(world, x, y+1, z) && WizardryUtilities.canBlockBeReplaced(world, x, y+2, z)){
				world.setBlock(x, y, z, Wizardry.iceStatue);
				if(world.getTileEntity(x, y, z) instanceof TileEntityStatue){
					((TileEntityStatue)world.getTileEntity(x, y, z)).setCreatureAndPart(target, 1, 3);
					((TileEntityStatue)world.getTileEntity(x, y, z)).setLifetime((int)(baseDuration*durationMultiplier));
				}

				world.setBlock(x, y+1, z, Wizardry.iceStatue);
				if(world.getTileEntity(x, y+1, z) instanceof TileEntityStatue){
					((TileEntityStatue)world.getTileEntity(x, y+1, z)).setCreatureAndPart(target, 2, 3);
				}

				world.setBlock(x, y+2, z, Wizardry.iceStatue);
				if(world.getTileEntity(x, y+2, z) instanceof TileEntityStatue){
					((TileEntityStatue)world.getTileEntity(x, y+2, z)).setCreatureAndPart(target, 3, 3);
				}
				target.setDead();
				world.playSoundAtEntity(target, "wizardry:freeze", 1.0F, world.rand.nextFloat() * 0.4F + 0.8F);
			}
		}
		if(world.isRemote){
			for(int i=1; i<(int)(25*rangeMultiplier); i+=2){
				float brightness = 0.5f + (world.rand.nextFloat()/2);

				double x1 = caster.posX + look.xCoord*i/2 + world.rand.nextFloat()/5 - 0.1f;
				double y1 = WizardryUtilities.getPlayerEyesPos(caster) - 0.4f + look.yCoord*i/2 + world.rand.nextFloat()/5 - 0.1f;
				double z1 = caster.posZ + look.zCoord*i/2 + world.rand.nextFloat()/5 - 0.1f;

				Wizardry.proxy.spawnParticle(EnumParticleType.SPARKLE, world, x1, y1, z1, 0.0d, 0.0d, 0.0d, 12 + world.rand.nextInt(8), brightness, brightness + 0.1f, 1.0f);
				Wizardry.proxy.spawnParticle(EnumParticleType.SNOW, world, x1, y1, z1, 0.0d, -0.02d, 0.0d, 20 + world.rand.nextInt(10));

			}
		}
		caster.swingItem();
		world.playSoundAtEntity(caster, "wizardry:ice", 1.0F, world.rand.nextFloat() * 0.4F + 1.2F);
		return true;
	}


}
