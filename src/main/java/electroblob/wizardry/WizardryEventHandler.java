package electroblob.wizardry;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import electroblob.wizardry.MagicDamage.DamageType;
import electroblob.wizardry.entity.EntityArc;
import electroblob.wizardry.entity.construct.EntityBubble;
import electroblob.wizardry.entity.construct.EntityDecay;
import electroblob.wizardry.entity.living.EntityEvilWizard;
import electroblob.wizardry.entity.living.EntitySilverfishMinion;
import electroblob.wizardry.entity.living.EntityWizard;
import electroblob.wizardry.item.ItemFlamingAxe;
import electroblob.wizardry.item.ItemFrostAxe;
import electroblob.wizardry.item.ItemSpectralArmour;
import electroblob.wizardry.item.ItemSpectralBow;
import electroblob.wizardry.item.ItemSpectralPickaxe;
import electroblob.wizardry.item.ItemSpectralSword;
import electroblob.wizardry.item.ItemWand;
import electroblob.wizardry.item.ItemWizardArmour;
import electroblob.wizardry.spell.Clairvoyance;
import electroblob.wizardry.spell.FreezingWeapon;
import electroblob.wizardry.spell.MindControl;
import electroblob.wizardry.spell.ShadowWard;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.spell.Intimidate;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;

public class WizardryEventHandler {
	
	@SubscribeEvent
	public void onEntityConstructingEvent(EntityConstructing event){
		if(event.entity instanceof EntityPlayer && ExtendedPlayer.get((EntityPlayer)event.entity) == null){
			ExtendedPlayer.register((EntityPlayer) event.entity);
		}
	}
	
	@SubscribeEvent
	public void onWorldLoadEvent(WorldEvent.Load event){
		if(!event.world.isRemote && event.world.provider.dimensionId == 0){
			SpellGlyphData.get(event.world);
		}
	}

	/* There is a subtle but important difference between LivingAttackEvent and LivingHurtEvent - LivingAttackEvent
	 * fires immediately when attackEntityFrom is called, whereas LivingHurtEvent only fires if the attack actually
	 * succeeded, i.e. if the entity in question takes damage (though the event is fired before that so you can cancel
	 * the damage). Things are processed in the following order:
	 * * LivingAttackEvent *
	 * - Invulnerability
	 * - Already-dead-ness
	 * - Fire resistance
	 * - Helmets vs. falling things
	 * - Hurt resistant time
	 * - Invulnerability (again)
	 * * LivingHurtEvent *
	 * - Armour
	 * - Potions
	 * - Health is finally changed
	 * Of course, there are no guarantees that other mods hooking into these two events will be called before or after
	 * yours, but you can have some degree of control by choosing which event to use.
	 * EDIT: Actually, there are. Firstly, you can set a priority in the @SubscribeEvent annotation which defines how
	 * early (higher priority) or late (lower priority) the method is called. Methods with the same priority are sorted
	 * alphabetically by mod id (so it's safe to assume wizardry would be fairly late on!). I wonder if there are any
	 * conventions for what sort of things take what priority...? */

