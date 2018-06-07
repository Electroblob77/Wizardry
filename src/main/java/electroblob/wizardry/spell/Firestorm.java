package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.MagicDamage;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.MagicDamage.DamageType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumAction;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class Firestorm extends Spell {

	public Firestorm() {
		super(EnumTier.MASTER, 15, EnumElement.FIRE, "firestorm", EnumSpellType.ATTACK, 0, EnumAction.none, true);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		Vec3 look = caster.getLookVec();
		
		MovingObjectPosition rayTrace = WizardryUtilities.standardEntityRayTrace(world, caster, 10*rangeMultiplier);
		
		if(rayTrace != null && rayTrace.typeOfHit == MovingObjectType.ENTITY && rayTrace.entityHit instanceof EntityLivingBase){
			
			EntityLivingBase target = (EntityLivingBase) rayTrace.entityHit;
			
			if(!MagicDamage.isEntityImmune(DamageType.FIRE, target)){
				
				target.setFire(10);
				
				// This motion stuff removes knockback, which is desirable for continuous spells.
				double motionX = target.motionX;
				double motionY = target.motionY;
				double motionZ = target.motionZ;
				
				target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.FIRE), 6.0f * damageMultiplier);
				
				target.motionX = motionX;
				target.motionY = motionY;
				target.motionZ = motionZ;
			}else{
				if(!world.isRemote && ticksInUse == 1) caster.addChatComponentMessage(new ChatComponentTranslation("spell.resist", target.getCommandSenderName(), this.getDisplayNameWithFormatting()));
			}
			
		}else if(rayTrace != null && rayTrace.typeOfHit == MovingObjectType.BLOCK){
			
			int blockHitX = rayTrace.blockX;
			int blockHitY = rayTrace.blockY;
			int blockHitZ = rayTrace.blockZ;
			int blockHitSide = rayTrace.sideHit;
			boolean flag = false;
			switch(blockHitSide){
			case 0:
				if(world.isAirBlock(blockHitX, blockHitY-1, blockHitZ)){
					if(!world.isRemote){
						world.setBlock(blockHitX, blockHitY-1, blockHitZ, Blocks.fire);
					}
					flag = true;
				}
				break;
			case 1:
				if(world.isAirBlock(blockHitX, blockHitY+1, blockHitZ)){
					if(!world.isRemote){
						world.setBlock(blockHitX, blockHitY+1, blockHitZ, Blocks.fire);
					}
					flag = true;
				}
				break;
			case 2:
				if(world.isAirBlock(blockHitX, blockHitY, blockHitZ-1)){
					if(!world.isRemote){
						world.setBlock(blockHitX, blockHitY, blockHitZ-1, Blocks.fire);
					}
					flag = true;
				}
				break;
			case 3:
				if(world.isAirBlock(blockHitX, blockHitY, blockHitZ+1)){
					if(!world.isRemote){
						world.setBlock(blockHitX, blockHitY, blockHitZ+1, Blocks.fire);
					}
					flag = true;
				}
				break;
			case 4:
				if(world.isAirBlock(blockHitX-1, blockHitY, blockHitZ)){
					if(!world.isRemote){
						world.setBlock(blockHitX-1, blockHitY, blockHitZ, Blocks.fire);
					}
					flag = true;
				}
				break;
			case 5:
				if(world.isAirBlock(blockHitX+1, blockHitY, blockHitZ)){
					if(!world.isRemote){
						world.setBlock(blockHitX+1, blockHitY, blockHitZ, Blocks.fire);
					}
					flag = true;
				}
				break;
			}
		}
		
		if(world.isRemote){
			for(int i=0; i<40; i++){
				// I figured it out! when on client side, entityplayer.posY is at the eyes, not the feet!
				double x1 = caster.posX + look.xCoord*i/2 + world.rand.nextFloat()*0.6f - 0.3f;
				double y1 = WizardryUtilities.getPlayerEyesPos(caster) - 0.4f + look.yCoord*i/2 + world.rand.nextFloat()*0.4f - 0.2f;
				double z1 = caster.posZ + look.zCoord*i/2 + world.rand.nextFloat()*0.6f - 0.3f;
				Wizardry.proxy.spawnParticle(EnumParticleType.MAGIC_FIRE, world, x1, y1, z1, look.xCoord*rangeMultiplier, look.yCoord*rangeMultiplier, look.zCoord*rangeMultiplier, 0, 3 + world.rand.nextFloat(), 0, 0);
				Wizardry.proxy.spawnParticle(EnumParticleType.MAGIC_FIRE, world, x1, y1, z1, look.xCoord*rangeMultiplier, look.yCoord*rangeMultiplier, look.zCoord*rangeMultiplier, 0, 3 + world.rand.nextFloat(), 0, 0);
			}
		}
		if(ticksInUse % 16 == 0){
			if(ticksInUse == 0) world.playAuxSFX(1009, (int)caster.posX, (int)caster.posY, (int)caster.posZ, 0);
			world.playSoundAtEntity(caster, "wizardry:flameray", 0.5F, 1.0f);
		}
		return true;
	}


}
