package net.frozenblock.wildmod.mixins;

import net.frozenblock.wildmod.liukrastapi.WildInput;
import net.minecraft.client.input.Input;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Input.class)
public class InputMixin implements WildInput {

    public void tick(boolean slowDown, float f) {
    }
}
