package electroblob.wizardry.spell;

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
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class Ignite extends Spell {

	public Ignite(){
		super(Tier.BASIC, 5, Element.FIRE, "ignite", SpellType.ATTACK, 10, EnumAction.NONE, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		// Entity ray trace is done first because block ray trace passes through entities; if it was the other
		// way round, entities would only be hit when there were no blocks in range behind them.
		RayTraceResult rayTrace = WizardryUtilities.standardEntityRayTrace(world, caster,
				10 * modifiers.get(WizardryItems.range_upgrade));

		if(rayTrace != null && rayTrace.typeOfHit == RayTraceResult.Type.ENTITY
				&& rayTrace.entityHit instanceof EntityLivingBase){

			EntityLivingBase target = (EntityLivingBase)rayTrace.entityHit;

			if(MagicDamage.isEntityImmune(DamageType.FIRE, target)){
				if(!world.isRemote) caster.sendMessage(new TextComponentTranslation("spell.resist", target.getName(),
						this.getNameForTranslationFormatted()));
			}else{
				target.setFire((int)(10 * modifiers.get(WizardryItems.duration_upgrade)));
			}

			if(world.isRemote){
				double dx = target.posX - caster.posX;
				double dy = (target.getEntityBoundingBox().minY + target.height / 2)
						- WizardryUtilities.getPlayerEyesPos(caster);
				double dz = target.posZ - caster.posZ;
				// i starts at 1 so that particles are not spawned in the player's head.
				for(int i = 1; i < 5; i++){
					// WizardryUtilities.spawnParticleAndNotify(world, EnumParticleTypes.FLAME, caster.posX + (i*(dx/5))
					// + world.rand.nextFloat()/5, caster.posY + (i*(dy/5)) + world.rand.nextFloat()/5, caster.posZ +
					// (i*(dz/5)) + world.rand.nextFloat()/5, 0, 0, 0, 0, 0, 0, 0);
					// WizardryUtilities.spawnParticleAndNotify(world, EnumParticleTypes.FLAME, caster.posX + (i*(dx/5))
					// + world.rand.nextFloat()/5, caster.posY + (i*(dy/5)) + world.rand.nextFloat()/5, caster.posZ +
					// (i*(dz/5)) + world.rand.nextFloat()/5, 0, 0, 0, 0, 0, 0, 0);
					world.spawnParticle(EnumParticleTypes.FLAME,
							caster.posX + (i * (dx / 5)) + world.rand.nextFloat() / 5,
							WizardryUtilities.getPlayerEyesPos(caster) + (i * (dy / 5)) + world.rand.nextFloat() / 5,
							caster.posZ + (i * (dz / 5)) + world.rand.nextFloat() / 5, 0, 0, 0);
					world.spawnParticle(EnumParticleTypes.FLAME,
							caster.posX + (i * (dx / 5)) + world.rand.nextFloat() / 5,
							WizardryUtilities.getPlayerEyesPos(caster) + (i * (dy / 5)) + world.rand.nextFloat() / 5,
							caster.posZ + (i * (dz / 5)) + world.rand.nextFloat() / 5, 0, 0, 0);
				}
			}

			caster.swingArm(hand);
			WizardryUtilities.playSoundAtPlayer(caster, SoundEvents.ITEM_FLINTANDSTEEL_USE, 1.0F,
					world.rand.nextFloat() * 0.4F + 0.8F);

			return true;

		}else{

			rayTrace = WizardryUtilities.rayTrace(10 * modifiers.get(WizardryItems.range_upgrade), world, caster,
					false);

			// Gets block the player is looking at and sets the appropriate surrounding air block to fire.
			// Note how the block is set on the server side only (kinda obvious really) but the particles are
			// spawned on the client side only.
			if(rayTrace != null && rayTrace.typeOfHit == RayTraceResult.Type.BLOCK){

				BlockPos pos = rayTrace.getBlockPos().offset(rayTrace.sideHit);

				if(world.isAirBlock(pos)){
					if(!world.isRemote){
						world.setBlockState(pos, Blocks.FIRE.getDefaultState());
					}

					if(world.isRemote){

						double dx = pos.getX() + 0.5 - caster.posX;
						double dy = pos.getY() + 0.5 - WizardryUtilities.getPlayerEyesPos(caster);
						double dz = pos.getZ() + 0.5 - caster.posZ;

						for(int i = 1; i < 5; i++){
							world.spawnParticle(EnumParticleTypes.FLAME,
									caster.posX + (i * (dx / 5)) + world.rand.nextFloat() / 5,
									WizardryUtilities.getPlayerEyesPos(caster) + (i * (dy / 5))
											+ world.rand.nextFloat() / 5,
									caster.posZ + (i * (dz / 5)) + world.rand.nextFloat() / 5, 0, 0, 0);
							world.spawnParticle(EnumParticleTypes.FLAME,
									caster.posX + (i * (dx / 5)) + world.rand.nextFloat() / 5,
									WizardryUtilities.getPlayerEyesPos(caster) + (i * (dy / 5))
											+ world.rand.nextFloat() / 5,
									caster.posZ + (i * (dz / 5)) + world.rand.nextFloat() / 5, 0, 0, 0);
						}
					}

					caster.swingArm(hand);
					WizardryUtilities.playSoundAtPlayer(caster, SoundEvents.ITEM_FLINTANDSTEEL_USE, 1.0F,
							world.rand.nextFloat() * 0.4F + 0.8F);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target,
			SpellModifiers modifiers){

		if(target != null){

			if(!MagicDamage.isEntityImmune(DamageType.FIRE, target))
				target.setFire((int)(10 * modifiers.get(WizardryItems.duration_upgrade)));

			if(world.isRemote){
				double dx = target.posX - caster.posX;
				double dy = (target.getEntityBoundingBox().minY + target.height / 2)
						- (caster.posY + caster.getEyeHeight());
				double dz = target.posZ - caster.posZ;
				// i starts at 1 so that particles are not spawned in the player's head.
				for(int i = 1; i < 5; i++){
					// WizardryUtilities.spawnParticleAndNotify(world, EnumParticleTypes.FLAME, caster.posX + (i*(dx/5))
					// + world.rand.nextFloat()/5, caster.posY + (i*(dy/5)) + world.rand.nextFloat()/5, caster.posZ +
					// (i*(dz/5)) + world.rand.nextFloat()/5, 0, 0, 0, 0, 0, 0, 0);
					// WizardryUtilities.spawnParticleAndNotify(world, EnumParticleTypes.FLAME, caster.posX + (i*(dx/5))
					// + world.rand.nextFloat()/5, caster.posY + (i*(dy/5)) + world.rand.nextFloat()/5, caster.posZ +
					// (i*(dz/5)) + world.rand.nextFloat()/5, 0, 0, 0, 0, 0, 0, 0);
					world.spawnParticle(EnumParticleTypes.FLAME,
							caster.posX + (i * (dx / 5)) + world.rand.nextFloat() / 5,
							caster.posY + caster.getEyeHeight() + (i * (dy / 5)) + world.rand.nextFloat() / 5,
							caster.posZ + (i * (dz / 5)) + world.rand.nextFloat() / 5, 0, 0, 0);
					world.spawnParticle(EnumParticleTypes.FLAME,
							caster.posX + (i * (dx / 5)) + world.rand.nextFloat() / 5,
							caster.posY + caster.getEyeHeight() + (i * (dy / 5)) + world.rand.nextFloat() / 5,
							caster.posZ + (i * (dz / 5)) + world.rand.nextFloat() / 5, 0, 0, 0);
				}
			}

			caster.swingArm(hand);
			caster.playSound(SoundEvents.ITEM_FLINTANDSTEEL_USE, 1.0F, world.rand.nextFloat() * 0.4F + 0.8F);

			return true;
		}

		return false;
	}

	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

}