	@SubscribeEvent
	public void onLivingAttackEvent(LivingAttackEvent event){

		// Prevents any damage to allies from magic if friendly fire is enabled
		if(!Wizardry.friendlyFire && event.source != null && event.source.getEntity() instanceof EntityPlayer
				&& event.entity instanceof EntityPlayer && event.source instanceof IElementalDamage){
			if(WizardryUtilities.isPlayerAlly((EntityPlayer)event.source.getEntity(), (EntityPlayer)event.entity)){
				event.setCanceled(true);
				// I think this ought to be here, since if the event is cancelled nothing else needs to happen.
				return;
			}
		}
		
		if(event.source instanceof IElementalDamage){
			if(MagicDamage.isEntityImmune(((IElementalDamage)event.source).getType(), event.entity)){
				event.setCanceled(true);
				// I would have liked to have done the 'resist' chat message here, but I overlooked the fact that I
				// would need an instance of the spell to get its display name!
				return;
			}
			// One convenient side effect of the new damage type system is that I can get rid of all the places where
			// creepers are charged and just put them here under shock damage - this is precisely the sort of
			// repetitive code I was trying to get rid of, since errors can (and did!) occur.
			if(event.entityLiving instanceof EntityCreeper && !((EntityCreeper)event.entityLiving).getPowered()
					&& ((IElementalDamage)event.source).getType() == DamageType.SHOCK){
				// Charges creepers when they are hit by shock damage
		        event.entityLiving.getDataWatcher().updateObject(17, Byte.valueOf((byte)1));
		        // Gives the player that caused the shock damage the 'It's Gonna Blow' achievement
				if(event.source.getEntity() instanceof EntityPlayer){
					((EntityPlayer)event.source.getEntity()).triggerAchievement(Wizardry.chargeCreeper);
				}
			}
		}

		// Bursts bubble when the creature inside takes damage
		if(event.entityLiving.ridingEntity instanceof EntityBubble &&
				!((EntityBubble)event.entityLiving.ridingEntity).isDarkOrb){
			event.entityLiving.ridingEntity.worldObj.playSoundAtEntity(event.entityLiving.ridingEntity, "random.pop", 1.5f, 1.0f);
			event.entityLiving.ridingEntity.setDead();
		}

		// Prevents all unblockable damage while transience is active
		if(event.entityLiving.isPotionActive(Wizardry.transience) && event.source != null && !event.source.isUnblockable()){
			event.setCanceled(true);
			// Again, I think this ought to be here, since if the event is cancelled nothing else needs to happen.
			return;
		}

		if(event.source != null && event.source.getEntity() instanceof EntityLivingBase){

			// Cancels the mind trick effect if the creature takes damage
			// This has been moved to within the (event.source.getEntity() instanceof EntityLivingBase) check so it doesn't
			// crash the game with a ConcurrentModificationException. If you think about it, mind trick only ought to be
			// cancelled if something attacks the entity since potions, drowning, cacti etc. don't affect the targeting.
			if(event.entityLiving.isPotionActive(Wizardry.mindTrick)){
				event.entityLiving.removePotionEffect(Wizardry.mindTrick.id);
			}

			// 'Revenge' effects

			EntityLivingBase attacker = (EntityLivingBase)event.source.getEntity();
			World world = event.entityLiving.worldObj;

			ItemStack wand = event.entityLiving.getHeldItem();

			if(event.entityLiving.isPotionActive(Wizardry.fireskin) && !event.source.isProjectile()){
				if(!MagicDamage.isEntityImmune(DamageType.FIRE, event.entityLiving)) attacker.setFire(5);
			}

			if(event.entityLiving.isPotionActive(Wizardry.iceShroud) && !event.source.isProjectile()){
				if(!MagicDamage.isEntityImmune(DamageType.FROST, event.entityLiving))
					attacker.addPotionEffect(new PotionEffect(Wizardry.frost.id, 100, 0, true));
			}

			if(event.entityLiving.isPotionActive(Wizardry.staticAura) && !event.source.isProjectile()){
				if(!world.isRemote){
					EntityArc arc = new EntityArc(world);
					arc.setEndpointCoords(event.entityLiving.posX, event.entityLiving.posY + 1, event.entityLiving.posZ,
							attacker.posX, attacker.posY + attacker.height/2, attacker.posZ);
					world.spawnEntityInWorld(arc);
				}else{
					for(int i=0;i<8;i++){
						Wizardry.proxy.spawnParticle(EnumParticleType.SPARK, world, attacker.posX + world.rand.nextFloat() - 0.5, WizardryUtilities.getEntityFeetPos(attacker) + attacker.height/2 + world.rand.nextFloat()*2 - 1, attacker.posZ + world.rand.nextFloat() - 0.5, 0, 0, 0, 3);
						world.spawnParticle("largesmoke", attacker.posX + world.rand.nextFloat() - 0.5, WizardryUtilities.getEntityFeetPos(attacker) + attacker.height/2 + world.rand.nextFloat()*2 - 1, attacker.posZ + world.rand.nextFloat() - 0.5, 0, 0, 0);
					}
				}

				attacker.attackEntityFrom(MagicDamage.causeDirectMagicDamage(event.entityLiving, DamageType.SHOCK), 4.0f);
				world.playSoundAtEntity(attacker, "wizardry:arc", 1.0F, world.rand.nextFloat() * 0.4F + 1.5F);
			}

			// Shadow ward
			if(event.entityLiving instanceof EntityPlayer){
				if(((EntityPlayer)event.entityLiving).isUsingItem() && wand != null && wand.getItemDamage() < wand.getMaxDamage()
						&& wand.getItem() instanceof ItemWand && WandHelper.getCurrentSpell(wand) instanceof ShadowWard
						&& !event.source.isUnblockable()){

					event.setCanceled(true);
					event.entityLiving.attackEntityFrom(DamageSource.magic, event.ammount/2);
					attacker.attackEntityFrom(MagicDamage.causeDirectMagicDamage(event.entityLiving, DamageType.MAGIC), event.ammount/2);
				}
			}

			if(attacker.isPotionActive(Wizardry.transience)){
				event.setCanceled(true);
			}

			// This behaviour has been removed, but is left here in case it is needed in future.
			/*
			if(attacker.isPotionActive(Wizardry.fireskin) && !event.source.isProjectile()){
				event.entityLiving.setFire(5);
			}

			if(attacker.isPotionActive(Wizardry.iceShroud) && !event.source.isProjectile()){
				event.entityLiving.addPotionEffect(new PotionEffect(Wizardry.frost.id, 100, 0, true));
			}
			*/
		}
	}

	@SubscribeEvent
	public void onLivingHurtEvent(LivingHurtEvent event){
		
		// Curse of soulbinding
		if(!event.entity.worldObj.isRemote && event.entityLiving instanceof EntityPlayer && !event.source.isUnblockable()){
			ExtendedPlayer properties = ExtendedPlayer.get((EntityPlayer)event.entityLiving);
			if(properties != null){
				properties.damageAllSoulboundCreatures(event.ammount);
			}
		}
		
		// Flaming and freezing swords
		if(event.source.getEntity() instanceof EntityLivingBase){
			EntityLivingBase attacker = (EntityLivingBase)event.source.getEntity();
			if(attacker.getHeldItem() != null && attacker.getHeldItem().getItem() instanceof ItemSword){
				
				int level = EnchantmentHelper.getEnchantmentLevel(Wizardry.flamingWeapon.effectId, attacker.getHeldItem());
				
				if(level > 0 && !MagicDamage.isEntityImmune(DamageType.FIRE, event.entityLiving))
					event.entityLiving.setFire(level*4);
				
				level = EnchantmentHelper.getEnchantmentLevel(Wizardry.freezingWeapon.effectId, attacker.getHeldItem());
				// Frost lasts for longer because it doesn't do any actual damage
				if(level > 0 && !MagicDamage.isEntityImmune(DamageType.FROST, event.entityLiving))
					event.entityLiving.addPotionEffect(new PotionEffect(Wizardry.frost.id, level*200, 0, true));
			}
		}
		
		// Freezing bow
		if(event.source.getSourceOfDamage() instanceof EntityArrow && event.source.getSourceOfDamage().getEntityData() != null){
			
			int level = event.source.getSourceOfDamage().getEntityData().getInteger(FreezingWeapon.FREEZING_ARROW_NBT_KEY);
			
			if(level > 0 && !MagicDamage.isEntityImmune(DamageType.FROST, event.entityLiving))
				event.entityLiving.addPotionEffect(new PotionEffect(Wizardry.frost.id, level*150, 0, true));
		}
		
		// Damage scaling
		if(event.source != null && event.source instanceof IElementalDamage){
			
			if(event.source.getEntity() instanceof EntityPlayer){
				event.ammount *= Wizardry.playerDamageScale;
			}else{
				event.ammount *= Wizardry.npcDamageScale;
			}
		}
	}

