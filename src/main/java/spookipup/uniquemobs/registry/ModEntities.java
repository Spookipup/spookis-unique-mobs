package spookipup.uniquemobs.registry;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import spookipup.uniquemobs.UniqueMobs;
import spookipup.uniquemobs.entity.projectile.FreezeSnowballEntity;
import spookipup.uniquemobs.entity.projectile.PoisonSpitEntity;
import spookipup.uniquemobs.entity.projectile.WebProjectileEntity;
import spookipup.uniquemobs.entity.variant.creeper.BurrowingCreeperEntity;
import spookipup.uniquemobs.entity.variant.creeper.EnderCreeperEntity;
import spookipup.uniquemobs.entity.variant.creeper.FrostCreeperEntity;
import spookipup.uniquemobs.entity.variant.creeper.LightningCreeperEntity;
import spookipup.uniquemobs.entity.variant.creeper.MagmaCreeperEntity;
import spookipup.uniquemobs.entity.variant.creeper.SculkCreeperEntity;
import spookipup.uniquemobs.entity.variant.creeper.ToxicCreeperEntity;
import spookipup.uniquemobs.entity.variant.creeper.WitherCreeperEntity;
import spookipup.uniquemobs.entity.variant.enderman.AssassinEndermanEntity;
import spookipup.uniquemobs.entity.variant.enderman.EnragedEndermanEntity;
import spookipup.uniquemobs.entity.variant.ghast.RagelingEntity;
import spookipup.uniquemobs.entity.variant.ghast.GreatMotherGhastEntity;
import spookipup.uniquemobs.entity.variant.skeleton.EmberSkeletonEntity;
import spookipup.uniquemobs.entity.variant.skeleton.EnderSkeletonEntity;
import spookipup.uniquemobs.entity.variant.skeleton.MultishotSkeletonEntity;
import spookipup.uniquemobs.entity.variant.skeleton.PoisonSkeletonEntity;
import spookipup.uniquemobs.entity.variant.skeleton.SniperSkeletonEntity;
import spookipup.uniquemobs.entity.variant.spider.IceSpiderEntity;
import spookipup.uniquemobs.entity.variant.spider.JumpingSpiderEntity;
import spookipup.uniquemobs.entity.variant.spider.MagmaSpiderEntity;
import spookipup.uniquemobs.entity.variant.spider.SpittingSpiderEntity;
import spookipup.uniquemobs.entity.variant.spider.WebSpinnerSpiderEntity;
import spookipup.uniquemobs.entity.variant.spider.WitherSpiderEntity;
import spookipup.uniquemobs.entity.variant.zombie.ArmoredZombieEntity;
import spookipup.uniquemobs.entity.variant.zombie.BuilderZombieEntity;
import spookipup.uniquemobs.entity.variant.zombie.EnderZombieEntity;
import spookipup.uniquemobs.entity.variant.zombie.FrozenZombieEntity;
import spookipup.uniquemobs.entity.variant.zombie.InfernalZombieEntity;
import spookipup.uniquemobs.entity.variant.zombie.PlagueZombieEntity;
import spookipup.uniquemobs.entity.variant.zombie.SprinterZombieEntity;
import spookipup.uniquemobs.entity.variant.zombie.VenomousZombieEntity;
import spookipup.uniquemobs.entity.variant.zombie.WitherZombieEntity;

public class ModEntities {

	// zombies

