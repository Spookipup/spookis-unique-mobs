package spookipup.uniquemobs.spawn;

import net.minecraft.world.entity.EntityType;
import net.minecraft.util.RandomSource;
import spookipup.uniquemobs.config.ModConfig;

import java.util.function.Supplier;

public final class ConfigWeightedSelection {

	public record WeightedType(Supplier<? extends EntityType<?>> type, int baseWeight) {}

	private ConfigWeightedSelection() {}

	@SafeVarargs
	public static EntityType<?> pickEntityType(RandomSource random,
											  Supplier<? extends EntityType<?>> fallback,
											  Supplier<? extends EntityType<?>>... candidates) {
		ModConfig cfg = ModConfig.get();
		int total = 0;

		for (Supplier<? extends EntityType<?>> candidate : candidates) {
			total += candidateWeight(cfg, candidate.get());
		}

		if (total <= 0) {
			return fallback.get();
		}

		int pick = random.nextInt(total);
		for (Supplier<? extends EntityType<?>> candidate : candidates) {
			EntityType<?> type = candidate.get();
			int weight = candidateWeight(cfg, type);
			if (weight <= 0) continue;
			if (pick < weight) {
				return type;
			}
			pick -= weight;
		}

		return fallback.get();
	}

	public static EntityType<?> pickWeightedEntityType(RandomSource random,
													   Supplier<? extends EntityType<?>> fallback,
													   WeightedType... candidates) {
		ModConfig cfg = ModConfig.get();
		int total = 0;

		for (WeightedType candidate : candidates) {
			total += candidateWeight(cfg, candidate.type().get(), candidate.baseWeight());
		}

		if (total <= 0) {
			return fallback == null ? null : fallback.get();
		}

		int pick = random.nextInt(total);
		for (WeightedType candidate : candidates) {
			EntityType<?> type = candidate.type().get();
			int weight = candidateWeight(cfg, type, candidate.baseWeight());
			if (weight <= 0) continue;
			if (pick < weight) {
				return type;
			}
			pick -= weight;
		}

		return fallback == null ? null : fallback.get();
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