	@SubscribeEvent
	public void onBlockPlaceEvent(BlockEvent.PlaceEvent event){

		if(event.player.isPotionActive(Wizardry.transience)){
			event.setCanceled(true);
			return;
		}

		// Spectral blocks cannot be built on
		if(event.placedAgainst == Wizardry.spectralBlock){
			event.setCanceled(true);
			return;
		}
	}

	@SubscribeEvent
	public void onBlockBreakEvent(BlockEvent.BreakEvent event){
		
		if(event.getPlayer().isPotionActive(Wizardry.transience)){
			event.setCanceled(true);
			return;
		}

		// Makes wizards angry if a player breaks a block in their tower
		if(!(event.getPlayer() instanceof FakePlayer)){

			List<EntityWizard> wizards = WizardryUtilities.getEntitiesWithinRadius(64, event.x, event.y, event.z, event.world, EntityWizard.class);

			if(!wizards.isEmpty()){
				for(EntityWizard wizard : wizards){
					if(wizard.isBlockPartOfTower(event.x, event.y, event.z)){
						wizard.setRevengeTarget(event.getPlayer());
						event.getPlayer().triggerAchievement(Wizardry.angerWizard);
					}
				}
			}
		}
	}

	@SubscribeEvent
	public void onEntityStruckByLightningEvent(EntityStruckByLightningEvent event){

		if(event.lightning.getEntityData() != null && event.lightning.getEntityData().hasKey("summoningPlayer")){

			EntityPlayer player = (EntityPlayer)WizardryUtilities.getEntityByUUID(event.lightning.worldObj, UUID.fromString(event.lightning.getEntityData().getString("summoningPlayer")));

			if(event.entity instanceof EntityCreeper){
				player.triggerAchievement(Wizardry.chargeCreeper);
			}

			if(event.entity instanceof EntityPig){
				player.triggerAchievement(Wizardry.frankenstein);
			}
		}

	}

