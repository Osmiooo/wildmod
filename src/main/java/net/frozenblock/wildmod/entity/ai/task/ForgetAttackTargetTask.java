package net.frozenblock.wildmod.entity.ai.task;

import com.google.common.collect.ImmutableMap;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class ForgetAttackTargetTask<E extends MobEntity> extends Task<E> {
    private static final int REMEMBER_TIME = 200;
    private final Predicate<LivingEntity> alternativeCondition;
    private final BiConsumer<E, LivingEntity> forgetCallback;
    private final boolean shouldForgetIfTargetUnreachable;

    public ForgetAttackTargetTask(Predicate<LivingEntity> condition, BiConsumer<E, LivingEntity> biConsumer, boolean shouldForgetIfTargetUnreachable) {
        super(ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, MemoryModuleState.VALUE_PRESENT, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleState.REGISTERED));
        this.alternativeCondition = condition;
        this.forgetCallback = biConsumer;
        this.shouldForgetIfTargetUnreachable = shouldForgetIfTargetUnreachable;
    }

    public ForgetAttackTargetTask(Predicate<LivingEntity> predicate, BiConsumer<E, LivingEntity> biConsumer) {
        this(predicate, biConsumer, true);
    }

    public ForgetAttackTargetTask(Predicate<LivingEntity> alternativeCondition) {
        this(alternativeCondition, (mobEntity, livingEntity) -> {
        });
    }

    public ForgetAttackTargetTask(BiConsumer<E, LivingEntity> biConsumer) {
        this((target) -> {
            return false;
        }, biConsumer);
    }

    public ForgetAttackTargetTask() {
        this((target) -> {
            return false;
        }, (mobEntity, livingEntity) -> {
        });
    }

    protected void run(ServerWorld serverWorld, E mobEntity, long l) {
        LivingEntity livingEntity = this.getAttackTarget(mobEntity);
        if (!mobEntity.canTarget(livingEntity)) {
            this.forgetAttackTarget(mobEntity);
        } else if (this.shouldForgetIfTargetUnreachable && cannotReachTarget(mobEntity)) {
            this.forgetAttackTarget(mobEntity);
        } else if (this.isAttackTargetDead(mobEntity)) {
            this.forgetAttackTarget(mobEntity);
        } else if (this.isAttackTargetInAnotherWorld(mobEntity)) {
            this.forgetAttackTarget(mobEntity);
        } else if (this.alternativeCondition.test(this.getAttackTarget(mobEntity))) {
            this.forgetAttackTarget(mobEntity);
        }
    }

    private boolean isAttackTargetInAnotherWorld(E entity) {
        return this.getAttackTarget(entity).world != entity.world;
    }

    private LivingEntity getAttackTarget(E entity) {
        return entity.getBrain().getOptionalMemory(MemoryModuleType.ATTACK_TARGET).get();
    }

    private static <E extends LivingEntity> boolean cannotReachTarget(E entity) {
        Optional<Long> optional = entity.getBrain().getOptionalMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        return optional.isPresent() && entity.world.getTime() - optional.get() > 200L;
    }

    private boolean isAttackTargetDead(E entity) {
        Optional<LivingEntity> optional = entity.getBrain().getOptionalMemory(MemoryModuleType.ATTACK_TARGET);
        return optional.isPresent() && !optional.get().isAlive();
    }

    protected void forgetAttackTarget(E entity) {
        this.forgetCallback.accept(entity, this.getAttackTarget(entity));
        entity.getBrain().forget(MemoryModuleType.ATTACK_TARGET);
    }
}
