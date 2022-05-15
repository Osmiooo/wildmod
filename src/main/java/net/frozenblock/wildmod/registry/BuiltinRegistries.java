package net.frozenblock.wildmod.registry;


import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import net.frozenblock.wildmod.world.gen.structure.WildConfiguredStructureFeature;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

import java.util.Map;
import java.util.function.Supplier;

public class BuiltinRegistries {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<Identifier, Supplier<? extends RegistryEntry<?>>> DEFAULT_VALUE_SUPPLIERS = Maps.newLinkedHashMap();
    private static final MutableRegistry<MutableRegistry<?>> ROOT = new SimpleRegistry(
            RegistryKey.ofRegistry(new Identifier("root")), Lifecycle.experimental(), null
    );
    public static final Registry<WildConfiguredStructureFeature<?, ?>> CONFIGURED_STRUCTURE_FEATURE = addRegistry(
            Registry.CONFIGURED_STRUCTURE_FEATURE_KEY, RegisterStructures.ANCIENT_CITY
    );

    public BuiltinRegistries() {
    }


    private static <T> Registry<T> addRegistry(
            RegistryKey<? extends Registry<T>> registryRef, Supplier<? extends RegistryEntry<? extends T>> defaultValueSupplier
    ) {
        return addRegistry(registryRef, Lifecycle.stable(), defaultValueSupplier);
    }

    private static <T> Registry<T> addRegistry(
            RegistryKey<? extends Registry<T>> registryRef, Lifecycle lifecycle, Supplier<? extends RegistryEntry<? extends T>> defaultValueSupplier
    ) {
        return addRegistry(registryRef, new SimpleRegistry(registryRef, lifecycle, null), defaultValueSupplier, lifecycle);
    }

    private static <T, R extends MutableRegistry<T>> R addRegistry(
            RegistryKey<? extends Registry<T>> registryRef, R registry, Supplier<? extends RegistryEntry<? extends T>> defaultValueSupplier, Lifecycle lifecycle
    ) {
        Identifier identifier = registryRef.getValue();
        DEFAULT_VALUE_SUPPLIERS.put(identifier, defaultValueSupplier);
        ROOT.add((RegistryKey<MutableRegistry<?>>) registryRef, registry, lifecycle);
        return registry;
    }

    static {
        Registry.validate(ROOT);
    }
}