	@SubscribeEvent
	public void onLivingUpdateEvent(LivingUpdateEvent event){

		if(event.entityLiving instanceof EntityPlayer){
			
			EntityPlayer entityplayer = (EntityPlayer)event.entityLiving;
			
			if(ExtendedPlayer.get(entityplayer) != null){
				ExtendedPlayer.get(entityplayer).update(entityplayer);
			}

			if(entityplayer.openContainer instanceof ContainerWorkbench){
				this.craftingTableTick(entityplayer);
			}

			if(entityplayer.openContainer instanceof ContainerPlayer){
				// Unfortunately I have no choice but to call this method every tick when the player isn't using another
				// inventory, since the only thing tracking whether the player is looking at their inventory is the GUI
				// itself, which is client-side only.
				this.playerInventoryTick(entityplayer);
			}

			if(entityplayer.getCurrentArmor(0) != null && entityplayer.getCurrentArmor(0).getItem() instanceof ItemWizardArmour
					&& entityplayer.getCurrentArmor(1) != null && entityplayer.getCurrentArmor(1).getItem() instanceof ItemWizardArmour
					&& entityplayer.getCurrentArmor(2) != null && entityplayer.getCurrentArmor(2).getItem() instanceof ItemWizardArmour
					&& entityplayer.getCurrentArmor(3) != null && entityplayer.getCurrentArmor(3).getItem() instanceof ItemWizardArmour){

				entityplayer.triggerAchievement(Wizardry.armourSet);
			}

			// Tests for magic weapons, decrements the time until the magic wears off, and removes the enchantment if
			// it has run out.

			magicWeapons:
			if(ExtendedPlayer.get(entityplayer) != null){

				for(ItemStack stack : entityplayer.inventory.mainInventory){

					if(stack != null && stack.isItemEnchanted()){

						Map enchantments = EnchantmentHelper.getEnchantments(stack);

						if(enchantments.containsKey(Wizardry.magicSword.effectId) || enchantments.containsKey(Wizardry.magicBow.effectId)){

							if(ExtendedPlayer.get(entityplayer).magicWeaponDuration > 0){
								// Decrements the time until the magic wears off
								ExtendedPlayer.get(entityplayer).magicWeaponDuration--;
							}else{
								// Removes the magic weapon enchantment from the enchantment map
								enchantments.remove(Wizardry.magicSword.effectId);
								enchantments.remove(Wizardry.magicBow.effectId);
								// Applies the new enchantment map to the item
								EnchantmentHelper.setEnchantments(enchantments, stack);
							}

							// If it found an imbued weapon, it can stop since there should only be one.
							// More importantly, doing this skips the reset thing below. Just a bit tidier than a flag.
							break magicWeapons;
						}
					}
				}

				// Resets the magic weapon timer if the player no longer has one.
				if(ExtendedPlayer.get(entityplayer).magicWeaponDuration > 0){
					ExtendedPlayer.get(entityplayer).magicWeaponDuration = 0;
				}
			}

			// Tests for flaming weapons, decrements the time until the magic wears off, and removes the enchantment if
			// it has run out.

			flamingWeapons:
			if(ExtendedPlayer.get(entityplayer) != null){

				for(ItemStack stack : entityplayer.inventory.mainInventory){

					if(stack != null && stack.isItemEnchanted()){

						Map enchantments = EnchantmentHelper.getEnchantments(stack);

						if(enchantments.containsKey(Wizardry.flamingWeapon.effectId)){

							if(ExtendedPlayer.get(entityplayer).flamingWeaponDuration > 0){
								// Decrements the time until the flaming wears off
								ExtendedPlayer.get(entityplayer).flamingWeaponDuration--;
							}else{
								// Removes the flaming weapon enchantment from the enchantment map
								enchantments.remove(Wizardry.flamingWeapon.effectId);
								// Applies the new enchantment map to the item
								EnchantmentHelper.setEnchantments(enchantments, stack);
							}

							// If it found an imbued weapon, it can stop since there should only be one.
							// More importantly, doing this skips the reset thing below. Just a bit tidier than a flag.
							break flamingWeapons;
						}
					}
				}

				// Resets the flaming weapon timer if the player no longer has one.
				if(ExtendedPlayer.get(entityplayer).flamingWeaponDuration > 0){
					ExtendedPlayer.get(entityplayer).flamingWeaponDuration = 0;
				}
			}
			
			// Tests for freezing weapons, decrements the time until the magic wears off, and removes the enchantment if
			// it has run out.

			freezingWeapons:
			if(ExtendedPlayer.get(entityplayer) != null){

				for(ItemStack stack : entityplayer.inventory.mainInventory){

					if(stack != null && stack.isItemEnchanted()){

						Map enchantments = EnchantmentHelper.getEnchantments(stack);

						if(enchantments.containsKey(Wizardry.freezingWeapon.effectId)){

							if(ExtendedPlayer.get(entityplayer).freezingWeaponDuration > 0){
								// Decrements the time until the freezing wears off
								ExtendedPlayer.get(entityplayer).freezingWeaponDuration--;
							}else{
								// Removes the freezing weapon enchantment from the enchantment map
								enchantments.remove(Wizardry.freezingWeapon.effectId);
								// Applies the new enchantment map to the item
								EnchantmentHelper.setEnchantments(enchantments, stack);
							}

							// If it found an imbued weapon, it can stop since there should only be one.
							// More importantly, doing this skips the reset thing below. Just a bit tidier than a flag.
							break freezingWeapons;
						}
					}
				}

				// Resets the freezing weapon timer if the player no longer has one.
				if(ExtendedPlayer.get(entityplayer).freezingWeaponDuration > 0){
					ExtendedPlayer.get(entityplayer).freezingWeaponDuration = 0;
				}
			}
			
		}

		if(event.entityLiving.worldObj.isRemote){

			if(event.entityLiving.isPotionActive(Wizardry.frost)){

				double x = event.entityLiving.posX + (event.entityLiving.worldObj.rand.nextDouble() - 0.5)*event.entityLiving.width;
				double y = WizardryUtilities.getEntityFeetPos(event.entityLiving) + event.entityLiving.worldObj.rand.nextDouble()*event.entityLiving.height;
				double z = event.entityLiving.posZ + (event.entityLiving.worldObj.rand.nextDouble() - 0.5)*event.entityLiving.width;

				Wizardry.proxy.spawnParticle(EnumParticleType.SNOW, event.entityLiving.worldObj, x, y, z, 0, -0.02, 0, 15 + event.entityLiving.worldObj.rand.nextInt(5));
			}

			if(event.entityLiving.isPotionActive(Wizardry.fireskin)){

				double x = event.entityLiving.posX + (event.entityLiving.worldObj.rand.nextDouble() - 0.5)*event.entityLiving.width;
				double y = WizardryUtilities.getEntityFeetPos(event.entityLiving) + event.entityLiving.worldObj.rand.nextDouble()*event.entityLiving.height;
				double z = event.entityLiving.posZ + (event.entityLiving.worldObj.rand.nextDouble() - 0.5)*event.entityLiving.width;

				event.entityLiving.worldObj.spawnParticle("flame", x, y, z, 0, 0, 0);
			}

			if(event.entityLiving.isPotionActive(Wizardry.iceShroud)){

				double x = event.entityLiving.posX + (event.entityLiving.worldObj.rand.nextDouble() - 0.5)*event.entityLiving.width;
				double y = WizardryUtilities.getEntityFeetPos(event.entityLiving) + event.entityLiving.worldObj.rand.nextDouble()*event.entityLiving.height;
				double z = event.entityLiving.posZ + (event.entityLiving.worldObj.rand.nextDouble() - 0.5)*event.entityLiving.width;

				float brightness = 0.5f + (event.entityLiving.worldObj.rand.nextFloat()/2);
				Wizardry.proxy.spawnParticle(EnumParticleType.SPARKLE, event.entityLiving.worldObj, x, y, z, 0, 0, 0, 48 + event.entityLiving.worldObj.rand.nextInt(12), brightness, brightness + 0.1f, 1.0f, true, 0);
				Wizardry.proxy.spawnParticle(EnumParticleType.SNOW, event.entityLiving.worldObj, x, y, z, 0, -0.02, 0, 40 + event.entityLiving.worldObj.rand.nextInt(10));
			}

			if(event.entityLiving.isPotionActive(Wizardry.staticAura)){

				double x = event.entityLiving.posX + (event.entityLiving.worldObj.rand.nextDouble() - 0.5)*event.entityLiving.width;
				double y = WizardryUtilities.getEntityFeetPos(event.entityLiving) + event.entityLiving.worldObj.rand.nextDouble()*event.entityLiving.height;
				double z = event.entityLiving.posZ + (event.entityLiving.worldObj.rand.nextDouble() - 0.5)*event.entityLiving.width;

				Wizardry.proxy.spawnParticle(EnumParticleType.SPARK, event.entityLiving.worldObj, x, y, z, 0, 0, 0, 3);
			}

			if(event.entityLiving.isPotionActive(Wizardry.transience)){

				double x = event.entityLiving.posX + (event.entityLiving.worldObj.rand.nextDouble() - 0.5)*event.entityLiving.width;
				double y = WizardryUtilities.getEntityFeetPos(event.entityLiving) + event.entityLiving.worldObj.rand.nextDouble()*event.entityLiving.height;
				double z = event.entityLiving.posZ + (event.entityLiving.worldObj.rand.nextDouble() - 0.5)*event.entityLiving.width;

				Wizardry.proxy.spawnParticle(EnumParticleType.DUST, event.entityLiving.worldObj, x, y, z, 0, 0, 0, (int)(16.0D / (Math.random() * 0.8D + 0.2D)), 0.8f, 0.8f, 1.0f);
			}

		}else{

			if(event.entityLiving.isPotionActive(Wizardry.decay) && event.entityLiving.onGround && event.entityLiving.ticksExisted % Wizardry.DECAY_SPREAD_INTERVAL == 0){

				List list = event.entityLiving.worldObj.getEntitiesWithinAABBExcludingEntity(event.entityLiving, event.entityLiving.boundingBox);

				boolean flag = true;

				for(Object object : list){
					if(object instanceof EntityDecay) flag = false;
				}

				if(flag){
					// The victim spreading the decay is the 'caster' here, so that it can actually wear off, otherwise it just gets infected with its own decay and the effect lasts forever.
					event.entityLiving.worldObj.spawnEntityInWorld(new EntityDecay(event.entityLiving.worldObj, event.entityLiving.posX, event.entityLiving.posY, event.entityLiving.posZ, event.entityLiving));
				}
			}
		}

		// Mind trick
		if(event.entityLiving.isPotionActive(Wizardry.mindTrick) && event.entityLiving instanceof EntityLiving){
			// Old AI (this can't be done in onLivingSetAttackTargetEvent because that only fires for the new AI).
			if(event.entityLiving instanceof EntityCreature) ((EntityCreature)event.entityLiving).setTarget(null);
		}

		// Mind control - old AI (this can't be done in onLivingSetAttackTargetEvent because that only fires for the new AI).
		mindcontrol:
			if(event.entityLiving.isPotionActive(Wizardry.mindControl) && event.entityLiving instanceof EntityLiving){

				NBTTagCompound entityNBT = event.entityLiving.getEntityData();

				if(entityNBT != null && entityNBT.hasKey(MindControl.NBT_KEY)){

					Entity caster = WizardryUtilities.getEntityByUUID(event.entity.worldObj, UUID.fromString(entityNBT.getString(MindControl.NBT_KEY)));

					if(caster instanceof EntityLivingBase){

						if(MindControl.findMindControlTarget((EntityLiving)event.entityLiving, (EntityLivingBase)caster, event.entity.worldObj)){
							// If it worked, skip setting the target to null.
							break mindcontrol;
						}
					}
				}
				// If the caster couldn't be found or no valid target was found, this just acts like mind trick.
				((EntityLiving)event.entityLiving).setAttackTarget(null);
			}
		
		// Terror
		if(event.entityLiving.isPotionActive(Wizardry.fear) && event.entityLiving instanceof EntityCreature){

			NBTTagCompound entityNBT = event.entityLiving.getEntityData();
			EntityCreature creature = (EntityCreature)event.entityLiving;

			if(entityNBT != null && entityNBT.hasKey(Intimidate.NBT_KEY)){

				Entity caster = WizardryUtilities.getEntityByUUID(creature.worldObj, UUID.fromString(entityNBT.getString(Intimidate.NBT_KEY)));

				if(caster instanceof EntityLivingBase){
					Intimidate.runAway(creature, (EntityLivingBase)caster);
				}
			}
		}
	}

