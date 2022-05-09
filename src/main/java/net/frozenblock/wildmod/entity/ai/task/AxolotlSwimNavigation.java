package net.frozenblock.wildmod.entity.ai.task;

import net.minecraft.entity.ai.pathing.AmphibiousPathNodeMaker;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.PathNodeNavigator;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class AxolotlSwimNavigation extends EntityNavigation {
    public AxolotlSwimNavigation(MobEntity mobEntity, World world) {
        super(mobEntity, world);
    }

    protected PathNodeNavigator createPathNodeNavigator(int range) {
        this.nodeMaker = new AmphibiousPathNodeMaker(false);
        this.nodeMaker.setCanEnterOpenDoors(true);
        return new PathNodeNavigator(this.nodeMaker, range);
    }

    protected boolean isAtValidPosition() {
        return true;
    }

    protected Vec3d getPos() {
        return new Vec3d(this.entity.getX(), this.entity.getBodyY(0.5), this.entity.getZ());
    }

    protected double adjustTargetY(Vec3d pos) {
        return pos.y;
    }

    protected boolean canPathDirectlyThrough(Vec3d origin, Vec3d target) {
        return this.isInLiquid() ? doesNotCollide(this.entity, origin, target) : false;
    }

    protected static boolean doesNotCollide(MobEntity entity, Vec3d startPos, Vec3d entityPos) {
        Vec3d vec3d = new Vec3d(entityPos.x, entityPos.y + (double)entity.getHeight() * 0.5, entityPos.z);
        return entity.world.raycast(new RaycastContext(startPos, vec3d, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, entity)).getType() == HitResult.Type.MISS;
    }

    public boolean isValidPosition(BlockPos pos) {
        return !this.world.getBlockState(pos.down()).isAir();
    }

    public void setCanSwim(boolean canSwim) {
    }
}