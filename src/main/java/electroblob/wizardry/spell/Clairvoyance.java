package electroblob.wizardry.spell;

import electroblob.wizardry.data.IStoredVariable;
import electroblob.wizardry.data.Persistence;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.item.ISpellCastingItem;
import electroblob.wizardry.misc.WizardryPathFinder;
import electroblob.wizardry.packet.PacketClairvoyance;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.tileentity.TileEntityDispenser;
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

	public static final IStoredVariable<BlockPos> LOCATION_KEY = IStoredVariable.StoredVariable.ofBlockPos("clairvoyancePos", Persistence.ALWAYS);
	public static final IStoredVariable<Integer> DIMENSION_KEY = IStoredVariable.StoredVariable.ofInt("clairvoyanceDimension", Persistence.ALWAYS);

	public Clairvoyance(){
		super("clairvoyance", EnumAction.BOW, false);
		addProperties(RANGE, DURATION);
		WizardData.registerStoredVariables(LOCATION_KEY, DIMENSION_KEY);
	}

	@Override public boolean canBeCastBy(EntityLiving npc, boolean override) { return false; }
	@Override public boolean canBeCastBy(TileEntityDispenser dispenser) { return false; }

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		WizardData data = WizardData.get(caster);

		if(data != null && !caster.isSneaking()){

			Integer dimension = data.getVariable(DIMENSION_KEY);
			BlockPos location = data.getVariable(LOCATION_KEY);

			if(dimension != null && caster.dimension == dimension){
				if(location != null){

					if(!world.isRemote) caster.sendStatusMessage(new TextComponentTranslation("spell." + this.getUnlocalisedName() + ".searching"), true);

					EntityZombie arbitraryZombie = new EntityZombie(world){
						@Override
						public float getBlockPathWeight(BlockPos pos){
							return 0;
						}
					};
					arbitraryZombie.getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE)
							.setBaseValue(getProperty(RANGE).doubleValue() * modifiers.get(WizardryItems.range_upgrade));
					arbitraryZombie.setPosition(caster.posX, caster.posY, caster.posZ);
					arbitraryZombie.setPathPriority(PathNodeType.WATER, 0.0F);
					arbitraryZombie.onGround = true;

					WizardryPathFinder pathfinder = new WizardryPathFinder(arbitraryZombie.getNavigator().getNodeProcessor());

					Path path = pathfinder.findPath(world, arbitraryZombie, location,
							getProperty(RANGE).floatValue() * modifiers.get(WizardryItems.range_upgrade));

					if(path != null && path.getFinalPathPoint() != null){

						int x = path.getFinalPathPoint().x;
						int y = path.getFinalPathPoint().y;
						int z = path.getFinalPathPoint().z;

						if(x == location.getX() && y == location.getY() && z == location.getZ()){

							this.playSound(world, caster, ticksInUse, -1, modifiers);

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
		if(world.isRemote) this.playSound(world, caster, ticksInUse, -1, modifiers);

		return false;
	}

	public static void spawnPathPaticles(World world, Path path, float durationMultiplier){

		// A bit annoying that we have to use the reference here but there's no easy way around it
		float duration = Spells.clairvoyance.getProperty(DURATION).floatValue();

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
			.time((int)(duration * durationMultiplier)).clr(0, 1, 0.3f).spawn(world);

			path.incrementPathIndex();
			path.incrementPathIndex();
		}

		point = path.getFinalPathPoint();

		ParticleBuilder.create(Type.PATH).pos(point.x + 0.5, point.y + 0.5, point.z + 0.5)
		.time((int)(duration * durationMultiplier)).clr(1f, 1f, 1f).spawn(world);
	}

	@SubscribeEvent
	public static void onRightClickBlockEvent(PlayerInteractEvent.RightClickBlock event){

		if(event.getEntityPlayer().isSneaking()){

			// The event now has an ItemStack, which greatly simplifies hand-related stuff.
			ItemStack stack = event.getItemStack();

			if(stack.getItem() instanceof ISpellCastingItem
					&& ((ISpellCastingItem)stack.getItem()).getCurrentSpell(stack) instanceof Clairvoyance){

				WizardData data = WizardData.get(event.getEntityPlayer());

				if(data != null){

					BlockPos pos = event.getPos().offset(event.getFace());

					data.setVariable(LOCATION_KEY, pos);
					data.setVariable(DIMENSION_KEY, event.getWorld().provider.getDimension());

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