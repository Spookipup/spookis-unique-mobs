package spookipup.uniquemobs.registry;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import spookipup.uniquemobs.UniqueMobs;
import spookipup.uniquemobs.entity.BlightSporeCloud;
import spookipup.uniquemobs.entity.SoulFireTrailCloud;
import spookipup.uniquemobs.entity.StormArcEntity;
import spookipup.uniquemobs.entity.WitherAshCloud;
import spookipup.uniquemobs.entity.projectile.BlossomArrowEntity;
import spookipup.uniquemobs.entity.projectile.BlightSporeEntity;
import spookipup.uniquemobs.entity.projectile.FreezeSnowballEntity;
import spookipup.uniquemobs.entity.projectile.PoisonSpitEntity;
import spookipup.uniquemobs.entity.projectile.WitherAshBoltEntity;
import spookipup.uniquemobs.entity.projectile.WebProjectileEntity;
import spookipup.uniquemobs.entity.variant.blaze.BlastBlazeEntity;
import spookipup.uniquemobs.entity.variant.blaze.BrandBlazeEntity;
import spookipup.uniquemobs.entity.variant.blaze.SoulBlazeEntity;
import spookipup.uniquemobs.entity.variant.blaze.StormBlazeEntity;
import spookipup.uniquemobs.entity.variant.blaze.WitherBlazeEntity;
import spookipup.uniquemobs.entity.variant.creeper.BlossomCreeperEntity;
import spookipup.uniquemobs.entity.variant.creeper.BurrowingCreeperEntity;
import spookipup.uniquemobs.entity.variant.creeper.EnderCreeperEntity;
import spookipup.uniquemobs.entity.variant.creeper.FrostCreeperEntity;
import spookipup.uniquemobs.entity.variant.creeper.LightningCreeperEntity;
import spookipup.uniquemobs.entity.variant.creeper.MagmaCreeperEntity;
import spookipup.uniquemobs.entity.variant.creeper.SculkCreeperEntity;
import spookipup.uniquemobs.entity.variant.creeper.ToxicCreeperEntity;
import spookipup.uniquemobs.entity.variant.creeper.WitherCreeperEntity;
import spookipup.uniquemobs.entity.variant.enderman.AssassinEndermanEntity;
import spookipup.uniquemobs.entity.variant.enderman.BlossomEndermanAfterimageEntity;
import spookipup.uniquemobs.entity.variant.enderman.BlossomEndermanEntity;
import spookipup.uniquemobs.entity.variant.enderman.EnragedEndermanEntity;
import spookipup.uniquemobs.entity.variant.ghast.BlightlingEntity;
import spookipup.uniquemobs.entity.variant.ghast.DeltaGhastEntity;
import spookipup.uniquemobs.entity.variant.ghast.GreatMotherGhastEntity;
import spookipup.uniquemobs.entity.variant.ghast.ObsidlingEntity;
import spookipup.uniquemobs.entity.variant.ghast.RagelingEntity;
import spookipup.uniquemobs.entity.variant.ghast.SkitterlingEntity;
import spookipup.uniquemobs.entity.variant.ghast.WitherGhastEntity;
import spookipup.uniquemobs.entity.variant.skeleton.BlossomSkeletonEntity;
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
import spookipup.uniquemobs.entity.variant.zombie.BlossomZombieEntity;
import spookipup.uniquemobs.entity.variant.zombie.BuilderZombieEntity;
import spookipup.uniquemobs.entity.variant.zombie.EnderZombieEntity;
import spookipup.uniquemobs.entity.variant.zombie.FrozenZombieEntity;
import spookipup.uniquemobs.entity.variant.zombie.InfernalZombieEntity;
import spookipup.uniquemobs.entity.variant.zombie.PlagueZombieEntity;
import spookipup.uniquemobs.entity.variant.zombie.SprinterZombieEntity;
import spookipup.uniquemobs.entity.variant.zombie.VenomousZombieEntity;
import spookipup.uniquemobs.entity.variant.zombie.WitherZombieEntity;

