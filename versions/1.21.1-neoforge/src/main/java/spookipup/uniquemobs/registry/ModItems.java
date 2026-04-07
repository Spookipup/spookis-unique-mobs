package spookipup.uniquemobs.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import spookipup.uniquemobs.UniqueMobs;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ModItems {

	private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(UniqueMobs.MOD_ID);
	private static final List<DeferredItem<Item>> SPAWN_EGGS = new ArrayList<>();

	// zombies
	public static final DeferredItem<Item> VENOMOUS_ZOMBIE_SPAWN_EGG = spawnEgg("venomous_zombie", ModEntities.VENOMOUS_ZOMBIE);
	public static final DeferredItem<Item> INFERNAL_ZOMBIE_SPAWN_EGG = spawnEgg("infernal_zombie", ModEntities.INFERNAL_ZOMBIE);
	public static final DeferredItem<Item> FROZEN_ZOMBIE_SPAWN_EGG = spawnEgg("frozen_zombie", ModEntities.FROZEN_ZOMBIE);
	public static final DeferredItem<Item> ENDER_ZOMBIE_SPAWN_EGG = spawnEgg("ender_zombie", ModEntities.ENDER_ZOMBIE);
	public static final DeferredItem<Item> WITHER_ZOMBIE_SPAWN_EGG = spawnEgg("wither_zombie", ModEntities.WITHER_ZOMBIE);
	public static final DeferredItem<Item> SPRINTER_ZOMBIE_SPAWN_EGG = spawnEgg("sprinter_zombie", ModEntities.SPRINTER_ZOMBIE);
	public static final DeferredItem<Item> BUILDER_ZOMBIE_SPAWN_EGG = spawnEgg("builder_zombie", ModEntities.BUILDER_ZOMBIE);
	public static final DeferredItem<Item> PLAGUE_ZOMBIE_SPAWN_EGG = spawnEgg("plague_zombie", ModEntities.PLAGUE_ZOMBIE);
	public static final DeferredItem<Item> ARMORED_ZOMBIE_SPAWN_EGG = spawnEgg("armored_zombie", ModEntities.ARMORED_ZOMBIE);
	public static final DeferredItem<Item> BLOSSOM_ZOMBIE_SPAWN_EGG = spawnEgg("blossom_zombie", ModEntities.BLOSSOM_ZOMBIE);

	// spiders
	public static final DeferredItem<Item> SPITTING_SPIDER_SPAWN_EGG = spawnEgg("spitting_spider", ModEntities.SPITTING_SPIDER);
	public static final DeferredItem<Item> WITHER_SPIDER_SPAWN_EGG = spawnEgg("wither_spider", ModEntities.WITHER_SPIDER);
	public static final DeferredItem<Item> MAGMA_SPIDER_SPAWN_EGG = spawnEgg("magma_spider", ModEntities.MAGMA_SPIDER);
	public static final DeferredItem<Item> ICE_SPIDER_SPAWN_EGG = spawnEgg("ice_spider", ModEntities.ICE_SPIDER);
	public static final DeferredItem<Item> JUMPING_SPIDER_SPAWN_EGG = spawnEgg("jumping_spider", ModEntities.JUMPING_SPIDER);
	public static final DeferredItem<Item> WEB_SPINNER_SPIDER_SPAWN_EGG = spawnEgg("web_spinner_spider", ModEntities.WEB_SPINNER_SPIDER);

	// skeletons
	public static final DeferredItem<Item> SNIPER_SKELETON_SPAWN_EGG = spawnEgg("sniper_skeleton", ModEntities.SNIPER_SKELETON);
	public static final DeferredItem<Item> EMBER_SKELETON_SPAWN_EGG = spawnEgg("ember_skeleton", ModEntities.EMBER_SKELETON);
	public static final DeferredItem<Item> ENDER_SKELETON_SPAWN_EGG = spawnEgg("ender_skeleton", ModEntities.ENDER_SKELETON);
	public static final DeferredItem<Item> POISON_SKELETON_SPAWN_EGG = spawnEgg("poison_skeleton", ModEntities.POISON_SKELETON);
	public static final DeferredItem<Item> MULTISHOT_SKELETON_SPAWN_EGG = spawnEgg("multishot_skeleton", ModEntities.MULTISHOT_SKELETON);
	public static final DeferredItem<Item> BLOSSOM_SKELETON_SPAWN_EGG = spawnEgg("blossom_skeleton", ModEntities.BLOSSOM_SKELETON);

	// creepers
	public static final DeferredItem<Item> TOXIC_CREEPER_SPAWN_EGG = spawnEgg("toxic_creeper", ModEntities.TOXIC_CREEPER);
	public static final DeferredItem<Item> WITHER_CREEPER_SPAWN_EGG = spawnEgg("wither_creeper", ModEntities.WITHER_CREEPER);
	public static final DeferredItem<Item> SCULK_CREEPER_SPAWN_EGG = spawnEgg("sculk_creeper", ModEntities.SCULK_CREEPER);
	public static final DeferredItem<Item> MAGMA_CREEPER_SPAWN_EGG = spawnEgg("magma_creeper", ModEntities.MAGMA_CREEPER);
	public static final DeferredItem<Item> FROST_CREEPER_SPAWN_EGG = spawnEgg("frost_creeper", ModEntities.FROST_CREEPER);
	public static final DeferredItem<Item> ENDER_CREEPER_SPAWN_EGG = spawnEgg("ender_creeper", ModEntities.ENDER_CREEPER);
	public static final DeferredItem<Item> LIGHTNING_CREEPER_SPAWN_EGG = spawnEgg("lightning_creeper", ModEntities.LIGHTNING_CREEPER);
	public static final DeferredItem<Item> BURROWING_CREEPER_SPAWN_EGG = spawnEgg("burrowing_creeper", ModEntities.BURROWING_CREEPER);
	public static final DeferredItem<Item> BLOSSOM_CREEPER_SPAWN_EGG = spawnEgg("blossom_creeper", ModEntities.BLOSSOM_CREEPER);

	// endermen
	public static final DeferredItem<Item> ASSASSIN_ENDERMAN_SPAWN_EGG = spawnEgg("assassin_enderman", ModEntities.ASSASSIN_ENDERMAN);
	public static final DeferredItem<Item> ENRAGED_ENDERMAN_SPAWN_EGG = spawnEgg("enraged_enderman", ModEntities.ENRAGED_ENDERMAN);
	public static final DeferredItem<Item> BLOSSOM_ENDERMAN_SPAWN_EGG = spawnEgg("blossom_enderman", ModEntities.BLOSSOM_ENDERMAN);

	// blazes
	public static final DeferredItem<Item> BLAST_BLAZE_SPAWN_EGG = spawnEgg("blast_blaze", ModEntities.BLAST_BLAZE);
	public static final DeferredItem<Item> STORM_BLAZE_SPAWN_EGG = spawnEgg("storm_blaze", ModEntities.STORM_BLAZE);
	public static final DeferredItem<Item> WITHER_BLAZE_SPAWN_EGG = spawnEgg("wither_blaze", ModEntities.WITHER_BLAZE);
	public static final DeferredItem<Item> SOUL_BLAZE_SPAWN_EGG = spawnEgg("soul_blaze", ModEntities.SOUL_BLAZE);
	public static final DeferredItem<Item> BRAND_BLAZE_SPAWN_EGG = spawnEgg("brand_blaze", ModEntities.BRAND_BLAZE);

	// ghasts
	public static final DeferredItem<Item> GREAT_MOTHER_GHAST_SPAWN_EGG = spawnEgg("great_mother_ghast", ModEntities.GREAT_MOTHER_GHAST);
	public static final DeferredItem<Item> DELTA_GHAST_SPAWN_EGG = spawnEgg("delta_ghast", ModEntities.DELTA_GHAST);
	public static final DeferredItem<Item> WITHER_GHAST_SPAWN_EGG = spawnEgg("wither_ghast", ModEntities.WITHER_GHAST);
	public static final DeferredItem<Item> RAGELING_SPAWN_EGG = spawnEgg("rageling", ModEntities.RAGELING);
	public static final DeferredItem<Item> SKITTERLING_SPAWN_EGG = spawnEgg("skitterling", ModEntities.SKITTERLING);
	public static final DeferredItem<Item> OBSIDLING_SPAWN_EGG = spawnEgg("obsidling", ModEntities.OBSIDLING);
	public static final DeferredItem<Item> BLIGHTLING_SPAWN_EGG = spawnEgg("blightling", ModEntities.BLIGHTLING);

	private static final ResourceLocation TAB_ID = ResourceLocation.fromNamespaceAndPath(UniqueMobs.MOD_ID, "unique_mobs");

	private static DeferredItem<Item> spawnEgg(String mobName, Supplier<? extends EntityType<? extends Mob>> entityType) {
		DeferredItem<Item> item = ITEMS.register(mobName + "_spawn_egg",
			() -> new DeferredSpawnEggItem(entityType, 0x000000, 0x000000, new Item.Properties()));
		SPAWN_EGGS.add(item);
		return item;
	}

	public static void register(IEventBus bus) {
		ITEMS.register(bus);
		bus.addListener(ModItems::registerCreativeTabs);
	}

	private static void registerCreativeTabs(RegisterEvent event) {
		event.register(Registries.CREATIVE_MODE_TAB, helper -> helper.register(TAB_ID,
			CreativeModeTab.builder()
				.title(Component.translatable("itemGroup." + UniqueMobs.MOD_ID))
				.icon(() -> new ItemStack(VENOMOUS_ZOMBIE_SPAWN_EGG.get()))
				.displayItems((params, output) -> {
					for (DeferredItem<Item> egg : SPAWN_EGGS) {
						output.accept(egg.get());
					}
				})
				.build()
		));
	}
}

