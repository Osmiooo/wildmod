package frozenblock.wild.mod.worldgen;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.StructureWeightType;

public class PoolElementStructurePieceWeightType extends PoolStructurePiece {
    public StructureWeightType weightType;

    public PoolElementStructurePieceWeightType(StructureManager structureManager, StructurePoolElement structurePoolElement, BlockPos blockPos, int i, BlockRotation blockRotation, BlockBox blockBox, StructureWeightType structureWeightType) {
        super(structureManager, structurePoolElement, blockPos, i, blockRotation, blockBox);
        this.weightType = structureWeightType;
    }

    public PoolElementStructurePieceWeightType(StructureContext structureContext, NbtCompound nbtCompound) {
        super(structureContext, nbtCompound);
        this.weightType = StructureWeightType.valueOf(nbtCompound.getString("noise_effect"));
    }

    protected void writeNbt(StructureContext context, NbtCompound nbt) {
        super.writeNbt(context, nbt);
        nbt.putString("noise_effect", this.weightType.name());
    }

    public StructureWeightType getWeightType() {
        return this.weightType;
    }
}
