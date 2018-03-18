package electroblob.wizardry.client;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import org.lwjgl.input.Keyboard;

import electroblob.wizardry.CommonProxy;
import electroblob.wizardry.SpellGlyphData;
import electroblob.wizardry.WizardData;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.client.model.ModelWizardArmour;
import electroblob.wizardry.client.particle.ParticleBlizzard;
import electroblob.wizardry.client.particle.ParticleDarkMagic;
import electroblob.wizardry.client.particle.ParticleDust;
import electroblob.wizardry.client.particle.ParticleGiantBubble;
import electroblob.wizardry.client.particle.ParticleIce;
import electroblob.wizardry.client.particle.ParticleLeaf;
import electroblob.wizardry.client.particle.ParticleMagicFlame;
import electroblob.wizardry.client.particle.ParticlePath;
import electroblob.wizardry.client.particle.ParticleRotatingSparkle;
import electroblob.wizardry.client.particle.ParticleSnow;
import electroblob.wizardry.client.particle.ParticleSpark;
import electroblob.wizardry.client.particle.ParticleSparkle;
import electroblob.wizardry.client.particle.ParticleTornado;
import electroblob.wizardry.client.renderer.LayerStone;
import electroblob.wizardry.client.renderer.RenderArc;
import electroblob.wizardry.client.renderer.RenderArcaneWorkbench;
import electroblob.wizardry.client.renderer.RenderBlackHole;
import electroblob.wizardry.client.renderer.RenderBlank;
import electroblob.wizardry.client.renderer.RenderBubble;
import electroblob.wizardry.client.renderer.RenderDecay;
import electroblob.wizardry.client.renderer.RenderDecoy;
import electroblob.wizardry.client.renderer.RenderEvilWizard;
import electroblob.wizardry.client.renderer.RenderFireRing;
import electroblob.wizardry.client.renderer.RenderForceArrow;
import electroblob.wizardry.client.renderer.RenderHammer;
import electroblob.wizardry.client.renderer.RenderIceGiant;
import electroblob.wizardry.client.renderer.RenderIceSpike;
import electroblob.wizardry.client.renderer.RenderLightningDisc;
import electroblob.wizardry.client.renderer.RenderLightningPulse;
import electroblob.wizardry.client.renderer.RenderMagicArrow;
import electroblob.wizardry.client.renderer.RenderMagicLight;
import electroblob.wizardry.client.renderer.RenderPhoenix;
import electroblob.wizardry.client.renderer.RenderProjectile;
import electroblob.wizardry.client.renderer.RenderSigil;
import electroblob.wizardry.client.renderer.RenderSpiritHorse;
import electroblob.wizardry.client.renderer.RenderSpiritWolf;
import electroblob.wizardry.client.renderer.RenderStatue;
import electroblob.wizardry.client.renderer.RenderWizard;
import electroblob.wizardry.entity.EntityArc;
import electroblob.wizardry.entity.EntityShield;
import electroblob.wizardry.entity.construct.EntityArrowRain;
import electroblob.wizardry.entity.construct.EntityBlackHole;
import electroblob.wizardry.entity.construct.EntityBlizzard;
import electroblob.wizardry.entity.construct.EntityBubble;
import electroblob.wizardry.entity.construct.EntityDecay;
import electroblob.wizardry.entity.construct.EntityEarthquake;
import electroblob.wizardry.entity.construct.EntityFireRing;
import electroblob.wizardry.entity.construct.EntityFireSigil;
import electroblob.wizardry.entity.construct.EntityForcefield;
import electroblob.wizardry.entity.construct.EntityFrostSigil;
import electroblob.wizardry.entity.construct.EntityHailstorm;
import electroblob.wizardry.entity.construct.EntityHammer;
import electroblob.wizardry.entity.construct.EntityHealAura;
import electroblob.wizardry.entity.construct.EntityIceSpike;
import electroblob.wizardry.entity.construct.EntityLightningPulse;
import electroblob.wizardry.entity.construct.EntityLightningSigil;
import electroblob.wizardry.entity.construct.EntityTornado;
import electroblob.wizardry.entity.living.EntityDecoy;
import electroblob.wizardry.entity.living.EntityEvilWizard;
import electroblob.wizardry.entity.living.EntityIceGiant;
import electroblob.wizardry.entity.living.EntityIceWraith;
import electroblob.wizardry.entity.living.EntityLightningWraith;
import electroblob.wizardry.entity.living.EntityPhoenix;
import electroblob.wizardry.entity.living.EntityShadowWraith;
import electroblob.wizardry.entity.living.EntitySpiritHorse;
import electroblob.wizardry.entity.living.EntitySpiritWolf;
import electroblob.wizardry.entity.living.EntityStormElemental;
import electroblob.wizardry.entity.living.EntityWizard;
import electroblob.wizardry.entity.living.ISpellCaster;
import electroblob.wizardry.entity.living.ISummonedCreature;
import electroblob.wizardry.entity.projectile.EntityDarknessOrb;
import electroblob.wizardry.entity.projectile.EntityDart;
import electroblob.wizardry.entity.projectile.EntityFirebolt;
import electroblob.wizardry.entity.projectile.EntityFirebomb;
import electroblob.wizardry.entity.projectile.EntityForceArrow;
import electroblob.wizardry.entity.projectile.EntityForceOrb;
import electroblob.wizardry.entity.projectile.EntityIceCharge;
import electroblob.wizardry.entity.projectile.EntityIceLance;
import electroblob.wizardry.entity.projectile.EntityIceShard;
import electroblob.wizardry.entity.projectile.EntityLightningArrow;
import electroblob.wizardry.entity.projectile.EntityLightningDisc;
import electroblob.wizardry.entity.projectile.EntityMagicMissile;
import electroblob.wizardry.entity.projectile.EntityPoisonBomb;
import electroblob.wizardry.entity.projectile.EntitySmokeBomb;
import electroblob.wizardry.entity.projectile.EntitySpark;
import electroblob.wizardry.entity.projectile.EntitySparkBomb;
import electroblob.wizardry.entity.projectile.EntityThunderbolt;
import electroblob.wizardry.event.SpellCastEvent;
import electroblob.wizardry.event.SpellCastEvent.Source;
import electroblob.wizardry.item.ItemScroll;
import electroblob.wizardry.item.ItemSpellBook;
import electroblob.wizardry.item.ItemWand;
import electroblob.wizardry.packet.PacketCastContinuousSpell;
import electroblob.wizardry.packet.PacketCastSpell;
import electroblob.wizardry.packet.PacketNPCCastSpell;
import electroblob.wizardry.packet.PacketPlayerSync.Message;
import electroblob.wizardry.packet.PacketTransportation;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.spell.Clairvoyance;
import electroblob.wizardry.spell.None;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.tileentity.TileEntityArcaneWorkbench;
import electroblob.wizardry.tileentity.TileEntityMagicLight;
import electroblob.wizardry.tileentity.TileEntityStatue;
import electroblob.wizardry.util.WandHelper;
import electroblob.wizardry.util.WizardryParticleType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderBlaze;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.config.GuiConfigEntries.NumberSliderEntry;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

