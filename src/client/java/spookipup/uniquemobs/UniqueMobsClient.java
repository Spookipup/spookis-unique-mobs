package spookipup.uniquemobs;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.resources.Identifier;
import spookipup.uniquemobs.registry.ModEntities;
import spookipup.uniquemobs.renderer.RetexturedCreeperRenderer;
import spookipup.uniquemobs.renderer.RetexturedEndermanRenderer;
import spookipup.uniquemobs.renderer.DeltaGhastRenderer;
import spookipup.uniquemobs.renderer.StormArcRenderer;
import spookipup.uniquemobs.renderer.WitherGhastRenderer;
import spookipup.uniquemobs.renderer.RetexturedGhastRenderer;
import spookipup.uniquemobs.renderer.RetexturedBlazeRenderer;
import spookipup.uniquemobs.renderer.RetexturedSkeletonRenderer;
import spookipup.uniquemobs.renderer.RetexturedSpiderRenderer;
import spookipup.uniquemobs.renderer.RetexturedZombieRenderer;
import spookipup.uniquemobs.renderer.WitherBlazeRenderer;
import spookipup.uniquemobs.renderer.WitherZombieModel;
import spookipup.uniquemobs.renderer.WitherZombieRenderer;

public class UniqueMobsClient implements ClientModInitializer {

	private static final String ID = UniqueMobs.MOD_ID;