	public static final EntityType<VenomousZombieEntity> VENOMOUS_ZOMBIE = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		entityKey("venomous_zombie"),
		EntityType.Builder.of(VenomousZombieEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 1.95F).clientTrackingRange(8)
			.build(entityKey("venomous_zombie"))
	);

	public static final EntityType<InfernalZombieEntity> INFERNAL_ZOMBIE = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		entityKey("infernal_zombie"),
		EntityType.Builder.of(InfernalZombieEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 1.95F).clientTrackingRange(8)
			.build(entityKey("infernal_zombie"))
	);

	public static final EntityType<FrozenZombieEntity> FROZEN_ZOMBIE = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		entityKey("frozen_zombie"),
		EntityType.Builder.of(FrozenZombieEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 1.95F).clientTrackingRange(8)
			.build(entityKey("frozen_zombie"))
	);

	public static final EntityType<EnderZombieEntity> ENDER_ZOMBIE = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		entityKey("ender_zombie"),
		EntityType.Builder.of(EnderZombieEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 1.95F).clientTrackingRange(8)
			.build(entityKey("ender_zombie"))
	);

	public static final EntityType<WitherZombieEntity> WITHER_ZOMBIE = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		entityKey("wither_zombie"),
		EntityType.Builder.of(WitherZombieEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 1.95F).clientTrackingRange(8)
			.build(entityKey("wither_zombie"))
	);

	public static final EntityType<SprinterZombieEntity> SPRINTER_ZOMBIE = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		entityKey("sprinter_zombie"),
		EntityType.Builder.of(SprinterZombieEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 1.95F).clientTrackingRange(8)
			.build(entityKey("sprinter_zombie"))
	);

	public static final EntityType<BuilderZombieEntity> BUILDER_ZOMBIE = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		entityKey("builder_zombie"),
		EntityType.Builder.of(BuilderZombieEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 1.95F).clientTrackingRange(8)
			.build(entityKey("builder_zombie"))
	);

	public static final EntityType<PlagueZombieEntity> PLAGUE_ZOMBIE = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		entityKey("plague_zombie"),
		EntityType.Builder.of(PlagueZombieEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 1.95F).clientTrackingRange(8)
			.build(entityKey("plague_zombie"))
	);

	public static final EntityType<ArmoredZombieEntity> ARMORED_ZOMBIE = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		entityKey("armored_zombie"),
		EntityType.Builder.of(ArmoredZombieEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 1.95F).clientTrackingRange(8)
			.build(entityKey("armored_zombie"))
	);

	// spiders

	public static final EntityType<SpittingSpiderEntity> SPITTING_SPIDER = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		entityKey("spitting_spider"),
		EntityType.Builder.of(SpittingSpiderEntity::new, MobCategory.MONSTER)
			.sized(1.4F, 0.9F).clientTrackingRange(8)
			.build(entityKey("spitting_spider"))
	);

	public static final EntityType<WitherSpiderEntity> WITHER_SPIDER = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		entityKey("wither_spider"),
		EntityType.Builder.of(WitherSpiderEntity::new, MobCategory.MONSTER)
			.sized(1.4F, 0.9F).clientTrackingRange(8)
			.build(entityKey("wither_spider"))
	);

	public static final EntityType<MagmaSpiderEntity> MAGMA_SPIDER = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		entityKey("magma_spider"),
		EntityType.Builder.of(MagmaSpiderEntity::new, MobCategory.MONSTER)
			.sized(1.4F, 0.9F).clientTrackingRange(8)
			.build(entityKey("magma_spider"))
	);

	public static final EntityType<IceSpiderEntity> ICE_SPIDER = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		entityKey("ice_spider"),
		EntityType.Builder.of(IceSpiderEntity::new, MobCategory.MONSTER)
			.sized(1.4F, 0.9F).clientTrackingRange(8)
			.build(entityKey("ice_spider"))
	);

	public static final EntityType<JumpingSpiderEntity> JUMPING_SPIDER = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		entityKey("jumping_spider"),
		EntityType.Builder.of(JumpingSpiderEntity::new, MobCategory.MONSTER)
			.sized(1.4F, 0.9F).clientTrackingRange(8)
			.build(entityKey("jumping_spider"))
	);

	public static final EntityType<WebSpinnerSpiderEntity> WEB_SPINNER_SPIDER = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		entityKey("web_spinner_spider"),
		EntityType.Builder.of(WebSpinnerSpiderEntity::new, MobCategory.MONSTER)
			.sized(1.4F, 0.9F).clientTrackingRange(8)
			.build(entityKey("web_spinner_spider"))
	);

	// skeletons

	public static final EntityType<SniperSkeletonEntity> SNIPER_SKELETON = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		entityKey("sniper_skeleton"),
		EntityType.Builder.of(SniperSkeletonEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 1.99F).clientTrackingRange(8)
			.build(entityKey("sniper_skeleton"))
	);

	public static final EntityType<EmberSkeletonEntity> EMBER_SKELETON = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		entityKey("ember_skeleton"),
		EntityType.Builder.of(EmberSkeletonEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 1.99F).clientTrackingRange(8)
			.build(entityKey("ember_skeleton"))
	);

	public static final EntityType<EnderSkeletonEntity> ENDER_SKELETON = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		entityKey("ender_skeleton"),
		EntityType.Builder.of(EnderSkeletonEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 1.99F).clientTrackingRange(8)
			.build(entityKey("ender_skeleton"))
	);

	public static final EntityType<PoisonSkeletonEntity> POISON_SKELETON = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		entityKey("poison_skeleton"),
		EntityType.Builder.of(PoisonSkeletonEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 1.99F).clientTrackingRange(8)
			.build(entityKey("poison_skeleton"))
	);

	public static final EntityType<MultishotSkeletonEntity> MULTISHOT_SKELETON = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		entityKey("multishot_skeleton"),
		EntityType.Builder.of(MultishotSkeletonEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 1.99F).clientTrackingRange(8)
			.build(entityKey("multishot_skeleton"))
	);

	// creepers

	public static final EntityType<ToxicCreeperEntity> TOXIC_CREEPER = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		entityKey("toxic_creeper"),
		EntityType.Builder.of(ToxicCreeperEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 1.7F).clientTrackingRange(8)
			.build(entityKey("toxic_creeper"))
	);

	public static final EntityType<WitherCreeperEntity> WITHER_CREEPER = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		entityKey("wither_creeper"),
		EntityType.Builder.of(WitherCreeperEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 1.7F).clientTrackingRange(8)
			.build(entityKey("wither_creeper"))
	);

	public static final EntityType<SculkCreeperEntity> SCULK_CREEPER = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		entityKey("sculk_creeper"),
		EntityType.Builder.of(SculkCreeperEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 1.7F).clientTrackingRange(8)
			.build(entityKey("sculk_creeper"))
	);

	public static final EntityType<MagmaCreeperEntity> MAGMA_CREEPER = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		entityKey("magma_creeper"),
		EntityType.Builder.of(MagmaCreeperEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 1.7F).clientTrackingRange(8)
			.build(entityKey("magma_creeper"))
	);

	public static final EntityType<FrostCreeperEntity> FROST_CREEPER = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		entityKey("frost_creeper"),
		EntityType.Builder.of(FrostCreeperEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 1.7F).clientTrackingRange(8)
			.build(entityKey("frost_creeper"))
	);

	public static final EntityType<EnderCreeperEntity> ENDER_CREEPER = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		entityKey("ender_creeper"),
		EntityType.Builder.of(EnderCreeperEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 1.7F).clientTrackingRange(8)
			.build(entityKey("ender_creeper"))
	);

	public static final EntityType<LightningCreeperEntity> LIGHTNING_CREEPER = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		entityKey("lightning_creeper"),
		EntityType.Builder.of(LightningCreeperEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 1.7F).clientTrackingRange(8)
			.build(entityKey("lightning_creeper"))
	);

	public static final EntityType<BurrowingCreeperEntity> BURROWING_CREEPER = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		entityKey("burrowing_creeper"),
		EntityType.Builder.of(BurrowingCreeperEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 1.7F).clientTrackingRange(8)
			.build(entityKey("burrowing_creeper"))
	);

	// endermen

	public static final EntityType<AssassinEndermanEntity> ASSASSIN_ENDERMAN = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		entityKey("assassin_enderman"),
		EntityType.Builder.of(AssassinEndermanEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 2.9F).clientTrackingRange(8)
			.build(entityKey("assassin_enderman"))
	);

	public static final EntityType<EnragedEndermanEntity> ENRAGED_ENDERMAN = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		entityKey("enraged_enderman"),
		EntityType.Builder.of(EnragedEndermanEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 2.9F).clientTrackingRange(8)
			.build(entityKey("enraged_enderman"))
	);

	// ghasts

	public static final EntityType<GreatMotherGhastEntity> GREAT_MOTHER_GHAST = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		entityKey("great_mother_ghast"),
		EntityType.Builder.of(GreatMotherGhastEntity::new, MobCategory.MONSTER)
			.sized(4.0F, 4.0F).clientTrackingRange(10).fireImmune()
			.build(entityKey("great_mother_ghast"))
	);

	public static final EntityType<RagelingEntity> RAGELING = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		entityKey("rageling"),
		EntityType.Builder.of(RagelingEntity::new, MobCategory.MONSTER)
			.sized(0.8F, 0.8F).clientTrackingRange(10).fireImmune()
			.build(entityKey("rageling"))
	);

	// projectiles

	public static final EntityType<PoisonSpitEntity> POISON_SPIT = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		entityKey("poison_spit"),
		EntityType.Builder.<PoisonSpitEntity>of(PoisonSpitEntity::new, MobCategory.MISC)
			.sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10)
			.build(entityKey("poison_spit"))
	);

	public static final EntityType<FreezeSnowballEntity> FREEZE_SNOWBALL = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		entityKey("freeze_snowball"),
		EntityType.Builder.<FreezeSnowballEntity>of(FreezeSnowballEntity::new, MobCategory.MISC)
			.sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10)
			.build(entityKey("freeze_snowball"))
	);

	public static final EntityType<WebProjectileEntity> WEB_PROJECTILE = Registry.register(
		BuiltInRegistries.ENTITY_TYPE,
		entityKey("web_projectile"),
		EntityType.Builder.<WebProjectileEntity>of(WebProjectileEntity::new, MobCategory.MISC)
			.sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10)
			.build(entityKey("web_projectile"))
	);

	private static ResourceKey<EntityType<?>> entityKey(String name) {
		return ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(UniqueMobs.MOD_ID, name));
	}

	private static void registerAttributes() {
		// zombies
		FabricDefaultAttributeRegistry.register(VENOMOUS_ZOMBIE, VenomousZombieEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(INFERNAL_ZOMBIE, InfernalZombieEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(FROZEN_ZOMBIE, FrozenZombieEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(ENDER_ZOMBIE, EnderZombieEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(WITHER_ZOMBIE, WitherZombieEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(SPRINTER_ZOMBIE, SprinterZombieEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(BUILDER_ZOMBIE, BuilderZombieEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(PLAGUE_ZOMBIE, PlagueZombieEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(ARMORED_ZOMBIE, ArmoredZombieEntity.createAttributes());

		// spiders
		FabricDefaultAttributeRegistry.register(SPITTING_SPIDER, SpittingSpiderEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(WITHER_SPIDER, WitherSpiderEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(MAGMA_SPIDER, MagmaSpiderEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(ICE_SPIDER, IceSpiderEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(JUMPING_SPIDER, JumpingSpiderEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(WEB_SPINNER_SPIDER, WebSpinnerSpiderEntity.createAttributes());

		// skeletons
		FabricDefaultAttributeRegistry.register(SNIPER_SKELETON, SniperSkeletonEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(EMBER_SKELETON, EmberSkeletonEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(ENDER_SKELETON, EnderSkeletonEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(POISON_SKELETON, PoisonSkeletonEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(MULTISHOT_SKELETON, MultishotSkeletonEntity.createAttributes());

		// creepers
		FabricDefaultAttributeRegistry.register(TOXIC_CREEPER, ToxicCreeperEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(WITHER_CREEPER, WitherCreeperEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(SCULK_CREEPER, SculkCreeperEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(MAGMA_CREEPER, MagmaCreeperEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(FROST_CREEPER, FrostCreeperEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(ENDER_CREEPER, EnderCreeperEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(LIGHTNING_CREEPER, LightningCreeperEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(BURROWING_CREEPER, BurrowingCreeperEntity.createAttributes());

		// endermen
		FabricDefaultAttributeRegistry.register(ASSASSIN_ENDERMAN, AssassinEndermanEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(ENRAGED_ENDERMAN, EnragedEndermanEntity.createAttributes());

		// ghasts
		FabricDefaultAttributeRegistry.register(GREAT_MOTHER_GHAST, GreatMotherGhastEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(RAGELING, RagelingEntity.createAttributes());
	}

	public static void init() {
		registerAttributes();
	}
}
