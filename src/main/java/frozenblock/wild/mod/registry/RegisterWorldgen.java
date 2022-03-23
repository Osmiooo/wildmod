package frozenblock.wild.mod.registry;

import com.google.common.collect.ImmutableList;
import frozenblock.wild.mod.WildMod;
import frozenblock.wild.mod.mixins.TreeDecoratorTypeInvoker;
import frozenblock.wild.mod.worldgen.*;
import net.minecraft.block.Blocks;
import net.minecraft.client.color.world.FoliageColors;
import net.minecraft.client.sound.MusicType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.sound.BiomeAdditionsSound;
import net.minecraft.sound.BiomeMoodSound;
import net.minecraft.sound.MusicSound;
import net.minecraft.structure.processor.*;
import net.minecraft.structure.rule.AlwaysTrueRuleTest;
import net.minecraft.structure.rule.RandomBlockMatchRuleTest;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeEffects;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.decorator.*;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.feature.size.TwoLayersFeatureSize;
import net.minecraft.world.gen.foliage.BlobFoliagePlacer;
import net.minecraft.world.gen.foliage.RandomSpreadFoliagePlacer;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.treedecorator.TreeDecoratorType;
import net.minecraft.world.gen.trunk.BendingTrunkPlacer;
import net.minecraft.world.gen.trunk.StraightTrunkPlacer;
import org.jetbrains.annotations.Nullable;

public class RegisterWorldgen {

    @Nullable
    private static final MusicSound DEFAULT_MUSIC = null;

    public static final RegistryKey<Biome> MANGROVE_SWAMP = register("mangrove_swamp");
    public static final RegistryKey<Biome> DEEP_DARK = register("deep_dark");

    private static final Feature<DefaultFeatureConfig> SCULK_CATASTROPHE_FEATURE = new LargeSculkPatchFeature(DefaultFeatureConfig.CODEC);
    private static final Feature<DefaultFeatureConfig> SCULK_PATCH_FEATURE = new SculkPatchFeature(DefaultFeatureConfig.CODEC);
    private static final Feature<DefaultFeatureConfig> RANDOM_SCULK_FEATURE = new RandomSculkFeature(DefaultFeatureConfig.CODEC);
    private static final Feature<DefaultFeatureConfig> RANDOM_VEINS_FEATURE = new RandomVeinsFeature(DefaultFeatureConfig.CODEC);
    private static final Feature<DefaultFeatureConfig> COMMON_ACTIVATOR_FEATURE = new CommonActivatorFeature(DefaultFeatureConfig.CODEC);
    private static final Feature<DefaultFeatureConfig> RARE_ACTIVATOR_FEATURE = new RareActivatorFeature(DefaultFeatureConfig.CODEC);

    public static StructureProcessorList ANCIENT_CITY_START_DEGRADATION;
    public static StructureProcessorList ANCIENT_CITY_GENERIC_DEGRADATION;
    public static StructureProcessorList ANCIENT_CITY_WALLS_DEGRADATION;

    public static final StructureFeature<StructurePoolFeatureConfig> ANCIENT_CITY;

    public static PlacedFeature TREES_MANGROVE;
    public static PlacedFeature SCULK_CATASTROPHE_PLACED;
    public static PlacedFeature SCULK_PATCH_PLACED;
    public static PlacedFeature RANDOM_SCULK_PLACED;
    public static PlacedFeature RANDOM_VEINS_PLACED;
    public static PlacedFeature COMMON_ACTIVATOR_PLACED;
    public static PlacedFeature RARE_ACTIVATOR_PLACED;

    public static ConfiguredFeature<TreeFeatureConfig, ?> MANGROVE;
    public static ConfiguredFeature<TreeFeatureConfig, ?> BIRCH_NEW;
    public static ConfiguredFeature<DefaultFeatureConfig, ?> SCULK_CATASTROPHE_CONFIGURED;
    public static ConfiguredFeature<DefaultFeatureConfig, ?> SCULK_PATCH_CONFIGURED;
    public static ConfiguredFeature<DefaultFeatureConfig, ?> RANDOM_SCULK_CONFIGURED;
    public static ConfiguredFeature<DefaultFeatureConfig, ?> RANDOM_VEINS_CONFIGURED;
    public static ConfiguredFeature<DefaultFeatureConfig, ?> COMMON_ACTIVATOR_CONFIGURED;
    public static ConfiguredFeature<DefaultFeatureConfig, ?> RARE_ACTIVATOR_CONFIGURED;

    public static final TreeDecoratorType<MangroveTreeDecorator> MANGROVE_TREE_DECORATOR = TreeDecoratorTypeInvoker.callRegister("rich_tree_decorator", MangroveTreeDecorator.CODEC);

