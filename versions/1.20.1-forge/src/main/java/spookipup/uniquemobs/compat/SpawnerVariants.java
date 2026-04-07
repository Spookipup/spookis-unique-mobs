package spookipup.uniquemobs.compat;

import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import spookipup.uniquemobs.spawn.BlazeStructureSpawnHelper;
import spookipup.uniquemobs.config.ModConfig;
import spookipup.uniquemobs.registry.ModEntities;

// biome-aware spawner variant selection for structure processor compat
public class SpawnerVariants {

	// entity-type level replacement for piece-based structures (spider dungeons)
	// that set spawners directly instead of going through processors
	public static EntityType<?> tryReplaceType(EntityType<?> vanilla, Holder<Biome> biome, RandomSource random) {
		if (vanilla != EntityType.ZOMBIE && vanilla != EntityType.SKELETON
			&& vanilla != EntityType.SPIDER && vanilla != EntityType.CAVE_SPIDER) {
			return vanilla;
		}

		boolean themed = isBiomeThemed(biome);
		ModConfig cfg = ModConfig.get();
		float chance = themed
			? (float) cfg.dungeonThemedReplacementChance
			: (float) cfg.dungeonReplacementChance;
		if (random.nextFloat() >= chance) return vanilla;

		EntityType<?> variant = pickVariant(vanilla, biome, random);
		if (variant == vanilla) return vanilla;

		String variantId = EntityType.getKey(variant).getPath();
		if (!cfg.isMobEnabled(variantId)) return vanilla;

		return variant;
	}

	public static StructureTemplate.StructureBlockInfo tryReplace(
		StructureTemplate.StructureBlockInfo info, Holder<Biome> biome, RandomSource random
	) {
		if (info.nbt() == null || !info.state().is(Blocks.SPAWNER)) return info;

		String entityId = readEntityId(info.nbt());
		if (entityId == null) return info;

		EntityType<?> vanilla = entityIdToType(entityId);
		if (vanilla == null) return info;

		boolean themed = isBiomeThemed(biome);
		ModConfig cfg = ModConfig.get();
		float chance = themed
			? (float) cfg.dungeonThemedReplacementChance
			: (float) cfg.dungeonReplacementChance;
		if (random.nextFloat() >= chance) return info;

		EntityType<?> variant = pickVariant(vanilla, biome, random);
		if (variant == vanilla) return info;

		String variantId = EntityType.getKey(variant).getPath();
		if (!cfg.isMobEnabled(variantId)) return info;

		String fullId = EntityType.getKey(variant).toString();
		CompoundTag newNbt = rewriteSpawnerEntity(info.nbt().copy(), fullId);
		return new StructureTemplate.StructureBlockInfo(info.pos(), info.state(), newNbt);
	}

	public static StructureTemplate.StructureBlockInfo tryReplaceFortressBlazeSpawner(
		StructureTemplate.StructureBlockInfo info, Holder<Biome> biome, RandomSource random
	) {
		if (info.nbt() == null || !info.state().is(Blocks.SPAWNER)) return info;

		String entityId = readEntityId(info.nbt());
		if (!"minecraft:blaze".equals(entityId)) return info;

		ModConfig cfg = ModConfig.get();
		if (random.nextFloat() >= (float) cfg.fortressSpawnerReplacementChance) return info;

		EntityType<?> variant = BlazeStructureSpawnHelper.pickFortressSpawnerVariant(cfg, random);
		if (variant == null || variant == EntityType.BLAZE) return info;

		String fullId = EntityType.getKey(variant).toString();
		CompoundTag newNbt = rewriteSpawnerEntity(info.nbt().copy(), fullId);
		return new StructureTemplate.StructureBlockInfo(info.pos(), info.state(), newNbt);
	}

	private static String readEntityId(CompoundTag nbt) {
		if (nbt.contains("SpawnData")) {
			CompoundTag spawnData = nbt.getCompound("SpawnData");
			if (spawnData.contains("entity")) {
				return spawnData.getCompound("entity").getString("id");
			}
		}
		return null;
	}

	private static EntityType<?> entityIdToType(String id) {
		return switch (id) {
			case "minecraft:zombie" -> EntityType.ZOMBIE;
			case "minecraft:skeleton" -> EntityType.SKELETON;
			case "minecraft:spider" -> EntityType.SPIDER;
			case "minecraft:cave_spider" -> EntityType.CAVE_SPIDER;
			default -> null;
		};
	}

	// rewrites all entity id references in a spawner's nbt
	private static CompoundTag rewriteSpawnerEntity(CompoundTag nbt, String newId) {
		if (nbt.contains("SpawnData")) {
			CompoundTag spawnData = nbt.getCompound("SpawnData");
			if (spawnData.contains("entity")) {
				spawnData.getCompound("entity").putString("id", newId);
			}
		}
		if (nbt.contains("SpawnPotentials")) {
			ListTag potentials = nbt.getList("SpawnPotentials", 10);
			for (int i = 0; i < potentials.size(); i++) {
				CompoundTag entry = potentials.getCompound(i);
				if (entry.contains("data")) {
					CompoundTag data = entry.getCompound("data");
					if (data.contains("entity")) {
						data.getCompound("entity").putString("id", newId);
					}
				}
			}
		}
		return nbt;
	}

