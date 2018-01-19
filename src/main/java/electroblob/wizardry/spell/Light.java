package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.tileentity.TileEntityTimer;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class Light extends Spell {

	public Light() {
		super(Tier.BASIC, 5, Element.SORCERY, "light", SpellType.UTILITY, 15, EnumAction.NONE, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers) {
		
		RayTraceResult rayTrace = WizardryUtilities.rayTrace(4, world, caster, false);
		
		if(rayTrace != null && rayTrace.typeOfHit == RayTraceResult.Type.BLOCK){
			
			BlockPos pos = rayTrace.getBlockPos().offset(rayTrace.sideHit);
			
			if(world.isAirBlock(pos)){
				
				if(!world.isRemote){
					world.setBlockState(pos, WizardryBlocks.magic_light.getDefaultState());
					if(world.getTileEntity(pos) instanceof TileEntityTimer){
						((TileEntityTimer)world.getTileEntity(pos)).setLifetime((int)(600*modifiers.get(WizardryItems.duration_upgrade)));
					}
				}
				
				caster.swingArm(hand);
				WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_CONJURATION, 1.0f, 1.0f);
				return true;
			}
		}else{
			
			int x = (int) (Math.floor(caster.posX) + caster.getLookVec().xCoord*4);
			int y = (int) (Math.floor(caster.posY) + caster.eyeHeight + caster.getLookVec().yCoord*4);
			int z = (int) (Math.floor(caster.posZ) + caster.getLookVec().zCoord*4);
			
			BlockPos pos = new BlockPos(x, y, z);
			
			if(world.isAirBlock(pos)){
				//world.playSound(x, y, z, "sound.ambient.cave.cave", 1.0f, 1.5f, false);
				if(!world.isRemote){
					world.setBlockState(pos, WizardryBlocks.magic_light.getDefaultState());
					if(world.getTileEntity(pos) instanceof TileEntityTimer){
						((TileEntityTimer)world.getTileEntity(pos)).setLifetime((int)(600*modifiers.get(WizardryItems.duration_upgrade)));
					}
				}
				caster.swingArm(hand);
				WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_CONJURATION, 1.0f, 1.0f);
				return true;
			}
		}
		return false;
	}


}
