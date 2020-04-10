package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.EnumAction;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Reversal extends SpellRay {

	public static final String REVERSED_EFFECTS = "reversed_effects";

	public Reversal(){
		super("reversal", false, EnumAction.NONE);
		addProperties(REVERSED_EFFECTS);
	}

	@Override
	public boolean canBeCastBy(TileEntityDispenser dispenser){
		return false;
	}

	@Override
	protected boolean onEntityHit(World world, Entity target, Vec3d hit, @Nullable EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){

		// Naturally, this spell won't work unless it has a living caster and target
		if(caster != null && target instanceof EntityLivingBase){

			List<PotionEffect> negativePotions = new ArrayList<>(caster.getActivePotionEffects());
			negativePotions.removeIf(p -> !p.getPotion().isBadEffect());

			if(!world.isRemote){

				if(negativePotions.isEmpty()) return false; // Needs potion effects to reverse!

				// 1 effect for non-necromancy wands, 2 for apprentice necromancy wands, 3 for advanced and 4 for master
				int bonusEffects = (int)((modifiers.get(SpellModifiers.POTENCY) - 1) / Constants.POTENCY_INCREASE_PER_TIER + 0.5f) - 1;
				int n = getProperty(REVERSED_EFFECTS).intValue() + bonusEffects;

				// Chooses n random negative potion effects, where n is the potency level
				Collections.shuffle(negativePotions);
				negativePotions = negativePotions.subList(0, negativePotions.size() < n ? negativePotions.size() : n);

				// Now reverse them!
				negativePotions.forEach(p -> caster.removePotionEffect(p.getPotion()));
				negativePotions.forEach(((EntityLivingBase)target)::addPotionEffect);

			}else{
				ParticleBuilder.create(Type.BUFF).entity(caster).clr(1, 1, 0.3f).spawn(world);
			}
		}

		return true;
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, Vec3d hit, @Nullable EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onMiss(World world, @Nullable EntityLivingBase caster, Vec3d origin, Vec3d direction, int ticksInUse, SpellModifiers modifiers){
		return true;
	}

	@Override
	protected void spawnParticle(World world, double x, double y, double z, double vx, double vy, double vz){
		ParticleBuilder.create(Type.DARK_MAGIC).pos(x, y, z).clr(0.1f, 0, 0).spawn(world);
		ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).time(12 + world.rand.nextInt(8)).clr(0.1f, 0, 0.05f).spawn(world);
	}
}
