package net.frozenblock.wildmod.block.deepdark;

import net.frozenblock.wildmod.registry.RegisterBlocks;
import net.frozenblock.wildmod.world.gen.SculkSpreadManager;
import net.frozenblock.wildmod.world.gen.SculkSpreadManager.Cursor;
import net.frozenblock.wildmod.world.gen.SculkSpreadable;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.OreBlock;
import net.minecraft.fluid.Fluids;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.WorldAccess;

import java.util.Random;

public class SculkBlock extends OreBlock implements SculkSpreadable {
    public SculkBlock(Settings settings) {
        super(settings, UniformIntProvider.create(1, 1));
    }

    public int spread(
            Cursor cursor, WorldAccess world, BlockPos catalystPos, Random random, SculkSpreadManager spreadManager, boolean shouldConvertToBlock
    ) {
        int i = cursor.getCharge();
        if (i != 0 && random.nextInt(spreadManager.getSpreadChance()) == 0) {
            BlockPos blockPos = cursor.getPos();
            boolean bl = blockPos.isWithinDistance(catalystPos, spreadManager.getMaxDistance());
            if (!bl && shouldNotDecay(world, blockPos)) {
                int j = spreadManager.getExtraBlockChance();
                if (random.nextInt(j) < i) {
                    BlockPos blockPos2 = blockPos.up();
                    BlockState blockState = this.getExtraBlockState(world, blockPos2, random, spreadManager.isWorldGen());
                    world.setBlockState(blockPos2, blockState, 3);
                    world.playSound(null, blockPos, blockState.getSoundGroup().getPlaceSound(), SoundCategory.BLOCKS, 1.0F, 1.0F);
                }

                return Math.max(0, i - j);
            } else {
                return random.nextInt(spreadManager.getDecayChance()) != 0 ? i : i - (bl ? 1 : getDecay(spreadManager, blockPos, catalystPos, i));
            }
        } else {
            return i;
        }
    }

    private static int getDecay(SculkSpreadManager spreadManager, BlockPos cursorPos, BlockPos catalystPos, int charge) {
        int i = spreadManager.getMaxDistance();
        float f = MathHelper.square((float) Math.sqrt(cursorPos.getSquaredDistance(catalystPos)) - (float) i);
        int j = MathHelper.square(24 - i);
        float g = Math.min(1.0F, f / (float) j);
        return Math.max(1, (int) ((float) charge * g * 0.5F));
    }

    private BlockState getExtraBlockState(WorldAccess world, BlockPos pos, Random random, boolean allowShrieker) {
        BlockState blockState;
        if (random.nextInt(11) == 0) {
            blockState = RegisterBlocks.SCULK_SHRIEKER.getDefaultState().with(SculkShriekerBlock.CAN_SUMMON, allowShrieker);
        } else {
            blockState = Blocks.SCULK_SENSOR.getDefaultState();
        }

        return blockState.contains(Properties.WATERLOGGED) && !world.getFluidState(pos).isEmpty()
                ? blockState.with(Properties.WATERLOGGED, true)
                : blockState;
    }

    private static boolean shouldNotDecay(WorldAccess world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos.up());
        if (blockState.isAir() || blockState.isOf(Blocks.WATER) && blockState.getFluidState().isOf(Fluids.WATER)) {
            int i = 0;

            for (BlockPos blockPos : BlockPos.iterate(pos.add(-4, 0, -4), pos.add(4, 2, 4))) {
                BlockState blockState2 = world.getBlockState(blockPos);
                if (blockState2.isOf(Blocks.SCULK_SENSOR) || blockState2.isOf(RegisterBlocks.SCULK_SHRIEKER)) {
                    ++i;
                }

                if (i > 2) {
                    return false;
                }
            }

            return true;
        } else {
            return false;
        }
    }

    public boolean shouldConvertToSpreadable() {
        return false;
    }
}
