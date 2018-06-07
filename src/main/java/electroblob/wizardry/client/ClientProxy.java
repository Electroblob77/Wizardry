package electroblob.wizardry.client;

import java.lang.ref.WeakReference;
import java.util.HashMap;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.config.GuiConfigEntries.NumberSliderEntry;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import electroblob.wizardry.CommonProxy;
import electroblob.wizardry.EnumParticleType;
import electroblob.wizardry.ExtendedPlayer;
import electroblob.wizardry.SpellGlyphData;
import electroblob.wizardry.WandHelper;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.WizardryRegistry;
import electroblob.wizardry.WizardryUtilities;
import electroblob.wizardry.client.model.ModelSpiritHorse;
import electroblob.wizardry.client.model.ModelSpiritWolf;
import electroblob.wizardry.client.model.ModelWizardArmour;
import electroblob.wizardry.client.particle.EntityBlizzardFX;
import electroblob.wizardry.client.particle.EntityDarkMagicFX;
import electroblob.wizardry.client.particle.EntityDustFX;
import electroblob.wizardry.client.particle.EntityIceFX;
import electroblob.wizardry.client.particle.EntityLeafFX;
import electroblob.wizardry.client.particle.EntityMagicBubbleFX;
import electroblob.wizardry.client.particle.EntityMagicFireFX;
import electroblob.wizardry.client.particle.EntityPathFX;
import electroblob.wizardry.client.particle.EntitySnowFX;
import electroblob.wizardry.client.particle.EntitySparkFX;
import electroblob.wizardry.client.particle.EntitySparkleFX;
import electroblob.wizardry.client.particle.EntityTornadoFX;
import electroblob.wizardry.client.renderer.RenderArc;
import electroblob.wizardry.client.renderer.RenderArcaneWorkbench;
import electroblob.wizardry.client.renderer.RenderBlackHole;
import electroblob.wizardry.client.renderer.RenderBlank;
import electroblob.wizardry.client.renderer.RenderBubble;
import electroblob.wizardry.client.renderer.RenderDecay;
import electroblob.wizardry.client.renderer.RenderDecoy;
import electroblob.wizardry.client.renderer.RenderEvilWizard;
import electroblob.wizardry.client.renderer.RenderFallingGrass;
import electroblob.wizardry.client.renderer.RenderFireRing;
import electroblob.wizardry.client.renderer.RenderForceArrow;
import electroblob.wizardry.client.renderer.RenderHammer;
import electroblob.wizardry.client.renderer.RenderIceGiant;
import electroblob.wizardry.client.renderer.RenderIceSpike;
import electroblob.wizardry.client.renderer.RenderLightningDisc;
import electroblob.wizardry.client.renderer.RenderLightningPulse;
import electroblob.wizardry.client.renderer.RenderMagicArrow;
import electroblob.wizardry.client.renderer.RenderMagicLight;
import electroblob.wizardry.client.renderer.RenderMagicSlime;
import electroblob.wizardry.client.renderer.RenderMeteor;
import electroblob.wizardry.client.renderer.RenderPhoenix;
import electroblob.wizardry.client.renderer.RenderProjectile;
import electroblob.wizardry.client.renderer.RenderSigil;
import electroblob.wizardry.client.renderer.RenderSilverfishMinion;
import electroblob.wizardry.client.renderer.RenderSkeletonMinion;
import electroblob.wizardry.client.renderer.RenderSpiderMinion;
import electroblob.wizardry.client.renderer.RenderSpiritHorse;
import electroblob.wizardry.client.renderer.RenderSpiritWolf;
import electroblob.wizardry.client.renderer.RenderStatue;
import electroblob.wizardry.client.renderer.RenderTranslucentItem;
import electroblob.wizardry.client.renderer.RenderWizard;
import electroblob.wizardry.client.renderer.RenderWraithMinion;
import electroblob.wizardry.client.renderer.RenderZombieMinion;
import electroblob.wizardry.entity.EntityArc;
import electroblob.wizardry.entity.EntityFallingGrass;
import electroblob.wizardry.entity.EntityMagicSlime;
import electroblob.wizardry.entity.EntityMeteor;
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
import electroblob.wizardry.entity.living.EntityBlazeMinion;
import electroblob.wizardry.entity.living.EntityDecoy;
import electroblob.wizardry.entity.living.EntityEvilWizard;
import electroblob.wizardry.entity.living.EntityIceGiant;
import electroblob.wizardry.entity.living.EntityIceWraith;
import electroblob.wizardry.entity.living.EntityLightningWraith;
import electroblob.wizardry.entity.living.EntityPhoenix;
import electroblob.wizardry.entity.living.EntityShadowWraith;
import electroblob.wizardry.entity.living.EntitySilverfishMinion;
import electroblob.wizardry.entity.living.EntitySkeletonMinion;
import electroblob.wizardry.entity.living.EntitySpiderMinion;
import electroblob.wizardry.entity.living.EntitySpiritHorse;
import electroblob.wizardry.entity.living.EntitySpiritWolf;
import electroblob.wizardry.entity.living.EntityStormElemental;
import electroblob.wizardry.entity.living.EntitySummonedCreature;
import electroblob.wizardry.entity.living.EntityWizard;
import electroblob.wizardry.entity.living.EntityZombieMinion;
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
import electroblob.wizardry.item.ItemFlamingAxe;
import electroblob.wizardry.item.ItemFrostAxe;
import electroblob.wizardry.item.ItemScroll;
import electroblob.wizardry.item.ItemSpectralArmour;
import electroblob.wizardry.item.ItemSpectralBow;
import electroblob.wizardry.item.ItemSpectralPickaxe;
import electroblob.wizardry.item.ItemSpectralSword;
import electroblob.wizardry.item.ItemSpellBook;
import electroblob.wizardry.item.ItemWand;
import electroblob.wizardry.packet.PacketCastContinuousSpell;
import electroblob.wizardry.packet.PacketCastSpell;
import electroblob.wizardry.packet.PacketPlayerSync.Message;
import electroblob.wizardry.packet.PacketTransportation;
import electroblob.wizardry.spell.Clairvoyance;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.tileentity.TileEntityArcaneWorkbench;
import electroblob.wizardry.tileentity.TileEntityMagicLight;
import electroblob.wizardry.tileentity.TileEntityStatue;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelSlime;
import net.minecraft.client.particle.EntityDiggingFX;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Property;

