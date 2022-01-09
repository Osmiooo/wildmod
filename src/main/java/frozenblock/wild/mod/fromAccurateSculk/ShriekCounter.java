package frozenblock.wild.mod.fromAccurateSculk;

import frozenblock.wild.mod.WildMod;
import frozenblock.wild.mod.entity.WardenEntity;
import frozenblock.wild.mod.registry.RegisterEntities;
import frozenblock.wild.mod.registry.RegisterSounds;
import net.minecraft.block.Blocks;
import net.minecraft.sound.SoundCategory;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.lang.Math.*;

public class ShriekCounter {
    public static int shrieks = 0;
    private static long timer;
    private static boolean running;

    public static void addShriek(BlockPos pos, World world) {
        if (!world.isClient() && world.getTime() > timer && !running) {
            timer=world.getTime()+30;
            if (!findWarden(world, pos) || world.getGameRules().getBoolean(WildMod.NO_WARDEN_COOLDOWN)) {
                //To hopefully only allow one Shrieker to use the method, and prevent sound spamming
                shrieks = shrieks + 1;
                for (int t = 8; t > 0; t--) {
                    ArrayList<BlockPos> candidates = findBlock(pos.add(-1, 0, -1), t, true, world);
                    if (!candidates.isEmpty()) {
                        for (int o = 0; o < 16; o++) {
                            int ran = UniformIntProvider.create(0, candidates.size() - 1).get(world.getRandom());
                            BlockPos currentCheck = candidates.get(ran);
                            warn(world, pos);
                            timer=world.getTime()+30;
                            if (shrieks >= 4) {
                                shrieks = 0;
                                WardenEntity warden = (WardenEntity) RegisterEntities.WARDEN.create(world);
                                warden.refreshPositionAndAngles((double) currentCheck.getX() + 1D, (double) currentCheck.up(1).getY(), (double) currentCheck.getZ() + 1D, 0.0F, 0.0F);
                                world.spawnEntity(warden);
                                warden.handleStatus((byte) 5);
                                warden.leaveTime=world.getTime()+1200;
                                world.playSound(null, currentCheck, RegisterSounds.ENTITY_WARDEN_EMERGE, SoundCategory.HOSTILE, 1F, 1F);
                                break;
                            }
                            break;
                        }
                        break;
                    } else
                    break;
                }
            } else if (findWarden(world, pos)) {
                shrieks = 0;
            }
        }
    }
    public static boolean findWarden(World world, BlockPos pos) {
        double x1 = pos.getX();
        double y1 = pos.getY();
        double z1 = pos.getZ();
        BlockPos side1 = new BlockPos(x1-50, y1-50, z1-50);
        BlockPos side2 = new BlockPos(x1+50, y1+50, z1+50);
        Box box = (new Box(side1, side2));
        List<WardenEntity> list = world.getNonSpectatingEntities(WardenEntity.class, box);
        if (!list.isEmpty()) {
            Iterator<WardenEntity> var11 = list.iterator();
            WardenEntity warden;
            while (var11.hasNext()) {
                warden = var11.next();
                if (warden.getBlockPos().isWithinDistance(pos, (49))) {
                    return true;
                }
            }
        }
        return false;
    }

    public static ArrayList<BlockPos> findBlock(BlockPos centerBlock, int radius, boolean hollow, World world) {
        int bx = centerBlock.getX();
        int by = centerBlock.getY();
        int bz = centerBlock.getZ();
        ArrayList<BlockPos> candidates = new ArrayList<>();
        for (int x = bx - radius; x <= bx + radius; x++) {
            for (int y = by - radius; y <= by + radius; y++) {
                for (int z = bz - radius; z <= bz + radius; z++) {
                    double distance = ((bx - x) * (bx - x) + ((bz - z) * (bz - z)) + ((by - y) * (by - y)));
                    if (distance < radius * radius && !(hollow && distance < ((radius - 1) * (radius - 1)))) {
                        BlockPos l = new BlockPos(x, y, z);
                            if (verifyWardenSpawn(l, world)) {
                                candidates.add(l);
                        }
                    }

                }
            }
        }
        return candidates;
    }
    public static boolean verifyWardenSpawn(BlockPos p, World world) {
        if (canSpawn(world, p) && canSpawn(world, p.add(1,0,0)) && canSpawn(world, p.add(1,0,1)) && canSpawn(world, p.add(0,0,1))) {
            if (wardenNonCollide(p, world) && wardenNonCollide(p.add(1,0,0), world) && wardenNonCollide(p.add(1,0,1), world) && wardenNonCollide(p.add(0,0,1), world)) {
                return true;
            }
        }
        return false;
    }
    public static boolean canSpawn(World world, BlockPos p) {
        return !SculkTags.WARDEN_SPAWNABLE.contains(world.getBlockState(p).getBlock()) && !world.getBlockState(p).isAir() && world.getBlockState(p).getBlock()!=Blocks.WATER &&  world.getBlockState(p).getBlock()!=Blocks.LAVA;
    }
    public static boolean wardenNonCollide(BlockPos p, World world) {
        if (SculkTags.WARDEN_NON_COLLIDE.contains(world.getBlockState(p.up()).getBlock()) && SculkTags.WARDEN_NON_COLLIDE.contains(world.getBlockState(p.up(2)).getBlock()) && SculkTags.WARDEN_NON_COLLIDE.contains(world.getBlockState(p.up(3)).getBlock())) {
            return true;
        }
        return false;
    }
    public static void warn(World world, BlockPos blockPos) {
        if (shrieks==1) {
            double a = random() * 2 * PI;
            double r = sqrt(16) * sqrt(random());
            int x = (int) (r * cos(a));
            int y = (int) (r * sin(a));
            BlockPos play = blockPos.add(x,0,y);
            world.playSound(null, play, RegisterSounds.ENTITY_WARDEN_CLOSE, SoundCategory.NEUTRAL, 0.2F, 1F);
        } else
        if (shrieks==2) {
            double a = random() * 2 * PI;
            double r = sqrt(12) * sqrt(random());
            int x = (int) (r * cos(a));
            int y = (int) (r * sin(a));
            BlockPos play = blockPos.add(x,0,y);
            world.playSound(null, play, RegisterSounds.ENTITY_WARDEN_CLOSER, SoundCategory.NEUTRAL, 0.3F, 1F);
        } else
        if (shrieks==3) {
            double a = random() * 2 * PI;
            double r = sqrt(10) * sqrt(random());
            int x = (int) (r * cos(a));
            int y = (int) (r * sin(a));
            BlockPos play = blockPos.add(x,0,y);
            world.playSound(null, play, RegisterSounds.ENTITY_WARDEN_CLOSEST, SoundCategory.NEUTRAL, 0.4F, 1F);
        }
    }

    public static BlockPos wardenOutLocation(BlockPos pos, World world) {
                for (int t = 1; t < 8; t++) {
                    ArrayList<BlockPos> candidates = findBlock(pos.add(-1, 0, -1), t, true, world);
                    if (!candidates.isEmpty()) {
                            int ran = UniformIntProvider.create(0, candidates.size() - 1).get(world.getRandom());
                            return candidates.get(ran);
                    }
                }
        return null;
        }

}
