package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.construct.EntityBubble;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.ParticleBuilder.Type;
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

public class Entrapment extends Spell {

	public Entrapment(){
		super(Tier.ADVANCED, 35, Element.NECROMANCY, "entrapment", SpellType.ATTACK, 75, EnumAction.NONE, false);
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

				EntityBubble entitybubble = new EntityBubble(world, entity.posX, entity.posY, entity.posZ, caster,
						(int)(200 * modifiers.get(WizardryItems.duration_upgrade)), true,
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

				world.spawnParticle(EnumParticleTypes.PORTAL, x1, y1 - 0.5, z1, 0.0d, 0.0d, 0.0d);
				Wizardry.proxy.spawnParticle(Type.DARK_MAGIC, world, x1, y1, z1, 0.0d, 0.0d, 0.0d, 0,
						0.1f, 0.0f, 0.0f);
			}
		}
		caster.swingArm(hand);
		WizardryUtilities.playSoundAtPlayer(caster, SoundEvents.ENTITY_WITHER_SHOOT, 1.0F,
				world.rand.nextFloat() * 0.3F + 0.7F);
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
						(int)(200 * modifiers.get(WizardryItems.duration_upgrade)), true,
						modifiers.get(SpellModifiers.DAMAGE));
				world.spawnEntity(entitybubble);
				target.startRiding(entitybubble);

			}
			if(world.isRemote){

				double dx = (target.posX - caster.posX) / caster.getDistance(target);
				double dy = (target.posY - caster.posY) / caster.getDistance(target);
				double dz = (target.posZ - caster.posZ) / caster.getDistance(target);

				for(int i = 1; i < 25; i += 2){

					double x1 = caster.posX + dx * i / 2 + world.rand.nextFloat() / 5 - 0.1f;
					double y1 = caster.posY + caster.getEyeHeight() - 0.4f + dy * i / 2 + world.rand.nextFloat() / 5
							- 0.1f;
					double z1 = caster.posZ + dz * i / 2 + world.rand.nextFloat() / 5 - 0.1f;

					world.spawnParticle(EnumParticleTypes.PORTAL, x1, y1 - 0.5, z1, 0.0d, 0.0d, 0.0d);
					Wizardry.proxy.spawnParticle(Type.DARK_MAGIC, world, x1, y1, z1, 0.0d, 0.0d, 0.0d,
							0, 0.1f, 0.0f, 0.0f);
				}
			}
			caster.swingArm(hand);
			caster.playSound(SoundEvents.ENTITY_WITHER_SHOOT, 1.0F, world.rand.nextFloat() * 0.3F + 0.7F);
			return true;
		}

		return false;
	}

	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

}
