package electroblob.wizardry.spell;

import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class MindTrick extends SpellRay {

	public MindTrick(){
		super("mind_trick", false, SpellActions.POINT);
		this.soundValues(0.7f, 1, 0.4f);
		addProperties(EFFECT_DURATION);
	}

	@Override
	protected boolean onEntityHit(World world, Entity target, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){
		
		if(WizardryUtilities.isLiving(target)){

			if(!world.isRemote){

				if(target instanceof EntityPlayer){

					((EntityLivingBase)target).addPotionEffect(new PotionEffect(MobEffects.NAUSEA,
							(int)(getProperty(EFFECT_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)), 0));

				}else if(target instanceof EntityLiving){

					((EntityLiving)target).setAttackTarget(null);
					((EntityLivingBase)target).addPotionEffect(new PotionEffect(WizardryPotions.mind_trick,
							(int)(getProperty(EFFECT_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)), 0));
				}
				
			}else{
				for(int i=0; i<10; i++){
					ParticleBuilder.create(Type.DARK_MAGIC, world.rand, target.posX,
							target.getEntityBoundingBox().minY + target.getEyeHeight(), target.posZ, 0.25, false)
					.clr(0.8f, 0.2f, 1.0f).spawn(world);
				}
			}
			
			return true;
		}
		
		return false;
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onMiss(World world, EntityLivingBase caster, Vec3d origin, Vec3d direction, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@SubscribeEvent
	public static void onLivingAttackEvent(LivingAttackEvent event){
		if(event.getSource() != null && event.getSource().getTrueSource() instanceof EntityLivingBase){
			// Cancels the mind trick effect if the creature takes damage
			// This has been moved to within an (event.getSource().getEntity() instanceof EntityLivingBase) check so it
			// doesn't crash the game with a ConcurrentModificationException. If you think about it, mind trick only
			// ought to be cancelled if something attacks the entity since potions, drowning, cacti etc. don't affect the
			// targeting.
			if(event.getEntityLiving().isPotionActive(WizardryPotions.mind_trick)){
				event.getEntityLiving().removePotionEffect(WizardryPotions.mind_trick);
			}
		}
	}

	@SubscribeEvent
	public static void onLivingSetAttackTargetEvent(LivingSetAttackTargetEvent event){
		// Mind trick
		// If the target is null already, no need to set it to null, or infinite loops will occur.
		if((event.getEntityLiving().isPotionActive(WizardryPotions.mind_trick)
				|| event.getEntityLiving().isPotionActive(WizardryPotions.fear))
				&& event.getEntityLiving() instanceof EntityLiving && event.getTarget() != null){
			((EntityLiving)event.getEntityLiving()).setAttackTarget(null);
		}
	}
}
