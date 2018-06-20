package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Whirlwind extends SpellRay {

	public Whirlwind(){
		super("whirlwind", Tier.APPRENTICE, Element.EARTH, SpellType.DEFENCE, 10, 15, false, 10, WizardrySounds.SPELL_ICE);
		this.soundValues(0.8f, 0.7f, 0.2f);
	}

	@Override
	protected boolean onEntityHit(World world, Entity target, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		
		// Left as EntityLivingBase because why not be able to move armour stands around?
		if(target instanceof EntityLivingBase){

			if(!world.isRemote){

				target.motionX = caster.getLookVec().x * 2;
				target.motionY = caster.getLookVec().y * 2 + 1;
				target.motionZ = caster.getLookVec().z * 2;

				// Player motion is handled on that player's client so needs packets
				if(target instanceof EntityPlayerMP){
					((EntityPlayerMP)target).connection.sendPacket(new SPacketEntityVelocity(target));
				}
			}

			if(world.isRemote){
				for(int i = 0; i < 10; i++){
					double x = (double)(caster.posX + world.rand.nextFloat() - 0.5F
							+ caster.getLookVec().x * caster.getDistance(target) * 0.5);
					double y = (double)(caster.getEntityBoundingBox().minY + caster.getEyeHeight() + world.rand.nextFloat() - 0.5F
							+ caster.getLookVec().y * caster.getDistance(target) * 0.5);
					double z = (double)(caster.posZ + world.rand.nextFloat() - 0.5F
							+ caster.getLookVec().z * caster.getDistance(target) * 0.5);
					world.spawnParticle(EnumParticleTypes.CLOUD, x, y, z, caster.getLookVec().x,
							caster.getLookVec().y, caster.getLookVec().z);
				}
			}
			
			return true;
		}
		
		return false;
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onMiss(World world, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

}
