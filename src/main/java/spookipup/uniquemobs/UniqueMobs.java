package spookipup.uniquemobs;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spookipup.uniquemobs.config.ModConfig;
import spookipup.uniquemobs.registry.ModEntities;
import spookipup.uniquemobs.registry.ModEffects;
import spookipup.uniquemobs.registry.ModItems;
import spookipup.uniquemobs.registry.ModSpawns;

public class UniqueMobs implements ModInitializer {
	public static final String MOD_ID = "unique-mobs";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModConfig.load();
		ModEffects.init();
		ModEntities.init();
		ModItems.init();
		ModSpawns.init();
		LOGGER.info("Unique Mobs initialized!");
	}
}
