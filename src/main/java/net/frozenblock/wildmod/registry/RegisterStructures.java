package net.frozenblock.wildmod.registry;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import net.frozenblock.wildmod.world.gen.structure.WildConfiguredStructureFeature;
import net.frozenblock.wildmod.world.gen.structure.WildStructureFeature;
import net.frozenblock.wildmod.world.gen.structure.ancientcity.AncientCityData;
import net.frozenblock.wildmod.world.gen.structure.class_7001;
import net.minecraft.structure.StructureGeneratorFactory;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.*;

import java.util.Locale;
import java.util.Map;

public class RegisterStructures<C extends FeatureConfig> extends WildStructureFeature<C> {
    public static final WildStructureFeature<StructurePoolFeatureConfig> ANCIENT_CITY = register(
            "Ancient_City", new class_7001(StructurePoolFeatureConfig.CODEC), GenerationStep.Feature.UNDERGROUND_DECORATION
    );
    public static final WildConfiguredStructureFeature<StructurePoolFeatureConfig, ? extends WildStructureFeature<StructurePoolFeatureConfig>> ANCIENT_CITY_CONFIGURED = register("ancient_city", ANCIENT_CITY.configure(new StructurePoolFeatureConfig(() -> AncientCityData.CITY_CENTER, 7)))
    public RegisterStructures(Codec<C> configCodec, StructureGeneratorFactory<C> piecesGenerator) {
        super(configCodec, piecesGenerator);
    }

    public WildConfiguredStructureFeature<C, WildStructureFeature<C>> configure(C config) {
        return new WildConfiguredStructureFeature<>(this, config);
    }

    private static <FC extends FeatureConfig, F extends WildStructureFeature<FC>> RegistryEntry<WildConfiguredStructureFeature<?, ?>> register(
            RegistryKey<WildConfiguredStructureFeature<?, ?>> key, WildConfiguredStructureFeature<FC, F> configuredStructureFeature
    ) {
        return BuiltinRegistries.add(net.frozenblock.wildmod.registry.BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE, key, configuredStructureFeature);
    }

    //private static final WildConfiguredStructureFeature<MineshaftFeatureConfig, ? extends StructureFeature<MineshaftFeatureConfig>> MINESHAFT = RegisterStructures.register("mineshaft", WildStructureFeature.MINESHAFT.configure(new MineshaftFeatureConfig(0.004f, MineshaftFeature.Type.NORMAL)));

    public static WildConfiguredStructureFeature<?, ?> getDefault() {
        return RegisterStructures.ANCIENT_CITY;
    }

    public static final BiMap<String, StructureFeature<?>> STRUCTURES = HashBiMap.create();
    public static final Map<StructureFeature<?>, GenerationStep.Feature> STRUCTURE_TO_GENERATION_STEP = Maps.newHashMap();

    private static <F extends StructureFeature<?>> F register(String name, F structureFeature, GenerationStep.Feature step) {
        STRUCTURES.put(name.toLowerCase(Locale.ROOT), structureFeature);
        STRUCTURE_TO_GENERATION_STEP.put(structureFeature, step);
        return (F) net.minecraft.util.registry.Registry.register(Registry.STRUCTURE_FEATURE, name.toLowerCase(Locale.ROOT), structureFeature);
    }
}
