package electroblob.wizardry.spell;

import electroblob.wizardry.integration.DamageSafetyChecker;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.util.*;
import electroblob.wizardry.util.MagicDamage.DamageType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class ShadowWard extends Spell {

	public static final String REFLECTED_FRACTION = "reflected_fraction";

	public ShadowWard(){
		super("shadow_ward", EnumAction.BLOCK, true);
		addProperties(REFLECTED_FRACTION);
		soundValues(0.6f, 1, 0);
	}

	@Override
	protected SoundEvent[] createSounds(){
		return this.createContinuousSpellSounds();
	}

	@Override
	protected void playSound(World world, EntityLivingBase entity, int ticksInUse, int duration, SpellModifiers modifiers, String... sounds){
		this.playSoundLoop(world, entity, ticksInUse);
	}

	@Override
	protected void playSound(World world, double x, double y, double z, int ticksInUse, int duration, SpellModifiers modifiers, String... sounds){
		this.playSoundLoop(world, x, y, z, ticksInUse, duration);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		if(world.isRemote){
			double dx = -1 + 2 * world.rand.nextFloat();
			double dy = -1 + world.rand.nextFloat();
			double dz = -1 + 2 * world.rand.nextFloat();
			world.spawnParticle(EnumParticleTypes.PORTAL, caster.posX, caster.posY + caster.getEyeHeight(), caster.posZ, dx, dy, dz);
		}

		if(ticksInUse % 50 == 0){
			this.playSound(world, caster, ticksInUse, -1, modifiers);
		}

		return true;
	}

	@SubscribeEvent
	public static void onLivingAttackEvent(LivingAttackEvent event){
		
		if(event.getSource() != null && event.getSource().getTrueSource() instanceof EntityLivingBase){

			if(EntityUtils.isCasting(event.getEntityLiving(), Spells.shadow_ward) && !event.getSource().isUnblockable()
					&& !(event.getSource() instanceof IElementalDamage && ((IElementalDamage)event.getSource()).isRetaliatory())){

				event.setCanceled(true);

				float reflectedFraction = MathHelper.clamp(Spells.shadow_ward.getProperty(REFLECTED_FRACTION).floatValue(), 0, 1);

				// Now we can preserve the original damage source (sort of) as long as we make it retaliatory.
				// For some reason this isn't working, so I've reverted to plain old magic damage for now.
				//event.getEntityLiving().attackEntityFrom(
				//		MagicDamage.causeDirectMagicDamage(event.getSource().getTrueSource(), DamageType.MAGIC, true), event.getAmount() * 0.5f);
				DamageSafetyChecker.attackEntitySafely(event.getEntity(), DamageSource.MAGIC, event.getAmount()
						* (1 - reflectedFraction), event.getSource().getDamageType());
				event.getSource().getTrueSource().attackEntityFrom(MagicDamage.causeDirectMagicDamage(
						event.getEntityLiving(), DamageType.MAGIC, true), event.getAmount() * reflectedFraction);
			}
		}
	}

}
