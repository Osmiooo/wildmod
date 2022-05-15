package net.frozenblock.wildmod;

import com.chocohead.mm.api.ClassTinkerers;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;

public class EarlyRiser implements Runnable {

    public static final MappingResolver remapper = FabricLoader.getInstance().getMappingResolver();
    public static final String entityPose = remapper.mapClassName("intermediary", "net.minecraft.class_4050");
    public static final String weightType = remapper.mapClassName("intermediary", "net.minecraft.class_5847");
    @Override
    public void run() {

        // EntityPose

        ClassTinkerers.enumBuilder(entityPose).addEnum("CROAKING").build();
        ClassTinkerers.enumBuilder(entityPose).addEnum("USING_TONGUE").build();
        ClassTinkerers.enumBuilder(entityPose).addEnum("ROARING").build();
        ClassTinkerers.enumBuilder(entityPose).addEnum("SNIFFING").build();
        ClassTinkerers.enumBuilder(entityPose).addEnum("EMERGING").build();
        ClassTinkerers.enumBuilder(entityPose).addEnum("DIGGING").build();

        // StructureWeightType

        ClassTinkerers.enumBuilder(weightType).addEnum("BEARD_AND_SHAVE").build();
    }
}