	@SubscribeEvent
	public void onBreakSpeedEvent(BreakSpeed event){
		if(event.entityPlayer.isPotionActive(Wizardry.frost)){
			// Amplifier + 1 because it starts at 0
			event.newSpeed = event.originalSpeed * (1 - Wizardry.FROST_FATIGUE_PER_LEVEL*(event.entityPlayer.getActivePotionEffect(Wizardry.frost).getAmplifier() + 1));
		}
	}

	private void playerInventoryTick(EntityPlayer player) {

		// Charges wand using mana flask. It is here rather than in the crafting handler so the result displays
		// the proper damage before it is actually crafted.

		boolean flag = false;
		ItemStack wand = null;
		ItemStack armour = null;
		IInventory craftMatrix = ((ContainerPlayer)player.openContainer).craftMatrix;
		ItemStack outputItem = ((ContainerPlayer)player.openContainer).craftResult.getStackInSlot(0);

		for (int i = 0; i < craftMatrix.getSizeInventory(); i++){
			if(craftMatrix.getStackInSlot(i) != null){
				ItemStack itemstack = craftMatrix.getStackInSlot(i);

				if(itemstack.getItem() == Wizardry.manaFlask){
					flag = true;
				}

				if(itemstack.getItem() instanceof ItemWand){
					wand = itemstack;
				}

				if(itemstack.getItem() instanceof ItemWizardArmour){
					armour = itemstack;
				}
			}
		}

		if(outputItem != null && outputItem.getItem() instanceof ItemWand && flag && wand != null){
			outputItem.setTagCompound(wand.getTagCompound());
			if(wand.getItemDamage()-Wizardry.MANA_PER_FLASK < 0){
				outputItem.setItemDamage(0);
			}else{
				outputItem.setItemDamage(wand.getItemDamage()-Wizardry.MANA_PER_FLASK);
			}
		}

		if(outputItem != null && outputItem.getItem() instanceof ItemWizardArmour && flag && armour != null){
			outputItem.setTagCompound(armour.getTagCompound());
			if(armour.getItemDamage()-Wizardry.MANA_PER_FLASK < 0){
				outputItem.setItemDamage(0);
			}else{
				outputItem.setItemDamage(wand.getItemDamage()-Wizardry.MANA_PER_FLASK);
			}
		}
	}

