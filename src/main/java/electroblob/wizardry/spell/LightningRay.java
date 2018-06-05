package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.EntityArc;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class LightningRay extends Spell {

	public LightningRay(){
		super(Tier.APPRENTICE, 5, Element.LIGHTNING, "lightning_ray", SpellType.ATTACK, 0, EnumAction.NONE, true);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		RayTraceResult rayTrace = WizardryUtilities.standardEntityRayTrace(world, caster,
				10 * modifiers.get(WizardryItems.range_upgrade), 2.0f);

		if(rayTrace != null && rayTrace.typeOfHit == RayTraceResult.Type.ENTITY && WizardryUtilities.isLiving(rayTrace.entityHit)){
			Entity target = rayTrace.entityHit;
			if(!world.isRemote){
				// This statement means the arc only spawns every other tick.
				if(ticksInUse % 2 == 0){

					EntityArc arc = new EntityArc(world);
					// The look vec stuff performs a translation on the start point to line it up with the wand.
					// EDIT: removed due to 1st/3rd person render differences.
					arc.setEndpointCoords(caster.posX, caster.posY + 1.2, caster.posZ, target.posX,
							target.posY + target.height / 2, target.posZ);

					arc.lifetime = 1;

					world.spawnEntity(arc);
				}

				if(MagicDamage.isEntityImmune(DamageType.SHOCK, target)){
					if(!world.isRemote && ticksInUse == 1)
						caster.sendMessage(new TextComponentTranslation("spell.resist", target.getName(),
								this.getNameForTranslationFormatted()));
				}else{
					WizardryUtilities.attackEntityWithoutKnockback(target,
							MagicDamage.causeDirectMagicDamage(caster, DamageType.SHOCK),
							3.0f * modifiers.get(SpellModifiers.DAMAGE));
				}

			}else{
				for(int i = 0; i < 5; i++){
					Wizardry.proxy.spawnParticle(Type.SPARK, world,
							target.posX + world.rand.nextFloat() - 0.5,
							target.getEntityBoundingBox().minY + target.height / 2 + world.rand.nextFloat() * 2 - 1,
							target.posZ + world.rand.nextFloat() - 0.5, 0, 0, 0, 3);
				}
			}

			if(ticksInUse == 1){
				WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_LIGHTNING, 1.0F, 1.0f);
			}else if(ticksInUse > 0 && ticksInUse % 20 == 0){
				WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_LOOP_LIGHTNING, 1.0F, 1.0f);
			}

			return true;

		}else{
			if(!world.isRemote){
				// This statement means the arc only spawns every other tick.
				if(ticksInUse % 2 == 0){

					EntityArc arc = new EntityArc(world);

					arc.setEndpointCoords(caster.posX, caster.posY + 1.2, caster.posZ,
							caster.posX + caster.getLookVec().x * 8,
							caster.posY + caster.eyeHeight + caster.getLookVec().y * 8,
							caster.posZ + caster.getLookVec().z * 8);

					arc.lifetime = 1;

					world.spawnEntity(arc);
				}
			}

			if(ticksInUse == 1){
				WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_LIGHTNING, 1.0F, 1.0f);
			}else if(ticksInUse > 0 && ticksInUse % 20 == 0){
				WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_LOOP_LIGHTNING, 1.0F, 1.0f);
			}

			return true;
		}
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target,
			SpellModifiers modifiers){

		if(target != null){
			if(!world.isRemote){
				// This statement means the arc only spawns every other tick.
				if(ticksInUse % 2 == 0){

					EntityArc arc = new EntityArc(world);
					// The look vec stuff performs a translation on the start point to line it up with the wand.
					// EDIT: removed due to 1st/3rd person render differences.
					arc.setEndpointCoords(caster.posX, caster.posY + 1.2, caster.posZ, target.posX,
							target.posY + target.height / 2, target.posZ);

					arc.lifetime = 1;

					world.spawnEntity(arc);
				}

				WizardryUtilities.attackEntityWithoutKnockback(target,
						MagicDamage.causeDirectMagicDamage(caster, DamageType.SHOCK),
						3.0f * modifiers.get(SpellModifiers.DAMAGE));

			}else{
				for(int i = 0; i < 5; i++){
					Wizardry.proxy.spawnParticle(Type.SPARK, world,
							target.posX + world.rand.nextFloat() - 0.5,
							target.getEntityBoundingBox().minY + target.height / 2 + world.rand.nextFloat() * 2 - 1,
							target.posZ + world.rand.nextFloat() - 0.5, 0, 0, 0, 3);
				}
			}

			if(ticksInUse == 1){
				caster.playSound(WizardrySounds.SPELL_LIGHTNING, 1.0F, 1.0f);
			}else if(ticksInUse > 0 && ticksInUse % 20 == 0){
				caster.playSound(WizardrySounds.SPELL_LOOP_LIGHTNING, 1.0F, 1.0f);
			}

			return true;
		}

		return false;
	}

	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

}
