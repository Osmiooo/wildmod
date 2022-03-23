package frozenblock.wild.mod.worldgen;

import com.mojang.serialization.Codec;
import net.minecraft.structure.StructureGeneratorFactory;
import net.minecraft.world.gen.StructureWeightType;
import net.minecraft.world.gen.feature.JigsawFeature;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;
import net.minecraft.world.gen.random.AtomicSimpleRandom;
import net.minecraft.world.gen.random.ChunkRandom;

public class AncientCityFeature extends JigsawFeature {
    public static final int MAX_PIECE_SIZE = 128;

    @
    public AncientCityFeature(Codec<StructurePoolFeatureConfig> codec) {
        super(codec, (structureManager, structurePoolElement, blockPos, i, blockRotation, blockBox) -> {
            return new PoolElementStructurePieceWeightType(structureManager, structurePoolElement, blockPos, i, blockRotation, blockBox, StructureWeightType.BEARD_AND_SHAVE);
        }, (random) -> {
            return -40;
        }, false, false, AncientCityFeature::method_40836, 128);
    }

    private static boolean method_40836(StructureGeneratorFactory.Context<StructurePoolFeatureConfig> context) {
        ChunkRandom chunkRandom = new ChunkRandom(new AtomicSimpleRandom(0L));
        chunkRandom.setCarverSeed(context.seed(), context.chunkPos().x, context.chunkPos().z);
        return chunkRandom.nextInt(5) >= 2;
    }
}
