package net.frozenblock.wildmod.world.gen.structure;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePiecesList;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.FeatureConfig;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public final class WildStructureStart<C extends FeatureConfig> {
    public static final String INVALID = "INVALID";
    public static final WildStructureStart<?> DEFAULT = new WildStructureStart<>(null, new ChunkPos(0, 0), 0, new StructurePiecesList(List.of()));
    private final WildStructureFeature<C> feature;
    private final StructurePiecesList children;
    private final ChunkPos pos;
    /**
     * The number of chunks that intersect the structures bounding box,
     * and have stored references to its starting chunk.
     * <p>
     * This number can be lower than the number of <em>potential</em>
     * intersecting chunks, since it is only updated when an actual reference
     * is created in such chunks (when they enter the corresponding chunk generation
     * phase).
     */
    private int references;
    @Nullable
    private volatile BlockBox boundingBox;

    public WildStructureStart(WildStructureFeature<C> feature, ChunkPos pos, int references, StructurePiecesList children) {
        this.feature = feature;
        this.pos = pos;
        this.references = references;
        this.children = children;
    }

    public BlockBox getBoundingBox() {
        BlockBox blockBox = this.boundingBox;
        if (blockBox == null) {
            this.boundingBox = blockBox = this.feature.calculateBoundingBox(this.children.getBoundingBox());
        }
        return blockBox;
    }

    public void place(StructureWorldAccess world, StructureAccessor structureAccessor, ChunkGenerator chunkGenerator, Random random, BlockBox chunkBox, ChunkPos chunkPos) {
        List<StructurePiece> list = this.children.pieces();
        if (list.isEmpty()) {
            return;
        }
        BlockBox blockBox = list.get(0).getBoundingBox();
        BlockPos blockPos = blockBox.getCenter();
        BlockPos blockPos2 = new BlockPos(blockPos.getX(), blockBox.getMinY(), blockPos.getZ());
        for (StructurePiece structurePiece : list) {
            if (!structurePiece.getBoundingBox().intersects(chunkBox)) continue;
            structurePiece.generate(world, structureAccessor, chunkGenerator, random, chunkBox, chunkPos, blockPos2);
        }
        this.feature.getPostProcessor().afterPlace(world, structureAccessor, chunkGenerator, random, chunkBox, chunkPos, this.children);
    }

    public NbtCompound toNbt(StructureContext context, ChunkPos chunkPos) {
        NbtCompound nbtCompound = new NbtCompound();
        if (!this.hasChildren()) {
            nbtCompound.putString("id", INVALID);
            return nbtCompound;
        }
        nbtCompound.putString("id", Registry.STRUCTURE_FEATURE.getId(this.getFeature()).toString());
        nbtCompound.putInt("ChunkX", chunkPos.x);
        nbtCompound.putInt("ChunkZ", chunkPos.z);
        nbtCompound.putInt("references", this.references);
        nbtCompound.put("Children", this.children.toNbt(context));
        return nbtCompound;
    }

    public boolean hasChildren() {
        return !this.children.isEmpty();
    }

    public ChunkPos getPos() {
        return this.pos;
    }

    public boolean isInExistingChunk() {
        return this.references < this.getReferenceCountToBeInExistingChunk();
    }

    public void incrementReferences() {
        ++this.references;
    }

    public int getReferences() {
        return this.references;
    }

    protected int getReferenceCountToBeInExistingChunk() {
        return 1;
    }

    public WildStructureFeature<C> getFeature() {
        return this.feature;
    }

    public List<StructurePiece> getChildren() {
        return this.children.pieces();
    }
}

