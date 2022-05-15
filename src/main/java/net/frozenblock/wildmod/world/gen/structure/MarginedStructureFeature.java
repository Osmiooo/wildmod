package net.frozenblock.wildmod.world.gen.structure;

import com.mojang.serialization.Codec;
import net.minecraft.structure.PostPlacementProcessor;
import net.minecraft.structure.StructureGeneratorFactory;
import net.minecraft.util.math.BlockBox;
import net.minecraft.world.gen.feature.FeatureConfig;

public abstract class MarginedStructureFeature<C extends FeatureConfig> extends WildStructureFeature<C> {
    public MarginedStructureFeature(Codec<C> codec, StructureGeneratorFactory<C> structureGeneratorFactory) {
        super(codec, structureGeneratorFactory);
    }

    public MarginedStructureFeature(Codec<C> codec, StructureGeneratorFactory<C> structureGeneratorFactory, PostPlacementProcessor postPlacementProcessor) {
        super(codec, structureGeneratorFactory, postPlacementProcessor);
    }

    @Override
    public BlockBox calculateBoundingBox(BlockBox box) {
        return super.calculateBoundingBox(box).expand(12);
    }
}

