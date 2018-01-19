package electroblob.wizardry.spell;

import java.util.List;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class Detonate extends Spell {

	public Detonate() {
		super(Tier.ADVANCED, 45, Element.FIRE, "detonate", SpellType.ATTACK, 50, EnumAction.NONE, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers) {

		RayTraceResult rayTrace = WizardryUtilities.rayTrace(16*modifiers.get(WizardryItems.range_upgrade), world, caster, false);

		if(rayTrace != null && rayTrace.typeOfHit == RayTraceResult.Type.BLOCK){
			if(!world.isRemote){
				List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(3.0d*modifiers.get(WizardryItems.blast_upgrade), (rayTrace.hitVec.xCoord+0.5), (rayTrace.hitVec.yCoord+0.5), (rayTrace.hitVec.zCoord+0.5), world);
				for(int i=0;i<targets.size();i++){
					targets.get(i).attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.BLAST),
							// Damage decreases with distance but cannot be less than 0, naturally.
							Math.max(12.0f - (float)((EntityLivingBase)targets.get(i)).getDistance((rayTrace.hitVec.xCoord+0.5), (rayTrace.hitVec.yCoord+0.5), (rayTrace.hitVec.zCoord+0.5))*4, 0) * modifiers.get(SpellModifiers.DAMAGE));

				}
			}
			if(world.isRemote){
				double dx = (rayTrace.hitVec.xCoord+0.5) - caster.posX;
				double dy = (rayTrace.hitVec.yCoord+0.5) - WizardryUtilities.getPlayerEyesPos(caster);
				double dz = (rayTrace.hitVec.zCoord+0.5) - caster.posZ;
				world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, (rayTrace.hitVec.xCoord+0.5), (rayTrace.hitVec.yCoord+0.5), (rayTrace.hitVec.zCoord+0.5), 0, 0, 0);
				for(int i=1;i<5;i++){
					world.spawnParticle(EnumParticleTypes.FLAME, caster.posX + (i*(dx/5)) + world.rand.nextFloat()/5, WizardryUtilities.getPlayerEyesPos(caster) + (i*(dy/5)) + world.rand.nextFloat()/5, caster.posZ + (i*(dz/5)) + world.rand.nextFloat()/5, 0, 0, 0);
					world.spawnParticle(EnumParticleTypes.FLAME, caster.posX + (i*(dx/5)) + world.rand.nextFloat()/5, WizardryUtilities.getPlayerEyesPos(caster) + (i*(dy/5)) + world.rand.nextFloat()/5, caster.posZ + (i*(dz/5)) + world.rand.nextFloat()/5, 0, 0, 0);
				}
			}
			world.playSound(caster, (rayTrace.hitVec.xCoord+0.5), (rayTrace.hitVec.yCoord+0.5), (rayTrace.hitVec.zCoord+0.5), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F, (1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F) * 0.7F);
			caster.swingArm(hand);
			return true;
		}
		return false;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target, SpellModifiers modifiers){

		if(target != null){
			if(!world.isRemote){
				List<EntityLivingBase> targets = WizardryUtilities.getEntitiesWithinRadius(3.0d, target.posX, target.posY, target.posZ, world);
				for(int i=0;i<targets.size();i++){
					targets.get(i).attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.BLAST),
							// Damage decreases with distance but cannot be less than 0, naturally.
							Math.max(12.0f - (float)((EntityLivingBase)targets.get(i)).getDistance(target.posX, target.posY, target.posZ)*4, 0) * modifiers.get(SpellModifiers.DAMAGE));

				}
			}
			if(world.isRemote){
				double dx = target.posX - caster.posX;
				double dy = target.posY - (caster.posY + caster.getEyeHeight());
				double dz = target.posZ - caster.posZ;
				world.spawnParticle(EnumParticleTypes.EXPLOSION_HUGE, target.posX, target.posY, target.posZ, 0, 0, 0);
				for(int i=1;i<5;i++){
					world.spawnParticle(EnumParticleTypes.FLAME, caster.posX + (i*(dx/5)) + world.rand.nextFloat()/5, caster.posY + caster.getEyeHeight() + (i*(dy/5)) + world.rand.nextFloat()/5, caster.posZ + (i*(dz/5)) + world.rand.nextFloat()/5, 0, 0, 0);
					world.spawnParticle(EnumParticleTypes.FLAME, caster.posX + (i*(dx/5)) + world.rand.nextFloat()/5, caster.posY + caster.getEyeHeight() + (i*(dy/5)) + world.rand.nextFloat()/5, caster.posZ + (i*(dz/5)) + world.rand.nextFloat()/5, 0, 0, 0);
				}
			}
			// Player is null here because the sound was not caused by a player.
			world.playSound(null, target.posX, target.posY, target.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 4.0F, (1.0F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F) * 0.7F);
			caster.swingArm(hand);
			return true;
		}

		return false;
	}

	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

}
