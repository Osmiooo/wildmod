package net.frozenblock.wildmod.world.feature.features;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.frozenblock.wildmod.world.feature.WildTrunkPlacer;
import net.frozenblock.wildmod.world.feature.foliage.WildFoliagePlacer;
import net.frozenblock.wildmod.world.gen.root.RootPlacer;
import net.minecraft.block.Blocks;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.size.FeatureSize;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.treedecorator.TreeDecorator;

import java.util.List;
import java.util.Optional;

public class WildTreeFeatureConfig implements FeatureConfig {
    public static final Codec<WildTreeFeatureConfig> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            BlockStateProvider.TYPE_CODEC.fieldOf("trunk_provider").forGetter(config -> config.trunkProvider),
                            WildTrunkPlacer.TYPE_CODEC.fieldOf("trunk_placer").forGetter(config -> config.trunkPlacer),
                            BlockStateProvider.TYPE_CODEC.fieldOf("foliage_provider").forGetter(config -> config.foliageProvider),
                            WildFoliagePlacer.TYPE_CODEC.fieldOf("foliage_placer").forGetter(config -> config.foliagePlacer),
                            RootPlacer.TYPE_CODEC.optionalFieldOf("root_placer").forGetter(config -> config.rootPlacer),
                            BlockStateProvider.TYPE_CODEC.fieldOf("dirt_provider").forGetter(config -> config.dirtProvider),
                            FeatureSize.TYPE_CODEC.fieldOf("minimum_size").forGetter(config -> config.minimumSize),
                            TreeDecorator.TYPE_CODEC.listOf().fieldOf("decorators").forGetter(config -> config.decorators),
                            Codec.BOOL.fieldOf("ignore_vines").orElse(false).forGetter(config -> config.ignoreVines),
                            Codec.BOOL.fieldOf("force_dirt").orElse(false).forGetter(config -> config.forceDirt)
                    )
                    .apply(instance, WildTreeFeatureConfig::new)
    );
    public final BlockStateProvider trunkProvider;
    public final BlockStateProvider dirtProvider;
    public final WildTrunkPlacer trunkPlacer;
    public final BlockStateProvider foliageProvider;
    public final WildFoliagePlacer foliagePlacer;
    public final Optional<RootPlacer> rootPlacer;
    public final FeatureSize minimumSize;
    public final List<TreeDecorator> decorators;
    public final boolean ignoreVines;
    public final boolean forceDirt;

    protected WildTreeFeatureConfig(
            BlockStateProvider trunkProvider,
            WildTrunkPlacer trunkPlacer,
            BlockStateProvider foliageProvider,
            WildFoliagePlacer foliagePlacer,
            Optional<RootPlacer> rootPlacer,
            BlockStateProvider dirtProvider,
            FeatureSize minimumSize,
            List<TreeDecorator> decorators,
            boolean ignoreVines,
            boolean forceDirt
    ) {
        this.trunkProvider = trunkProvider;
        this.trunkPlacer = trunkPlacer;
        this.foliageProvider = foliageProvider;
        this.foliagePlacer = foliagePlacer;
        this.rootPlacer = rootPlacer;
        this.dirtProvider = dirtProvider;
        this.minimumSize = minimumSize;
        this.decorators = decorators;
        this.ignoreVines = ignoreVines;
        this.forceDirt = forceDirt;
    }

    public static class Builder {
        public final BlockStateProvider trunkProvider;
        private final WildTrunkPlacer trunkPlacer;
        public final BlockStateProvider foliageProvider;
        private final WildFoliagePlacer foliagePlacer;
        private final Optional<RootPlacer> rootPlacer;
        private BlockStateProvider dirtProvider;
        private final FeatureSize minimumSize;
        private List<TreeDecorator> decorators = ImmutableList.of();
        private boolean ignoreVines;
        private boolean forceDirt;

        public Builder(
                BlockStateProvider trunkProvider,
                WildTrunkPlacer trunkPlacer,
                BlockStateProvider foliageProvider,
                WildFoliagePlacer foliagePlacer,
                Optional<RootPlacer> rootPlacer,
                FeatureSize minimumSize
        ) {
            this.trunkProvider = trunkProvider;
            this.trunkPlacer = trunkPlacer;
            this.foliageProvider = foliageProvider;
            this.dirtProvider = BlockStateProvider.of(Blocks.DIRT);
            this.foliagePlacer = foliagePlacer;
            this.rootPlacer = rootPlacer;
            this.minimumSize = minimumSize;
        }

        public Builder(
                BlockStateProvider trunkProvider, WildTrunkPlacer trunkPlacer, BlockStateProvider foliageProvider, WildFoliagePlacer foliagePlacer, FeatureSize minimumSize
        ) {
            this(trunkProvider, trunkPlacer, foliageProvider, foliagePlacer, Optional.empty(), minimumSize);
        }

        public WildTreeFeatureConfig.Builder dirtProvider(BlockStateProvider dirtProvider) {
            this.dirtProvider = dirtProvider;
            return this;
        }

        public WildTreeFeatureConfig.Builder decorators(List<TreeDecorator> decorators) {
            this.decorators = decorators;
            return this;
        }

        public WildTreeFeatureConfig.Builder ignoreVines() {
            this.ignoreVines = true;
            return this;
        }

        public WildTreeFeatureConfig.Builder forceDirt() {
            this.forceDirt = true;
            return this;
        }

        public WildTreeFeatureConfig build() {
            return new WildTreeFeatureConfig(
                    this.trunkProvider,
                    this.trunkPlacer,
                    this.foliageProvider,
                    this.foliagePlacer,
                    this.rootPlacer,
                    this.dirtProvider,
                    this.minimumSize,
                    this.decorators,
                    this.ignoreVines,
                    this.forceDirt
            );
        }
    }
}
