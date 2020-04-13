package electroblob.wizardry.spell;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.data.IVariable;
import electroblob.wizardry.data.IVariable.Variable;
import electroblob.wizardry.data.Persistence;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.entity.living.*;
import electroblob.wizardry.entity.projectile.*;
import electroblob.wizardry.integration.DamageSafetyChecker;
import electroblob.wizardry.packet.PacketControlInput;
import electroblob.wizardry.packet.PacketPossession;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.registry.WizardryItems;
import electroblob.wizardry.util.NBTExtras;
import electroblob.wizardry.util.ParticleBuilder;
import electroblob.wizardry.util.ParticleBuilder.Type;
import electroblob.wizardry.util.SpellModifiers;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Mod.EventBusSubscriber
public class Possession extends SpellRay {

	/** A {@code ResourceLocation} representing the shader file used when possessing an entity. */
	public static final ResourceLocation SHADER = new ResourceLocation(Wizardry.MODID, "shaders/post/possession.json");

	/** The NBT tag name for storing the possessing entity's UUID in the target's tag compound. */
	public static final String NBT_KEY = "possessor";
	/** The NBT tag name for storing the possessor's previous inventory in their tag compound. */
	public static final String INVENTORY_NBT_KEY = "prevInventory";

	/** The health (in half-hearts) below or equal to which the possessor will automatically stop possessing. */
	public static final String CRITICAL_HEALTH = "critical_health";

	private static final int PROJECTILE_COOLDOWN = 30;

	public static final IVariable<Integer> TIMER_KEY = new Variable<Integer>(Persistence.DIMENSION_CHANGE).withTicker(Possession::update);
	public static final IVariable<EntityLiving> POSSESSEE_KEY = new Variable<>(Persistence.DIMENSION_CHANGE);
	public static final IVariable<Integer> SHOOT_COOLDOWN_KEY = new Variable<Integer>(Persistence.DIMENSION_CHANGE).withTicker((p, n) -> Math.max(n-1, 0));

	private static final Multimap<Class<? extends EntityLiving>, BiConsumer<?, EntityPlayer>> abilities = HashMultimap.create();
	private static final Map<Class<? extends EntityLiving>, Function<World, ? extends IProjectile>> projectiles = new HashMap<>();

	private static final Map<IAttribute, UUID> INHERITED_ATTRIBUTES;

	static {

		INHERITED_ATTRIBUTES = ImmutableMap.of(
				SharedMonsterAttributes.MOVEMENT_SPEED, UUID.fromString("f65cfcaf-e7ec-4dfb-aa6c-711735d007e3"),
				SharedMonsterAttributes.ATTACK_DAMAGE, UUID.fromString("ab67c89e-74a5-4e27-9621-40bffb4f7a03"),
				SharedMonsterAttributes.KNOCKBACK_RESISTANCE, UUID.fromString("05529535-9bcf-42bb-8822-45f5ce6a8f08"));

		addAbility(EntitySpider.class, (spider, player) -> { if(player.collidedHorizontally) player.motionY = 0.2; });
		addAbility(EntityChicken.class, (chicken, player) -> { if(!player.onGround && player.motionY < 0) player.motionY *= 0.6D; });
		addAbility(EntityLiving.class, (entity, player) -> { if(!entity.isImmuneToFire() && player.isBurning()) player.extinguish(); });

		addProjectile(EntitySnowman.class, EntitySnowball::new); // Woooo snowballs!
		addProjectile(EntityBlaze.class, EntityMagicFireball::new); // Ugh normal fireballs don't fit so let's just use mine!
		addProjectile(EntityGhast.class, EntityLargeMagicFireball::new);
		addProjectile(EntityIceWraith.class, EntityIceShard::new);
		addProjectile(EntityShadowWraith.class, EntityDarknessOrb::new);
		addProjectile(EntityStormElemental.class, EntityLightningDisc::new);
		addProjectile(EntityWitch.class, EntityPotion::new);

	}

