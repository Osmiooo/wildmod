package net.frozenblock.wildmod.world.gen.structure;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.structure.*;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.random.AtomicSimpleRandom;
import net.minecraft.world.gen.random.ChunkRandom;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class WildStructureFeature<C extends FeatureConfig> extends StructureFeature {
    public static final BiMap<String, StructureFeature<?>> STRUCTURES = HashBiMap.create();
    public static final Map<StructureFeature<?>, GenerationStep.Feature> STRUCTURE_TO_GENERATION_STEP = Maps.newHashMap();
    public static final Logger LOGGER = LogManager.getLogger();
    public final Codec codec;
    public final StructureGeneratorFactory<C> piecesGenerator;
    private final PostPlacementProcessor postProcessor;

    public WildStructureFeature(Codec<C> configCodec, StructureGeneratorFactory<C> piecesGenerator) {
        this(configCodec, piecesGenerator, PostPlacementProcessor.EMPTY);
    }

    public WildStructureFeature(Codec<C> configCodec, StructureGeneratorFactory<C> piecesGenerator, PostPlacementProcessor postPlacementProcessor) {
        super(configCodec, piecesGenerator);
        this.codec = configCodec.fieldOf("config")
                .xmap(config -> new WildConfiguredStructureFeature(this, config), configuredFeature -> (C) configuredFeature.config)
                .codec();
        this.piecesGenerator = piecesGenerator;
        this.postProcessor = postPlacementProcessor;
    }

    public WildConfiguredStructureFeature<C, WildStructureFeature<C>> configure(C config) {
        return new WildConfiguredStructureFeature<C, WildStructureFeature<C>>(this, config);
    }

    public PostPlacementProcessor getPostProcessor() {
        return this.postProcessor;
    }

    public boolean isUniformDistribution() {
        return true;
    }

    public final ChunkPos getStartChunk(StructureConfig config, long seed, int x, int z) {
        int n;
        int m;
        int i = config.getSpacing();
        int j = config.getSeparation();
        int k = Math.floorDiv(x, i);
        int l = Math.floorDiv(z, i);
        ChunkRandom chunkRandom = new ChunkRandom(new AtomicSimpleRandom(0L));
        chunkRandom.setRegionSeed(seed, k, l, config.getSalt());
        if (this.isUniformDistribution()) {
            m = chunkRandom.nextInt(i - j);
            n = chunkRandom.nextInt(i - j);
        } else {
            m = (chunkRandom.nextInt(i - j) + chunkRandom.nextInt(i - j)) / 2;
            n = (chunkRandom.nextInt(i - j) + chunkRandom.nextInt(i - j)) / 2;
        }
        return new ChunkPos(k * i + m, l * i + n);
    }

    public WildStructureStart<?> tryPlaceStart(DynamicRegistryManager registryManager, ChunkGenerator chunkGenerator, BiomeSource biomeSource, StructureManager structureManager, long worldSeed, ChunkPos pos, int structureReferences, StructureConfig structureConfig, C config, HeightLimitView world, Predicate<RegistryEntry<Biome>> biomePredicate) {
        Optional<StructurePiecesGenerator<C>> optional;
        ChunkPos chunkPos = this.getStartChunk(structureConfig, worldSeed, pos.x, pos.z);
        if (pos.x == chunkPos.x && pos.z == chunkPos.z && (optional = this.piecesGenerator.createGenerator(new StructureGeneratorFactory.Context<C>(chunkGenerator, biomeSource, worldSeed, pos, config, world, biomePredicate, structureManager, registryManager))).isPresent()) {
            StructurePiecesCollector structurePiecesCollector = new StructurePiecesCollector();
            ChunkRandom chunkRandom = new ChunkRandom(new AtomicSimpleRandom(0L));
            chunkRandom.setCarverSeed(worldSeed, pos.x, pos.z);
            optional.get().generatePieces(structurePiecesCollector, new StructurePiecesGenerator.Context<C>(config, chunkGenerator, structureManager, pos, world, chunkRandom, worldSeed));
            WildStructureStart<?> structureStart = new WildStructureStart<>(this, pos, structureReferences, structurePiecesCollector.toList());
            if (structureStart.hasChildren()) {
                return structureStart;
            }
        }
        return WildStructureStart.DEFAULT;
    }

    public Codec<WildConfiguredStructureFeature<C, WildStructureFeature<C>>> getCodec() {
        return this.codec;
    }

    public BlockBox calculateBoundingBox(BlockBox box) {
        return box;
    }
}
