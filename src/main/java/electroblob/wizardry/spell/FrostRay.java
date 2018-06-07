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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntitySnowman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class FrostRay extends Spell {

	public FrostRay() {
		super(EnumTier.APPRENTICE, 5, EnumElement.ICE, "frost_ray", EnumSpellType.ATTACK, 0, EnumAction.none, true);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		Vec3 look = caster.getLookVec();
		
		MovingObjectPosition rayTrace = WizardryUtilities.standardEntityRayTrace(world, caster, 10*rangeMultiplier);
		
		if(rayTrace != null && rayTrace.typeOfHit == MovingObjectType.ENTITY && rayTrace.entityHit instanceof EntityLivingBase){
			
			EntityLivingBase target = (EntityLivingBase) rayTrace.entityHit;
			
			if(target.isBurning()){
				target.extinguish();
			}
			
			if(MagicDamage.isEntityImmune(DamageType.FROST, target)){
				if(!world.isRemote && ticksInUse == 1) caster.addChatComponentMessage(new ChatComponentTranslation("spell.resist", target.getCommandSenderName(), this.getDisplayNameWithFormatting()));
			}else{
				// For frost ray the entity can move slightly, unlike freeze.
				target.addPotionEffect(new PotionEffect(Wizardry.frost.id, (int)(200*durationMultiplier), 0, true));
				
				if(target instanceof EntityBlaze || target instanceof EntityMagmaCube || target instanceof EntityBlazeMinion){
					// This motion stuff removes knockback, which is desirable for continuous spells.
					double motionX = target.motionX;
					double motionY = target.motionY;
					double motionZ = target.motionZ;
					
					target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.FROST), 6.0f * damageMultiplier);

					target.motionX = motionX;
					target.motionY = motionY;
					target.motionZ = motionZ;
				}else{
					// This motion stuff removes knockback, which is desirable for continuous spells.
					double motionX = target.motionX;
					double motionY = target.motionY;
					double motionZ = target.motionZ;
					
					target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.FROST), 3.0f * damageMultiplier);

					target.motionX = motionX;
					target.motionY = motionY;
					target.motionZ = motionZ;
				}
			}
		}
		
		if(world.isRemote){
			for(int i=0; i<20; i++){
				double x1 = caster.posX + look.xCoord*i/2 + world.rand.nextFloat()/5 - 0.1f;
				double y1 = WizardryUtilities.getPlayerEyesPos(caster) - 0.4f + look.yCoord*i/2 + world.rand.nextFloat()/5 - 0.1f;
				double z1 = caster.posZ + look.zCoord*i/2 + world.rand.nextFloat()/5 - 0.1f;
				Wizardry.proxy.spawnParticle(EnumParticleType.SPARKLE, world, x1, y1, z1, look.xCoord*rangeMultiplier, look.yCoord*rangeMultiplier, look.zCoord*rangeMultiplier, 8 + world.rand.nextInt(12), 0.4f, 0.6f, 1.0f);

				x1 = caster.posX + look.xCoord*i/2 + world.rand.nextFloat()/5 - 0.1f;
				y1 = WizardryUtilities.getPlayerEyesPos(caster) - 0.4f + look.yCoord*i/2 + world.rand.nextFloat()/5 - 0.1f;
				z1 = caster.posZ + look.zCoord*i/2 + world.rand.nextFloat()/5 - 0.1f;
				Wizardry.proxy.spawnParticle(EnumParticleType.SPARKLE, world, x1, y1, z1, look.xCoord*rangeMultiplier, look.yCoord*rangeMultiplier, look.zCoord*rangeMultiplier, 8 + world.rand.nextInt(12), 1.0f, 1.0f, 1.0f);
			}
		}
		
		if(ticksInUse % 12 == 0){
			if(ticksInUse == 0)world.playSoundAtEntity(caster, "wizardry:ice", 0.5F, 1.0f);
			world.playSoundAtEntity(caster, "wizardry:frostray", 0.5F, 1.0f);
		}
		return true;
	}


}
