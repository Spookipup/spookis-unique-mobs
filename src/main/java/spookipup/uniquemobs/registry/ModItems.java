package spookipup.uniquemobs.registry;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.entity.Mob;

import java.util.ArrayList;
import java.util.List;

import spookipup.uniquemobs.UniqueMobs;

public class ModItems {

	private static final List<Item> SPAWN_EGGS = new ArrayList<>();

	// zombies
	public static final Item VENOMOUS_ZOMBIE_SPAWN_EGG = spawnEgg("venomous_zombie", ModEntities.VENOMOUS_ZOMBIE);
	public static final Item INFERNAL_ZOMBIE_SPAWN_EGG = spawnEgg("infernal_zombie", ModEntities.INFERNAL_ZOMBIE);
	public static final Item FROZEN_ZOMBIE_SPAWN_EGG = spawnEgg("frozen_zombie", ModEntities.FROZEN_ZOMBIE);
	public static final Item ENDER_ZOMBIE_SPAWN_EGG = spawnEgg("ender_zombie", ModEntities.ENDER_ZOMBIE);
	public static final Item WITHER_ZOMBIE_SPAWN_EGG = spawnEgg("wither_zombie", ModEntities.WITHER_ZOMBIE);
	public static final Item SPRINTER_ZOMBIE_SPAWN_EGG = spawnEgg("sprinter_zombie", ModEntities.SPRINTER_ZOMBIE);
	public static final Item BUILDER_ZOMBIE_SPAWN_EGG = spawnEgg("builder_zombie", ModEntities.BUILDER_ZOMBIE);
	public static final Item PLAGUE_ZOMBIE_SPAWN_EGG = spawnEgg("plague_zombie", ModEntities.PLAGUE_ZOMBIE);
	public static final Item ARMORED_ZOMBIE_SPAWN_EGG = spawnEgg("armored_zombie", ModEntities.ARMORED_ZOMBIE);

	// spiders
	public static final Item SPITTING_SPIDER_SPAWN_EGG = spawnEgg("spitting_spider", ModEntities.SPITTING_SPIDER);
	public static final Item WITHER_SPIDER_SPAWN_EGG = spawnEgg("wither_spider", ModEntities.WITHER_SPIDER);
	public static final Item MAGMA_SPIDER_SPAWN_EGG = spawnEgg("magma_spider", ModEntities.MAGMA_SPIDER);
	public static final Item ICE_SPIDER_SPAWN_EGG = spawnEgg("ice_spider", ModEntities.ICE_SPIDER);
	public static final Item JUMPING_SPIDER_SPAWN_EGG = spawnEgg("jumping_spider", ModEntities.JUMPING_SPIDER);
	public static final Item WEB_SPINNER_SPIDER_SPAWN_EGG = spawnEgg("web_spinner_spider", ModEntities.WEB_SPINNER_SPIDER);

	// skeletons
	public static final Item SNIPER_SKELETON_SPAWN_EGG = spawnEgg("sniper_skeleton", ModEntities.SNIPER_SKELETON);
	public static final Item EMBER_SKELETON_SPAWN_EGG = spawnEgg("ember_skeleton", ModEntities.EMBER_SKELETON);
	public static final Item ENDER_SKELETON_SPAWN_EGG = spawnEgg("ender_skeleton", ModEntities.ENDER_SKELETON);
	public static final Item POISON_SKELETON_SPAWN_EGG = spawnEgg("poison_skeleton", ModEntities.POISON_SKELETON);
	public static final Item MULTISHOT_SKELETON_SPAWN_EGG = spawnEgg("multishot_skeleton", ModEntities.MULTISHOT_SKELETON);

	// creepers
	public static final Item TOXIC_CREEPER_SPAWN_EGG = spawnEgg("toxic_creeper", ModEntities.TOXIC_CREEPER);
	public static final Item WITHER_CREEPER_SPAWN_EGG = spawnEgg("wither_creeper", ModEntities.WITHER_CREEPER);
	public static final Item SCULK_CREEPER_SPAWN_EGG = spawnEgg("sculk_creeper", ModEntities.SCULK_CREEPER);
	public static final Item MAGMA_CREEPER_SPAWN_EGG = spawnEgg("magma_creeper", ModEntities.MAGMA_CREEPER);
	public static final Item FROST_CREEPER_SPAWN_EGG = spawnEgg("frost_creeper", ModEntities.FROST_CREEPER);
	public static final Item ENDER_CREEPER_SPAWN_EGG = spawnEgg("ender_creeper", ModEntities.ENDER_CREEPER);
	public static final Item LIGHTNING_CREEPER_SPAWN_EGG = spawnEgg("lightning_creeper", ModEntities.LIGHTNING_CREEPER);
	public static final Item BURROWING_CREEPER_SPAWN_EGG = spawnEgg("burrowing_creeper", ModEntities.BURROWING_CREEPER);

	// endermen
	public static final Item ASSASSIN_ENDERMAN_SPAWN_EGG = spawnEgg("assassin_enderman", ModEntities.ASSASSIN_ENDERMAN);
	public static final Item ENRAGED_ENDERMAN_SPAWN_EGG = spawnEgg("enraged_enderman", ModEntities.ENRAGED_ENDERMAN);

	private static final ResourceLocation TAB_ID = new ResourceLocation(UniqueMobs.MOD_ID, "unique_mobs");

	private static Item spawnEgg(String mobName, EntityType<? extends Mob> entityType) {
		ResourceLocation id = new ResourceLocation(UniqueMobs.MOD_ID, mobName + "_spawn_egg");
		Item item = new SpawnEggItem(entityType, 0x000000, 0x000000, new Item.Properties());
		Item registered = Registry.register(BuiltInRegistries.ITEM, id, item);
		SPAWN_EGGS.add(registered);
		return registered;
	}

	public static void init() {
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TAB_ID,
			FabricItemGroup.builder()
				.title(Component.translatable("itemGroup." + UniqueMobs.MOD_ID))
				.icon(() -> new ItemStack(VENOMOUS_ZOMBIE_SPAWN_EGG))
				.displayItems((params, output) -> {
					for (Item egg : SPAWN_EGGS) {
						output.accept(egg);
					}
				})
				.build()
		);
	}
}
