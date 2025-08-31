package electroblob.wizardry.tileentity;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.block.BlockPedestal;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.entity.living.EntityEvilWizard;
import electroblob.wizardry.entity.living.EntityWizard;
import electroblob.wizardry.packet.PacketConquerShrine;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.potion.PotionContainment;
import electroblob.wizardry.registry.WizardryBlocks;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.spell.ArcaneLock;
import electroblob.wizardry.util.*;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.util.*;

public class TileEntityShrineCore extends TileEntity implements ITickable {

	private static final double ACTIVATION_RADIUS = 5;

	private boolean activated = false;
	private boolean conquered = false;
	private long regenerationTime = 0;
	private long lastRegenerationTime = 0;
	private Element shrineElement;
	private AxisAlignedBB containmentField;
	private final UUID[] linkedWizards = new UUID[3];
	private TileEntity linkedContainer;
	private BlockPos linkedContainerPos; // Temporary stores the container position read from NBT until the world is set
	private final Set<UUID> lootedPlayers = new HashSet<>();

	@Override
	public void setPos(BlockPos pos) {
		super.setPos(pos);
		initContainmentField(pos);
	}

	private void initContainmentField(BlockPos pos) {
		float r = PotionContainment.getContainmentDistance(0);
		this.containmentField = new AxisAlignedBB(-r, -r, -r, r, r, r).offset(GeometryUtils.getCentre(pos));
	}

	public void linkContainer(TileEntity container) {
		this.linkedContainer = container;
	}

	public void setShrineElement(Element element) {
		this.shrineElement = element;
	}

	public boolean canPlayerLoot(EntityPlayer player) {
		if (Wizardry.settings.shrineAllowMultipleLoot) {
			return true; // Allow multiple looting if enabled
		}
		return !lootedPlayers.contains(player.getUniqueID());
	}

	public void recordPlayerLoot(EntityPlayer player) {
		if (!Wizardry.settings.shrineAllowMultipleLoot) {
			lootedPlayers.add(player.getUniqueID());
			this.markDirty();
		}
	}

	@Override
	public void update() {

		if (this.linkedContainer == null && this.linkedContainerPos != null) {
			this.linkContainer(world.getTileEntity(this.linkedContainerPos));
		}

		// Handle shrine regeneration
		if (conquered && Wizardry.settings.shrineRegenerationEnabled && regenerationTime > 0) {
			if (world.getTotalWorldTime() >= regenerationTime) {
				regenerate();
				return; // Don't process other logic during regeneration
			}
		}

		double x = this.pos.getX() + 0.5;
		double y = this.pos.getY() + 0.5;
		double z = this.pos.getZ() + 0.5;

		if (!activated && !conquered && world.getClosestPlayer(x, y, z, ACTIVATION_RADIUS, false) != null && (lastRegenerationTime == 0 || world.getTotalWorldTime() - lastRegenerationTime > 100)) { // 5 second delay after regeneration

			this.activated = true;

			if (world.isRemote) {
				ParticleBuilder.create(Type.SPHERE).pos(x, y + 1, z).clr(0xf06495).scale(5).time(12).spawn(world);
			}

			world.playSound(x, y, z, WizardrySounds.BLOCK_PEDESTAL_ACTIVATE, SoundCategory.BLOCKS, 1.5f, 1, false);

			if (!world.isRemote) {

				EntityEvilWizard[] wizards = new EntityEvilWizard[linkedWizards.length];

				for (int i = 0; i < linkedWizards.length; i++) {

					EntityEvilWizard wizard = new EntityEvilWizard(world);

					float angle = world.rand.nextFloat() * 2 * (float) Math.PI;
					double x1 = this.pos.getX() + 0.5 + 5 * MathHelper.sin(angle);
					double z1 = this.pos.getZ() + 0.5 + 5 * MathHelper.cos(angle);
					Integer y1 = BlockUtils.getNearestFloor(world, new BlockPos(x1, this.pos.getY(), z1), 8);
					if (y1 == null) {
						// Fallback to the position of the shrine core if it failed to find a position (unlikely)
						x1 = this.pos.getX() + 1; // Offset it so the wizard isn't inside the block
						y1 = this.pos.getY();
						z1 = this.pos.getZ();
					}

					wizard.setLocationAndAngles(x1, y1 + 0.5, z1, 0, 0);
					wizard.setElement(world.getBlockState(pos).getValue(BlockPedestal.ELEMENT));
					wizard.onInitialSpawn(world.getDifficultyForLocation(pos), null);
					wizard.hasStructure = true;

					world.spawnEntity(wizard);
					wizards[i] = wizard;
					linkedWizards[i] = wizard.getUniqueID();
				}

				for (EntityEvilWizard wizard : wizards) wizard.groupUUIDs.addAll(Arrays.asList(linkedWizards));
			}

			containNearbyTargets();
		}

		if (!areWizardsDead() && activated && world.getTotalWorldTime() % 20L == 0) containNearbyTargets();

		if (activated && areWizardsDead() && !world.isRemote) {
			conquer();
		}
	}

