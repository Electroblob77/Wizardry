package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class LightningBolt extends Spell {

	public LightningBolt() {
		super(Tier.ADVANCED, 40, Element.LIGHTNING, "lightning_bolt", SpellType.ATTACK, 80, EnumAction.NONE, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers) {
		
		RayTraceResult rayTrace = WizardryUtilities.rayTrace(200, world, caster, false);
        
		if(rayTrace != null && rayTrace.typeOfHit == RayTraceResult.Type.BLOCK){
        	
			BlockPos pos = rayTrace.getBlockPos();
        	
        	// Not sure why it is up 1 but it has to be for canBlockSeeSky to work properly.
			// TODO: Remove this requirement?
        	if(world.canBlockSeeSky(pos.up())){
            
	        	if(!world.isRemote){
		            EntityLightningBolt entitylightning = new EntityLightningBolt(world, pos.getX(), pos.getY(), pos.getZ(), false);
		            world.addWeatherEffect(entitylightning);
		            
		            // Code for eventhandler recognition; for achievements and such like. Left in for future use.
		            NBTTagCompound entityNBT = entitylightning.getEntityData();
		            entityNBT.setUniqueId("summoningPlayer", caster.getUniqueID());
	        	}
	        	
	        	caster.swingArm(hand);
	            return true;
        	}
        }
		
        return false;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target, SpellModifiers modifiers){
		
		if(target != null){

			int x = (int)target.posX;
        	int y = (int)target.posY;
        	int z = (int)target.posZ;

        	// Not sure why it is up 1 but it has to be for canBlockSeeSky to work properly.
			// TODO: Remove this requirement?
        	if(world.canBlockSeeSky(new BlockPos(x, y, z))){
            
	        	if(!world.isRemote){
		            EntityLightningBolt entitylightning = new EntityLightningBolt(world, x, y, z, false);
		            world.addWeatherEffect(entitylightning);
	        	}
	        	
	        	caster.swingArm(hand);
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
