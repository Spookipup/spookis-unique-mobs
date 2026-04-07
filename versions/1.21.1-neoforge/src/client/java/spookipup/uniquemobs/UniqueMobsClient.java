package spookipup.uniquemobs;

import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.entity.TippableArrowRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import spookipup.uniquemobs.registry.ModEntities;
import spookipup.uniquemobs.renderer.DeltaGhastRenderer;
import spookipup.uniquemobs.renderer.InvisibleEntityRenderer;
import spookipup.uniquemobs.renderer.RetexturedBlazeRenderer;
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

@EventBusSubscriber(modid = UniqueMobs.NEOFORGE_MOD_ID, value = Dist.CLIENT)
public class UniqueMobsClient {

	private static final String ID = UniqueMobs.MOD_ID;

	@SubscribeEvent
	public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(WitherZombieModel.LAYER_LOCATION, WitherZombieModel::createBodyLayer);
	}

	@SuppressWarnings("unchecked")
	@SubscribeEvent
	public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
		// zombies
		event.registerEntityRenderer(ModEntities.VENOMOUS_ZOMBIE.get(),
			ctx -> new RetexturedZombieRenderer(ctx, zombieTex("venomous_zombie"), null));
		event.registerEntityRenderer(ModEntities.WITHER_ZOMBIE.get(), WitherZombieRenderer::new);
		event.registerEntityRenderer(ModEntities.INFERNAL_ZOMBIE.get(),
			ctx -> new RetexturedZombieRenderer(ctx, zombieTex("infernal_zombie"), zombieTex("infernal_zombie_eyes")));
		event.registerEntityRenderer(ModEntities.FROZEN_ZOMBIE.get(),
			ctx -> new RetexturedZombieRenderer(ctx, zombieTex("frozen_zombie"), null));
		event.registerEntityRenderer(ModEntities.ENDER_ZOMBIE.get(),
			ctx -> new RetexturedZombieRenderer(ctx, zombieTex("ender_zombie"), zombieTex("ender_zombie_eyes")));
		event.registerEntityRenderer(ModEntities.SPRINTER_ZOMBIE.get(),
			ctx -> new RetexturedZombieRenderer(ctx, zombieTex("sprinter_zombie"), null));
		event.registerEntityRenderer(ModEntities.BUILDER_ZOMBIE.get(),
			ctx -> new RetexturedZombieRenderer(ctx, zombieTex("builder_zombie"), null));
		event.registerEntityRenderer(ModEntities.PLAGUE_ZOMBIE.get(),
			ctx -> new RetexturedZombieRenderer(ctx, zombieTex("plague_zombie"), null));
		event.registerEntityRenderer(ModEntities.ARMORED_ZOMBIE.get(),
			ctx -> new RetexturedZombieRenderer(ctx, zombieTex("armored_zombie"), null));
		event.registerEntityRenderer(ModEntities.BLOSSOM_ZOMBIE.get(),
			ctx -> new RetexturedZombieRenderer(ctx, zombieTex("blossom_zombie"), null));

		// creepers
		event.registerEntityRenderer(ModEntities.TOXIC_CREEPER.get(),
			ctx -> new RetexturedCreeperRenderer(ctx, creeperTex("toxic_creeper"), null));
		event.registerEntityRenderer(ModEntities.WITHER_CREEPER.get(),
			ctx -> new RetexturedCreeperRenderer(ctx, creeperTex("wither_creeper"), creeperTex("wither_creeper_eyes")));
		event.registerEntityRenderer(ModEntities.SCULK_CREEPER.get(),
			ctx -> new RetexturedCreeperRenderer(ctx, creeperTex("sculk_creeper"), creeperTex("sculk_creeper_eyes")));
		event.registerEntityRenderer(ModEntities.FROST_CREEPER.get(),
			ctx -> new RetexturedCreeperRenderer(ctx, creeperTex("frost_creeper"), null));
		event.registerEntityRenderer(ModEntities.MAGMA_CREEPER.get(),
			ctx -> new RetexturedCreeperRenderer(ctx, creeperTex("magma_creeper"), creeperTex("magma_creeper_eyes")));
		event.registerEntityRenderer(ModEntities.ENDER_CREEPER.get(),
			ctx -> new RetexturedCreeperRenderer(ctx, creeperTex("ender_creeper"), creeperTex("ender_creeper_eyes")));
		event.registerEntityRenderer(ModEntities.LIGHTNING_CREEPER.get(),
			ctx -> new RetexturedCreeperRenderer(ctx, creeperTex("lightning_creeper"), creeperTex("lightning_creeper_top")));
		event.registerEntityRenderer(ModEntities.BURROWING_CREEPER.get(),
			ctx -> new RetexturedCreeperRenderer(ctx, creeperTex("burrowing_creeper"), null));
		event.registerEntityRenderer(ModEntities.BLOSSOM_CREEPER.get(),
			ctx -> new RetexturedCreeperRenderer(ctx, creeperTex("blossom_creeper"), null));

		// spiders
		event.registerEntityRenderer(ModEntities.SPITTING_SPIDER.get(),
			ctx -> new RetexturedSpiderRenderer(ctx, spiderTex("spitting_spider"), spiderTex("spitting_spider_eyes")));
		event.registerEntityRenderer(ModEntities.WITHER_SPIDER.get(),
			ctx -> new RetexturedSpiderRenderer(ctx, spiderTex("wither_spider"), spiderTex("wither_spider_eyes")));
		event.registerEntityRenderer(ModEntities.MAGMA_SPIDER.get(),
			ctx -> new RetexturedSpiderRenderer(ctx, spiderTex("magma_spider"), spiderTex("magma_spider_eyes")));
		event.registerEntityRenderer(ModEntities.ICE_SPIDER.get(),
			ctx -> new RetexturedSpiderRenderer(ctx, spiderTex("ice_spider"), spiderTex("ice_spider_eyes")));
		event.registerEntityRenderer(ModEntities.JUMPING_SPIDER.get(),
			ctx -> new RetexturedSpiderRenderer(ctx, spiderTex("jumping_spider"), spiderTex("jumping_spider_eyes")));
		event.registerEntityRenderer(ModEntities.WEB_SPINNER_SPIDER.get(),
			ctx -> new RetexturedSpiderRenderer(ctx, spiderTex("web_spinner_spider"), spiderTex("web_spinner_spider_eyes")));

		// skeletons
		event.registerEntityRenderer(ModEntities.SNIPER_SKELETON.get(),
			ctx -> new RetexturedSkeletonRenderer(ctx, skeletonTex("sniper_skeleton"), null));
		event.registerEntityRenderer(ModEntities.EMBER_SKELETON.get(),
			ctx -> new RetexturedSkeletonRenderer(ctx, skeletonTex("ember_skeleton"), skeletonTex("ember_skeleton_eyes")));
		event.registerEntityRenderer(ModEntities.ENDER_SKELETON.get(),
			ctx -> new RetexturedSkeletonRenderer(ctx, skeletonTex("ender_skeleton"), skeletonTex("ender_skeleton_eyes")));
		event.registerEntityRenderer(ModEntities.POISON_SKELETON.get(),
			ctx -> new RetexturedSkeletonRenderer(ctx, skeletonTex("poison_skeleton"), null));
		event.registerEntityRenderer(ModEntities.MULTISHOT_SKELETON.get(),
			ctx -> new RetexturedSkeletonRenderer(ctx, skeletonTex("multishot_skeleton"), null));
		event.registerEntityRenderer(ModEntities.BLOSSOM_SKELETON.get(),
			ctx -> new RetexturedSkeletonRenderer(ctx, skeletonTex("blossom_skeleton"), null));

		// endermen
		event.registerEntityRenderer(ModEntities.ASSASSIN_ENDERMAN.get(),
			ctx -> new RetexturedEndermanRenderer(ctx, endermanTex("assassin_enderman"), endermanTex("assassin_enderman_eyes")));
		event.registerEntityRenderer(ModEntities.ENRAGED_ENDERMAN.get(),
			ctx -> new RetexturedEndermanRenderer(ctx, endermanTex("enraged_enderman"), endermanTex("enraged_enderman_eyes")));
		event.registerEntityRenderer(ModEntities.BLOSSOM_ENDERMAN.get(),
			ctx -> new RetexturedEndermanRenderer(ctx, endermanTex("blossom_enderman"), endermanTex("blossom_enderman_eyes")));
		event.registerEntityRenderer(ModEntities.BLOSSOM_ENDERMAN_AFTERIMAGE.get(),
			ctx -> new RetexturedEndermanRenderer(ctx, endermanTex("blossom_enderman"), endermanTex("blossom_enderman_eyes")));

		// blazes
		event.registerEntityRenderer(ModEntities.BLAST_BLAZE.get(),
			ctx -> new RetexturedBlazeRenderer(ctx, blazeTex("blast_blaze")));
		event.registerEntityRenderer(ModEntities.STORM_BLAZE.get(),
			ctx -> new RetexturedBlazeRenderer(ctx, blazeTex("storm_blaze")));
		event.registerEntityRenderer(ModEntities.WITHER_BLAZE.get(),
			ctx -> new WitherBlazeRenderer(ctx, blazeTex("wither_blaze")));
		event.registerEntityRenderer(ModEntities.SOUL_BLAZE.get(),
			ctx -> new RetexturedBlazeRenderer(ctx, blazeTex("soul_blaze")));
		event.registerEntityRenderer(ModEntities.BRAND_BLAZE.get(),
			ctx -> new RetexturedBlazeRenderer(ctx, blazeTex("brand_blaze")));

		// ghasts
		event.registerEntityRenderer(ModEntities.GREAT_MOTHER_GHAST.get(),
			ctx -> new RetexturedGhastRenderer(ctx,
				ghastTex("great_mother_ghast"), ghastTex("great_mother_ghast_shooting"), null));
		event.registerEntityRenderer(ModEntities.DELTA_GHAST.get(),
			ctx -> new DeltaGhastRenderer(ctx,
				ghastTex("delta_ghast"), ghastTex("delta_ghast_charging"),
				ghastTex("delta_ghast"), ghastTex("delta_ghast_charging")));
		event.registerEntityRenderer(ModEntities.WITHER_GHAST.get(),
			ctx -> new WitherGhastRenderer(ctx,
				ghastTex("wither_ghast"), ghastTex("wither_ghast_charging"),
				null, null));
		event.registerEntityRenderer(ModEntities.RAGELING.get(),
			ctx -> new RetexturedGhastRenderer(ctx,
				ghastTex("rageling"), ghastTex("rageling_attacking"),
				ghastTex("rageling"), ghastTex("rageling_attacking")).withScale(0.12F, 0.12F));
		event.registerEntityRenderer(ModEntities.SKITTERLING.get(),
			ctx -> new RetexturedGhastRenderer(ctx,
				ghastTex("skitterling"), ghastTex("skitterling_charging"),
				null, ghastTex("skitterling_charging_glow")).withScale(0.28F, 0.10F));
		event.registerEntityRenderer(ModEntities.OBSIDLING.get(),
			ctx -> new RetexturedGhastRenderer(ctx,
				ghastTex("obsidling"), ghastTex("obsidling_attacking"),
				ghastTex("obsidling"), ghastTex("obsidling_attacking")).withScale(0.28F, 0.10F));
		event.registerEntityRenderer(ModEntities.BLIGHTLING.get(),
			ctx -> new RetexturedGhastRenderer(ctx,
				ghastTex("blightling"), ghastTex("blightling_attacking"),
				ghastTex("blightling"), ghastTex("blightling_attacking")).withScale(0.28F, 0.10F));

		// projectiles
		event.registerEntityRenderer(ModEntities.WEB_PROJECTILE.get(), ThrownItemRenderer::new);
		event.registerEntityRenderer(ModEntities.FREEZE_SNOWBALL.get(), ThrownItemRenderer::new);
		event.registerEntityRenderer(ModEntities.POISON_SPIT.get(), ThrownItemRenderer::new);
		event.registerEntityRenderer(ModEntities.BLIGHT_SPORE.get(), ThrownItemRenderer::new);
		event.registerEntityRenderer(ModEntities.WITHER_ASH_BOLT.get(), ThrownItemRenderer::new);
		event.registerEntityRenderer(ModEntities.BLOSSOM_ARROW.get(), TippableArrowRenderer::new);
		event.registerEntityRenderer(ModEntities.STORM_ARC.get(), StormArcRenderer::new);
		event.registerEntityRenderer(ModEntities.WITHER_ASH_CLOUD.get(), InvisibleEntityRenderer::new);
		event.registerEntityRenderer(ModEntities.BLIGHT_SPORE_CLOUD.get(), InvisibleEntityRenderer::new);
		event.registerEntityRenderer(ModEntities.SOUL_FIRE_TRAIL_CLOUD.get(), InvisibleEntityRenderer::new);
	}

	private static ResourceLocation zombieTex(String name) {
		return ResourceLocation.fromNamespaceAndPath(ID, "textures/entity/zombie/" + name + ".png");
	}

	private static ResourceLocation creeperTex(String name) {
		return ResourceLocation.fromNamespaceAndPath(ID, "textures/entity/creeper/" + name + ".png");
	}

	private static ResourceLocation spiderTex(String name) {
		return ResourceLocation.fromNamespaceAndPath(ID, "textures/entity/spider/" + name + ".png");
	}

	private static ResourceLocation skeletonTex(String name) {
		return ResourceLocation.fromNamespaceAndPath(ID, "textures/entity/skeleton/" + name + ".png");
	}

	private static ResourceLocation endermanTex(String name) {
		return ResourceLocation.fromNamespaceAndPath(ID, "textures/entity/enderman/" + name + ".png");
	}

	private static ResourceLocation ghastTex(String name) {
		return ResourceLocation.fromNamespaceAndPath(ID, "textures/entity/ghast/" + name + ".png");
	}

	private static ResourceLocation blazeTex(String name) {
		return ResourceLocation.fromNamespaceAndPath(ID, "textures/entity/blaze/" + name + ".png");
	}
}