    private static RegistryKey<Biome> register(String name) {
        return RegistryKey.of(Registry.BIOME_KEY, new Identifier(WildMod.MOD_ID, name));
    }

    private static StructureProcessorList registerList(String id, ImmutableList<StructureProcessor> processorList) {
        Identifier identifier = new Identifier(id);
        StructureProcessorList structureProcessorList = new StructureProcessorList(processorList);
        return (StructureProcessorList)BuiltinRegistries.add(BuiltinRegistries.STRUCTURE_PROCESSOR_LIST, identifier, structureProcessorList);
    }

    public static Biome createDeepDark() {
        SpawnSettings.Builder builder = new SpawnSettings.Builder();
        net.minecraft.world.biome.GenerationSettings.Builder builder2 = new net.minecraft.world.biome.GenerationSettings.Builder();
        DefaultBiomeFeatures.addFossils(builder2);
        addBasicFeaturesNoSprings(builder2);
        DefaultBiomeFeatures.addPlainsTallGrass(builder2);
        DefaultBiomeFeatures.addDefaultGrass(builder2);
        DefaultBiomeFeatures.addPlainsFeatures(builder2);
        DefaultBiomeFeatures.addDefaultVegetation(builder2);
        DefaultBiomeFeatures.addDefaultOres(builder2);
        builder2.feature(GenerationStep.Feature.VEGETAL_DECORATION, SCULK_PATCH_PLACED);
        builder2.feature(GenerationStep.Feature.VEGETAL_DECORATION, SCULK_CATASTROPHE_PLACED);
        builder2.feature(GenerationStep.Feature.VEGETAL_DECORATION, RANDOM_SCULK_PLACED);
        builder2.feature(GenerationStep.Feature.VEGETAL_DECORATION, RANDOM_VEINS_PLACED);
        builder2.feature(GenerationStep.Feature.VEGETAL_DECORATION, COMMON_ACTIVATOR_PLACED);
        builder2.feature(GenerationStep.Feature.VEGETAL_DECORATION, RARE_ACTIVATOR_PLACED);

        MusicSound musicSound = MusicType.createIngameMusic(RegisterSounds.MUSIC_OVERWORLD_DEEP_DARK);
        return (
                new net.minecraft.world.biome.Biome.Builder())
                .precipitation(Biome.Precipitation.NONE)
                .category(Biome.Category.UNDERGROUND)
                .temperature(0.8F).downfall(0.9F)
                .effects((new net.minecraft.world.biome.BiomeEffects.Builder())
                        .waterColor(4159204)
                        .waterFogColor(329011)
                        .fogColor(0)
                        .skyColor(getSkyColor(0.8F))
                        .foliageColor(FoliageColors.getDefaultColor())
                        .grassColorModifier(BiomeEffects.GrassColorModifier.NONE)
                        .music(musicSound)
                        .loopSound(RegisterSounds.AMBIENT_DEEP_DARK_LOOP)
                        .additionsSound(new BiomeAdditionsSound(RegisterSounds.AMBIENT_DEEP_DARK_ADDITIONS, 0.0072D))
                        .moodSound(BiomeMoodSound.CAVE).build())
                .spawnSettings(builder.build())
                .generationSettings(builder2.build()).build();

    }

    public static Biome createMangroveSwamp() {
        SpawnSettings.Builder builder = new SpawnSettings.Builder();
        DefaultBiomeFeatures.addFarmAnimals(builder);
        builder.spawn(SpawnGroup.MONSTER, new SpawnSettings.SpawnEntry(EntityType.SLIME, 1, 1, 1));
        net.minecraft.world.biome.GenerationSettings.Builder builder2 = new net.minecraft.world.biome.GenerationSettings.Builder();
        DefaultBiomeFeatures.addFossils(builder2);
        addBasicFeatures(builder2);
        DefaultBiomeFeatures.addDefaultOres(builder2);
        DefaultBiomeFeatures.addClayDisk(builder2);
        addMangroveSwampFeatures(builder2);
        DefaultBiomeFeatures.addDefaultMushrooms(builder2);
        DefaultBiomeFeatures.addSwampVegetation(builder2);
        builder2.feature(GenerationStep.Feature.VEGETAL_DECORATION, OceanPlacedFeatures.SEAGRASS_SWAMP);
        return (
                new net.minecraft.world.biome.Biome.Builder())
                .precipitation(Biome.Precipitation.RAIN)
                .category(Biome.Category.SWAMP)
                .temperature(0.8F).downfall(0.9F)
                .effects((new net.minecraft.world.biome.BiomeEffects.Builder())
                        .waterColor(0x397d71)
                        .waterFogColor(0x397d71)
                        .fogColor(12638463)
                        .skyColor(getSkyColor(0.8F))
                        .foliageColor(6975545)
                        .grassColorModifier(BiomeEffects.GrassColorModifier.SWAMP)
                        .moodSound(BiomeMoodSound.CAVE).build())
                .spawnSettings(builder.build())
                .generationSettings(builder2.build()).build();

    }