	@SuppressWarnings("unchecked")
	@Override
	public void onInitializeClient() {
		// model layers
		ModelLayerRegistry.registerModelLayer(
			WitherZombieModel.LAYER_LOCATION, WitherZombieModel::createBodyLayer);

		// zombies
		EntityRenderers.register(ModEntities.VENOMOUS_ZOMBIE,
			ctx -> new RetexturedZombieRenderer(ctx, zombieTex("venomous_zombie"), null));
		EntityRenderers.register(ModEntities.WITHER_ZOMBIE, WitherZombieRenderer::new);
		EntityRenderers.register(ModEntities.INFERNAL_ZOMBIE,
			ctx -> new RetexturedZombieRenderer(ctx, zombieTex("infernal_zombie"), zombieTex("infernal_zombie_eyes")));
		EntityRenderers.register(ModEntities.FROZEN_ZOMBIE,
			ctx -> new RetexturedZombieRenderer(ctx, zombieTex("frozen_zombie"), null));
		EntityRenderers.register(ModEntities.ENDER_ZOMBIE,
			ctx -> new RetexturedZombieRenderer(ctx, zombieTex("ender_zombie"), zombieTex("ender_zombie_eyes")));
		EntityRenderers.register(ModEntities.SPRINTER_ZOMBIE,
			ctx -> new RetexturedZombieRenderer(ctx, zombieTex("sprinter_zombie"), null));
		EntityRenderers.register(ModEntities.BUILDER_ZOMBIE,
			ctx -> new RetexturedZombieRenderer(ctx, zombieTex("builder_zombie"), null));
		EntityRenderers.register(ModEntities.PLAGUE_ZOMBIE,
			ctx -> new RetexturedZombieRenderer(ctx, zombieTex("plague_zombie"), null));
		EntityRenderers.register(ModEntities.ARMORED_ZOMBIE,
			ctx -> new RetexturedZombieRenderer(ctx, zombieTex("armored_zombie"), null));

		// creepers
		EntityRenderers.register(ModEntities.TOXIC_CREEPER,
			ctx -> new RetexturedCreeperRenderer(ctx, creeperTex("toxic_creeper"), null));
		EntityRenderers.register(ModEntities.WITHER_CREEPER,
			ctx -> new RetexturedCreeperRenderer(ctx, creeperTex("wither_creeper"), creeperTex("wither_creeper_eyes")));
		EntityRenderers.register(ModEntities.SCULK_CREEPER,
			ctx -> new RetexturedCreeperRenderer(ctx, creeperTex("sculk_creeper"), creeperTex("sculk_creeper_eyes")));
		EntityRenderers.register(ModEntities.FROST_CREEPER,
			ctx -> new RetexturedCreeperRenderer(ctx, creeperTex("frost_creeper"), null));
		EntityRenderers.register(ModEntities.MAGMA_CREEPER,
			ctx -> new RetexturedCreeperRenderer(ctx, creeperTex("magma_creeper"), creeperTex("magma_creeper_eyes")));
		EntityRenderers.register(ModEntities.ENDER_CREEPER,
			ctx -> new RetexturedCreeperRenderer(ctx, creeperTex("ender_creeper"), creeperTex("ender_creeper_eyes")));
		EntityRenderers.register(ModEntities.LIGHTNING_CREEPER,
			ctx -> new RetexturedCreeperRenderer(ctx, creeperTex("lightning_creeper"), null));
		EntityRenderers.register(ModEntities.BURROWING_CREEPER,
			ctx -> new RetexturedCreeperRenderer(ctx, creeperTex("burrowing_creeper"), null));

		// spiders
		EntityRenderers.register(ModEntities.SPITTING_SPIDER,
			ctx -> new RetexturedSpiderRenderer(ctx, spiderTex("spitting_spider"), spiderTex("spitting_spider_eyes")));
		EntityRenderers.register(ModEntities.WITHER_SPIDER,
			ctx -> new RetexturedSpiderRenderer(ctx, spiderTex("wither_spider"), spiderTex("wither_spider_eyes")));
		EntityRenderers.register(ModEntities.MAGMA_SPIDER,
			ctx -> new RetexturedSpiderRenderer(ctx, spiderTex("magma_spider"), spiderTex("magma_spider_eyes")));
		EntityRenderers.register(ModEntities.ICE_SPIDER,
			ctx -> new RetexturedSpiderRenderer(ctx, spiderTex("ice_spider"), spiderTex("ice_spider_eyes")));
		EntityRenderers.register(ModEntities.JUMPING_SPIDER,
			ctx -> new RetexturedSpiderRenderer(ctx, spiderTex("jumping_spider"), spiderTex("jumping_spider_eyes")));
		EntityRenderers.register(ModEntities.WEB_SPINNER_SPIDER,
			ctx -> new RetexturedSpiderRenderer(ctx, spiderTex("web_spinner_spider"), spiderTex("web_spinner_spider_eyes")));

		// skeletons
		EntityRenderers.register(ModEntities.SNIPER_SKELETON,
			ctx -> new RetexturedSkeletonRenderer(ctx, skeletonTex("sniper_skeleton"), null));
		EntityRenderers.register(ModEntities.EMBER_SKELETON,
			ctx -> new RetexturedSkeletonRenderer(ctx, skeletonTex("ember_skeleton"), skeletonTex("ember_skeleton_eyes")));
		EntityRenderers.register(ModEntities.ENDER_SKELETON,
			ctx -> new RetexturedSkeletonRenderer(ctx, skeletonTex("ender_skeleton"), skeletonTex("ender_skeleton_eyes")));
		EntityRenderers.register(ModEntities.POISON_SKELETON,
			ctx -> new RetexturedSkeletonRenderer(ctx, skeletonTex("poison_skeleton"), null));
		EntityRenderers.register(ModEntities.MULTISHOT_SKELETON,
			ctx -> new RetexturedSkeletonRenderer(ctx, skeletonTex("multishot_skeleton"), null));

		// endermen
		EntityRenderers.register(ModEntities.ASSASSIN_ENDERMAN,
			ctx -> new RetexturedEndermanRenderer(ctx, endermanTex("assassin_enderman"), endermanTex("assassin_enderman_eyes")));
		EntityRenderers.register(ModEntities.ENRAGED_ENDERMAN,
			ctx -> new RetexturedEndermanRenderer(ctx, endermanTex("enraged_enderman"), endermanTex("enraged_enderman_eyes")));

		// blazes
		EntityRenderers.register(ModEntities.BLAST_BLAZE,
			ctx -> new RetexturedBlazeRenderer(ctx, blazeTex("blast_blaze")));
		EntityRenderers.register(ModEntities.STORM_BLAZE,
			ctx -> new RetexturedBlazeRenderer(ctx, blazeTex("storm_blaze")));
		EntityRenderers.register(ModEntities.WITHER_BLAZE,
			ctx -> new WitherBlazeRenderer(ctx, blazeTex("wither_blaze")));
		EntityRenderers.register(ModEntities.SOUL_BLAZE,
			ctx -> new RetexturedBlazeRenderer(ctx, blazeTex("soul_blaze")));
		EntityRenderers.register(ModEntities.BRAND_BLAZE,
			ctx -> new RetexturedBlazeRenderer(ctx, blazeTex("brand_blaze")));

		// ghasts
		EntityRenderers.register(ModEntities.GREAT_MOTHER_GHAST,
			ctx -> new RetexturedGhastRenderer(ctx, ghastTex("great_mother_ghast"), ghastTex("great_mother_ghast_shooting"), null));
		EntityRenderers.register(ModEntities.DELTA_GHAST,
			ctx -> new DeltaGhastRenderer(ctx, ghastTex("delta_ghast"), ghastTex("delta_ghast_charging"),
				ghastTex("delta_ghast"), ghastTex("delta_ghast_charging")));
		EntityRenderers.register(ModEntities.WITHER_GHAST,
			ctx -> new WitherGhastRenderer(ctx, ghastTex("wither_ghast"), ghastTex("wither_ghast_charging"),
				null, null));
		// fullbright glow that swaps with the face
		EntityRenderers.register(ModEntities.RAGELING,
			ctx -> new RetexturedGhastRenderer(ctx, ghastTex("rageling"), ghastTex("rageling_attacking"),
				ghastTex("rageling"), ghastTex("rageling_attacking")));
		EntityRenderers.register(ModEntities.SKITTERLING,
			ctx -> new RetexturedGhastRenderer(ctx, ghastTex("skitterling"), ghastTex("skitterling_charging"),
				ghastTex("skitterling"), ghastTex("skitterling_charging")));
		EntityRenderers.register(ModEntities.OBSIDLING,
			ctx -> new RetexturedGhastRenderer(ctx, ghastTex("obsidling"), ghastTex("obsidling_attacking"),
				ghastTex("obsidling"), ghastTex("obsidling_attacking")));
		EntityRenderers.register(ModEntities.BLIGHTLING,
			ctx -> new RetexturedGhastRenderer(ctx, ghastTex("blightling"), ghastTex("blightling_attacking"),
				ghastTex("blightling"), ghastTex("blightling_attacking")));

		// projectiles
		EntityRenderers.register(ModEntities.WEB_PROJECTILE, ThrownItemRenderer::new);
		EntityRenderers.register(ModEntities.FREEZE_SNOWBALL, ThrownItemRenderer::new);
		EntityRenderers.register(ModEntities.POISON_SPIT, ThrownItemRenderer::new);
		EntityRenderers.register(ModEntities.BLIGHT_SPORE, ThrownItemRenderer::new);
		EntityRenderers.register(ModEntities.WITHER_ASH_BOLT, ThrownItemRenderer::new);
		EntityRenderers.register(ModEntities.STORM_ARC, StormArcRenderer::new);
		EntityRenderers.register(ModEntities.WITHER_ASH_CLOUD, NoopRenderer::new);
		EntityRenderers.register(ModEntities.BLIGHT_SPORE_CLOUD, NoopRenderer::new);
		EntityRenderers.register(ModEntities.SOUL_FIRE_TRAIL_CLOUD, NoopRenderer::new);
	}

	private static Identifier zombieTex(String name) {
		return Identifier.fromNamespaceAndPath(ID, "textures/entity/zombie/" + name + ".png");
	}

	private static Identifier creeperTex(String name) {
		return Identifier.fromNamespaceAndPath(ID, "textures/entity/creeper/" + name + ".png");
	}

	private static Identifier spiderTex(String name) {
		return Identifier.fromNamespaceAndPath(ID, "textures/entity/spider/" + name + ".png");
	}

	private static Identifier skeletonTex(String name) {
		return Identifier.fromNamespaceAndPath(ID, "textures/entity/skeleton/" + name + ".png");
	}

	private static Identifier endermanTex(String name) {
		return Identifier.fromNamespaceAndPath(ID, "textures/entity/enderman/" + name + ".png");
	}

	private static Identifier ghastTex(String name) {
		return Identifier.fromNamespaceAndPath(ID, "textures/entity/ghast/" + name + ".png");
	}

	private static Identifier blazeTex(String name) {
		return Identifier.fromNamespaceAndPath(ID, "textures/entity/blaze/" + name + ".png");
	}
}
