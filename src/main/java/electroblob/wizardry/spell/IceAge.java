package electroblob.wizardry.spell;

import java.util.List;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.tileentity.TileEntityStatue;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumAction;
import net.minecraft.world.World;

public class IceAge extends Spell {
	
	private static final int baseDuration = 1200;

	public IceAge() {
		super(EnumTier.MASTER, 70, EnumElement.ICE, "ice_age", EnumSpellType.ATTACK, 250, EnumAction.bow, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(7*blastMultiplier, caster.posX, caster.posY, caster.posZ, world);
		
		for(EntityLivingBase target : targets){
			if(WizardryUtilities.isValidTarget(caster, target)){
				if(!world.isRemote){
					if(target instanceof EntityBlaze || target instanceof EntityMagmaCube){
						// These have been removed for the time being because they cause the entity to sink into the floor when it breaks out.
						//target.attackEntityFrom(WizardryUtilities.causePlayerMagicDamage(entityplayer), 8.0f * damageMultiplier);
					}else{
						//target.attackEntityFrom(WizardryUtilities.causePlayerMagicDamage(entityplayer), 4.0f * damageMultiplier);
					}
					if(target.isBurning()){
						target.extinguish();
					}

					if(target instanceof EntityBlaze) caster.addStat(Wizardry.freezeBlaze, 1);
					
					if(target instanceof EntityLiving){
						
						// Stops the entity looking red while frozen and the resulting z-fighting
						target.hurtTime = 0;
						
						int x = (int)Math.floor(target.posX);
						int y = (int)Math.floor(target.posY);
						int z = (int)Math.floor(target.posZ);
						
						//Short mobs such as spiders and pigs
						if((target.height < 1.2 || target.isChild()) && WizardryUtilities.canBlockBeReplaced(world, x, y, z)){
							world.setBlock(x, y, z, Wizardry.iceStatue);
							if(world.getTileEntity(x, y, z) instanceof TileEntityStatue){
								((TileEntityStatue)world.getTileEntity(x, y, z)).setCreatureAndPart((EntityLiving) target, 1, 1);
								((TileEntityStatue)world.getTileEntity(x, y, z)).setLifetime((int)(baseDuration*durationMultiplier));
							}
							target.setDead();
							world.playSoundAtEntity(target, "wizardry:freeze", 1.0F, world.rand.nextFloat() * 0.4F + 0.8F);
						}
						//Normal sized mobs like zombies and skeletons
						else if(target.height < 2.5 && WizardryUtilities.canBlockBeReplaced(world, x, y, z) && WizardryUtilities.canBlockBeReplaced(world, x, y+1, z)){
							world.setBlock(x, y, z, Wizardry.iceStatue);
							if(world.getTileEntity(x, y, z) instanceof TileEntityStatue){
								((TileEntityStatue)world.getTileEntity(x, y, z)).setCreatureAndPart((EntityLiving) target, 1, 2);
								((TileEntityStatue)world.getTileEntity(x, y, z)).setLifetime((int)(baseDuration*durationMultiplier));
							}
							
							world.setBlock(x, y+1, z, Wizardry.iceStatue);
							if(world.getTileEntity(x, y+1, z) instanceof TileEntityStatue){
								((TileEntityStatue)world.getTileEntity(x, y+1, z)).setCreatureAndPart((EntityLiving) target, 2, 2);
							}
							target.setDead();
							world.playSoundAtEntity(target, "wizardry:freeze", 1.0F, world.rand.nextFloat() * 0.4F + 0.8F);
						}
						//Tall mobs like endermen
						else if(WizardryUtilities.canBlockBeReplaced(world, x, y, z) && WizardryUtilities.canBlockBeReplaced(world, x, y+1, z) && WizardryUtilities.canBlockBeReplaced(world, x, y+2, z)){
							world.setBlock(x, y, z, Wizardry.iceStatue);
							if(world.getTileEntity(x, y, z) instanceof TileEntityStatue){
								((TileEntityStatue)world.getTileEntity(x, y, z)).setCreatureAndPart((EntityLiving) target, 1, 3);
								((TileEntityStatue)world.getTileEntity(x, y, z)).setLifetime((int)(baseDuration*durationMultiplier));
							}
							
							world.setBlock(x, y+1, z, Wizardry.iceStatue);
							if(world.getTileEntity(x, y+1, z) instanceof TileEntityStatue){
								((TileEntityStatue)world.getTileEntity(x, y+1, z)).setCreatureAndPart((EntityLiving) target, 2, 3);
							}
							
							world.setBlock(x, y+2, z, Wizardry.iceStatue);
							if(world.getTileEntity(x, y+2, z) instanceof TileEntityStatue){
								((TileEntityStatue)world.getTileEntity(x, y+2, z)).setCreatureAndPart((EntityLiving) target, 3, 3);
							}
							target.setDead();
							world.playSoundAtEntity(target, "wizardry:freeze", 1.0F, world.rand.nextFloat() * 0.4F + 0.8F);
						}
					}
				}
			}
		}
		if(!world.isRemote){
			for(int i=-7; i<8; i++){
				for(int j=-7; j<8; j++){
					int y = WizardryUtilities.getNearestFloorLevelB(world, (int)caster.posX + i, (int)caster.posY, (int)caster.posZ + j, 7);
					//System.out.println(y);
					double dist = caster.getDistance((int)caster.posX + i, y, (int)caster.posZ + j);
					// Randomised with weighting so that the nearer the block the more likely it is to be snowed.
					if(y != -1 && world.rand.nextInt((int)dist*2 + 1) < 7 && dist < 8){
						if(world.getBlock((int)caster.posX + i, y-1, (int)caster.posZ + j) == Blocks.water){
							world.setBlock((int)caster.posX + i, y-1, (int)caster.posZ + j, Blocks.ice);
						}else if(world.getBlock((int)caster.posX + i, y-1, (int)caster.posZ + j) == Blocks.lava){
							world.setBlock((int)caster.posX + i, y-1, (int)caster.posZ + j, Blocks.obsidian);
						}else if(world.getBlock((int)caster.posX + i, y-1, (int)caster.posZ + j) == Blocks.flowing_lava){
							world.setBlock((int)caster.posX + i, y-1, (int)caster.posZ + j, Blocks.cobblestone);
						}else{
							world.setBlock((int)caster.posX + i, y, (int)caster.posZ + j, Blocks.snow_layer);
						}
					}
				}
			}
		}
		world.playSoundAtEntity(caster, "wizardry:ice", 0.7F, 1.0f);
		world.playSoundAtEntity(caster, "wizardry:wind", 1.0F, 1.0f);
		return true;
	}


}
