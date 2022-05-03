package net.frozenblock.wildmod.registry;

import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.frozenblock.wildmod.world.gen.random.WildAbstractRandom;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SimpleRegistry<T> extends MutableRegistry<T> {
    private static final Logger field_36635 = LogUtils.getLogger();
    private final ObjectList<RegistryEntry.Reference<T>> rawIdToEntry = new ObjectArrayList<>(256);
    private final Object2IntMap<T> entryToRawId = Util.make(
            new Object2IntOpenCustomHashMap<T>(Util.identityHashStrategy()), object2IntOpenCustomHashMap -> object2IntOpenCustomHashMap.defaultReturnValue(-1)
    );
    private final Map<Identifier, RegistryEntry.Reference<T>> idToEntry = new HashMap<>();
    private final Map<RegistryKey<T>, RegistryEntry.Reference<T>> keyToEntry = new HashMap<>();
    private final Map<T, RegistryEntry.Reference<T>> valueToEntry = new IdentityHashMap<>();
    private final Map<T, Lifecycle> entryToLifecycle = new IdentityHashMap<>();
    private Lifecycle lifecycle;
    private volatile Map<net.minecraft.tag.TagKey<T>, RegistryEntryList.Named<T>> tagToEntryList = new IdentityHashMap<>();
    private boolean frozen;
    @Nullable
    private final Function<T, RegistryEntry.Reference<T>> valueToEntryFunction;
    @Nullable
    private Map<T, RegistryEntry.Reference<T>> unfrozenValueToEntry;
    @Nullable
    private List<RegistryEntry.Reference<T>> cachedEntries;
    private int nextId;

    public SimpleRegistry(RegistryKey<? extends Registry<T>> key, Lifecycle lifecycle, @Nullable Function<T, RegistryEntry.Reference<T>> valueToEntryFunction) {
        super(key, lifecycle);
        this.lifecycle = lifecycle;
        this.valueToEntryFunction = valueToEntryFunction;
        if (valueToEntryFunction != null) {
            this.unfrozenValueToEntry = new IdentityHashMap<>();
        }

    }

    private List<RegistryEntry.Reference<T>> getEntries() {
        if (this.cachedEntries == null) {
            this.cachedEntries = this.rawIdToEntry.stream().filter(Objects::nonNull).toList();
        }

        return this.cachedEntries;
    }

    private void assertNotFrozen(RegistryKey<T> key) {
        if (this.frozen) {
            throw new IllegalStateException("Registry is already frozen (trying to add key " + key + ")");
        }
    }

    public RegistryEntry<T> set(int rawId, RegistryKey<T> key, T value, Lifecycle lifecycle) {
        return this.set(rawId, key, value, lifecycle, true);
    }

    private RegistryEntry<T> set(int rawId, RegistryKey<T> key, T value, Lifecycle lifecycle, boolean checkDuplicateKeys) {
        this.assertNotFrozen(key);
        Validate.notNull(key);
        Validate.notNull(value);
        this.rawIdToEntry.size(Math.max(this.rawIdToEntry.size(), rawId + 1));
        this.entryToRawId.put(value, rawId);
        this.cachedEntries = null;
        if (checkDuplicateKeys && this.keyToEntry.containsKey(key)) {
            Util.error("Adding duplicate key '" + key + "' to registry");
        }

        if (this.valueToEntry.containsKey(value)) {
            Util.error("Adding duplicate value '" + value + "' to registry");
        }

        this.entryToLifecycle.put(value, lifecycle);
        this.lifecycle = this.lifecycle.add(lifecycle);
        if (this.nextId <= rawId) {
            this.nextId = rawId + 1;
        }

        RegistryEntry.Reference<T> reference;
        if (this.valueToEntryFunction != null) {
            reference = (RegistryEntry.Reference)this.valueToEntryFunction.apply(value);
            RegistryEntry.Reference<T> reference2 = this.keyToEntry.put(key, reference);
            if (reference2 != null && reference2 != reference) {
                throw new IllegalStateException("Invalid holder present for key " + key);
            }
        } else {
            reference = (RegistryEntry.Reference)this.keyToEntry.computeIfAbsent(key, keyx -> RegistryEntry.Reference.standAlone(this, keyx));
        }

        this.idToEntry.put(key.getValue(), reference);
        this.valueToEntry.put(value, reference);
        reference.setKeyAndValue(key, value);
        this.rawIdToEntry.set(rawId, reference);
        return reference;
    }

    public RegistryEntry<T> add(RegistryKey<T> key, T entry, Lifecycle lifecycle) {
        return this.set(this.nextId, key, entry, lifecycle);
    }

    public RegistryEntry<T> replace(OptionalInt rawId, RegistryKey<T> key, T newEntry, Lifecycle lifecycle) {
        this.assertNotFrozen(key);
        Validate.notNull(key);
        Validate.notNull(newEntry);
        RegistryEntry<T> registryEntry = this.keyToEntry.get(key);
        T object = (T)(registryEntry != null && registryEntry.hasKeyAndValue() ? registryEntry.value() : null);
        int i;
        if (object == null) {
            i = rawId.orElse(this.nextId);
        } else {
            i = this.entryToRawId.getInt(object);
            if (rawId.isPresent() && rawId.getAsInt() != i) {
                throw new IllegalStateException("ID mismatch");
            }

            this.entryToLifecycle.remove(object);
            this.entryToRawId.removeInt(object);
            this.valueToEntry.remove(object);
        }

        return this.set(i, key, newEntry, lifecycle, false);
    }

    @Nullable
    public Identifier getId(T value) {
        RegistryEntry.Reference<T> reference = this.valueToEntry.get(value);
        return reference != null ? reference.registryKey().getValue() : null;
    }

    public Optional<RegistryKey<T>> getKey(T entry) {
        return Optional.ofNullable((RegistryEntry.Reference)this.valueToEntry.get(entry)).map(RegistryEntry.Reference::registryKey);
    }

    public int getRawId(@Nullable T value) {
        return this.entryToRawId.getInt(value);
    }

    @Nullable
    public T get(@Nullable RegistryKey<T> key) {
        return getValue((RegistryEntry.Reference<T>)this.keyToEntry.get(key));
    }

    @Nullable
    public T get(int index) {
        return index >= 0 && index < this.rawIdToEntry.size() ? getValue((RegistryEntry.Reference<T>)this.rawIdToEntry.get(index)) : null;
    }

    public Optional<RegistryEntry<T>> getEntry(int rawId) {
        return rawId >= 0 && rawId < this.rawIdToEntry.size() ? Optional.ofNullable((RegistryEntry)this.rawIdToEntry.get(rawId)) : Optional.empty();
    }

    public Optional<RegistryEntry<T>> getEntry(RegistryKey<T> key) {
        return Optional.ofNullable(this.keyToEntry.get(key));
    }

    public RegistryEntry<T> getOrCreateEntry(RegistryKey<T> key) {
        return (RegistryEntry<T>)this.keyToEntry.computeIfAbsent(key, keyx -> {
            if (this.valueToEntryFunction != null) {
                throw new IllegalStateException("This registry can't create new holders without value");
            } else {
                this.assertNotFrozen(keyx);
                return RegistryEntry.Reference.standAlone(this, keyx);
            }
        });
    }

    public int size() {
        return this.keyToEntry.size();
    }

    public Lifecycle getEntryLifecycle(T entry) {
        return (Lifecycle)this.entryToLifecycle.get(entry);
    }

    public Lifecycle getLifecycle() {
        return this.lifecycle;
    }

    public Iterator<T> iterator() {
        return Iterators.transform(this.getEntries().iterator(), RegistryEntry::value);
    }

    @Nullable
    public T get(@Nullable Identifier id) {
        RegistryEntry.Reference<T> reference = this.idToEntry.get(id);
        return getValue(reference);
    }

    @Nullable
    private static <T> T getValue(@Nullable RegistryEntry.Reference<T> entry) {
        return (T)(entry != null ? entry.value() : null);
    }

    public Set<Identifier> getIds() {
        return Collections.unmodifiableSet(this.idToEntry.keySet());
    }

    public Set<RegistryKey<T>> getKeys() {
        return Collections.unmodifiableSet(this.keyToEntry.keySet());
    }

    public Set<Map.Entry<RegistryKey<T>, T>> getEntrySet() {
        return Collections.unmodifiableSet(Maps.transformValues(this.keyToEntry, RegistryEntry::value).entrySet());
    }

    @Override
    public Stream<RegistryEntry.Reference<T>> streamEntries() {
        return this.getEntries().stream();
    }

    public boolean containsTag(net.minecraft.tag.TagKey<T> tag) {
        return this.tagToEntryList.containsKey(tag);
    }

    public Stream<Pair<net.minecraft.tag.TagKey<T>, RegistryEntryList.Named<T>>> streamTagsAndEntries() {
        return this.tagToEntryList.entrySet().stream().map(entry -> Pair.of(entry.getKey(), entry.getValue()));
    }

    public RegistryEntryList.Named<T> getOrCreateEntryList(net.minecraft.tag.TagKey<T> tag) {
        RegistryEntryList.Named<T> named = this.tagToEntryList.get(tag);
        if (named == null) {
            named = this.createNamedEntryList(tag);
            Map<net.minecraft.tag.TagKey<T>, RegistryEntryList.Named<T>> map = new IdentityHashMap<>(this.tagToEntryList);
            map.put(tag, named);
            this.tagToEntryList = map;
        }

        return named;
    }

    private RegistryEntryList.Named<T> createNamedEntryList(net.minecraft.tag.TagKey<T> tag) {
        return new RegistryEntryList.Named<>(this, tag);
    }

    public Stream<net.minecraft.tag.TagKey<T>> streamTags() {
        return this.tagToEntryList.keySet().stream();
    }

    public boolean isEmpty() {
        return this.keyToEntry.isEmpty();
    }

    public Optional<RegistryEntry<T>> getRandom(WildAbstractRandom random) {
        return net.frozenblock.wildmod.registry.Util.getRandomOrEmpty(this.getEntries(), random).map(RegistryEntry::upcast);
    }

    public boolean containsId(Identifier id) {
        return this.idToEntry.containsKey(id);
    }

    public boolean contains(RegistryKey<T> key) {
        return this.keyToEntry.containsKey(key);
    }

    public Registry<T> freeze() {
        this.frozen = true;
        List<Identifier> list = this.keyToEntry
                .entrySet()
                .stream()
                .filter(entry -> !entry.getValue().hasKeyAndValue())
                .map(entry -> entry.getKey().getValue())
                .sorted()
                .toList();
        if (!list.isEmpty()) {
            throw new IllegalStateException("Unbound values in registry " + this.getKey() + ": " + list);
        } else {
            if (this.unfrozenValueToEntry != null) {
                List<RegistryEntry.Reference<T>> list2 = this.unfrozenValueToEntry.values().stream().filter(entry -> !entry.hasKeyAndValue()).toList();
                if (!list2.isEmpty()) {
                    throw new IllegalStateException("Some intrusive holders were not added to registry: " + list2);
                }

                this.unfrozenValueToEntry = null;
            }

            return this;
        }
    }

    public RegistryEntry.Reference<T> createEntry(T value) {
        if (this.valueToEntryFunction == null) {
            throw new IllegalStateException("This registry can't create intrusive holders");
        } else if (!this.frozen && this.unfrozenValueToEntry != null) {
            return (RegistryEntry.Reference<T>)this.unfrozenValueToEntry.computeIfAbsent(value, key -> RegistryEntry.Reference.intrusive(this, key));
        } else {
            throw new IllegalStateException("Registry is already frozen");
        }
    }

    public Optional<RegistryEntryList.Named<T>> getEntryList(TagKey<T> tag) {
        return Optional.ofNullable(this.tagToEntryList.get(tag));
    }

    public void populateTags(Map<net.minecraft.tag.TagKey<T>, List<RegistryEntry<T>>> tagEntries) {
        Map<RegistryEntry.Reference<T>, List<net.minecraft.tag.TagKey<T>>> map = new IdentityHashMap<>();
        this.keyToEntry.values().forEach(entry -> map.put(entry, new ArrayList<>()));
        tagEntries.forEach((tag, entries) -> {
            for(RegistryEntry<T> registryEntry : entries) {
                if (!registryEntry.matchesRegistry(this)) {
                    throw new IllegalStateException("Can't create named set " + tag + " containing value " + registryEntry + " from outside registry " + this);
                }

                if (!(registryEntry instanceof RegistryEntry.Reference)) {
                    throw new IllegalStateException("Found direct holder " + registryEntry + " value in tag " + tag);
                }

                RegistryEntry.Reference<T> reference = (RegistryEntry.Reference<T>)registryEntry;
                (map.get(reference)).add(tag);
            }

        });
        Set<net.minecraft.tag.TagKey<T>> set = Sets.difference(this.tagToEntryList.keySet(), tagEntries.keySet());
        if (!set.isEmpty()) {
            field_36635.warn(
                    "Not all defined tags for registry {} are present in data pack: {}",
                    this.getKey(),
                    set.stream().map(tag -> tag.id().toString()).sorted().collect(Collectors.joining(", "))
            );
        }

        Map<net.minecraft.tag.TagKey<T>, RegistryEntryList.Named<T>> map2 = new IdentityHashMap<>(this.tagToEntryList);
        tagEntries.forEach((tag, entries) -> (map2.computeIfAbsent(tag, this::createNamedEntryList)).copyOf(entries));
        map.forEach(RegistryEntry.Reference::setTags);
        this.tagToEntryList = map2;
    }

    public void clearTags() {
        this.tagToEntryList.values().forEach(entryList -> entryList.copyOf(List.of()));
        this.keyToEntry.values().forEach(entry -> entry.setTags(Set.of()));
    }
}