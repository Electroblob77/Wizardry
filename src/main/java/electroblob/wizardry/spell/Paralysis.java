package electroblob.wizardry.spell;

import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.util.*;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class Paralysis extends SpellRay {

	/** Creatures with this much health or less will snap out of paralysis - but only when they take damage, so a
	 * creature on critical health may still be paralysed, but if it takes any damage at all the paralysis effect
	 * will end. */
	private static final String CRITICAL_HEALTH = "critical_health";

	public Paralysis(){
		super("paralysis", false, SpellActions.POINT);
		addProperties(DAMAGE, EFFECT_DURATION, CRITICAL_HEALTH);
	}

	@Override
	protected boolean onEntityHit(World world, Entity target, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){
		
		if(EntityUtils.isLiving(target)){
		
			if(world.isRemote){
				// Rather neatly, the entity can be set here and if it's null nothing will happen.
				ParticleBuilder.create(Type.BEAM).entity(caster).clr(0.2f, 0.6f, 1)
				.pos(caster != null ? origin.subtract(caster.getPositionVector()) : origin).target(target).spawn(world);
				ParticleBuilder.create(Type.LIGHTNING).entity(caster)
				.pos(caster != null ? origin.subtract(caster.getPositionVector()) : origin).target(target).spawn(world);
			}
	
			// This is a lot neater than it was, thanks to the damage type system.
			if(MagicDamage.isEntityImmune(DamageType.SHOCK, target)){
				if(!world.isRemote && caster instanceof EntityPlayer) ((EntityPlayer)caster).sendStatusMessage(
						new TextComponentTranslation("spell.resist",
						target.getName(), this.getNameForTranslationFormatted()), true);
			}else{
				target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.SHOCK),
						getProperty(DAMAGE).floatValue() * modifiers.get(SpellModifiers.POTENCY));
			}
			
			((EntityLivingBase)target).addPotionEffect(new PotionEffect(WizardryPotions.paralysis,
					(int)(getProperty(EFFECT_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)), 0));
		}
		
		return false;
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){
		
		if(world.isRemote){
			
			if(world.getBlockState(pos).getMaterial().isSolid()){
				Vec3d vec = hit.add(new Vec3d(side.getDirectionVec()).scale(GeometryUtils.ANTI_Z_FIGHTING_OFFSET));
				ParticleBuilder.create(Type.SCORCH).pos(vec).face(side).clr(0.4f, 0.8f, 1).spawn(world);
			}
		}
		
		return false;
	}

	@Override
	protected boolean onMiss(World world, EntityLivingBase caster, Vec3d origin, Vec3d direction, int ticksInUse, SpellModifiers modifiers){
		// This is first because we want the endpoint to be unaffected by the offset
		Vec3d endpoint = origin.add(direction.scale(getProperty(RANGE).floatValue() * modifiers.get(WizardryItems.range_upgrade)));

		if(world.isRemote){
			ParticleBuilder.create(Type.LIGHTNING).time(4).pos(origin).target(endpoint).scale(0.5f).spawn(world);
			ParticleBuilder.create(Type.BEAM).clr(0.2f, 0.6f, 1).time(4).pos(origin)
			.target(endpoint).spawn(world);
		}
		
		return true;
	}
	
	// See WizardryClientEventHandler for prevention of players' movement under the effects of paralysis
	
	// TODO: (Animated?) screen overlay effect for paralysed players in first-person
	
	@SubscribeEvent
	public static void onLivingUpdateEvent(LivingUpdateEvent event){
		// Disables entities' AI when under the effects of paralysis and re-enables it on the last update of the effect
		// - this can't be in the potion class because it requires access to the duration and hence the actual
		// PotionEffect instance
		if(event.getEntity() instanceof EntityLiving && event.getEntityLiving().isPotionActive(WizardryPotions.paralysis)){
			int timeLeft = event.getEntityLiving().getActivePotionEffect(WizardryPotions.paralysis).getDuration();
			((EntityLiving)event.getEntity()).setNoAI(timeLeft > 1);
		}
	}
	
	@SubscribeEvent
	public static void onLivingHurtEvent(LivingHurtEvent event){
		// Paralysed creatures snap out of paralysis when they take critical damage
		if(event.getEntityLiving().isPotionActive(WizardryPotions.paralysis) && event.getEntityLiving().getHealth()
				- event.getAmount() <= Spells.paralysis.getProperty(CRITICAL_HEALTH).floatValue()){
			event.getEntityLiving().removePotionEffect(WizardryPotions.paralysis);
		}
	}

}
