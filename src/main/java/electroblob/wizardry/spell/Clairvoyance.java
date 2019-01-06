package electroblob.wizardry.spell;

import electroblob.wizardry.WizardData;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.item.ItemWand;
import electroblob.wizardry.packet.PacketClairvoyance;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WandHelper;
import electroblob.wizardry.util.WizardryPathFinder;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class Clairvoyance extends Spell {

	/** The number of ticks it takes each path particle to move from one path point to the next. */
	public static final int PARTICLE_MOVEMENT_INTERVAL = 45;

	public Clairvoyance(){
		super("clairvoyance", Tier.APPRENTICE, Element.SORCERY, SpellType.UTILITY, 20, 100, EnumAction.BOW, false);
	}

	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		WizardData properties = WizardData.get(caster);

		if(properties != null && !caster.isSneaking()){
			if(caster.dimension == properties.getClairvoyanceDimension()){
				if(properties.getClairvoyanceLocation() != null){

					if(!world.isRemote) caster.sendStatusMessage(new TextComponentTranslation("spell." + this.getUnlocalisedName() + ".searching"), true);

					EntityZombie arbitraryZombie = new EntityZombie(world){
						@Override
						public float getBlockPathWeight(BlockPos pos){
							return 0;
						}
					};
					arbitraryZombie.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE)
							.setBaseValue(256 * modifiers.get(WizardryItems.range_upgrade));
					arbitraryZombie.setPosition(caster.posX, caster.posY, caster.posZ);
					arbitraryZombie.setPathPriority(PathNodeType.WATER, 0.0F);
					arbitraryZombie.onGround = true;

					BlockPos destination = properties.getClairvoyanceLocation();

					WizardryPathFinder pathfinder = new WizardryPathFinder(arbitraryZombie.getNavigator().getNodeProcessor());

					Path path = pathfinder.findPath(world, arbitraryZombie, destination, 256 * modifiers.get(WizardryItems.range_upgrade));

					if(path != null && path.getFinalPathPoint() != null){

						int x = path.getFinalPathPoint().x;
						int y = path.getFinalPathPoint().y;
						int z = path.getFinalPathPoint().z;

						if(x == destination.getX() && y == destination.getY() && z == destination.getZ()){

							WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_CONJURATION, 1.0f, 1.0f);

							if(!world.isRemote && caster instanceof EntityPlayerMP){
								WizardryPacketHandler.net.sendTo(new PacketClairvoyance.Message(path, modifiers.get(WizardryItems.duration_upgrade)),
										(EntityPlayerMP)caster);
							}

							return true;
						}
					}

					if(!world.isRemote) caster.sendStatusMessage(new TextComponentTranslation("spell." + this.getUnlocalisedName() + ".outofrange"), true);

				}else{
					if(!world.isRemote) caster.sendStatusMessage(new TextComponentTranslation("spell." + this.getUnlocalisedName() + ".undefined"), true);
				}
			}else{
				if(!world.isRemote) caster.sendStatusMessage(new TextComponentTranslation("spell." + this.getUnlocalisedName() + ".wrongdimension"), true);
			}
		}

		// Fixes the problem with the sound not playing for the client of the caster.
		if(world.isRemote) WizardryUtilities.playSoundAtPlayer(caster, WizardrySounds.SPELL_CONJURATION, 1.0f, 1.0f);

		return false;
	}

	public static void spawnPathPaticles(World world, Path path, float durationMultiplier){

		PathPoint point, nextPoint;

		while(!path.isFinished()){

			point = path.getPathPointFromIndex(path.getCurrentPathIndex());
			if(point == path.getFinalPathPoint()) break;
			nextPoint = path.getCurrentPathLength() - path.getCurrentPathIndex() <= 2 ? path.getFinalPathPoint()
					: path.getPathPointFromIndex(path.getCurrentPathIndex() + 2);

			ParticleBuilder.create(Type.PATH).pos(point.x + 0.5, point.y + 0.5, point.z + 0.5).vel(
					(nextPoint.x - point.x) / (float)PARTICLE_MOVEMENT_INTERVAL,
					(nextPoint.y - point.y) / (float)PARTICLE_MOVEMENT_INTERVAL,
					(nextPoint.z - point.z) / (float)PARTICLE_MOVEMENT_INTERVAL)
			.time((int)(1800 * durationMultiplier)).clr(0, 1, 0.3f).spawn(world);

			path.incrementPathIndex();
			path.incrementPathIndex();
		}

		point = path.getFinalPathPoint();

		ParticleBuilder.create(Type.PATH).pos(point.x + 0.5, point.y + 0.5, point.z + 0.5)
		.time((int)(1800 * durationMultiplier)).clr(1, 1, 1).spawn(world);
	}

	@SubscribeEvent
	public static void onRightClickBlockEvent(PlayerInteractEvent.RightClickBlock event){

		if(event.getEntityPlayer().isSneaking()){

			// The event now has an ItemStack, which greatly simplifies hand-related stuff.
			ItemStack wand = event.getItemStack();

			if(wand.getItem() instanceof ItemWand && WandHelper.getCurrentSpell(wand) instanceof Clairvoyance){

				WizardData properties = WizardData.get(event.getEntityPlayer());

				if(properties != null){
					// THIS is why BlockPos is a thing - in 1.7.10 this requires a clumsy switch statement.
					BlockPos pos = event.getPos().offset(event.getFace());

					properties.setClairvoyancePoint(pos, event.getWorld().provider.getDimension());

					if(!event.getWorld().isRemote){
						event.getEntityPlayer().sendStatusMessage(
								new TextComponentTranslation("spell." + Spells.clairvoyance.getUnlocalisedName() + ".confirm", Spells.clairvoyance.getNameForTranslationFormatted()), true);
					}

					event.setCanceled(true);
				}
			}
		}
	}

}