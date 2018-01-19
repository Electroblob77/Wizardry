package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.construct.EntityIceSpike;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class IceSpikes extends Spell {

	public IceSpikes() {
		super(Tier.ADVANCED, 30, Element.ICE, "ice_spikes", SpellType.ATTACK, 75, EnumAction.NONE, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers) {
		
		RayTraceResult rayTrace = WizardryUtilities.rayTrace(20*modifiers.get(WizardryItems.range_upgrade), world, caster, false);
		
		if(rayTrace != null && rayTrace.typeOfHit == RayTraceResult.Type.BLOCK && rayTrace.sideHit == EnumFacing.UP){
			
			if(!world.isRemote){
				
				double x = rayTrace.hitVec.xCoord;
				double y = rayTrace.hitVec.yCoord;
				double z = rayTrace.hitVec.zCoord;
				
				for(int i=0; i<(int)(18*modifiers.get(WizardryItems.blast_upgrade)); i++){
					
					float angle = (float)(world.rand.nextFloat()*Math.PI*2);
					double radius = 0.5 + world.rand.nextDouble()*2*modifiers.get(WizardryItems.blast_upgrade);
					
					double x1 = x + radius*MathHelper.sin(angle);
					double z1 = z + radius*MathHelper.cos(angle);
					double y1 = WizardryUtilities.getNearestFloorLevel(world, new BlockPos(MathHelper.floor_double(x1), (int)y, MathHelper.floor_double(z1)), 2) - 1;
					
					if(y1 > -1){
						EntityIceSpike icespike = new EntityIceSpike(world, x1, y1, z1, caster, 30 + world.rand.nextInt(15), modifiers.get(SpellModifiers.DAMAGE));
						world.spawnEntityInWorld(icespike);
					}
				}
			}
			
			caster.swingArm(hand);
			WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_ICE, 1.0F, 1.0F);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target, SpellModifiers modifiers){
		
		if(target != null){
			
			if(!world.isRemote){
				
				double x = target.posX;
				double y = target.posY;
				double z = target.posZ;
				
				for(int i=0; i<(int)(18*modifiers.get(WizardryItems.blast_upgrade)); i++){
					
					float angle = (float)(world.rand.nextFloat()*Math.PI*2);
					double radius = 0.5 + world.rand.nextDouble()*2*modifiers.get(WizardryItems.blast_upgrade);
					
					double x1 = x + radius*MathHelper.sin(angle);
					double z1 = z + radius*MathHelper.cos(angle);
					double y1 = WizardryUtilities.getNearestFloorLevel(world, new BlockPos(MathHelper.floor_double(x1), (int)y, MathHelper.floor_double(z1)), 2) - 1;
					
					if(y1 > -1){
						EntityIceSpike icespike = new EntityIceSpike(world, x1, y1, z1, caster, 30 + world.rand.nextInt(15), modifiers.get(SpellModifiers.DAMAGE));
						world.spawnEntityInWorld(icespike);
					}
				}
			}
			caster.swingArm(hand);
			caster.playSound(WizardrySounds.SPELL_ICE, 1.0F, 1.0F);
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}
	
}