	private void craftingTableTick(EntityPlayer player) {

		// Charges wand using mana flask. It is here rather than in the crafting handler so the result displays
		// the proper damage before it is actually crafted.

		boolean flag = false;
		ItemStack wand = null;
		ItemStack armour = null;
		IInventory craftMatrix = ((ContainerWorkbench)player.openContainer).craftMatrix;
		ItemStack outputItem = ((ContainerWorkbench)player.openContainer).craftResult.getStackInSlot(0);

		for (int i = 0; i < craftMatrix.getSizeInventory(); i++){
			if(craftMatrix.getStackInSlot(i) != null){
				ItemStack itemstack = craftMatrix.getStackInSlot(i);

				if(itemstack.getItem() == Wizardry.manaFlask){
					flag = true;
				}

				if(itemstack.getItem() instanceof ItemWand){
					wand = itemstack;
				}

				if(itemstack.getItem() instanceof ItemWizardArmour){
					armour = itemstack;
				}
			}
		}

		if(outputItem != null && outputItem.getItem() instanceof ItemWand && flag && wand != null){
			outputItem.setTagCompound(wand.getTagCompound());
			if(wand.getItemDamage()-Wizardry.MANA_PER_FLASK < 0){
				outputItem.setItemDamage(0);
			}else{
				outputItem.setItemDamage(wand.getItemDamage()-Wizardry.MANA_PER_FLASK);
			}
		}

		if(outputItem != null && outputItem.getItem() instanceof ItemWizardArmour && flag && armour != null){
			outputItem.setTagCompound(armour.getTagCompound());
			if(armour.getItemDamage()-Wizardry.MANA_PER_FLASK < 0){
				outputItem.setItemDamage(0);
			}else{
				outputItem.setItemDamage(wand.getItemDamage()-Wizardry.MANA_PER_FLASK);
			}
		}
	}

	@SubscribeEvent
	public void onLivingDeathEvent(LivingDeathEvent event){

		// Extended player data saving
		if (!event.entity.worldObj.isRemote && event.entity instanceof EntityPlayer){
			// This should never be null, but no harm in checking.
			if(ExtendedPlayer.get((EntityPlayer)event.entity) != null){

				ExtendedPlayer.get((EntityPlayer)event.entity).onPlayerDeath();

				NBTTagCompound playerData = new NBTTagCompound();
				ExtendedPlayer.get((EntityPlayer)event.entity).saveNBTData(playerData);
				CommonProxy.storeEntityData(ExtendedPlayer.getSaveKey((EntityPlayer)event.entity), playerData);
			}
		}

		if(event.source.getEntity() instanceof EntityPlayer){

			EntityPlayer player = (EntityPlayer)event.source.getEntity();

			// Mana siphoning. Works for the first wand with a siphon upgrade on the hotbar.
			for(int i=0; i<9; i++){
				ItemStack itemstack = player.inventory.getStackInSlot(i);
				if(itemstack != null && itemstack.getItem() instanceof ItemWand && itemstack.isItemDamaged() && WandHelper.getUpgradeLevel(itemstack, Wizardry.siphonUpgrade) > 0){
					int damage = itemstack.getItemDamage() - Wizardry.SIPHON_MANA_PER_LEVEL*WandHelper.getUpgradeLevel(itemstack, Wizardry.siphonUpgrade) - player.worldObj.rand.nextInt(Wizardry.SIPHON_MANA_PER_LEVEL);
					if(damage < 0) damage = 0;
					itemstack.setItemDamage(damage);
					break;
				}
			}

			if(event.entityLiving == player && event.source instanceof IElementalDamage){
				player.triggerAchievement(Wizardry.selfDestruct);
			}
		}

		if(event.source.getEntity() instanceof EntitySilverfishMinion){
			int lifetime = ((EntitySilverfishMinion)event.source.getEntity()).lifetime;
			// Summons 1-4 more silverfish
			int alliesToSummon = event.entity.worldObj.rand.nextInt(4) + 1;

			for(int i=0; i<alliesToSummon; i++){
				EntitySilverfishMinion silverfish = new EntitySilverfishMinion(event.entity.worldObj, event.entity.posX, event.entity.posY, event.entity.posZ, ((EntitySilverfishMinion)event.source.getEntity()).getCaster(), lifetime);
				event.entity.worldObj.spawnEntityInWorld(silverfish);
			}
		}
	}

