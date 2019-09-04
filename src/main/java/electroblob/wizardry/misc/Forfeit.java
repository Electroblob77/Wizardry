package electroblob.wizardry.misc;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.entity.EntityMeteor;
import electroblob.wizardry.entity.construct.*;
import electroblob.wizardry.entity.living.*;
import electroblob.wizardry.entity.projectile.EntityFirebomb;
import electroblob.wizardry.entity.projectile.EntityMagicFireball;
import electroblob.wizardry.event.SpellCastEvent;
import electroblob.wizardry.item.ItemArtefact;
import electroblob.wizardry.registry.*;
import electroblob.wizardry.spell.Banish;
import electroblob.wizardry.util.WizardryUtilities;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.BiConsumer;

/**
 * A {@code Forfeit} object represents a negative effect that may happen when a player attempts to cast an
 * undiscovered spell. The nature and severity of the forfeit depends on the element and tier of the spell that was
 * attempted.<br>
 * <br>
 * Adding a new forfeit is as simple as calling {@link Forfeit#add(Tier, Element, Forfeit)} and supplying the
 * {@code Forfeit} instance along with a tier and element to associate it with. To create a forfeit, you may extend
 * this class and instantiate it, or use {@link Forfeit#create(ResourceLocation, BiConsumer)} to concisely define the
 * behaviour (this method is provided since most forfeits' behaviour code is fairly brief and would otherwise result in
 * a large number of trivial 'stub' classes - in fact, many of wizardry's forfeits are defined in a single line).<br>
 * <br>
 * This class also handles the (event-driven) selection of forfeits and determines when one should be applied.
 *
 * @author Electroblob
 * @since Wizardry 4.2
 */
// With the exception of sound events, everything forfeit-related is done right here. How about that for modularity? :P
@Mod.EventBusSubscriber
public abstract class Forfeit {

	private static final ListMultimap<Pair<Tier, Element>, Forfeit> forfeits = ArrayListMultimap.create();

	private static final float TIER_CHANGE_CHANCE = 0.2f;

	private final ResourceLocation name;

	protected final SoundEvent sound;

	public Forfeit(ResourceLocation name){
		this.name = name;
		this.sound = WizardrySounds.createSound("forfeit." + name.getPath());
	}

	public abstract void apply(World world, EntityPlayer player);

	/**
	 * Returns an {@link ITextComponent} for the message displayed when this forfeit is activated.
	 * @param implementName An {@code ITextComponent} for the name of the implement being used. This is usually
	 *                      something generic like 'wand' or 'scroll'.
	 * @return An {@code ITextComponent} representing this forfeit's message, for use in chat messages.
	 * @see Forfeit#getMessageForWand()
	 * @see Forfeit#getMessageForScroll()
	 */
	public ITextComponent getMessage(ITextComponent implementName){
		return new TextComponentTranslation("forfeit." + name.toString(), implementName);
	}

	/** Wrapper for {@link Forfeit#getMessage(ITextComponent)} with {@code implementName} set to the lang file key
	 * {@code item.ebwizardry:wand.generic} */
	public ITextComponent getMessageForWand(){
		return getMessage(new TextComponentTranslation("item." + Wizardry.MODID + ":wand.generic"));
	}

	/** Wrapper for {@link Forfeit#getMessage(ITextComponent)} with {@code implementName} set to the lang file key
	 * {@code item.ebwizardry:scroll.generic} */
	public ITextComponent getMessageForScroll(){
		return getMessage(new TextComponentTranslation("item." + Wizardry.MODID + ":scroll.generic"));
	}

	/** Returns the {@link SoundEvent} played when this forfeit is activated. */
	public SoundEvent getSound(){
		return sound;
	}

	public static void add(Tier tier, Element element, Forfeit forfeit){
		forfeits.put(Pair.of(tier, element), forfeit);
	}

	public static Forfeit getRandomForfeit(Random random, Tier tier, Element element){
		float f = random.nextFloat();
		if(f < TIER_CHANGE_CHANCE && tier.ordinal() > 0) tier = Tier.values()[tier.ordinal() - 1];
		else if(f > 1 - TIER_CHANGE_CHANCE && tier.ordinal() < Tier.values().length-1) tier = Tier.values()[tier.ordinal() + 1];
		List<Forfeit> matches = forfeits.get(Pair.of(tier, element));
		if(matches.isEmpty()){
			Wizardry.logger.warn("No forfeits with tier {} and element {}!", tier, element);
			return null;
		}
		return matches.get(random.nextInt(matches.size()));
	}