public class ClientProxy extends CommonProxy {

	/** Static instance of the mixed font renderer */
	public static MixedFontRenderer mixedFontRenderer;
	
	// Key Bindings
	public static final KeyBinding nextSpell = new KeyBinding("key.next_spell", Keyboard.KEY_N, "key.categories.wizardry");
	public static final KeyBinding previousSpell = new KeyBinding("key.previous_spell", Keyboard.KEY_B, "key.categories.wizardry");

	// Armour Model
	public static ModelBiped wizardArmourModel = new ModelWizardArmour(0.75f);
	
	@Override
	public ModelBiped getWizardArmourModel(){
		return wizardArmourModel;
	}
	
	@Override
	public double getPlayerEyesPos(EntityPlayer player){
		return Minecraft.getMinecraft().thePlayer == player ? player.posY : player.posY + player.eyeHeight;
    }
	
	@Override
	public Vec3 getWandTipPosition(EntityLivingBase entity){
		// Behaves differently in first person
		if(Minecraft.getMinecraft().thePlayer == entity && Minecraft.getMinecraft().gameSettings.thirdPersonView == 0){
			return Vec3.createVectorHelper(entity.posX, entity.boundingBox.minY + 1.2, entity.posZ);
		}else{
			return super.getWandTipPosition(entity);
		}
	}
	
	@Override
    public void registerKeyBindings(){
    	ClientRegistry.registerKeyBinding(nextSpell);
    	ClientRegistry.registerKeyBinding(previousSpell);
	}
	
