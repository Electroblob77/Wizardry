package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.MagicDamage;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.MagicDamage.DamageType;
import electroblob.wizardry.entity.living.EntityIceGiant;
import electroblob.wizardry.entity.living.EntityIceWraith;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySnowman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumAction;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;

public class Ignite extends Spell {

	public Ignite() {
		super(EnumTier.BASIC, 5, EnumElement.FIRE, "ignite", EnumSpellType.ATTACK, 10, EnumAction.none, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		// Entity ray trace is done first because block ray trace passes through entities; if it was the other
		// way round, entities would only be hit when there were no blocks in range behind them.
		MovingObjectPosition rayTrace = WizardryUtilities.standardEntityRayTrace(world, caster, 10*rangeMultiplier);
		
		if(rayTrace != null && rayTrace.typeOfHit == MovingObjectType.ENTITY && rayTrace.entityHit instanceof EntityLivingBase){
			
			EntityLivingBase target = (EntityLivingBase) rayTrace.entityHit;
			
			if(MagicDamage.isEntityImmune(DamageType.FIRE, target)){
				if(!world.isRemote) caster.addChatComponentMessage(new ChatComponentTranslation("spell.resist", target.getCommandSenderName(), this.getDisplayNameWithFormatting()));
			}else{
				target.setFire((int)(10*durationMultiplier));
			}
			
			if(world.isRemote){
				double dx = target.posX - caster.posX;
				double dy = (WizardryUtilities.getEntityFeetPos(target) + target.height/2) - WizardryUtilities.getPlayerEyesPos(caster);
				double dz = target.posZ - caster.posZ;
				// i starts at 1 so that particles are not spawned in the player's head.
				for(int i=1;i<5;i++){
					//WizardryUtilities.spawnParticleAndNotify(world, "flame", caster.posX + (i*(dx/5)) + world.rand.nextFloat()/5, caster.posY + (i*(dy/5)) + world.rand.nextFloat()/5, caster.posZ + (i*(dz/5)) + world.rand.nextFloat()/5, 0, 0, 0, 0, 0, 0, 0);
					//WizardryUtilities.spawnParticleAndNotify(world, "flame", caster.posX + (i*(dx/5)) + world.rand.nextFloat()/5, caster.posY + (i*(dy/5)) + world.rand.nextFloat()/5, caster.posZ + (i*(dz/5)) + world.rand.nextFloat()/5, 0, 0, 0, 0, 0, 0, 0);
					world.spawnParticle("flame", caster.posX + (i*(dx/5)) + world.rand.nextFloat()/5, WizardryUtilities.getPlayerEyesPos(caster) + (i*(dy/5)) + world.rand.nextFloat()/5, caster.posZ + (i*(dz/5)) + world.rand.nextFloat()/5, 0, 0, 0);
					world.spawnParticle("flame", caster.posX + (i*(dx/5)) + world.rand.nextFloat()/5, WizardryUtilities.getPlayerEyesPos(caster) + (i*(dy/5)) + world.rand.nextFloat()/5, caster.posZ + (i*(dz/5)) + world.rand.nextFloat()/5, 0, 0, 0);
				}
			}
			
			caster.swingItem();
			world.playSoundEffect((double)caster.posX + 0.5D, (double)caster.posY + 1.5D, (double)caster.posZ + 0.5D, "fire.ignite", 1.0F, world.rand.nextFloat() * 0.4F + 0.8F);
			
			return true;
			
		}else{
			
			rayTrace = WizardryUtilities.rayTrace(10*rangeMultiplier, world, caster, false);
			
			// Gets block the player is looking at and sets the appropriate surrounding air block to fire.
			// Note how the block is set on the server side only (kinda obvious really) but the particles are
			// spawned on the client side only.
			if(rayTrace != null && rayTrace.typeOfHit == MovingObjectType.BLOCK){
				
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
				if(flag){
					double dx = blockHitX + 0.5 - caster.posX;
					double dy = blockHitY + 1.5 - (WizardryUtilities.getPlayerEyesPos(caster) + 1);
					double dz = blockHitZ + 0.5 - caster.posZ;
					if(world.isRemote){
						for(int i=1;i<5;i++){
							world.spawnParticle("flame", caster.posX + (i*(dx/5)) + world.rand.nextFloat()/5, WizardryUtilities.getPlayerEyesPos(caster) + (i*(dy/5)) + world.rand.nextFloat()/5, caster.posZ + (i*(dz/5)) + world.rand.nextFloat()/5, 0, 0, 0);
							world.spawnParticle("flame", caster.posX + (i*(dx/5)) + world.rand.nextFloat()/5, WizardryUtilities.getPlayerEyesPos(caster) + (i*(dy/5)) + world.rand.nextFloat()/5, caster.posZ + (i*(dz/5)) + world.rand.nextFloat()/5, 0, 0, 0);
						}
					}
					caster.swingItem();
					world.playSoundEffect((double)caster.posX + 0.5D, (double)caster.posY + 1.5D, (double)caster.posZ + 0.5D, "fire.ignite", 1.0F, world.rand.nextFloat() * 0.4F + 0.8F);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EntityLivingBase target, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier){
		
		if(target != null){
		
			if(!MagicDamage.isEntityImmune(DamageType.FIRE, target)) target.setFire((int)(10*durationMultiplier));
			
			if(world.isRemote){
				double dx = target.posX - caster.posX;
				double dy = (WizardryUtilities.getEntityFeetPos(target) + target.height/2) - (caster.posY + caster.getEyeHeight());
				double dz = target.posZ - caster.posZ;
				// i starts at 1 so that particles are not spawned in the player's head.
				for(int i=1;i<5;i++){
					//WizardryUtilities.spawnParticleAndNotify(world, "flame", caster.posX + (i*(dx/5)) + world.rand.nextFloat()/5, caster.posY + (i*(dy/5)) + world.rand.nextFloat()/5, caster.posZ + (i*(dz/5)) + world.rand.nextFloat()/5, 0, 0, 0, 0, 0, 0, 0);
					//WizardryUtilities.spawnParticleAndNotify(world, "flame", caster.posX + (i*(dx/5)) + world.rand.nextFloat()/5, caster.posY + (i*(dy/5)) + world.rand.nextFloat()/5, caster.posZ + (i*(dz/5)) + world.rand.nextFloat()/5, 0, 0, 0, 0, 0, 0, 0);
					world.spawnParticle("flame", caster.posX + (i*(dx/5)) + world.rand.nextFloat()/5, caster.posY + caster.getEyeHeight() + (i*(dy/5)) + world.rand.nextFloat()/5, caster.posZ + (i*(dz/5)) + world.rand.nextFloat()/5, 0, 0, 0);
					world.spawnParticle("flame", caster.posX + (i*(dx/5)) + world.rand.nextFloat()/5, caster.posY + caster.getEyeHeight() + (i*(dy/5)) + world.rand.nextFloat()/5, caster.posZ + (i*(dz/5)) + world.rand.nextFloat()/5, 0, 0, 0);
				}
			}
			
			caster.swingItem();
			world.playSoundEffect((double)caster.posX + 0.5D, (double)caster.posY + 1.5D, (double)caster.posZ + 0.5D, "fire.ignite", 1.0F, world.rand.nextFloat() * 0.4F + 0.8F);
			
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

}