	public static Collection<Forfeit> getForfeits(){
		return Collections.unmodifiableCollection(forfeits.values());
	}

	/** Static helper method that creates a {@code Forfeit} with the given name and an effect specified by the given
	 * consumer. This allows code to use a neater lambda expression rather than an anonymous class. */
	public static Forfeit create(ResourceLocation name, BiConsumer<World, EntityPlayer> effect){
		return new Forfeit(name){
			@Override
			public void apply(World world, EntityPlayer player){
				effect.accept(world, player);
			}
		};
	}

	/** Internal wrapper for {@link Forfeit#create(ResourceLocation, BiConsumer)} so I don't have to put wizardry's
	 * mod ID in every time. */
	private static Forfeit create(String name, BiConsumer<World, EntityPlayer> effect){
		return create(new ResourceLocation(Wizardry.MODID, name), effect);
	}

	@SubscribeEvent(priority = EventPriority.NORMAL) // Forfeits come after spell disabling but before modifiers
	public static void onSpellCastPreEvent(SpellCastEvent.Pre event){

		if(!Wizardry.settings.discoveryMode) return;

		if(event.getCaster() instanceof EntityPlayer && !((EntityPlayer)event.getCaster()).isCreative()
				&& (event.getSource() == SpellCastEvent.Source.WAND || event.getSource() == SpellCastEvent.Source.SCROLL)){

			EntityPlayer player = (EntityPlayer)event.getCaster();
			WizardData data = WizardData.get(player);

			float chance = (float)Wizardry.settings.forfeitChance;
			if(ItemArtefact.isArtefactActive(player, WizardryItems.amulet_wisdom)) chance *= 0.5;

			// Use the synchronised random to ensure the same outcome on client- and server-side
			if(data.synchronisedRandom.nextFloat() < chance && !data.hasSpellBeenDiscovered(event.getSpell())){

				event.setCanceled(true);

				Forfeit forfeit = getRandomForfeit(event.getWorld().rand, event.getSpell().getTier(), event.getSpell().getElement());

				if(forfeit == null){ // Should never happen, but just in case...
					if(!event.getWorld().isRemote) player.sendMessage(new TextComponentTranslation("forfeit.ebwizardry:do_nothing"));
					return;
				}

				forfeit.apply(event.getWorld(), player);

				WizardryAdvancementTriggers.spell_failure.triggerFor(player);

				WizardryUtilities.playSoundAtPlayer(player, forfeit.getSound(), WizardrySounds.SPELLS, 1, 1);

				if(!event.getWorld().isRemote) player.sendMessage(
						event.getSource() == SpellCastEvent.Source.WAND ? forfeit.getMessageForWand() : forfeit.getMessageForScroll());
			}
		}
	}