	@Override
	public void registerEventHandlers(){
		super.registerEventHandlers();
		MinecraftForge.EVENT_BUS.register(new WizardryClientEventHandler());
	}
	
	@Override
    public void registerSpellHUD(){
		MinecraftForge.EVENT_BUS.register(new GuiSpellDisplay(Minecraft.getMinecraft()));
	}
	
	@Override
	public void setToNumberSliderEntry(Property property) {
		property.setConfigEntryClass(NumberSliderEntry.class);
	}
	
	@Override
	public FontRenderer getFontRenderer(ItemStack stack){
		
		Spell spell = WizardryRegistry.none;
		
		if(stack.getItem() instanceof ItemWand){
			spell = WandHelper.getCurrentSpell(stack);
		}else if(stack.getItem() instanceof ItemSpellBook || stack.getItem() instanceof ItemScroll){
			spell = Spell.get(stack.getItemDamage());
		}
		
		if(Wizardry.discoveryMode && ExtendedPlayer.get(Minecraft.getMinecraft().thePlayer) != null && !Minecraft.getMinecraft().thePlayer.capabilities.isCreativeMode
				&& !ExtendedPlayer.get(Minecraft.getMinecraft().thePlayer).hasSpellBeenDiscovered(spell)){
			return mixedFontRenderer;
		}
		
		return null;
	}
	
	@Override
	public String getScrollDisplayName(ItemStack scroll){

		// Displays [Empty slot] if spell is continuous.
		Spell spell = Spell.get(scroll.getItemDamage());
		if(spell.isContinuous) spell = WizardryRegistry.none;
		
		EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        
        boolean discovered = true;
        // It seems that this method is called when the world is loading, before thePlayer has been initialised.
        // If the player is null, the spell is assumed to be discovered.
		if(player != null && Wizardry.discoveryMode && !player.capabilities.isCreativeMode
				&& ExtendedPlayer.get(player) != null && !ExtendedPlayer.get(player).hasSpellBeenDiscovered(spell)){
			discovered = false;
		}
		
		if(discovered){
			return I18n.format("item.scroll.name", spell.getDisplayName()).trim();
		}else{
			return I18n.format("item.scroll.undiscovered.name", "#" + SpellGlyphData.getGlyphName(spell, player.worldObj) + "#").trim();
		}
	}
	
	@Override
	public void spawnParticle(EnumParticleType type, World world, double x, double y, double z, double velX, double velY, double velZ, int maxAge, float r, float g, float b, boolean doGravity, double radius){
    
		switch(type){
		
		case BLIZZARD:
			Minecraft.getMinecraft().effectRenderer.addEffect(new EntityBlizzardFX(world, r, g, b, maxAge, x, z, radius, y));
			break;
		case BRIGHT_DUST:
			Minecraft.getMinecraft().effectRenderer.addEffect(new EntityDustFX(world, x, y, z, velX, velY, velZ, r, g, b, false));
			break;
		case DARK_MAGIC:
			Minecraft.getMinecraft().effectRenderer.addEffect(new EntityDarkMagicFX(world, x, y, z, velX, velY, velZ, r, g, b));
			break;
		case DUST:
			Minecraft.getMinecraft().effectRenderer.addEffect(new EntityDustFX(world, x, y, z, velX, velY, velZ, r, g, b, true));
			break;
		case ICE:
			Minecraft.getMinecraft().effectRenderer.addEffect(new EntityIceFX(world, x, y, z, velX, velY, velZ, maxAge));
			break;
		case LEAF:
			Minecraft.getMinecraft().effectRenderer.addEffect(new EntityLeafFX(world, x, y, z, velX, velY, velZ, maxAge));
			break;
		case MAGIC_BUBBLE:
			Minecraft.getMinecraft().effectRenderer.addEffect(new EntityMagicBubbleFX(world, x, y, z, velX, velY, velZ));
			break;
		case MAGIC_FIRE:
			Minecraft.getMinecraft().effectRenderer.addEffect(new EntityMagicFireFX(world, x, y, z, velX, velY, velZ, maxAge, r == 0 ? 1 + world.rand.nextFloat() : r));
			break;
		case PATH:
			Minecraft.getMinecraft().effectRenderer.addEffect(new EntityPathFX(world, x, y, z, velX, velY, velZ, r, g, b, maxAge));
			break;
		case SNOW:
			Minecraft.getMinecraft().effectRenderer.addEffect(new EntitySnowFX(world, x, y, z, velX, velY, velZ));
			break;
		case SPARK:
			Minecraft.getMinecraft().effectRenderer.addEffect(new EntitySparkFX(world, x, y, z, velX, velY, velZ));
			break;
		case SPARKLE:
			Minecraft.getMinecraft().effectRenderer.addEffect(new EntitySparkleFX(world, x, y, z, velX, velY, velZ, r, g, b, maxAge, doGravity));
			break;
		}
	}
	
