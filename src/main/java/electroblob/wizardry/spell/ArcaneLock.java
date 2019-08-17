package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.util.AllyDesignationSystem;
import electroblob.wizardry.util.NBTExtras;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class ArcaneLock extends SpellRay {

	/** The NBT tag name for storing the owner's UUID in the tile entity data. */
	public static final String NBT_KEY = "arcaneLockOwner";

	public ArcaneLock(){
		super("arcane_lock", false, EnumAction.NONE);
	}

	@Override public boolean requiresPacket(){ return true; }

	@Override public boolean canBeCastByDispensers(){ return false; }

	@Override public boolean canBeCastByNPCs(){ return false; }

	@Override
	protected boolean onEntityHit(World world, Entity target, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){
		
		if(caster instanceof EntityPlayer){
			
			TileEntity tileentity = world.getTileEntity(pos);

			if(tileentity != null){

				if(tileentity.getTileData().hasUniqueId(NBT_KEY)){
					// Unlocking
					if(world.getPlayerEntityByUUID(tileentity.getTileData().getUniqueId(NBT_KEY)) == caster){
						NBTExtras.removeUniqueId(tileentity.getTileData(), NBT_KEY);
						return true;
					}
				}else{
					// Locking
					tileentity.getTileData().setUniqueId(NBT_KEY, caster.getUniqueID());
					return true;
				}
			}
		}
		
		return false;
	}

	@Override
	protected boolean onMiss(World world, EntityLivingBase caster, Vec3d origin, Vec3d direction, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@SubscribeEvent
	public static void onLeftClickBlockEvent(PlayerInteractEvent.LeftClickBlock event){

		if(!canBypassLocks(event.getEntityPlayer())){

			TileEntity tileentity = event.getWorld().getTileEntity(event.getPos());

			// Prevents arcane-locked containers from being broken
			// Need to check if it has the unique id first because if it is absent getUniqueId will return the nil UUID
			if(tileentity != null && tileentity.getTileData().hasUniqueId(ArcaneLock.NBT_KEY)){
				// Only the player that owns the lock may break the container
				// If nobody owns it (i.e. it's part of a shrine), player will be null
				// Why is getUniqueId marked @Nullable? It literally creates a UUID and returns it!
				if(event.getEntityPlayer().getUniqueID() != tileentity.getTileData().getUniqueId(ArcaneLock.NBT_KEY)){
					event.setCanceled(true);
				}
			}
		}
	}

	@SubscribeEvent
	public static void onRightClickBlockEvent(PlayerInteractEvent.RightClickBlock event){

		if(!canBypassLocks(event.getEntityPlayer())){

			TileEntity tileentity = event.getWorld().getTileEntity(event.getPos());

			// Prevents arcane-locked containers from being opened
			// Need to check if it has the unique id first because if it is absent getUniqueId will return the nil UUID
			if(tileentity != null && tileentity.getTileData().hasUniqueId(ArcaneLock.NBT_KEY)){
				// Why is getUniqueId marked @Nullable? It literally creates a UUID and returns it!
				EntityPlayer owner = event.getWorld().getPlayerEntityByUUID(tileentity.getTileData().getUniqueId(ArcaneLock.NBT_KEY));
				// Only the player that owns the lock or an ally of that player may open the container
				// If nobody owns it (i.e. it's part of a shrine, or the owner logged out), player will be null
				// Unfortunately we can't get the owner's allies if the owner is offline
				// Perhaps if this crops up again we can store inverted 'ally of' maps as well?
				if(owner == null || (owner != event.getEntityPlayer() && !AllyDesignationSystem.isPlayerAlly(owner, event.getEntityPlayer()))){
					event.setCanceled(true);
				}
			}
		}
	}

	private static boolean canBypassLocks(EntityPlayer player){

		if(!player.isCreative()) return false;
		if(Wizardry.settings.creativeBypassesArcaneLock) return true;
		MinecraftServer server = player.world.getMinecraftServer();
		return server != null && WizardryUtilities.isPlayerOp(player, server);
	}

	@SubscribeEvent
	public static void onExplosionEvent(ExplosionEvent.Detonate event){
		// Prevents arcane-locked containers from being exploded
		event.getAffectedBlocks().removeIf(pos -> event.getWorld().getTileEntity(pos) != null
				&& event.getWorld().getTileEntity(pos).getTileData().hasUniqueId(NBT_KEY));
	}

}
