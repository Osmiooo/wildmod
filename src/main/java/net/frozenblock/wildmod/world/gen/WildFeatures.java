package net.frozenblock.wildmod.world.gen;

import net.frozenblock.wildmod.WildMod;
import net.frozenblock.wildmod.mixins.TrunkPlacerTypeInvoker;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.trunk.TrunkPlacerType;

public class WildFeatures<FC extends FeatureConfig> {

    public static final TrunkPlacerType<UpwardsBranchingTrunkPlacer> UPWARDS_BRANCHING_TRUNK_PLACER = TrunkPlacerTypeInvoker.callRegister(
            "upwards_branching_trunk_placer", UpwardsBranchingTrunkPlacer.CODEC
    );

    public static Feature<SculkPatchFeatureConfig> SCULK_PATCH;
    public static final Feature<MultifaceGrowthFeatureConfig> MULTIFACE_GROWTH = register(
            "multiface_growth", new MultifaceGrowthFeature(MultifaceGrowthFeatureConfig.CODEC)
    );

    private static <C extends FeatureConfig, F extends Feature<C>> F register(String name, final F feature) {
        return Registry.register(Registry.FEATURE, new Identifier(WildMod.MOD_ID, name), feature);
    }

    static {
        SCULK_PATCH = register("sculk_patch", new SculkPatchFeature(SculkPatchFeatureConfig.CODEC));
    }
}