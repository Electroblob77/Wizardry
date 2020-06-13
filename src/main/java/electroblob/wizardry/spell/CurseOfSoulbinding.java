package electroblob.wizardry.spell;

import electroblob.wizardry.data.IStoredVariable;
import electroblob.wizardry.data.Persistence;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.integration.DamageSafetyChecker;
import electroblob.wizardry.item.SpellActions;
import electroblob.wizardry.registry.WizardryPotions;
import electroblob.wizardry.registry.WizardrySounds;
import electroblob.wizardry.util.*;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber
public class CurseOfSoulbinding extends SpellRay {

	public static final IStoredVariable<Set<UUID>> TARGETS_KEY = new IStoredVariable.StoredVariable<>("soulboundCreatures",
			s -> NBTExtras.listToNBT(s, NBTUtil::createUUIDTag),
			// For some reason gradle screams at me unless I explicitly declare the type of t here, despite IntelliJ being fine without it
			(NBTTagList t) -> new HashSet<>(NBTExtras.NBTToList(t, NBTUtil::getUUIDFromTag)),
			// Curse of soulbinding is lifted when the caster dies, but not when they switch dimensions.
			Persistence.DIMENSION_CHANGE);

	public CurseOfSoulbinding(){
		super("curse_of_soulbinding", false, SpellActions.POINT);
		this.soundValues(1, 1.1f, 0.2f);
		WizardData.registerStoredVariables(TARGETS_KEY);
	}

	@Override public boolean canBeCastBy(EntityLiving npc, boolean override) { return false; }
	// You can't damage a dispenser so this would be nonsense!
	@Override public boolean canBeCastBy(TileEntityDispenser dispenser) { return false; }

	@Override
	protected boolean onEntityHit(World world, Entity target, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){
		
		if(EntityUtils.isLiving(target) && caster instanceof EntityPlayer){
			WizardData data = WizardData.get((EntityPlayer)caster);
			if(data != null){
				// Return false if soulbinding failed (e.g. if the target is already soulbound)
				if(getSoulboundCreatures(data).add(target.getUniqueID())){
					// This will actually run out in the end, but only if you leave Minecraft running for 3.4 years
					((EntityLivingBase)target).addPotionEffect(new PotionEffect(WizardryPotions.curse_of_soulbinding, Integer.MAX_VALUE));
				}else{
					return false;
				}
			}
		}
		
		return true;
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onMiss(World world, EntityLivingBase caster, Vec3d origin, Vec3d direction, int ticksInUse, SpellModifiers modifiers){
		return true;
	}
	
	@Override
	protected void spawnParticle(World world, double x, double y, double z, double vx, double vy, double vz){
		ParticleBuilder.create(Type.DARK_MAGIC).pos(x, y, z).clr(0.4f, 0, 0).spawn(world);
		ParticleBuilder.create(Type.DARK_MAGIC).pos(x, y, z).clr(0.1f, 0, 0).spawn(world);
		ParticleBuilder.create(Type.SPARKLE).pos(x, y, z).time(12 + world.rand.nextInt(8)).clr(1, 0.8f, 1).spawn(world);
	}

	@SubscribeEvent
	public static void onLivingHurtEvent(LivingHurtEvent event){

		if(!event.getEntity().world.isRemote && event.getEntityLiving() instanceof EntityPlayer
				&& !event.getSource().isUnblockable() && !(event.getSource() instanceof IElementalDamage
						&& ((IElementalDamage)event.getSource()).isRetaliatory())){

			EntityPlayer player = (EntityPlayer)event.getEntityLiving();
			WizardData data = WizardData.get(player);

			if(data != null){

				for(Iterator<UUID> iterator = getSoulboundCreatures(data).iterator(); iterator.hasNext();){

					Entity entity = EntityUtils.getEntityByUUID(player.world, iterator.next());

					if(entity == null) iterator.remove();

					if(entity instanceof EntityLivingBase){
						// Retaliatory effect
						if(DamageSafetyChecker.attackEntitySafely(entity, MagicDamage.causeDirectMagicDamage(player,
								MagicDamage.DamageType.MAGIC, true), event.getAmount(), event.getSource().getDamageType(),
								DamageSource.MAGIC, false)){
							// Sound only plays if the damage succeeds
							entity.playSound(WizardrySounds.SPELL_CURSE_OF_SOULBINDING_RETALIATE, 1.0F, player.world.rand.nextFloat() * 0.2F + 1.0F);
						}
					}
				}

			}
		}
	}

	public static Set<UUID> getSoulboundCreatures(WizardData data){

		if(data.getVariable(TARGETS_KEY) == null){
			Set<UUID> result = new HashSet<>();
			data.setVariable(TARGETS_KEY, result);
			return result;

		}else return data.getVariable(TARGETS_KEY);
	}

}
