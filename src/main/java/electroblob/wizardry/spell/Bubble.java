package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.construct.EntityBubble;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryParticleType;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class Bubble extends Spell {

	public Bubble(){
		super(Tier.APPRENTICE, 15, Element.EARTH, "bubble", SpellType.ATTACK, 20, EnumAction.NONE, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		Vec3d look = caster.getLookVec();

		RayTraceResult rayTrace = WizardryUtilities.standardEntityRayTrace(world, caster,
				10 * modifiers.get(WizardryItems.range_upgrade));

		if(rayTrace != null && rayTrace.typeOfHit == RayTraceResult.Type.ENTITY && WizardryUtilities.isLiving(rayTrace.entityHit)){
			EntityLivingBase entity = (EntityLivingBase)rayTrace.entityHit;
			if(!world.isRemote){
				entity.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.MAGIC),
						1.0f * modifiers.get(SpellModifiers.DAMAGE));
				// Deprecated in favour of entity riding method
				// entity.addPotionEffect(new PotionEffect(Wizardry.bubblePotion, 200, 0));
				EntityBubble entitybubble = new EntityBubble(world, entity.posX, entity.posY, entity.posZ, caster,
						(int)(200 * modifiers.get(WizardryItems.duration_upgrade)), false,
						modifiers.get(SpellModifiers.DAMAGE));
				world.spawnEntity(entitybubble);
				entity.startRiding(entitybubble);
			}
		}
		if(world.isRemote){
			for(int i = 1; i < (int)(25 * modifiers.get(WizardryItems.range_upgrade)); i += 2){
				double x1 = caster.posX + look.x * i / 2 + world.rand.nextFloat() / 5 - 0.1f;
				double y1 = WizardryUtilities.getPlayerEyesPos(caster) - 0.4f + look.y * i / 2
						+ world.rand.nextFloat() / 5 - 0.1f;
				double z1 = caster.posZ + look.z * i / 2 + world.rand.nextFloat() / 5 - 0.1f;

				world.spawnParticle(EnumParticleTypes.WATER_SPLASH, x1, y1, z1, 0.0d, 0.0d, 0.0d);
				Wizardry.proxy.spawnParticle(WizardryParticleType.MAGIC_BUBBLE, world, x1, y1, z1, 0.0d, 0.0d, 0.0d, 0);
			}
		}
		caster.swingArm(hand);
		WizardryUtilities.playSoundAtPlayer(caster, SoundEvents.ENTITY_GENERIC_SWIM, 1.0F,
				world.rand.nextFloat() * 0.2F + 1.0F);
		WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_ICE, 0.5F,
				world.rand.nextFloat() * 0.2F + 1.0F);
		return true;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target,
			SpellModifiers modifiers){

		if(target != null){

			if(!world.isRemote){
				target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.MAGIC),
						1.0f * modifiers.get(SpellModifiers.DAMAGE));
				// Deprecated in favour of entity riding method
				// entity.addPotionEffect(new PotionEffect(Wizardry.bubblePotion, 200, 0));
				EntityBubble entitybubble = new EntityBubble(world, target.posX, target.posY, target.posZ, caster,
						(int)(200 * modifiers.get(WizardryItems.duration_upgrade)), false,
						modifiers.get(SpellModifiers.DAMAGE));
				world.spawnEntity(entitybubble);
				target.startRiding(entitybubble);

			}
			if(world.isRemote){

				double dx = (target.posX - caster.posX) / caster.getDistance(target);
				double dy = (target.posY - caster.posY) / caster.getDistance(target);
				double dz = (target.posZ - caster.posZ) / caster.getDistance(target);

				for(int i = 1; i < (int)(25 * modifiers.get(WizardryItems.range_upgrade)); i += 2){

					double x1 = caster.posX + dx * i / 2 + world.rand.nextFloat() / 5 - 0.1f;
					double y1 = caster.posY + caster.getEyeHeight() - 0.4f + dy * i / 2 + world.rand.nextFloat() / 5
							- 0.1f;
					double z1 = caster.posZ + dz * i / 2 + world.rand.nextFloat() / 5 - 0.1f;

					world.spawnParticle(EnumParticleTypes.WATER_SPLASH, x1, y1, z1, 0.0d, 0.0d, 0.0d);
					Wizardry.proxy.spawnParticle(WizardryParticleType.MAGIC_BUBBLE, world, x1, y1, z1, 0.0d, 0.0d, 0.0d,
							0);
				}
			}
			caster.swingArm(hand);
			caster.playSound(SoundEvents.ENTITY_GENERIC_SWIM, 1.0F, world.rand.nextFloat() * 0.2F + 1.0F);
			caster.playSound(WizardrySounds.SPELL_ICE, 0.5F, world.rand.nextFloat() * 0.2F + 1.0F);
			return true;
		}

		return false;
	}

	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

}
