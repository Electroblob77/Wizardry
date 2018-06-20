package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.entity.living.EntityBlazeMinion;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.registry.WizardrySounds;
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
import net.minecraft.init.Blocks;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class Freeze extends SpellRay {
	
	/** The base duration of the frostbite effect appled by this spell. */
	private static final int BASE_DURATION = 200;
	/** The base damage dealt to entities vulnerable to frost. */
	private static final float BASE_DAMAGE = 3;

	public Freeze(){
		super("freeze", Tier.BASIC, Element.ICE, SpellType.ATTACK, 5, 10, false, 10, WizardrySounds.SPELL_ICE);
		this.soundValues(1, 1.4f, 0.4f);
	}

	@Override
	protected boolean onEntityHit(World world, Entity target, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		
		if(WizardryUtilities.isLiving(target)){

			if(target instanceof EntityBlaze || target instanceof EntityMagmaCube || target instanceof EntityBlazeMinion){
				target.attackEntityFrom(MagicDamage.causeDirectMagicDamage(caster, DamageType.FROST),
						BASE_DAMAGE * modifiers.get(SpellModifiers.POTENCY));
			}

			if(MagicDamage.isEntityImmune(DamageType.FROST, target)){
				if(!world.isRemote) caster.sendMessage(new TextComponentTranslation("spell.resist", target.getName(),
						this.getNameForTranslationFormatted()));
			}else{
				((EntityLivingBase)target).addPotionEffect(new PotionEffect(WizardryPotions.frost,
						(int)(BASE_DURATION * modifiers.get(WizardryItems.duration_upgrade)), 1));
			}

			if(target.isBurning()) target.extinguish();
			
			return true;
		}
		
		return false; // If the spell hit a non-living entity
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		
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
		
		return true; // Always succeeds if it hits a block
	}

	@Override
	protected boolean onMiss(World world, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		return false;
	}
	
	@Override
	protected void spawnParticle(World world, double x, double y, double z, double vx, double vy, double vz){
		float brightness = 0.5f + (world.rand.nextFloat() / 2);
		ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).lifetime(12 + world.rand.nextInt(8))
		.colour(brightness, brightness + 0.1f, 1).spawn(world);
		ParticleBuilder.create(Type.SNOW).pos(x, y, z).spawn(world);
	}

}
