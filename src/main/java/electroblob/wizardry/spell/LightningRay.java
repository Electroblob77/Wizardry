package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.EntityArc;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class LightningRay extends SpellRay {
	
	private static final float BASE_DAMAGE = 3;

	public LightningRay(){
		super("lightning_ray", Tier.APPRENTICE, Element.LIGHTNING, SpellType.ATTACK, 5, 0, true, 10, null);
	}
	
	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){
		// TODO: Temporary solution until I implement a better continuous sound system
		boolean flag = super.cast(world, caster, hand, ticksInUse, modifiers);
		if(flag){
			if(ticksInUse == 1){
				WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_LIGHTNING, 1, 1);
			}else if(ticksInUse > 0 && ticksInUse % 20 == 0){
				WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_LOOP_LIGHTNING, 1, 1);
			}
		}
		return flag;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target, SpellModifiers modifiers){
		boolean flag = super.cast(world, caster, hand, ticksInUse, target, modifiers);
		if(flag){
			if(ticksInUse == 1){
				caster.playSound(WizardrySounds.SPELL_LIGHTNING, 1, 1);
			}else if(ticksInUse > 0 && ticksInUse % 20 == 0){
				caster.playSound(WizardrySounds.SPELL_LOOP_LIGHTNING, 1, 1);
			}
		}
		return flag;
	}

	@Override
	protected boolean onEntityHit(World world, Entity target, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		
		if(WizardryUtilities.isLiving(target)){

			if(MagicDamage.isEntityImmune(DamageType.SHOCK, target)){
				if(!world.isRemote && ticksInUse == 1)
					caster.sendMessage(new TextComponentTranslation("spell.resist", target.getName(),
							this.getNameForTranslationFormatted()));
			}else{
				WizardryUtilities.attackEntityWithoutKnockback(target,
						MagicDamage.causeDirectMagicDamage(caster, DamageType.SHOCK),
						BASE_DAMAGE * modifiers.get(SpellModifiers.POTENCY));
			}
			
			if(!world.isRemote){
				
				if(ticksInUse % 2 == 0){

					EntityArc arc = new EntityArc(world);
					arc.setEndpointCoords(caster.posX, caster.posY + 1.2, caster.posZ, target.posX,
							target.posY + target.height / 2, target.posZ);
					arc.lifetime = 1;
					world.spawnEntity(arc);
				}

			}else{
				// Particle effect
				for(int i=0; i<5; i++){
					ParticleBuilder.create(Type.SPARK, target);
				}
			}
		}
		
		return false;
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onMiss(World world, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		// This is a nice example of when onMiss is used for more than just returning a boolean
		if(!world.isRemote){
			
			if(ticksInUse % 2 == 0){
				
				double freeRange = 0.8 * baseRange; // The arc does not reach full range when it has a free end

				EntityArc arc = new EntityArc(world);
				arc.setEndpointCoords(caster.posX, caster.posY + 1.2, caster.posZ,
						caster.posX + caster.getLookVec().x * freeRange,
						caster.posY + caster.getEyeHeight() + caster.getLookVec().y * freeRange,
						caster.posZ + caster.getLookVec().z * freeRange);
				arc.lifetime = 1;
				world.spawnEntity(arc);
				
			}
		}
		
		return true;
	}

}