	@Override
	public void spawnDigParticle(World world, double x, double y, double z, double velX, double velY, double velZ, Block block){
    	Minecraft.getMinecraft().effectRenderer.addEffect(new EntityDiggingFX(world, x, y, z, velX, velY, velZ, block, 0));
    }
	
	@Override
	public void spawnTornadoParticle(World world, double x, double y, double z, double velX, double velZ, double radius, int maxAge, Block block, int metadata){
		Minecraft.getMinecraft().effectRenderer.addEffect(new EntityTornadoFX(world, maxAge, x, z, radius, y, velX, velZ, block, metadata, world.rand.nextInt(6)));
    }

	@Override
	public void playMovingSound(Entity entity, String soundName, float volume, float pitch, boolean repeat){
		Minecraft.getMinecraft().getSoundHandler().playSound(new MovingSoundEntity(entity, soundName, volume, pitch, repeat));
	}
	
	@Override
	public void handleCastSpellPacket(PacketCastSpell.Message message){

		World world = Minecraft.getMinecraft().theWorld;
		Entity caster = world.getEntityByID(message.casterID);
		Entity target = world.getEntityByID(message.targetID);
		Spell spell = Spell.get(message.spellID);
		
		if(caster instanceof EntityPlayer){
			
			ItemStack stack = ((EntityPlayer)caster).getHeldItem();
			
			if(stack != null) ((EntityPlayer)caster).setItemInUse(stack, stack.getMaxItemUseDuration());
			
			// Duration isn't needed because it only ever affects things server-side, and anything that is
			// seen client-side gets synced elsewhere.
			spell.cast(world, (EntityPlayer)caster, 0, message.damageMultiplier, message.rangeMultiplier, 1, message.blastMultiplier);
		
			// Updates the spell discovery data client-side. Could be done by calling sync(), but that means sending
			// another packet unnecessarily.
			if(ExtendedPlayer.get((EntityPlayer)caster) != null){
				ExtendedPlayer.get((EntityPlayer)caster).discoverSpell(spell);
			}
			
		}
		else if(caster instanceof EntityLiving && target instanceof EntityLivingBase){
			spell.cast(world, (EntityLiving)caster, (EntityLivingBase)target, message.damageMultiplier, message.rangeMultiplier, 1, message.blastMultiplier);
		}
	}
	
