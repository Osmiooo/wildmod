package net.frozenblock.wildmod.world.gen.structure;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.dynamic.RegistryElementCodec;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class WildConfiguredStructureFeature<FC extends FeatureConfig, F extends WildStructureFeature<FC>> {
    public static final Codec<ConfiguredStructureFeature<?, ?>> CODEC = Registry.STRUCTURE_FEATURE
            .getCodec()
            .dispatch(configuredStructureFeature -> configuredStructureFeature.feature, StructureFeature::getCodec);
    public static final RegistryElementCodec<ConfiguredStructureFeature<?, ?>> REGISTRY_CODEC = of(Registry.CONFIGURED_STRUCTURE_FEATURE_KEY, CODEC);
    //public static final Codec<List<Supplier<net.minecraft.world.gen.feature.ConfiguredStructureFeature<?, ?>>>> REGISTRY_ELEMENT_CODEC = RegistryElementCodec.method_31194(Registry.CONFIGURED_STRUCTURE_FEATURE_KEY, CODEC);
    public final F feature;
    public final FC config;

    public WildConfiguredStructureFeature(F feature, FC config) {
        this.feature = feature;
        this.config = config;
    }

    private static <E> RegistryElementCodec<E> of(RegistryKey<? extends Registry<E>> registryRef, Codec<E> elementCodec) {
        return new RegistryElementCodec<>(registryRef, elementCodec, true);
    }

    private static <E> RegistryElementCodec<E> of(RegistryKey<? extends Registry<E>> registryRef, Codec<E> elementCodec, boolean allowInlineDefinitions) {
        return new RegistryElementCodec<>(registryRef, elementCodec, allowInlineDefinitions);
    }

    public WildStructureStart<?> tryPlaceStart(
            DynamicRegistryManager registryManager,
            ChunkGenerator chunkGenerator,
            BiomeSource biomeSource,
            StructureManager structureManager,
            long worldSeed,
            ChunkPos chunkPos,
            int structureReferences,
            StructureConfig structureConfig,
            HeightLimitView world,
            Predicate<RegistryEntry<Biome>> biomeLimit
    ) {
        return this.feature
                .tryPlaceStart(
                        registryManager,
                        chunkGenerator,
                        biomeSource,
                        structureManager,
                        worldSeed,
                        chunkPos,
                        structureReferences,
                        structureConfig,
                        this.config,
                        world,
                        biomeLimit
                );
    }
}

