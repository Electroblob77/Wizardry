package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.MagicDamage;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.MagicDamage.DamageType;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class Wither extends Spell {

	public Wither() {
		super(EnumTier.APPRENTICE, 10, EnumElement.NECROMANCY, "wither", EnumSpellType.ATTACK, 20, EnumAction.none, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {

		Vec3 look = caster.getLookVec();

		MovingObjectPosition rayTrace = WizardryUtilities.standardEntityRayTrace(world, caster, 10*rangeMultiplier);

		if(rayTrace != null && rayTrace.typeOfHit == MovingObjectType.ENTITY && rayTrace.entityHit instanceof EntityLivingBase){
			
			EntityLivingBase target = (EntityLivingBase) rayTrace.entityHit;
			
			// Has no effect on withers or wither skeletons.
			if(MagicDamage.isEntityImmune(DamageType.WITHER, target)){
				if(!world.isRemote) caster.addChatComponentMessage(new ChatComponentTranslation("spell.resist", target.getCommandSenderName(), this.getDisplayNameWithFormatting()));
			}else{
				target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.WITHER), 1.0f * damageMultiplier);
				target.addPotionEffect(new PotionEffect(Potion.wither.id, (int)(200*durationMultiplier), 1));
			}
		}
		if(world.isRemote){
			for(int i=1; i<(int)(25*rangeMultiplier); i+=2){
				// I figured it out! when on client side, entityplayer.posY is at the eyes, not the feet!
				// This is a test for lining up the ray with the wand tip. Not sure if I like it or not.
				/*
				Vec3 origin = Wizardry.proxy.getWandTipPosition(caster);
				double x1 = origin.xCoord + look.xCoord*i/2 + world.rand.nextFloat()/5 - 0.1f;
				double y1 = origin.yCoord + look.yCoord*i/2 + world.rand.nextFloat()/5 - 0.1f;
				double z1 = origin.zCoord + look.zCoord*i/2 + world.rand.nextFloat()/5 - 0.1f;
				*/
				double x1 = caster.posX + look.xCoord*i/2 + world.rand.nextFloat()/5 - 0.1f;
				double y1 = WizardryUtilities.getPlayerEyesPos(caster) - 0.4f + look.yCoord*i/2 + world.rand.nextFloat()/5 - 0.1f;
				double z1 = caster.posZ + look.zCoord*i/2 + world.rand.nextFloat()/5 - 0.1f;
				//world.spawnParticle("mobSpell", x1, y1, z1, -1*look.xCoord, -1*look.yCoord, -1*look.zCoord);
				Wizardry.proxy.spawnParticle(EnumParticleType.DARK_MAGIC, world, x1, y1, z1, 0.0d, 0.0d, 0.0d, 0, 0.1f, 0.0f, 0.0f);
				Wizardry.proxy.spawnParticle(EnumParticleType.SPARKLE, world, x1, y1, z1, 0.0d, 0.0d, 0.0d, 12 + world.rand.nextInt(8), 0.1f, 0.0f, 0.05f);
			}
		}
		caster.swingItem();
		world.playSoundAtEntity(caster, "mob.wither.hurt", 1.0F, world.rand.nextFloat() * 0.2F + 1.0F);
		return true;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EntityLivingBase target, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier){

		if(target != null){
			// Has no effect on withers or wither skeletons.
			if(!MagicDamage.isEntityImmune(DamageType.WITHER, target) && !world.isRemote){
				target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.WITHER), 1.0f * damageMultiplier);
				target.addPotionEffect(new PotionEffect(Potion.wither.id, (int)(200*durationMultiplier), 1));
			}
			
			if(world.isRemote){

				double dx = (target.posX - caster.posX)/caster.getDistanceToEntity(target);
				double dy = (target.posY - caster.posY)/caster.getDistanceToEntity(target);
				double dz = (target.posZ - caster.posZ)/caster.getDistanceToEntity(target);
				
				for(int i=1; i<(int)(25*rangeMultiplier); i+=2){

					double x1 = caster.posX + dx*i/2 + world.rand.nextFloat()/5 - 0.1f;
					double y1 = caster.posY + caster.getEyeHeight() - 0.4f + dy*i/2 + world.rand.nextFloat()/5 - 0.1f;
					double z1 = caster.posZ + dz*i/2 + world.rand.nextFloat()/5 - 0.1f;

					Wizardry.proxy.spawnParticle(EnumParticleType.DARK_MAGIC, world, x1, y1, z1, 0.0d, 0.0d, 0.0d, 0, 0.1f, 0.0f, 0.0f);
					Wizardry.proxy.spawnParticle(EnumParticleType.SPARKLE, world, x1, y1, z1, 0.0d, 0.0d, 0.0d, 12 + world.rand.nextInt(8), 0.1f, 0.0f, 0.05f);
				}
			}
			caster.swingItem();
			world.playSoundAtEntity(caster, "mob.wither.hurt", 1.0F, world.rand.nextFloat() * 0.2F + 1.0F);
			return true;
		}

		return false;
	}

	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

}
