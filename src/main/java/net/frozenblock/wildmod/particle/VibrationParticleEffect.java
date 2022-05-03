//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.frozenblock.wildmod.particle;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.event.BlockPositionSource;
import net.minecraft.world.event.PositionSource;
import net.minecraft.world.event.PositionSourceType;

import java.util.Locale;

public class VibrationParticleEffect implements ParticleEffect {
    public static final Codec<VibrationParticleEffect> CODEC = RecordCodecBuilder.create((instance) -> {
        return instance.group(PositionSource.CODEC.fieldOf("destination").forGetter((effect) -> {
            return effect.destination;
        }), Codec.INT.fieldOf("arrival_in_ticks").forGetter((vibrationParticleEffect) -> {
            return vibrationParticleEffect.arrivalInTicks;
        })).apply(instance, VibrationParticleEffect::new);
    });
    public static final ParticleEffect.Factory<VibrationParticleEffect> PARAMETERS_FACTORY = new ParticleEffect.Factory<VibrationParticleEffect>() {
        public VibrationParticleEffect read(ParticleType<VibrationParticleEffect> particleType, StringReader stringReader) throws CommandSyntaxException {
            stringReader.expect(' ');
            float f = (float)stringReader.readDouble();
            stringReader.expect(' ');
            float g = (float)stringReader.readDouble();
            stringReader.expect(' ');
            float h = (float)stringReader.readDouble();
            stringReader.expect(' ');
            int i = stringReader.readInt();
            BlockPos blockPos = new BlockPos((double)f, (double)g, (double)h);
            return new VibrationParticleEffect(new BlockPositionSource(blockPos), i);
        }

        public VibrationParticleEffect read(ParticleType<VibrationParticleEffect> particleType, PacketByteBuf packetByteBuf) {
            PositionSource positionSource = PositionSourceType.read(packetByteBuf);
            int i = packetByteBuf.readVarInt();
            return new VibrationParticleEffect(positionSource, i);
        }
    };
    private final PositionSource destination;
    private final int arrivalInTicks;

    public VibrationParticleEffect(PositionSource positionSource, int i) {
        this.destination = positionSource;
        this.arrivalInTicks = i;
    }

    public void write(PacketByteBuf buf) {
        PositionSourceType.write(this.destination, buf);
        buf.writeVarInt(this.arrivalInTicks);
    }

    public String asString() {
        Vec3d vec3d = Vec3d.ofCenter(this.destination.getPos(null).get());
        double d = vec3d.getX();
        double e = vec3d.getY();
        double f = vec3d.getZ();
        return String.format(Locale.ROOT, "%s %.2f %.2f %.2f %d", Registry.PARTICLE_TYPE.getId(this.getType()), d, e, f, this.arrivalInTicks);
    }

    public ParticleType<net.minecraft.particle.VibrationParticleEffect> getType() {
        return ParticleTypes.VIBRATION;
    }

    public PositionSource getVibration() {
        return this.destination;
    }

    public int getArrivalInTicks() {
        return this.arrivalInTicks;
    }
}