/**
 * The client proxy for wizardry.
 * 
 * @author Electroblob
 * @since Wizardry 1.0
 */
public class ClientProxy extends CommonProxy {

	/** Static instance of the mixed font renderer */
	public static MixedFontRenderer mixedFontRenderer;

	// Key Bindings
	public static final KeyBinding NEXT_SPELL = new KeyBinding("key." + Wizardry.MODID + ".next_spell", Keyboard.KEY_N, "key.categories." + Wizardry.MODID);
	public static final KeyBinding PREVIOUS_SPELL = new KeyBinding("key." + Wizardry.MODID + ".previous_spell", Keyboard.KEY_B, "key.categories." + Wizardry.MODID);

	// Armour Model
	public static final ModelBiped WIZARD_ARMOUR_MODEL = new ModelWizardArmour(0.75f);

	// SECTION Registry
	// ===============================================================================================================

	@Override
	public ModelBiped getWizardArmourModel(){
		return WIZARD_ARMOUR_MODEL;
	}

	@Override
	public void registerKeyBindings(){
		ClientRegistry.registerKeyBinding(NEXT_SPELL);
		ClientRegistry.registerKeyBinding(PREVIOUS_SPELL);
	}

	@Override
	public void registerSpellHUD(){
		MinecraftForge.EVENT_BUS.register(new GuiSpellDisplay(Minecraft.getMinecraft()));
	}