	public Possession(){
		super("possession", false, EnumAction.NONE);
		addProperties(EFFECT_DURATION, CRITICAL_HEALTH);
	}

	@Override public boolean canBeCastBy(EntityLiving npc, boolean override) { return false; }
	@Override public boolean canBeCastBy(TileEntityDispenser dispenser) { return false; }

	@Override
	public boolean requiresPacket(){
		return false; // Has its own packet
	}

	@Override
	protected SoundEvent[] createSounds(){
		return createSoundsWithSuffixes("possess", "end");
	}

	@Override
	public boolean cast(World world, EntityPlayer caster, EnumHand hand, int ticksInUse, SpellModifiers modifiers){

		Vec3d look = caster.getLookVec();
		Vec3d origin = new Vec3d(caster.posX, caster.getEntityBoundingBox().minY + caster.getEyeHeight() - Y_OFFSET, caster.posZ);

		if(!shootSpell(world, origin, look, caster, ticksInUse, modifiers)) return false;

		if(casterSwingsArm(world, caster, hand, ticksInUse, modifiers)) caster.swingArm(hand);
		this.playSound(world, caster, ticksInUse, -1, modifiers, "possess"); // TODO: There must be a better way...
		return true;
	}

	@Override
	protected boolean onEntityHit(World world, Entity target, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse,
								  SpellModifiers modifiers){

		if(target instanceof EntityLiving && caster instanceof EntityPlayer && !isPossessing((EntityPlayer)caster)){

			EntityPlayer player = (EntityPlayer)caster;

			if(!player.isCreative() && player.getHealth() <= getProperty(CRITICAL_HEALTH).floatValue()){
				player.sendStatusMessage(new TextComponentTranslation(
						"spell." + this.getRegistryName() + ".insufficienthealth"), true);
				return false;
			}

			if(!world.isRemote){
				int duration = (int)(getProperty(EFFECT_DURATION).floatValue() * modifiers.get(WizardryItems.duration_upgrade));
				if(possess(player, (EntityLiving)target, duration)){
					return true;
				}
			}
		}

		return false;
	}

	@Override
	protected boolean onBlockHit(World world, BlockPos pos, EnumFacing side, Vec3d hit, EntityLivingBase caster, Vec3d origin, int ticksInUse,
								 SpellModifiers modifiers){
		return false;
	}

	@Override
	protected boolean onMiss(World world, EntityLivingBase caster, Vec3d origin, Vec3d direction, int ticksInUse, SpellModifiers modifiers){
		return false;
	}

	// ================================================ Helper methods ================================================

