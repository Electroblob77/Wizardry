package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryParticleType;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class FrostRay extends Spell {

	public FrostRay(){
		super(Tier.APPRENTICE, 5, Element.ICE, "frost_ray", SpellType.ATTACK, 0, EnumAction.NONE, true);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		Vec3d look = caster.getLookVec();

		RayTraceResult rayTrace = WizardryUtilities.standardEntityRayTrace(world, caster,
				10 * modifiers.get(WizardryItems.range_upgrade));

		if(rayTrace != null && rayTrace.typeOfHit == RayTraceResult.Type.ENTITY
				&& rayTrace.entityHit instanceof EntityLivingBase){

			EntityLivingBase target = (EntityLivingBase)rayTrace.entityHit;

			if(target.isBurning()){
				target.extinguish();
			}

			if(MagicDamage.isEntityImmune(DamageType.FROST, target)){
				if(!world.isRemote && ticksInUse == 1) caster.sendMessage(new TextComponentTranslation("spell.resist",
						target.getName(), this.getNameForTranslationFormatted()));
			}else{
				// For frost ray the entity can move slightly, unlike freeze.
				target.addPotionEffect(new PotionEffect(WizardryPotions.frost,
						(int)(200 * modifiers.get(WizardryItems.duration_upgrade)), 0));

				float baseDamage = target instanceof EntityBlaze || target instanceof EntityMagmaCube ? 6.0f : 3.0f;
				WizardryUtilities.attackEntityWithoutKnockback(target,
						MagicDamage.causeDirectMagicDamage(caster, DamageType.FROST),
						baseDamage * modifiers.get(SpellModifiers.DAMAGE));

			}
		}

		if(world.isRemote){
			for(int i = 0; i < 20; i++){
				double x1 = caster.posX + look.xCoord * i / 2 + world.rand.nextFloat() / 5 - 0.1f;
				double y1 = WizardryUtilities.getPlayerEyesPos(caster) - 0.4f + look.yCoord * i / 2
						+ world.rand.nextFloat() / 5 - 0.1f;
				double z1 = caster.posZ + look.zCoord * i / 2 + world.rand.nextFloat() / 5 - 0.1f;
				Wizardry.proxy.spawnParticle(WizardryParticleType.SPARKLE, world, x1, y1, z1,
						look.xCoord * modifiers.get(WizardryItems.range_upgrade),
						look.yCoord * modifiers.get(WizardryItems.range_upgrade),
						look.zCoord * modifiers.get(WizardryItems.range_upgrade), 8 + world.rand.nextInt(12), 0.4f,
						0.6f, 1.0f);

				x1 = caster.posX + look.xCoord * i / 2 + world.rand.nextFloat() / 5 - 0.1f;
				y1 = WizardryUtilities.getPlayerEyesPos(caster) - 0.4f + look.yCoord * i / 2
						+ world.rand.nextFloat() / 5 - 0.1f;
				z1 = caster.posZ + look.zCoord * i / 2 + world.rand.nextFloat() / 5 - 0.1f;
				Wizardry.proxy.spawnParticle(WizardryParticleType.SPARKLE, world, x1, y1, z1,
						look.xCoord * modifiers.get(WizardryItems.range_upgrade),
						look.yCoord * modifiers.get(WizardryItems.range_upgrade),
						look.zCoord * modifiers.get(WizardryItems.range_upgrade), 8 + world.rand.nextInt(12), 1.0f,
						1.0f, 1.0f);
			}
		}

		if(ticksInUse % 12 == 0){
			if(ticksInUse == 0) WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_ICE, 0.5F, 1.0f);
			WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_LOOP_ICE, 0.5F, 1.0f);
		}
		return true;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target,
			SpellModifiers modifiers){

		if(target != null){

			Vec3d vec = new Vec3d(target.posX - caster.posX, target.posY - caster.posY, target.posZ - caster.posZ)
					.normalize();

			if(target.isBurning()){
				target.extinguish();
			}

			if(!MagicDamage.isEntityImmune(DamageType.FROST, target)){
				// For frost ray the entity can move slightly, unlike freeze.
				target.addPotionEffect(new PotionEffect(WizardryPotions.frost,
						(int)(200 * modifiers.get(WizardryItems.duration_upgrade)), 0));

				float baseDamage = target instanceof EntityBlaze || target instanceof EntityMagmaCube ? 6.0f : 3.0f;
				WizardryUtilities.attackEntityWithoutKnockback(target,
						MagicDamage.causeDirectMagicDamage(caster, DamageType.FROST),
						baseDamage * modifiers.get(SpellModifiers.DAMAGE));

			}

			if(world.isRemote){
				for(int i = 0; i < 20; i++){
					double x1 = caster.posX + vec.xCoord * i / 2 + world.rand.nextFloat() / 5 - 0.1f;
					double y1 = caster.posY + caster.getEyeHeight() - 0.4f + vec.yCoord * i / 2
							+ world.rand.nextFloat() / 5 - 0.1f;
					double z1 = caster.posZ + vec.zCoord * i / 2 + world.rand.nextFloat() / 5 - 0.1f;
					Wizardry.proxy.spawnParticle(WizardryParticleType.SPARKLE, world, x1, y1, z1,
							vec.xCoord * modifiers.get(WizardryItems.range_upgrade),
							vec.yCoord * modifiers.get(WizardryItems.range_upgrade),
							vec.zCoord * modifiers.get(WizardryItems.range_upgrade), 8 + world.rand.nextInt(12), 0.4f,
							0.6f, 1.0f);

					x1 = caster.posX + vec.xCoord * i / 2 + world.rand.nextFloat() / 5 - 0.1f;
					y1 = caster.posY + caster.getEyeHeight() - 0.4f + vec.yCoord * i / 2 + world.rand.nextFloat() / 5
							- 0.1f;
					z1 = caster.posZ + vec.zCoord * i / 2 + world.rand.nextFloat() / 5 - 0.1f;
					Wizardry.proxy.spawnParticle(WizardryParticleType.SPARKLE, world, x1, y1, z1,
							vec.xCoord * modifiers.get(WizardryItems.range_upgrade),
							vec.yCoord * modifiers.get(WizardryItems.range_upgrade),
							vec.zCoord * modifiers.get(WizardryItems.range_upgrade), 8 + world.rand.nextInt(12), 1.0f,
							1.0f, 1.0f);
				}
			}

			if(ticksInUse % 12 == 0){
				if(ticksInUse == 0) caster.playSound(WizardrySounds.SPELL_ICE, 0.5F, 1.0f);
				caster.playSound(WizardrySounds.SPELL_LOOP_ICE, 0.5F, 1.0f);
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
