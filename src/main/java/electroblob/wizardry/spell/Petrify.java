package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.tileentity.TileEntityStatue;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class Petrify extends Spell {
	
	private static final int baseDuration = 900;

	public Petrify() {
		super(EnumTier.ADVANCED, 40, EnumElement.SORCERY, "petrify", EnumSpellType.ATTACK, 100, EnumAction.none, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {

		Vec3 look = caster.getLookVec();

		MovingObjectPosition rayTrace = WizardryUtilities.standardEntityRayTrace(world, caster, 10*rangeMultiplier);

		if(rayTrace != null && rayTrace.typeOfHit == MovingObjectType.ENTITY && rayTrace.entityHit instanceof EntityLiving && !world.isRemote){
			
			EntityLiving entity = (EntityLiving) rayTrace.entityHit;

			int x = (int)Math.floor(entity.posX);
			int y = (int)Math.floor(entity.posY);
			int z = (int)Math.floor(entity.posZ);

			entity.extinguish();
			
			//Short mobs such as spiders and pigs
			if((entity.height < 1.2 || entity.isChild()) && WizardryUtilities.canBlockBeReplaced(world, x, y, z)){
				world.setBlock(x, y, z, Wizardry.petrifiedStone);
				if(world.getTileEntity(x, y, z) instanceof TileEntityStatue){
					((TileEntityStatue)world.getTileEntity(x, y, z)).setCreatureAndPart(entity, 1, 1);
					((TileEntityStatue)world.getTileEntity(x, y, z)).setLifetime((int)(baseDuration*durationMultiplier));
				}
				entity.setDead();
			}
			//Normal sized mobs like zombies and skeletons
			else if(entity.height < 2.5 && WizardryUtilities.canBlockBeReplaced(world, x, y, z) && WizardryUtilities.canBlockBeReplaced(world, x, y+1, z)){
				world.setBlock(x, y, z, Wizardry.petrifiedStone);
				if(world.getTileEntity(x, y, z) instanceof TileEntityStatue){
					((TileEntityStatue)world.getTileEntity(x, y, z)).setCreatureAndPart(entity, 1, 2);
					((TileEntityStatue)world.getTileEntity(x, y, z)).setLifetime((int)(baseDuration*durationMultiplier));
				}

				world.setBlock(x, y+1, z, Wizardry.petrifiedStone);
				if(world.getTileEntity(x, y+1, z) instanceof TileEntityStatue){
					((TileEntityStatue)world.getTileEntity(x, y+1, z)).setCreatureAndPart(entity, 2, 2);
				}
				entity.setDead();
			}
			//Tall mobs like endermen
			else if(WizardryUtilities.canBlockBeReplaced(world, x, y, z) && WizardryUtilities.canBlockBeReplaced(world, x, y+1, z) && WizardryUtilities.canBlockBeReplaced(world, x, y+2, z)){
				world.setBlock(x, y, z, Wizardry.petrifiedStone);
				if(world.getTileEntity(x, y, z) instanceof TileEntityStatue){
					((TileEntityStatue)world.getTileEntity(x, y, z)).setCreatureAndPart(entity, 1, 3);
					((TileEntityStatue)world.getTileEntity(x, y, z)).setLifetime((int)(baseDuration*durationMultiplier));
				}

				world.setBlock(x, y+1, z, Wizardry.petrifiedStone);
				if(world.getTileEntity(x, y+1, z) instanceof TileEntityStatue){
					((TileEntityStatue)world.getTileEntity(x, y+1, z)).setCreatureAndPart(entity, 2, 3);
				}

				world.setBlock(x, y+2, z, Wizardry.petrifiedStone);
				if(world.getTileEntity(x, y+2, z) instanceof TileEntityStatue){
					((TileEntityStatue)world.getTileEntity(x, y+2, z)).setCreatureAndPart(entity, 3, 3);
				}
				entity.setDead();
			}
		}
		if(world.isRemote){
			for(int i=1; i<(int)(25*rangeMultiplier); i+=2){
				// I figured it out! when on client side, entityplayer.posY is at the eyes, not the feet!
				double x1 = caster.posX + look.xCoord*i/2 + world.rand.nextFloat()/5 - 0.1f;
				double y1 = WizardryUtilities.getPlayerEyesPos(caster) - 0.4f + look.yCoord*i/2 + world.rand.nextFloat()/5 - 0.1f;
				double z1 = caster.posZ + look.zCoord*i/2 + world.rand.nextFloat()/5 - 0.1f;
				//world.spawnParticle("mobSpell", x1, y1, z1, -1*look.xCoord, -1*look.yCoord, -1*look.zCoord);
				Wizardry.proxy.spawnParticle(EnumParticleType.DARK_MAGIC, world, x1, y1, z1, 0.0d, 0.0d, 0.0d, 0, 0.1f, 0.1f, 0.1f);
				Wizardry.proxy.spawnParticle(EnumParticleType.SPARKLE, world, x1, y1, z1, 0.0d, 0.0d, 0.0d, 12 + world.rand.nextInt(8), 0.2f, 0.2f, 0.2f);
			}
		}
		caster.swingItem();
		world.playSoundAtEntity(caster, "mob.wither.spawn", 1.0F, world.rand.nextFloat() * 0.2F + 1.0F);
		return true;
	}


}
