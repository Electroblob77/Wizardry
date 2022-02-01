package electroblob.wizardry.item;

import com.google.common.collect.Streams;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.entity.construct.EntityFireRing;
import electroblob.wizardry.entity.construct.EntityIceBarrier;
import electroblob.wizardry.entity.living.ISummonedCreature;
import electroblob.wizardry.entity.projectile.EntityDart;
import electroblob.wizardry.entity.projectile.EntityForceOrb;
import electroblob.wizardry.entity.projectile.EntityIceShard;
import electroblob.wizardry.event.SpellCastEvent;
import electroblob.wizardry.event.SpellCastEvent.Source;
import electroblob.wizardry.integration.DamageSafetyChecker;
import electroblob.wizardry.integration.baubles.WizardryBaublesIntegration;
import electroblob.wizardry.registry.*;
import electroblob.wizardry.spell.*;
import electroblob.wizardry.util.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Base class for all artefact items, which handles effects, textures and so on. The majority of artefacts are
 * event-driven so it is unlikely that this class will need to be extended, unless other {@code Item} methods are to be
 * overridden.
 * <p></p>
 * This class contains methods and an enum that mirror those in {@code IBauble} from the Baubles mod. If Baubles is
 * loaded, these are called via the bauble capability; otherwise, they are called from regular {@link Item} methods
 * or events with appropriate checks. This allows wizardry to run with Baubles as an optional dependency.
 * <p></p>
 * <b>Do not reference any Baubles classes from subclasses of this</b>, or the dependency will no longer be optional!
 * Use {@link ItemArtefact#isArtefactActive(EntityPlayer, Item)} to test if a particular artefact is active. Use
 * {@link ItemArtefact#getActiveArtefacts(EntityPlayer, Type...)} to get a list of active artefacts.
 * <p></p>
 * @author Electroblob
 * @since Wizardry 4.2
 * @see electroblob.wizardry.integration.baubles.WizardryBaublesIntegration
 */
@Mod.EventBusSubscriber
public class ItemArtefact extends Item {

	// Artefact checklist:
	// - Create and register item, add model and texture
	// - Program effect, using events if possible (if it only affects a specific spell or entity, in there is ok)
	// - Add name AND description to lang files
	// - Add to loot_tables/subsets/[rarity]_artefacts.json
	// - Add to advancements/artefact.json and advancements/all_artefacts.json

	public enum Type {

		/** An artefact that improves attacking spells. Two of these can be active at any one time. */ RING(2),
		/** An artefact that improves defensive spells. One of these can be active at any one time. */ AMULET(1),
		/** An artefact that improves utility spells. One of these can be active at any one time. */ CHARM(1);

		public final int maxAtOnce;

		Type(int maxAtOnce){
			this.maxAtOnce = maxAtOnce;
		}
	}

	// If Baubles is not installed, artefacts will still work, but must instead be
	// on the player's hotbar (and only the first n of a given type will work, where n is the number of baubles slots of that
	// artefact's type).

	// Rarity was chosen over Tier here for a couple of reasons: firstly, displaying a tier would add unnecessary clutter
	// to a potentially already-long tooltip whereas rarity provides a compact, neat way of displaying it that everyone is
	// reasonably familiar with. Secondly, rarity makes more sense for an artefact, since it's something you find rather
	// than upgrade to, and artefacts are not tied to tiers of wand/spell/whatever - they can be used whenever.
	private final EnumRarity rarity;
	private final Type type;

	/** False if this artefact has been disabled in the config, true otherwise. */
	private boolean enabled = true;

	public ItemArtefact(EnumRarity rarity, Type type){
		setMaxStackSize(1);
		setCreativeTab(WizardryTabs.GEAR);
		this.rarity = rarity;
		this.type = type;
	}

	/** Sets whether this artefact is enabled or not. */
	public void setEnabled(boolean enabled){
		this.enabled = enabled;
	}

	public boolean isEnabled() { return enabled; }

	@Override
	public EnumRarity getRarity(ItemStack stack){
		return rarity;
	}

	public Type getType(){
		return type;
	}

	@Override
	public boolean hasEffect(ItemStack stack){
		return rarity == EnumRarity.EPIC;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, net.minecraft.client.util.ITooltipFlag advanced){
		Wizardry.proxy.addMultiLineDescription(tooltip, "item." + this.getRegistryName() + ".desc");
		if(!enabled) tooltip.add(Wizardry.proxy.translate("item." + Wizardry.MODID + ":generic.disabled", new Style().setColor(TextFormatting.RED)));
	}

	@Nullable
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt){
		return WizardryBaublesIntegration.enabled() ? new WizardryBaublesIntegration.ArtefactBaubleProvider(type) : null;
	}

	// IBauble does of course have an onWornTick method. However, because it's an optional dependency, it doesn't really
	// make sense to use that method when it's easier to just use the isBaubleEquipped method in the same
	// place as the non-baubles check. In other words, most artefacts are event-driven anyway so I'd rather have the
	// tick-driven ones use events as well for the sake of consistency.

	/**
	 * Returns whether the given artefact is active for the given player. If Baubles is loaded, an artefact is active
	 * when it is equipped in an appropriate bauble slot. If Baubles is not loaded, an artefact is active if it is one
	 * of the first n of its type on the player's hands/hotbar, where n is the number of bauble slots of that type.
	 * <p></p>
	 * N.B. This method is inefficient if you are defining multiple artefact behaviours in the same place. In this use
	 * case, it is preferable to use {@link ItemArtefact#getActiveArtefacts(EntityPlayer, Type...)}.
	 *
	 * @param player   The player whose inventory is to be checked.
	 * @param artefact The artefact to check for.
	 * @return True if the player has the artefact and it is active, false if not. Always returns false if the given
	 * item is not an instance of {@code ItemArtefact}.
	 * @throws IllegalArgumentException If the given item is not an artefact.
	 */
	// It's cleaner to cast to ItemArtefact here than wherever it is used - items can't be stored as ItemWhatever objects
	public static boolean isArtefactActive(EntityPlayer player, Item artefact){

		if(!(artefact instanceof ItemArtefact)) throw new IllegalArgumentException("Not an artefact!");

		if(!((ItemArtefact)artefact).enabled) return false; // Disabled in the config

		if(WizardryBaublesIntegration.enabled()){
			return WizardryBaublesIntegration.isBaubleEquipped(player, artefact);
		}else{
			// To find out if the given artefact is one of the first n on the player's hotbar (where n is the maximum
			// number of that kind of artefact that can be active at once):
			return InventoryUtils.getPrioritisedHotbarAndOffhand(player).stream() // Retrieve the stacks in question
					// Filter out all except artefacts of the same type as the given one (preserving order)
					.filter(s -> s.getItem() instanceof ItemArtefact && ((ItemArtefact)s.getItem()).type == ((ItemArtefact)artefact).type)
					.limit(((ItemArtefact)artefact).type.maxAtOnce)    // Ignore all but the first n
					.anyMatch(s -> s.getItem() == artefact); // Check if the remaining stacks contain the artefact
			// Note that streaming a list DOES retain the order (unless you call unordered(), obviously)
		}
	}

	/**
	 * Returns the currently active artefacts for the given player. If Baubles is loaded, an artefact is active
	 * when it is equipped in an appropriate bauble slot. If Baubles is not loaded, an artefact is active if it is one
	 * of the first n of its type on the player's hands/hotbar, where n is the number of bauble slots of that type.
	 * <p></p>
	 * This method is more efficient for processing multiple artefact behaviours at once.
	 *
	 * @param player The player whose inventory is to be checked.
	 * @param types The artefact types to check for. If omitted, all artefact types will be checked.
	 * @return True if the player has the artefact and it is active, false if not. Always returns false if the given
	 * item is not an instance of {@code ItemArtefact}.
	 */
	public static List<ItemArtefact> getActiveArtefacts(EntityPlayer player, Type... types){

		if(types.length == 0) types = Type.values();

		if(WizardryBaublesIntegration.enabled()){
			List<ItemArtefact> artefacts = WizardryBaublesIntegration.getEquippedArtefacts(player, types);
			artefacts.removeIf(i -> !i.enabled); // Remove artefacts that are disabled in the config
			return artefacts;
		}else{

			List<ItemArtefact> artefacts = new ArrayList<>();

			for(Type type : types){
				artefacts.addAll(InventoryUtils.getPrioritisedHotbarAndOffhand(player).stream()
						.filter(s -> s.getItem() instanceof ItemArtefact)
						.map(s -> (ItemArtefact)s.getItem())
						.filter(i -> type == i.type && i.enabled)
						.limit(type.maxAtOnce)
						.collect(Collectors.toList()));
			}

			return artefacts;
		}
	}

	/**
	 * Helper method that scans through all wands on the given player's hotbar and offhand and executes the given action
	 * if any of them have the given spell bound to them. This is a useful code pattern for artefact effects.
	 *
	 * @param player The player whose hotbar is to be checked
	 * @param spell The spell to search for
	 * @param action A {@link Consumer} specifying the action to be performed if a wand with the given spell is found.
	 *               The stack passed to this consumer will be the wand in question.
	 * @return True if the action was executed, false otherwise.
	 */
	public static boolean findMatchingWandAndExecute(EntityPlayer player, Spell spell, Consumer<? super ItemStack> action){

		List<ItemStack> hotbar = InventoryUtils.getPrioritisedHotbarAndOffhand(player);

		Optional<ItemStack> stack = hotbar.stream().filter(s -> s.getItem() instanceof ISpellCastingItem
				&& Arrays.asList(((ISpellCastingItem)s.getItem()).getSpells(s)).contains(spell)).findFirst();

		stack.ifPresent(action);
		return stack.isPresent();
	}

	/**
	 * Helper method that scans through all wands on the given player's hotbar and offhand and casts the given spell if
	 * it is bound to any of them. This is a useful code pattern for artefact effects.
	 *
	 * @param player The player whose hotbar is to be checked
	 * @param spell The spell to search for and cast
	 * @return True if the spell was cast, false otherwise.
	 */
	public static boolean findMatchingWandAndCast(EntityPlayer player, Spell spell){

		return findMatchingWandAndExecute(player, spell, wand -> {

			SpellModifiers modifiers = new SpellModifiers();

			if(((ISpellCastingItem)wand.getItem()).canCast(wand, spell, player, EnumHand.MAIN_HAND, 0, modifiers)){
				((ISpellCastingItem)wand.getItem()).cast(wand, spell, player, EnumHand.MAIN_HAND, 0, modifiers);
			}
		});
	}

	// ================================================ Event Handlers ================================================

	@SubscribeEvent
	public static void onPlayerTickEvent(TickEvent.PlayerTickEvent event){

		if(event.phase == TickEvent.Phase.START){

			EntityPlayer player = event.player;
			World world = player.world;

			for(ItemArtefact artefact : getActiveArtefacts(player)){

				if(artefact == WizardryItems.ring_condensing){

					if(player.ticksExisted % 150 == 0){
						for(ItemStack stack : InventoryUtils.getHotbar(player)){
							// Needs to be both of these interfaces because this ring only recharges wands
							// (or more accurately, chargeable spellcasting items)
							if(stack.getItem() instanceof ISpellCastingItem && stack.getItem() instanceof IManaStoringItem)
								((IManaStoringItem)stack.getItem()).rechargeMana(stack, 1);
						}
					}

				}else if(artefact == WizardryItems.amulet_arcane_defence){

					if(player.ticksExisted % 300 == 0){
						for(ItemStack stack : player.getArmorInventoryList()){
							// IManaStoringItem is sufficient, since anything in the armour slots is probably armour
							if(stack.getItem() instanceof IManaStoringItem)
								((IManaStoringItem)stack.getItem()).rechargeMana(stack, 1);
						}
					}

				}else if(artefact == WizardryItems.amulet_recovery){

					if(player.shouldHeal() && player.getHealth() < player.getMaxHealth()/2
						&& player.ticksExisted % 50 == 0){

						int totalArmourMana = Streams.stream(player.getArmorInventoryList())
								.filter(s -> s.getItem() instanceof IManaStoringItem)
								.mapToInt(s -> ((IManaStoringItem)s.getItem()).getMana(s))
								.sum();

						if(totalArmourMana >= 2){
							player.heal(1);
							// 2 mana per half-heart, randomly distributed
							List<ItemStack> chargedArmour = Streams.stream(player.getArmorInventoryList())
									.filter(s -> s.getItem() instanceof IManaStoringItem)
									.filter(s -> !((IManaStoringItem)s.getItem()).isManaEmpty(s))
									.collect(Collectors.toList());

							if(chargedArmour.size() == 1){
								((IManaStoringItem)chargedArmour.get(0).getItem()).consumeMana(chargedArmour.get(0), 2, player);
							}else{
								Collections.shuffle(chargedArmour);
								((IManaStoringItem)chargedArmour.get(0).getItem()).consumeMana(chargedArmour.get(0), 1, player);
								((IManaStoringItem)chargedArmour.get(1).getItem()).consumeMana(chargedArmour.get(1), 1, player);
							}
						}
					}

				}else if(artefact == WizardryItems.amulet_glide){
					// This should be a chance per fall, so we can't just check fall distance is greater than 3 each tick
					// Based on a stationary start and a gravity acceleration of 0.02 blocks/tick^2, at 3 blocks of fall
					// distance the player should be falling at about 0.35b/t, so 0.5 blocks should be enough of a window
					if(player.fallDistance > 3f && player.fallDistance < 3.5f && player.world.rand.nextFloat() < 0.5f){
						if(!WizardData.get(player).isCasting()) WizardData.get(player).startCastingContinuousSpell(Spells.glide, new SpellModifiers(), 600);
					}else if(player.onGround){
						WizardData data = WizardData.get(player);
						if(data.currentlyCasting() == Spells.glide) data.stopCastingContinuousSpell();
					}

				}else if(artefact == WizardryItems.amulet_auto_shield){

					findMatchingWandAndExecute(player, Spells.shield, wand -> {

						if(wand.getItem() instanceof ItemScroll) return; // Ignore scrolls since they shouldn't work

						List<Entity> projectiles = EntityUtils.getEntitiesWithinRadius(5, player.posX, player.posY, player.posZ, world, Entity.class);
						projectiles.removeIf(e -> !(e instanceof IProjectile));
						Vec3d look = player.getLookVec();
						Vec3d playerPos = player.getPositionVector().add(0, player.height/2, 0);

						for(Entity projectile : projectiles){
							Vec3d vec = playerPos.subtract(projectile.getPositionVector()).normalize();
							double angle = Math.acos(vec.scale(-1).dotProduct(look));
							if(angle > Math.PI * 0.4f) continue; // (Roughly) the angle the shield will protect
							Vec3d velocity = new Vec3d(projectile.motionX, projectile.motionY, projectile.motionZ).normalize();
							double angle1 = Math.acos(vec.dotProduct(velocity));
							if(angle1 < Math.PI * 0.2f){
								SpellModifiers modifiers = new SpellModifiers();
								if(((ISpellCastingItem)wand.getItem()).canCast(wand, Spells.shield, player, EnumHand.MAIN_HAND, 0, modifiers)){
									((ISpellCastingItem)wand.getItem()).cast(wand, Spells.shield, player, EnumHand.MAIN_HAND, 0, modifiers);
								}
								break;
							}
						}
					});

				}else if(artefact == WizardryItems.amulet_frost_warding){

					if(!world.isRemote && player.ticksExisted % 40 == 0){

						List<EntityIceBarrier> barriers = world.getEntitiesWithinAABB(EntityIceBarrier.class, player.getEntityBoundingBox().grow(1.5));

						// Check whether any barriers near the player are facing away from them, meaning the player is behind them
						if(!barriers.isEmpty() && barriers.stream().anyMatch(b -> b.getLookVec().dotProduct(b.getPositionVector().subtract(player.getPositionVector())) > 0)){
							player.addPotionEffect(new PotionEffect(WizardryPotions.ward, 50, 1));
						}

					}

				}else if(artefact == WizardryItems.charm_feeding){
					// Every 5 seconds, feed the player if they are hungry enough
					if(player.ticksExisted % 100 == 0){
						if(player.getFoodStats().getFoodLevel() < 20 - Spells.satiety.getProperty(Satiety.HUNGER_POINTS).intValue()){
							if(findMatchingWandAndCast(player, Spells.satiety)) continue;
						}
						if(player.getFoodStats().getFoodLevel() < 20 - Spells.replenish_hunger.getProperty(ReplenishHunger.HUNGER_POINTS).intValue()){
							findMatchingWandAndCast(player, Spells.replenish_hunger);
						}
					}
				}
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void onSpellCastPreEvent(SpellCastEvent.Pre event){

		if(event.getCaster() instanceof EntityPlayer){

			EntityPlayer player = (EntityPlayer)event.getCaster();
			SpellModifiers modifiers = event.getModifiers();

			for(ItemArtefact artefact : getActiveArtefacts(player)){

				float potency = modifiers.get(SpellModifiers.POTENCY);
				float cooldown = modifiers.get(WizardryItems.cooldown_upgrade);
				Biome biome = player.world.getBiome(player.getPosition());

				if(artefact == WizardryItems.ring_battlemage){

					if(player.getHeldItemOffhand().getItem() instanceof ISpellCastingItem
						&& ImbueWeapon.isSword(player.getHeldItemMainhand())){
						modifiers.set(SpellModifiers.POTENCY, 1.1f * potency, false);
					}

				}else if(artefact == WizardryItems.ring_fire_biome){

					if(event.getSpell().getElement() == Element.FIRE
							&& BiomeDictionary.hasType(biome, BiomeDictionary.Type.HOT)
							&& BiomeDictionary.hasType(biome, BiomeDictionary.Type.DRY)){
						modifiers.set(SpellModifiers.POTENCY, 1.3f * potency, false);
					}

				}else if(artefact == WizardryItems.ring_ice_biome){

					if(event.getSpell().getElement() == Element.ICE
							&& BiomeDictionary.hasType(biome, BiomeDictionary.Type.SNOWY)){
						modifiers.set(SpellModifiers.POTENCY, 1.3f * potency, false);
					}

				}else if(artefact == WizardryItems.ring_earth_biome){

					if(event.getSpell().getElement() == Element.EARTH
							// If it was any forest that would be far too many, so taigas and jungles are excluded
							&& BiomeDictionary.hasType(biome, BiomeDictionary.Type.FOREST)
							&& !BiomeDictionary.hasType(biome, BiomeDictionary.Type.CONIFEROUS)
							&& !BiomeDictionary.hasType(biome, BiomeDictionary.Type.JUNGLE)){
						modifiers.set(SpellModifiers.POTENCY, 1.3f * potency, false);
					}

				}else if(artefact == WizardryItems.ring_storm){

					if(event.getSpell().getElement() == Element.LIGHTNING && player.world.isThundering()){
						modifiers.set(WizardryItems.cooldown_upgrade, cooldown * 0.3f, false);
					}

				}else if(artefact == WizardryItems.ring_full_moon){

					if(event.getSpell().getElement() == Element.EARTH && !player.world.isDaytime()
							&& player.world.provider.getMoonPhase(player.world.getWorldTime()) == 0){
						modifiers.set(WizardryItems.cooldown_upgrade, cooldown * 0.3f, false);
					}

				}else if(artefact == WizardryItems.ring_blockwrangler){

					if(event.getSpell() == Spells.greater_telekinesis){
						modifiers.set(SpellModifiers.POTENCY, modifiers.get(SpellModifiers.POTENCY) * 2, false);
					}

				}else if(artefact == WizardryItems.ring_conjurer){

					if(event.getSpell() instanceof SpellConjuration){
						modifiers.set(WizardryItems.duration_upgrade, modifiers.get(WizardryItems.duration_upgrade) * 2, false);
					}

				}else if(artefact == WizardryItems.charm_minion_health){
					// We COULD check the spell is a SpellMinion here, but there's really no point
					modifiers.set(SpellMinion.HEALTH_MODIFIER, 1.25f * modifiers.get(SpellMinion.HEALTH_MODIFIER), true);

				}else if(artefact == WizardryItems.charm_flight){

					if(event.getSpell() == Spells.flight || event.getSpell() == Spells.glide){
						modifiers.set(SpellModifiers.POTENCY, 1.5f * potency, true);
					}

				}else if(artefact == WizardryItems.charm_experience_tome){

					modifiers.set(SpellModifiers.PROGRESSION, modifiers.get(SpellModifiers.PROGRESSION) * 1.5f, false);

				}else if(artefact == WizardryItems.charm_hunger_casting){

					if(!player.capabilities.isCreativeMode && event.getSource() == Source.WAND && !event.getSpell().isContinuous){ // TODO: Continuous spells?

						ItemStack wand = player.getHeldItemMainhand();

						if(!(wand.getItem() instanceof ISpellCastingItem && wand.getItem() instanceof IManaStoringItem)){
							wand = player.getHeldItemOffhand();
							if(!(wand.getItem() instanceof ISpellCastingItem && wand.getItem() instanceof IManaStoringItem)) return;
						}

						if(((IManaStoringItem)wand.getItem()).getMana(wand) < event.getSpell().getCost() * modifiers.get(SpellModifiers.COST)){

							int hunger = event.getSpell().getCost() / 5;

							if(player.getFoodStats().getFoodLevel() >= hunger){
								player.getFoodStats().addStats(-hunger, 0);
								modifiers.set(SpellModifiers.COST, 0, false);
							}
						}

					}

				}
			}
		}
	}

	@SubscribeEvent
	public static void onSpellCastPostEvent(SpellCastEvent.Post event){

		if(event.getCaster() instanceof EntityPlayer){

			EntityPlayer player = (EntityPlayer)event.getCaster();

			if(isArtefactActive(player, WizardryItems.ring_paladin)){

				if(event.getSpell() instanceof Heal || event.getSpell() instanceof HealAlly || event.getSpell() instanceof GreaterHeal){
					// Spell properties allow all three of the above spells to be dealt with the same way - neat!
					float healthGained = event.getSpell().getProperty(Spell.HEALTH).floatValue() * event.getModifiers().get(SpellModifiers.POTENCY);

					List<EntityLivingBase> nearby = EntityUtils.getLivingWithinRadius(4, player.posX, player.posY, player.posZ, event.getWorld());

					for(EntityLivingBase entity : nearby){
						if(AllyDesignationSystem.isAllied(player, entity) && entity.getHealth() > 0 && entity.getHealth() < entity.getMaxHealth()){
							entity.heal(healthGained * 0.2f); // 1/5 of the amount healed by the spell itself
							if(event.getWorld().isRemote) ParticleBuilder.spawnHealParticles(event.getWorld(), entity);
						}
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void onLivingUpdateEvent(LivingEvent.LivingUpdateEvent event){

		EntityLivingBase entity = event.getEntityLiving();

		// No point doing this every tick, every 2.5 seconds should be enough
		if(entity.ticksExisted % 50 == 0 && entity.isPotionActive(WizardryPotions.mind_control)){

			NBTTagCompound entityNBT = entity.getEntityData();

			if(entityNBT.hasUniqueId(MindControl.NBT_KEY)){

				Entity caster = EntityUtils.getEntityByUUID(entity.world, entityNBT.getUniqueId(MindControl.NBT_KEY));

				if(caster instanceof EntityPlayer){

					if(isArtefactActive((EntityPlayer)caster, WizardryItems.ring_mind_control)){

						EntityUtils.getEntitiesWithinRadius(3, entity.posX, entity.posY, entity.posZ, entity.world, EntityLiving.class).stream()
								.filter(e -> e.world.rand.nextInt(10) == 0)
								.filter(MindControl::canControl)
								.filter(e -> AllyDesignationSystem.isValidTarget(caster, e))
								.forEach(target -> MindControl.startControlling(target, (EntityPlayer)caster,
										// Control the new target for only the remaining duration, otherwise it could go on forever!
										entity.getActivePotionEffect(WizardryPotions.mind_control).getDuration()));
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void onLivingHurtEvent(LivingHurtEvent event){

		if(event.getEntity() instanceof EntityPlayer){

			EntityPlayer player = (EntityPlayer)event.getEntity();

			for(ItemArtefact artefact : getActiveArtefacts(player)){

				if(artefact == WizardryItems.amulet_warding){

					if(!event.getSource().isUnblockable() && event.getSource().isMagicDamage()){
						event.setAmount(event.getAmount() * 0.9f);
					}

				}else if(artefact == WizardryItems.amulet_fire_protection){

					if(event.getSource().isFireDamage()) event.setAmount(event.getAmount() * 0.7f);

				}else if(artefact == WizardryItems.amulet_ice_protection){

					if(event.getSource() instanceof IElementalDamage
							&& ((IElementalDamage)event.getSource()).getType() == MagicDamage.DamageType.FROST)
						event.setAmount(event.getAmount() * 0.7f);

				}else if(artefact == WizardryItems.amulet_channeling){

					if(player.world.rand.nextFloat() < 0.3f && event.getSource() instanceof IElementalDamage
							&& ((IElementalDamage)event.getSource()).getType() == MagicDamage.DamageType.SHOCK){
						event.setCanceled(true);
						return;
					}

				}else if(artefact == WizardryItems.amulet_fire_cloaking){

					if(!event.getSource().isUnblockable()){

						List<EntityFireRing> fireRings = player.world.getEntitiesWithinAABB(EntityFireRing.class, player.getEntityBoundingBox());

						for(EntityFireRing fireRing : fireRings){
							if(fireRing.getCaster() instanceof EntityPlayer && (fireRing.getCaster() == player
									|| AllyDesignationSystem.isOwnerAlly(player, fireRing))){
								event.setAmount(event.getAmount() * 0.25f);
							}
						}
					}

				}else if(artefact == WizardryItems.amulet_potential){

					if(player.world.rand.nextFloat() < 0.2f && EntityUtils.isMeleeDamage(event.getSource())
						&& event.getSource().getTrueSource() instanceof EntityLivingBase){

						EntityLivingBase target = (EntityLivingBase)event.getSource().getTrueSource();

						if(player.world.isRemote){

							ParticleBuilder.create(ParticleBuilder.Type.LIGHTNING).entity(event.getEntity())
									.pos(0, event.getEntity().height/2, 0).target(target).spawn(player.world);

							ParticleBuilder.spawnShockParticles(player.world, target.posX,
									target.posY + target.height/2, target.posZ);
						}

						DamageSafetyChecker.attackEntitySafely(target, MagicDamage.causeDirectMagicDamage(player,
								MagicDamage.DamageType.SHOCK, true), Spells.static_aura.getProperty(Spell.DAMAGE).floatValue(), event.getSource().getDamageType());
						target.playSound(WizardrySounds.SPELL_STATIC_AURA_RETALIATE, 1.0F, player.world.rand.nextFloat() * 0.4F + 1.5F);

					}

				}else if(artefact == WizardryItems.amulet_lich){

					if(!event.getSource().isUnblockable() && player.world.rand.nextFloat() < 0.15f){

						List<EntityLiving> nearbyMobs = EntityUtils.getEntitiesWithinRadius(5, player.posX, player.posY, player.posZ, player.world, EntityLiving.class);
						nearbyMobs.removeIf(e -> !(e instanceof ISummonedCreature && ((ISummonedCreature)e).getCaster() == player));

						if(!nearbyMobs.isEmpty()){
							Collections.shuffle(nearbyMobs);
							// Even though we're passing the same damage source through, we still need the safety check
							DamageSafetyChecker.attackEntitySafely(nearbyMobs.get(0), event.getSource(), event.getAmount(), event.getSource().getDamageType());
							event.setCanceled(true);
							return; // Standard practice: stop as soon as the event is canceled
						}
					}

				}else if(artefact == WizardryItems.amulet_banishing){

					if(player.world.rand.nextFloat() < 0.2f && EntityUtils.isMeleeDamage(event.getSource())
							&& event.getSource().getTrueSource() instanceof EntityLivingBase){

						EntityLivingBase target = (EntityLivingBase)event.getSource().getTrueSource();
						((Banish)Spells.banish).teleport(target, target.world, 8 + target.world.rand.nextDouble() * 8);
					}

				}else if(artefact == WizardryItems.amulet_transience){

					if(player.getHealth() <= 6 && player.world.rand.nextFloat() < 0.25f){
						player.addPotionEffect(new PotionEffect(WizardryPotions.transience, 300));
						player.addPotionEffect(new PotionEffect(MobEffects.INVISIBILITY, 300, 0, false, false));
					}
				}
			}
		}

		if(event.getSource().getTrueSource() instanceof EntityPlayer){

			EntityPlayer player = (EntityPlayer)event.getSource().getTrueSource();
			ItemStack mainhandItem = player.getHeldItemMainhand();
			World world = player.world;

			for(ItemArtefact artefact : getActiveArtefacts(player)){

				if(artefact == WizardryItems.ring_fire_melee){
					// Used ItemWand intentionally because we need the element
					// Other mods can always make their own events if they want their own spellcasting items to do this
					if(EntityUtils.isMeleeDamage(event.getSource()) && mainhandItem.getItem() instanceof ItemWand
							&& ((ItemWand)mainhandItem.getItem()).element == Element.FIRE){
						event.getEntity().setFire(5);
					}

				}else if(artefact == WizardryItems.ring_ice_melee){

					if(EntityUtils.isMeleeDamage(event.getSource()) && mainhandItem.getItem() instanceof ItemWand
							&& ((ItemWand)mainhandItem.getItem()).element == Element.ICE){
						event.getEntityLiving().addPotionEffect(new PotionEffect(WizardryPotions.frost, 200, 0));
					}

				}else if(artefact == WizardryItems.ring_lightning_melee){

					if(EntityUtils.isMeleeDamage(event.getSource()) && mainhandItem.getItem() instanceof ItemWand
							&& ((ItemWand)mainhandItem.getItem()).element == Element.LIGHTNING){

						EntityUtils.getLivingWithinRadius(3, player.posX, player.posY, player.posZ, world).stream()
								.filter(EntityUtils::isLiving)
								.filter(e -> e != player)
								.min(Comparator.comparingDouble(player::getDistanceSq))
								.ifPresent(target -> {

									if(world.isRemote){

										ParticleBuilder.create(ParticleBuilder.Type.LIGHTNING).entity(event.getEntity())
												.pos(0, event.getEntity().height/2, 0).target(target).spawn(world);

										ParticleBuilder.spawnShockParticles(world, target.posX,
												target.posY + target.height/2, target.posZ);
									}

									DamageSafetyChecker.attackEntitySafely(target, MagicDamage.causeDirectMagicDamage(player,
											MagicDamage.DamageType.SHOCK, true), Spells.static_aura.getProperty(Spell.DAMAGE).floatValue(), event.getSource().getDamageType());
									target.playSound(WizardrySounds.SPELL_STATIC_AURA_RETALIATE, 1.0F, world.rand.nextFloat() * 0.4F + 1.5F);
								});
					}

				}else if(artefact == WizardryItems.ring_necromancy_melee){

					if(EntityUtils.isMeleeDamage(event.getSource()) && mainhandItem.getItem() instanceof ItemWand
							&& ((ItemWand)mainhandItem.getItem()).element == Element.NECROMANCY){
						event.getEntityLiving().addPotionEffect(new PotionEffect(MobEffects.WITHER, 200, 0));
					}

				}else if(artefact == WizardryItems.ring_earth_melee){

					if(EntityUtils.isMeleeDamage(event.getSource()) && mainhandItem.getItem() instanceof ItemWand
							&& ((ItemWand)mainhandItem.getItem()).element == Element.EARTH){
						event.getEntityLiving().addPotionEffect(new PotionEffect(MobEffects.POISON, 200, 0));
					}

				}else if(artefact == WizardryItems.ring_shattering){

					if(!player.world.isRemote && player.world.rand.nextFloat() < 0.15f
							&& event.getEntityLiving().getHealth() < 12f // Otherwise it's a bit overpowered!
							&& event.getEntityLiving().isPotionActive(WizardryPotions.frost)
							&& EntityUtils.isMeleeDamage(event.getSource())){

						event.setAmount(12f);

						for(int i = 0; i < 8; i++){
							double dx = event.getEntity().world.rand.nextDouble() - 0.5;
							double dy = event.getEntity().world.rand.nextDouble() - 0.5;
							double dz = event.getEntity().world.rand.nextDouble() - 0.5;
							EntityIceShard iceshard = new EntityIceShard(event.getEntity().world);
							iceshard.setPosition(event.getEntity().posX + dx + Math.signum(dx) * event.getEntity().width,
									event.getEntity().posY + event.getEntity().height/2 + dy,
									event.getEntity().posZ + dz + Math.signum(dz) * event.getEntity().width);
							iceshard.motionX = dx * 1.5;
							iceshard.motionY = dy * 1.5;
							iceshard.motionZ = dz * 1.5;
							iceshard.setCaster(player);
							event.getEntity().world.spawnEntity(iceshard);
						}
					}

				}else if(artefact == WizardryItems.ring_soulbinding){

					// Best guess at necromancy spell damage: either it's wither damage...
					if((event.getSource() instanceof IElementalDamage
							&& (((IElementalDamage)event.getSource()).getType() == MagicDamage.DamageType.WITHER))
						// or it's direct, non-melee damage and the player is holding a wand with a necromancy spell selected
						|| (event.getSource().getImmediateSource() == player && !EntityUtils.isMeleeDamage(event.getSource())
							&& Streams.stream(player.getHeldEquipment()).anyMatch(s -> s.getItem() instanceof ISpellCastingItem
							&& ((ISpellCastingItem)s.getItem()).getCurrentSpell(s).getElement() == Element.NECROMANCY))){

						CurseOfSoulbinding.getSoulboundCreatures(WizardData.get(player)).add(event.getEntity().getUniqueID());
					}

				}else if(artefact == WizardryItems.ring_leeching){

					// Best guess at necromancy spell damage: either it's wither damage...
					if(player.world.rand.nextFloat() < 0.3f && ((event.getSource() instanceof IElementalDamage
							&& (((IElementalDamage)event.getSource()).getType() == MagicDamage.DamageType.WITHER))
							// ...or it's direct, non-melee damage and the player is holding a wand with a necromancy spell selected
							|| (event.getSource().getImmediateSource() == player && !EntityUtils.isMeleeDamage(event.getSource())
							&& Streams.stream(player.getHeldEquipment()).anyMatch(s -> s.getItem() instanceof ISpellCastingItem
							&& ((ISpellCastingItem)s.getItem()).getCurrentSpell(s).getElement() == Element.NECROMANCY
							&& ((ISpellCastingItem)s.getItem()).getCurrentSpell(s) != Spells.life_drain)))){

						if(player.shouldHeal()){
							player.heal(event.getAmount() * Spells.life_drain.getProperty(LifeDrain.HEAL_FACTOR).floatValue());
						}
					}

				}else if(artefact == WizardryItems.ring_poison){

					// Best guess at earth spell damage: either it's poison damage...
					if((event.getSource() instanceof IElementalDamage
							&& (((IElementalDamage)event.getSource()).getType() == MagicDamage.DamageType.POISON))
							// ...or it was from a dart...
							|| event.getSource().getImmediateSource() instanceof EntityDart
							// ...or it's direct, non-melee damage and the player is holding a wand with an earth spell selected
							|| (event.getSource().getImmediateSource() == player && !EntityUtils.isMeleeDamage(event.getSource())
							&& Streams.stream(player.getHeldEquipment()).anyMatch(s -> s.getItem() instanceof ISpellCastingItem
							&& ((ISpellCastingItem)s.getItem()).getCurrentSpell(s).getElement() == Element.EARTH))){

						event.getEntityLiving().addPotionEffect(new PotionEffect(MobEffects.POISON, 200, 0));
					}

				}else if(artefact == WizardryItems.ring_extraction){

					// Best guess at sorcery spell damage: either it's force damage...
					if((event.getSource() instanceof IElementalDamage
							&& (((IElementalDamage)event.getSource()).getType() == MagicDamage.DamageType.FORCE))
							// ...or it was from a force orb...
							|| event.getSource().getImmediateSource() instanceof EntityForceOrb
							// ...or it's direct, non-melee damage and the player is holding a wand with a sorcery spell selected
							|| (event.getSource().getImmediateSource() == player && !EntityUtils.isMeleeDamage(event.getSource())
							&& Streams.stream(player.getHeldEquipment()).anyMatch(s -> s.getItem() instanceof ISpellCastingItem
							&& ((ISpellCastingItem)s.getItem()).getCurrentSpell(s).getElement() == Element.SORCERY))){

						InventoryUtils.getPrioritisedHotbarAndOffhand(player).stream()
								.filter(s -> s.getItem() instanceof ISpellCastingItem && s.getItem() instanceof IManaStoringItem
										&& !((IManaStoringItem)s.getItem()).isManaFull(s))
								.findFirst()
								.ifPresent(s -> ((IManaStoringItem)s.getItem()).rechargeMana(s, 4 + world.rand.nextInt(3)));
					}

				}

			}
		}
	}

	@SubscribeEvent
	public static void onLivingDeathEvent(LivingDeathEvent event){

		if(event.getSource().getTrueSource() instanceof EntityPlayer){

			EntityPlayer player = (EntityPlayer)event.getSource().getTrueSource();

			for(ItemArtefact artefact : getActiveArtefacts(player)){

				if(artefact == WizardryItems.ring_combustion){

					if(event.getSource() instanceof IElementalDamage && ((IElementalDamage)event.getSource()).getType() == MagicDamage.DamageType.FIRE){
						event.getEntity().world.createExplosion(event.getEntity(), event.getEntity().posX, event.getEntity().posY,
								event.getEntity().posZ, 1.5f, false);
					}

				}else if(artefact == WizardryItems.ring_disintegration){

					if(event.getSource() instanceof IElementalDamage && ((IElementalDamage)event.getSource()).getType() == MagicDamage.DamageType.FIRE){
						Disintegration.spawnEmbers(event.getEntity().world, player, event.getEntity(),
								Spells.disintegration.getProperty(Disintegration.EMBER_COUNT).intValue());
					}

				}else if(artefact == WizardryItems.ring_arcane_frost){

					if(!player.world.isRemote && event.getSource() instanceof IElementalDamage
							&& ((IElementalDamage)event.getSource()).getType() == MagicDamage.DamageType.FROST){

						for(int i = 0; i < 8; i++){
							double dx = event.getEntity().world.rand.nextDouble() - 0.5;
							double dy = event.getEntity().world.rand.nextDouble() - 0.5;
							double dz = event.getEntity().world.rand.nextDouble() - 0.5;
							EntityIceShard iceshard = new EntityIceShard(event.getEntity().world);
							iceshard.setPosition(event.getEntity().posX + dx + Math.signum(dx) * event.getEntity().width,
									event.getEntity().posY + event.getEntity().height/2 + dy,
									event.getEntity().posZ + dz + Math.signum(dz) * event.getEntity().width);
							iceshard.motionX = dx * 1.5;
							iceshard.motionY = dy * 1.5;
							iceshard.motionZ = dz * 1.5;
							iceshard.setCaster(player);
							event.getEntity().world.spawnEntity(iceshard);
						}
					}
				}
			}

		}
	}

	@SubscribeEvent(priority = EventPriority.HIGH) // Needs to happen before gravestones, etc.
	public static void onPlayerDropsEvent(PlayerDropsEvent event){
		// Amulet of the immortal allows players to hold onto a wand with resurrection
		// This needs to happen or we can't cast the spell with it and use up the mana
		if(isArtefactActive(event.getEntityPlayer(), WizardryItems.amulet_resurrection)){

			EntityItem item = event.getDrops().stream()
					.filter(e -> Resurrection.canStackResurrect(e.getItem(), event.getEntityPlayer()))
					.findFirst().orElse(null);

			if(item == null) return; // The player didn't have a wand with resurrection on it
			if(!InventoryUtils.getHotbar(event.getEntityPlayer()).contains(ItemStack.EMPTY)) return; // No space on hotbar

			event.getDrops().remove(item);
			// At this point the player probably has nothing in their hand, but if not just find a free space somewhere
			if(event.getEntityPlayer().getHeldItemMainhand().isEmpty()) event.getEntityPlayer().setHeldItem(EnumHand.MAIN_HAND, item.getItem());
			else event.getEntityPlayer().addItemStackToInventory(item.getItem()); // Always chooses hotbar slots first
		}
	}

	@SubscribeEvent
	public static void onPotionApplicableEvent(PotionEvent.PotionApplicableEvent event){

		if(event.getEntity() instanceof EntityPlayer){

			EntityPlayer player = (EntityPlayer)event.getEntity();

			for(ItemArtefact artefact : getActiveArtefacts(player)){

				if(artefact == WizardryItems.amulet_ice_immunity){

					if(event.getPotionEffect().getPotion() == WizardryPotions.frost) event.setResult(Event.Result.DENY);

				}else if(artefact == WizardryItems.amulet_wither_immunity){

					if(event.getPotionEffect().getPotion() == MobEffects.WITHER) event.setResult(Event.Result.DENY);
				}
			}
		}
	}

	@SubscribeEvent
	public static void onItemPickupEvent(PlayerEvent.ItemPickupEvent event){

		// ItemPickupEvent is just a convenient trigger for this; we don't actually care what got picked up
		if(isArtefactActive(event.player, WizardryItems.charm_auto_smelt)){

			// So this doesn't waste mana, only cast pocket furnace when it would smelt the maximum number of items
			if(event.player.inventory.mainInventory.stream()
					.filter(s -> !FurnaceRecipes.instance().getSmeltingResult(s).isEmpty())
					.mapToInt(ItemStack::getCount)
					.sum() >= Spells.pocket_furnace.getProperty(PocketFurnace.ITEMS_SMELTED).intValue()){

				findMatchingWandAndCast(event.player, Spells.pocket_furnace);
			}
		}
	}

}
