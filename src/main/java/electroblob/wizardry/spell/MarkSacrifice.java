package electroblob.wizardry.spell;

import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.util.EntityUtils;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@EventBusSubscriber
public class MarkSacrifice extends SpellRay {

	private static final float DAMAGE_INCREASE_PER_LEVEL = 0.6f;

	public MarkSacrifice(){
		super("mark_sacrifice", SpellActions.POINT, false);
		this.soundValues(1, 1.1f, 0.2f);
		addProperties(EFFECT_DURATION, EFFECT_STRENGTH);
	}

	@Override
	protected boolean onEntityHit(World world, Entity target, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){
		
		if(EntityUtils.isLiving(target)){
			((EntityLivingBase)target).addPotionEffect(new PotionEffect(WizardryPotions.mark_of_sacrifice,
					(int)(getProperty(EFFECT_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)),
					getProperty(EFFECT_STRENGTH).intValue() + SpellBuff.getStandardBonusAmplifier(modifiers.get(SpellModifiers.POTENCY))));
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
		ParticleBuilder.create(Type.DARK_MAGIC).pos(x, y, z).clr(0xe90e48).spawn(world);
		ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).time(12 + world.rand.nextInt(8)).clr(0xff7bbb).spawn(world);
	}

	@SubscribeEvent
	public static void onLivingHurtEvent(LivingHurtEvent event){

		PotionEffect effect = event.getEntityLiving().getActivePotionEffect(WizardryPotions.mark_of_sacrifice);

		if(effect != null && event.getSource().isMagicDamage()){
			event.setAmount(event.getAmount() * (1 + (1 + effect.getAmplifier()) * DAMAGE_INCREASE_PER_LEVEL));
			event.getEntityLiving().removePotionEffect(WizardryPotions.mark_of_sacrifice);
		}
	}

}