	@SubscribeEvent
	public void onEntityJoinWorld(EntityJoinWorldEvent event){

		if(!event.entity.worldObj.isRemote && event.entity instanceof EntityPlayer){
			
			NBTTagCompound playerData = CommonProxy.getEntityData(ExtendedPlayer.getSaveKey((EntityPlayer)event.entity));
			if (playerData != null) {
				ExtendedPlayer.get((EntityPlayer)event.entity).loadNBTData(playerData);
			}
			
			// NBT data is synced AFTER it is loaded from the save file!
			ExtendedPlayer.get((EntityPlayer)event.entity).sync();
			
			// When a player joins the world, they are sent the glyph data.
			// Is there a way to only make this happen when the player actually logged in, i.e. not on respawn
			// or leaving a portal?
			if(event.entity instanceof EntityPlayerMP){
				SpellGlyphData.get(event.world).sync((EntityPlayerMP)event.entity);
			}
		}

		// Rather long-winded (but necessary) way of getting an arrow just after it has been fired, checking if the bow
		// that fired it has the imbuement enchantment, and applying extra damage accordingly.
		if(!event.entity.worldObj.isRemote && event.entity instanceof EntityArrow){

			EntityArrow arrow = (EntityArrow)event.entity;

			if(arrow.shootingEntity instanceof EntityLivingBase){

				EntityLivingBase archer = (EntityLivingBase)arrow.shootingEntity;

				if(archer.getHeldItem() != null && archer.getHeldItem().getItem() instanceof ItemBow){

					// Taken directly from ItemBow, so it works exactly the same as the power enchantment.
					int level = EnchantmentHelper.getEnchantmentLevel(Wizardry.magicBow.effectId, archer.getHeldItem());

					if(level > 0){
						arrow.setDamage(arrow.getDamage() + (double)level * 0.5D + 0.5D);
					}
					
					if(EnchantmentHelper.getEnchantmentLevel(Wizardry.flamingWeapon.effectId, archer.getHeldItem()) > 0){
						// Again, this is exactly what happens in ItemBow (flame is flame; level does nothing).
						arrow.setFire(100);
					}
					
					level = EnchantmentHelper.getEnchantmentLevel(Wizardry.freezingWeapon.effectId, archer.getHeldItem());
					
					if(level > 0){
						if(arrow.getEntityData() != null){
							arrow.getEntityData().setInteger(FreezingWeapon.FREEZING_ARROW_NBT_KEY, level);
						}
					}
				}
			}
		}

	}

	@SubscribeEvent
	public void onLivingDropsEvent(LivingDropsEvent event){
		// Evil wizards drop spell books themselves
		if(event.entityLiving instanceof EntityMob && !(event.entityLiving instanceof EntityEvilWizard) && event.source.getEntity() instanceof EntityPlayer && !(event.source.getEntity() instanceof FakePlayer) && Wizardry.spellBookDropChance > 0){

			// This does exactly what the entity drop method does, but with a different random number so that the
			// spell book doesn't always drop with other rare drops.
			int rareDropNumber = event.entity.worldObj.rand.nextInt(200) - event.lootingLevel;
			if(rareDropNumber < Wizardry.spellBookDropChance){
				// Drops a spell book
				int id = WizardryUtilities.getStandardWeightedRandomSpellId(event.entity.worldObj.rand);

				event.drops.add(new EntityItem(event.entityLiving.worldObj, event.entityLiving.posX, event.entityLiving.posY, event.entityLiving.posZ,
						new ItemStack(Wizardry.spellBook, 1, id)));
			}
		}

		for(EntityItem item : event.drops){

			// Destroys conjured items if their caster dies.
			if(item.getEntityItem().getItem() instanceof ItemSpectralSword
					|| item.getEntityItem().getItem() instanceof ItemSpectralPickaxe
					|| item.getEntityItem().getItem() instanceof ItemSpectralBow
					|| item.getEntityItem().getItem() instanceof ItemSpectralArmour
					|| item.getEntityItem().getItem() instanceof ItemFlamingAxe
					|| item.getEntityItem().getItem() instanceof ItemFrostAxe){

				item.setDead();
			}

			// Instantly disenchants an imbued weapon if it is dropped when the player dies.
			if(item.getEntityItem().isItemEnchanted()){

				// No need to check what enchantments the item has, since remove() does nothing if the element does not exist.
				Map enchantments = EnchantmentHelper.getEnchantments(item.getEntityItem());
				
				// Removes the magic weapon enchantments from the enchantment map
				enchantments.remove(Wizardry.magicSword.effectId);
				enchantments.remove(Wizardry.magicBow.effectId);
				enchantments.remove(Wizardry.flamingWeapon.effectId);
				enchantments.remove(Wizardry.freezingWeapon.effectId);
				
				// Applies the new enchantment map to the item
				EnchantmentHelper.setEnchantments(enchantments, item.getEntityItem());
			}
		}
	}