    public static void addMangroveSwampFeatures(GenerationSettings.Builder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, TREES_MANGROVE);
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_WATERLILY);
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.BROWN_MUSHROOM_SWAMP);
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.RED_MUSHROOM_SWAMP);
    }

    private static void addBasicFeatures(net.minecraft.world.biome.GenerationSettings.Builder generationSettings) {
        DefaultBiomeFeatures.addLandCarvers(generationSettings);
        DefaultBiomeFeatures.addAmethystGeodes(generationSettings);
        DefaultBiomeFeatures.addDungeons(generationSettings);
        DefaultBiomeFeatures.addMineables(generationSettings);
        DefaultBiomeFeatures.addSprings(generationSettings);
        DefaultBiomeFeatures.addFrozenTopLayer(generationSettings);
    }

    private static void addBasicFeaturesNoSprings(net.minecraft.world.biome.GenerationSettings.Builder generationSettings) {
        DefaultBiomeFeatures.addLandCarvers(generationSettings);
        DefaultBiomeFeatures.addAmethystGeodes(generationSettings);
        DefaultBiomeFeatures.addDungeons(generationSettings);
        DefaultBiomeFeatures.addMineables(generationSettings);
        DefaultBiomeFeatures.addFrozenTopLayer(generationSettings);
    }

    protected static int getSkyColor(float temperature) {
        float f = temperature / 3.0F;
        f = MathHelper.clamp(f, -1.0F, 1.0F);
        return MathHelper.hsvToRgb(0.62222224F - f * 0.05F, 0.5F + f * 0.1F, 1.0F);
    }

    public static void RegisterWorldgen() {
        Registry.register(Registry.FEATURE, new Identifier(WildMod.MOD_ID, "sculk_catastrophe_feature"), SCULK_CATASTROPHE_FEATURE);
        Registry.register(Registry.FEATURE, new Identifier(WildMod.MOD_ID, "sculk_patch_feature"), SCULK_PATCH_FEATURE);
        Registry.register(Registry.FEATURE, new Identifier(WildMod.MOD_ID, "random_sculk_feature"), RANDOM_SCULK_FEATURE);
        Registry.register(Registry.FEATURE, new Identifier(WildMod.MOD_ID, "random_veins_feature"), RANDOM_VEINS_FEATURE);
        Registry.register(Registry.FEATURE, new Identifier(WildMod.MOD_ID, "common_activator_feature"), COMMON_ACTIVATOR_FEATURE);
        Registry.register(Registry.FEATURE, new Identifier(WildMod.MOD_ID, "rare_activator_feature"), RARE_ACTIVATOR_FEATURE);

        MANGROVE = Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, new Identifier(WildMod.MOD_ID, "mangrove"), Feature.TREE
                .configure(new TreeFeatureConfig.Builder(
                        BlockStateProvider.of(MangroveWoods.MANGROVE_LOG),
                        new BendingTrunkPlacer(8, 2, 0, 8, UniformIntProvider.create(1, 2)), BlockStateProvider.of(MangroveWoods.MANGROVE_LEAVES),
                        new RandomSpreadFoliagePlacer(ConstantIntProvider.create(3), ConstantIntProvider.create(0), ConstantIntProvider.create(3), 100),
                        new TwoLayersFeatureSize(1, 0, 2)).ignoreVines()
                        .decorators(ImmutableList.of(MangroveTreeDecorator.INSTANCE))
                        .build()));
        BIRCH_NEW = Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, new Identifier(WildMod.MOD_ID, "birch"), Feature.TREE
                .configure(new TreeFeatureConfig.Builder(
                        BlockStateProvider.of(MangroveWoods.MANGROVE_LOG),
                        new StraightTrunkPlacer(7, 3, 9), BlockStateProvider.of(Blocks.BIRCH_LEAVES),
                        new BlobFoliagePlacer(ConstantIntProvider.create(3), ConstantIntProvider.create(0), 10),
                        new TwoLayersFeatureSize(1, 0, 2)).ignoreVines()
                        .build()));

        SCULK_CATASTROPHE_CONFIGURED = Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, new Identifier(WildMod.MOD_ID, "sculk_catastrophe"), SCULK_CATASTROPHE_FEATURE.configure(new DefaultFeatureConfig()));
        SCULK_PATCH_CONFIGURED = Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, new Identifier(WildMod.MOD_ID, "sculk_patch"), SCULK_PATCH_FEATURE.configure(new DefaultFeatureConfig()));
        RANDOM_SCULK_CONFIGURED = Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, new Identifier(WildMod.MOD_ID, "random_sculk_patch"), RANDOM_SCULK_FEATURE.configure(new DefaultFeatureConfig()));
        RANDOM_VEINS_CONFIGURED = Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, new Identifier(WildMod.MOD_ID, "random_veins_patch"), RANDOM_VEINS_FEATURE.configure(new DefaultFeatureConfig()));
        COMMON_ACTIVATOR_CONFIGURED = Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, new Identifier(WildMod.MOD_ID, "common_activators"), COMMON_ACTIVATOR_FEATURE.configure(new DefaultFeatureConfig()));
        RARE_ACTIVATOR_CONFIGURED = Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, new Identifier(WildMod.MOD_ID, "rare_activators"), RARE_ACTIVATOR_FEATURE.configure(new DefaultFeatureConfig()));


        TREES_MANGROVE = Registry.register(BuiltinRegistries.PLACED_FEATURE, new Identifier(WildMod.MOD_ID, "trees_mangrove"), MANGROVE.withPlacement(PlacedFeatures.createCountExtraModifier(8, 0.1f, 1), SquarePlacementModifier.of(), SurfaceWaterDepthFilterPlacementModifier.of(6), PlacedFeatures.OCEAN_FLOOR_HEIGHTMAP, BiomePlacementModifier.of(), BlockFilterPlacementModifier.of(BlockPredicate.wouldSurvive(MangroveWoods.MANGROVE_PROPAGULE.getDefaultState(), BlockPos.ORIGIN))));
        SCULK_CATASTROPHE_PLACED = Registry.register(BuiltinRegistries.PLACED_FEATURE, new Identifier(WildMod.MOD_ID, "sculk_catastrophe"), SCULK_CATASTROPHE_CONFIGURED.withPlacement(PlacedFeatures.createCountExtraModifier(1, 0.1f, 3), SquarePlacementModifier.of(), HeightRangePlacementModifier.uniform(YOffset.getBottom(), YOffset.fixed(0)), EnvironmentScanPlacementModifier.of(Direction.DOWN, BlockPredicate.solid(), BlockPredicate.IS_AIR, 12), BiomePlacementModifier.of()));
        SCULK_PATCH_PLACED = Registry.register(BuiltinRegistries.PLACED_FEATURE, new Identifier(WildMod.MOD_ID, "sculk_patch"), SCULK_PATCH_CONFIGURED.withPlacement(PlacedFeatures.createCountExtraModifier(20, 0.1f, 3), SquarePlacementModifier.of(), HeightRangePlacementModifier.uniform(YOffset.getBottom(), YOffset.fixed(0)), EnvironmentScanPlacementModifier.of(Direction.DOWN, BlockPredicate.solid(), BlockPredicate.IS_AIR, 12), BiomePlacementModifier.of()));
        RANDOM_SCULK_PLACED = Registry.register(BuiltinRegistries.PLACED_FEATURE, new Identifier(WildMod.MOD_ID, "random_sculk_patch"), RANDOM_SCULK_CONFIGURED.withPlacement(PlacedFeatures.createCountExtraModifier(10, 0.1f, 3), SquarePlacementModifier.of(), HeightRangePlacementModifier.uniform(YOffset.getBottom(), YOffset.fixed(0)), EnvironmentScanPlacementModifier.of(Direction.DOWN, BlockPredicate.solid(), BlockPredicate.IS_AIR, 12), BiomePlacementModifier.of()));
        RANDOM_VEINS_PLACED = Registry.register(BuiltinRegistries.PLACED_FEATURE, new Identifier(WildMod.MOD_ID, "random_veins_patch"), RANDOM_VEINS_CONFIGURED.withPlacement(PlacedFeatures.createCountExtraModifier(10, 0.1f, 3), SquarePlacementModifier.of(), HeightRangePlacementModifier.uniform(YOffset.getBottom(), YOffset.fixed(0)), EnvironmentScanPlacementModifier.of(Direction.DOWN, BlockPredicate.solid(), BlockPredicate.IS_AIR, 12), BiomePlacementModifier.of()));
        COMMON_ACTIVATOR_PLACED = Registry.register(BuiltinRegistries.PLACED_FEATURE, new Identifier(WildMod.MOD_ID, "common_activators"), COMMON_ACTIVATOR_CONFIGURED.withPlacement(PlacedFeatures.createCountExtraModifier(70, 0.1f, 3), SquarePlacementModifier.of(), HeightRangePlacementModifier.uniform(YOffset.getBottom(), YOffset.fixed(0)), EnvironmentScanPlacementModifier.of(Direction.DOWN, BlockPredicate.solid(), BlockPredicate.IS_AIR, 12), BiomePlacementModifier.of()));
        RARE_ACTIVATOR_PLACED = Registry.register(BuiltinRegistries.PLACED_FEATURE, new Identifier(WildMod.MOD_ID, "rare_activators"), RARE_ACTIVATOR_CONFIGURED.withPlacement(PlacedFeatures.createCountExtraModifier(23, 0.1f, 3), SquarePlacementModifier.of(), HeightRangePlacementModifier.uniform(YOffset.getBottom(), YOffset.fixed(0)), EnvironmentScanPlacementModifier.of(Direction.DOWN, BlockPredicate.solid(), BlockPredicate.IS_AIR, 12), BiomePlacementModifier.of()));

        ANCIENT_CITY_START_DEGRADATION = registerList("ancient_city_start_degradation", ImmutableList.of(new ReplaceInTagStructureProcessor(RegisterTags.ANCIENT_CITY_REPLACEABLES, 0.98F), new RuleStructureProcessor(ImmutableList.of(new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.DEEPSLATE_BRICKS, 0.3F), AlwaysTrueRuleTest.INSTANCE, Blocks.CRACKED_DEEPSLATE_BRICKS.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.DEEPSLATE_TILES, 0.3F), AlwaysTrueRuleTest.INSTANCE, Blocks.CRACKED_DEEPSLATE_TILES.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.SOUL_LANTERN, 0.05F), AlwaysTrueRuleTest.INSTANCE, Blocks.AIR.getDefaultState()))), new ProtectedBlocksStructureProcessor(BlockTags.FEATURES_CANNOT_REPLACE.getId())));
        ANCIENT_CITY_GENERIC_DEGRADATION = registerList("ancient_city_generic_degradation", ImmutableList.of(new ReplaceInTagStructureProcessor(RegisterTags.ANCIENT_CITY_REPLACEABLES, 0.95F), new RuleStructureProcessor(ImmutableList.of(new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.DEEPSLATE_BRICKS, 0.3F), AlwaysTrueRuleTest.INSTANCE, Blocks.CRACKED_DEEPSLATE_BRICKS.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.DEEPSLATE_TILES, 0.3F), AlwaysTrueRuleTest.INSTANCE, Blocks.CRACKED_DEEPSLATE_TILES.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.SOUL_LANTERN, 0.05F), AlwaysTrueRuleTest.INSTANCE, Blocks.AIR.getDefaultState()))), new ProtectedBlocksStructureProcessor(BlockTags.FEATURES_CANNOT_REPLACE.getId())));
        ANCIENT_CITY_WALLS_DEGRADATION = registerList("ancient_city_walls_degradation", ImmutableList.of(new ReplaceInTagStructureProcessor(RegisterTags.ANCIENT_CITY_REPLACEABLES, 0.95F), new RuleStructureProcessor(ImmutableList.of(new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.DEEPSLATE_BRICKS, 0.3F), AlwaysTrueRuleTest.INSTANCE, Blocks.CRACKED_DEEPSLATE_BRICKS.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.DEEPSLATE_TILES, 0.3F), AlwaysTrueRuleTest.INSTANCE, Blocks.CRACKED_DEEPSLATE_TILES.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.DEEPSLATE_TILE_SLAB, 0.3F), AlwaysTrueRuleTest.INSTANCE, Blocks.AIR.getDefaultState()), new StructureProcessorRule(new RandomBlockMatchRuleTest(Blocks.SOUL_LANTERN, 0.05F), AlwaysTrueRuleTest.INSTANCE, Blocks.AIR.getDefaultState()))), new ProtectedBlocksStructureProcessor(BlockTags.FEATURES_CANNOT_REPLACE.getId())));

        ANCIENT_CITY = register("Ancient_City", new AncientCityFeature(StructurePoolFeatureConfig.CODEC), GenerationStep.Feature.UNDERGROUND_DECORATION);

        BuiltinRegistries.add(BuiltinRegistries.BIOME, MANGROVE_SWAMP, createMangroveSwamp());
        BuiltinRegistries.add(BuiltinRegistries.BIOME, DEEP_DARK, createDeepDark());
    }
}
