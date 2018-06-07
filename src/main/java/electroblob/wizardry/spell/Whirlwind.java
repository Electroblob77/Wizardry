package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.WizardryUtilities;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumAction;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class Whirlwind extends Spell {

	public Whirlwind() {
		super(EnumTier.APPRENTICE, 10, EnumElement.EARTH, "whirlwind", EnumSpellType.DEFENCE, 15, EnumAction.none, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {

		MovingObjectPosition rayTrace = WizardryUtilities.standardEntityRayTrace(world, caster, 10*rangeMultiplier);

		if(rayTrace != null && rayTrace.entityHit instanceof EntityLivingBase){
			EntityLivingBase target = (EntityLivingBase) rayTrace.entityHit;
			
			if(!world.isRemote){
				
				target.motionX = caster.getLookVec().xCoord * 2;
				target.motionY = caster.getLookVec().yCoord * 2 + 1;
				target.motionZ = caster.getLookVec().zCoord * 2;
				
				// Player motion is handled on that player's client so needs packets
				if(target instanceof EntityPlayerMP){
					((EntityPlayerMP)target).playerNetServerHandler.sendPacket(new S12PacketEntityVelocity(target));
				}
			}
			
			if(world.isRemote){
				for(int i=0; i<10; i++){
					double x2 = (double)(caster.posX + world.rand.nextFloat() - 0.5F + caster.getLookVec().xCoord*caster.getDistanceToEntity(target)*0.5);
					double y2 = (double)(WizardryUtilities.getPlayerEyesPos(caster) + world.rand.nextFloat() - 0.5F + caster.getLookVec().yCoord*caster.getDistanceToEntity(target)*0.5);
					double z2 = (double)(caster.posZ + world.rand.nextFloat() - 0.5F + caster.getLookVec().zCoord*caster.getDistanceToEntity(target)*0.5);
					world.spawnParticle("cloud", x2, y2, z2, caster.getLookVec().xCoord, caster.getLookVec().yCoord, caster.getLookVec().zCoord);
					//Minecraft.getMinecraft().effectRenderer.addEffect(new EntitySparkleFX(world, x2, y2, z2, entityplayer.getLookVec().xCoord, entityplayer.getLookVec().yCoord, entityplayer.getLookVec().zCoord, null, 1.0f, 1.0f, 0.8f, 10));
				}
			}
			caster.swingItem();
			world.playSoundAtEntity(caster, "wizardry:ice", 0.8F, world.rand.nextFloat() * 0.2F + 0.6F);
			return true;
		}
		return false;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EntityLivingBase target, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier){
		
		if(target != null){
		
			if(!world.isRemote){
				target.motionX = caster.getLookVec().xCoord * 2;
				target.motionY = caster.getLookVec().yCoord * 2 + 1;
				target.motionZ = caster.getLookVec().zCoord * 2;
				
				// Player motion is handled on that player's client so needs packets
				if(target instanceof EntityPlayerMP){
					((EntityPlayerMP)target).playerNetServerHandler.sendPacket(new S12PacketEntityVelocity(target));
				}
			}
			if(world.isRemote){
				for(int i=0; i<10; i++){
					double x2 = (double)(caster.posX + world.rand.nextFloat() - 0.5F + caster.getLookVec().xCoord*caster.getDistanceToEntity(target)*0.5);
					double y2 = (double)(caster.posY + caster.getEyeHeight() + world.rand.nextFloat() - 0.5F + caster.getLookVec().yCoord*caster.getDistanceToEntity(target)*0.5);
					double z2 = (double)(caster.posZ + world.rand.nextFloat() - 0.5F + caster.getLookVec().zCoord*caster.getDistanceToEntity(target)*0.5);
					world.spawnParticle("cloud", x2, y2, z2, caster.getLookVec().xCoord, caster.getLookVec().yCoord, caster.getLookVec().zCoord);
				}
			}
			caster.swingItem();
			world.playSoundAtEntity(caster, "wizardry:ice", 0.8F, world.rand.nextFloat() * 0.2F + 0.6F);
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

}
