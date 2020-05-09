package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.item.ISpellCastingItem;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class Mine extends SpellRay {

	private static final Method getSilkTouchDrop;

	static {
		getSilkTouchDrop = ObfuscationReflectionHelper.findMethod(Block.class, "func_180643_i", ItemStack.class, IBlockState.class);
	}

	public Mine(){
		super("mine", false, SpellActions.POINT);
		this.ignoreLivingEntities(true);
		this.particleSpacing(0.5);
	}

	@Override
	protected boolean onEntityHit(World world, Entity target, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){

		// Needs to be outside because it gets run on the client-side
		if(caster instanceof EntityPlayer){
			if(caster.getHeldItemMainhand().getItem() instanceof ISpellCastingItem){
				caster.swingArm(EnumHand.MAIN_HAND);
			}else if(caster.getHeldItemOffhand().getItem() instanceof ISpellCastingItem){
				caster.swingArm(EnumHand.OFF_HAND);
			}
		}

		if(!world.isRemote){

			if(WizardryUtilities.isBlockUnbreakable(world, pos)) return false;
			// The mine spell ignores the block damage setting for players, since that's the entire point of the spell
			// Instead, it triggers block break events at the appropriate points, which protection mods should be able to
			// pick up and allow/disallow accordingly
			// For the time being, dispensers respect the mobGriefing gamerule
			if(!(caster instanceof EntityPlayer) && !WizardryUtilities.canDamageBlocks(caster, world)) return false;
			// Can't mine arcane-locked blocks
			if(world.getTileEntity(pos) != null && world.getTileEntity(pos).getTileData().hasUniqueId(ArcaneLock.NBT_KEY)) return false;

			IBlockState state = world.getBlockState(pos);
			// The maximum harvest level as determined by the potency multiplier. The + 0.5f is so that
			// weird float processing doesn't incorrectly round it down.
			int harvestLevel = (int)((modifiers.get(SpellModifiers.POTENCY) - 1) / Constants.POTENCY_INCREASE_PER_TIER + 0.5f);

			if(harvestLevel > 0) harvestLevel--; // Shifts them all down one since normally novice wands give some potency

			// The >= 3 is to allow master earth wands to break anything.
			if(state.getBlock().getHarvestLevel(state) <= harvestLevel || harvestLevel >= 3){

				boolean flag = false;

				int blastUpgradeCount = (int)((modifiers.get(WizardryItems.blast_upgrade) - 1) / Constants.RANGE_INCREASE_PER_LEVEL + 0.5f);
				// Results in the following patterns:
				// 0 blast upgrades: single block
				// 1 blast upgrade: 3x3 without corners or edges
				// 2 blast upgrades: 3x3 with corners
				// 3 blast upgrades: 5x5 without corners or edges
				float radius = 0.5f + 0.73f * blastUpgradeCount;

				List<BlockPos> sphere = WizardryUtilities.getBlockSphere(pos, radius);

				for(BlockPos pos1 : sphere){

					if(WizardryUtilities.isBlockUnbreakable(world, pos1)) continue;

					IBlockState state1 = world.getBlockState(pos1);

					if(state1.getBlock().getHarvestLevel(state1) <= harvestLevel || harvestLevel >= 3){

						if(caster instanceof EntityPlayerMP){ // Everything in here is server-side only so this is fine

							boolean silkTouch = state1.getBlock().canSilkHarvest(world, pos1, state1, (EntityPlayer)caster)
									&& ItemArtefact.isArtefactActive((EntityPlayer)caster, WizardryItems.charm_silk_touch);

							// Some protection mods seem to use this event instead so let's trigger it to check
							if(ForgeEventFactory.getBreakSpeed((EntityPlayer)caster, state1, 1, pos1) <= 0) continue;

							int xp = ForgeHooks.onBlockBreakEvent(world,
									((EntityPlayerMP)caster).interactionManager.getGameType(), (EntityPlayerMP)caster, pos1);

							if(xp == -1) continue; // Event was cancelled

							if(silkTouch){
								flag = world.destroyBlock(pos1, false);
								if(flag){
									ItemStack stack = getSilkTouchDrop(state1);
									if(stack != null) Block.spawnAsEntity(world, pos1, stack);
								}
							}else{
								flag = world.destroyBlock(pos1, true);
								if(flag) state1.getBlock().dropXpOnBlockBreak(world, pos1, xp);
							}

						}else{
							// NPCs can dig the block under the target's feet
							flag = world.destroyBlock(pos1, true) || flag;
						}
					}
				}

				return flag;
			}
		}else{
			return true;
		}

		return false;
	}

	@Override
	protected boolean onMiss(World world, EntityLivingBase caster, Vec3d origin, Vec3d direction, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@Override
	protected void spawnParticle(World world, double x, double y, double z, double vx, double vy, double vz){
		ParticleBuilder.create(Type.DUST).pos(x, y, z).time(20 + world.rand.nextInt(5)).clr(0.9f, 0.95f, 1)
				.shaded(false).spawn(world);
	}

	private static ItemStack getSilkTouchDrop(IBlockState state){

		try {
			return (ItemStack)getSilkTouchDrop.invoke(state.getBlock(), state);
		}catch(IllegalAccessException | InvocationTargetException e){
			Wizardry.logger.error("Error while reflectively retrieving silk touch drop", e);
		}

		return null;
	}

}
