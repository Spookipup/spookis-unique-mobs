package spookipup.uniquemobs;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spookipup.uniquemobs.config.ModConfig;
import spookipup.uniquemobs.registry.ModEntities;
import spookipup.uniquemobs.registry.ModEffects;
import spookipup.uniquemobs.registry.ModItems;
import spookipup.uniquemobs.registry.ModSpawns;

@Mod(UniqueMobs.FORGE_MOD_ID)
public class UniqueMobs {
	public static final String FORGE_MOD_ID = "unique_mobs";
	public static final String MOD_ID = "unique-mobs";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public UniqueMobs() {
		IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

		ModConfig.load();
		ModEffects.register(modBus);
		ModEntities.register(modBus);
		ModItems.register(modBus);
		ModSpawns.register(modBus);
		LOGGER.info("Unique Mobs initialized!");
	}
}

