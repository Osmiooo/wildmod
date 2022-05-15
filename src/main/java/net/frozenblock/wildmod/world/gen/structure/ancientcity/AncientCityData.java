package net.frozenblock.wildmod.world.gen.structure.ancientcity;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.frozenblock.wildmod.WildMod;
import net.frozenblock.wildmod.registry.RegisterWorldgen;
import net.frozenblock.wildmod.world.gen.structure.StructurePoolElement;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePools;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.RegistryEntry;

public class AncientCityData
{
    public static final RegistryEntry<StructurePool> CITY_CENTER = StructurePools.register(
            new StructurePool(
                    new Identifier(WildMod.MOD_ID, "ancient_city/city_center"),
                    new Identifier("empty"),
                    ImmutableList.of(
                            Pair.of(
                                    StructurePoolElement.ofProcessedSingle("ancient_city/city_center/city_center_1", RegisterWorldgen.ANCIENT_CITY_START_DEGRADATION), 1
                            ),
                            Pair.of(
                                    StructurePoolElement.ofProcessedSingle("ancient_city/city_center/city_center_2", RegisterWorldgen.ANCIENT_CITY_START_DEGRADATION), 1
                            ),
                            Pair.of(
                                    StructurePoolElement.ofProcessedSingle("ancient_city/city_center/city_center_3", RegisterWorldgen.ANCIENT_CITY_START_DEGRADATION), 1
                            )
                    ),
                    StructurePool.Projection.RIGID
            )
    );

    public AncientCityData() {
    }

    public static void init() {
        AncientCityOutskirtsData.init();
    }
}
