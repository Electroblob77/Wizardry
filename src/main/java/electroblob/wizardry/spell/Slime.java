package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.living.EntityMagicSlime;
import electroblob.wizardry.registry.WizardryAchievements;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryParticleType;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class Slime extends Spell {

	public Slime(){
		super(Tier.ADVANCED, 20, Element.EARTH, "slime", SpellType.ATTACK, 50, EnumAction.NONE, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		Vec3d look = caster.getLookVec();

		RayTraceResult rayTrace = WizardryUtilities.standardEntityRayTrace(world, caster,
				8 * modifiers.get(WizardryItems.range_upgrade));

		if(rayTrace != null && rayTrace.entityHit != null && WizardryUtilities.isLiving(rayTrace.entityHit)){

			EntityLivingBase target = (EntityLivingBase)rayTrace.entityHit;

			if(target instanceof EntitySlime){
				if(!world.isRemote) caster.sendMessage(new TextComponentTranslation("spell.resist", target.getName(),
						this.getNameForTranslationFormatted()));
			}else if(!(target instanceof EntityMagicSlime)){

				if(target instanceof EntitySkeleton) caster.addStat(WizardryAchievements.slime_skeleton);

				if(!world.isRemote){
					EntityMagicSlime slime = new EntityMagicSlime(world, caster, target,
							(int)(200 * modifiers.get(WizardryItems.duration_upgrade)));
					world.spawnEntity(slime);
				}
			}
		}

		if(world.isRemote){
			for(int i = 1; i < (int)(25 * modifiers.get(WizardryItems.range_upgrade)); i += 2){
				double x1 = caster.posX + look.x * i / 2 + world.rand.nextFloat() / 5 - 0.1f;
				double y1 = WizardryUtilities.getPlayerEyesPos(caster) - 0.4f + look.y * i / 2
						+ world.rand.nextFloat() / 5 - 0.1f;
				double z1 = caster.posZ + look.z * i / 2 + world.rand.nextFloat() / 5 - 0.1f;

				world.spawnParticle(EnumParticleTypes.SLIME, x1, y1, z1, 0.0d, 0.0d, 0.0d);
				Wizardry.proxy.spawnParticle(WizardryParticleType.DARK_MAGIC, world, x1, y1, z1, 0.0d, 0.0d, 0.0d, 0,
						0.2f, 0.8f, 0.1f);
			}
		}

		caster.swingArm(hand);
		WizardryUtilities.playSoundAtPlayer(caster, SoundEvents.ENTITY_SLIME_ATTACK, 1.0F, 0.5F);
		WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_ICE, 1.0F, 1.0F);
		return true;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target,
			SpellModifiers modifiers){

		if(target != null && !(target instanceof EntitySlime) && !(target instanceof EntityMagicSlime)){

			if(!world.isRemote){
				EntityMagicSlime slime = new EntityMagicSlime(world, caster, target,
						(int)(200 * modifiers.get(WizardryItems.duration_upgrade)));
				world.spawnEntity(slime);
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

					world.spawnParticle(EnumParticleTypes.SLIME, x1, y1, z1, 0.0d, 0.0d, 0.0d);
					Wizardry.proxy.spawnParticle(WizardryParticleType.DARK_MAGIC, world, x1, y1, z1, 0.0d, 0.0d, 0.0d,
							0, 0.2f, 0.8f, 0.1f);
				}
			}

			caster.swingArm(hand);
			caster.playSound(SoundEvents.ENTITY_SLIME_ATTACK, 1.0F, 0.5F);
			caster.playSound(WizardrySounds.SPELL_ICE, 1.0F, 1.0F);
			return true;

		}

		return false;
	}

	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

}
