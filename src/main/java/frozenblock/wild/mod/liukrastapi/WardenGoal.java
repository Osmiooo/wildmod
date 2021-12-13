package frozenblock.wild.mod.liukrastapi;

import frozenblock.wild.mod.entity.WardenEntity;
import frozenblock.wild.mod.registry.RegisterSounds;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.VibrationParticleEffect;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Vibration;
import net.minecraft.world.World;
import net.minecraft.world.event.BlockPositionSource;
import net.minecraft.world.event.PositionSource;

public class WardenGoal extends Goal {
    private int cooldown;

    private double VX;
    private double VY;
    private double VZ;

    private boolean ROAR;

    private final WardenEntity mob;
    private final double speed;

    public WardenGoal(WardenEntity mob, double speed) {
        this.mob = mob;
        this.speed = speed;
    }

    public boolean canStart() {
        boolean exit = false;
        BlockPos lasteventpos = this.mob.lasteventpos;
        World lasteventWorld = this.mob.lasteventworld;

        if(this.mob.getAttacker() == null) {
            if(lasteventWorld != null && lasteventpos != null) {
                if(lasteventWorld == this.mob.getEntityWorld()) {
                    double distancex = Math.pow(this.mob.getBlockX() - lasteventpos.getX(), 2);
                    double distancey = Math.pow(this.mob.getBlockY() - lasteventpos.getY(), 2);
                    double distancez = Math.pow(this.mob.getBlockZ() - lasteventpos.getZ(), 2);
                    if(Math.sqrt(distancex + distancey + distancez) < 15) {

                        this.mob.playSound(SoundEvents.BLOCK_SCULK_SENSOR_CLICKING, 1.0F, 1.0F);


                        exit = true;
                    }
                }
            }
        } else {
            BlockPos blockPos = this.mob.getAttacker().getBlockPos();
            if(blockPos != null) {
                this.VX = this.mob.getAttacker().getX();
                this.VY = this.mob.getAttacker().getY();
                this.VZ = this.mob.getAttacker().getZ();
            }
            exit = true;
        }

        if(this.mob.getNavigation().isIdle()) {
            if(Math.random() > 0.5) {
                this.mob.roar();
                exit = false;
            }
        }

        int r = this.mob.getRoarTicksLeft1();
        if(r > 0) {
            exit = false;
        }

        return exit;
    }

    public boolean shouldContinue() {
        boolean exit;
        exit = !this.mob.getNavigation().isIdle();

        return exit;
    }

    public void start() {
        BlockPos lasteventpos = this.mob.lasteventpos;
        LivingEntity lastevententity = this.mob.lastevententity;
        if(this.mob.getAttacker() != null) {
            double distance = MathAddon.distance(this.VX, this.VY, this.VZ, this.mob.getX(), this.mob.getY(), this.mob.getZ()) / 2;
            if(distance < 2) {distance = 2;}
            double finalspeed = (speed * 2) / (distance -1);
            if(finalspeed < speed) {finalspeed = speed;}

            LivingEntity target = this.mob.getAttacker();
            this.mob.getNavigation().startMovingTo(this.VX, this.VY, this.VZ, finalspeed);
            double d = (this.mob.getWidth() * 2.0F * this.mob.getWidth() * 2.0F);
            double e = this.mob.squaredDistanceTo(target.getX(), target.getY(), target.getZ());
            this.cooldown = Math.max(this.cooldown - 1, 0);
            if (!(e > d)) {
                if (this.cooldown <= 0) {
                    this.cooldown = 20;
                    this.mob.tryAttack(target);
                }
            }

            this.mob.lastevententity = null;
            this.mob.lasteventpos = null;
            this.mob.lasteventworld = null;
        } else {

            //Calculating a good speed for the warden depending on his distance from the event.
            double distance = MathAddon.distance(lasteventpos.getX(), lasteventpos.getY(), lasteventpos.getZ(), this.mob.getX(), this.mob.getY(), this.mob.getZ()) / 2;
            if(distance < 2) {distance = 2;}
            double finalspeed = (speed * 2) / (distance - 1);
            if(finalspeed < speed) {finalspeed = speed;}

            //start moving the warden to the location

            this.mob.getNavigation().startMovingTo(lasteventpos.getX(), lasteventpos.getY(), lasteventpos.getZ(), finalspeed);

            if(lastevententity != null) {
                double d = (this.mob.getWidth() * 2.0F * this.mob.getWidth() * 2.0F);
                double e = this.mob.squaredDistanceTo(lastevententity.getX(), lastevententity.getY(), lastevententity.getZ());
                this.cooldown = Math.max(this.cooldown - 1, 0);
                if (!(e > d)) {
                    if (this.cooldown <= 0) {
                        this.cooldown = 20;
                        this.mob.tryAttack(lastevententity);
                    }
                }

                this.mob.lastevententity = null;
                this.mob.lasteventpos = null;
                this.mob.lasteventworld = null;
            }

        }
    }

    public void stop() {
    }
}