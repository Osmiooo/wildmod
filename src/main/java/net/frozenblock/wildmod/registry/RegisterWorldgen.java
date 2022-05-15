package net.frozenblock.wildmod.registry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import net.frozenblock.wildmod.WildMod;
import net.frozenblock.wildmod.mixins.TreeDecoratorTypeInvoker;
import net.frozenblock.wildmod.tags.BiomeTags;
import net.frozenblock.wildmod.world.gen.UpwardsBranchingTrunkPlacer;
import net.frozenblock.wildmod.world.gen.structure.BlockRotStructureProcessor;
import net.frozenblock.wildmod.world.gen.WildConfiguredFeatures;
import net.frozenblock.wildmod.world.gen.WildPlacedFeatures;
import net.frozenblock.wildmod.world.gen.structure.ancientcity.AncientCityData;
import net.frozenblock.wildmod.world.gen.structure.class_7001;
import net.frozenblock.wildmod.world.gen.structure.StructureTerrainAdaptation;
import net.frozenblock.wildmod.world.gen.structure.ancientcity.AncientCityGenerator;
import net.minecraft.block.Blocks;
import net.minecraft.client.color.world.FoliageColors;
import net.minecraft.client.sound.MusicType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.sound.BiomeMoodSound;
import net.minecraft.sound.MusicSound;
import net.minecraft.structure.processor.*;
import net.minecraft.structure.rule.AlwaysTrueRuleTest;
import net.minecraft.structure.rule.RandomBlockMatchRuleTest;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.StructureSpawns;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeEffects;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.carver.ConfiguredCarvers;
import net.minecraft.world.gen.feature.*;
import net.minecraft.world.gen.feature.size.TwoLayersFeatureSize;
import net.minecraft.world.gen.foliage.BlobFoliagePlacer;
import net.minecraft.world.gen.foliage.RandomSpreadFoliagePlacer;
import net.minecraft.world.gen.heightprovider.ConstantHeightProvider;
import net.minecraft.world.gen.placementmodifier.BiomePlacementModifier;
import net.minecraft.world.gen.placementmodifier.BlockFilterPlacementModifier;
import net.minecraft.world.gen.placementmodifier.SquarePlacementModifier;
import net.minecraft.world.gen.placementmodifier.SurfaceWaterDepthFilterPlacementModifier;
import net.minecraft.world.gen.placementmodifier.*;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.treedecorator.TreeDecoratorType;
import net.minecraft.world.gen.trunk.BendingTrunkPlacer;
import net.minecraft.world.gen.trunk.StraightTrunkPlacer;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class RegisterWorldgen {

    public static final RegistryKey<Biome> MANGROVE_SWAMP = register("mangrove_swamp");
    public static final RegistryKey<Biome> DEEP_DARK = register("deep_dark");

    public static RegistryEntry<PlacedFeature> TREES_MANGROVE;
    //public static RegistryEntry<ConfiguredFeature<RandomFeatureConfig, ?>> MANGROVE_VEGETATION;

    //public static RegistryEntry<ConfiguredFeature<TreeFeatureConfig, ?>> MANGROVE;
    public static RegistryEntry<ConfiguredFeature<TreeFeatureConfig, ?>> BIRCH_NEW;

    //public static final TreeDecoratorType<MangroveTreeDecorator> MANGROVE_TREE_DECORATOR = TreeDecoratorTypeInvoker.callRegister("rich_tree_decorator", MangroveTreeDecorator.CODEC);

    private static RegistryKey<Biome> register(String name) {
        return RegistryKey.of(Registry.BIOME_KEY, new Identifier(WildMod.MOD_ID, name));
    }

    public static Biome createDeepDark() {
        SpawnSettings.Builder builder = new SpawnSettings.Builder();
        GenerationSettings.Builder builder2 = new GenerationSettings.Builder();
        builder2.carver(GenerationStep.Carver.AIR, ConfiguredCarvers.CAVE);
        builder2.carver(GenerationStep.Carver.AIR, ConfiguredCarvers.CAVE_EXTRA_UNDERGROUND);
        builder2.carver(GenerationStep.Carver.AIR, ConfiguredCarvers.CANYON);
        DefaultBiomeFeatures.addAmethystGeodes(builder2);
        DefaultBiomeFeatures.addDungeons(builder2);
        DefaultBiomeFeatures.addMineables(builder2);
        DefaultBiomeFeatures.addFrozenTopLayer(builder2);
        DefaultBiomeFeatures.addPlainsTallGrass(builder2);
        DefaultBiomeFeatures.addDefaultOres(builder2, true);
        DefaultBiomeFeatures.addDefaultDisks(builder2);
        DefaultBiomeFeatures.addPlainsFeatures(builder2);
        DefaultBiomeFeatures.addDefaultMushrooms(builder2);
        DefaultBiomeFeatures.addDefaultVegetation(builder2);
        addSculk(builder2);
        MusicSound musicSound = MusicType.createIngameMusic(RegisterSounds.MUSIC_OVERWORLD_DEEP_DARK);
        return (
                new Biome.Builder())
                .precipitation(Biome.Precipitation.RAIN)
                .category(Biome.Category.UNDERGROUND)
                .temperature(0.8F).downfall(0.4F)
                .effects((new BiomeEffects.Builder())
                        .waterColor(4159204)
                        .waterFogColor(329011)
                        .fogColor(12638463)
                        .skyColor(getSkyColor(0.8F))
                        .foliageColor(FoliageColors.getDefaultColor())
                        .grassColorModifier(BiomeEffects.GrassColorModifier.NONE)
                        .moodSound(BiomeMoodSound.CAVE)
                        .music(musicSound)
                        //.loopSound(RegisterSounds.AMBIENT_DEEP_DARK_LOOP)
                        //.additionsSound(new BiomeAdditionsSound(RegisterSounds.AMBIENT_DEEP_DARK_ADDITIONS, 0.0072D))
                        .moodSound(BiomeMoodSound.CAVE).build())
                .spawnSettings(builder.build())
                .generationSettings(builder2.build()).build();

    }

    public static Biome createMangroveSwamp() {
        SpawnSettings.Builder builder = new SpawnSettings.Builder();
        DefaultBiomeFeatures.addBatsAndMonsters(builder);
        builder.spawn(SpawnGroup.MONSTER, new SpawnSettings.SpawnEntry(EntityType.SLIME, 1, 1, 1));
        builder.spawn(SpawnGroup.WATER_AMBIENT, new SpawnSettings.SpawnEntry(EntityType.TROPICAL_FISH, 25, 8, 8));
        GenerationSettings.Builder builder2 = new GenerationSettings.Builder();
        DefaultBiomeFeatures.addFossils(builder2);
        addBasicFeatures(builder2);
        DefaultBiomeFeatures.addDefaultOres(builder2);
        addGrassAndClayDisks(builder2);
        addMangroveSwampFeatures(builder2);
        builder2.feature(GenerationStep.Feature.VEGETAL_DECORATION, OceanPlacedFeatures.SEAGRASS_SWAMP);
        MusicSound musicSound = MusicType.createIngameMusic(RegisterSounds.MUSIC_OVERWORLD_SWAMP);
        return (
                new Biome.Builder())
                .precipitation(Biome.Precipitation.RAIN)
                .category(Biome.Category.SWAMP)
                .temperature(0.8F).downfall(0.9F)
                .effects((new BiomeEffects.Builder())
                        .waterColor(3832426)
                        .waterFogColor(5077600)
                        .fogColor(12638463)
                        .skyColor(getSkyColor(0.8F))
                        .foliageColor(9285927)
                        .grassColorModifier(BiomeEffects.GrassColorModifier.SWAMP)
                        .moodSound(BiomeMoodSound.CAVE)
                        .music(musicSound)
                        .build()
                )
                .spawnSettings(builder.build())
                .generationSettings(builder2.build())
                .build();
    }
    private static void addBasicFeatures(GenerationSettings.Builder generationSettings) {
        DefaultBiomeFeatures.addLandCarvers(generationSettings);
        DefaultBiomeFeatures.addAmethystGeodes(generationSettings);
        DefaultBiomeFeatures.addDungeons(generationSettings);
        DefaultBiomeFeatures.addMineables(generationSettings);
        DefaultBiomeFeatures.addSprings(generationSettings);
        DefaultBiomeFeatures.addFrozenTopLayer(generationSettings);
    }
    private static void addGrassAndClayDisks(GenerationSettings.Builder builder) {
        builder.feature(GenerationStep.Feature.UNDERGROUND_ORES, WildPlacedFeatures.DISK_GRASS);
        builder.feature(GenerationStep.Feature.UNDERGROUND_ORES, MiscPlacedFeatures.DISK_CLAY);
    }
    private static void addMangroveSwampFeatures(GenerationSettings.Builder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, TREES_MANGROVE);
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_GRASS_NORMAL);
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_DEAD_BUSH);
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_WATERLILY);
    }
    private static void addSculk(GenerationSettings.Builder builder) {
        builder.feature(GenerationStep.Feature.UNDERGROUND_DECORATION, WildPlacedFeatures.SCULK_VEIN);
        builder.feature(GenerationStep.Feature.UNDERGROUND_DECORATION, WildPlacedFeatures.SCULK_PATCH_DEEP_DARK);
    }
    protected static int getSkyColor(float temperature) {
        float f = temperature / 3.0F;
        f = MathHelper.clamp(f, -1.0F, 1.0F);
        return MathHelper.hsvToRgb(0.62222224F - f * 0.05F, 0.5F + f * 0.1F, 1.0F);
    }

    public static void RegisterWorldgen() {


        BIRCH_NEW = WildConfiguredFeatures.register("birch", Feature.TREE, new TreeFeatureConfig.Builder(
                BlockStateProvider.of(MangroveWoods.MANGROVE_LOG),
                new StraightTrunkPlacer(7, 3, 9), BlockStateProvider.of(Blocks.BIRCH_LEAVES),
                new BlobFoliagePlacer(ConstantIntProvider.create(3), ConstantIntProvider.create(0), 10),
                new TwoLayersFeatureSize(1, 0, 2)).ignoreVines()
                .build());

        TREES_MANGROVE = WildPlacedFeatures.register("trees_mangrove", WildConfiguredFeatures.MANGROVE_VEGETATION, CountPlacementModifier.of(30), SquarePlacementModifier.of(), SurfaceWaterDepthFilterPlacementModifier.of(5), PlacedFeatures.OCEAN_FLOOR_HEIGHTMAP, BiomePlacementModifier.of(), BlockFilterPlacementModifier.of(BlockPredicate.wouldSurvive(MangroveWoods.MANGROVE_PROPAGULE.getDefaultState(), BlockPos.ORIGIN)));

        BuiltinRegistries.add(BuiltinRegistries.BIOME, MANGROVE_SWAMP, createMangroveSwamp());
        BuiltinRegistries.add(BuiltinRegistries.BIOME, DEEP_DARK, createDeepDark());
    }

    // Structures

    private static RegistryEntry<StructureProcessorList> registerList(String id, ImmutableList<StructureProcessor> processorList) {
        Identifier identifier = new Identifier(WildMod.MOD_ID, id);
        StructureProcessorList structureProcessorList = new StructureProcessorList(processorList);
        return BuiltinRegistries.add(BuiltinRegistries.STRUCTURE_PROCESSOR_LIST, identifier, structureProcessorList);
    }


    public static RegistryEntry<StructureProcessorList> ANCIENT_CITY_START_DEGRADATION = registerList(
            "ancient_city_start_degradation",
            ImmutableList.of(
                    new RuleStructureProcessor(
                            ImmutableList.of(
                                    new StructureProcessorRule(
                                            new RandomBlockMatchRuleTest(Blocks.DEEPSLATE_BRICKS, 0.3F),
                                            AlwaysTrueRuleTest.INSTANCE,
                                            Blocks.CRACKED_DEEPSLATE_BRICKS.getDefaultState()
                                    ),
                                    new StructureProcessorRule(
                                            new RandomBlockMatchRuleTest(Blocks.DEEPSLATE_TILES, 0.3F),
                                            AlwaysTrueRuleTest.INSTANCE,
                                            Blocks.CRACKED_DEEPSLATE_TILES.getDefaultState()
                                    ),
                                    new StructureProcessorRule(
                                            new RandomBlockMatchRuleTest(Blocks.SOUL_LANTERN, 0.05F), AlwaysTrueRuleTest.INSTANCE, Blocks.AIR.getDefaultState()
                                    )
                            )
                    ),
                    new ProtectedBlocksStructureProcessor(BlockTags.FEATURES_CANNOT_REPLACE)
            )
    );
    public static RegistryEntry<StructureProcessorList> ANCIENT_CITY_GENERIC_DEGRADATION = registerList(
            "ancient_city_generic_degradation",
            ImmutableList.of(
                    new BlockRotStructureProcessor(RegisterTags.ANCIENT_CITY_REPLACEABLE, 0.95F),
                    new RuleStructureProcessor(
                            ImmutableList.of(
                                    new StructureProcessorRule(
                                            new RandomBlockMatchRuleTest(Blocks.DEEPSLATE_BRICKS, 0.3F),
                                            AlwaysTrueRuleTest.INSTANCE,
                                            Blocks.CRACKED_DEEPSLATE_BRICKS.getDefaultState()
                                    ),
                                    new StructureProcessorRule(
                                            new RandomBlockMatchRuleTest(Blocks.DEEPSLATE_TILES, 0.3F),
                                            AlwaysTrueRuleTest.INSTANCE,
                                            Blocks.CRACKED_DEEPSLATE_TILES.getDefaultState()
                                    ),
                                    new StructureProcessorRule(
                                            new RandomBlockMatchRuleTest(Blocks.SOUL_LANTERN, 0.05F), AlwaysTrueRuleTest.INSTANCE, Blocks.AIR.getDefaultState()
                                    )
                            )
                    ),
                    new ProtectedBlocksStructureProcessor(BlockTags.FEATURES_CANNOT_REPLACE)
            )
    );
    public static RegistryEntry<StructureProcessorList> ANCIENT_CITY_WALLS_DEGRADATION = registerList(
            "ancient_city_walls_degradation",
            ImmutableList.of(
                    new BlockRotStructureProcessor(RegisterTags.ANCIENT_CITY_REPLACEABLE, 0.95F),
                    new RuleStructureProcessor(
                            ImmutableList.of(
                                    new StructureProcessorRule(
                                            new RandomBlockMatchRuleTest(Blocks.DEEPSLATE_BRICKS, 0.3F),
                                            AlwaysTrueRuleTest.INSTANCE,
                                            Blocks.CRACKED_DEEPSLATE_BRICKS.getDefaultState()
                                    ),
                                    new StructureProcessorRule(
                                            new RandomBlockMatchRuleTest(Blocks.DEEPSLATE_TILES, 0.3F),
                                            AlwaysTrueRuleTest.INSTANCE,
                                            Blocks.CRACKED_DEEPSLATE_TILES.getDefaultState()
                                    ),
                                    new StructureProcessorRule(
                                            new RandomBlockMatchRuleTest(Blocks.DEEPSLATE_TILE_SLAB, 0.3F), AlwaysTrueRuleTest.INSTANCE, Blocks.AIR.getDefaultState()
                                    ),
                                    new StructureProcessorRule(
                                            new RandomBlockMatchRuleTest(Blocks.SOUL_LANTERN, 0.05F), AlwaysTrueRuleTest.INSTANCE, Blocks.AIR.getDefaultState()
                                    )
                            )
                    ),
                    new ProtectedBlocksStructureProcessor(BlockTags.FEATURES_CANNOT_REPLACE)
            )
    );
}