	private boolean areWizardsDead() {

		for (UUID uuid : linkedWizards) {
			Entity entity = EntityUtils.getEntityByUUID(world, uuid);
			if (entity instanceof EntityEvilWizard && entity.isEntityAlive()) return false;
		}

		return true;
	}

	public void conquer() {

		double x = this.pos.getX() + 0.5;
		double y = this.pos.getY() + 0.5;
		double z = this.pos.getZ() + 0.5;

		if (!world.isRemote) {

			WizardryPacketHandler.net.sendToAllAround(new PacketConquerShrine.Message(this.pos), new NetworkRegistry.TargetPoint(this.world.provider.getDimension(), x, y, z, 64));

			// Remove containment effects from nearby targets when shrine is conquered
			removeContainmentFromNearbyTargets();

			// If regeneration is enabled, schedule regeneration instead of removing the tile entity
			if (Wizardry.settings.shrineRegenerationEnabled) {
				this.conquered = true;
				this.regenerationTime = world.getTotalWorldTime() + (Wizardry.settings.shrineRegenerationTime * 1200L); // Convert minutes to ticks (20 ticks per second * 60 seconds)
				this.activated = false;
				Arrays.fill(this.linkedWizards, null); // Clear wizard references
				// Keep lootedPlayers list - it's permanent tracking for this shrine instance

				// Handle chest breaking and loot dropping
				handleChestLootDrop();

				// Mark the tile entity for update
				this.markDirty();

				if (world.getBlockState(pos).getBlock() == WizardryBlocks.runestone_pedestal) {
					// Keep the block state the same, just mark as conquered
					this.shrineElement = world.getBlockState(pos).getValue(BlockPedestal.ELEMENT);
				} else {
					Wizardry.logger.warn("What's going on?! A shrine core is being conquered but the block at its position is not a runestone pedestal!");
				}
			} else {
				// Original behavior: remove the tile entity
				BlockPos chestPos = this.pos.up();
				TileEntity chestTileEntity = world.getTileEntity(chestPos);
				if (chestTileEntity instanceof TileEntityChest) {
					TileEntityChest chest = (TileEntityChest) chestTileEntity;
					NBTExtras.removeUniqueId(chest.getTileData(), ArcaneLock.NBT_KEY);
					chest.markDirty();
					world.markAndNotifyBlock(pos, null, world.getBlockState(pos), world.getBlockState(pos), 3);
				}
				if (world.getBlockState(pos).getBlock() == WizardryBlocks.runestone_pedestal) {
					world.setBlockState(pos, WizardryBlocks.runestone_pedestal.getDefaultState().withProperty(BlockPedestal.ELEMENT, world.getBlockState(pos).getValue(BlockPedestal.ELEMENT)));
				} else {
					Wizardry.logger.warn("What's going on?! A shrine core is being conquered but the block at its position is not a runestone pedestal!");
				}
				world.markTileEntityForRemoval(this);
			}
		}

		if (linkedContainer == null) {
			linkedContainer = world.getTileEntity(pos.up());
		}

		if (linkedContainer != null) {
			NBTExtras.removeUniqueId(linkedContainer.getTileData(), ArcaneLock.NBT_KEY);
			//linkedContainer.getTileData().removeTag("arcaneLockOwnerMost");
			//linkedContainer.getTileData().removeTag("arcaneLockOwnerLeast");
		}
		world.playSound(x, y, z, WizardrySounds.BLOCK_PEDESTAL_CONQUER, SoundCategory.BLOCKS, 1, 1, false);

		if (world.isRemote) {
			ParticleBuilder.create(Type.SPHERE).scale(5).pos(x, y + 1, z).clr(0xf06495).time(12).spawn(world);
			for (int i = 0; i < 5; i++) {
				float brightness = 0.8f + world.rand.nextFloat() * 0.2f;
				ParticleBuilder.create(Type.SPARKLE, world.rand, x, y + 1, z, 1, true).clr(1, brightness, brightness).spawn(world);
			}
		}
	}

