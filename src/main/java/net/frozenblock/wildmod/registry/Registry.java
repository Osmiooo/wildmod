package net.frozenblock.wildmod.registry;

import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.*;
import net.frozenblock.wildmod.WildMod;
import net.frozenblock.wildmod.entity.FrogEntity;
import net.frozenblock.wildmod.entity.FrogVariant;
import net.frozenblock.wildmod.event.PositionSourceType;
import net.frozenblock.wildmod.world.gen.structure.WildConfiguredStructureFeature;
import net.minecraft.Bootstrap;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public abstract class Registry<T> implements Keyable, IndexedIterable<T> {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<Identifier, Supplier<?>> DEFAULT_ENTRIES = Maps.newLinkedHashMap();
    public static final Identifier ROOT_KEY = new Identifier("root");
    private final RegistryKey<? extends Registry<T>> registryKey;
    private final Lifecycle lifecycle;
    public static final RegistryKey<Registry<FrogVariant>> FROG_VARIANT_KEY = createRegistryKey("frog_variant");

    public static final Registry<FrogVariant> FROG_VARIANT = create(FROG_VARIANT_KEY, registry -> FrogVariant.TEMPERATE);

    public static final TrackedDataHandler<FrogVariant> FROG_VARIANT_DATA = net.frozenblock.wildmod.entity.TrackedDataHandler.of(FROG_VARIANT);


    //public static final RegistryKey<Registry<net.frozenblock.wildmod.world.gen.structure.StructureType<?>>> STRUCTURE_TYPE_KEY = createRegistryKey("worldgen/structure_type");
    //public static final Registry<StructureType<?>> STRUCTURE_TYPE = create(STRUCTURE_TYPE_KEY, registry -> net.frozenblock.wildmod.world.gen.structure.StructureType.JIGSAW);

    //public static final RegistryKey<Registry<StructureType<?>>> STRUCTURE_KEY = createRegistryKey("worldgen/structure");
    public static final RegistryKey<Registry<WildConfiguredStructureFeature<?, ?>>> CONFIGURED_STRUCTURE_FEATURE_KEY = createRegistryKey(
            "worldgen/configured_structure_feature"
    );
    public static final RegistryKey<Registry<DoublePerlinNoiseSampler.NoiseParameters>> NOISE_KEY = createRegistryKey("worldgen/noise");

    protected Registry(RegistryKey<? extends Registry<T>> key, Lifecycle lifecycle) {
        super();
        Bootstrap.ensureBootstrapped(() -> "registry" + key);
        this.registryKey = key;
        this.lifecycle = lifecycle;
    }

    protected static final MutableRegistry<MutableRegistry<?>> ROOT = new SimpleRegistry(createRegistryKey("root"), Lifecycle.experimental(), null);

    private static <T> RegistryKey<Registry<T>> createRegistryKey(String registryId) {
        return RegistryKey.ofRegistry(new Identifier(WildMod.MOD_ID, registryId));
    }

    private static <T> net.minecraft.util.registry.RegistryKey<net.minecraft.util.registry.Registry<T>> createVanillaRegistryKey(String registryId) {
        return net.minecraft.util.registry.RegistryKey.ofRegistry(new Identifier(WildMod.MOD_ID, registryId));
    }


    public static <T> T register(Registry<? super T> registry, String id, T entry) {
        return register(registry, new Identifier(WildMod.MOD_ID, id), entry);
    }

    public static <V, T extends V> T register(Registry<V> registry, Identifier id, T entry) {
        return register(registry, RegistryKey.of(registry.registryKey, id), entry);
    }

    public static <V, T extends V> T register(Registry<V> registry, RegistryKey<V> key, T entry) {
        ((MutableRegistry)registry).add(key, entry, Lifecycle.stable());
        return entry;
    }

    public static <V, T extends V> T register(Registry<V> registry, int rawId, String id, T entry) {
        ((MutableRegistry)registry).set(rawId, RegistryKey.of(registry.registryKey, new Identifier(WildMod.MOD_ID, id)), entry, Lifecycle.stable());
        return entry;
    }


    public static final RegistryKey<Registry<PositionSourceType<?>>> WILD_POSITION_SOURCE_TYPE_KEY = createRegistryKey("wild_position_source_type");

    public static final Registry<PositionSourceType<?>> WILD_POSITION_SOURCE_TYPE = create(WILD_POSITION_SOURCE_TYPE_KEY, registry -> PositionSourceType.BLOCK);


    public static <T extends net.minecraft.util.registry.Registry<?>> void validate(MutableRegistry<MutableRegistry<?>> registries) {
        registries.forEach(registry -> {
            if (registry.getIds().isEmpty()) {
                Util.error("Registry '" + registries.getId(registry) + "' was empty after loading");
            }

            if (registry instanceof DefaultedRegistry) {
                Identifier identifier = ((DefaultedRegistry<?>)registry).getDefaultId();
                Validate.notNull(registry.get(identifier), "Missing default of DefaultedMappedRegistry: " + identifier);
            }

        });
    }


    private static <T, R extends MutableRegistry<T>> R create(
            RegistryKey<? extends Registry<T>> key, R registry, Registry.DefaultEntryGetter<T> defaultEntryGetter, Lifecycle lifecycle
    ) {
        Identifier identifier = key.getValue();
        DEFAULT_ENTRIES.put(identifier, (Supplier)() -> defaultEntryGetter.run(registry));
        //ROOT.add((RegistryKey<MutableRegistry<?>>) key, registry, lifecycle);
        return registry;
    }

    private static <T> Registry<T> create(RegistryKey<? extends Registry<T>> key, Lifecycle lifecycle, DefaultEntryGetter<T> defaultEntryGetter) {
        return create(key, new SimpleRegistry<>(key, lifecycle, null), defaultEntryGetter, lifecycle);
    }


    private static <T> Registry<T> create(RegistryKey<? extends Registry<T>> key, DefaultEntryGetter<T> defaultEntryGetter) {
        return create(key, Lifecycle.experimental(), defaultEntryGetter);
    }

    @Override
    public <U> Stream<U> keys(DynamicOps<U> ops) {
        return this.getIds().stream().map(id -> ops.createString(id.toString()));
    }

    @Override
    public int getRawId(T value) {
        return 0;
    }

    @Nullable
    @Override
    public T get(int index) {
        return null;
    }

    @Nullable
    public T get(@Nullable RegistryKey<T> key) {
        return null;
    }

    @Nullable
    public T get(@Nullable Identifier id) {
        return null;
    }

    public IndexedIterable<RegistryEntry.Reference<T>> getIndexedEntries() {
        return new IndexedIterable<>() {
            public int getRawId(RegistryEntry.Reference<T> registryEntry) {
                return Registry.this.getRawId(registryEntry.value());
            }

            public RegistryEntry.Reference<T> get(int i) {
                return (RegistryEntry.Reference<T>) Registry.this.getEntry(i).orElse(null);
            }

            public int size() {
                return Registry.this.size();
            }

            public Iterator<RegistryEntry.Reference<T>> iterator() {
                return Registry.this.streamEntries()/*.map(entry -> entry)*/.iterator();
            }
        };
    }

    public abstract Optional<RegistryEntry<T>> getEntry(int rawId);

    public abstract Optional<RegistryEntry<T>> getEntry(RegistryKey<T> key);

    public abstract Stream<RegistryEntry.Reference<T>> streamEntries();

    @NotNull
    @Override
    public abstract Iterator<T> iterator();

    @Nullable
    public abstract Identifier getId(T value);

    public abstract Set<Identifier> getIds();

    public RegistryKey<? extends Registry<T>> getKey() {
        return this.registryKey;
    }

    public abstract Optional<RegistryKey<T>> getKey(T entry);

    public Optional<T> getOrEmpty(@Nullable Identifier id) {
        return Optional.ofNullable(this.get(id));
    }

    public abstract Lifecycle getEntryLifecycle(T entry);



    public Codec<T> getCodec() {
        Codec<T> codec = Identifier.CODEC
                .flatXmap(
                        id -> Optional.ofNullable(this.get(id))
                                .map(DataResult::success)
                                .orElseGet(() -> DataResult.error("Unknown registry key in " + this.registryKey + ": " + id)),
                        value -> this.getKey((T)value)
                                .map(RegistryKey::getValue)
                                .map(DataResult::success)
                                .orElseGet(() -> DataResult.error("Unknown registry element in " + this.registryKey + ":" + value))
                );
        Codec<T> codec2 = Codecs.rawIdChecked(value -> this.getKey((T)value).isPresent() ? this.getRawId((T)value) : -1, this::get, -1);
        return Codecs.withLifecycle(Codecs.orCompressed(codec, codec2), this::getEntryLifecycle, value -> this.lifecycle);
    }

    @FunctionalInterface
    interface DefaultEntryGetter<T> {
        T run(Registry<T> registry);
    }

    static {
        BuiltinRegistries.init();
        DEFAULT_ENTRIES.forEach((id, defaultEntry) -> {
            if (defaultEntry.get() == null) {
                LOGGER.error("Unable to bootstrap registry '{}'", id);
            }

        });
        validate(ROOT);
    }

}
