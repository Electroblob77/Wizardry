package electroblob.wizardry.spell;

import electroblob.wizardry.event.SpellCastEvent;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySpellcasterIllager;
import net.minecraft.item.EnumAction;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class ArcaneJammer extends SpellRay {

	public ArcaneJammer(){
		super("arcane_jammer", false, EnumAction.NONE);
		this.soundValues(0.7f, 1, 0.4f);
		this.addProperties(EFFECT_DURATION);
	}

	@Override
	protected boolean onEntityHit(World world, Entity target, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){
		
		if(WizardryUtilities.isLiving(target)){
			
			if(!world.isRemote){
				((EntityLivingBase)target).addPotionEffect(new PotionEffect(WizardryPotions.arcane_jammer,
						(int)(getProperty(EFFECT_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)), 0));
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
		// Arcane jammer prevents spell casting.
		if(event.getCaster() != null && event.getCaster().isPotionActive(WizardryPotions.arcane_jammer)) event.setCanceled(true);
	}

	@SubscribeEvent
	public static void onLivingUpdateEvent(LivingEvent.LivingUpdateEvent event){
		if(event.getEntity() instanceof EntitySpellcasterIllager
				&& event.getEntityLiving().isPotionActive(WizardryPotions.arcane_jammer)){
			((EntitySpellcasterIllager)event.getEntity()).setSpellType(EntitySpellcasterIllager.SpellType.NONE);
		}
	}

}
