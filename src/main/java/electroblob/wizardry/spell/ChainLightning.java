package electroblob.wizardry.spell;

import java.util.List;

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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class ChainLightning extends Spell {

	public ChainLightning() {
		super(EnumTier.ADVANCED, 25, EnumElement.LIGHTNING, "chain_lightning", EnumSpellType.ATTACK, 50, EnumAction.none, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {
		
		// First shot has range 10 (this is the only range affected by upgrades) and does 5 hearts of damage.
		// Chains to up to 5 secondary targets within a range of 5 of the primary target, and then to up to 2
		// tertiary targets per secondary target within a range of 5 of that. Secondary targets are dealt 4 hearts
		// of damage; tertiary targets are dealt 3 hearts of damage.

		MovingObjectPosition rayTrace = WizardryUtilities.standardEntityRayTrace(world, caster, 10*rangeMultiplier, 8.0f);
		
		// Anything can be attacked with the initial arc, because the player has control over where it goes. If they
		// hit a minion or an ally, it's their problem!
		if(rayTrace != null && rayTrace.entityHit != null && rayTrace.entityHit instanceof EntityLivingBase){
			
			Entity target = rayTrace.entityHit;
			
			if(!world.isRemote){
				EntityArc arc = new EntityArc(world);
				arc.setEndpointCoords(caster.posX, caster.posY + caster.height/2, caster.posZ,
						target.posX, target.posY + target.height/2, target.posZ);
				world.spawnEntityInWorld(arc);
			}else{
				for(int i=0;i<8;i++){
					Wizardry.proxy.spawnParticle(EnumParticleType.SPARK, world, target.posX + world.rand.nextFloat() - 0.5, WizardryUtilities.getEntityFeetPos(target) + target.height/2 + world.rand.nextFloat()*2 - 1, target.posZ + world.rand.nextFloat() - 0.5, 0, 0, 0, 3);
					world.spawnParticle("largesmoke", target.posX + world.rand.nextFloat() - 0.5, WizardryUtilities.getEntityFeetPos(target) + target.height/2 + world.rand.nextFloat()*2 - 1, target.posZ + world.rand.nextFloat() - 0.5, 0, 0, 0);
				}
			}
			
			world.playSoundAtEntity(target, "wizardry:arc", 1.0F, world.rand.nextFloat() * 0.4F + 1.5F);
			
			if(MagicDamage.isEntityImmune(DamageType.SHOCK, target)){
				if(!world.isRemote) caster.addChatComponentMessage(new ChatComponentTranslation("spell.resist", target.getCommandSenderName(), this.getDisplayNameWithFormatting()));
			}else{
				target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.SHOCK), 10.0f * damageMultiplier);
			}
			
			// Secondary chaining effect
			double seekerRange = 5.0d;

			List<EntityLivingBase> secondaryTargets = WizardryUtilities.getEntitiesWithinRadius(seekerRange, target.posX, target.posY + target.height/2, target.posZ, world);

			for(int i=0; i<Math.min(secondaryTargets.size(), 5); i++){
				
				EntityLivingBase secondaryTarget = secondaryTargets.get(i);
				
				if(secondaryTarget != target && WizardryUtilities.isValidTarget(caster, secondaryTarget)){
					
					if(!world.isRemote){
						EntityArc arc = new EntityArc(world);
						arc.setEndpointCoords(target.posX, target.posY + target.height/2, target.posZ,
								secondaryTarget.posX, secondaryTarget.posY + secondaryTarget.height/2, secondaryTarget.posZ);
						world.spawnEntityInWorld(arc);
					}else{
						for(int j=0;j<8;j++){
							Wizardry.proxy.spawnParticle(EnumParticleType.SPARK, world, secondaryTarget.posX + world.rand.nextFloat() - 0.5, WizardryUtilities.getEntityFeetPos(secondaryTarget) + secondaryTarget.height/2 + world.rand.nextFloat()*2 - 1, secondaryTarget.posZ + world.rand.nextFloat() - 0.5, 0, 0, 0, 3);
							world.spawnParticle("largesmoke", secondaryTarget.posX + world.rand.nextFloat() - 0.5, WizardryUtilities.getEntityFeetPos(secondaryTarget) + secondaryTarget.height/2 + world.rand.nextFloat()*2 - 1, secondaryTarget.posZ + world.rand.nextFloat() - 0.5, 0, 0, 0);
						}
					}
					
					world.playSoundAtEntity(secondaryTarget, "wizardry:arc", 1.0F, world.rand.nextFloat() * 0.4F + 1.5F);
					
					if(MagicDamage.isEntityImmune(DamageType.SHOCK, secondaryTarget)){
						if(!world.isRemote) caster.addChatComponentMessage(new ChatComponentTranslation("spell.resist", secondaryTarget.getCommandSenderName(), this.getDisplayNameWithFormatting()));
					}else{
						secondaryTarget.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.SHOCK), 8.0f * damageMultiplier);
					}
					
					// Tertiary chaining effect

					List<EntityLivingBase> tertiaryTargets = WizardryUtilities.getEntitiesWithinRadius(seekerRange, secondaryTarget.posX, secondaryTarget.posY + secondaryTarget.height/2, secondaryTarget.posZ, world);

					for(int j=0;j<Math.min(tertiaryTargets.size(), 2);j++){
						
						EntityLivingBase tertiaryTarget = (EntityLivingBase)tertiaryTargets.get(j);

						if(tertiaryTarget != target && !secondaryTargets.contains(tertiaryTarget) && WizardryUtilities.isValidTarget(caster, tertiaryTarget)){

							if(!world.isRemote){
								EntityArc arc = new EntityArc(world);
								arc.setEndpointCoords(secondaryTarget.posX, secondaryTarget.posY + secondaryTarget.height/2, secondaryTarget.posZ,
										tertiaryTarget.posX, tertiaryTarget.posY + tertiaryTarget.height/2, tertiaryTarget.posZ);
								world.spawnEntityInWorld(arc);
							}else{
								for(int k=0;k<8;k++){
									Wizardry.proxy.spawnParticle(EnumParticleType.SPARK, world, tertiaryTarget.posX + world.rand.nextFloat() - 0.5, WizardryUtilities.getEntityFeetPos(tertiaryTarget) + tertiaryTarget.height/2 + world.rand.nextFloat()*2 - 1, tertiaryTarget.posZ + world.rand.nextFloat() - 0.5, 0, 0, 0, 3);
									world.spawnParticle("largesmoke", tertiaryTarget.posX + world.rand.nextFloat() - 0.5, WizardryUtilities.getEntityFeetPos(tertiaryTarget) + tertiaryTarget.height/2 + world.rand.nextFloat()*2 - 1, tertiaryTarget.posZ + world.rand.nextFloat() - 0.5, 0, 0, 0);
								}
							}
							
							world.playSoundAtEntity(tertiaryTarget, "wizardry:arc", 1.0F, world.rand.nextFloat() * 0.4F + 1.5F);
							
							if(MagicDamage.isEntityImmune(DamageType.SHOCK, tertiaryTarget)){
								if(!world.isRemote) caster.addChatComponentMessage(new ChatComponentTranslation("spell.resist", tertiaryTarget.getCommandSenderName(), this.getDisplayNameWithFormatting()));
							}else{
								tertiaryTarget.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.SHOCK), 6.0f * damageMultiplier);
							}
						}
					}
				}
			}

			caster.swingItem();
			return true;
		}
		return false;
	}


}