	/** Called from the preInit method in the main mod class to set up all the forfeits. */
	public static void register(){

		add(Tier.NOVICE, Element.FIRE, create("burn_self", (w, p) -> p.setFire(5)));

		add(Tier.APPRENTICE, Element.FIRE, create("fireball", (w, p) -> {
			if(!w.isRemote){
				EntityMagicFireball fireball = new EntityMagicFireball(w);
				Vec3d vec = p.getPositionEyes(0).add(p.getLookVec().scale(6));
				fireball.setPosition(vec.x, vec.y, vec.z);
				fireball.shoot(p.posX, p.posY + p.getEyeHeight(), p.posZ, 1.5f, 1);
				w.spawnEntity(fireball);
			}
		}));

		add(Tier.APPRENTICE, Element.FIRE, create("firebomb", (w, p) -> {
			if(!w.isRemote){
				EntityFirebomb firebomb = new EntityFirebomb(w);
				firebomb.setPosition(p.posX, p.posY + 5, p.posZ);
				w.spawnEntity(firebomb);
			}
		}));

		add(Tier.ADVANCED, Element.FIRE, create("explode", (w, p) -> w.createExplosion(null, p.posX, p.posY, p.posZ, 1, false)));

		add(Tier.ADVANCED, Element.FIRE, create("blazes", (w, p) -> {
			if(!w.isRemote){
				for(int i = 0; i < 3; i++){
					BlockPos pos = WizardryUtilities.findNearbyFloorSpace(p, 4, 2);
					if(pos == null) break;
					EntityBlazeMinion blaze = new EntityBlazeMinion(w);
					blaze.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
					w.spawnEntity(blaze);
				}
			}
		}));

		add(Tier.MASTER, Element.FIRE, create("burn_surroundings", (w, p) -> {
			if(!w.isRemote){
				List<BlockPos> sphere = WizardryUtilities.getBlockSphere(p.getPosition(), 6);
				for(BlockPos pos : sphere){
					if(w.rand.nextBoolean() && w.isAirBlock(pos)) w.setBlockState(pos, Blocks.FIRE.getDefaultState());
				}
			}
		}));

		add(Tier.MASTER, Element.FIRE, create("meteors", (w, p) -> {
			if(!w.isRemote) for(int i=0; i<5; i++) w.spawnEntity(new EntityMeteor(w, p.posX + w.rand.nextDouble() * 16 - 8,
						p.posY + 40 + w.rand.nextDouble() * 30, p.posZ + w.rand.nextDouble() * 16 - 8,
						1, WizardryUtilities.canDamageBlocks(p, w)));
		}));

		add(Tier.NOVICE, Element.ICE, create("freeze_self", (w, p) -> p.addPotionEffect(new PotionEffect(WizardryPotions.frost, 200))));

		add(Tier.APPRENTICE, Element.ICE, create("freeze_self_2", (w, p) -> p.addPotionEffect(new PotionEffect(WizardryPotions.frost, 300, 1))));

		add(Tier.APPRENTICE, Element.ICE, create("ice_spikes", (w, p) -> {
			if(!w.isRemote){
				for(int i = 0; i < 5; i++){
					EntityIceSpike iceSpike = new EntityIceSpike(w);
					double x = p.posX + 2 - w.rand.nextFloat() * 4;
					double z = p.posZ + 2 - w.rand.nextFloat() * 4;
					Integer y = WizardryUtilities.getNearestSurface(w, new BlockPos(x, p.posY, z), EnumFacing.UP, 2, true,
							WizardryUtilities.SurfaceCriteria.basedOn(World::isBlockFullCube));
					if(y == null) break;
					iceSpike.setFacing(EnumFacing.UP);
					iceSpike.setPosition(x, y, z);
					w.spawnEntity(iceSpike);
				}
			}
		}));

		add(Tier.ADVANCED, Element.ICE, create("blizzard", (w, p) -> {
			if(!w.isRemote){
				EntityBlizzard blizzard = new EntityBlizzard(w);
				blizzard.setPosition(p.posX, p.posY, p.posZ);
				w.spawnEntity(blizzard);
			}
		}));

		add(Tier.ADVANCED, Element.ICE, create("ice_wraiths", (w, p) -> {
			if(!w.isRemote){
				for(int i = 0; i < 3; i++){
					BlockPos pos = WizardryUtilities.findNearbyFloorSpace(p, 4, 2);
					if(pos == null) break;
					EntityIceWraith iceWraith = new EntityIceWraith(w);
					iceWraith.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
					w.spawnEntity(iceWraith);
				}
			}
		}));

		add(Tier.MASTER, Element.ICE, create("hailstorm", (w, p) -> {
			if(!w.isRemote){
				EntityHailstorm hailstorm = new EntityHailstorm(w);
				hailstorm.setPosition(p.posX, p.posY + 5, p.posZ - 3); // Subtract 3 from z because it's facing south (yaw 0)
				w.spawnEntity(hailstorm);
			}
		}));

		add(Tier.MASTER, Element.ICE, create("ice_giant", (w, p) -> {
			if(!w.isRemote){
				EntityIceGiant iceGiant = new EntityIceGiant(w);
				iceGiant.setPosition(p.posX + p.getLookVec().x * 4, p.posY, p.posZ + p.getLookVec().z * 4);
				w.spawnEntity(iceGiant);
			}
		}));

		add(Tier.NOVICE, Element.LIGHTNING, create("thunder", (w, p) -> {
			p.addVelocity(-p.getLookVec().x, 0, -p.getLookVec().z);
			if(w.isRemote) w.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, p.posX, p.posY, p.posZ, 0, 0, 0);
		}));

		add(Tier.APPRENTICE, Element.LIGHTNING, create("storm", (w, p) -> {
			int standardWeatherTime = (300 + (new Random()).nextInt(600)) * 20;
			w.getWorldInfo().setRaining(true);
			w.getWorldInfo().setRainTime(standardWeatherTime);
			w.getWorldInfo().setThundering(true);
			w.getWorldInfo().setThunderTime(standardWeatherTime);
		}));

