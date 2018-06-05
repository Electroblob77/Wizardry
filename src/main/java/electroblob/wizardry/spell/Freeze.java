package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.living.EntityBlazeMinion;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumAction;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class Freeze extends Spell {

	public Freeze(){
		super(Tier.BASIC, 5, Element.ICE, "freeze", SpellType.ATTACK, 10, EnumAction.NONE, false);
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		// Entity ray trace is done first because block ray trace passes through entities; if it was the other
		// way round, entities would only be hit when there were no blocks in range behind them.
		RayTraceResult rayTrace = WizardryUtilities.standardEntityRayTrace(world, caster,
				10 * modifiers.get(WizardryItems.range_upgrade));

		if(rayTrace != null && rayTrace.typeOfHit == RayTraceResult.Type.ENTITY && WizardryUtilities.isLiving(rayTrace.entityHit)){

			EntityLivingBase target = (EntityLivingBase)rayTrace.entityHit;

			if(target instanceof EntityBlaze || target instanceof EntityMagmaCube
					|| target instanceof EntityBlazeMinion){
				target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.FROST),
						3.0f * modifiers.get(SpellModifiers.DAMAGE));
			}

			if(MagicDamage.isEntityImmune(DamageType.FROST, target)){
				if(!world.isRemote) caster.sendMessage(new TextComponentTranslation("spell.resist", target.getName(),
						this.getNameForTranslationFormatted()));
			}else{
				target.addPotionEffect(new PotionEffect(WizardryPotions.frost,
						(int)(200 * modifiers.get(WizardryItems.duration_upgrade)), 1));
			}

			if(target.isBurning()){
				target.extinguish();
			}

			if(world.isRemote){
				double dx = target.posX - caster.posX;
				double dy = (target.getEntityBoundingBox().minY + target.height / 2)
						- WizardryUtilities.getPlayerEyesPos(caster);
				double dz = target.posZ - caster.posZ;
				for(int i = 1; i < 5; i++){
					float brightness = 0.5f + (world.rand.nextFloat() / 2);
					Wizardry.proxy.spawnParticle(Type.SPARKLE, world,
							caster.posX + (i * (dx / 5)) + world.rand.nextFloat() / 5,
							WizardryUtilities.getPlayerEyesPos(caster) + (i * (dy / 5)) + world.rand.nextFloat() / 5,
							caster.posZ + (i * (dz / 5)) + world.rand.nextFloat() / 5, 0.0d, 0.0d, 0.0d,
							12 + world.rand.nextInt(8), brightness, brightness + 0.1f, 1.0f);
					Wizardry.proxy.spawnParticle(Type.SNOW, world,
							caster.posX + (i * (dx / 5)) + world.rand.nextFloat() / 5,
							WizardryUtilities.getPlayerEyesPos(caster) + (i * (dy / 5)) + world.rand.nextFloat() / 5,
							caster.posZ + (i * (dz / 5)) + world.rand.nextFloat() / 5, 0, -0.02, 0,
							40 + world.rand.nextInt(10));
				}
			}

			caster.swingArm(hand);
			WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_ICE, 1.0F,
					world.rand.nextFloat() * 0.4F + 1.2F);
			return true;

		}else{
			rayTrace = WizardryUtilities.rayTrace(10 * modifiers.get(WizardryItems.range_upgrade), world, caster, true);
			// Gets block the player is looking at and sets to ice or covers with snow as necessary
			// Note how the block is set on the server side only (kinda obvious really) but the particles are
			// spawned on the client side only.
			if(rayTrace != null && rayTrace.typeOfHit == RayTraceResult.Type.BLOCK){

				BlockPos pos = rayTrace.getBlockPos();

				if(world.getBlockState(pos).getBlock() == Blocks.WATER && !world.isRemote){
					world.setBlockState(pos, Blocks.ICE.getDefaultState());
				}else if(world.getBlockState(pos).getBlock() == Blocks.LAVA && !world.isRemote){
					world.setBlockState(pos, Blocks.OBSIDIAN.getDefaultState());
				}else if(world.getBlockState(pos).getBlock() == Blocks.FLOWING_LAVA && !world.isRemote){
					world.setBlockState(pos, Blocks.COBBLESTONE.getDefaultState());
				}else if(rayTrace.sideHit == EnumFacing.UP && !world.isRemote && world.isSideSolid(pos, EnumFacing.UP)
						&& WizardryUtilities.canBlockBeReplaced(world, pos.up())){
					world.setBlockState(pos.up(), Blocks.SNOW_LAYER.getDefaultState());
				}

				if(world.isRemote){

					double dx = pos.getX() + 0.5 - caster.posX;
					double dy = pos.getY() + 0.5 - WizardryUtilities.getPlayerEyesPos(caster);
					double dz = pos.getZ() + 0.5 - caster.posZ;

					for(int i = 1; i < 5; i++){
						float brightness = 0.5f + (world.rand.nextFloat() / 2);
						Wizardry.proxy.spawnParticle(Type.SPARKLE, world,
								caster.posX + (i * (dx / 5)) + world.rand.nextFloat() / 5,
								WizardryUtilities.getPlayerEyesPos(caster) + (i * (dy / 5))
										+ world.rand.nextFloat() / 5,
								caster.posZ + (i * (dz / 5)) + world.rand.nextFloat() / 5, 0.0d, 0.0d, 0.0d,
								20 + world.rand.nextInt(8), brightness, brightness + 0.1f, 1.0f);
						Wizardry.proxy.spawnParticle(Type.SPARKLE, world,
								caster.posX + (i * (dx / 5)) + world.rand.nextFloat() / 5,
								WizardryUtilities.getPlayerEyesPos(caster) + (i * (dy / 5))
										+ world.rand.nextFloat() / 5,
								caster.posZ + (i * (dz / 5)) + world.rand.nextFloat() / 5, 0, 0, 0,
								20 + world.rand.nextInt(8), 1.0f, 1.0f, 1.0f);
					}
				}

				caster.swingArm(hand);
				WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_ICE, 1.0F,
						world.rand.nextFloat() * 0.4F + 1.2F);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean cast(World world, EntityLiving caster, EnumHand hand, int ticksInUse, EntityLivingBase target,
			SpellModifiers modifiers){

		if(target != null){

			if(target instanceof EntityBlaze || target instanceof EntityMagmaCube
					|| target instanceof EntityBlazeMinion){
				target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.FROST),
						3.0f * modifiers.get(SpellModifiers.DAMAGE));
			}

			if(!world.isRemote && !MagicDamage.isEntityImmune(DamageType.FROST, target)){
				target.addPotionEffect(new PotionEffect(WizardryPotions.frost,
						(int)(200 * modifiers.get(WizardryItems.duration_upgrade)), 1));
			}

			if(target.isBurning()){
				target.extinguish();
			}

			if(world.isRemote){
				double dx = target.posX - caster.posX;
				double dy = (target.getEntityBoundingBox().minY + target.height / 2)
						- (caster.posY + caster.getEyeHeight());
				double dz = target.posZ - caster.posZ;
				for(int i = 1; i < 5; i++){
					float brightness = 0.5f + (world.rand.nextFloat() / 2);
					Wizardry.proxy.spawnParticle(Type.SPARKLE, world,
							caster.posX + (i * (dx / 5)) + world.rand.nextFloat() / 5,
							caster.posY + caster.getEyeHeight() + (i * (dy / 5)) + world.rand.nextFloat() / 5,
							caster.posZ + (i * (dz / 5)) + world.rand.nextFloat() / 5, 0.0d, 0.0d, 0.0d,
							12 + world.rand.nextInt(8), brightness, brightness + 0.1f, 1.0f);
					Wizardry.proxy.spawnParticle(Type.SNOW, world,
							caster.posX + (i * (dx / 5)) + world.rand.nextFloat() / 5,
							caster.posY + caster.getEyeHeight() + (i * (dy / 5)) + world.rand.nextFloat() / 5,
							caster.posZ + (i * (dz / 5)) + world.rand.nextFloat() / 5, 0, -0.02, 0,
							40 + world.rand.nextInt(10));
				}
			}

			caster.swingArm(hand);
			caster.playSound(WizardrySounds.SPELL_ICE, 1.0F, world.rand.nextFloat() * 0.4F + 1.2F);
			return true;
		}

		return false;
	}

	@Override
	public boolean canBeCastByNPCs(){
		return true;
	}

}