	@SubscribeEvent
	public void onLivingSetAttackTargetEvent(LivingSetAttackTargetEvent event){
		// Mind trick
		// If the target is null already, no need to set it to null, or infinite loops will occur.
		if((event.entityLiving.isPotionActive(Wizardry.mindTrick) || event.entityLiving.isPotionActive(Wizardry.fear)) && event.entityLiving instanceof EntityLiving && event.target != null){
			// New AI
			((EntityLiving)event.entityLiving).setAttackTarget(null);
		}

		// Mind control
		mindcontrol:
			if(event.entityLiving.isPotionActive(Wizardry.mindControl) && event.entityLiving instanceof EntityLiving){

				NBTTagCompound entityNBT = event.entityLiving.getEntityData();

				if(entityNBT != null && entityNBT.hasKey(MindControl.NBT_KEY)){

					Entity caster = WizardryUtilities.getEntityByUUID(event.entity.worldObj, UUID.fromString(entityNBT.getString(MindControl.NBT_KEY)));

					// If the target that the event tried to set is already a valid mind control target, nothing happens.
					if(WizardryUtilities.isValidTarget(caster, event.target)) break mindcontrol;

					if(caster instanceof EntityLivingBase){

						if(MindControl.findMindControlTarget((EntityLiving)event.entityLiving, (EntityLivingBase)caster, event.entity.worldObj)){
							// If it worked, skip setting the target to null.
							break mindcontrol;
						}
					}
				}
				// If the caster couldn't be found or no valid target was found, this just acts like mind trick.
				// If the target is null already, no need to set it to null, or infinite loops will occur.
				if(event.target != null) ((EntityLiving)event.entityLiving).setAttackTarget(null);
			}
	}

	@SubscribeEvent
	public void onItemPickupEvent(EntityItemPickupEvent event){
		if(event.item.getEntityItem().getItem() == Wizardry.magicCrystal){
			event.entityPlayer.addStat(Wizardry.crystal, 1);
		}
	}

	@SubscribeEvent
	public void onItemTossEvent(ItemTossEvent event){

		// Prevents conjured items being thrown by dragging and dropping outside the inventory.
		if(event.entityItem.getEntityItem().getItem() instanceof ItemSpectralSword
				|| event.entityItem.getEntityItem().getItem() instanceof ItemSpectralPickaxe
				|| event.entityItem.getEntityItem().getItem() instanceof ItemSpectralBow
				|| event.entityItem.getEntityItem().getItem() instanceof ItemSpectralArmour
				|| event.entityItem.getEntityItem().getItem() instanceof ItemFlamingAxe
				|| event.entityItem.getEntityItem().getItem() instanceof ItemFrostAxe){

			event.setCanceled(true);
			event.player.inventory.addItemStackToInventory(event.entityItem.getEntityItem());
		}

		// Instantly disenchants an imbued weapon if it is thrown on the ground.
		if(event.entityItem.getEntityItem().isItemEnchanted()){

			// No need to check what enchantments the item has, since remove() does nothing if the element does not exist.
			Map enchantments = EnchantmentHelper.getEnchantments(event.entityItem.getEntityItem());
			
			// Removes the magic weapon enchantments from the enchantment map
			enchantments.remove(Wizardry.magicSword.effectId);
			enchantments.remove(Wizardry.magicBow.effectId);
			enchantments.remove(Wizardry.flamingWeapon.effectId);
			enchantments.remove(Wizardry.freezingWeapon.effectId);
			
			// Applies the new enchantment map to the item
			EnchantmentHelper.setEnchantments(enchantments, event.entityItem.getEntityItem());
		}
	}

	@SubscribeEvent
	public void onPlayerInteractEvent(PlayerInteractEvent event){

		if(event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && event.entityPlayer.isSneaking()){

			ItemStack wand = event.entityPlayer.getHeldItem();

			if(wand != null && wand.getItem() instanceof ItemWand && WandHelper.getCurrentSpell(wand) instanceof Clairvoyance){

				ExtendedPlayer properties = ExtendedPlayer.get(event.entityPlayer);

				if(properties != null){

					int x = event.x;
					int y = event.y;
					int z = event.z;

					if(event.face == 0) y--;
					if(event.face == 1) y++;
					if(event.face == 2) z--;
					if(event.face == 3) z++;
					if(event.face == 4) x--;
					if(event.face == 5) x++;

					properties.setClairvoyancePoint(x, y, z, event.world.provider.dimensionId);
					if(!event.world.isRemote){
						event.entityPlayer.addChatMessage(new ChatComponentTranslation("spell.clairvoyance.confirm", WizardryRegistry.clairvoyance.getDisplayNameWithFormatting()));
						event.setCanceled(true);
					}
				}
			}
		}
	}
	
	@SubscribeEvent
	public void onBonemealEvent(BonemealEvent event){
		// Grows crystal flowers when bonemeal is used on grass
		if(event.block == Blocks.grass){
			
			int x = event.x + event.world.rand.nextInt(8) - event.world.rand.nextInt(8);
			int y = event.y + event.world.rand.nextInt(4) - event.world.rand.nextInt(4);
			int z = event.z + event.world.rand.nextInt(8) - event.world.rand.nextInt(8);

			if (event.world.isAirBlock(x, y, z) && (!event.world.provider.hasNoSky || y < 127) && Wizardry.crystalFlower.canBlockStay(event.world, x, y, z))
			{
				event.world.setBlock(x, y, z, Wizardry.crystalFlower, 0, 2);
			}
		}
	}
}