		add(Tier.APPRENTICE, Element.LIGHTNING, create("lightning_sigils", (w, p) -> {
			if(!w.isRemote){
				for(EnumFacing direction : EnumFacing.HORIZONTALS){
					BlockPos pos = p.getPosition().offset(direction, 2);
					Integer y = WizardryUtilities.getNearestFloor(w, pos, 2);
					if(y == null) continue;
					EntityLightningSigil sigil = new EntityLightningSigil(w);
					sigil.setPosition(pos.getX() + 0.5, y, pos.getZ() + 0.5);
					w.spawnEntity(sigil);
				}
			}
		}));

		add(Tier.ADVANCED, Element.LIGHTNING, create("lightning", (w, p) -> w.addWeatherEffect(new EntityLightningBolt(w, p.posX, p.posY, p.posZ, false))));

		add(Tier.ADVANCED, Element.LIGHTNING, create("paralyse_self", (w, p) -> p.addPotionEffect(new PotionEffect(WizardryPotions.paralysis, 200))));

		add(Tier.ADVANCED, Element.LIGHTNING, create("lightning_wraiths", (w, p) -> {
			if(!w.isRemote){
				for(int i = 0; i < 3; i++){
					BlockPos pos = WizardryUtilities.findNearbyFloorSpace(p, 4, 2);
					if(pos == null) break;
					EntityLightningWraith lightningWraith = new EntityLightningWraith(w);
					lightningWraith.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
					w.spawnEntity(lightningWraith);
				}
			}
		}));

		add(Tier.MASTER, Element.LIGHTNING, create("storm_elementals", (w, p) -> {
			if(!w.isRemote){
				for(EnumFacing direction : EnumFacing.HORIZONTALS){
					BlockPos pos = p.getPosition().offset(direction, 3);
					EntityStormElemental stormElemental = new EntityStormElemental(w);
					stormElemental.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
					w.spawnEntity(stormElemental);
				}
			}
		}));