	@Override
	public void handleTransportationPacket(PacketTransportation.Message message){
		
		World world = Minecraft.getMinecraft().theWorld;
		Entity caster = world.getEntityByID(message.casterID);
		
		for(int i=0; i<20; i++){
			double radius = 1;
    		double angle = world.rand.nextDouble()*Math.PI*2;
        	double x = caster.posX + radius*Math.cos(angle);
        	double y = WizardryUtilities.getEntityFeetPos(caster) + world.rand.nextDouble()*2;
        	double z = caster.posZ + radius*Math.sin(angle);
			Minecraft.getMinecraft().effectRenderer.addEffect(new EntitySparkleFX(world, x, y, z, 0, 0.02, 0, 0.6f, 1.0f, 0.6f, 80 + world.rand.nextInt(10)));
		}
		for(int i=0; i<20; i++){
			double radius = 1;
    		double angle = world.rand.nextDouble()*Math.PI*2;
        	double x = caster.posX + radius*Math.cos(angle);
        	double y = WizardryUtilities.getEntityFeetPos(caster) + world.rand.nextDouble()*2;
        	double z = caster.posZ + radius*Math.sin(angle);
        	world.spawnParticle("happyVillager", x, y, z, 0, 0.02, 0);
		}
		for(int i=0; i<20; i++){
			double radius = 1;
    		double angle = world.rand.nextDouble()*Math.PI*2;
        	double x = caster.posX + radius*Math.cos(angle);
        	double y = WizardryUtilities.getEntityFeetPos(caster) + world.rand.nextDouble()*2;
        	double z = caster.posZ + radius*Math.sin(angle);
        	world.spawnParticle("enchantmenttable", x, y, z, 0, 0.02, 0);
		}
	}
	
	@Override
	public void handlePlayerSyncPacket(Message message){
		
		ExtendedPlayer properties = ExtendedPlayer.get(Minecraft.getMinecraft().thePlayer);
		
		if(properties != null){
			
			properties.spellsDiscovered = message.spellsDiscovered;
			
			if(message.selectedMinionID == -1){
				properties.selectedMinion = null;
			}else{
				Entity entity = Minecraft.getMinecraft().theWorld.getEntityByID(message.selectedMinionID);
				
				if(entity instanceof EntitySummonedCreature){
					properties.selectedMinion = new WeakReference(entity);
				}else{
					properties.selectedMinion = null;
				}
			}
		}
	}
	
	@Override
	public void handleGlyphDataPacket(electroblob.wizardry.packet.PacketGlyphData.Message message){
		
		SpellGlyphData data = SpellGlyphData.get(Minecraft.getMinecraft().theWorld);
		
		data.randomNames = new HashMap<Spell, String>();
		data.randomDescriptions = new HashMap<Spell, String>();
		
		for(Spell spell : Spell.getSpells(Spell.allSpells)){
			// -1 because the none spell isn't included
			data.randomNames.put(spell, message.names.get(spell.id() - 1));
			data.randomDescriptions.put(spell, message.descriptions.get(spell.id() - 1));
		}
	}
	
	@Override
	public void handleCastContinuousSpellPacket(PacketCastContinuousSpell.Message message){
		
		World world = Minecraft.getMinecraft().theWorld;
		Entity caster = world.getEntityByID(message.casterID);
		Spell spell = Spell.get(message.spellID);
		// This should always be true
		if(caster instanceof EntityPlayer){
			
			ExtendedPlayer properties = ExtendedPlayer.get((EntityPlayer)caster);
			
			if(properties != null){
    			if(properties.isCasting()){
    				ExtendedPlayer.get((EntityPlayer)caster).stopCastingContinuousSpell();
    			}else{
    				ExtendedPlayer.get((EntityPlayer)caster).startCastingContinuousSpell(spell, message.damageMultiplier, message.rangeMultiplier, 1, message.blastMultiplier);
    			}
    		}
		}
	}
	
	@Override
	public void handleClairvoyancePacket(electroblob.wizardry.packet.PacketClairvoyance.Message message) {
		Clairvoyance.spawnPathPaticles(Minecraft.getMinecraft().theWorld, message.path, message.durationMultiplier);
	}
	
	@Override
	public double getWandDisplayDamage(ItemStack stack){
		ExtendedPlayer properties = ExtendedPlayer.get(net.minecraft.client.Minecraft.getMinecraft().thePlayer);
		// If the player is creative or this wand is not the wand being used, the arbitrary placeholder damage is not displayed.
		if(net.minecraft.client.Minecraft.getMinecraft().thePlayer.capabilities.isCreativeMode || net.minecraft.client.Minecraft.getMinecraft().thePlayer.getHeldItem() != stack){
			return (double)stack.getItemDamageForDisplay() / (double)stack.getMaxDamage();
		}else{
			return (double)(stack.getItemDamageForDisplay() + properties.damageToApply) / (double)stack.getMaxDamage();
		}
	}
	
