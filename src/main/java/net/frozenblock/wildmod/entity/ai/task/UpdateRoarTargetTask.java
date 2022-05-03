package net.frozenblock.wildmod.entity.ai.task;

import net.frozenblock.wildmod.entity.WardenEntity;
import net.frozenblock.wildmod.registry.RegisterMemoryModules;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.task.LookTargetUtil;
import net.minecraft.entity.ai.brain.task.UpdateAttackTargetTask;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class UpdateRoarTargetTask extends UpdateAttackTargetTask<WardenEntity> {
    public UpdateRoarTargetTask(Predicate<WardenEntity> predicate, Function<WardenEntity, Optional<? extends LivingEntity>> function, int roarDuration) {
        super(predicate, function);
    }

    protected void run(ServerWorld serverWorld, WardenEntity wardenEntity, long l) {
        LookTargetUtil.lookAt(wardenEntity, (LivingEntity)wardenEntity.getBrain().getOptionalMemory(RegisterMemoryModules.ROAR_TARGET).get());
    }

    protected void finishRunning(ServerWorld serverWorld, WardenEntity wardenEntity, long l) {
        this.runAndForget(serverWorld, wardenEntity, l);
    }

    private void runAndForget(ServerWorld world, WardenEntity warden, long time) {
        super.run(world, warden, time);
        warden.getBrain().forget(RegisterMemoryModules.ROAR_TARGET);
    }

    protected boolean shouldKeepRunning(ServerWorld serverWorld, WardenEntity wardenEntity, long l) {
        Optional<LivingEntity> optional = wardenEntity.getBrain().getOptionalMemory(RegisterMemoryModules.ROAR_TARGET);
        return optional.filter(EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR).isPresent();
    }
}