		add(Tier.NOVICE, Element.NECROMANCY, create("nausea", (w, p) -> p.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 400))));

		add(Tier.APPRENTICE, Element.NECROMANCY, create("zombie_horde", (w, p) -> {
			if(!w.isRemote){
				for(int i = 0; i < 3; i++){
					BlockPos pos = WizardryUtilities.findNearbyFloorSpace(p, 4, 2);
					if(pos == null) break;
					EntityZombieMinion zombie = new EntityZombieMinion(w);
					zombie.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
					w.spawnEntity(zombie);
				}
			}
		}));

		add(Tier.ADVANCED, Element.NECROMANCY, create("wither_self", (w, p) -> p.addPotionEffect(new PotionEffect(MobEffects.WITHER, 400))));

		add(Tier.MASTER, Element.NECROMANCY, create("cripple_self", (w, p) -> p.attackEntityFrom(DamageSource.MAGIC, p.getHealth() - 1)));

		add(Tier.MASTER, Element.NECROMANCY, create("shadow_wraiths", (w, p) -> {
			if(!w.isRemote){
				for(EnumFacing direction : EnumFacing.HORIZONTALS){
					BlockPos pos = p.getPosition().offset(direction, 3);
					EntityShadowWraith wraith = new EntityShadowWraith(w);
					wraith.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
					w.spawnEntity(wraith);
				}
			}
		}));

		add(Tier.NOVICE, Element.EARTH, create("snares", (w, p) -> {
			if(!w.isRemote){
				for(EnumFacing direction : EnumFacing.HORIZONTALS){
					BlockPos pos = p.getPosition().offset(direction);
					if(WizardryUtilities.canBlockBeReplaced(w, pos)) w.setBlockState(pos, WizardryBlocks.snare.getDefaultState());
				}
			}
		}));

		add(Tier.NOVICE, Element.EARTH, create("squid", (w, p) -> {
			if(!w.isRemote){
				EntitySquid squid = new EntitySquid(w);
				squid.setPosition(p.posX, p.posY + 3, p.posZ);
				w.spawnEntity(squid);
			}
		}));

		add(Tier.APPRENTICE, Element.EARTH, create("uproot_plants", (w, p) -> {
			if(!w.isRemote){
				List<BlockPos> sphere = WizardryUtilities.getBlockSphere(p.getPosition(), 5);
				sphere.removeIf(pos -> !(w.getBlockState(pos).getBlock() instanceof IPlantable));
				sphere.forEach(pos -> w.destroyBlock(pos, true));
			}
		}));

		add(Tier.APPRENTICE, Element.EARTH, create("poison_self", (w, p) -> p.addPotionEffect(new PotionEffect(MobEffects.POISON, 400, 1))));

		add(Tier.ADVANCED, Element.EARTH, create("flood", (w, p) -> {
			if(!w.isRemote){
				List<BlockPos> sphere = WizardryUtilities.getBlockSphere(p.getPosition().up(), 2);
				sphere.removeIf(pos -> !WizardryUtilities.canBlockBeReplaced(w, pos, true));
				sphere.forEach(pos -> w.setBlockState(pos, Blocks.WATER.getDefaultState()));
			}
		}));

		add(Tier.MASTER, Element.EARTH, create("bury_self", (w, p) -> {
			if(!w.isRemote){
				List<BlockPos> sphere = WizardryUtilities.getBlockSphere(p.getPosition(), 4);
				sphere.removeIf(pos -> !w.getBlockState(pos).isFullCube());
				sphere.removeIf(pos -> WizardryUtilities.isBlockUnbreakable(w, pos));
				sphere.forEach(pos -> {
					EntityFallingBlock block = new EntityFallingBlock(w, pos.getX() + 0.5, pos.getY() + 0.5,
							pos.getZ() + 0.5, w.getBlockState(pos));
					block.motionY = 0.3 * (4 - (p.getPosition().getY() - pos.getY()));
					w.spawnEntity(block);
				});
			}
		}));

		add(Tier.NOVICE, Element.SORCERY, create("spill_inventory", (w, p) -> {
			for(int i = 0; i < p.inventory.mainInventory.size(); i++){
				ItemStack stack = p.inventory.mainInventory.get(i);
				if(!stack.isEmpty()){
					p.dropItem(stack, true, false);
					p.inventory.mainInventory.set(i, ItemStack.EMPTY);
				}
			}
		}));

		add(Tier.APPRENTICE, Element.SORCERY, create("teleport_self", (w, p) -> ((Banish)Spells.banish).teleport(p, w, 8 + w.rand.nextDouble() * 8)));

		add(Tier.ADVANCED, Element.SORCERY, create("levitate_self", (w, p) -> p.addPotionEffect(new PotionEffect(MobEffects.LEVITATION, 200))));

		add(Tier.ADVANCED, Element.SORCERY, create("vex_horde", (w, p) -> {
			if(!w.isRemote){
				for(int i = 0; i < 4; i++){
					BlockPos pos = WizardryUtilities.findNearbyFloorSpace(p, 4, 2);
					if(pos == null) break;
					EntityVexMinion vex = new EntityVexMinion(w);
					vex.setPosition(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
					w.spawnEntity(vex);
				}
			}
		}));

		add(Tier.MASTER, Element.SORCERY, create("black_hole", (w, p) -> {
			EntityBlackHole blackHole = new EntityBlackHole(w);
			Vec3d vec = p.getPositionEyes(1).add(p.getLookVec().scale(4));
			blackHole.setPosition(vec.x, vec.y, vec.z);
			w.spawnEntity(blackHole);
		}));

		add(Tier.MASTER, Element.SORCERY, create("arrow_rain", (w, p) -> {
			if(!w.isRemote){
				EntityArrowRain arrowRain = new EntityArrowRain(w);
				arrowRain.setPosition(p.posX, p.posY + 5, p.posZ - 3); // Subtract 3 from z because it's facing south (yaw 0)
				w.spawnEntity(arrowRain);
			}
		}));

		add(Tier.NOVICE, Element.HEALING, create("damage_self", (w, p) -> p.attackEntityFrom(DamageSource.MAGIC, 4)));

		add(Tier.NOVICE, Element.HEALING, create("spill_armour", (w, p) -> {
			for(int i = 0; i < p.inventory.armorInventory.size(); i++){
				ItemStack stack = p.inventory.armorInventory.get(i);
				if(!stack.isEmpty()){
					p.dropItem(stack, true, false);
					p.inventory.armorInventory.set(i, ItemStack.EMPTY);
				}
			}
		}));

		add(Tier.APPRENTICE, Element.HEALING, create("hunger", (w, p) -> p.addPotionEffect(new PotionEffect(MobEffects.HUNGER, 400, 4))));

		add(Tier.APPRENTICE, Element.HEALING, create("blind_self", (w, p) -> p.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 200))));

		add(Tier.ADVANCED, Element.HEALING, create("weaken_self", (w, p) -> p.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 600, 3))));

		add(Tier.ADVANCED, Element.HEALING, create("jam_self", (w, p) -> p.addPotionEffect(new PotionEffect(WizardryPotions.arcane_jammer, 300))));

		add(Tier.MASTER, Element.HEALING, create("curse_self", (w, p) -> p.addPotionEffect(new PotionEffect(WizardryPotions.curse_of_undeath, Integer.MAX_VALUE))));

	}

}