	private void regenerate() {

		if (world.isRemote) return;

		// Remove containment effects from nearby targets when shrine regenerates
		removeContainmentFromNearbyTargets();

		// Reset shrine state
		this.conquered = false;
		this.regenerationTime = 0;
		this.activated = false;
		Arrays.fill(this.linkedWizards, null);
		// Keep lootedPlayers list - it's permanent for this shrine instance

		// Force a short delay before allowing reactivation to prevent immediate re-activation
		this.lastRegenerationTime = world.getTotalWorldTime();

		// Forcibly replace whatever block is above the altar with a fresh chest
		BlockPos chestPos = this.pos.up();

		// Always replace the block above with a fresh chest during regeneration
		world.setBlockState(chestPos, Blocks.CHEST.getDefaultState());

		// Set up the loot table for the shrine chest
		TileEntity chestTileEntity = world.getTileEntity(chestPos);
		if (chestTileEntity instanceof TileEntityChest) {
			TileEntityChest chest = (TileEntityChest) chestTileEntity;
			chest.setLootTable(new ResourceLocation(Wizardry.MODID, "chests/shrine"), world.rand.nextLong());
		}

		// Link and apply arcane lock to the container
		if (chestTileEntity != null) {
			this.linkContainer(chestTileEntity);
			chestTileEntity.getTileData().setUniqueId(ArcaneLock.NBT_KEY, new UUID(0, 0)); // Nil UUID for shrine lock
			chestTileEntity.markDirty(); // Mark tile entity as dirty for client sync

			// Trigger visual update for arcane lock effect
			IBlockState blockState = world.getBlockState(chestPos);
			world.markAndNotifyBlock(chestPos, null, blockState, blockState, 3);
			world.notifyBlockUpdate(chestPos, blockState, blockState, 3); // Additional client sync
		}

		// Visual and audio effects for regeneration
		double x = this.pos.getX() + 0.5;
		double y = this.pos.getY() + 0.5;
		double z = this.pos.getZ() + 0.5;

		//	WizardryPacketHandler.net.sendToAllAround(new PacketConquerShrine.Message(this.pos), new NetworkRegistry.TargetPoint(this.world.provider.getDimension(), x, y, z, 64));

		if (world.isRemote) {
			ParticleBuilder.create(Type.SPHERE).pos(x, y + 1, z).clr(0xf06495).scale(5).time(12).spawn(world);
		}

		world.playSound(x, y, z, WizardrySounds.BLOCK_PEDESTAL_ACTIVATE, SoundCategory.BLOCKS, 1.5f, 1, false);

		this.markDirty();
	}

	private void handleChestLootDrop() {
		BlockPos chestPos = this.pos.up();

		// Check if there's a chest at the expected position
		if (world.getBlockState(chestPos).getBlock() == Blocks.CHEST) {
			TileEntity chestTileEntity = world.getTileEntity(chestPos);

			if (chestTileEntity instanceof TileEntityChest) {
				TileEntityChest chest = (TileEntityChest) chestTileEntity;

				// Find the player who conquered the shrine (closest player)
				EntityPlayer conqueringPlayer = world.getClosestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 20.0, false);

				if (conqueringPlayer != null && !canPlayerLoot(conqueringPlayer)) {
					// Send message to player that they've already looted this shrine
					if (!world.isRemote) {
						conqueringPlayer.sendMessage(new TextComponentTranslation("wizardry.shrine_already_looted"));
					}
					chest.setLootTable(null, world.rand.nextLong());
					if (world.getBlockState(chestPos).getBlock() == Blocks.CHEST) {
						world.setBlockToAir(chestPos);
					}
					return;
				}

				if (conqueringPlayer != null && canPlayerLoot(conqueringPlayer)) {
					// Record that this player has looted the shrine
					recordPlayerLoot(conqueringPlayer);
				}
				// If player has already looted, don't drop anything
			}
		}

