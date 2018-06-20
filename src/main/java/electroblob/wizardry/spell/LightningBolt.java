package electroblob.wizardry.spell;

import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.WizardryAdvancementTriggers;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class LightningBolt extends SpellRay {

	/** The NBT key used to store the UUID of the player that summoned the lightning bolt. Used for achievements. */
	public static final String NBT_KEY = "summoningPlayer";

	public LightningBolt(){
		super("lightning_bolt", Tier.ADVANCED, Element.LIGHTNING, SpellType.ATTACK, 40, 80, false, 200, null);
		this.ignoreEntities(true);
	}

	@Override public boolean doesSpellRequirePacket(){ return false; }

	@Override
	protected boolean onEntityHit(World world, Entity target, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		
		if(world.canBlockSeeSky(pos.up())){

			if(!world.isRemote){
				EntityLightningBolt entitylightning = new EntityLightningBolt(world, pos.getX(), pos.getY(),
						pos.getZ(), false);
				world.addWeatherEffect(entitylightning);

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
	protected boolean onMiss(World world, EntityLivingBase caster, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@SubscribeEvent
	public static void onEntityStruckByLightningEvent(EntityStruckByLightningEvent event){

		if(event.getLightning().getEntityData() != null && event.getLightning().getEntityData().hasUniqueId(NBT_KEY)){

			EntityPlayer player = (EntityPlayer)WizardryUtilities.getEntityByUUID(event.getLightning().world,
					event.getLightning().getEntityData().getUniqueId("summoningPlayer"));

			if(event.getEntity() instanceof EntityCreeper){
				WizardryAdvancementTriggers.charge_creeper.triggerFor(player);
			}

			if(event.getEntity() instanceof EntityPig){
				WizardryAdvancementTriggers.frankenstein.triggerFor(player);
			}
		}
	}
	
}
