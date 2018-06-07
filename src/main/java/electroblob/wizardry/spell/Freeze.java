package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.MagicDamage;
import electroblob.wizardry.MagicDamage.DamageType;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.entity.living.EntityBlazeMinion;
import electroblob.wizardry.entity.living.EntityIceGiant;
import electroblob.wizardry.entity.living.EntityIceWraith;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntitySnowman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumAction;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class Freeze extends Spell {

	public Freeze() {
		super(EnumTier.BASIC, 5, EnumElement.ICE, "freeze", EnumSpellType.ATTACK, 10, EnumAction.none, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {

		//caster.addPotionEffect(new PotionEffect(Wizardry.frost.id, 200, 0));

		// Entity ray trace is done first because block ray trace passes through entities; if it was the other
		// way round, entities would only be hit when there were no blocks in range behind them.
		MovingObjectPosition rayTrace = WizardryUtilities.standardEntityRayTrace(world, caster, 10*rangeMultiplier);

		if(rayTrace != null && rayTrace.typeOfHit == MovingObjectType.ENTITY && rayTrace.entityHit instanceof EntityLivingBase){
			
			EntityLivingBase target = (EntityLivingBase) rayTrace.entityHit;
			
			if(target instanceof EntityBlaze || target instanceof EntityMagmaCube || target instanceof EntityBlazeMinion){
				target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.FROST), 3.0f * damageMultiplier);
			}
			
			if(MagicDamage.isEntityImmune(DamageType.FROST, target)){
				if(!world.isRemote) caster.addChatComponentMessage(new ChatComponentTranslation("spell.resist", target.getCommandSenderName(), this.getDisplayNameWithFormatting()));
			}else{
				target.addPotionEffect(new PotionEffect(Wizardry.frost.id, (int)(200*durationMultiplier), 1, true));
			}
			
			if(target.isBurning()){
				target.extinguish();
			}

			if(world.isRemote){
				double dx = target.posX - caster.posX;
				double dy = (WizardryUtilities.getEntityFeetPos(target) + target.height/2) - WizardryUtilities.getPlayerEyesPos(caster);
				double dz = target.posZ - caster.posZ;
				for(int i=1;i<5;i++){
					float brightness = 0.5f + (world.rand.nextFloat()/2);
					Wizardry.proxy.spawnParticle(EnumParticleType.SPARKLE, world, caster.posX + (i*(dx/5)) + world.rand.nextFloat()/5, WizardryUtilities.getPlayerEyesPos(caster) + (i*(dy/5)) + world.rand.nextFloat()/5, caster.posZ + (i*(dz/5)) + world.rand.nextFloat()/5, 0.0d, 0.0d, 0.0d, 12 + world.rand.nextInt(8), brightness, brightness + 0.1f, 1.0f);
					Wizardry.proxy.spawnParticle(EnumParticleType.SNOW, world, caster.posX + (i*(dx/5)) + world.rand.nextFloat()/5, WizardryUtilities.getPlayerEyesPos(caster) + (i*(dy/5)) + world.rand.nextFloat()/5, caster.posZ + (i*(dz/5)) + world.rand.nextFloat()/5, 0, -0.02, 0, 40 + world.rand.nextInt(10));
				}
			}

			caster.swingItem();
			world.playSoundAtEntity(caster, "wizardry:ice", 1.0F, world.rand.nextFloat() * 0.4F + 1.2F);
			return true;

		}else{
			rayTrace = WizardryUtilities.rayTrace(10*rangeMultiplier, world, caster, true);
			// Gets block the player is looking at and sets to ice or covers with snow as necessary
			// Note how the block is set on the server side only (kinda obvious really) but the particles are
			// spawned on the client side only.
			if(rayTrace != null && rayTrace.typeOfHit == MovingObjectType.BLOCK){
				
				int blockHitX = rayTrace.blockX;
				int blockHitY = rayTrace.blockY;
				int blockHitZ = rayTrace.blockZ;
				int blockHitSide = rayTrace.sideHit;
				if(world.getBlock(blockHitX, blockHitY, blockHitZ) == Blocks.water && !world.isRemote){
					world.setBlock(blockHitX, blockHitY, blockHitZ, Blocks.ice);
				}else if(world.getBlock(blockHitX, blockHitY, blockHitZ) == Blocks.lava && !world.isRemote){
					world.setBlock(blockHitX, blockHitY, blockHitZ, Blocks.obsidian);
				}else if(world.getBlock(blockHitX, blockHitY, blockHitZ) == Blocks.flowing_lava && !world.isRemote){
					world.setBlock(blockHitX, blockHitY, blockHitZ, Blocks.cobblestone);
				}else if(blockHitSide == 1 && !world.isRemote && world.isSideSolid(blockHitX, blockHitY, blockHitZ, ForgeDirection.UP) && WizardryUtilities.canBlockBeReplaced(world, blockHitX, blockHitY+1, blockHitZ)){
					world.setBlock(blockHitX, blockHitY+1, blockHitZ, Blocks.snow_layer);
				}
				
				if(world.isRemote){
					double dx = blockHitX + 0.5 - caster.posX;
					double dy = blockHitY + 1.5 - (caster.posY + caster.height/2);
					double dz = blockHitZ + 0.5 - caster.posZ;
					for(int i=1;i<5;i++){
						float brightness = 0.5f + (world.rand.nextFloat()/2);
						Wizardry.proxy.spawnParticle(EnumParticleType.SPARKLE, world, caster.posX + (i*(dx/5)) + world.rand.nextFloat()/5, WizardryUtilities.getPlayerEyesPos(caster) + (i*(dy/5)) + world.rand.nextFloat()/5, caster.posZ + (i*(dz/5)) + world.rand.nextFloat()/5, 0.0d, 0.0d, 0.0d, 20 + world.rand.nextInt(8), brightness, brightness + 0.1f, 1.0f);
						Wizardry.proxy.spawnParticle(EnumParticleType.SPARKLE, world, caster.posX + (i*(dx/5)) + world.rand.nextFloat()/5, WizardryUtilities.getPlayerEyesPos(caster) + (i*(dy/5)) + world.rand.nextFloat()/5, caster.posZ + (i*(dz/5)) + world.rand.nextFloat()/5, 0, 0, 0, 20 + world.rand.nextInt(8), 1.0f, 1.0f, 1.0f);
					}
				}
				caster.swingItem();
				world.playSoundAtEntity(caster, "wizardry:ice", 1.0F, world.rand.nextFloat() * 0.4F + 1.2F);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EntityLivingBase target, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier){
		
		if(target != null){
		
			if(target instanceof EntityBlaze || target instanceof EntityMagmaCube || target instanceof EntityBlazeMinion){
				target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.FROST), 3.0f * damageMultiplier);
			}
			
			if(!world.isRemote && !MagicDamage.isEntityImmune(DamageType.FROST, target)){
				target.addPotionEffect(new PotionEffect(Wizardry.frost.id, (int)(200*durationMultiplier), 1, true));
			}

			if(target.isBurning()){
				target.extinguish();
			}

			if(world.isRemote){
				double dx = target.posX - caster.posX;
				double dy = (WizardryUtilities.getEntityFeetPos(target) + target.height/2) - (caster.posY + caster.getEyeHeight());
				double dz = target.posZ - caster.posZ;
				for(int i=1;i<5;i++){
					float brightness = 0.5f + (world.rand.nextFloat()/2);
					Wizardry.proxy.spawnParticle(EnumParticleType.SPARKLE, world, caster.posX + (i*(dx/5)) + world.rand.nextFloat()/5, caster.posY + caster.getEyeHeight() + (i*(dy/5)) + world.rand.nextFloat()/5, caster.posZ + (i*(dz/5)) + world.rand.nextFloat()/5, 0.0d, 0.0d, 0.0d, 12 + world.rand.nextInt(8), brightness, brightness + 0.1f, 1.0f);
					Wizardry.proxy.spawnParticle(EnumParticleType.SNOW, world, caster.posX + (i*(dx/5)) + world.rand.nextFloat()/5, caster.posY + caster.getEyeHeight() + (i*(dy/5)) + world.rand.nextFloat()/5, caster.posZ + (i*(dz/5)) + world.rand.nextFloat()/5, 0, -0.02, 0, 40 + world.rand.nextInt(10));
				}
			}

			caster.swingItem();
			world.playSoundAtEntity(caster, "wizardry:ice", 1.0F, world.rand.nextFloat() * 0.4F + 1.2F);
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

}
