package frozenblock.wild.mod.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import frozenblock.wild.mod.WildMod;
import frozenblock.wild.mod.registry.RegisterWorldgen;
import net.minecraft.block.Block;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.tag.ServerTagManagerHolder;
import net.minecraft.tag.Tag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class ReplaceInTagStructureProcessor extends StructureProcessor {
    public static final Codec<ReplaceInTagStructureProcessor> CODEC = RecordCodecBuilder.create((instance) -> instance.group(Tag.codec(() -> ServerTagManagerHolder.getTagManager().getOrCreateTagGroup(Registry.BLOCK_KEY)).fieldOf("rottable_blocks").forGetter((processor) -> processor.rottableBlocks), Codec.FLOAT.fieldOf("integrity").forGetter((processor) -> processor.integrity)).apply(instance, ReplaceInTagStructureProcessor::new));
    private final Tag<Block> rottableBlocks;
    private final float integrity;

    public ReplaceInTagStructureProcessor(Tag<Block> rottableBlocks, float integrity) {
        this.integrity = integrity;
        this.rottableBlocks = rottableBlocks;
    }

    @Nullable
    public Structure.StructureBlockInfo process(WorldView world, BlockPos pos, BlockPos pivot, Structure.StructureBlockInfo structureBlockInfo, Structure.StructureBlockInfo structureBlockInfo2, StructurePlacementData data) {
        Random random = data.getRandom(structureBlockInfo2.pos);
        return structureBlockInfo.state.isIn(this.rottableBlocks) && !(this.integrity >= 1.0F) && !(random.nextFloat() <= this.integrity) ? null : structureBlockInfo2;
    }

    protected StructureProcessorType<?> getType() {
        return WildMod.REPLACE_IN_TAG;
    }
}
