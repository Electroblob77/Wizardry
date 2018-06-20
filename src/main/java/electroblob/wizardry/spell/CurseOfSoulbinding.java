package electroblob.wizardry.spell;

import electroblob.wizardry.WizardData;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.util.IElementalDamage;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class CurseOfSoulbinding extends SpellRay {

	public CurseOfSoulbinding(){
		super("curse_of_soulbinding", Tier.ADVANCED, Element.NECROMANCY, SpellType.ATTACK, 35, 100, false, 10, SoundEvents.ENTITY_WITHER_SPAWN);
		this.soundValues(1, 1.1f, 0.2f);
	}
	
	@Override public boolean canBeCastByNPCs() { return false; }

	@Override
	protected boolean onEntityHit(World world, Entity target, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		
		if(WizardryUtilities.isLiving(target) && caster instanceof EntityPlayer
				&& WizardData.get((EntityPlayer)caster) != null){
			// Return false if soulbinding failed (e.g. if the target is already soulbound)
			if(!WizardData.get((EntityPlayer)caster).soulbind((EntityLivingBase)target)) return false;
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
		ParticleBuilder.create(Type.DARK_MAGIC).pos(x, y, z).colour(0.4f, 0, 0).spawn(world);
		ParticleBuilder.create(Type.DARK_MAGIC).pos(x, y, z).colour(0.1f, 0, 0).spawn(world);
		ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).lifetime(12 + world.rand.nextInt(8)).colour(1, 0.8f, 1).spawn(world);
	}

	@SubscribeEvent
	public static void onLivingHurtEvent(LivingHurtEvent event){

		if(!event.getEntity().world.isRemote && event.getEntityLiving() instanceof EntityPlayer
				&& !event.getSource().isUnblockable() && !(event.getSource() instanceof IElementalDamage
						&& ((IElementalDamage)event.getSource()).isRetaliatory())){
			WizardData data = WizardData.get((EntityPlayer)event.getEntityLiving());
			if(data != null){
				data.damageAllSoulboundCreatures(event.getAmount());
			}
		}
	}

}
