package electroblob.wizardry.spell;

import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.util.MagicDamage;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class Freeze extends SpellRay {

	public Freeze(){
		super("freeze", false, SpellActions.POINT);
		this.soundValues(1, 1.4f, 0.4f);
		addProperties(DAMAGE, EFFECT_DURATION, EFFECT_STRENGTH);
		this.hitLiquids(true);
	}

	@Override
	protected boolean onEntityHit(World world, Entity target, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){
		
		if(WizardryUtilities.isLiving(target)){

			if(target instanceof EntityBlaze || target instanceof EntityMagmaCube){
				target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.FROST),
						getProperty(DAMAGE).floatValue() * modifiers.get(SpellModifiers.POTENCY));
			}

			if(MagicDamage.isEntityImmune(DamageType.FROST, target)){
				if(!world.isRemote && caster instanceof EntityPlayer) ((EntityPlayer)caster).sendStatusMessage(
						new TextComponentTranslation("spell.resist", target.getName(), this.getNameForTranslationFormatted()), true);
			}else{
				((EntityLivingBase)target).addPotionEffect(new PotionEffect(WizardryPotions.frost,
						(int)(getProperty(EFFECT_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade)),
						getProperty(EFFECT_STRENGTH).intValue()));
			}

			if(target.isBurning()) target.extinguish();

			return true;
		}
		
		return false; // If the spell hit a non-living entity
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){

		if(WizardryUtilities.canDamageBlocks(caster, world)){

			if(world.getBlockState(pos).getBlock() == Blocks.WATER && !world.isRemote){
				world.setBlockState(pos, Blocks.ICE.getDefaultState());
			}else if(world.getBlockState(pos).getBlock() == Blocks.LAVA && !world.isRemote){
				world.setBlockState(pos, Blocks.OBSIDIAN.getDefaultState());
			}else if(world.getBlockState(pos).getBlock() == Blocks.FLOWING_LAVA && !world.isRemote){
				world.setBlockState(pos, Blocks.COBBLESTONE.getDefaultState());
			}else if(side == EnumFacing.UP && !world.isRemote && world.isSideSolid(pos, EnumFacing.UP)
					&& WizardryUtilities.canBlockBeReplaced(world, pos.up())){
				world.setBlockState(pos.up(), Blocks.SNOW_LAYER.getDefaultState());
			}
		}
		
		return true; // Always succeeds if it hits a block
	}

	@Override
	protected boolean onMiss(World world, EntityLivingBase caster, Vec3d origin, Vec3d direction, int ticksInUse, SpellModifiers modifiers){
		return false;
	}
	
	@Override
	protected void spawnParticle(World world, double x, double y, double z, double vx, double vy, double vz){
		float brightness = 0.5f + (world.rand.nextFloat() / 2);
		ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).time(12 + world.rand.nextInt(8))
		.clr(brightness, brightness + 0.1f, 1).spawn(world);
		ParticleBuilder.create(Type.SNOW).pos(x, y, z).spawn(world);
	}

}
