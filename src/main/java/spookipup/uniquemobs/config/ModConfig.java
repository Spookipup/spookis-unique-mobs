package spookipup.uniquemobs.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import spookipup.uniquemobs.UniqueMobs;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class ModConfig {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("unique-mobs.json");

	private static ModConfig instance;

	// -- general --

	public double globalSpawnWeightMultiplier = 1.0;
	public double dungeonReplacementChance = 0.25;
	public double dungeonThemedReplacementChance = 0.5;
	public double mineshaftReplacementChance = 0.4;
	public double fortressSpawnerReplacementChance = 0.45;

	// -- per-mob settings, keyed by entity id (e.g. "sprinter_zombie") --

	public Map<String, MobEntry> mobs = new LinkedHashMap<>();

	public static class MobEntry {
		public boolean enabled = true;
		public double spawnWeightMultiplier = 1.0;

		public MobEntry() {}

		public MobEntry(boolean enabled, double spawnWeightMultiplier) {
			this.enabled = enabled;
			this.spawnWeightMultiplier = spawnWeightMultiplier;
		}
	}

	public static ModConfig get() {
		if (instance == null) load();
		return instance;
	}

	public static void load() {
		if (Files.exists(CONFIG_PATH)) {
			try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
				instance = GSON.fromJson(reader, ModConfig.class);
				if (instance == null) instance = new ModConfig();
			} catch (Exception e) {
				UniqueMobs.LOGGER.error("Failed to read config, using defaults", e);
				instance = new ModConfig();
			}
		} else {
			instance = new ModConfig();
		}

		instance.populateDefaults();
		save();
	}

	public static void save() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
				GSON.toJson(instance, writer);
			}
		} catch (IOException e) {
			UniqueMobs.LOGGER.error("Failed to save config", e);
		}
	}

	public boolean isMobEnabled(String id) {
		MobEntry entry = mobs.get(id);
		return entry == null || entry.enabled;
	}

	public int adjustWeight(String id, int baseWeight) {
		double global = Math.max(0, globalSpawnWeightMultiplier);
		double mob = 1.0;
		MobEntry entry = mobs.get(id);
		if (entry != null) mob = Math.max(0, entry.spawnWeightMultiplier);
		return Math.max(0, (int) Math.round(baseWeight * global * mob));
	}

	// makes sure every mob has an entry so users can see and edit them all
	private void populateDefaults() {
		String[] allMobs = {
			"sprinter_zombie", "armored_zombie", "venomous_zombie", "plague_zombie",
			"builder_zombie", "frozen_zombie", "infernal_zombie", "wither_zombie", "ender_zombie",
			"spitting_spider", "web_spinner_spider", "ice_spider", "magma_spider",
			"jumping_spider", "wither_spider",
			"multishot_skeleton", "sniper_skeleton", "poison_skeleton", "ember_skeleton", "ender_skeleton",
			"lightning_creeper", "burrowing_creeper", "toxic_creeper", "frost_creeper",
			"magma_creeper", "wither_creeper", "ender_creeper", "sculk_creeper",
			"assassin_enderman", "enraged_enderman",
			"blast_blaze", "storm_blaze", "wither_blaze", "soul_blaze", "brand_blaze",
			"great_mother_ghast", "delta_ghast", "wither_ghast", "rageling",
			"skitterling", "obsidling", "blightling"
		};
		for (String id : allMobs) {
			mobs.putIfAbsent(id, new MobEntry());
		}
	}
}

