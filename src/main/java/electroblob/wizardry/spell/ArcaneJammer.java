package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.event.SpellCastEvent;
import electroblob.wizardry.event.SpellCastEvent.Source;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySpellcasterIllager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Field;
import java.util.Random;

@Mod.EventBusSubscriber
public class ArcaneJammer extends SpellRay {

	/** Random number generator used to coordinate whether spellcasting works or not. */
	private static final Random random = new Random();
	/** The number of ticks between updates of whether spellcasting works or not. */
	private static final int UPDATE_INTERVAL = 15;

	private static final Field spellTicks;

	static { // Yay more reflection
		spellTicks = ObfuscationReflectionHelper.findField(EntitySpellcasterIllager.class, "field_193087_b");
	}

	public ArcaneJammer(){
		super("arcane_jammer", false, SpellActions.POINT);
		this.soundValues(0.7f, 1, 0.4f);
		this.addProperties(EFFECT_DURATION, EFFECT_STRENGTH);
	}

	@Override
	protected boolean onEntityHit(World world, Entity target, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){
		
		if(WizardryUtilities.isLiving(target)){
			if(!world.isRemote){
				((EntityLivingBase)target).addPotionEffect(new PotionEffect(WizardryPotions.arcane_jammer,
						(int)(getProperty(EFFECT_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)),
						getProperty(EFFECT_STRENGTH).intValue() + (int)((modifiers.get(SpellModifiers.POTENCY) - 1)
								/ Constants.POTENCY_INCREASE_PER_TIER + 0.5f)));
			}
		}
		
		return true;
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onMiss(World world, EntityLivingBase caster, Vec3d origin, Vec3d direction, int ticksInUse, SpellModifiers modifiers){
		return true;
	}
	
	@Override
	protected void spawnParticle(World world, double x, double y, double z, double vx, double vy, double vz){
		ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).time(12 + world.rand.nextInt(8)).clr(0.9f, 0.3f, 0.7f)
		.spawn(world);
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST) // Prevents all spells so it comes before everything else
	public static void onSpellCastPreEvent(SpellCastEvent.Pre event){
		// Arcane jammer has a chance to prevent spell casting
		// We can't just use a straight-up random number because otherwise people can spam it to get it to cast
		// Instead, we're using the world time to make blocks of time when spells will and won't work
		random.setSeed(event.getWorld().getTotalWorldTime() / UPDATE_INTERVAL);
		// For some unfathomable reason, the first call to this after setting the seed remains the same for long
		// sequences of consecutive seeds, so let's clear it out first to get to a more changeable value
		random.nextInt(2);

		if(event.getCaster() != null && event.getCaster().isPotionActive(WizardryPotions.arcane_jammer)
				// Arcane jammer I has a 1/2 chance, level II has a 2/3 chance, and so on
				&& random.nextInt(event.getCaster().getActivePotionEffect(WizardryPotions.arcane_jammer).getAmplifier() + 2) > 0){

			event.setCanceled(true);

			// TODO: This currently doesn't play nicely with continuous or charge-up spells
			if(event.getSource() == Source.WAND || event.getSource() == Source.SCROLL){
				event.getCaster().setActiveHand(EnumHand.MAIN_HAND);
			}

			// Because we're using a seed that should be consistent, we can do client-side stuff!
			event.getWorld().playSound(event.getCaster().posX, event.getCaster().posY, event.getCaster().posZ,
					WizardrySounds.MISC_SPELL_FAIL, WizardrySounds.SPELLS, 1, 1, false);

			if(event.getWorld().isRemote){

				Vec3d centre = event.getCaster().getPositionEyes(0).add(event.getCaster().getLookVec());

				for(int i = 0; i < 5; i++){
					double x = centre.x + 0.5f * (event.getWorld().rand.nextFloat() - 0.5f);
					double y = centre.y + 0.5f * (event.getWorld().rand.nextFloat() - 0.5f);
					double z = centre.z + 0.5f * (event.getWorld().rand.nextFloat() - 0.5f);
					event.getWorld().spawnParticle(EnumParticleTypes.SMOKE_LARGE, x, y, z, 0, 0, 0);
				}
			}
		}
	}

	@SubscribeEvent
	public static void onLivingUpdateEvent(LivingEvent.LivingUpdateEvent event){

		if(event.getEntity() instanceof EntitySpellcasterIllager
				&& event.getEntityLiving().isPotionActive(WizardryPotions.arcane_jammer)){

			((EntitySpellcasterIllager)event.getEntity()).setSpellType(EntitySpellcasterIllager.SpellType.NONE);

			try{
				spellTicks.set(event.getEntity(), 10);
			}catch(IllegalAccessException e){
				Wizardry.logger.error("Error setting evoker spell timer:", e);
			}
		}
	}

}
