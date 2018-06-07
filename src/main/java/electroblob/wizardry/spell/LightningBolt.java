package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.WizardryUtilities;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;

public class LightningBolt extends Spell {

	public LightningBolt() {
		super(EnumTier.ADVANCED, 40, EnumElement.LIGHTNING, "lightning_bolt", EnumSpellType.ATTACK, 80, EnumAction.none, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		MovingObjectPosition rayTrace = WizardryUtilities.rayTrace(200, world, caster, false);
        
		if(rayTrace != null && rayTrace.typeOfHit == MovingObjectType.BLOCK){
        	
			int i = rayTrace.blockX;
        	int j = rayTrace.blockY;
        	int k = rayTrace.blockZ;
        	
        	// Not sure why it is +1 but it has to be to work properly.
        	if(world.canBlockSeeTheSky(i, j+1, k)){
            
	        	if(!world.isRemote){
		            EntityLightningBolt entitylightning = new EntityLightningBolt(world, i, j, k);
		            world.addWeatherEffect(entitylightning);
		            
		            // Code for eventhandler recognition; for achievements and such like. Left in for future use.
		            NBTTagCompound entityNBT = entitylightning.getEntityData();
		            entityNBT.setString("summoningPlayer", caster.getUniqueID().toString());
	        	}
	        	
	        	caster.swingItem();
	            return true;
        	}
        }
		
        return false;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EntityLivingBase target, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier){
		
		if(target != null){

			int i = (int)target.posX;
        	int j = (int)target.posY;
        	int k = (int)target.posZ;
        	
        	// Not sure why it is +1 but it has to be to work properly.
        	if(world.canBlockSeeTheSky(i, j+1, k)){
            
	        	if(!world.isRemote){
		            EntityLightningBolt entitylightning = new EntityLightningBolt(world, i, j, k);
		            world.addWeatherEffect(entitylightning);
	        	}
	        	
	        	caster.swingItem();
	            return true;
        	}
		}
		
		return false;
	}
	
	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}
}
