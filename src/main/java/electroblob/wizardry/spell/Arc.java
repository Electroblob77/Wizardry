package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.MagicDamage;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.MagicDamage.DamageType;
import electroblob.wizardry.entity.EntityArc;
import electroblob.wizardry.entity.living.EntityLightningWraith;
import electroblob.wizardry.entity.living.EntityStormElemental;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

// This spell was the 'guinea pig' for damage types, so to speak, so there's a bit of commentary on them here that may
// be useful for future reference.
public class Arc extends Spell {

	public Arc() {
		super(EnumTier.BASIC, 5, EnumElement.LIGHTNING, "arc", EnumSpellType.ATTACK, 15, EnumAction.none, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {

		MovingObjectPosition rayTrace = WizardryUtilities.standardEntityRayTrace(world, caster, 8*rangeMultiplier, 4.0f);

		if(rayTrace != null && rayTrace.entityHit != null && rayTrace.entityHit instanceof EntityLivingBase){
			
			Entity target = rayTrace.entityHit;
			
			if(!world.isRemote){
				EntityArc arc = new EntityArc(world);
				arc.setEndpointCoords(caster.posX, caster.posY + 1, caster.posZ,
						target.posX, target.posY + target.height/2, target.posZ);
				world.spawnEntityInWorld(arc);
			}else{
				for(int i=0;i<8;i++){
					Wizardry.proxy.spawnParticle(EnumParticleType.SPARK, world, target.posX + world.rand.nextFloat() - 0.5, WizardryUtilities.getEntityFeetPos(target) + target.height/2 + world.rand.nextFloat()*2 - 1, target.posZ + world.rand.nextFloat() - 0.5, 0, 0, 0, 3);
					world.spawnParticle("largesmoke", target.posX + world.rand.nextFloat() - 0.5, WizardryUtilities.getEntityFeetPos(target) + target.height/2 + world.rand.nextFloat()*2 - 1, target.posZ + world.rand.nextFloat() - 0.5, 0, 0, 0);
				}
			}
			
			// This is a lot neater than it was, thanks to the damage type system.
			if(MagicDamage.isEntityImmune(DamageType.SHOCK, target)){
				if(!world.isRemote) caster.addChatComponentMessage(new ChatComponentTranslation("spell.resist", target.getCommandSenderName(), this.getDisplayNameWithFormatting()));
			}else{
				target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.SHOCK), 3.0f * damageMultiplier);
			}

			caster.swingItem();
			world.playSoundAtEntity(target, "wizardry:arc", 1.0F, world.rand.nextFloat() * 0.4F + 1.5F);
			return true;
		}

		return false;
	}
	
	@Override
	public boolean cast(World world, EntityLiving caster, EntityLivingBase target, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier){
		
		if(target != null){
			
			if(!world.isRemote){
				EntityArc arc = new EntityArc(world);
				arc.setEndpointCoords(caster.posX, caster.posY + 1, caster.posZ,
						target.posX, target.posY + target.height/2, target.posZ);
				world.spawnEntityInWorld(arc);
			}else{
				for(int i=0;i<8;i++){
					Wizardry.proxy.spawnParticle(EnumParticleType.SPARK, world, target.posX + world.rand.nextFloat() - 0.5, WizardryUtilities.getEntityFeetPos(target) + target.height/2 + world.rand.nextFloat()*2 - 1, target.posZ + world.rand.nextFloat() - 0.5, 0, 0, 0, 3);
					world.spawnParticle("largesmoke", target.posX + world.rand.nextFloat() - 0.5, WizardryUtilities.getEntityFeetPos(target) + target.height/2 + world.rand.nextFloat()*2 - 1, target.posZ + world.rand.nextFloat() - 0.5, 0, 0, 0);
				}
			}

			// What's great about the damage type system is that, because I don't need to know if the creature resisted
			// the damage here, I can simply call this without having to check for immunities at all.
			target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.SHOCK), 3.0f * damageMultiplier);

			caster.swingItem();
			world.playSoundAtEntity(target, "wizardry:arc", 1.0F, world.rand.nextFloat() * 0.4F + 1.5F);
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}


}
