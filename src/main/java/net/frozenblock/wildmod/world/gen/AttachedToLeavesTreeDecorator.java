/*package net.frozenblock.wildmod.world.gen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Util;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.treedecorator.TreeDecorator;
import net.minecraft.world.gen.treedecorator.TreeDecoratorType;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class AttachedToLeavesTreeDecorator extends TreeDecorator {
    public static final Codec<AttachedToLeavesTreeDecorator> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            Codec.floatRange(0.0F, 1.0F).fieldOf("probability").forGetter(treeDecorator -> treeDecorator.probability),
                            Codec.intRange(0, 16).fieldOf("exclusion_radius_xz").forGetter(treeDecorator -> treeDecorator.exclusionRadiusXZ),
                            Codec.intRange(0, 16).fieldOf("exclusion_radius_y").forGetter(treeDecorator -> treeDecorator.exclusionRadiusY),
                            BlockStateProvider.TYPE_CODEC.fieldOf("block_provider").forGetter(treeDecorator -> treeDecorator.blockProvider),
                            Codec.intRange(1, 16).fieldOf("required_empty_blocks").forGetter(treeDecorator -> treeDecorator.requiredEmptyBlocks),
                            Codecs.nonEmptyList(Direction.CODEC.listOf()).fieldOf("directions").forGetter(treeDecorator -> treeDecorator.directions)
                    )
                    .apply(instance, AttachedToLeavesTreeDecorator::new)
    );
    protected final float probability;
    protected final int exclusionRadiusXZ;
    protected final int exclusionRadiusY;
    protected final BlockStateProvider blockProvider;
    protected final int requiredEmptyBlocks;
    protected final List<Direction> directions;

    public AttachedToLeavesTreeDecorator(
            float probability, int exclusionRadiusXZ, int exclusionRadiusY, BlockStateProvider blockProvider, int requiredEmptyBlocks, List<Direction> directions
    ) {
        this.probability = probability;
        this.exclusionRadiusXZ = exclusionRadiusXZ;
        this.exclusionRadiusY = exclusionRadiusY;
        this.blockProvider = blockProvider;
        this.requiredEmptyBlocks = requiredEmptyBlocks;
        this.directions = directions;
    }

    public void generate(Generator generator) {
        Set<BlockPos> set = new HashSet<>();
        Random abstractRandom = generator.getRandom();

        for(BlockPos blockPos : Util.copyShuffled(generator.getLeavesPositions(), abstractRandom)) {
            Direction direction = (Direction)Util.getRandom(this.directions, abstractRandom);
            BlockPos blockPos2 = blockPos.offset(direction);
            if (!set.contains(blockPos2) && abstractRandom.nextFloat() < this.probability && this.meetsRequiredEmptyBlocks(generator, blockPos, direction)) {
                BlockPos blockPos3 = blockPos2.add(-this.exclusionRadiusXZ, -this.exclusionRadiusY, -this.exclusionRadiusXZ);
                BlockPos blockPos4 = blockPos2.add(this.exclusionRadiusXZ, this.exclusionRadiusY, this.exclusionRadiusXZ);

                for(BlockPos blockPos5 : BlockPos.iterate(blockPos3, blockPos4)) {
                    set.add(blockPos5.toImmutable());
                }

                generator.replace(blockPos2, this.blockProvider.getBlockState(abstractRandom, blockPos2));
            }
        }

    }

    private boolean meetsRequiredEmptyBlocks(Generator generator, BlockPos pos, Direction direction) {
        for(int i = 1; i <= this.requiredEmptyBlocks; ++i) {
            BlockPos blockPos = pos.offset(direction, i);
            if (!generator.isAir(blockPos)) {
                return false;
            }
        }

        return true;
    }

    protected TreeDecoratorType<?> getType() {
        return TreeDecoratorType.ATTACHED_TO_LEAVES;
    }
}
*/