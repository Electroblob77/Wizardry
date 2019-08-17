package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.block.BlockCrystalOre;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.RelativeFacing;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockOre;
import net.minecraft.block.BlockRedstoneOre;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class Divination extends Spell {

	private static final float NUDGE_SPEED = 0.2f;

	public Divination(){
		super("divination", EnumAction.NONE, false);
		addProperties(RANGE);
	}

	/** A set of constants representing the different 'signal strengths' for the divination spell. In practical
	 * terms, this means different chat readouts and effects. */
	protected enum Strength {

		NOTHING("nothing", -1),
		WEAK("weak", 0),
		MODERATE("moderate", 0.25f),
		STRONG("strong", 0.5f),
		VERY_STRONG("very_strong", 0.75f);

		String key;
		float minWeight;

		Strength(String key, float minWeight){
			this.key = key;
			this.minWeight = minWeight;
		}

		protected static Strength forWeight(float weight){
			return Arrays.stream(values()).filter(s -> s.minWeight < weight).max(Comparator.naturalOrder()).orElse(NOTHING);
		}
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		double range = getProperty(RANGE).floatValue() * modifiers.get(WizardryItems.range_upgrade);

		List<BlockPos> sphere = WizardryUtilities.getBlockSphere(caster.getPosition(), range);

		sphere.removeIf(b -> !(world.getBlockState(b).getBlock() instanceof BlockOre
							|| world.getBlockState(b).getBlock() instanceof BlockRedstoneOre
							|| world.getBlockState(b).getBlock() instanceof BlockCrystalOre
							|| Arrays.asList(Wizardry.settings.divinationOreWhitelist)
				.contains(world.getBlockState(b).getBlock().getRegistryName())));

		Strength strength = Strength.NOTHING;

		EnumFacing direction = EnumFacing.DOWN; // Doesn't matter what this is

		if(!sphere.isEmpty()){

			// Sorts the positions based on weight (see below), in ascending order
			sphere.sort(Comparator.comparingDouble(b -> calculateWeight(world, caster, b, range, modifiers)));

			// The weights are sorted in ascending order, so this must be the largest
			BlockPos target = sphere.get(sphere.size() - 1);

			direction = EnumFacing.getFacingFromVector((float)(target.getX() + 0.5 - caster.posX),
					(float)(target.getY() + 0.5 - (caster.getEntityBoundingBox().minY + caster.getEyeHeight())),
					(float)(target.getZ() + 0.5 - caster.posZ));

			if(world.isRemote) ParticleBuilder.create(ParticleBuilder.Type.SPARKLE).pos(target.getX() + 0.5,
					target.getY() + 1.5, target.getZ() + 0.5).spawn(world);

			strength = Strength.forWeight(calculateWeight(world, caster, target, range, modifiers));
		}

		if(!world.isRemote){
			caster.sendStatusMessage(new TextComponentTranslation("spell." + this.getUnlocalisedName() + "."
					+ strength.key, new TextComponentTranslation("spell." + this.getUnlocalisedName() + "."
					+ RelativeFacing.relativise(direction, caster).name)), false);
		}else{
			switch(strength){
				case NOTHING: break;
				case WEAK: break;
				case MODERATE:
					spawnHintParticles(world, caster, 3, direction);
					break;
				case STRONG:
					spawnHintParticles(world, caster, 8, direction);
					break;
				case VERY_STRONG:
					spawnHintParticles(world, caster, 12, direction);
					caster.addVelocity(direction.getXOffset() * NUDGE_SPEED, direction.getYOffset() * NUDGE_SPEED,
							direction.getZOffset() * NUDGE_SPEED);
					break;
			}
		}

		return true;
	}

	private static void spawnHintParticles(World world, Entity caster, int count, EnumFacing direction){

		Vec3d vec = new Vec3d(caster.getPosition().offset(EnumFacing.UP).offset(direction, 2)).add(0.5, 0.5, 0.5);

		for(int i=0; i<count; i++){
			ParticleBuilder.create(ParticleBuilder.Type.FLASH, world.rand, vec.x, vec.y, vec.z, 0.7, false)
					.time(20 + world.rand.nextInt(5)).clr(0.6f + world.rand.nextFloat() * 0.4f,
					0.6f + world.rand.nextFloat() * 0.4f, 0.6f + world.rand.nextFloat() * 0.4f).scale(0.3f).spawn(world);
		}
	}

	protected static float calculateWeight(World world, EntityPlayer caster, BlockPos pos, double range, SpellModifiers modifiers){

		Block block = world.getBlockState(pos).getBlock();
		// On a non-sorcery wand, the value of the ore has no effect on its weight
		float weightModifier = modifiers.get(SpellModifiers.POTENCY) - 1;

		// xp is a decent way of determining the 'value' of a block
		// There is a degree of randomness associated with it though...
		float xp = block.getExpDrop(world.getBlockState(pos), world, pos, 0);
		// For some reason smelting gives a lot less than mining, hence the multiplying by 4
		if(xp == 0) xp = 4 * FurnaceRecipes.instance().getSmeltingExperience(new ItemStack(block));

		// By my (rather rough) calculations, this should mean that using a master sorcerer wand, an iron ore block
		// and a diamond ore block just under twice as far away as the iron ore should have about the same weight
		return (float)(1 - caster.getDistance(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)/range + 0.2 * weightModifier * xp);
	}

}
