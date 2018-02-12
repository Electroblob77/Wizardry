package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryParticleType;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class Wither extends Spell {

	public Wither(){
		super(Tier.APPRENTICE, 10, Element.NECROMANCY, "wither", SpellType.ATTACK, 20, EnumAction.NONE, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		Vec3d look = caster.getLookVec();

		RayTraceResult rayTrace = WizardryUtilities.standardEntityRayTrace(world, caster,
				10 * modifiers.get(WizardryItems.range_upgrade));

		if(rayTrace != null && rayTrace.typeOfHit == RayTraceResult.Type.ENTITY
				&& rayTrace.entityHit instanceof EntityLivingBase){

			EntityLivingBase target = (EntityLivingBase)rayTrace.entityHit;

			// Has no effect on withers or wither skeletons.
			if(MagicDamage.isEntityImmune(DamageType.WITHER, target)){
				if(!world.isRemote) caster.sendMessage(new TextComponentTranslation("spell.resist", target.getName(),
						this.getNameForTranslationFormatted()));
			}else{
				target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.WITHER),
						1.0f * modifiers.get(SpellModifiers.DAMAGE));
				target.addPotionEffect(new PotionEffect(MobEffects.WITHER,
						(int)(200 * modifiers.get(WizardryItems.duration_upgrade)), 1));
			}
		}
		if(world.isRemote){
			for(int i = 1; i < (int)(25 * modifiers.get(WizardryItems.range_upgrade)); i += 2){
				// I figured it out! when on client side, entityplayer.posY is at the eyes, not the feet!
				// This is a test for lining up the ray with the wand tip. Not sure if I like it or not.
				/* Vec3d origin = Wizardry.proxy.getWandTipPosition(caster); double x1 = origin.xCoord + look.xCoord*i/2
				 * + world.rand.nextFloat()/5 - 0.1f; double y1 = origin.yCoord + look.yCoord*i/2 +
				 * world.rand.nextFloat()/5 - 0.1f; double z1 = origin.zCoord + look.zCoord*i/2 +
				 * world.rand.nextFloat()/5 - 0.1f; */
				double x1 = caster.posX + look.x * i / 2 + world.rand.nextFloat() / 5 - 0.1f;
				double y1 = WizardryUtilities.getPlayerEyesPos(caster) - 0.4f + look.y * i / 2
						+ world.rand.nextFloat() / 5 - 0.1f;
				double z1 = caster.posZ + look.z * i / 2 + world.rand.nextFloat() / 5 - 0.1f;
				// world.spawnParticle("mobSpell", x1, y1, z1, -1*look.xCoord, -1*look.yCoord, -1*look.zCoord);
				Wizardry.proxy.spawnParticle(WizardryParticleType.DARK_MAGIC, world, x1, y1, z1, 0.0d, 0.0d, 0.0d, 0,
						0.1f, 0.0f, 0.0f);
				Wizardry.proxy.spawnParticle(WizardryParticleType.SPARKLE, world, x1, y1, z1, 0.0d, 0.0d, 0.0d,
						12 + world.rand.nextInt(8), 0.1f, 0.0f, 0.05f);
			}
		}
		caster.swingArm(hand);
		WizardryUtilities.playSoundAtPlayer(caster, SoundEvents.ENTITY_WITHER_HURT, 1.0F,
				world.rand.nextFloat() * 0.2F + 1.0F);
		return true;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target,
			SpellModifiers modifiers){

		if(target != null){
			// Has no effect on withers or wither skeletons.
			if(!MagicDamage.isEntityImmune(DamageType.WITHER, target) && !world.isRemote){
				target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.WITHER),
						1.0f * modifiers.get(SpellModifiers.DAMAGE));
				target.addPotionEffect(new PotionEffect(MobEffects.WITHER,
						(int)(200 * modifiers.get(WizardryItems.duration_upgrade)), 1));
			}

			if(world.isRemote){

				double dx = (target.posX - caster.posX) / caster.getDistanceToEntity(target);
				double dy = (target.posY - caster.posY) / caster.getDistanceToEntity(target);
				double dz = (target.posZ - caster.posZ) / caster.getDistanceToEntity(target);

				for(int i = 1; i < (int)(25 * modifiers.get(WizardryItems.range_upgrade)); i += 2){

					double x1 = caster.posX + dx * i / 2 + world.rand.nextFloat() / 5 - 0.1f;
					double y1 = caster.posY + caster.getEyeHeight() - 0.4f + dy * i / 2 + world.rand.nextFloat() / 5
							- 0.1f;
					double z1 = caster.posZ + dz * i / 2 + world.rand.nextFloat() / 5 - 0.1f;

					Wizardry.proxy.spawnParticle(WizardryParticleType.DARK_MAGIC, world, x1, y1, z1, 0.0d, 0.0d, 0.0d,
							0, 0.1f, 0.0f, 0.0f);
					Wizardry.proxy.spawnParticle(WizardryParticleType.SPARKLE, world, x1, y1, z1, 0.0d, 0.0d, 0.0d,
							12 + world.rand.nextInt(8), 0.1f, 0.0f, 0.05f);
				}
			}
			caster.swingArm(hand);
			caster.playSound(SoundEvents.ENTITY_WITHER_HURT, 1.0F, world.rand.nextFloat() * 0.2F + 1.0F);
			return true;
		}

		return false;
	}

	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

}
