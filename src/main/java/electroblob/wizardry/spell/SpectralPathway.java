package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.tileentity.TileEntityTimer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumAction;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class SpectralPathway extends Spell {

	public SpectralPathway() {
		super(EnumTier.ADVANCED, 40, EnumElement.SORCERY, "spectral_pathway", EnumSpellType.UTILITY, 300, EnumAction.bow, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		// Won't work if caster is airborne or if they are already on a bridge (prevents infinite bridges)
		if(WizardryUtilities.getBlockEntityIsStandingOn(caster) == Blocks.air
				|| WizardryUtilities.getBlockEntityIsStandingOn(caster) == Wizardry.spectralBlock){
			return false;
		}
		
		// Changes the yaw to 0, 1, 2 or 3 (compass directions)
		//int direction = 0;
		/*= (int)((entityplayer.rotationYawHead-45)/90);
					if(direction == 4) direction = 0;*/
		/*
		if(caster.rotationYawHead <= 45 || caster.rotationYawHead > 315){
			direction = 3;
		}else if(caster.rotationYawHead > 45 && caster.rotationYawHead <= 135){
			direction = 0;
		}else if(caster.rotationYawHead > 135 && caster.rotationYawHead <= 225){
			direction = 1;
		}else if(caster.rotationYawHead > 225 && caster.rotationYawHead <= 315){
			direction = 2;
		}
		*/
		
		int direction = MathHelper.floor_double((double)(caster.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
		
		boolean flag = false;

		if(!world.isRemote){
			
			int baseLength = 15;
			
			//entityplayer.addChatMessage("Direction: " + direction);
			switch(direction){
			case 0:
				for(int i=0; i<(int)(baseLength*rangeMultiplier); i++){
					if(WizardryUtilities.canBlockBeReplacedB(world, (int)(caster.posX+0.5) - 1, (int)caster.posY-1, (int)(caster.posZ+0.5) + i - 1)){
						world.setBlock((int)(caster.posX+0.5) - 1, (int)caster.posY-1, (int)(caster.posZ+0.5) + i - 1, Wizardry.spectralBlock);
						if(world.getTileEntity((int)(caster.posX+0.5) - 1, (int)caster.posY-1, (int)(caster.posZ+0.5) + i - 1) instanceof TileEntityTimer){
							((TileEntityTimer)world.getTileEntity((int)(caster.posX+0.5) - 1, (int)caster.posY-1, (int)(caster.posZ+0.5) + i - 1)).setLifetime((int)(1200*durationMultiplier));
						}
						flag = true;
					}
					if(WizardryUtilities.canBlockBeReplacedB(world, (int)(caster.posX+0.5) - 2, (int)caster.posY-1, (int)(caster.posZ+0.5) + i - 1)){
						world.setBlock((int)(caster.posX+0.5) - 2, (int)caster.posY-1, (int)(caster.posZ+0.5) + i - 1, Wizardry.spectralBlock);
						if(world.getTileEntity((int)(caster.posX+0.5) - 2, (int)caster.posY-1, (int)(caster.posZ+0.5) + i - 1) instanceof TileEntityTimer){
							((TileEntityTimer)world.getTileEntity((int)(caster.posX+0.5) - 2, (int)caster.posY-1, (int)(caster.posZ+0.5) + i - 1)).setLifetime((int)(1200*durationMultiplier));
						}
						flag = true;
					}
				}
				break;
			case 1:
				for(int i=0; i<(int)(baseLength*rangeMultiplier); i++){
					if(WizardryUtilities.canBlockBeReplacedB(world, (int)(caster.posX+0.5) - i - 1, (int)caster.posY-1, (int)(caster.posZ+0.5) - 1)){
						world.setBlock((int)(caster.posX+0.5) - i - 1, (int)caster.posY-1, (int)(caster.posZ+0.5) - 1, Wizardry.spectralBlock);
						if(world.getTileEntity((int)(caster.posX+0.5) - i - 1, (int)caster.posY-1, (int)(caster.posZ+0.5) - 1) instanceof TileEntityTimer){
							((TileEntityTimer)world.getTileEntity((int)(caster.posX+0.5) - i - 1, (int)caster.posY-1, (int)(caster.posZ+0.5) - 1)).setLifetime((int)(1200*durationMultiplier));
						}
						flag = true;
					}
					if(WizardryUtilities.canBlockBeReplacedB(world, (int)(caster.posX+0.5) - i - 1, (int)caster.posY-1, (int)(caster.posZ+0.5))){
						world.setBlock((int)(caster.posX+0.5) - i - 1, (int)caster.posY-1, (int)(caster.posZ+0.5), Wizardry.spectralBlock);
						if(world.getTileEntity((int)(caster.posX+0.5) - i - 1, (int)caster.posY-1, (int)(caster.posZ+0.5)) instanceof TileEntityTimer){
							((TileEntityTimer)world.getTileEntity((int)(caster.posX+0.5) - i - 1, (int)caster.posY-1, (int)(caster.posZ+0.5))).setLifetime((int)(1200*durationMultiplier));
						}
						flag = true;
					}
				}
				break;
			case 2:
				for(int i=0; i<(int)(baseLength*rangeMultiplier); i++){
					if(WizardryUtilities.canBlockBeReplacedB(world, (int)(caster.posX+0.5) - 1, (int)caster.posY-1, (int)(caster.posZ+0.5) - i)){
						world.setBlock((int)(caster.posX+0.5) - 1, (int)caster.posY-1, (int)(caster.posZ+0.5) - i, Wizardry.spectralBlock);
						if(world.getTileEntity((int)(caster.posX+0.5) - 1, (int)caster.posY-1, (int)(caster.posZ+0.5) - i) instanceof TileEntityTimer){
							((TileEntityTimer)world.getTileEntity((int)(caster.posX+0.5) - 1, (int)caster.posY-1, (int)(caster.posZ+0.5) - i)).setLifetime((int)(1200*durationMultiplier));
						}
						flag = true;
					}
					if(WizardryUtilities.canBlockBeReplacedB(world, (int)(caster.posX+0.5) - 2, (int)caster.posY-1, (int)(caster.posZ+0.5) - i)){
						world.setBlock((int)(caster.posX+0.5) - 2, (int)caster.posY-1, (int)(caster.posZ+0.5) - i, Wizardry.spectralBlock);
						if(world.getTileEntity((int)(caster.posX+0.5) - 2, (int)caster.posY-1, (int)(caster.posZ+0.5) - i) instanceof TileEntityTimer){
							((TileEntityTimer)world.getTileEntity((int)(caster.posX+0.5) - 2, (int)caster.posY-1, (int)(caster.posZ+0.5) - i)).setLifetime((int)(1200*durationMultiplier));
						}
						flag = true;
					}
				}
				break;
			case 3:
				for(int i=0; i<(int)(baseLength*rangeMultiplier); i++){
					if(WizardryUtilities.canBlockBeReplacedB(world, (int)(caster.posX+0.5) + i - 2, (int)caster.posY-1, (int)(caster.posZ+0.5) - 1)){
						world.setBlock((int)(caster.posX+0.5) + i - 2, (int)caster.posY-1, (int)(caster.posZ+0.5) - 1, Wizardry.spectralBlock);
						if(world.getTileEntity((int)(caster.posX+0.5) + i - 2, (int)caster.posY-1, (int)(caster.posZ+0.5) - 1) instanceof TileEntityTimer){
							((TileEntityTimer)world.getTileEntity((int)(caster.posX+0.5) + i - 2, (int)caster.posY-1, (int)(caster.posZ+0.5) - 1)).setLifetime((int)(1200*durationMultiplier));
						}
						flag = true;
					}
					if(WizardryUtilities.canBlockBeReplacedB(world, (int)(caster.posX+0.5) + i - 2, (int)caster.posY-1, (int)(caster.posZ+0.5))){
						world.setBlock((int)(caster.posX+0.5) + i - 2, (int)caster.posY-1, (int)(caster.posZ+0.5), Wizardry.spectralBlock);
						if(world.getTileEntity((int)(caster.posX+0.5) + i - 2, (int)caster.posY-1, (int)(caster.posZ+0.5)) instanceof TileEntityTimer){
							((TileEntityTimer)world.getTileEntity((int)(caster.posX+0.5) + i - 2, (int)caster.posY-1, (int)(caster.posZ+0.5))).setLifetime((int)(1200*durationMultiplier));
						}
						flag = true;
					}
				}
				break;
			}
		}
		
		if(flag) world.playSoundAtEntity(caster, "wizardry:largeaura", 1.0f, 1.0f);
		
		return flag;
	}


}
