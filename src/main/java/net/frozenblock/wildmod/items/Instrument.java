package net.frozenblock.wildmod.items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.dynamic.Codecs;

public record Instrument(SoundEvent soundEvent, int useDuration, float range) {
    public static final Codec<Instrument> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            SoundEvent.CODEC.fieldOf("sound_event").forGetter(Instrument::soundEvent),
                            Codecs.POSITIVE_INT.fieldOf("use_duration").forGetter(Instrument::useDuration),
                            Codecs.POSITIVE_FLOAT.fieldOf("range").forGetter(Instrument::range)
                    )
                    .apply(instance, Instrument::new)
    );
}
