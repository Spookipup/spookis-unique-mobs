package spookipup.uniquemobs.spawn;

import net.minecraft.world.entity.EntityType;
import net.minecraft.util.RandomSource;
import spookipup.uniquemobs.config.ModConfig;

public final class ConfigWeightedSelection {

	public record WeightedType(EntityType<?> type, int baseWeight) {}

	private ConfigWeightedSelection() {}

	public static EntityType<?> pickEntityType(RandomSource random, EntityType<?> fallback, EntityType<?>... candidates) {
		ModConfig cfg = ModConfig.get();
		int total = 0;

		for (EntityType<?> candidate : candidates) {
			total += candidateWeight(cfg, candidate);
		}

		if (total <= 0) {
			return fallback;
		}

		int pick = random.nextInt(total);
		for (EntityType<?> candidate : candidates) {
			int weight = candidateWeight(cfg, candidate);
			if (weight <= 0) continue;
			if (pick < weight) {
				return candidate;
			}
			pick -= weight;
		}

		return fallback;
	}

	public static EntityType<?> pickWeightedEntityType(RandomSource random, EntityType<?> fallback, WeightedType... candidates) {
		ModConfig cfg = ModConfig.get();
		int total = 0;

		for (WeightedType candidate : candidates) {
			total += candidateWeight(cfg, candidate.type(), candidate.baseWeight());
		}

		if (total <= 0) {
			return fallback;
		}

		int pick = random.nextInt(total);
		for (WeightedType candidate : candidates) {
			int weight = candidateWeight(cfg, candidate.type(), candidate.baseWeight());
			if (weight <= 0) continue;
			if (pick < weight) {
				return candidate.type();
			}
			pick -= weight;
		}

		return fallback;
	}

	private static int candidateWeight(ModConfig cfg, EntityType<?> type) {
		return candidateWeight(cfg, type, 1);
	}

	private static int candidateWeight(ModConfig cfg, EntityType<?> type, int baseWeight) {
		String id = EntityType.getKey(type).getPath();
		if (!cfg.isMobEnabled(id)) {
			return 0;
		}
		return Math.max(0, cfg.adjustWeight(id, baseWeight));
	}
}
