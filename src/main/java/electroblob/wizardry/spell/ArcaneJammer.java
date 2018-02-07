package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.living.EntityWizard;
import electroblob.wizardry.event.SpellCastEvent;
import electroblob.wizardry.registry.WizardryAchievements;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryParticleType;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class ArcaneJammer extends Spell {

	public ArcaneJammer(){
		super(Tier.ADVANCED, 30, Element.HEALING, "arcane_jammer", SpellType.ATTACK, 50, EnumAction.NONE, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		Vec3d look = caster.getLookVec();

		RayTraceResult rayTrace = WizardryUtilities.standardEntityRayTrace(world, caster,
				10 * modifiers.get(WizardryItems.range_upgrade));

		if(rayTrace != null && rayTrace.typeOfHit == RayTraceResult.Type.ENTITY
				&& rayTrace.entityHit instanceof EntityLivingBase){

			EntityLivingBase entity = (EntityLivingBase)rayTrace.entityHit;
			if(entity instanceof EntityWizard) caster.addStat(WizardryAchievements.jam_wizard);

			if(!world.isRemote){
				entity.addPotionEffect(new PotionEffect(WizardryPotions.arcane_jammer,
						(int)(300 * modifiers.get(WizardryItems.duration_upgrade)), 0));
			}
		}
		if(world.isRemote){
			for(int i = 1; i < (int)(25 * modifiers.get(WizardryItems.range_upgrade)); i += 2){
				double x1 = caster.posX + look.xCoord * i / 2 + world.rand.nextFloat() / 5 - 0.1f;
				double y1 = WizardryUtilities.getPlayerEyesPos(caster) - 0.4f + look.yCoord * i / 2
						+ world.rand.nextFloat() / 5 - 0.1f;
				double z1 = caster.posZ + look.zCoord * i / 2 + world.rand.nextFloat() / 5 - 0.1f;
				Wizardry.proxy.spawnParticle(WizardryParticleType.SPARKLE, world, x1, y1, z1, 0.0d, 0.0d, 0.0d,
						12 + world.rand.nextInt(8), 0.9f, 0.3f, 0.7f);
			}
		}
		caster.swingArm(hand);
		WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_DEFLECTION, 0.7F,
				world.rand.nextFloat() * 0.4F + 0.8F);
		return true;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target,
			SpellModifiers modifiers){

		if(target != null){
			if(!world.isRemote){
				target.addPotionEffect(new PotionEffect(WizardryPotions.arcane_jammer,
						(int)(300 * modifiers.get(WizardryItems.duration_upgrade)), 0));
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

					Wizardry.proxy.spawnParticle(WizardryParticleType.SPARKLE, world, x1, y1, z1, 0.0d, 0.0d, 0.0d,
							12 + world.rand.nextInt(8), 0.9f, 0.3f, 0.7f);
				}
			}
			caster.swingArm(hand);
			caster.playSound(WizardrySounds.SPELL_DEFLECTION, 0.7F, world.rand.nextFloat() * 0.4F + 0.8F);
		}

		return false;
	}

	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

	@SubscribeEvent
	public static void onSpellCastPreEvent(SpellCastEvent.Pre event){
		// Arcane jammer prevents spell casting.
		if(event.getEntityLiving().isPotionActive(WizardryPotions.arcane_jammer)) event.setCanceled(true);
	}

}