	private static EntityType<?> pickVariant(EntityType<?> vanilla, Holder<Biome> biome, RandomSource random) {
		if (vanilla == EntityType.ZOMBIE) return pickZombie(biome, random);
		if (vanilla == EntityType.SKELETON) return pickSkeleton(biome, random);
		if (vanilla == EntityType.SPIDER || vanilla == EntityType.CAVE_SPIDER) return pickSpider(biome, random);
		return vanilla;
	}

	private static boolean isBiomeThemed(Holder<Biome> biome) {
		return biome.is(BiomeTags.SPAWNS_SNOW_FOXES)
			|| biome.is(BiomeTags.HAS_DESERT_PYRAMID)
			|| biome.is(BiomeTags.IS_BADLANDS)
			|| biome.is(BiomeTags.IS_JUNGLE)
			|| biome.is(BiomeTags.IS_FOREST)
			|| biome.is(Biomes.SWAMP)
			|| biome.is(Biomes.MANGROVE_SWAMP)
			|| biome.is(Biomes.LUSH_CAVES)
			|| biome.is(Biomes.DRIPSTONE_CAVES);
	}

	private static boolean isSwamp(Holder<Biome> biome) {
		return biome.is(Biomes.SWAMP) || biome.is(Biomes.MANGROVE_SWAMP);
	}

	private static EntityType<?> pickZombie(Holder<Biome> biome, RandomSource random) {
		if (random.nextInt(10) == 0) return ModEntities.ENDER_ZOMBIE.get();
		if (biome.is(BiomeTags.SPAWNS_SNOW_FOXES)) return ModEntities.FROZEN_ZOMBIE.get();
		if (biome.is(BiomeTags.HAS_DESERT_PYRAMID) || biome.is(BiomeTags.IS_BADLANDS)) return ModEntities.INFERNAL_ZOMBIE.get();
		if (biome.is(BiomeTags.IS_JUNGLE)) {
			return random.nextBoolean() ? ModEntities.VENOMOUS_ZOMBIE.get() : ModEntities.PLAGUE_ZOMBIE.get();
		}
		if (isSwamp(biome)) {
			return random.nextBoolean() ? ModEntities.PLAGUE_ZOMBIE.get() : ModEntities.VENOMOUS_ZOMBIE.get();
		}
		if (biome.is(Biomes.LUSH_CAVES) || biome.is(Biomes.DRIPSTONE_CAVES)) {
			return random.nextBoolean() ? ModEntities.VENOMOUS_ZOMBIE.get() : ModEntities.PLAGUE_ZOMBIE.get();
		}
		return switch (random.nextInt(4)) {
			case 0 -> ModEntities.SPRINTER_ZOMBIE.get();
			case 1 -> ModEntities.ARMORED_ZOMBIE.get();
			case 2 -> ModEntities.VENOMOUS_ZOMBIE.get();
			default -> ModEntities.PLAGUE_ZOMBIE.get();
		};
	}

	private static EntityType<?> pickSkeleton(Holder<Biome> biome, RandomSource random) {
		if (random.nextInt(10) == 0) return ModEntities.ENDER_SKELETON.get();
		if (biome.is(BiomeTags.HAS_DESERT_PYRAMID) || biome.is(BiomeTags.IS_BADLANDS)) return ModEntities.EMBER_SKELETON.get();
		if (biome.is(BiomeTags.IS_JUNGLE) || biome.is(BiomeTags.IS_FOREST) || isSwamp(biome)) return ModEntities.POISON_SKELETON.get();
		return switch (random.nextInt(3)) {
			case 0 -> ModEntities.MULTISHOT_SKELETON.get();
			case 1 -> ModEntities.SNIPER_SKELETON.get();
			default -> ModEntities.POISON_SKELETON.get();
		};
	}

	private static EntityType<?> pickSpider(Holder<Biome> biome, RandomSource random) {
		if (biome.is(BiomeTags.SPAWNS_SNOW_FOXES)) return ModEntities.ICE_SPIDER.get();
		if (biome.is(BiomeTags.HAS_DESERT_PYRAMID) || biome.is(BiomeTags.IS_BADLANDS)) return ModEntities.MAGMA_SPIDER.get();
		if (biome.is(BiomeTags.IS_JUNGLE)) {
			return random.nextBoolean() ? ModEntities.JUMPING_SPIDER.get() : ModEntities.SPITTING_SPIDER.get();
		}
		if (biome.is(BiomeTags.IS_FOREST)) {
			return random.nextBoolean() ? ModEntities.JUMPING_SPIDER.get() : ModEntities.WEB_SPINNER_SPIDER.get();
		}
		if (isSwamp(biome)) {
			return random.nextBoolean() ? ModEntities.SPITTING_SPIDER.get() : ModEntities.WEB_SPINNER_SPIDER.get();
		}
		if (biome.is(Biomes.LUSH_CAVES) || biome.is(Biomes.DRIPSTONE_CAVES)) {
			return random.nextBoolean() ? ModEntities.WEB_SPINNER_SPIDER.get() : ModEntities.SPITTING_SPIDER.get();
		}
		return switch (random.nextInt(3)) {
			case 0 -> ModEntities.SPITTING_SPIDER.get();
			case 1 -> ModEntities.WEB_SPINNER_SPIDER.get();
			default -> ModEntities.JUMPING_SPIDER.get();
		};
	}
}
