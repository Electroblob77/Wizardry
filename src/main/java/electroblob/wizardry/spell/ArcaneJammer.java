package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.living.EntityWizard;
import electroblob.wizardry.event.SpellCastEvent;
import electroblob.wizardry.registry.WizardryAdvancementTriggers;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class ArcaneJammer extends SpellRay {

	public ArcaneJammer(){
		super("arcane_jammer", Tier.ADVANCED, Element.HEALING, SpellType.ATTACK, 30, 50, false, 10, WizardrySounds.SPELL_DEFLECTION);
		this.soundValues(0.7f, 1, 0.4f);
	}

	@Override
	protected boolean onEntityHit(World world, Entity target, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		
		if(WizardryUtilities.isLiving(target)){
			
			if(target instanceof EntityWizard && caster instanceof EntityPlayer)
				WizardryAdvancementTriggers.jam_wizard.triggerFor((EntityPlayer)caster);
			
			if(!world.isRemote){
				((EntityLivingBase)target).addPotionEffect(new PotionEffect(WizardryPotions.arcane_jammer,
						(int)(300 * modifiers.get(WizardryItems.duration_upgrade)), 0));
			}
		}
		
		return true;
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onMiss(World world, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		return true;
	}
	
	@Override
	protected void spawnParticle(World world, double x, double y, double z, double vx, double vy, double vz){
		ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).lifetime(12 + world.rand.nextInt(8)).colour(0.9f, 0.3f, 0.7f)
		.spawn(world);
	}

	@SubscribeEvent
	public static void onSpellCastPreEvent(SpellCastEvent.Pre event){
		// Arcane jammer prevents spell casting.
		if(event.getCaster() != null && event.getCaster().isPotionActive(WizardryPotions.arcane_jammer)) event.setCanceled(true);
	}

}