import java.util.function.Supplier;

public class ModEntities {

	private static final DeferredRegister<EntityType<?>> ENTITIES =
		DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, UniqueMobs.MOD_ID);

	// zombies

	public static final RegistryObject<EntityType<VenomousZombieEntity>> VENOMOUS_ZOMBIE = register("venomous_zombie", () -> EntityType.Builder.of(VenomousZombieEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 1.95F).clientTrackingRange(8)
			.build(id("venomous_zombie")));

	public static final RegistryObject<EntityType<InfernalZombieEntity>> INFERNAL_ZOMBIE = register("infernal_zombie", () -> EntityType.Builder.of(InfernalZombieEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 1.95F).clientTrackingRange(8)
			.build(id("infernal_zombie")));

	public static final RegistryObject<EntityType<FrozenZombieEntity>> FROZEN_ZOMBIE = register("frozen_zombie", () -> EntityType.Builder.of(FrozenZombieEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 1.95F).clientTrackingRange(8)
			.build(id("frozen_zombie")));

	public static final RegistryObject<EntityType<EnderZombieEntity>> ENDER_ZOMBIE = register("ender_zombie", () -> EntityType.Builder.of(EnderZombieEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 1.95F).clientTrackingRange(8)
			.build(id("ender_zombie")));

	public static final RegistryObject<EntityType<WitherZombieEntity>> WITHER_ZOMBIE = register("wither_zombie", () -> EntityType.Builder.of(WitherZombieEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 1.95F).clientTrackingRange(8)
			.build(id("wither_zombie")));

	public static final RegistryObject<EntityType<SprinterZombieEntity>> SPRINTER_ZOMBIE = register("sprinter_zombie", () -> EntityType.Builder.of(SprinterZombieEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 1.95F).clientTrackingRange(8)
			.build(id("sprinter_zombie")));

	public static final RegistryObject<EntityType<BuilderZombieEntity>> BUILDER_ZOMBIE = register("builder_zombie", () -> EntityType.Builder.of(BuilderZombieEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 1.95F).clientTrackingRange(8)
			.build(id("builder_zombie")));

	public static final RegistryObject<EntityType<PlagueZombieEntity>> PLAGUE_ZOMBIE = register("plague_zombie", () -> EntityType.Builder.of(PlagueZombieEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 1.95F).clientTrackingRange(8)
			.build(id("plague_zombie")));

	public static final RegistryObject<EntityType<ArmoredZombieEntity>> ARMORED_ZOMBIE = register("armored_zombie", () -> EntityType.Builder.of(ArmoredZombieEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 1.95F).clientTrackingRange(8)
			.build(id("armored_zombie")));

	public static final RegistryObject<EntityType<BlossomZombieEntity>> BLOSSOM_ZOMBIE = register("blossom_zombie", () -> EntityType.Builder.of(BlossomZombieEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 1.95F).clientTrackingRange(8)
			.build(id("blossom_zombie")));

	// spiders

	public static final RegistryObject<EntityType<SpittingSpiderEntity>> SPITTING_SPIDER = register("spitting_spider", () -> EntityType.Builder.of(SpittingSpiderEntity::new, MobCategory.MONSTER)
			.sized(1.4F, 0.9F).clientTrackingRange(8)
			.build(id("spitting_spider")));

	public static final RegistryObject<EntityType<WitherSpiderEntity>> WITHER_SPIDER = register("wither_spider", () -> EntityType.Builder.of(WitherSpiderEntity::new, MobCategory.MONSTER)
			.sized(1.4F, 0.9F).clientTrackingRange(8)
			.build(id("wither_spider")));

	public static final RegistryObject<EntityType<MagmaSpiderEntity>> MAGMA_SPIDER = register("magma_spider", () -> EntityType.Builder.of(MagmaSpiderEntity::new, MobCategory.MONSTER)
			.sized(1.4F, 0.9F).clientTrackingRange(8)
			.build(id("magma_spider")));

	public static final RegistryObject<EntityType<IceSpiderEntity>> ICE_SPIDER = register("ice_spider", () -> EntityType.Builder.of(IceSpiderEntity::new, MobCategory.MONSTER)
			.sized(1.4F, 0.9F).clientTrackingRange(8)
			.build(id("ice_spider")));

	public static final RegistryObject<EntityType<JumpingSpiderEntity>> JUMPING_SPIDER = register("jumping_spider", () -> EntityType.Builder.of(JumpingSpiderEntity::new, MobCategory.MONSTER)
			.sized(1.4F, 0.9F).clientTrackingRange(8)
			.build(id("jumping_spider")));

	public static final RegistryObject<EntityType<WebSpinnerSpiderEntity>> WEB_SPINNER_SPIDER = register("web_spinner_spider", () -> EntityType.Builder.of(WebSpinnerSpiderEntity::new, MobCategory.MONSTER)
			.sized(1.4F, 0.9F).clientTrackingRange(8)
			.build(id("web_spinner_spider")));

	// skeletons

	public static final RegistryObject<EntityType<SniperSkeletonEntity>> SNIPER_SKELETON = register("sniper_skeleton", () -> EntityType.Builder.of(SniperSkeletonEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 1.99F).clientTrackingRange(8)
			.build(id("sniper_skeleton")));

	public static final RegistryObject<EntityType<EmberSkeletonEntity>> EMBER_SKELETON = register("ember_skeleton", () -> EntityType.Builder.of(EmberSkeletonEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 1.99F).clientTrackingRange(8)
			.build(id("ember_skeleton")));

	public static final RegistryObject<EntityType<EnderSkeletonEntity>> ENDER_SKELETON = register("ender_skeleton", () -> EntityType.Builder.of(EnderSkeletonEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 1.99F).clientTrackingRange(8)
			.build(id("ender_skeleton")));

	public static final RegistryObject<EntityType<PoisonSkeletonEntity>> POISON_SKELETON = register("poison_skeleton", () -> EntityType.Builder.of(PoisonSkeletonEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 1.99F).clientTrackingRange(8)
			.build(id("poison_skeleton")));

	public static final RegistryObject<EntityType<MultishotSkeletonEntity>> MULTISHOT_SKELETON = register("multishot_skeleton", () -> EntityType.Builder.of(MultishotSkeletonEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 1.99F).clientTrackingRange(8)
			.build(id("multishot_skeleton")));

	public static final RegistryObject<EntityType<BlossomSkeletonEntity>> BLOSSOM_SKELETON = register("blossom_skeleton", () -> EntityType.Builder.of(BlossomSkeletonEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 1.99F).clientTrackingRange(8)
			.build(id("blossom_skeleton")));

	// creepers

	public static final RegistryObject<EntityType<ToxicCreeperEntity>> TOXIC_CREEPER = register("toxic_creeper", () -> EntityType.Builder.of(ToxicCreeperEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 1.7F).clientTrackingRange(8)
			.build(id("toxic_creeper")));

	public static final RegistryObject<EntityType<WitherCreeperEntity>> WITHER_CREEPER = register("wither_creeper", () -> EntityType.Builder.of(WitherCreeperEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 1.7F).clientTrackingRange(8)
			.build(id("wither_creeper")));

	public static final RegistryObject<EntityType<SculkCreeperEntity>> SCULK_CREEPER = register("sculk_creeper", () -> EntityType.Builder.of(SculkCreeperEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 1.7F).clientTrackingRange(8)
			.build(id("sculk_creeper")));

	public static final RegistryObject<EntityType<MagmaCreeperEntity>> MAGMA_CREEPER = register("magma_creeper", () -> EntityType.Builder.of(MagmaCreeperEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 1.7F).clientTrackingRange(8)
			.build(id("magma_creeper")));

	public static final RegistryObject<EntityType<FrostCreeperEntity>> FROST_CREEPER = register("frost_creeper", () -> EntityType.Builder.of(FrostCreeperEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 1.7F).clientTrackingRange(8)
			.build(id("frost_creeper")));

	public static final RegistryObject<EntityType<EnderCreeperEntity>> ENDER_CREEPER = register("ender_creeper", () -> EntityType.Builder.of(EnderCreeperEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 1.7F).clientTrackingRange(8)
			.build(id("ender_creeper")));

	public static final RegistryObject<EntityType<LightningCreeperEntity>> LIGHTNING_CREEPER = register("lightning_creeper", () -> EntityType.Builder.of(LightningCreeperEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 1.7F).clientTrackingRange(8)
			.build(id("lightning_creeper")));

	public static final RegistryObject<EntityType<BurrowingCreeperEntity>> BURROWING_CREEPER = register("burrowing_creeper", () -> EntityType.Builder.of(BurrowingCreeperEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 1.7F).clientTrackingRange(8)
			.build(id("burrowing_creeper")));

	public static final RegistryObject<EntityType<BlossomCreeperEntity>> BLOSSOM_CREEPER = register("blossom_creeper", () -> EntityType.Builder.of(BlossomCreeperEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 1.7F).clientTrackingRange(8)
			.build(id("blossom_creeper")));

	// endermen

	public static final RegistryObject<EntityType<AssassinEndermanEntity>> ASSASSIN_ENDERMAN = register("assassin_enderman", () -> EntityType.Builder.of(AssassinEndermanEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 2.9F).clientTrackingRange(8)
			.build(id("assassin_enderman")));

	public static final RegistryObject<EntityType<EnragedEndermanEntity>> ENRAGED_ENDERMAN = register("enraged_enderman", () -> EntityType.Builder.of(EnragedEndermanEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 2.9F).clientTrackingRange(8)
			.build(id("enraged_enderman")));

	public static final RegistryObject<EntityType<BlossomEndermanEntity>> BLOSSOM_ENDERMAN = register("blossom_enderman", () -> EntityType.Builder.of(BlossomEndermanEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 2.9F).clientTrackingRange(8)
			.build(id("blossom_enderman")));

	public static final RegistryObject<EntityType<BlossomEndermanAfterimageEntity>> BLOSSOM_ENDERMAN_AFTERIMAGE = register("blossom_enderman_afterimage", () -> EntityType.Builder.of(BlossomEndermanAfterimageEntity::new, MobCategory.MISC)
			.sized(0.6F, 2.9F).clientTrackingRange(8).updateInterval(2)
			.build(id("blossom_enderman_afterimage")));

	// blazes

	public static final RegistryObject<EntityType<BlastBlazeEntity>> BLAST_BLAZE = register("blast_blaze", () -> EntityType.Builder.of(BlastBlazeEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 1.8F).clientTrackingRange(8).fireImmune()
			.build(id("blast_blaze")));

	public static final RegistryObject<EntityType<StormBlazeEntity>> STORM_BLAZE = register("storm_blaze", () -> EntityType.Builder.of(StormBlazeEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 1.8F).clientTrackingRange(8).fireImmune()
			.build(id("storm_blaze")));

	public static final RegistryObject<EntityType<WitherBlazeEntity>> WITHER_BLAZE = register("wither_blaze", () -> EntityType.Builder.of(WitherBlazeEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 1.8F).clientTrackingRange(8).fireImmune()
			.build(id("wither_blaze")));

	public static final RegistryObject<EntityType<SoulBlazeEntity>> SOUL_BLAZE = register("soul_blaze", () -> EntityType.Builder.of(SoulBlazeEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 1.8F).clientTrackingRange(8).fireImmune()
			.build(id("soul_blaze")));

	public static final RegistryObject<EntityType<BrandBlazeEntity>> BRAND_BLAZE = register("brand_blaze", () -> EntityType.Builder.of(BrandBlazeEntity::new, MobCategory.MONSTER)
			.sized(0.6F, 1.8F).clientTrackingRange(8).fireImmune()
			.build(id("brand_blaze")));

	// ghasts

	public static final RegistryObject<EntityType<GreatMotherGhastEntity>> GREAT_MOTHER_GHAST = register("great_mother_ghast", () -> EntityType.Builder.of(GreatMotherGhastEntity::new, MobCategory.MONSTER)
			.sized(4.0F, 4.0F).clientTrackingRange(10).fireImmune()
			.build(id("great_mother_ghast")));

	public static final RegistryObject<EntityType<DeltaGhastEntity>> DELTA_GHAST = register("delta_ghast", () -> EntityType.Builder.of(DeltaGhastEntity::new, MobCategory.MONSTER)
			.sized(3.5F, 3.5F).clientTrackingRange(10).fireImmune()
			.build(id("delta_ghast")));

	public static final RegistryObject<EntityType<WitherGhastEntity>> WITHER_GHAST = register("wither_ghast", () -> EntityType.Builder.of(WitherGhastEntity::new, MobCategory.MONSTER)
			.sized(3.5F, 3.5F).clientTrackingRange(10).fireImmune()
			.build(id("wither_ghast")));

	public static final RegistryObject<EntityType<RagelingEntity>> RAGELING = register("rageling", () -> EntityType.Builder.of(RagelingEntity::new, MobCategory.MONSTER)
			.sized(0.8F, 0.8F).clientTrackingRange(10).fireImmune()
			.build(id("rageling")));

	public static final RegistryObject<EntityType<SkitterlingEntity>> SKITTERLING = register("skitterling", () -> EntityType.Builder.of(SkitterlingEntity::new, MobCategory.MONSTER)
			.sized(1.0F, 1.0F).clientTrackingRange(10).fireImmune()
			.build(id("skitterling")));

	public static final RegistryObject<EntityType<ObsidlingEntity>> OBSIDLING = register("obsidling", () -> EntityType.Builder.of(ObsidlingEntity::new, MobCategory.MONSTER)
			.sized(1.0F, 1.0F).clientTrackingRange(10).fireImmune()
			.build(id("obsidling")));

	public static final RegistryObject<EntityType<BlightlingEntity>> BLIGHTLING = register("blightling", () -> EntityType.Builder.of(BlightlingEntity::new, MobCategory.MONSTER)
			.sized(1.0F, 1.0F).clientTrackingRange(10).fireImmune()
			.build(id("blightling")));

	// projectiles

	public static final RegistryObject<EntityType<PoisonSpitEntity>> POISON_SPIT = register("poison_spit", () -> EntityType.Builder.<PoisonSpitEntity>of(PoisonSpitEntity::new, MobCategory.MISC)
			.sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10)
			.build(id("poison_spit")));

	public static final RegistryObject<EntityType<FreezeSnowballEntity>> FREEZE_SNOWBALL = register("freeze_snowball", () -> EntityType.Builder.<FreezeSnowballEntity>of(FreezeSnowballEntity::new, MobCategory.MISC)
			.sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10)
			.build(id("freeze_snowball")));

	public static final RegistryObject<EntityType<WebProjectileEntity>> WEB_PROJECTILE = register("web_projectile", () -> EntityType.Builder.<WebProjectileEntity>of(WebProjectileEntity::new, MobCategory.MISC)
			.sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10)
			.build(id("web_projectile")));

	public static final RegistryObject<EntityType<BlightSporeEntity>> BLIGHT_SPORE = register("blight_spore", () -> EntityType.Builder.<BlightSporeEntity>of(BlightSporeEntity::new, MobCategory.MISC)
			.sized(0.3F, 0.3F).clientTrackingRange(6).updateInterval(10)
			.build(id("blight_spore")));

	public static final RegistryObject<EntityType<WitherAshBoltEntity>> WITHER_ASH_BOLT = register("wither_ash_bolt", () -> EntityType.Builder.<WitherAshBoltEntity>of(WitherAshBoltEntity::new, MobCategory.MISC)
			.sized(0.25F, 0.25F).clientTrackingRange(6).updateInterval(10)
			.build(id("wither_ash_bolt")));

	public static final RegistryObject<EntityType<BlossomArrowEntity>> BLOSSOM_ARROW = register("blossom_arrow", () -> EntityType.Builder.<BlossomArrowEntity>of(BlossomArrowEntity::new, MobCategory.MISC)
			.sized(0.5F, 0.5F).clientTrackingRange(4).updateInterval(20)
			.build(id("blossom_arrow")));

	public static final RegistryObject<EntityType<StormArcEntity>> STORM_ARC = register("storm_arc", () -> EntityType.Builder.<StormArcEntity>of(StormArcEntity::new, MobCategory.MISC)
			.sized(0.2F, 0.2F).clientTrackingRange(16).updateInterval(1)
			.build(id("storm_arc")));

	public static final RegistryObject<EntityType<WitherAshCloud>> WITHER_ASH_CLOUD = register("wither_ash_cloud", () -> EntityType.Builder.<WitherAshCloud>of(WitherAshCloud::new, MobCategory.MISC)
			.sized(3.0F, 1.5F).clientTrackingRange(8).updateInterval(Integer.MAX_VALUE)
			.build(id("wither_ash_cloud")));

	public static final RegistryObject<EntityType<BlightSporeCloud>> BLIGHT_SPORE_CLOUD = register("blight_spore_cloud", () -> EntityType.Builder.<BlightSporeCloud>of(BlightSporeCloud::new, MobCategory.MISC)
			.sized(3.0F, 2.4F).clientTrackingRange(8).updateInterval(Integer.MAX_VALUE)
			.build(id("blight_spore_cloud")));

	public static final RegistryObject<EntityType<SoulFireTrailCloud>> SOUL_FIRE_TRAIL_CLOUD = register("soul_fire_trail_cloud", () -> EntityType.Builder.<SoulFireTrailCloud>of(SoulFireTrailCloud::new, MobCategory.MISC)
			.sized(2.7F, 1.2F).clientTrackingRange(8).updateInterval(Integer.MAX_VALUE)
			.build(id("soul_fire_trail_cloud")));

	private static String id(String name) {
		return UniqueMobs.MOD_ID + ":" + name;
	}

	private static <T extends Entity> RegistryObject<EntityType<T>> register(String name, Supplier<EntityType<T>> type) {
		return ENTITIES.register(name, type);
	}

	public static void register(IEventBus bus) {
		ENTITIES.register(bus);
		bus.addListener(ModEntities::registerAttributes);
	}

	private static void registerAttributes(EntityAttributeCreationEvent event) {
		// zombies
		event.put(VENOMOUS_ZOMBIE.get(), VenomousZombieEntity.createAttributes().build());
		event.put(INFERNAL_ZOMBIE.get(), InfernalZombieEntity.createAttributes().build());
		event.put(FROZEN_ZOMBIE.get(), FrozenZombieEntity.createAttributes().build());
		event.put(ENDER_ZOMBIE.get(), EnderZombieEntity.createAttributes().build());
		event.put(WITHER_ZOMBIE.get(), WitherZombieEntity.createAttributes().build());
		event.put(SPRINTER_ZOMBIE.get(), SprinterZombieEntity.createAttributes().build());
		event.put(BUILDER_ZOMBIE.get(), BuilderZombieEntity.createAttributes().build());
		event.put(PLAGUE_ZOMBIE.get(), PlagueZombieEntity.createAttributes().build());
		event.put(ARMORED_ZOMBIE.get(), ArmoredZombieEntity.createAttributes().build());
		event.put(BLOSSOM_ZOMBIE.get(), BlossomZombieEntity.createAttributes().build());

		// spiders
		event.put(SPITTING_SPIDER.get(), SpittingSpiderEntity.createAttributes().build());
		event.put(WITHER_SPIDER.get(), WitherSpiderEntity.createAttributes().build());
		event.put(MAGMA_SPIDER.get(), MagmaSpiderEntity.createAttributes().build());
		event.put(ICE_SPIDER.get(), IceSpiderEntity.createAttributes().build());
		event.put(JUMPING_SPIDER.get(), JumpingSpiderEntity.createAttributes().build());
		event.put(WEB_SPINNER_SPIDER.get(), WebSpinnerSpiderEntity.createAttributes().build());

		// skeletons
		event.put(SNIPER_SKELETON.get(), SniperSkeletonEntity.createAttributes().build());
		event.put(EMBER_SKELETON.get(), EmberSkeletonEntity.createAttributes().build());
		event.put(ENDER_SKELETON.get(), EnderSkeletonEntity.createAttributes().build());
		event.put(POISON_SKELETON.get(), PoisonSkeletonEntity.createAttributes().build());
		event.put(MULTISHOT_SKELETON.get(), MultishotSkeletonEntity.createAttributes().build());
		event.put(BLOSSOM_SKELETON.get(), BlossomSkeletonEntity.createAttributes().build());

		// creepers
		event.put(TOXIC_CREEPER.get(), ToxicCreeperEntity.createAttributes().build());
		event.put(WITHER_CREEPER.get(), WitherCreeperEntity.createAttributes().build());
		event.put(SCULK_CREEPER.get(), SculkCreeperEntity.createAttributes().build());
		event.put(MAGMA_CREEPER.get(), MagmaCreeperEntity.createAttributes().build());
		event.put(FROST_CREEPER.get(), FrostCreeperEntity.createAttributes().build());
		event.put(ENDER_CREEPER.get(), EnderCreeperEntity.createAttributes().build());
		event.put(LIGHTNING_CREEPER.get(), LightningCreeperEntity.createAttributes().build());
		event.put(BURROWING_CREEPER.get(), BurrowingCreeperEntity.createAttributes().build());
		event.put(BLOSSOM_CREEPER.get(), BlossomCreeperEntity.createAttributes().build());

		// endermen
		event.put(ASSASSIN_ENDERMAN.get(), AssassinEndermanEntity.createAttributes().build());
		event.put(ENRAGED_ENDERMAN.get(), EnragedEndermanEntity.createAttributes().build());
		event.put(BLOSSOM_ENDERMAN.get(), BlossomEndermanEntity.createAttributes().build());
		event.put(BLOSSOM_ENDERMAN_AFTERIMAGE.get(), BlossomEndermanAfterimageEntity.createAttributes().build());

		// blazes
		event.put(BLAST_BLAZE.get(), BlastBlazeEntity.createAttributes().build());
		event.put(STORM_BLAZE.get(), StormBlazeEntity.createAttributes().build());
		event.put(WITHER_BLAZE.get(), WitherBlazeEntity.createAttributes().build());
		event.put(SOUL_BLAZE.get(), SoulBlazeEntity.createAttributes().build());
		event.put(BRAND_BLAZE.get(), BrandBlazeEntity.createAttributes().build());

		// ghasts
		event.put(GREAT_MOTHER_GHAST.get(), GreatMotherGhastEntity.createAttributes().build());
		event.put(DELTA_GHAST.get(), DeltaGhastEntity.createAttributes().build());
		event.put(WITHER_GHAST.get(), WitherGhastEntity.createAttributes().build());
		event.put(RAGELING.get(), RagelingEntity.createAttributes().build());
		event.put(SKITTERLING.get(), SkitterlingEntity.createAttributes().build());
		event.put(OBSIDLING.get(), ObsidlingEntity.createAttributes().build());
		event.put(BLIGHTLING.get(), BlightlingEntity.createAttributes().build());
	}
}

