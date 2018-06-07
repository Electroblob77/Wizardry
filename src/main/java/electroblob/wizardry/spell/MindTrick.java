package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryUtilities;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class MindTrick extends Spell {

	public MindTrick() {
		super(EnumTier.BASIC, 10, EnumElement.NECROMANCY, "mind_trick", EnumSpellType.ATTACK, 40, EnumAction.none, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {

		MovingObjectPosition rayTrace = WizardryUtilities.standardEntityRayTrace(world, caster, 8*rangeMultiplier);

		if(rayTrace != null && rayTrace.entityHit != null && rayTrace.entityHit instanceof EntityLivingBase){

			EntityLivingBase target = (EntityLivingBase)rayTrace.entityHit;

			if(!world.isRemote){
				if(target instanceof EntityPlayer){
					target.addPotionEffect(new PotionEffect(Potion.confusion.id, (int)(300*durationMultiplier), 0));
				}else if(target instanceof EntityLiving){
					// New AI
					((EntityLiving)target).setAttackTarget(null);
					// Old AI
					if(target instanceof EntityCreature) ((EntityCreature)target).setTarget(null);

					target.addPotionEffect(new PotionEffect(Wizardry.mindTrick.id, (int)(300*durationMultiplier), 0));
				}
			}else{
				for(int i=0; i<10; i++){
					Wizardry.proxy.spawnParticle(EnumParticleType.DARK_MAGIC, world, target.posX - 0.25 + world.rand.nextDouble()*0.5,
							WizardryUtilities.getEntityFeetPos(target) + target.getEyeHeight() - 0.25 + world.rand.nextDouble()*0.5,
							target.posZ - 0.25 + world.rand.nextDouble()*0.5,
							0, 0, 0, 0, 0.8f, 0.2f, 1.0f);
				}
			}
			world.playSoundAtEntity(target, "wizardry:effect", 0.7F, world.rand.nextFloat() * 0.4F + 0.8F);
			caster.swingItem();
			return true;
		}
		return false;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EntityLivingBase target, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {

		if(target != null){
			if(!world.isRemote){
				if(target instanceof EntityPlayer){
					target.addPotionEffect(new PotionEffect(Potion.confusion.id, (int)(300*durationMultiplier), 0));
				}else if(target instanceof EntityLiving){
					// New AI
					((EntityLiving)target).setAttackTarget(null);
					// Old AI
					if(target instanceof EntityCreature) ((EntityCreature)target).setTarget(null);

					target.addPotionEffect(new PotionEffect(Wizardry.mindTrick.id, (int)(300*durationMultiplier), 0));
				}
			}else{
				for(int i=0; i<10; i++){
					Wizardry.proxy.spawnParticle(EnumParticleType.DARK_MAGIC, world, target.posX - 0.25 + world.rand.nextDouble()*0.5,
							WizardryUtilities.getEntityFeetPos(target) + target.getEyeHeight() - 0.25 + world.rand.nextDouble()*0.5,
							target.posZ - 0.25 + world.rand.nextDouble()*0.5,
							0, 0, 0, 0, 0.8f, 0.2f, 1.0f);
				}
			}
			world.playSoundAtEntity(target, "wizardry:effect", 0.7F, world.rand.nextFloat() * 0.4F + 0.8F);
			caster.swingItem();
			return true;
		}
		return false;
	}
	
	@Override
	public boolean canBeCastByNPCs() {
		return true;
	}
}