	/**
	 * Causes the given player to start possessing the given target for the given duration, and sets all relevant data
	 * for both entities accordingly. Also takes care of sending packets to update clients.
	 * @param possessor The player doing the possessing.
	 * @param target The entity being possessed.
	 * @param duration The number of ticks for which the possession should last. Pass in a negative integer to make the
	 * possession last indefinitely (until manually ended with the dismount key).
	 * @return True if the possession succeeded, false if for some reason it did not (only happens if the player's
	 * {@link WizardData} is null).
	 */
	public boolean possess(EntityPlayer possessor, EntityLiving target, int duration){

		if(possessor.isSneaking()) return false;

		if(WizardData.get(possessor) != null){

			WizardData.get(possessor).setVariable(POSSESSEE_KEY, target);
			WizardData.get(possessor).setVariable(TIMER_KEY, duration);

			possessor.setPositionAndRotation(target.posX, target.posY, target.posZ, target.rotationYaw, target.rotationPitch);
			possessor.eyeHeight = target.getEyeHeight();
			setSize(possessor, target.width, target.height);

			target.setDead();
			target.setNoAI(true);
			target.setAttackTarget(null);

			// Attributes

			if(target instanceof EntityFlying || target instanceof net.minecraft.entity.passive.EntityFlying){
				possessor.capabilities.allowFlying = true;
				possessor.capabilities.isFlying = true;
			}

			// Apply attribute modifiers which change the player's attribute value to the target's value
			// Uses predefined UUIDs so we can easily remove them later
			attributes:
			for(IAttribute attribute : INHERITED_ATTRIBUTES.keySet()){

				IAttributeInstance instance = target.getAttributeMap().getAttributeInstance(attribute);

				if(instance != null){

					double targetValue = instance.getAttributeValue();
					double currentValue = possessor.getAttributeMap().getAttributeInstance(attribute).getAttributeValue();
					// Don't ask me why, but the player's base movement speed seems to be 0.1
					if(attribute == SharedMonsterAttributes.MOVEMENT_SPEED) currentValue /= possessor.capabilities.getWalkSpeed();

					for(EntityEquipmentSlot slot : EntityEquipmentSlot.values()){
						if(target.getItemStackFromSlot(slot).getAttributeModifiers(slot).containsKey(attribute.getName())){
							// If the mob has equipment, use the modifiers for that equipment instead of the mob's normal ones
							// Not doing this results in the player being able to one-hit most mobs when possessing a zombie pigman!
							continue attributes;
						}
					}

					possessor.getAttributeMap().getAttributeInstance(attribute).applyModifier(new AttributeModifier(
							INHERITED_ATTRIBUTES.get(attribute), "possessionModifier", targetValue / currentValue,
							WizardryUtilities.Operations.MULTIPLY_FLAT));
				}
			}

			if(possessor.world.isRemote){
				// Shaders and effects
				Wizardry.proxy.loadShader(possessor, SHADER);
				Wizardry.proxy.playBlinkEffect(possessor);

			}else{

				// Targeting

				for(EntityLiving creature : WizardryUtilities.getEntitiesWithinRadius(16, possessor.posX,
						possessor.posY, possessor.posZ, possessor.world, EntityLiving.class)){
					// Mobs are dumb, if a player possesses something they're like "Huh?! Where'd you go?"
					// Of course, this won't last long if the player attacks them, since they'll revenge-target them
					if(creature.getAttackTarget() == possessor && !creature.canAttackClass(target.getClass()))
						creature.setAttackTarget(null);
				}

				// Inventory and items

				if(possessor.getEntityData() != null){
					NBTExtras.storeTagSafely(possessor.getEntityData(), INVENTORY_NBT_KEY, possessor.inventory.writeToNBT(new NBTTagList()));
				}

				possessor.inventory.clear();
				possessor.inventoryContainer.detectAndSendChanges();

				ItemStack stack = target.getHeldItemMainhand().copy();

				if(target instanceof EntityEnderman && ((EntityEnderman)target).getHeldBlockState() != null){
					stack = new ItemStack(((EntityEnderman)target).getHeldBlockState().getBlock());

				}else if(stack.getItem() instanceof ItemBow){
					Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
					enchantments.put(Enchantments.INFINITY, 1);
					EnchantmentHelper.setEnchantments(enchantments, stack);
					ItemStack arrow = new ItemStack(Items.ARROW);
					if(target instanceof EntityStray || target instanceof EntityStrayMinion){
						arrow = new ItemStack(Items.TIPPED_ARROW);
						PotionUtils.addPotionToItemStack(arrow, PotionTypes.SLOWNESS);
					}
					possessor.setHeldItem(EnumHand.OFF_HAND, arrow);
				}

				possessor.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, stack);

				// Packets

				WizardryPacketHandler.net.sendToAllTracking(new PacketPossession.Message(possessor, target, duration), possessor);
				if(possessor instanceof EntityPlayerMP){
					WizardryPacketHandler.net.sendTo(new PacketPossession.Message(possessor, target, duration), (EntityPlayerMP)possessor);
				}
			}

			return true;
		}