	@Override
	public void initGuiBits(){
		mixedFontRenderer = new MixedFontRenderer(Minecraft.getMinecraft().gameSettings, new ResourceLocation("textures/font/ascii.png"),
				Minecraft.getMinecraft().renderEngine, false);
		GuiWizardHandbook.initDisplayRecipes();
	}

	// SECTION Misc
	// ===============================================================================================================

	@Override
	public void setToNumberSliderEntry(Property property){
		property.setConfigEntryClass(NumberSliderEntry.class);
	}

	@Override
	public World getTheWorld(){
		return Minecraft.getMinecraft().world;
	}

	@Override
	public void playMovingSound(Entity entity, SoundEvent sound, float volume, float pitch, boolean repeat){
		Minecraft.getMinecraft().getSoundHandler().playSound(new MovingSoundEntity(entity, sound, volume, pitch, repeat));
	}

	// SECTION Items
	// ===============================================================================================================

	@Override
	public FontRenderer getFontRenderer(ItemStack stack){

		Spell spell = Spells.none;

		if(stack.getItem() instanceof ItemWand){
			spell = WandHelper.getCurrentSpell(stack);
		}else if(stack.getItem() instanceof ItemSpellBook || stack.getItem() instanceof ItemScroll){
			spell = Spell.get(stack.getItemDamage());
		}

		if(Minecraft.getMinecraft().player != null && Wizardry.settings.discoveryMode && WizardData.get(Minecraft.getMinecraft().player) != null
				&& !Minecraft.getMinecraft().player.capabilities.isCreativeMode
				&& !WizardData.get(Minecraft.getMinecraft().player).hasSpellBeenDiscovered(spell)){
			return mixedFontRenderer;
		}

		return null;
	}

	@Override
	public String getScrollDisplayName(ItemStack scroll){

		// Displays [Empty slot] if spell is continuous.
		Spell spell = Spell.get(scroll.getItemDamage());
		if(spell.isContinuous) spell = Spells.none;

		EntityPlayer player = Minecraft.getMinecraft().player;

		boolean discovered = true;
		// It seems that this method is called when the world is loading, before thePlayer has been initialised.
		// If the player is null, the spell is assumed to be discovered.
		if(player != null && Wizardry.settings.discoveryMode && !player.capabilities.isCreativeMode && WizardData.get(player) != null
				&& !WizardData.get(player).hasSpellBeenDiscovered(spell)){
			discovered = false;
		}

		if(discovered){
			return I18n.format("item." + Wizardry.MODID + ":scroll.name", spell.getDisplayName()).trim();
		}else{
			return I18n.format("item." + Wizardry.MODID + ":scroll.undiscovered.name", "#" + SpellGlyphData.getGlyphName(spell, player.world) + "#").trim();
		}
	}

	@Override
	public double getConjuredBowDurability(ItemStack stack){
		EntityPlayer player = Minecraft.getMinecraft().player;
		if(player.getActiveItemStack() == stack){
			return (double)(stack.getItemDamage() + (player.getItemInUseMaxCount())) / (double)stack.getMaxDamage();
		}
		return super.getConjuredBowDurability(stack);
	}

	// SECTION Particles
	// ===============================================================================================================

