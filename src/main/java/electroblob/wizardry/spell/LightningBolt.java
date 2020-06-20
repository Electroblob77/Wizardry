package electroblob.wizardry.spell;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.util.SpellModifiers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

//@Mod.EventBusSubscriber
public class LightningBolt extends SpellRay {

	/** The NBT key used to store the UUID of the player that summoned the lightning bolt. Used for achievements. */
	public static final String NBT_KEY = "summoningPlayer";

	public LightningBolt(){
		super("lightning_bolt", SpellActions.POINT, false);
		this.ignoreLivingEntities(true);
	}

	@Override public boolean requiresPacket(){ return false; }

	@Override
	protected boolean onEntityHit(World world, Entity target, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){
		
		if(world.canBlockSeeSky(pos.up())){

			if(!world.isRemote){
				// Temporarily disable the fire tick gamerule if player block damage is disabled
				// Bit of a hack but it works fine!
				boolean doFireTick = world.getGameRules().getBoolean("doFireTick");
				if(doFireTick && !Wizardry.settings.playerBlockDamage) world.getGameRules().setOrCreateGameRule("doFireTick", "false");
				EntityLightningBolt entitylightning = new EntityLightningBolt(world, pos.getX(), pos.getY(),
						pos.getZ(), false);
				world.addWeatherEffect(entitylightning);
				// Reset doFireTick to true if it was true before
				if(doFireTick && !Wizardry.settings.playerBlockDamage) world.getGameRules().setOrCreateGameRule("doFireTick", "true");

				// Code for eventhandler recognition for achievements
				if(caster instanceof EntityPlayer){
					NBTTagCompound entityNBT = entitylightning.getEntityData();
					entityNBT.setUniqueId(NBT_KEY, caster.getUniqueID());
				}
			}

			return true;
		}
		
		return false;
	}

	@Override
	protected boolean onMiss(World world, EntityLivingBase caster, Vec3d origin, Vec3d direction, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

//	@SubscribeEvent
//	public static void onEntityStruckByLightningEvent(EntityStruckByLightningEvent event){
//
//		if(event.getLightning().getEntityData() != null && event.getLightning().getEntityData().hasUniqueId(NBT_KEY)){
//
//			EntityPlayer player = (EntityPlayer)WizardryUtilities.getEntityByUUID(event.getLightning().world,
//					event.getLightning().getEntityData().getUniqueId("summoningPlayer"));
//
//			if(event.getEntity() instanceof EntityCreeper){
//				WizardryAdvancementTriggers.charge_creeper.triggerFor(player);
//			}
//
//			if(event.getEntity() instanceof EntityPig){
//				WizardryAdvancementTriggers.frankenstein.triggerFor(player);
//			}
//		}
//	}
	
}
