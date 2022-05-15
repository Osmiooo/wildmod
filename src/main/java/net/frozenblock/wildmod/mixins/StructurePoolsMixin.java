package net.frozenblock.wildmod.mixins;

import net.frozenblock.wildmod.world.gen.structure.ancientcity.AncientCityData;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePools;
import net.minecraft.util.registry.RegistryEntry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StructurePools.class)
public class StructurePoolsMixin {

    @Shadow
    @Final
    private static RegistryEntry<StructurePool> INVALID;

    @Inject(method = "initDefaultPools", at = @At("RETURN"))
    private static RegistryEntry<StructurePool> initDefaultPools(CallbackInfoReturnable<RegistryEntry<StructurePool>> cir) {
        AncientCityData.init();
        return INVALID;
    }
}
