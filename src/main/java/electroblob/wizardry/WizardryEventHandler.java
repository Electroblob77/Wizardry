package electroblob.wizardry;

import electroblob.wizardry.constants.Constants;
import electroblob.wizardry.data.SpellEmitterData;
import electroblob.wizardry.data.SpellGlyphData;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.entity.living.ISpellCaster;
import electroblob.wizardry.event.DiscoverSpellEvent;
import electroblob.wizardry.event.SpellCastEvent;
import electroblob.wizardry.integration.DamageSafetyChecker;
import electroblob.wizardry.item.IManaStoringItem;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.packet.PacketSyncAdvancements;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.registry.*;
import electroblob.wizardry.spell.FreezingWeapon;
import electroblob.wizardry.spell.ImbueWeapon;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.*;
import electroblob.wizardry.util.MagicDamage.DamageType;
import electroblob.wizardry.util.ParticleBuilder.Type;
import net.minecraft.advancements.Advancement;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * General-purpose event handler for things that don't fit anywhere else or groups of related behaviours that are better
 * kept together. As of Wizardry 2.1, most of the code in this class has been relocated somewhere sensible, leaving only
 * a few miscellaneous things that don't make much sense anywhere else, or that are better kept together (previously,
 * this was a gigantic class with about half of the entire mod's logic in it!)
 * 
 * @author Electroblob
 * @since Wizardry 1.0
 */
// The general rules for where event-based logic goes are:
// - If the logic relates only to vanilla things or is general (e.g. gameplay settings) it lives in here
// - If there is an obvious class for the thing relevant to the logic being performed, it goes in there for the sake
//   of modularity/separation-of-concerns (e.g. armour cost reductions go in ItemWizardArmour)
// - If the thing has an instance but not a separate class (e.g. most custom potions), it goes in a related class
//   where applicable (e.g. a spell class), or if not it goes in here
// - If several things share a significant amount of logic, to avoid duplicate code and potentially improve efficiency
//   they should be kept together either in a class relevant to all of them (probably a common superclass) or in here
// - Client-side logic goes in WizardryClientEventHandler or another relevant client-side class
@Mod.EventBusSubscriber
public final class WizardryEventHandler {

	private WizardryEventHandler(){} // No instances!

