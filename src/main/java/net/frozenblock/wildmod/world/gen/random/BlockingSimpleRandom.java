package net.frozenblock.wildmod.world.gen.random;

import net.minecraft.world.gen.random.AbstractRandom;
import net.minecraft.world.gen.random.BaseSimpleRandom;
import net.minecraft.world.gen.random.GaussianGenerator;
import net.minecraft.world.gen.random.RandomDeriver;

import java.util.concurrent.atomic.AtomicLong;

@Deprecated
public class BlockingSimpleRandom implements BaseSimpleRandom {
    private static final int INT_BITS = 48;
    private static final long SEED_MASK = 281474976710655L;
    private static final long MULTIPLIER = 25214903917L;
    private static final long INCREMENT = 11L;
    private final AtomicLong seed = new AtomicLong();
    private final GaussianGenerator gaussianGenerator = new GaussianGenerator(this);

    public BlockingSimpleRandom(long seed) {
        this.setSeed(seed);
    }

    public AbstractRandom derive() {
        return new BlockingSimpleRandom(this.nextLong());
    }

    public RandomDeriver createRandomDeriver() {
        return new AtomicSimpleRandom.RandomDeriver(this.nextLong());
    }

    public void setSeed(long seed) {
        this.seed.set((seed ^ 25214903917L) & 281474976710655L);
    }

    public int next(int bits) {
        long l;
        long m;
        do {
            l = this.seed.get();
            m = l * 25214903917L + 11L & 281474976710655L;
        } while (!this.seed.compareAndSet(l, m));

        return (int) (m >>> 48 - bits);
    }

    public double nextGaussian() {
        return this.gaussianGenerator.next();
    }
}
