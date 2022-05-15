package net.frozenblock.wildmod.world.gen.structure;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.StructureGeneratorFactory;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructurePiecesGenerator;
import net.minecraft.structure.pool.*;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.StructureFeature;
import net.minecraft.world.gen.feature.StructurePoolFeatureConfig;
import net.minecraft.world.gen.random.AtomicSimpleRandom;
import net.minecraft.world.gen.random.ChunkRandom;
import org.apache.commons.lang3.mutable.MutableObject;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Predicate;

public class WildJigsawFeature
        extends MarginedStructureFeature<StructurePoolFeatureConfig> {
    private static final Random field_36903 = new Random();

    public WildJigsawFeature(Codec<StructurePoolFeatureConfig> codec, int structureStartY, boolean modifyBoundingBox, boolean surface, Predicate<StructureGeneratorFactory.Context<StructurePoolFeatureConfig>> predicate) {
        this(codec, PoolStructurePiece::new, random -> structureStartY, modifyBoundingBox, surface, predicate, 80);
    }

    public WildJigsawFeature(Codec<StructurePoolFeatureConfig> codec, StructurePoolBasedGenerator.PieceFactory pieceFactory, Function<Random, Integer> function, boolean bl, boolean bl2, Predicate<StructureGeneratorFactory.Context<StructurePoolFeatureConfig>> predicate, int i) {
        super(codec, context -> {
            if (!predicate.test(context)) {
                return Optional.empty();
            }
            BlockPos blockPos = new BlockPos(context.chunkPos().getStartX(), (Integer)function.apply(field_36903), context.chunkPos().getStartZ());
            StructurePools.initDefaultPools();
            return generate(context, pieceFactory, blockPos, bl, bl2, i);
        });
    }

    public static Optional<StructurePiecesGenerator<StructurePoolFeatureConfig>> generate(StructureGeneratorFactory.Context<StructurePoolFeatureConfig> context2, StructurePoolBasedGenerator.PieceFactory pieceFactory, BlockPos pos, boolean bl, boolean bl2, int i) {
        ChunkRandom chunkRandom = new ChunkRandom(new AtomicSimpleRandom(0L));
        chunkRandom.setCarverSeed(context2.seed(), context2.chunkPos().x, context2.chunkPos().z);
        DynamicRegistryManager dynamicRegistryManager = context2.registryManager();
        StructurePoolFeatureConfig structurePoolFeatureConfig = context2.config();
        ChunkGenerator chunkGenerator = context2.chunkGenerator();
        StructureManager structureManager = context2.structureManager();
        HeightLimitView heightLimitView = context2.world();
        Predicate<RegistryEntry<Biome>> predicate = context2.validBiome();
        StructureFeature.init();
        Registry<StructurePool> registry = dynamicRegistryManager.get(Registry.STRUCTURE_POOL_KEY);
        BlockRotation blockRotation = BlockRotation.random(chunkRandom);
        StructurePool structurePool = structurePoolFeatureConfig.getStartPool().value();
        StructurePoolElement structurePoolElement = structurePool.getRandomElement(chunkRandom);
        if (structurePoolElement == EmptyPoolElement.INSTANCE) {
            return Optional.empty();
        }
        PoolStructurePiece poolStructurePiece = pieceFactory.create(structureManager, structurePoolElement, pos, structurePoolElement.getGroundLevelDelta(), blockRotation, structurePoolElement.getBoundingBox(structureManager, pos, blockRotation));
        BlockBox blockBox = poolStructurePiece.getBoundingBox();
        int j = (blockBox.getMaxX() + blockBox.getMinX()) / 2;
        int k = (blockBox.getMaxZ() + blockBox.getMinZ()) / 2;
        int l = bl2 ? pos.getY() + chunkGenerator.getHeightOnGround(j, k, Heightmap.Type.WORLD_SURFACE_WG, heightLimitView) : pos.getY();
        if (!predicate.test(chunkGenerator.getBiomeForNoiseGen(BiomeCoords.fromBlock(j), BiomeCoords.fromBlock(l), BiomeCoords.fromBlock(k)))) {
            return Optional.empty();
        }
        int m = blockBox.getMinY() + poolStructurePiece.getGroundLevelDelta();
        poolStructurePiece.translate(0, l - m, 0);
        return Optional.of((structurePiecesCollector, context) -> {
            ArrayList<PoolStructurePiece> list = Lists.newArrayList();
            list.add(poolStructurePiece);
            if (structurePoolFeatureConfig.getSize() <= 0) {
                return;
            }
            int sus = i;
            Box box = new Box(j - sus, l - sus, k - sus, j + sus + 1, l + sus + 1, k + sus + 1);
            StructurePoolBasedGenerator.StructurePoolGenerator structurePoolGenerator = new StructurePoolBasedGenerator.StructurePoolGenerator(registry, structurePoolFeatureConfig.getSize(), pieceFactory, chunkGenerator, structureManager, list, chunkRandom);
            structurePoolGenerator.structurePieces.addLast(new StructurePoolBasedGenerator.ShapedPoolStructurePiece(poolStructurePiece, new MutableObject<VoxelShape>(VoxelShapes.combineAndSimplify(VoxelShapes.cuboid(box), VoxelShapes.cuboid(Box.from(blockBox)), BooleanBiFunction.ONLY_FIRST)), 0));
            while (!structurePoolGenerator.structurePieces.isEmpty()) {
                StructurePoolBasedGenerator.ShapedPoolStructurePiece shapedPoolStructurePiece = structurePoolGenerator.structurePieces.removeFirst();
                structurePoolGenerator.generatePiece(shapedPoolStructurePiece.piece, shapedPoolStructurePiece.pieceShape, shapedPoolStructurePiece.currentSize, bl, heightLimitView);
            }
            list.forEach(structurePiecesCollector::addPiece);
        });
    }
}

