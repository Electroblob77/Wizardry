package electroblob.wizardry.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Element;
import electroblob.wizardry.constants.SpellType;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.registry.Spells;
import electroblob.wizardry.spell.Spell;
import io.netty.buffer.ByteBuf;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Object that stores base properties associated with spells. Each spell has a single instance of this class which
 * stores its base properties and other data. This class also handles loading of the properties from JSON.
 * <p></p>
 * All the fields in this class are final and are assigned during object creation. This is because the intent is that a
 * new SpellProperties object is created on load and synced with each client on player login. Having final fields
 * therefore guarantees that the properties are always synced whenever necessary, but cannot otherwise be fiddled
 * with programmatically.
 * <p></p>
 * Generally, users need not worry about this class; it is intended that the various property getters in Spell be used
 * rather than querying this class directly <i>(in fact, you can't do that without reflection anyway)</i>.
 * <p></p>
 * @author Electroblob
 * @since Wizardry 4.2
 */
public final class SpellProperties {

	private static final Gson gson = new Gson();

	/** Set of enum constants representing contexts in which a spell can be enabled/disabled. */
	public enum Context {

		/** Disabling this context will make a spell's book unobtainable and unusable. */			BOOK("book"),
		/** Disabling this context will make a spell's scroll unobtainable and unusable. */			SCROLL("scroll"),
		/** Disabling this context will prevent a spell from being cast using a wand. */			WANDS("wands"),
		/** Disabling this context will prevent NPCs from casting or dropping a spell. */			NPCS("npcs"),
		/** Disabling this context will prevent dispensers from casting a spell. */					DISPENSERS("dispensers"),
		/** Disabling this context will prevent a spell from being cast using commands. */			COMMANDS("commands"),
		/** Disabling this context will prevent a spell's book or scroll generating in chests. */ 	TREASURE("treasure"),
		/** Disabling this context will prevent a spell's book or scroll from being sold by NPCs.*/ TRADES("trades"),
		/** Disabling this context will prevent a spell's book or scroll being dropped by mobs. */	LOOTING("looting");

		/** The JSON identifier for this context. */
		public final String name;

		Context(String name){
			this.name = name;
		}
	}

	/** A map storing whether each context is enabled for this spell. */
	private final Map<Context, Boolean> enabledContexts;
	/** A map storing the base values for this spell. These values are defined by the spell class and cannot be
	 * changed. */
	// We're using Number here because it makes implementors think about what they convert it to.
	// If we did what attributes do and just use doubles, people (myself included!) might plug them into calculations
	// without thinking. However, with Number you can't just do that, you have to convert and therefore you have to
	// decide how to do the conversion. Internally they're handled as floats though.
	private final Map<String, Number> baseValues;

	/** The tier this spell belongs to. */
	public final Tier tier;
	/** The element this spell belongs to. */
	public final Element element;
	/** The type of spell this is classified as. */
	public final SpellType type;
	/** Mana cost of the spell. If it is a continuous spell the cost is per second. */
	public final int cost;
	/** The charge-up time of the spell, in ticks. */
	public final int chargeup;
	/** The cooldown time of the spell, in ticks. */
	public final int cooldown;

	// Sometimes it just makes more sense to do the JSON parsing in the constructor
	// It's the only way we're gonna keep the fields final!
	/**
	 * Parses the given JSON object and constructs a new {@code SpellProperties} from it, setting all the relevant
	 * fields and references.
	 *
	 * @param json A JSON object representing the spell properties to be constructed.
	 * @param spell The spell that this {@code SpellProperties} object is for.
	 * @throws JsonSyntaxException if at any point the JSON object is found to be invalid.
	 */
	private SpellProperties(JsonObject json, Spell spell){

		String[] baseValueNames = spell.getPropertyKeys();

		enabledContexts = new EnumMap<>(Context.class);
		baseValues = new HashMap<>();

		JsonObject enabled = JsonUtils.getJsonObject(json, "enabled");

		// This time we know the exact set of properties so we can iterate over them instead of the json object
		// In fact, we actually want to throw an exception if any of them are missing
		for(Context context : Context.values()){
			enabledContexts.put(context, JsonUtils.getBoolean(enabled, context.name));
		}

		try {
			tier = Tier.fromName(JsonUtils.getString(json, "tier"));
			element = Element.fromName(JsonUtils.getString(json, "element"));
			type = SpellType.fromName(JsonUtils.getString(json, "type"));
		}catch(IllegalArgumentException e){
			throw new JsonSyntaxException("Incorrect spell property value", e);
		}

		cost = JsonUtils.getInt(json, "cost");
		chargeup = JsonUtils.getInt(json, "chargeup");
		cooldown = JsonUtils.getInt(json, "cooldown");

		// There's not much point specifying the classes of the numbers here because the json getter methods just
		// perform conversion to the requested type anyway. It therefore makes very little difference whether the
		// conversion is done during JSON parsing or when we actually use the value - and at least in the latter case,
		// individual subclasses have control over how it is converted.

		// My case in point: summoning 2.5 spiders is obviously nonsense, but what happens when we cast that with a
		// modifier of 2? Should we round the base value down to 2 and then apply the x2 modifier to get 4 spiders?
		// Should we round it up instead? Or should we apply the modifier first and then do the rounding, so with no
		// modifier we still get 2 spiders but with the x2 modifier we get 5?
		// The most pragmatic solution is to let the spell class decide for itself.
		// (Of course, we can only hope that the users aren't jerks and don't try to summon 2 and a half spiders...)

		JsonObject baseValueObject = JsonUtils.getJsonObject(json, "base_properties");

		// If the code requests more values than the JSON file contains, that will cause a JsonSyntaxException here anyway.
		// If there are redundant values in the JSON file, chances are that a user has misunderstood the system and tried
		// to add properties that aren't implemented. However, redundant values will also be found if a programmer has
		// forgotten to call addProperties in their spell constructor (I know I have!), potentially causing a crash at
		// some random point in the future. Since redundant values aren't a problem by themselves, we shouldn't throw an
		// exception, but a warning is appropriate.

		int redundantKeys = baseValueObject.size() - baseValueNames.length;
		if(redundantKeys > 0) Wizardry.logger.warn("Spell " + spell.getRegistryName() + " has " + redundantKeys +
				" redundant spell property key(s) defined in its JSON file. Extra values will have no effect! (Modders:" +
				" make sure you have called addProperties(...) during spell construction)");

		if(baseValueNames.length > 0){

			for(String baseValueName : baseValueNames){
				baseValues.put(baseValueName, JsonUtils.getFloat(baseValueObject, baseValueName));
			}
		}

	}

	/** Constructs a new SpellProperties object for the given spell, reading its values from the given ByteBuf. */
	public SpellProperties(Spell spell, ByteBuf buf){

		enabledContexts = new EnumMap<>(Context.class);
		baseValues = new HashMap<>();

		for(Context context : Context.values()){
			// Enum maps have a guaranteed iteration order so this works fine
			enabledContexts.put(context, buf.readBoolean());
		}

		tier = Tier.values()[buf.readShort()];
		element = Element.values()[buf.readShort()];
		type = SpellType.values()[buf.readShort()];

		cost = buf.readInt();
		chargeup = buf.readInt();
		cooldown = buf.readInt();

		List<String> keys = Arrays.asList(spell.getPropertyKeys());
		Collections.sort(keys); // Should be the same list of keys in the same order they were written to the ByteBuf

		for(String key : keys){
			baseValues.put(key, buf.readFloat());
		}
	}

	/** Writes this SpellProperties object to the given ByteBuf so it can be sent via packets. */
	public void write(ByteBuf buf){

		for(Context context : Context.values()){
			// Enum maps have a guaranteed iteration order so this works fine
			buf.writeBoolean(enabledContexts.get(context));
		}

		buf.writeShort(tier.ordinal());
		buf.writeShort(element.ordinal());
		buf.writeShort(type.ordinal());

		buf.writeInt(cost);
		buf.writeInt(chargeup);
		buf.writeInt(cooldown);

		List<String> keys = new ArrayList<>(baseValues.keySet());
		Collections.sort(keys); // Sort alphabetically (as long as the order is consistent it doesn't matter)

		for(String key : keys){
			buf.writeFloat(baseValues.get(key).floatValue());
		}
	}

	/**
	 * Returns whether the spell is enabled in any of the given contexts.
	 * @param contexts The context in which to check if the spell is enabled.
	 * @return True if the spell is enabled in any of the given contexts, false if not.
	 */
	public boolean isEnabled(Context... contexts){
		return enabledContexts.entrySet().stream().anyMatch(e -> e.getValue() && Arrays.asList(contexts).contains(e.getKey()));
	}

	/**
	 * Returns whether a base value was defined with the given identifier.
	 * @param identifier The string identifier to check for.
	 * @return True if a base value was defined with the given identifier, false otherwise.
	 */
	public boolean hasBaseValue(String identifier){
		return baseValues.containsKey(identifier);
	}

	/**
	 * Returns the base value for this spell that corresponds to the given identifier. To check whether an identifier
	 * exists, use {@link SpellProperties#hasBaseValue(String)}.
	 * @param identifier The string identifier to fetch the base value for.
	 * @return The base value, as a {@code Number}.
	 * @throws IllegalArgumentException if no base value was defined with the given identifier.
	 */
	// Better to throw an exception than make this nullable because the vast majority of uses are for retrieving
	// specific spells' properties that are known to exist, and IntelliJ would scream at us for not checking
	public Number getBaseValue(String identifier){
		if(!baseValues.containsKey(identifier)){
			throw new IllegalArgumentException("Base value with identifier '" + identifier + "' is not defined.");
		}
		return baseValues.get(identifier);
	}

	/**
	 * Called from preInit() in the main mod class to initialise the spell property system.
	 */
	// For some reason I had this called from a method in CommonProxy which was overridden to do nothing in
	// ClientProxy, but that method was never called and instead this one was called directly from the main mod class.
	// I *think* I decided against the proxy thing and just forgot to delete the methods (they're gone now), but if
	// things don't work as expected then that may be why - pretty sure it's fine though since the properties get
	// wiped client-side on each login anyway.
	public static void init(){

		// Collecting to a set should give us one of each mod ID
		Set<String> modIDs = Spell.getAllSpells().stream().map(s -> s.getRegistryName().getNamespace()).collect(Collectors.toSet());

		boolean flag = loadConfigSpellProperties();

		for(String modID : modIDs){
			flag &= loadBuiltInSpellProperties(modID); // Don't short-circuit, or mods later on won't get loaded!
		}

		if(!flag) Wizardry.logger.warn("Some spell property files did not load correctly; this will likely cause problems later!");
	}

	// There are now three 'layers' of spell properties - in order of priority, these are:
	// 1. World-specific properties, stored in saves/[world]/data/spells
	// 2. Global overrides, stored in config/ebwizardry/spells
	// 3. Built-in properties, stored in mods/[mod jar]/assets/spells
	// There's a method for loading each of these below, because that makes sense to me!

	public static void loadWorldSpecificSpellProperties(World world){

		Wizardry.logger.info("Loading custom spell properties for world {}", world.getWorldInfo().getWorldName());

		File spellJSONDir = new File(new File(world.getSaveHandler().getWorldDirectory(), "data"), "spells");

		if(spellJSONDir.mkdirs()) return; // If it just got created it can't possibly have anything inside

		if(!loadSpellPropertiesFromDir(spellJSONDir)) Wizardry.logger.warn("Some spell property files did not load correctly; this will likely cause problems later!");
	}

	private static boolean loadConfigSpellProperties(){

		Wizardry.logger.info("Loading spell properties from config folder");

		File spellJSONDir = new File(Wizardry.configDirectory, "spells");

		if(!spellJSONDir.exists()) return true; // If there's no global spell properties folder, do nothing

		return loadSpellPropertiesFromDir(spellJSONDir);
	}

	// For crafting recipes, Forge does some stuff behind the scenes to load recipe JSON files from mods' namespaces.
	// This leverages the same methods.

	private static boolean loadBuiltInSpellProperties(String modID){

		// Yes, I know you're not supposed to do orElse(null). But... meh.
		ModContainer mod = Loader.instance().getModList().stream().filter(m -> m.getModId().equals(modID)).findFirst().orElse(null);

		if(mod == null){
			Wizardry.logger.warn("Tried to load built-in spell properties for mod with ID '" + modID + "', but no such mod was loaded");
			return false; // Failed!
		}

		// Spells will be removed from this list as their properties are set
		// If everything works properly, it should be empty by the end
		List<Spell> spells = Spell.getSpells(s -> s.getRegistryName().getNamespace().equals(modID));
		if(modID.equals(Wizardry.MODID)) spells.add(Spells.none); // In this particular case we do need the none spell

		Wizardry.logger.info("Loading built-in spell properties for " + spells.size() + " spells in mod " + modID);

		// This method is used by Forge to load mod recipes and advancements, so it's a fair bet it's the right one
		// In the absence of Javadoc, here's what the non-obvious parameters do:
		// - preprocessor is called once with just the root directory, allowing any global index files to be processed
		// - processor is called once for each file in the directory so processing can be done
		// - defaultUnfoundRoot is the default value to return if the root specified isn't found
		// - visitAllFiles determines whether the method short-circuits; in other words, if the processor returns false
		// at any point and visitAllFiles is false, the method returns immediately.
		boolean success = CraftingHelper.findFiles(mod, "assets/" + modID + "/spells", null,

				(root, file) -> {

					String relative = root.relativize(file).toString();
					if(!"json".equals(FilenameUtils.getExtension(file.toString())) || relative.startsWith("_"))
						return true; // True or it'll look like it failed just because it found a non-JSON file

					String name = FilenameUtils.removeExtension(relative).replaceAll("\\\\", "/");
					ResourceLocation key = new ResourceLocation(modID, name);

					Spell spell = Spell.registry.getValue(key);

					// If no spell matches a particular file, log it and just ignore the file
					if(spell == null){
						Wizardry.logger.info("Spell properties file " + name + ".json does not match any registered spells; ensure the filename is spelled correctly.");
						return true;
					}

					// We want to do this regardless of whether the JSON file got read properly, because that prints its
					// own separate warning
					if(!spells.remove(spell)) Wizardry.logger.warn("What's going on?!");

					// Ignore spells overridden in the config folder
					// This needs to be done AFTER the above line or it'll think there are missing spell properties files
					if(spell.arePropertiesInitialised()) return true;

					BufferedReader reader = null;

					try{

						reader = Files.newBufferedReader(file);

						JsonObject json = JsonUtils.fromJson(gson, reader, JsonObject.class);
						SpellProperties properties = new SpellProperties(json, spell);
						spell.setProperties(properties);

					}catch(JsonParseException jsonparseexception){
						Wizardry.logger.error("Parsing error loading spell property file for " + key, jsonparseexception);
						return false;
					}catch(IOException ioexception){
						Wizardry.logger.error("Couldn't read spell property file for " + key, ioexception);
						return false;
					}finally{
						IOUtils.closeQuietly(reader);
					}

					return true;

				},
				true, true);

		// If a spell is missing its file, log an error
		if(!spells.isEmpty()){
			if(spells.size() <= 15){
				spells.forEach(s -> Wizardry.logger.error("Spell " + s.getRegistryName() + " is missing a properties file!"));
			}else{
				// If there are more than 15 don't bother logging them all, chances are they're all missing
				Wizardry.logger.error("Mod " + modID + " has " + spells.size() + " spells that are missing properties files!");
			}
		}

		return success;
	}

	private static boolean loadSpellPropertiesFromDir(File dir){

		boolean success = true;

		for(File file : FileUtils.listFiles(dir, new String[]{"json"}, true)){

			// The structure in world and config folders is subtly different in that the "spells" and mod id directories
			// are in the opposite order, i.e. it's spells/modid/whatever.json instead of modid/spells/whatever.json
			String relative = dir.toPath().relativize(file.toPath()).toString(); // modid\whatever.json
			String nameAndModID = FilenameUtils.removeExtension(relative).replaceAll("\\\\", "/"); // modid/whatever
			String modID = nameAndModID.split("/")[0]; // modid
			String name = nameAndModID.substring(nameAndModID.indexOf('/') + 1); // whatever

			ResourceLocation key = new ResourceLocation(modID, name);

			Spell spell = Spell.registry.getValue(key);

			// If no spell matches a particular file, log it and just ignore the file
			if(spell == null){
				Wizardry.logger.info("Spell properties file " + nameAndModID + ".json does not match any registered spells; ensure the filename is spelled correctly.");
				continue;
			}

			BufferedReader reader = null;

			try{

				reader = Files.newBufferedReader(file.toPath());

				JsonObject json = JsonUtils.fromJson(gson, reader, JsonObject.class);
				SpellProperties properties = new SpellProperties(json, spell);
				spell.setProperties(properties);

			}catch(JsonParseException jsonparseexception){
				Wizardry.logger.error("Parsing error loading spell property file for " + key, jsonparseexception);
				success = false;
			}catch(IOException ioexception){
				Wizardry.logger.error("Couldn't read spell property file for " + key, ioexception);
				success = false;
			}finally{
				IOUtils.closeQuietly(reader);
			}
		}

		return success;
	}

}

// We probably could have used the attribute system for all of this, but I am reluctant to do so for a number of
// reasons:
// - It's a mess.
// - Unlike entities and itemstacks, spells don't have a separate instance for each time they are cast, which might
// prove problematic.
// - I'm loading my base properties once and not touching them again, so they're more like block materials than anything
// else.
// - Some of the properties aren't numerical, and some of them can't have modifiers applied. In fact, most of them can't!
// So even if we were to use attributes, we'd still need this class.