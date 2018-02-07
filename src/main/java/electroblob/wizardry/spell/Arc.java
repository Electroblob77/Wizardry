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
import electroblob.wizardry.util.WizardryParticleType;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

// This spell was the 'guinea pig' for damage types, so to speak, so there's a bit of commentary on them here that may
// be useful for future reference.
public class Arc extends Spell {

	public Arc(){
		super(Tier.BASIC, 5, Element.LIGHTNING, "arc", SpellType.ATTACK, 15, EnumAction.NONE, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		RayTraceResult rayTrace = WizardryUtilities.standardEntityRayTrace(world, caster,
				8 * modifiers.get(WizardryItems.range_upgrade), 4.0f);

		if(rayTrace != null && rayTrace.entityHit != null && rayTrace.entityHit instanceof EntityLivingBase){

			Entity target = rayTrace.entityHit;

			if(!world.isRemote){
				EntityArc arc = new EntityArc(world);
				arc.setEndpointCoords(caster.posX, caster.posY + 1, caster.posZ, target.posX,
						target.posY + target.height / 2, target.posZ);
				world.spawnEntity(arc);
			}else{
				for(int i = 0; i < 8; i++){
					Wizardry.proxy.spawnParticle(WizardryParticleType.SPARK, world,
							target.posX + world.rand.nextFloat() - 0.5,
							target.getEntityBoundingBox().minY + target.height / 2 + world.rand.nextFloat() * 2 - 1,
							target.posZ + world.rand.nextFloat() - 0.5, 0, 0, 0, 3);
					world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, target.posX + world.rand.nextFloat() - 0.5,
							target.getEntityBoundingBox().minY + target.height / 2 + world.rand.nextFloat() * 2 - 1,
							target.posZ + world.rand.nextFloat() - 0.5, 0, 0, 0);
				}
			}

			// This is a lot neater than it was, thanks to the damage type system.
			if(MagicDamage.isEntityImmune(DamageType.SHOCK, target)){
				if(!world.isRemote) caster.sendMessage(new TextComponentTranslation("spell.resist", target.getName(),
						this.getNameForTranslationFormatted()));
			}else{
				target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.SHOCK),
						3.0f * modifiers.get(SpellModifiers.DAMAGE));
			}

			caster.swingArm(hand);
			target.playSound(WizardrySounds.SPELL_SPARK, 1.0F, world.rand.nextFloat() * 0.4F + 1.5F);
			return true;
		}

		return false;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target,
			SpellModifiers modifiers){

		if(target != null){

			if(!world.isRemote){
				EntityArc arc = new EntityArc(world);
				arc.setEndpointCoords(caster.posX, caster.posY + 1, caster.posZ, target.posX,
						target.posY + target.height / 2, target.posZ);
				world.spawnEntity(arc);
			}else{
				for(int i = 0; i < 8; i++){
					Wizardry.proxy.spawnParticle(WizardryParticleType.SPARK, world,
							target.posX + world.rand.nextFloat() - 0.5,
							target.getEntityBoundingBox().minY + target.height / 2 + world.rand.nextFloat() * 2 - 1,
							target.posZ + world.rand.nextFloat() - 0.5, 0, 0, 0, 3);
					world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, target.posX + world.rand.nextFloat() - 0.5,
							target.getEntityBoundingBox().minY + target.height / 2 + world.rand.nextFloat() * 2 - 1,
							target.posZ + world.rand.nextFloat() - 0.5, 0, 0, 0);
				}
			}

			// What's great about the damage type system is that, because I don't need to know if the creature resisted
			// the damage here, I can simply call this without having to check for immunities at all.
			target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.SHOCK),
					3.0f * modifiers.get(SpellModifiers.DAMAGE));

			caster.swingArm(hand);
			target.playSound(WizardrySounds.SPELL_SPARK, 1.0F, world.rand.nextFloat() * 0.4F + 1.5F);
			return true;
		}

		return false;
	}

	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

}