		return false;
	}

	/** Causes the given player to stop possessing their current possessee, if any, and resets all relevant data for
	 * both entities. Also takes care of sending packets to update clients. */
	public void endPossession(EntityPlayer player){

		// Reverts the possessed entity back to normal

		EntityLiving victim = getPossessee(player);

		if(victim != null){

			victim.isDead = false;
			victim.setNoAI(false);
			victim.setPosition(player.posX, player.posY, player.posZ);
			if(!player.world.isRemote) player.world.spawnEntity(victim);

			for(PotionEffect effect : player.getActivePotionEffects()){
				victim.addPotionEffect(effect);
			}
		}

		// Reverts the player back to normal

		player.clearActivePotions();

		player.eyeHeight = player.getDefaultEyeHeight(); // How convenient!

		if(WizardData.get(player) != null){
			WizardData.get(player).setVariable(TIMER_KEY, 0);
			WizardData.get(player).setVariable(POSSESSEE_KEY, null);
		}

		if(!player.capabilities.isCreativeMode){
			player.capabilities.allowFlying = false;
			player.capabilities.isFlying = false;
		}

		if(player.world.isRemote && player == net.minecraft.client.Minecraft.getMinecraft().player){
			net.minecraft.client.Minecraft.getMinecraft().entityRenderer.stopUseShader();
			Wizardry.proxy.playBlinkEffect(player);
		}

		for(IAttribute attribute : INHERITED_ATTRIBUTES.keySet()){
			player.getAttributeMap().getAttributeInstance(attribute).removeModifier(INHERITED_ATTRIBUTES.get(attribute));
		}

		if(player instanceof EntityPlayerMP){

			player.inventory.clear();

			if(player.getEntityData() != null){
				player.inventory.readFromNBT(player.getEntityData().getTagList(INVENTORY_NBT_KEY, NBT.TAG_COMPOUND));
			}

			player.inventoryContainer.detectAndSendChanges();
		}

		this.playSound(player.world, player, 0, -1, null, "end");

		if(!player.world.isRemote && player instanceof EntityPlayerMP){
			WizardryPacketHandler.net.sendToAllTracking(new PacketPossession.Message(player, null, 0), player);
			WizardryPacketHandler.net.sendTo(new PacketPossession.Message(player, null, 0), (EntityPlayerMP)player);
		}
	}

	/** Returns the {@code EntityLiving} that is currently being possessed by the given player, or null if the player is
	 * not currently possessing an entity. */
	@Nullable
	public static EntityLiving getPossessee(EntityPlayer player){
		return WizardData.get(player) == null ? null : WizardData.get(player).getVariable(POSSESSEE_KEY);
	}

	/** Returns true if the given player is currently possessing an entity, false otherwise. Just a shortcut for
	 * {@code Possession.getPossessee(player) != null}. */
	public static boolean isPossessing(EntityPlayer player){
		return getPossessee(player) != null;
	}

	private static int update(EntityPlayer player, Integer possessionTimer){

		if(possessionTimer == null) possessionTimer = 0;

		if(possessionTimer > 0){

			if(isPossessing(player) && !player.isSneaking()){

				possessionTimer--;

				if(player.world.isRemote){
					ParticleBuilder.create(Type.DARK_MAGIC, player).clr(0.1f, 0, 0.3f).spawn(player.world);
					Wizardry.proxy.loadShader(player, SHADER);
				}

			}else{
				((Possession)Spells.possession).endPossession(player);
				return 0;
			}

		}else if(isPossessing(player)){
			((Possession)Spells.possession).endPossession(player);
		}

		return possessionTimer;
	}

	/** Adds the given {@link BiConsumer} to the list of abilities. An <i>ability</i> is an entity-specific action or
	 * effect that happens when a certain type of entity is possessed. For example, spiders can climb walls, endermen
	 * can pick up blocks, creepers explode, etc. Other mods may use this method to */
	public static <T extends EntityLiving> void addAbility(Class<T> entityType, BiConsumer<T, EntityPlayer> ability){
		abilities.put(entityType, ability);
	}

	@SuppressWarnings("unchecked") // Guess what? Type erasure again!
	private static <T extends EntityLiving> void performAbilities(EntityLiving entity, EntityPlayer player){
		// Now we have a type parameter T to work with we can ram the entity into the consumer without a compiler error
		for(Class<? extends EntityLiving> entityType : abilities.keySet()){
			if(entityType.isAssignableFrom(entity.getClass())){
				abilities.get(entityType).forEach(a -> ((BiConsumer<T, EntityPlayer>)a).accept((T)entity, player));
			}
		}
	}

	/** Adds the given factory to the list of projectiles. When a player right-clicks while possessing an entity of the
	 * given type, the given projectile factory will be invoked to create a projectile, which is then aimed and spawned. */
	public static <T extends Entity & IProjectile> void addProjectile(Class<? extends EntityLiving> entityType, Function<World, T> factory){
		projectiles.put(entityType, factory);
	}

	/** Copied from Entity#setSize, with the call to move(...) removed. This is presumably also better than reflecting
	 * into Entity#setSize, which is protected. */
	private static void setSize(Entity entity, float width, float height){

		if(width != entity.width || height != entity.height){

			entity.width = width;
			entity.height = height;

			double halfWidth = (double)width / 2.0D;
			entity.setEntityBoundingBox(new AxisAlignedBB(entity.posX - halfWidth, entity.posY, entity.posZ - halfWidth, entity.posX + halfWidth, entity.posY + (double)entity.height, entity.posZ + halfWidth));
		}
	}

	// ================================================ Event Handlers ================================================
	// We got every kind of event handler goin', folks!

	@SubscribeEvent
	public static void onPlayerTickEvent(TickEvent.PlayerTickEvent event){

		if(event.phase == TickEvent.Phase.START){

			EntityLiving possessee = getPossessee(event.player);

			if(possessee != null){
				// Updating these to the player's variables won't have an effect on player movement, but it will
				// affect various bits of mob-specific logic
				possessee.setPosition(event.player.posX, event.player.posY, event.player.posZ);
				possessee.motionX = event.player.motionX;
				possessee.motionY = event.player.motionY;
				possessee.motionZ = event.player.motionZ;
				possessee.onGround = event.player.onGround;

				possessee.onUpdate(); // Event though it's not in the world, it still needs updating
				possessee.ticksExisted++; // Normally gets updated from World

				if(possessee.getHealth() <= 0){
					((Possession)Spells.possession).endPossession(event.player);
				}

				performAbilities(possessee, event.player);
			}
		}

		// Right at the end of EntityPlayer#onUpdate() it calls EntityPlayer#updateSize(), which resets the player's
		// size (and is also where this event is fired from, oddly enough) ... but not on my watch!
		if(event.phase == TickEvent.Phase.END){
			EntityLiving possessee = getPossessee(event.player);
			if(possessee != null){
				setSize(event.player, possessee.width, possessee.height);
			}
		}
	}

	// When possessing, attacks are diverted to the possessed entity for armour, immunity, resistance calculations
	// and so on, then when the damage is actually applied, the player is also damaged via onLivingDamageEvent below
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void onLivingAttackEvent(LivingAttackEvent event){

		if(event.getEntity() instanceof EntityPlayer && event.getSource() != DamageSource.OUT_OF_WORLD){

			EntityLiving possessee = getPossessee((EntityPlayer)event.getEntity());

			if(possessee != null){
				DamageSafetyChecker.attackEntitySafely(possessee, event.getSource(), event.getAmount(), event.getSource().getDamageType());
				event.setCanceled(true);
			}
		}
	}

	// LivingDamageEvent used in preference to LivingHurtEvent because the player is 'inside' the possessed entity, so
	// any damage should come through that entity (and any armour, potions, enchantments etc. it has) first.
	@SubscribeEvent
	public static void onLivingDamageEvent(LivingDamageEvent event){

		for(EntityPlayer player : event.getEntity().world.playerEntities){

			EntityLiving possessee = getPossessee(player);

			if(possessee == event.getEntity()){
				// Possessors take half of all damage taken by the entity they are possessing. If the possessor receives
				// fatal/critical damage (i.e. damage that takes them to half a heart or less), their health is reset to half
				// a heart and the possession ends.
				if(!player.capabilities.isCreativeMode){
					// TODO: Make this a proper DamageSource?
					DamageSafetyChecker.attackEntitySafely(player, DamageSource.OUT_OF_WORLD, event.getAmount() / 2,
							DamageSource.OUT_OF_WORLD.getDamageType());
				}

				if(player.getHealth() <= Spells.possession.getProperty(CRITICAL_HEALTH).floatValue()){
					player.setHealth(Spells.possession.getProperty(CRITICAL_HEALTH).floatValue());
					((Possession)Spells.possession).endPossession(player);
				}
			}
		}
	}

	// Prevents possessing players from interacting with blocks and controls projectile shooting
	@SubscribeEvent
	public static void onPlayerInteractEvent(PlayerInteractEvent event){

		if(event instanceof PlayerInteractEvent.RightClickItem) return; // Can always do this

		EntityLiving possessee = getPossessee(event.getEntityPlayer());

		if(possessee != null){

			// Let endermen interact with blocks
			if(possessee instanceof EntityEnderman && (
					(event instanceof PlayerInteractEvent.RightClickBlock && ((EntityEnderman)possessee).getHeldBlockState() != null)
							|| (event instanceof PlayerInteractEvent.LeftClickBlock && ((EntityEnderman)possessee).getHeldBlockState() == null)))
				return;

			if(WizardData.get(event.getEntityPlayer()) != null && event.getWorld().isRemote
					&& (event instanceof PlayerInteractEvent.RightClickEmpty
					|| event instanceof PlayerInteractEvent.EntityInteract
					|| event instanceof PlayerInteractEvent.RightClickBlock)){

				Integer cooldown = WizardData.get(event.getEntityPlayer()).getVariable(SHOOT_COOLDOWN_KEY);

				if(cooldown == null || cooldown == 0){

					WizardryPacketHandler.net.sendToServer(new PacketControlInput.Message(PacketControlInput.ControlType.POSSESSION_PROJECTILE));
					WizardData.get(event.getEntityPlayer()).setVariable(SHOOT_COOLDOWN_KEY, PROJECTILE_COOLDOWN);

					if(possessee instanceof EntityLightningWraith){
						Spells.arc.cast(event.getWorld(), event.getEntityPlayer(), EnumHand.MAIN_HAND, 0, new SpellModifiers());
					}

					if(possessee instanceof EntityCreeper){
						((EntityCreeper)possessee).ignite();
					}
				}
			}

			if(event.isCancelable()) event.setCanceled(true);
		}
	}

	/** Called via packets to shoot a projectile if the entity currently possessed by the given player can do so. */
	public static void shootProjectile(EntityPlayer possessor){

		if(WizardData.get(possessor) != null){

			Integer cooldown = WizardData.get(possessor).getVariable(SHOOT_COOLDOWN_KEY);

			if(cooldown == null || cooldown == 0){

				EntityLiving possessee = getPossessee(possessor);

				if(possessee != null){

					if(possessee instanceof EntityLightningWraith){
						Spells.arc.cast(possessor.world, possessor, EnumHand.MAIN_HAND, 0, new SpellModifiers());
					}

					if(possessee instanceof EntityCreeper){
						((Possession)Spells.possession).endPossession(possessor);
						((EntityCreeper)possessee).ignite(); // Aaaaaaand.... RUN!
					}

					Function<World, ? extends IProjectile> factory = projectiles.get(possessee.getClass());

					if(factory != null){

						IProjectile projectile = factory.apply(possessor.world);
						Vec3d look = possessor.getLookVec();
						((Entity)projectile).setPosition(possessor.posX + look.x, possessor.posY + possessor.getEyeHeight() + look.y, possessor.posZ + look.z);
						projectile.shoot(look.x, look.y, look.z, 1.6f, WizardryUtilities.getDefaultAimingError(possessor.world.getDifficulty()));

						if(projectile instanceof EntityMagicProjectile) ((EntityMagicProjectile)projectile).setCaster(possessor);
						else if(projectile instanceof EntityMagicArrow) ((EntityMagicArrow)projectile).setCaster(possessor);

						possessor.world.spawnEntity((Entity)projectile);

					}

					WizardData.get(possessor).setVariable(SHOOT_COOLDOWN_KEY, PROJECTILE_COOLDOWN);
				}
			}
		}
	}

	@SubscribeEvent
	public static void onLivingSetAttackTargetEvent(LivingSetAttackTargetEvent event){ // Not fired for revenge-targeting
		if(event.getTarget() instanceof EntityPlayer && event.getEntityLiving() instanceof EntityLiving){
			EntityLiving possessee = getPossessee((EntityPlayer)event.getTarget());
			EntityLiving attacker = (EntityLiving)event.getEntityLiving();
			if(possessee != null && !attacker.canAttackClass(possessee.getClass())){
				((EntityLiving)event.getEntityLiving()).setAttackTarget(null); // Mobs can't target a player possessing an entity they don't normally attack
			}
		}
	}

	// With these two methods I'm pretty sure it's watertight

	@SubscribeEvent
	public static void onLivingDeathEvent(LivingDeathEvent event){
		if(event.getEntity() instanceof EntityPlayer && isPossessing((EntityPlayer)event.getEntity())){
			((Possession)Spells.possession).endPossession((EntityPlayer)event.getEntity()); // Just in case, to make sure the player drops their items
		}
	}

	@SubscribeEvent
	public static void onPlayerLoggedOutEvent(PlayerEvent.PlayerLoggedOutEvent event){
		if(isPossessing(event.player)) ((Possession)Spells.possession).endPossession(event.player);
	}

	@SubscribeEvent
	public static void onBlockBreakEvent(BlockEvent.BreakEvent event){

		EntityLiving possessee = getPossessee(event.getPlayer());

		if(possessee instanceof EntityEnderman){
			if(((EntityEnderman)possessee).getHeldBlockState() == null){
				((EntityEnderman)possessee).setHeldBlockState(event.getState());
				event.getPlayer().setHeldItem(EnumHand.MAIN_HAND, new ItemStack(event.getState().getBlock()));
				event.setExpToDrop(0);
				event.getWorld().setBlockToAir(event.getPos()); // Remove block before it can drop
			}else{
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public static void onBlockPlaceEvent(BlockEvent.PlaceEvent event){

		EntityLiving possessee = getPossessee(event.getPlayer());

		if(possessee instanceof EntityEnderman){
			if(((EntityEnderman)possessee).getHeldBlockState() == event.getState()){
				((EntityEnderman)possessee).setHeldBlockState(null);
			}else{
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent
	public static void onEntityItemPickupEvent(EntityItemPickupEvent event){ // Why are there two item pickup events?

		EntityLiving possessee = getPossessee(event.getEntityPlayer());

		if(possessee != null){

			event.setCanceled(true);

//			if(possessee.canPickUpLoot() && possessee.getHeldItemMainhand().isEmpty()){
//				possessee.setHeldItem(EnumHand.MAIN_HAND, event.getItem().getItem());
//			}else{
//				event.setCanceled(true);
//			}
		}
	}

	@SubscribeEvent
	static void onItemTossEvent(ItemTossEvent event){
		if(isPossessing(event.getPlayer())){ // Can't drop items while possessing
			event.setCanceled(true);
			event.getPlayer().inventory.addItemStackToInventory(event.getEntityItem().getItem());
		}
	}

	@SubscribeEvent
	public static void onAttackEntityEvent(AttackEntityEvent event){

		EntityLiving possessee = getPossessee(event.getEntityPlayer());

		if(possessee == null) return;

		if(possessee instanceof EntityCreeper){
			event.setCanceled(true); // Why do creepers have a melee AI?!
		}else if(possessee.tasks.taskEntries.stream().noneMatch(t -> t.action instanceof EntityAIAttackMelee)){
			event.setCanceled(true); // Can't melee with a non-melee mob
		}
	}

}