		// Break the chest regardless
		if (world.getBlockState(chestPos).getBlock() == Blocks.CHEST) {
			world.setBlockToAir(chestPos);
		}
	}

	private void containNearbyTargets() {
		List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, containmentField, e -> e instanceof EntityPlayer || e instanceof EntityWizard || e instanceof EntityEvilWizard);

		for (EntityLivingBase entity : entities) {
			entity.addPotionEffect(new PotionEffect(WizardryPotions.containment, 219));
			NBTExtras.storeTagSafely(entity.getEntityData(), PotionContainment.ENTITY_TAG, NBTUtil.createPosTag(this.pos));
		}
	}

	private void removeContainmentFromNearbyTargets() {
//		List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, containmentField,
//				e -> e instanceof EntityPlayer || e instanceof EntityWizard || e instanceof EntityEvilWizard);
//
//		for(EntityLivingBase entity : entities){
//			// Remove the containment potion effect
//			if(entity.isPotionActive(WizardryPotions.containment)){
//				entity.removePotionEffect(WizardryPotions.containment);
//			}
//			// Also remove the containment position tag
//			if(entity.getEntityData().hasKey(PotionContainment.ENTITY_TAG)){
//				BlockPos containmentPos = NBTUtil.getPosFromTag(entity.getEntityData().getCompoundTag(PotionContainment.ENTITY_TAG));
//				// Only remove if the containment position matches this shrine
//				if(containmentPos.equals(this.pos)){
//					entity.getEntityData().removeTag(PotionContainment.ENTITY_TAG);
//				}
//			}
//		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {

		compound.setBoolean("activated", this.activated);
		compound.setBoolean("conquered", this.conquered);
		compound.setLong("regenerationTime", this.regenerationTime);
		compound.setLong("lastRegenerationTime", this.lastRegenerationTime);
		if (shrineElement != null) compound.setInteger("shrineElement", this.shrineElement.ordinal());
		if (linkedContainer != null)
			NBTExtras.storeTagSafely(compound, "linkedContainerPos", NBTUtil.createPosTag(linkedContainer.getPos()));

		NBTTagList wizardTagList = new NBTTagList();
		for (UUID uuid : linkedWizards) {
			if (uuid != null) wizardTagList.appendTag(NBTUtil.createUUIDTag(uuid));
		}
		NBTExtras.storeTagSafely(compound, "wizards", wizardTagList);

		NBTTagList playerTagList = new NBTTagList();
		for (UUID uuid : lootedPlayers) {
			playerTagList.appendTag(NBTUtil.createUUIDTag(uuid));
		}
		NBTExtras.storeTagSafely(compound, "lootedPlayers", playerTagList);

		return super.writeToNBT(compound);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {

		this.activated = compound.getBoolean("activated");
		this.conquered = compound.getBoolean("conquered");
		this.regenerationTime = compound.getLong("regenerationTime");
		this.lastRegenerationTime = compound.getLong("lastRegenerationTime");
		if (compound.hasKey("shrineElement"))
			this.shrineElement = Element.values()[compound.getInteger("shrineElement")];
		this.linkedContainerPos = NBTUtil.getPosFromTag(compound.getCompoundTag("linkedContainerPos"));

		NBTTagList wizardTagList = compound.getTagList("wizards", Constants.NBT.TAG_COMPOUND);
		int i = 0;
		for (NBTBase tag : wizardTagList) {
			if (tag instanceof NBTTagCompound) linkedWizards[i++] = NBTUtil.getUUIDFromTag((NBTTagCompound) tag);
			else Wizardry.logger.warn("Unexpected tag type in NBT tag list of compound tags!");
		}

		this.lootedPlayers.clear();
		if (compound.hasKey("lootedPlayers")) {
			NBTTagList playerTagList = compound.getTagList("lootedPlayers", Constants.NBT.TAG_COMPOUND);
			for (NBTBase tag : playerTagList) {
				if (tag instanceof NBTTagCompound) lootedPlayers.add(NBTUtil.getUUIDFromTag((NBTTagCompound) tag));
			}
		}

		super.readFromNBT(compound);
		// Must be after super
		initContainmentField(this.pos);
	}
}