	@SubscribeEvent
	public static void onPlayerLoggedInEvent(PlayerLoggedInEvent event){
		// When a player logs in, they are sent the glyph data, server settings and spell properties.
		if(event.player instanceof EntityPlayerMP){
			SpellGlyphData.get(event.player.world).sync((EntityPlayerMP)event.player);
			SpellEmitterData.get(event.player.world).sync((EntityPlayerMP)event.player);
			Wizardry.settings.sync((EntityPlayerMP)event.player);
			syncAdvancements((EntityPlayerMP)event.player, false);
		}
		Spell.syncProperties(event.player);
	}

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onPlaySoundAtEntityEvent(PlaySoundAtEntityEvent event){
		// Muffle (there's no spell class for it so it's here instead)
		if(event.getEntity() instanceof EntityLivingBase
				&& ((EntityLivingBase)event.getEntity()).isPotionActive(WizardryPotions.muffle)){
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onAdvancementEvent(AdvancementEvent event){
		// Forge has no hook for revoked advancements :(
		// Guess we'll just have to make do
		// Also, this seems to get fired on player login, so to prevent the toasts from appearing every login the
		// only way I can see to do it is by testing the player has been around long enough.
		if(event.getEntityPlayer() instanceof EntityPlayerMP && event.getEntityPlayer().ticksExisted > 0){
			syncAdvancements((EntityPlayerMP)event.getEntityPlayer(), true);
		}
	}

	private static void syncAdvancements(EntityPlayerMP player, boolean showToasts){

		Wizardry.logger.info("Synchronising advancements for " + player.getName());

		ArrayList<ResourceLocation> advancements = new ArrayList<>();

		for(Advancement advancement : player.getServer().getAdvancementManager().getAdvancements()){
			if(player.getAdvancements().getProgress(advancement).isDone()) advancements.add(advancement.getId());
		}

		WizardryPacketHandler.net.sendTo(new PacketSyncAdvancements.Message(showToasts, advancements.toArray(new ResourceLocation[0])), player);
	}

	@SubscribeEvent(priority = EventPriority.HIGH) // Disabling of specific spells comes after arcane jammer but before everything else
	public static void onSpellCastPreEvent(SpellCastEvent.Pre event){

		boolean enabled = true;

		switch(event.getSource()){
			case WAND: 		enabled = event.getSpell().isEnabled(SpellProperties.Context.WANDS); break;
			case SCROLL:	enabled = event.getSpell().isEnabled(SpellProperties.Context.SCROLL); break;
			case COMMAND:	enabled = event.getSpell().isEnabled(SpellProperties.Context.COMMANDS); break;
			case NPC: 		enabled = event.getSpell().isEnabled(SpellProperties.Context.NPCS); break;
			case DISPENSER: enabled = event.getSpell().isEnabled(SpellProperties.Context.DISPENSERS); break;
			case OTHER: 	enabled = event.getSpell().isEnabled(); break; // Any enabled context will do for this one
		}

		// If a spell is disabled in the config, it will not work.
		if(!enabled){
			if(event.getCaster() != null && !event.getCaster().world.isRemote) event.getCaster().sendMessage(
					new TextComponentTranslation("spell.disabled", event.getSpell().getNameForTranslationFormatted()));
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onSpellCastPostEvent(SpellCastEvent.Post event){

		if(event.getCaster() instanceof EntityPlayer){

			EntityPlayer player = (EntityPlayer)event.getCaster();

			// Advancement triggers
			if(player instanceof EntityPlayerMP){
				WizardryAdvancementTriggers.cast_spell.trigger((EntityPlayerMP)player, event.getSpell(), player.getHeldItem(player.getActiveHand()));
			}

			// Spell discovery (only players can discover spells, obviously)
			WizardData data = WizardData.get(player);

			if(data != null){
				// Data is updated on both sides (This line was added client-side to fix a bug back in 1.1.3, so now
				// it's in common code, which is nice!)
				// Short-circuiting AND means that discoverSpell is only called if the event isn't cancelled.
				if(!MinecraftForge.EVENT_BUS.post(new DiscoverSpellEvent(player, event.getSpell(), DiscoverSpellEvent.Source.CASTING))
						&& data.discoverSpell(event.getSpell())){

					// If the spell wasn't already discovered, other stuff happens:
					if(event.getSource() == SpellCastEvent.Source.COMMAND){
						// If the spell didn't send a packet itself, the extended player needs to be synced so the
						// spell discovery updates on the client.
						if(!event.getSpell().requiresPacket()) data.sync();

					}else if(!event.getCaster().world.isRemote && !player.isCreative()
							&& Wizardry.settings.discoveryMode){
						// Sound and text only happen server-side, in survival, with discovery mode on, and only when
						// the spell wasn't cast using commands.
						EntityUtils.playSoundAtPlayer(player, WizardrySounds.MISC_DISCOVER_SPELL, 1.25f, 1);
						player.sendMessage(new TextComponentTranslation("spell.discover",
								event.getSpell().getNameForTranslationFormatted()));
					}
				}
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onDiscoverSpellEvent(DiscoverSpellEvent event){
		if(event.getEntityPlayer() instanceof EntityPlayerMP){
			WizardryAdvancementTriggers.discover_spell.trigger((EntityPlayerMP)event.getEntityPlayer(), event.getSpell(), event.getSource());
		}
	}

	@SubscribeEvent
	public static void onLivingSetAttackTargetEvent(LivingSetAttackTargetEvent event){

		if(event.getEntityLiving() instanceof EntityLiving && event.getTarget() != null){

			// Muffle
			if(event.getTarget().isPotionActive(WizardryPotions.muffle)){

				Vec3d vec = event.getTarget().getPositionEyes(1).subtract(event.getEntity().getPositionEyes(1));
				// Find the angle between the direction the mob is looking and the direction the player is in
				// Angle between a and b = acos((a.b) / (|a|*|b|))
				double angle = Math.acos(vec.dotProduct(event.getEntity().getLookVec()) / vec.length());
				System.out.println(angle);
				// If the player is not within the 144-degree arc in front of the mob, it won't detect them
				if(angle > 0.4 * Math.PI){
					((EntityLiving)event.getEntityLiving()).setAttackTarget(null);
				}
			}

			// Blindness tweak
			// I'm not going as far as potion core's implementation, this is just so it does *something* to mobs
			if(event.getEntityLiving().isPotionActive(MobEffects.BLINDNESS) && !Loader.isModLoaded("potioncore")
					&& Wizardry.settings.blindnessTweak && event.getTarget().getDistanceSq(event.getEntity()) > 3.5 * 3.5){
				// Can't detect anything more than 3.5 blocks away (roughly the player's view distance when blinded)
				((EntityLiving)event.getEntityLiving()).setAttackTarget(null);
			}
		}
	}

	/* There is a subtle but important difference between LivingAttackEvent and LivingHurtEvent - LivingAttackEvent
	 * fires immediately when attackEntityFrom is called, whereas LivingHurtEvent only fires if the attack actually
	 * succeeded, i.e. if the entity in question takes damage (though the event is fired before that so you can cancel
	 * the damage). Things are processed in the following order:
	 *
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
	 * * LivingDamageEvent *
	 * - Health is finally changed
	 *
	 * Of course, there are no guarantees that other
	 * mods hooking into these two events will be called before or after yours, but you can have some degree of control
	 * by choosing which event to use. EDIT: Actually, there are. Firstly, you can set a priority in the @SubscribeEvent
	 * annotation which defines how early (higher priority) or late (lower priority) the method is called. Methods with
	 * the same priority are sorted alphabetically by mod id (so it's safe to assume wizardry would be fairly late on!).
	 * I wonder if there are any conventions for what sort of things take what priority...? */

	@SubscribeEvent(priority = EventPriority.LOW) // Low priority in case the event gets cancelled at default priority
	public static void onLivingAttackEvent(LivingAttackEvent event){

		// Retaliatory effects
		// These are better off here because the revenge effects are pretty similar, and I'd rather keep the (lengthy)
		// if statement in one place.
		if(event.getSource() != null && event.getSource().getTrueSource() instanceof EntityLivingBase
				&& !event.getSource().isProjectile() && !(event.getSource() instanceof IElementalDamage
						&& ((IElementalDamage)event.getSource()).isRetaliatory())){

			EntityLivingBase attacker = (EntityLivingBase)event.getSource().getTrueSource();
			World world = event.getEntityLiving().world;

			if(attacker.getDistance(event.getEntityLiving()) < 10){

				// Fireskin
				if(event.getEntityLiving().isPotionActive(WizardryPotions.fireskin)
						&& !MagicDamage.isEntityImmune(DamageType.FIRE, event.getEntityLiving()))
					attacker.setFire(Spells.fire_breath.getProperty(Spell.BURN_DURATION).intValue());

				// Ice Shroud
				if(event.getEntityLiving().isPotionActive(WizardryPotions.ice_shroud)
						&& !MagicDamage.isEntityImmune(DamageType.FROST, event.getEntityLiving())
						&& !(attacker instanceof FakePlayer)) // Fake players cause problems
					attacker.addPotionEffect(new PotionEffect(WizardryPotions.frost,
							Spells.ice_shroud.getProperty(Spell.EFFECT_DURATION).intValue(),
							Spells.ice_shroud.getProperty(Spell.EFFECT_STRENGTH).intValue()));

				// Static Aura
				if(event.getEntityLiving().isPotionActive(WizardryPotions.static_aura)){

					if(world.isRemote){

						ParticleBuilder.create(Type.LIGHTNING).entity(event.getEntity()).pos(0, event.getEntity().height / 2, 0)
								.target(attacker).spawn(world);

						ParticleBuilder.spawnShockParticles(world, attacker.posX,
								attacker.getEntityBoundingBox().minY + attacker.height / 2, attacker.posZ);
					}

					DamageSafetyChecker.attackEntitySafely(attacker, MagicDamage.causeDirectMagicDamage(event.getEntityLiving(),
							DamageType.SHOCK, true), Spells.static_aura.getProperty(Spell.DAMAGE).floatValue(), event.getSource().getDamageType());
					attacker.playSound(WizardrySounds.SPELL_STATIC_AURA_RETALIATE, 1.0F, world.rand.nextFloat() * 0.4F + 1.5F);
				}
			}
		}

	}

	@SubscribeEvent(priority = EventPriority.LOW) // Again, we don't want these effects if the event is cancelled
	public static void onLivingHurtEvent(LivingHurtEvent event){

		// Flaming and freezing swords
		if(event.getSource().getTrueSource() instanceof EntityLivingBase){

			EntityLivingBase attacker = (EntityLivingBase)event.getSource().getTrueSource();

			// Players can only ever attack with their main hand, so this is the right method to use here.
			if(!attacker.getHeldItemMainhand().isEmpty() && ImbueWeapon.isSword(attacker.getHeldItemMainhand())){

				int level = EnchantmentHelper.getEnchantmentLevel(WizardryEnchantments.flaming_weapon,
						attacker.getHeldItemMainhand());

				if(level > 0 && !MagicDamage.isEntityImmune(DamageType.FIRE, event.getEntityLiving()))
					event.getEntityLiving().setFire(level * 4);

				level = EnchantmentHelper.getEnchantmentLevel(WizardryEnchantments.freezing_weapon,
						attacker.getHeldItemMainhand());
				// Frost lasts for longer because it doesn't do any actual damage
				if(level > 0 && !MagicDamage.isEntityImmune(DamageType.FROST, event.getEntityLiving()))
					event.getEntityLiving().addPotionEffect(new PotionEffect(WizardryPotions.frost, level * 200, 0));
			}
		}

		// Freezing bow
		if(event.getSource().getImmediateSource() instanceof EntityArrow
				&& event.getSource().getImmediateSource().getEntityData() != null){

			int level = event.getSource().getImmediateSource().getEntityData()
					.getInteger(FreezingWeapon.FREEZING_ARROW_NBT_KEY);

			if(level > 0 && !MagicDamage.isEntityImmune(DamageType.FROST, event.getEntityLiving()))
				event.getEntityLiving().addPotionEffect(new PotionEffect(WizardryPotions.frost, level * 150, 0));
		}

		// Damage scaling
		if(event.getSource() != null && event.getSource() instanceof IElementalDamage){

			if(event.getSource().getTrueSource() instanceof EntityPlayer){
				event.setAmount((float)(event.getAmount() * Wizardry.settings.playerDamageScale));
			}else{
				event.setAmount((float)(event.getAmount() * Wizardry.settings.npcDamageScale));
			}
		}
	}

	@SubscribeEvent
	public static void onLivingUpdateEvent(LivingUpdateEvent event){

		// Experimental animation feature
//		if(event.getEntityLiving().isHandActive() && event.getEntityLiving().getActiveItemStack().getItemUseAction() == WizardryUtilities.POINT){
//			event.getEntityLiving().isSwingInProgress = true;
//			event.getEntityLiving().swingProgress = 1f;
//			event.getEntityLiving().prevSwingProgress = 1;
//			event.getEntityLiving().swingingHand = event.getEntityLiving().getActiveHand();
//		}

		if(event.getEntityLiving().world.isRemote){

			// Client-side continuous spell casting for NPCs

			if(event.getEntity() instanceof ISpellCaster && event.getEntity() instanceof EntityLiving){

				Spell spell = ((ISpellCaster)event.getEntity()).getContinuousSpell();
				SpellModifiers modifiers = ((ISpellCaster)event.getEntity()).getModifiers();
				int count = ((ISpellCaster)event.getEntity()).getSpellCounter();

				if(spell != null && spell != Spells.none){ // IntelliJ is wrong, do NOT remove the null check!

					if(!MinecraftForge.EVENT_BUS.post(new SpellCastEvent.Tick(SpellCastEvent.Source.NPC, spell, event.getEntityLiving(),
							modifiers, count))){

						spell.cast(event.getEntity().world, (EntityLiving)event.getEntity(), EnumHand.MAIN_HAND, count,
								// TODO: This implementation of modifiers relies on them being accessible client-side.
								// 		 Right now that doesn't matter because NPCs don't use modifiers, but they might in future
								((EntityLiving)event.getEntity()).getAttackTarget(), modifiers);
					}
				}

				((ISpellCaster)event.getEntity()).setSpellCounter(count + 1);
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST) // No siphoning if the event is cancelled, that could be exploited...
	public static void onLivingDeathEvent(LivingDeathEvent event){

		if(event.getSource().getTrueSource() instanceof EntityPlayer){

			EntityPlayer player = (EntityPlayer)event.getSource().getTrueSource();

			// Compatibility with "Lycanites Mobs" -it uses custom loot drop logic which can't be hooked, given the
			// number of mobs that spawn as Lycanites when this mod is active it would massively nerf the wizard drops
			//  if we didn't handle this
			if(event.getEntity().getClass().getCanonicalName().contains("lycanitesmobs"))
			{
				WorldServer world = (WorldServer)event.getEntity().getEntityWorld();
				LootTable table =  world.getLootTableManager().getLootTableFromLocation(new ResourceLocation(Wizardry.MODID, "entities/mob_additions"));

				LootContext ctx = new LootContext.Builder(world).withPlayer(player).build();
				List<ItemStack> stacks = table.generateLootForPools(world.rand, ctx);

				for(ItemStack stack : stacks) {
					event.getEntity().entityDropItem(stack, 0f);
				}
			}

			for(ItemStack stack : InventoryUtils.getPrioritisedHotbarAndOffhand(player)){

				if(stack.getItem() instanceof IManaStoringItem && !((IManaStoringItem)stack.getItem()).isManaFull(stack)
						&& WandHelper.getUpgradeLevel(stack, WizardryItems.siphon_upgrade) > 0){

					int mana = Constants.SIPHON_MANA_PER_LEVEL
							* WandHelper.getUpgradeLevel(stack, WizardryItems.siphon_upgrade)
							+ player.world.rand.nextInt(Constants.SIPHON_MANA_PER_LEVEL);

					if(ItemArtefact.isArtefactActive(player, WizardryItems.ring_siphoning)) mana *= 1.3f;

					((IManaStoringItem)stack.getItem()).rechargeMana(stack, mana);

					break; // Only recharge one item per kill
				}
			}
		}
	}

	// These two are lifted from EntityLivingBase#travel
	private static final double LIVING_ENTITY_GRAVITY = 0.08;
	private static final double LIVING_ENTITY_DRAG = 0.98;

	private static final double LIVING_ENTITY_TERMINAL_VELOCITY = 3.92; // From Minecraft Wiki!

	private static final double LOG_LIVING_ENTITY_DRAG = Math.log(LIVING_ENTITY_DRAG);
	private static final double FALL_TICKS_ERROR_CORRECTION = 0.500841776608447;

	@SubscribeEvent // Priority doesn't matter here, we're only setting event fields so if it's cancelled it won't matter
	public static void onLivingFallEvent(LivingFallEvent event){
		// Why is fall damage based on distance fallen? Why? Who on earth came up with that? It makes no sense whatsoever!
		if(!event.getEntity().world.isRemote && Wizardry.settings.replaceVanillaFallDamage && !Loader.isModLoaded("speedbasedfalldamage")){
			// We want to keep the fall damage EXACTLY THE SAME for free, uninterrupted falls, but fix the weirdness
			// caused when something else changes the entity's velocity
			// All living entities have a gravity of 0.08b/t^2
			// Therefore it would be simple to say v^2 = u^2 + 2gs gives the equivalent fall distance as motionY^2 / 0.16
			// However, Minecraft also has a drag of 0.02 * the velocity, so we actually expect a slightly different value
			// Much maths later...

			double v = event.getEntity().motionY;
			// Players are weird, their velocity somehow resets on the server just before this event fires so
			// instead we're storing the y velocity from the previous tick in WizardData and retrieving it here
			// Of course, if another mod screws things up and sets a player's velocity client-side only then this won't
			// work ...but it's better than having clients calculate their own fall damage
			if(event.getEntity() instanceof EntityPlayer){
				WizardData data = WizardData.get((EntityPlayer)event.getEntity());
				if(data != null){
					v = data.prevMotionY;
				}
			}

			// At terminal velocity, there's no way of finding fall distance from velocity, and the entity is probably dead anyway!
			if(v > -3.9){

				// Just to make the code more readable, java will replace them all with numbers anyway
				double g = LIVING_ENTITY_GRAVITY;
				double f = LIVING_ENTITY_DRAG;
				double lnf = LOG_LIVING_ENTITY_DRAG;
				double tv = LIVING_ENTITY_TERMINAL_VELOCITY;

				// Work backwards from y velocity to get fall time
				// logs are probably slow but it's not like this gets calculated every tick, and we only need one log
				double t = Math.log(((-v - tv) * lnf) / g) / lnf; // Log the number over log the base

				// Because time is in discrete ticks, the above equation for t results in a constant error of
				// +0.500841776608447, so I guess we can just subtract it... if it works it works I guess!
				t -= FALL_TICKS_ERROR_CORRECTION; // Don't cast to int or perform any rounding

				// Now work forwards from t to find the effective fall distance, i.e. the distance the entity would
				// have to freefall to reach the same velocity
				// Don't ask me where the 196 is from, again, it just works!
				double y = (g * Math.pow(f, t)) / (lnf*lnf) + tv * (t) - 196;

				// DEBUG
//				if(event.getEntity() instanceof EntityPlayer){
//					Wizardry.logger.info("Replaced fall distance {} with effective distance {} based on entity velocity", event.getDistance(), y);
//				}

				// Don't let it increase fall damage beyond vanilla values
				if(y < event.getDistance()) event.setDistance((float)y);
			}
		}
	}

}