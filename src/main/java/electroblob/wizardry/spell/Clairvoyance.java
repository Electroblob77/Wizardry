package electroblob.wizardry.spell;

import electroblob.wizardry.EnumElement;
import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.EnumSpellType;
import electroblob.wizardry.EnumTier;
import electroblob.wizardry.ExtendedPlayer;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.packet.PacketClairvoyance;
import electroblob.wizardry.packet.WizardryPacketHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumAction;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;

public class Clairvoyance extends Spell {
	
	/** The number of ticks it takes each path particle to move from one path point to the next. */
	public static final int PARTICLE_MOVEMENT_INTERVAL = 45;

	public Clairvoyance() {
		super(EnumTier.APPRENTICE, 20, EnumElement.SORCERY, "clairvoyance", EnumSpellType.UTILITY, 100, EnumAction.bow, false);
	}
	
	@Override
	public boolean doesSpellRequirePacket(){
		return false;
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, int ticksInUse, float damageMultiplier, float rangeMultiplier, float durationMultiplier, float blastMultiplier) {

		ExtendedPlayer properties = ExtendedPlayer.get(caster);

		if(properties != null && !caster.isSneaking()){
			if(caster.dimension == properties.clairvoyanceDimension){
				// Has to be y since x and z could reasonably be -1.
				if(properties.clairvoyanceY > -1){
					
					if(!world.isRemote) caster.addChatComponentMessage(new ChatComponentTranslation("spell.clairvoyance.searching"));

					// Parameters: entity, x, y, z, range, passOpenDoors, passClosedDoors, avoidWater, canSwim
					PathEntity path = world.getEntityPathToXYZ(caster, properties.clairvoyanceX, properties.clairvoyanceY, properties.clairvoyanceZ, 256*rangeMultiplier, true, true, false, true);

					// If the coordinates weren't checked, the path might not lead to the correct location.
					if(path != null && path.getFinalPathPoint() != null
							&& path.getFinalPathPoint().xCoord == properties.clairvoyanceX
							&& path.getFinalPathPoint().yCoord == properties.clairvoyanceY
							&& path.getFinalPathPoint().zCoord == properties.clairvoyanceZ){

						world.playSoundAtEntity(caster, "wizardry:aura", 1.0f, 1.0f);

						// TODO: When the chunk the destination is in is not loaded, the particles don't spawn.
						// I think I can fix this by sending the path positions in a packet to the caster, with the
						// added benefit of only spawning them on that player's client.
						if(!world.isRemote && caster instanceof EntityPlayerMP){
							WizardryPacketHandler.net.sendTo(new PacketClairvoyance.Message(path, durationMultiplier), (EntityPlayerMP)caster);
						}

						return true;

					}else{
						if(!world.isRemote) caster.addChatComponentMessage(new ChatComponentTranslation("spell.clairvoyance.outofrange"));
					}
				}else{
					if(!world.isRemote) caster.addChatComponentMessage(new ChatComponentTranslation("spell.clairvoyance.undefined"));
				}
			}else{
				if(!world.isRemote) caster.addChatComponentMessage(new ChatComponentTranslation("spell.clairvoyance.wrongdimension"));
			}
		}

		return false;
	}
	
	public static void spawnPathPaticles(World world, PathEntity path, float durationMultiplier) {
		
		PathPoint point, nextPoint;

		while(!path.isFinished()){
			
			point = path.getPathPointFromIndex(path.getCurrentPathIndex());
			if(point == path.getFinalPathPoint()) break;
			nextPoint = path.getCurrentPathLength() - path.getCurrentPathIndex() <= 2 ? path.getFinalPathPoint() : path.getPathPointFromIndex(path.getCurrentPathIndex() + 2);
			
			Wizardry.proxy.spawnParticle(EnumParticleType.PATH, world, point.xCoord + 0.5, point.yCoord + 0.5, point.zCoord + 0.5,
					(nextPoint.xCoord - point.xCoord)/(float)PARTICLE_MOVEMENT_INTERVAL,
					(nextPoint.yCoord - point.yCoord)/(float)PARTICLE_MOVEMENT_INTERVAL,
					(nextPoint.zCoord - point.zCoord)/(float)PARTICLE_MOVEMENT_INTERVAL,
					(int)(1800*durationMultiplier), 0, 1, 0.3f);
			
			path.incrementPathIndex();
			path.incrementPathIndex();
		}

		point = path.getFinalPathPoint();

		Wizardry.proxy.spawnParticle(EnumParticleType.PATH, world, point.xCoord + 0.5, point.yCoord + 0.5, point.zCoord + 0.5, 0, 0, 0, (int)(1800*durationMultiplier), 1, 1, 1);
	}

}