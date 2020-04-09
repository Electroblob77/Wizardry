package electroblob.wizardry.potion;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentFrostWalker;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Field;

@Mod.EventBusSubscriber
public class PotionFrostStep extends PotionMagicEffect implements ICustomPotionParticles {

	private static final Field prevBlockPos = ObfuscationReflectionHelper.findField(EntityLivingBase.class, "field_184620_bC");

	public PotionFrostStep(boolean isBadEffect, int liquidColour){
		super(isBadEffect, liquidColour, new ResourceLocation(Wizardry.MODID, "textures/gui/potion_icons/frost_step.png"));
		this.setPotionName("potion." + Wizardry.MODID + ":frost_step");
	}

//	@Override
//	public boolean isReady(int duration, int amplifier){
//		return true; // Execute the effect every tick
//	}

	@Override
	public void spawnCustomParticle(World world, double x, double y, double z){
		ParticleBuilder.create(Type.SNOW).pos(x, y, z).time(15 + world.rand.nextInt(5)).spawn(world);
	}

	// Use LivingUpdateEvent instead of performEffect because it gets called before the actual frost walker processing
	// performEffect is called afterwards, at which point prevBlockPos has already been set to the current position
	// regardless of whether the player is wearing frost walker boots or not

	@SubscribeEvent
	public static void onLivingUpdateEvent(LivingUpdateEvent event){

		EntityLivingBase host = event.getEntityLiving();

		if(host.isPotionActive(WizardryPotions.frost_step)){
			// Mimics the behaviour of the frost walker enchantment itself
			if(!host.world.isRemote){

				BlockPos currentPos = new BlockPos(host);

				try{

					if(!currentPos.equals(prevBlockPos.get(host))){

						prevBlockPos.set(host, currentPos);

						int strength = host.getActivePotionEffect(WizardryPotions.frost_step).getAmplifier();

						EnchantmentFrostWalker.freezeNearby(host, host.world, currentPos, strength);

						if(host instanceof EntityPlayer && ItemArtefact.isArtefactActive((EntityPlayer)host, WizardryItems.charm_lava_walking)){
							freezeNearbyLava(host, host.world, currentPos, strength);
						}
					}

				}catch(IllegalAccessException e){
					Wizardry.logger.error("Error accessing living entity previous block pos:", e);
				}
			}
		}
	}

	/** Copied from {@link EnchantmentFrostWalker#freezeNearby(EntityLivingBase, World, BlockPos, int)} and modified
	 * to turn lava to obsidian crust blocks instead. */
	private static void freezeNearbyLava(EntityLivingBase living, World world, BlockPos pos, int level){

		if(living.onGround){

			float f = (float)Math.min(16, 2 + level);
			BlockPos.MutableBlockPos pos1 = new BlockPos.MutableBlockPos(0, 0, 0);

			for(BlockPos.MutableBlockPos pos2 : BlockPos.getAllInBoxMutable(pos.add((double)(-f), -1.0D, (double)(-f)), pos.add((double)f, -1.0D, (double)f))){

				if(pos2.distanceSqToCenter(living.posX, living.posY, living.posZ) <= (double)(f * f)){

					pos1.setPos(pos2.getX(), pos2.getY() + 1, pos2.getZ());
					IBlockState state1 = world.getBlockState(pos1);

					if(state1.getMaterial() == Material.AIR){

						IBlockState state2 = world.getBlockState(pos2);

						if(state2.getMaterial() == Material.LAVA && (state2.getBlock() == Blocks.LAVA || state2.getBlock() == Blocks.FLOWING_LAVA) && state2.getValue(BlockLiquid.LEVEL) == 0 && world.mayPlace(WizardryBlocks.obsidian_crust, pos2, false, EnumFacing.DOWN, null)){
							world.setBlockState(pos2, WizardryBlocks.obsidian_crust.getDefaultState());
							world.scheduleUpdate(pos2.toImmutable(), WizardryBlocks.obsidian_crust, MathHelper.getInt(living.getRNG(), 60, 120));
						}
					}
				}
			}
		}
	}

}