	@Override
	public void spawnParticle(WizardryParticleType type, World world, double x, double y, double z, double velX, double velY, double velZ, int maxAge,
			float r, float g, float b, boolean doGravity, double radius){

		// Colour values are now automatically clamped to between 0 and 1, as values outside this range seem to
		// cause strange effects in 1.10 (or more specifically, particles that are bright pink!)
		// TODO: This is a terrible dirty fix, but it'll do for now. Find a nicer way in future.
		if(type != WizardryParticleType.MAGIC_FIRE) r = MathHelper.clamp(r, 0, 1);
		g = MathHelper.clamp(g, 0, 1);
		b = MathHelper.clamp(b, 0, 1);

		switch(type){

		case BLIZZARD:
			Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleBlizzard(world, maxAge, x, z, radius, y));
			break;
		case BRIGHT_DUST:
			Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleDust(world, x, y, z, velX, velY, velZ, r, g, b, false));
			break;
		case DARK_MAGIC:
			Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleDarkMagic(world, x, y, z, velX, velY, velZ, r, g, b));
			break;
		case DUST:
			Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleDust(world, x, y, z, velX, velY, velZ, r, g, b, true));
			break;
		case ICE:
			Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleIce(world, x, y, z, velX, velY, velZ, maxAge));
			break;
		case LEAF:
			Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleLeaf(world, x, y, z, velX, velY, velZ, maxAge));
			break;
		case MAGIC_BUBBLE:
			Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleGiantBubble(world, x, y, z, velX, velY, velZ));
			break;
		case MAGIC_FIRE:
			Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleMagicFlame(world, x, y, z, velX, velY, velZ, maxAge, r == 0 ? 1 + world.rand.nextFloat() : r));
			break;
		case PATH:
			Minecraft.getMinecraft().effectRenderer.addEffect(new ParticlePath(world, x, y, z, velX, velY, velZ, r, g, b, maxAge));
			break;
		case SNOW:
			Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleSnow(world, x, y, z, velX, velY, velZ));
			break;
		case SPARK:
			Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleSpark(world, x, y, z, velX, velY, velZ));
			break;
		case SPARKLE:
			Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleSparkle(world, x, y, z, velX, velY, velZ, r, g, b, maxAge, doGravity));
			break;
		case SPARKLE_ROTATING:
			Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleRotatingSparkle(world, maxAge, x, z, radius, y, r, g, b));
			break;
		default:
			break;
		}
	}

	@Override
	public void spawnTornadoParticle(World world, double x, double y, double z, double velX, double velZ, double radius, int maxAge,
			IBlockState block, BlockPos pos){
		Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleTornado(world, maxAge, x, z, radius, y, velX, velZ, block).setBlockPos(pos));// ,
																																					// world.rand.nextInt(6)));
	}

	// SECTION Packet Handlers
	// ===============================================================================================================

	@Override
	public void handleCastSpellPacket(PacketCastSpell.Message message){

		World world = Minecraft.getMinecraft().world;
		Entity caster = world.getEntityByID(message.casterID);
		Spell spell = Spell.get(message.spellID);
		// Should always be true
		if(caster instanceof EntityPlayer){

			((EntityPlayer)caster).setActiveHand(message.hand);

			// Duration isn't needed because it only ever affects things server-side, and anything that is
			// seen client-side gets synced elsewhere.
			spell.cast(world, (EntityPlayer)caster, message.hand, 0, message.modifiers);

			Source source = Source.OTHER;

			Item item = ((EntityPlayer)caster).getHeldItem(message.hand).getItem();

			if(item instanceof ItemWand){
				source = Source.WAND;
			}else if(item instanceof ItemScroll){
				source = Source.SCROLL;
			}

			// No need to check if the spell succeeded, because the packet is only ever sent when it succeeds.
			// The handler for this event now deals with discovery.
			MinecraftForge.EVENT_BUS.post(new SpellCastEvent.Post((EntityPlayer)caster, spell, message.modifiers, source));

		}else{
			Wizardry.logger.warn("Recieved a PacketCastSpell, but the caster ID was not the ID of a player");
		}
	}

	@Override
	public void handleCastContinuousSpellPacket(PacketCastContinuousSpell.Message message){

		World world = Minecraft.getMinecraft().world;
		Entity caster = world.getEntityByID(message.casterID);
		Spell spell = Spell.get(message.spellID);
		// Should always be true
		if(caster instanceof EntityPlayer){

			WizardData data = WizardData.get((EntityPlayer)caster);

			if(data != null){
				if(data.isCasting()){
					WizardData.get((EntityPlayer)caster).stopCastingContinuousSpell();
				}else{
					WizardData.get((EntityPlayer)caster).startCastingContinuousSpell(spell, message.modifiers);
				}
			}
		}else{
			Wizardry.logger.warn("Recieved a PacketCastContinuousSpell, but the caster ID was not the ID of a player");
		}
	}

	@Override
	public void handleNPCCastSpellPacket(PacketNPCCastSpell.Message message){

		World world = Minecraft.getMinecraft().world;
		Entity caster = world.getEntityByID(message.casterID);
		Entity target = message.targetID == -1 ? null : world.getEntityByID(message.targetID);
		Spell spell = Spell.get(message.spellID);
		// Should always be true
		if(caster instanceof EntityLiving){

			if(target instanceof EntityLivingBase){

				spell.cast(world, (EntityLiving)caster, message.hand, 0, (EntityLivingBase)target, message.modifiers);
				// Again, no need to check if the spell succeeded, because the packet is only ever sent when it
				// succeeds.
				MinecraftForge.EVENT_BUS.post(new SpellCastEvent.Post((EntityLiving)caster, spell, message.modifiers, Source.NPC));
			}

			if(caster instanceof ISpellCaster){
				if(spell.isContinuous || spell instanceof None){
					((ISpellCaster)caster).setContinuousSpell(spell);
					((EntityLiving)caster).setAttackTarget((EntityLivingBase)target);
				}
			}
		}else{
			Wizardry.logger.warn("Recieved a PacketNPCCastSpell, but the caster ID was not the ID of an EntityLiving");
		}
	}

	@Override
	public void handleTransportationPacket(PacketTransportation.Message message){

		World world = Minecraft.getMinecraft().world;
		Entity caster = world.getEntityByID(message.casterID);
		// Moved from when the packet is sent to when it is received; fixes the sound not playing in first person.
		caster.playSound(SoundEvents.BLOCK_PORTAL_TRAVEL, 1, 1);

		for(int i = 0; i < 20; i++){
			double radius = 1;
			double angle = world.rand.nextDouble() * Math.PI * 2;
			double x = caster.posX + radius * Math.cos(angle);
			double y = caster.getEntityBoundingBox().minY + world.rand.nextDouble() * 2;
			double z = caster.posZ + radius * Math.sin(angle);
			Minecraft.getMinecraft().effectRenderer
					.addEffect(new ParticleSparkle(world, x, y, z, 0, 0.02, 0, 0.6f, 1.0f, 0.6f, 80 + world.rand.nextInt(10)));
		}
		for(int i = 0; i < 20; i++){
			double radius = 1;
			double angle = world.rand.nextDouble() * Math.PI * 2;
			double x = caster.posX + radius * Math.cos(angle);
			double y = caster.getEntityBoundingBox().minY + world.rand.nextDouble() * 2;
			double z = caster.posZ + radius * Math.sin(angle);
			world.spawnParticle(EnumParticleTypes.VILLAGER_HAPPY, x, y, z, 0, 0.02, 0);
		}
		for(int i = 0; i < 20; i++){
			double radius = 1;
			double angle = world.rand.nextDouble() * Math.PI * 2;
			double x = caster.posX + radius * Math.cos(angle);
			double y = caster.getEntityBoundingBox().minY + world.rand.nextDouble() * 2;
			double z = caster.posZ + radius * Math.sin(angle);
			world.spawnParticle(EnumParticleTypes.ENCHANTMENT_TABLE, x, y, z, 0, 0.02, 0);
		}
	}

	@Override
	public void handlePlayerSyncPacket(Message message){

		WizardData properties = WizardData.get(Minecraft.getMinecraft().player);

		if(properties != null){

			properties.spellsDiscovered = message.spellsDiscovered;

			if(message.selectedMinionID == -1){
				properties.selectedMinion = null;
			}else{
				Entity entity = Minecraft.getMinecraft().world.getEntityByID(message.selectedMinionID);

				if(entity instanceof ISummonedCreature){
					properties.selectedMinion = new WeakReference<ISummonedCreature>((ISummonedCreature)entity);
				}else{
					properties.selectedMinion = null;
				}
			}
		}
	}

	@Override
	public void handleGlyphDataPacket(electroblob.wizardry.packet.PacketGlyphData.Message message){

		SpellGlyphData data = SpellGlyphData.get(Minecraft.getMinecraft().world);

		data.randomNames = new HashMap<Spell, String>();
		data.randomDescriptions = new HashMap<Spell, String>();

		for(Spell spell : Spell.getSpells(Spell.allSpells)){
			// -1 because the none spell isn't included
			data.randomNames.put(spell, message.names.get(spell.id() - 1));
			data.randomDescriptions.put(spell, message.descriptions.get(spell.id() - 1));
		}
	}

	@Override
	public void handleClairvoyancePacket(electroblob.wizardry.packet.PacketClairvoyance.Message message){
		Clairvoyance.spawnPathPaticles(Minecraft.getMinecraft().world, message.path, message.durationMultiplier);
	}

	// SECTION Rendering
	// ===============================================================================================================

	private static final ResourceLocation ICE_WRAITH_TEXTURE = new ResourceLocation(Wizardry.MODID, "textures/entity/ice_wraith.png");
	private static final ResourceLocation LIGHTNING_WRAITH_TEXTURE = new ResourceLocation(Wizardry.MODID, "textures/entity/lightning_wraith.png");

	/** Static instance of the statue renderer, used to access the block breaking texture. */
	public static RenderStatue renderStatue;

	@Override
	public void initialiseLayers(){
		LayerStone.initialiseLayers();
	}

	@Override
	public void registerRenderers(){

		// Minions
		// Yet another advantage to the new system: turns out you don't even need to register the renderer if you
		// just want the vanilla one for the mob you're extending.

		// An anonymous class in a lambda expression! No point writing a separate class really, is there?
		RenderingRegistry.registerEntityRenderingHandler(EntityLightningWraith.class, manager -> new RenderBlaze(manager){
			@Override
			protected ResourceLocation getEntityTexture(EntityBlaze entity){
				return LIGHTNING_WRAITH_TEXTURE;
			}
		});

		RenderingRegistry.registerEntityRenderingHandler(EntityIceWraith.class, manager -> new RenderBlaze(manager){
			@Override
			protected ResourceLocation getEntityTexture(EntityBlaze entity){
				return ICE_WRAITH_TEXTURE;
			}
		});

		RenderingRegistry.registerEntityRenderingHandler(EntityIceGiant.class, RenderIceGiant::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityPhoenix.class, RenderPhoenix::new);

		// Projectiles
		RenderingRegistry.registerEntityRenderingHandler(EntityMagicMissile.class, manager -> new RenderMagicArrow(manager,
				new ResourceLocation(Wizardry.MODID, "textures/entity/magic_missile.png"), false, 8.0, 4.0, 16, 9, false));
		RenderingRegistry.registerEntityRenderingHandler(EntityIceShard.class, manager -> new RenderMagicArrow(manager,
				new ResourceLocation(Wizardry.MODID, "textures/entity/ice_shard.png"), false, 8.0, 2.0, 16, 5, false));
		RenderingRegistry.registerEntityRenderingHandler(EntityLightningArrow.class, manager -> new RenderMagicArrow(manager,
				new ResourceLocation(Wizardry.MODID, "textures/entity/lightning_arrow.png"), true, 8.0, 2.0, 16, 5, false));
		RenderingRegistry.registerEntityRenderingHandler(EntityDart.class, manager -> new RenderMagicArrow(manager,
				new ResourceLocation(Wizardry.MODID, "textures/entity/dart.png"), false, 8.0, 2.0, 16, 5, true));
		RenderingRegistry.registerEntityRenderingHandler(EntityIceLance.class, manager -> new RenderMagicArrow(manager,
				new ResourceLocation(Wizardry.MODID, "textures/entity/ice_lance.png"), false, 16.0, 3.0, 22, 5, true));

		RenderingRegistry.registerEntityRenderingHandler(EntityForceArrow.class, RenderForceArrow::new);

		// Creatures
		RenderingRegistry.registerEntityRenderingHandler(EntitySpiritWolf.class, manager -> new RenderSpiritWolf(manager));
		RenderingRegistry.registerEntityRenderingHandler(EntitySpiritHorse.class, manager -> new RenderSpiritHorse(manager));
		RenderingRegistry.registerEntityRenderingHandler(EntityWizard.class, RenderWizard::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityEvilWizard.class, RenderEvilWizard::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityDecoy.class, RenderDecoy::new);

		// Throwables
		RenderingRegistry.registerEntityRenderingHandler(EntitySparkBomb.class,
				manager -> new RenderProjectile(manager, 0.6f, new ResourceLocation(Wizardry.MODID, "textures/entity/spark_bomb.png"), false));
		RenderingRegistry.registerEntityRenderingHandler(EntityFirebomb.class,
				manager -> new RenderProjectile(manager, 0.6f, new ResourceLocation(Wizardry.MODID, "textures/items/firebomb.png"), false));
		RenderingRegistry.registerEntityRenderingHandler(EntityPoisonBomb.class,
				manager -> new RenderProjectile(manager, 0.6f, new ResourceLocation(Wizardry.MODID, "textures/items/poison_bomb.png"), false));
		RenderingRegistry.registerEntityRenderingHandler(EntityIceCharge.class,
				manager -> new RenderProjectile(manager, 0.6f, new ResourceLocation(Wizardry.MODID, "textures/entity/ice_charge.png"), false));
		RenderingRegistry.registerEntityRenderingHandler(EntityForceOrb.class,
				manager -> new RenderProjectile(manager, 0.7f, new ResourceLocation(Wizardry.MODID, "textures/entity/force_orb.png"), true));
		RenderingRegistry.registerEntityRenderingHandler(EntitySpark.class,
				manager -> new RenderProjectile(manager, 0.4f, new ResourceLocation(Wizardry.MODID, "textures/entity/spark.png"), true));
		RenderingRegistry.registerEntityRenderingHandler(EntityDarknessOrb.class,
				manager -> new RenderProjectile(manager, 0.6f, new ResourceLocation(Wizardry.MODID, "textures/entity/darkness_orb.png"), true));
		RenderingRegistry.registerEntityRenderingHandler(EntityFirebolt.class,
				manager -> new RenderProjectile(manager, 0.2f, new ResourceLocation(Wizardry.MODID, "textures/entity/firebolt.png"), false));
		RenderingRegistry.registerEntityRenderingHandler(EntityLightningDisc.class,
				manager -> new RenderLightningDisc(manager, new ResourceLocation(Wizardry.MODID, "textures/entity/lightning_sigil.png"), 2.0f));
		RenderingRegistry.registerEntityRenderingHandler(EntitySmokeBomb.class,
				manager -> new RenderProjectile(manager, 0.6f, new ResourceLocation(Wizardry.MODID, "textures/items/smoke_bomb.png"), false));

		// Effects and constructs
		RenderingRegistry.registerEntityRenderingHandler(EntityArc.class, RenderArc::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityBlackHole.class, RenderBlackHole::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityShield.class, RenderBlank::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityBubble.class, RenderBubble::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityHammer.class, RenderHammer::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityIceSpike.class, RenderIceSpike::new);

		// Stuff that doesn't render
		RenderingRegistry.registerEntityRenderingHandler(EntityBlizzard.class, RenderBlank::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityTornado.class, RenderBlank::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityArrowRain.class, RenderBlank::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityShadowWraith.class, RenderBlank::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityForcefield.class, RenderBlank::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityThunderbolt.class, RenderBlank::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityStormElemental.class, RenderBlank::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityEarthquake.class, RenderBlank::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityHailstorm.class, RenderBlank::new);

		// Runes on ground
		RenderingRegistry.registerEntityRenderingHandler(EntityHealAura.class,
				manager -> new RenderSigil(manager, new ResourceLocation(Wizardry.MODID, "textures/entity/healing_aura.png"), 5.0f, false));
		RenderingRegistry.registerEntityRenderingHandler(EntityFireSigil.class,
				manager -> new RenderSigil(manager, new ResourceLocation(Wizardry.MODID, "textures/entity/fire_sigil.png"), 2.0f, true));
		RenderingRegistry.registerEntityRenderingHandler(EntityFrostSigil.class,
				manager -> new RenderSigil(manager, new ResourceLocation(Wizardry.MODID, "textures/entity/frost_sigil.png"), 2.0f, true));
		RenderingRegistry.registerEntityRenderingHandler(EntityLightningSigil.class,
				manager -> new RenderSigil(manager, new ResourceLocation(Wizardry.MODID, "textures/entity/lightning_sigil.png"), 2.0f, true));
		RenderingRegistry.registerEntityRenderingHandler(EntityFireRing.class,
				manager -> new RenderFireRing(manager, new ResourceLocation(Wizardry.MODID, "textures/entity/ring_of_fire.png"), 5.0f));
		RenderingRegistry.registerEntityRenderingHandler(EntityDecay.class, RenderDecay::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityLightningPulse.class, manager -> new RenderLightningPulse(manager, 8.0f));

		// TESRs
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityArcaneWorkbench.class, new RenderArcaneWorkbench());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityStatue.class, renderStatue = new RenderStatue());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMagicLight.class, new RenderMagicLight());

	}
}