	@Override
	public int getConjuredItemDisplayDamage(ItemStack stack){
		
		if(stack.getItem() instanceof ItemSpectralSword){
			ExtendedPlayer properties = ExtendedPlayer.get(Minecraft.getMinecraft().thePlayer);
			return stack.getItemDamage() + properties.conjuredSwordDuration;
			
		}else if(stack.getItem() instanceof ItemSpectralPickaxe){
			ExtendedPlayer properties = ExtendedPlayer.get(Minecraft.getMinecraft().thePlayer);
			return stack.getItemDamage() + properties.conjuredPickaxeDuration;
			
		}else if(stack.getItem() instanceof ItemSpectralBow){
			ExtendedPlayer properties = ExtendedPlayer.get(Minecraft.getMinecraft().thePlayer);
			return stack.getItemDamage() + properties.conjuredBowDuration;
			
		}else if(stack.getItem() instanceof ItemSpectralArmour){
			ExtendedPlayer properties = ExtendedPlayer.get(Minecraft.getMinecraft().thePlayer);
			return stack.getItemDamage() + properties.conjuredArmourDuration;
			
		}else if(stack.getItem() instanceof ItemFlamingAxe){
			ExtendedPlayer properties = ExtendedPlayer.get(Minecraft.getMinecraft().thePlayer);
			return stack.getItemDamage() + properties.flamingAxeDuration;
			
		}else if(stack.getItem() instanceof ItemFrostAxe){
			ExtendedPlayer properties = ExtendedPlayer.get(Minecraft.getMinecraft().thePlayer);
			return stack.getItemDamage() + properties.frostAxeDuration;
		}
		
		return 0;
	}
	
