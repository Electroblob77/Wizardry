package electroblob.wizardry.spell;

import electroblob.wizardry.block.BlockTransportationStone;
import electroblob.wizardry.data.IStoredVariable;
import electroblob.wizardry.data.Persistence;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.packet.PacketTransportation;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Transportation extends Spell {

	public static final String TELEPORT_COUNTDOWN = "teleport_countdown";

	public static final int MAX_REMEMBERED_LOCATIONS = 4;

	// For some reason 'the diamond' doesn't work if I chain methods onto this. Type inference is weird.
	public static final IStoredVariable<List<Location>> LOCATIONS_KEY = new IStoredVariable.StoredVariable<List<Location>, NBTTagList>("stoneCirclePos",
			s -> NBTExtras.listToNBT(s, Location::toNBT), t -> new ArrayList<>(NBTExtras.NBTToList(t, Location::fromNBT)), Persistence.ALWAYS).setSynced();
	public static final IStoredVariable<Integer> COUNTDOWN_KEY = IStoredVariable.StoredVariable.ofInt("tpCountdown", Persistence.NEVER).withTicker(Transportation::update);

	public Transportation(){
		super("transportation", SpellActions.POINT_UP, false);
		addProperties(TELEPORT_COUNTDOWN);
		WizardData.registerStoredVariables(LOCATIONS_KEY, COUNTDOWN_KEY);
	}

	@Override
	public boolean requiresPacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		WizardData data = WizardData.get(caster);
		// Fixes the sound not playing in first person.
		if(world.isRemote) this.playSound(world, caster, ticksInUse, -1, modifiers);

		// Only works when the caster is in the same dimension.
		if(data != null){

			Integer countdown = data.getVariable(COUNTDOWN_KEY);

			if(countdown == null || countdown == 0){

				List<Location> locations = data.getVariable(Transportation.LOCATIONS_KEY);

				if(locations == null) data.setVariable(Transportation.LOCATIONS_KEY, locations = new ArrayList<>(Transportation.MAX_REMEMBERED_LOCATIONS));

				if(locations.isEmpty()){
					if(!world.isRemote) caster.sendStatusMessage(new TextComponentTranslation("spell." + this.getUnlocalisedName() + ".undefined"), true);
					return false;
				}

				if(ItemArtefact.isArtefactActive(caster, WizardryItems.charm_transportation)){

					List<Location> locationsInDimension = locations.stream().filter(l -> l.dimension == caster.dimension).collect(Collectors.toList());

					if(locationsInDimension.isEmpty()){
						if(!world.isRemote) caster.sendStatusMessage(new TextComponentTranslation("spell." + this.getUnlocalisedName() + ".wrongdimension"), true);
						return false;
					}

					Location destination = getLocationAimedAt(caster, locationsInDimension, 1);

					if(destination == null) return false; // None of them were aimed at

					if(attemptTravelTo(caster, world, destination.pos, modifiers)){
						// Move the selected destination to the end of the list, making it the 'most recent' one
						// This makes my life easier in update() below, and is a kind of useful feature too
						locations.remove(destination);
						locations.add(destination);
						if(!world.isRemote) data.sync();
						return true;
					}

				}else{

					Location destination = locations.get(locations.size() - 1); // The most recent one, or the only one

					if(destination.dimension == caster.dimension){
						return attemptTravelTo(caster, world, destination.pos, modifiers);
					}else{
						if(!world.isRemote) caster.sendStatusMessage(new TextComponentTranslation("spell." + this.getUnlocalisedName() + ".wrongdimension"), true);
					}

				}
			}
		}

		return false;
	}

	// The following four methods centralise and neaten up the code
	// Since the deviation angle is also used by the UI renderer, this also ensures they use the same calculation

	/** Returns the location from the given list that the give player is aiming at, or null if they are not aiming
	 * at any of them. */
	public static Location getLocationAimedAt(EntityPlayer player, List<Location> locations, float partialTicks){
		return locations.stream()
				.filter(l -> isLocationAimedAt(player, l.pos, partialTicks))
				.min(Comparator.comparingDouble(l -> getLookDeviationAngle(player, l.pos, partialTicks)))
				.orElse(null);
	}

	public static boolean isLocationAimedAt(EntityPlayer player, BlockPos pos, float partialTicks){

		Vec3d origin = player.getPositionEyes(partialTicks);
		Vec3d centre = GeometryUtils.getCentre(pos);
		Vec3d direction = centre.subtract(origin);
		double distance = direction.length();

		return getLookDeviationAngle(player, pos, partialTicks) < getIconSize(distance);
	}

	public static double getLookDeviationAngle(EntityPlayer player, BlockPos pos, float partialTicks){

		Vec3d origin = player.getPositionEyes(partialTicks);
		Vec3d look = player.getLook(partialTicks);
		Vec3d centre = GeometryUtils.getCentre(pos);
		Vec3d direction = centre.subtract(origin);
		double distance = direction.length();

		return Math.acos(direction.dotProduct(look) / distance); // Angle between a and b = acos((a.b) / (|a|*|b|))
	}

	public static double getIconSize(double distance){
		return 0.05 + 2/(distance + 5);
	}

	private boolean attemptTravelTo(EntityPlayer player, World world, BlockPos destination, SpellModifiers modifiers){

		WizardData data = WizardData.get(player);

		if(BlockTransportationStone.testForCircle(world, destination)){
			this.playSound(world, player, 0, -1, modifiers);
			player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 150, 0));
			data.setVariable(COUNTDOWN_KEY, getProperty(TELEPORT_COUNTDOWN).intValue());
			return true;
		}else{
			if(!world.isRemote) player.sendStatusMessage(new TextComponentTranslation("spell." + this.getUnlocalisedName() + ".missing"), true);
			return false;
		}
	}

	private static int update(EntityPlayer player, Integer countdown){

		if(countdown == null) return 0;

		if(!player.world.isRemote){

			WizardData data = WizardData.get(player);

			List<Location> locations = data.getVariable(Transportation.LOCATIONS_KEY);
			if(locations == null || locations.isEmpty()) return 0;

			// If the location was selected, either it was already at the end of the list or it was moved there
			Location destination = locations.get(locations.size() - 1);

			if(countdown == 1 && destination.dimension == player.dimension){
				player.setPositionAndUpdate(destination.pos.getX() + 0.5, destination.pos.getY(), destination.pos.getZ() + 0.5);
				player.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 50, 0));
				IMessage msg = new PacketTransportation.Message(player.getEntityId());
				WizardryPacketHandler.net.sendToDimension(msg, player.world.provider.getDimension());
			}

			if(countdown > 0){
				countdown--;
			}
		}

		return countdown;
	}

}
