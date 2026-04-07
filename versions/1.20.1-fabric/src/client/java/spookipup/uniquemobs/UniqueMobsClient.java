package spookipup.uniquemobs;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.renderer.entity.TippableArrowRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.resources.ResourceLocation;
import spookipup.uniquemobs.renderer.DeltaGhastRenderer;
import spookipup.uniquemobs.renderer.InvisibleEntityRenderer;
import spookipup.uniquemobs.renderer.RetexturedBlazeRenderer;
import spookipup.uniquemobs.registry.ModEntities;
import spookipup.uniquemobs.renderer.RetexturedCreeperRenderer;
import spookipup.uniquemobs.renderer.RetexturedEndermanRenderer;
import spookipup.uniquemobs.renderer.RetexturedGhastRenderer;
import spookipup.uniquemobs.renderer.RetexturedSkeletonRenderer;
import spookipup.uniquemobs.renderer.RetexturedSpiderRenderer;
import spookipup.uniquemobs.renderer.RetexturedZombieRenderer;
import spookipup.uniquemobs.renderer.StormArcRenderer;
import spookipup.uniquemobs.renderer.WitherBlazeRenderer;
import spookipup.uniquemobs.renderer.WitherGhastRenderer;
import spookipup.uniquemobs.renderer.WitherZombieModel;
import spookipup.uniquemobs.renderer.WitherZombieRenderer;

public class UniqueMobsClient implements ClientModInitializer {

	private static final String ID = UniqueMobs.MOD_ID;