	@Override
	public void registerRenderers() {
		
		mixedFontRenderer = new MixedFontRenderer(Minecraft.getMinecraft().gameSettings,
				new ResourceLocation("textures/font/ascii.png"), Minecraft.getMinecraft().renderEngine, false);

		// Minions
		RenderingRegistry.registerEntityRenderingHandler(EntityZombieMinion.class, new RenderZombieMinion());
		RenderingRegistry.registerEntityRenderingHandler(EntitySkeletonMinion.class, new RenderSkeletonMinion());
		RenderingRegistry.registerEntityRenderingHandler(EntitySpiderMinion.class, new RenderSpiderMinion());
		RenderingRegistry.registerEntityRenderingHandler(EntityBlazeMinion.class, new RenderWraithMinion(new ResourceLocation("textures/entity/blaze.png")));
		RenderingRegistry.registerEntityRenderingHandler(EntityIceWraith.class, new RenderWraithMinion(new ResourceLocation("wizardry:textures/entity/ice_wraith.png")));
		RenderingRegistry.registerEntityRenderingHandler(EntityLightningWraith.class, new RenderWraithMinion(new ResourceLocation("wizardry:textures/entity/lightning_wraith.png")));
		RenderingRegistry.registerEntityRenderingHandler(EntityMagicSlime.class, new RenderMagicSlime(new ModelSlime(16), new ModelSlime(0), 0.25F));
		RenderingRegistry.registerEntityRenderingHandler(EntityIceGiant.class, new RenderIceGiant());
		RenderingRegistry.registerEntityRenderingHandler(EntityPhoenix.class, new RenderPhoenix());
		RenderingRegistry.registerEntityRenderingHandler(EntitySilverfishMinion.class, new RenderSilverfishMinion());

		// Projectiles
		RenderingRegistry.registerEntityRenderingHandler(EntityMagicMissile.class, new RenderMagicArrow(new ResourceLocation("wizardry:textures/entity/magic_missile.png"), false, 8.0, 4.0, 16, 9, false));
		RenderingRegistry.registerEntityRenderingHandler(EntityIceShard.class, new RenderMagicArrow(new ResourceLocation("wizardry:textures/entity/ice_shard.png"), false, 8.0, 2.0, 16, 5, false));
		RenderingRegistry.registerEntityRenderingHandler(EntityLightningArrow.class, new RenderMagicArrow(new ResourceLocation("wizardry:textures/entity/lightning_arrow.png"), true, 8.0, 2.0, 16, 5, false));
		RenderingRegistry.registerEntityRenderingHandler(EntityDart.class, new RenderMagicArrow(new ResourceLocation("wizardry:textures/entity/dart.png"), false, 8.0, 2.0, 16, 5, true));
		RenderingRegistry.registerEntityRenderingHandler(EntityIceLance.class, new RenderMagicArrow(new ResourceLocation("wizardry:textures/entity/ice_lance.png"), false, 16.0, 3.0, 22, 5, true));

		RenderingRegistry.registerEntityRenderingHandler(EntityForceArrow.class, new RenderForceArrow());

		// Creatures
		RenderingRegistry.registerEntityRenderingHandler(EntitySpiritWolf.class, new RenderSpiritWolf(new ModelSpiritWolf(), new ModelSpiritWolf(), 0.5f));
		RenderingRegistry.registerEntityRenderingHandler(EntitySpiritHorse.class, new RenderSpiritHorse(new ModelSpiritHorse(), 0.5f));
		RenderingRegistry.registerEntityRenderingHandler(EntityWizard.class, new RenderWizard());
		RenderingRegistry.registerEntityRenderingHandler(EntityEvilWizard.class, new RenderEvilWizard());
		RenderingRegistry.registerEntityRenderingHandler(EntityDecoy.class, new RenderDecoy());

		// Throwables
		RenderingRegistry.registerEntityRenderingHandler(EntitySparkBomb.class, new RenderProjectile(0.6f, new ResourceLocation("wizardry:textures/entity/spark_bomb.png"), false));
		RenderingRegistry.registerEntityRenderingHandler(EntityFirebomb.class, new RenderProjectile(0.6f, new ResourceLocation("wizardry:textures/items/firebomb.png"), false));
		RenderingRegistry.registerEntityRenderingHandler(EntityPoisonBomb.class, new RenderProjectile(0.6f, new ResourceLocation("wizardry:textures/items/poison_bomb.png"), false));
		RenderingRegistry.registerEntityRenderingHandler(EntityIceCharge.class, new RenderProjectile(0.6f, new ResourceLocation("wizardry:textures/entity/ice_charge.png"), false));
		RenderingRegistry.registerEntityRenderingHandler(EntityForceOrb.class, new RenderProjectile(0.7f, new ResourceLocation("wizardry:textures/entity/force_orb.png"), true));
		RenderingRegistry.registerEntityRenderingHandler(EntitySpark.class, new RenderProjectile(0.4f, new ResourceLocation("wizardry:textures/entity/spark.png"), true));
		RenderingRegistry.registerEntityRenderingHandler(EntityDarknessOrb.class, new RenderProjectile(0.6f, new ResourceLocation("wizardry:textures/entity/darkness_orb.png"), true));
		RenderingRegistry.registerEntityRenderingHandler(EntityFirebolt.class, new RenderProjectile(0.2f, new ResourceLocation("wizardry:textures/entity/firebolt.png"), false));
		RenderingRegistry.registerEntityRenderingHandler(EntityLightningDisc.class, new RenderLightningDisc(new ResourceLocation("wizardry:textures/entity/lightning_sigil.png"), 2.0f));
		RenderingRegistry.registerEntityRenderingHandler(EntitySmokeBomb.class, new RenderProjectile(0.6f, new ResourceLocation("wizardry:textures/items/smoke_bomb.png"), false));

		// Effects and constructs
		RenderingRegistry.registerEntityRenderingHandler(EntityArc.class, new RenderArc());
		RenderingRegistry.registerEntityRenderingHandler(EntityBlackHole.class, new RenderBlackHole());
		RenderingRegistry.registerEntityRenderingHandler(EntityShield.class, new RenderBlank());
		RenderingRegistry.registerEntityRenderingHandler(EntityMeteor.class, new RenderMeteor());
		RenderingRegistry.registerEntityRenderingHandler(EntityBubble.class, new RenderBubble());
		RenderingRegistry.registerEntityRenderingHandler(EntityHammer.class, new RenderHammer());
		RenderingRegistry.registerEntityRenderingHandler(EntityFallingGrass.class, new RenderFallingGrass());
		RenderingRegistry.registerEntityRenderingHandler(EntityIceSpike.class, new RenderIceSpike());

		// Stuff that doesn't render
		RenderingRegistry.registerEntityRenderingHandler(EntityBlizzard.class, new RenderBlank());
		RenderingRegistry.registerEntityRenderingHandler(EntityTornado.class, new RenderBlank());
		RenderingRegistry.registerEntityRenderingHandler(EntityArrowRain.class, new RenderBlank());
		RenderingRegistry.registerEntityRenderingHandler(EntityShadowWraith.class, new RenderBlank());
		RenderingRegistry.registerEntityRenderingHandler(EntityForcefield.class, new RenderBlank());
		RenderingRegistry.registerEntityRenderingHandler(EntityThunderbolt.class, new RenderBlank());
		RenderingRegistry.registerEntityRenderingHandler(EntityStormElemental.class, new RenderBlank());
		RenderingRegistry.registerEntityRenderingHandler(EntityEarthquake.class, new RenderBlank());
		RenderingRegistry.registerEntityRenderingHandler(EntityHailstorm.class, new RenderBlank());

		// Runes on ground
		RenderingRegistry.registerEntityRenderingHandler(EntityHealAura.class, new RenderSigil(new ResourceLocation("wizardry:textures/entity/healing_aura.png"), 5.0f, false));
		RenderingRegistry.registerEntityRenderingHandler(EntityFireSigil.class, new RenderSigil(new ResourceLocation("wizardry:textures/entity/fire_sigil.png"), 2.0f, true));
		RenderingRegistry.registerEntityRenderingHandler(EntityFrostSigil.class, new RenderSigil(new ResourceLocation("wizardry:textures/entity/frost_sigil.png"), 2.0f, true));
		RenderingRegistry.registerEntityRenderingHandler(EntityLightningSigil.class, new RenderSigil(new ResourceLocation("wizardry:textures/entity/lightning_sigil.png"), 2.0f, true));
		RenderingRegistry.registerEntityRenderingHandler(EntityFireRing.class, new RenderFireRing(new ResourceLocation("wizardry:textures/entity/ring_of_fire.png"), 5.0f));
		RenderingRegistry.registerEntityRenderingHandler(EntityDecay.class, new RenderDecay());
		RenderingRegistry.registerEntityRenderingHandler(EntityLightningPulse.class, new RenderLightningPulse(8.0f));

		//RenderingRegistry.registerBlockHandler(new RenderSigilBlock());

		// TESRs
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityArcaneWorkbench.class, new RenderArcaneWorkbench());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityStatue.class, new RenderStatue());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMagicLight.class, new RenderMagicLight());

		// Items
		MinecraftForgeClient.registerItemRenderer(Wizardry.spectralSword, new RenderTranslucentItem());
		MinecraftForgeClient.registerItemRenderer(Wizardry.spectralPickaxe, new RenderTranslucentItem());
		MinecraftForgeClient.registerItemRenderer(Wizardry.spectralBow, new RenderTranslucentItem());
		MinecraftForgeClient.registerItemRenderer(Wizardry.spectralHelmet, new RenderTranslucentItem());
		MinecraftForgeClient.registerItemRenderer(Wizardry.spectralChestplate, new RenderTranslucentItem());
		MinecraftForgeClient.registerItemRenderer(Wizardry.spectralLeggings, new RenderTranslucentItem());
		MinecraftForgeClient.registerItemRenderer(Wizardry.spectralBoots, new RenderTranslucentItem());

	}
}