	@SuppressWarnings("unchecked")
	@Override
	public void onInitializeClient() {
		// model layers
		EntityModelLayerRegistry.registerModelLayer(
			WitherZombieModel.LAYER_LOCATION, WitherZombieModel::createBodyLayer);

		// zombies
		EntityRendererRegistry.register(ModEntities.VENOMOUS_ZOMBIE,
			ctx -> new RetexturedZombieRenderer(ctx, zombieTex("venomous_zombie"), null));
		EntityRendererRegistry.register(ModEntities.WITHER_ZOMBIE, WitherZombieRenderer::new);
		EntityRendererRegistry.register(ModEntities.INFERNAL_ZOMBIE,
			ctx -> new RetexturedZombieRenderer(ctx, zombieTex("infernal_zombie"), zombieTex("infernal_zombie_eyes")));
		EntityRendererRegistry.register(ModEntities.FROZEN_ZOMBIE,
			ctx -> new RetexturedZombieRenderer(ctx, zombieTex("frozen_zombie"), null));
		EntityRendererRegistry.register(ModEntities.ENDER_ZOMBIE,
			ctx -> new RetexturedZombieRenderer(ctx, zombieTex("ender_zombie"), zombieTex("ender_zombie_eyes")));
		EntityRendererRegistry.register(ModEntities.SPRINTER_ZOMBIE,
			ctx -> new RetexturedZombieRenderer(ctx, zombieTex("sprinter_zombie"), null));
		EntityRendererRegistry.register(ModEntities.BUILDER_ZOMBIE,
			ctx -> new RetexturedZombieRenderer(ctx, zombieTex("builder_zombie"), null));
		EntityRendererRegistry.register(ModEntities.PLAGUE_ZOMBIE,
			ctx -> new RetexturedZombieRenderer(ctx, zombieTex("plague_zombie"), null));
		EntityRendererRegistry.register(ModEntities.ARMORED_ZOMBIE,
			ctx -> new RetexturedZombieRenderer(ctx, zombieTex("armored_zombie"), null));
		EntityRendererRegistry.register(ModEntities.BLOSSOM_ZOMBIE,
			ctx -> new RetexturedZombieRenderer(ctx, zombieTex("blossom_zombie"), null));

		// creepers
		EntityRendererRegistry.register(ModEntities.TOXIC_CREEPER,
			ctx -> new RetexturedCreeperRenderer(ctx, creeperTex("toxic_creeper"), null));
		EntityRendererRegistry.register(ModEntities.WITHER_CREEPER,
			ctx -> new RetexturedCreeperRenderer(ctx, creeperTex("wither_creeper"), creeperTex("wither_creeper_eyes")));
		EntityRendererRegistry.register(ModEntities.SCULK_CREEPER,
			ctx -> new RetexturedCreeperRenderer(ctx, creeperTex("sculk_creeper"), creeperTex("sculk_creeper_eyes")));
		EntityRendererRegistry.register(ModEntities.FROST_CREEPER,
			ctx -> new RetexturedCreeperRenderer(ctx, creeperTex("frost_creeper"), null));
		EntityRendererRegistry.register(ModEntities.MAGMA_CREEPER,
			ctx -> new RetexturedCreeperRenderer(ctx, creeperTex("magma_creeper"), creeperTex("magma_creeper_eyes")));
		EntityRendererRegistry.register(ModEntities.ENDER_CREEPER,
			ctx -> new RetexturedCreeperRenderer(ctx, creeperTex("ender_creeper"), creeperTex("ender_creeper_eyes")));
		EntityRendererRegistry.register(ModEntities.LIGHTNING_CREEPER,
			ctx -> new RetexturedCreeperRenderer(ctx, creeperTex("lightning_creeper"), creeperTex("lightning_creeper_top")));
		EntityRendererRegistry.register(ModEntities.BURROWING_CREEPER,
			ctx -> new RetexturedCreeperRenderer(ctx, creeperTex("burrowing_creeper"), null));
		EntityRendererRegistry.register(ModEntities.BLOSSOM_CREEPER,
			ctx -> new RetexturedCreeperRenderer(ctx, creeperTex("blossom_creeper"), null));

		// spiders
		EntityRendererRegistry.register(ModEntities.SPITTING_SPIDER,
			ctx -> new RetexturedSpiderRenderer(ctx, spiderTex("spitting_spider"), spiderTex("spitting_spider_eyes")));
		EntityRendererRegistry.register(ModEntities.WITHER_SPIDER,
			ctx -> new RetexturedSpiderRenderer(ctx, spiderTex("wither_spider"), spiderTex("wither_spider_eyes")));
		EntityRendererRegistry.register(ModEntities.MAGMA_SPIDER,
			ctx -> new RetexturedSpiderRenderer(ctx, spiderTex("magma_spider"), spiderTex("magma_spider_eyes")));
		EntityRendererRegistry.register(ModEntities.ICE_SPIDER,
			ctx -> new RetexturedSpiderRenderer(ctx, spiderTex("ice_spider"), spiderTex("ice_spider_eyes")));
		EntityRendererRegistry.register(ModEntities.JUMPING_SPIDER,
			ctx -> new RetexturedSpiderRenderer(ctx, spiderTex("jumping_spider"), spiderTex("jumping_spider_eyes")));
		EntityRendererRegistry.register(ModEntities.WEB_SPINNER_SPIDER,
			ctx -> new RetexturedSpiderRenderer(ctx, spiderTex("web_spinner_spider"), spiderTex("web_spinner_spider_eyes")));

		// skeletons
		EntityRendererRegistry.register(ModEntities.SNIPER_SKELETON,
			ctx -> new RetexturedSkeletonRenderer(ctx, skeletonTex("sniper_skeleton"), null));
		EntityRendererRegistry.register(ModEntities.EMBER_SKELETON,
			ctx -> new RetexturedSkeletonRenderer(ctx, skeletonTex("ember_skeleton"), skeletonTex("ember_skeleton_eyes")));
		EntityRendererRegistry.register(ModEntities.ENDER_SKELETON,
			ctx -> new RetexturedSkeletonRenderer(ctx, skeletonTex("ender_skeleton"), skeletonTex("ender_skeleton_eyes")));
		EntityRendererRegistry.register(ModEntities.POISON_SKELETON,
			ctx -> new RetexturedSkeletonRenderer(ctx, skeletonTex("poison_skeleton"), null));
		EntityRendererRegistry.register(ModEntities.MULTISHOT_SKELETON,
			ctx -> new RetexturedSkeletonRenderer(ctx, skeletonTex("multishot_skeleton"), null));
		EntityRendererRegistry.register(ModEntities.BLOSSOM_SKELETON,
			ctx -> new RetexturedSkeletonRenderer(ctx, skeletonTex("blossom_skeleton"), null));

		// endermen
		EntityRendererRegistry.register(ModEntities.ASSASSIN_ENDERMAN,
			ctx -> new RetexturedEndermanRenderer(ctx, endermanTex("assassin_enderman"), endermanTex("assassin_enderman_eyes")));
		EntityRendererRegistry.register(ModEntities.ENRAGED_ENDERMAN,
			ctx -> new RetexturedEndermanRenderer(ctx, endermanTex("enraged_enderman"), endermanTex("enraged_enderman_eyes")));
		EntityRendererRegistry.register(ModEntities.BLOSSOM_ENDERMAN,
			ctx -> new RetexturedEndermanRenderer(ctx, endermanTex("blossom_enderman"), endermanTex("blossom_enderman_eyes")));
		EntityRendererRegistry.register(ModEntities.BLOSSOM_ENDERMAN_AFTERIMAGE,
			ctx -> new RetexturedEndermanRenderer(ctx, endermanTex("blossom_enderman"), endermanTex("blossom_enderman_eyes")));

		// blazes
		EntityRendererRegistry.register(ModEntities.BLAST_BLAZE,
			ctx -> new RetexturedBlazeRenderer(ctx, blazeTex("blast_blaze")));
		EntityRendererRegistry.register(ModEntities.STORM_BLAZE,
			ctx -> new RetexturedBlazeRenderer(ctx, blazeTex("storm_blaze")));
		EntityRendererRegistry.register(ModEntities.WITHER_BLAZE,
			ctx -> new WitherBlazeRenderer(ctx, blazeTex("wither_blaze")));
		EntityRendererRegistry.register(ModEntities.SOUL_BLAZE,
			ctx -> new RetexturedBlazeRenderer(ctx, blazeTex("soul_blaze")));
		EntityRendererRegistry.register(ModEntities.BRAND_BLAZE,
			ctx -> new RetexturedBlazeRenderer(ctx, blazeTex("brand_blaze")));

		// ghasts
		EntityRendererRegistry.register(ModEntities.GREAT_MOTHER_GHAST,
			ctx -> new RetexturedGhastRenderer(ctx,
				ghastTex("great_mother_ghast"), ghastTex("great_mother_ghast_shooting"), null));
		EntityRendererRegistry.register(ModEntities.DELTA_GHAST,
			ctx -> new DeltaGhastRenderer(ctx,
				ghastTex("delta_ghast"), ghastTex("delta_ghast_charging"),
				ghastTex("delta_ghast"), ghastTex("delta_ghast_charging")));
		EntityRendererRegistry.register(ModEntities.WITHER_GHAST,
			ctx -> new WitherGhastRenderer(ctx,
				ghastTex("wither_ghast"), ghastTex("wither_ghast_charging"),
				null, null));
		EntityRendererRegistry.register(ModEntities.RAGELING,
			ctx -> new RetexturedGhastRenderer(ctx,
				ghastTex("rageling"), ghastTex("rageling_attacking"),
				ghastTex("rageling"), ghastTex("rageling_attacking")).withScale(0.12F, 0.12F));
		EntityRendererRegistry.register(ModEntities.SKITTERLING,
			ctx -> new RetexturedGhastRenderer(ctx,
				ghastTex("skitterling"), ghastTex("skitterling_charging"),
				null, ghastTex("skitterling_charging_glow")).withScale(0.28F, 0.10F));
		EntityRendererRegistry.register(ModEntities.OBSIDLING,
			ctx -> new RetexturedGhastRenderer(ctx,
				ghastTex("obsidling"), ghastTex("obsidling_attacking"),
				ghastTex("obsidling"), ghastTex("obsidling_attacking")).withScale(0.28F, 0.10F));
		EntityRendererRegistry.register(ModEntities.BLIGHTLING,
			ctx -> new RetexturedGhastRenderer(ctx,
				ghastTex("blightling"), ghastTex("blightling_attacking"),
				ghastTex("blightling"), ghastTex("blightling_attacking")).withScale(0.28F, 0.10F));

		// projectiles
		EntityRendererRegistry.register(ModEntities.WEB_PROJECTILE, ThrownItemRenderer::new);
		EntityRendererRegistry.register(ModEntities.FREEZE_SNOWBALL, ThrownItemRenderer::new);
		EntityRendererRegistry.register(ModEntities.POISON_SPIT, ThrownItemRenderer::new);
		EntityRendererRegistry.register(ModEntities.BLIGHT_SPORE, ThrownItemRenderer::new);
		EntityRendererRegistry.register(ModEntities.WITHER_ASH_BOLT, ThrownItemRenderer::new);
		EntityRendererRegistry.register(ModEntities.BLOSSOM_ARROW, TippableArrowRenderer::new);
		EntityRendererRegistry.register(ModEntities.STORM_ARC, StormArcRenderer::new);
		EntityRendererRegistry.register(ModEntities.WITHER_ASH_CLOUD, InvisibleEntityRenderer::new);
		EntityRendererRegistry.register(ModEntities.BLIGHT_SPORE_CLOUD, InvisibleEntityRenderer::new);
		EntityRendererRegistry.register(ModEntities.SOUL_FIRE_TRAIL_CLOUD, InvisibleEntityRenderer::new);
	}

	private static ResourceLocation zombieTex(String name) {
		return new ResourceLocation(ID, "textures/entity/zombie/" + name + ".png");
	}

	private static ResourceLocation creeperTex(String name) {
		return new ResourceLocation(ID, "textures/entity/creeper/" + name + ".png");
	}

	private static ResourceLocation spiderTex(String name) {
		return new ResourceLocation(ID, "textures/entity/spider/" + name + ".png");
	}

	private static ResourceLocation skeletonTex(String name) {
		return new ResourceLocation(ID, "textures/entity/skeleton/" + name + ".png");
	}

	private static ResourceLocation endermanTex(String name) {
		return new ResourceLocation(ID, "textures/entity/enderman/" + name + ".png");
	}

	private static ResourceLocation ghastTex(String name) {
		return new ResourceLocation(ID, "textures/entity/ghast/" + name + ".png");
	}

	private static ResourceLocation blazeTex(String name) {
		return new ResourceLocation(ID, "textures/entity/blaze/" + name + ".png");
	}
}
