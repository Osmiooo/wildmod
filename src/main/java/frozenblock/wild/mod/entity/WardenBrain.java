package frozenblock.wild.mod.entity;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import frozenblock.wild.mod.WildMod;
import frozenblock.wild.mod.liukrastapi.Angriness;
import frozenblock.wild.mod.registry.RegisterEntities;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.*;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.brain.task.*;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.List;

public class WardenBrain {
    private static final float field_38175 = 0.5F;
    private static final float field_38176 = 0.7F;
    private static final float field_38177 = 1.2F;
    private static final int field_38178 = 18;
    private static final int DIG_DURATION = MathHelper.ceil(100.0F);
    public static int EMERGE_DURATION = 150;//MathHelper.ceil(133.59999F);
    public static int ROAR_DURATION = MathHelper.ceil(84.0F);
    public static int SNIFF_DURATION = MathHelper.ceil(83.2F);;
    public static int DIG_COOLDOWN = 1200;
    private static final int field_38181 = 100;
    private static final List<SensorType<? extends Sensor<? super WardenEntity>>> SENSORS;
    private static final List<MemoryModuleType<?>> MEMORY_MODULES;
    private static final Task<WardenEntity> RESET_DIG_COOLDOWN_TASK;

    public WardenBrain() {
    }

    public static void tick(WardenEntity warden) {
        warden.getBrain().resetPossibleActivities(ImmutableList.of(WildMod.EMERGE, WildMod.DIG, WildMod.ROAR, Activity.FIGHT, WildMod.INVESTIGATE, WildMod.SNIFF, Activity.IDLE));
    }

    protected static Brain<?> create(WardenEntity warden, Dynamic<?> dynamic) {
        Brain.Profile<WardenEntity> profile = Brain.createProfile(MEMORY_MODULES, SENSORS);
        Brain<WardenEntity> brain = profile.deserialize(dynamic);
        addCoreActivities(brain);
        addEmergeActivities(brain);
        addDigActivities(brain);
        addIdleActivities(brain);
        addRoarActivities(brain);
        addFightActivities(warden, brain);
        addInvestigateActivities(brain);
        addSniffActivities(brain);
        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.resetPossibleActivities();
        return brain;
    }

    private static void addCoreActivities(Brain<WardenEntity> brain) {
        brain.setTaskList(Activity.CORE, 0, ImmutableList.of(new StayAboveWaterTask(0.8F), new LookAtDisturbanceTask(), new LookAroundTask(45, 90), new WanderAroundTask()));
    }

    private static void addEmergeActivities(Brain<WardenEntity> brain) {
        brain.setTaskList(WildMod.EMERGE, 5, ImmutableList.of(new EmergeTask<>(EMERGE_DURATION)), RegisterEntities.IS_EMERGING);
    }

    private static void addDigActivities(Brain<WardenEntity> brain) {
        brain.setTaskList(WildMod.DIG, ImmutableList.of(Pair.of(0, new DigTask<>(DIG_DURATION))), ImmutableSet.of(Pair.of(RegisterEntities.ROAR_TARGET, MemoryModuleState.VALUE_ABSENT), Pair.of(RegisterEntities.DIG_COOLDOWN, MemoryModuleState.VALUE_ABSENT)));
    }

    private static void addIdleActivities(Brain<WardenEntity> brain) {
        brain.setTaskList(Activity.IDLE, 10, ImmutableList.of(new FindRoarTargetTask<>(WardenEntity::getPrimeSuspect), new StartSniffingTask(), new RandomTask<>(ImmutableMap.of(RegisterEntities.IS_SNIFFING, MemoryModuleState.VALUE_ABSENT), ImmutableList.of(Pair.of(new StrollTask(0.5F), 2), Pair.of(new WaitTask(30, 60), 1)))));
    }

    private static void addInvestigateActivities(Brain<WardenEntity> brain) {
        brain.setTaskList(WildMod.INVESTIGATE, 5, ImmutableList.of(new FindRoarTargetTask<>(WardenEntity::getPrimeSuspect), new GoToCelebrateTask<>(RegisterEntities.DISTURBANCE_LOCATION, 2, 0.7F), new WaitTask(10, 20)), RegisterEntities.DISTURBANCE_LOCATION);
    }

    private static void addSniffActivities(Brain<WardenEntity> brain) {
        brain.setTaskList(WildMod.SNIFF, 5, ImmutableList.of(new FindRoarTargetTask<>(WardenEntity::getPrimeSuspect), new SniffTask<>(SNIFF_DURATION)), RegisterEntities.IS_SNIFFING);
    }

    private static void addRoarActivities(Brain<WardenEntity> brain) {
        brain.setTaskList(WildMod.ROAR, 10, ImmutableList.of(new RoarTask()), RegisterEntities.ROAR_TARGET);
    }

    private static void addFightActivities(WardenEntity warden, Brain<WardenEntity> brain) {
        brain.setTaskList(Activity.FIGHT, 10, ImmutableList.of(RESET_DIG_COOLDOWN_TASK, new ForgetAttackTargetTask<>((entity) -> {
            return warden.getAngriness() != Angriness.ANGRY || !warden.isValidTarget(entity);
        }, WardenBrain::removeDeadSuspect, false), new FollowMobTask((entity) -> {
            return isTargeting(warden, entity);
        }, (float)warden.getAttributeValue(EntityAttributes.GENERIC_FOLLOW_RANGE)), new RangedApproachTask(1.2F), new SonicBoomTask(), new MeleeAttackTask(18)), MemoryModuleType.ATTACK_TARGET);
    }

    private static RandomTask<WardenEntity> getStrollOrWaitTask() {
        return new RandomTask<>(ImmutableList.of(Pair.of(new StrollTask(0.5F), 2), Pair.of(new WaitTask(30, 60), 1)));
    }

    private static boolean isTargeting(WardenEntity warden, LivingEntity entity) {
        return warden.getBrain().getOptionalMemory(MemoryModuleType.ATTACK_TARGET).filter((entityx) -> {
            return entityx == entity;
        }).isPresent();
    }

    private static void removeDeadSuspect(WardenEntity warden, LivingEntity suspect) {
        if (!warden.isValidTarget(suspect)) {
            warden.removeSuspect(suspect);
        }

        resetDigCooldown(warden);
    }

    public static void resetDigCooldown(LivingEntity warden) {
        if (warden.getBrain().hasMemoryModule(RegisterEntities.DIG_COOLDOWN)) {
            warden.getBrain().remember(RegisterEntities.DIG_COOLDOWN, Unit.INSTANCE, 1200L);
        }

    }

    public static void lookAtDisturbance(WardenEntity warden, BlockPos pos) {
        if (warden.world.getWorldBorder().contains(pos) && !warden.getPrimeSuspect().isPresent() && !warden.getBrain().getOptionalMemory(MemoryModuleType.ATTACK_TARGET).isPresent()) {
            resetDigCooldown(warden);
            warden.getBrain().remember(RegisterEntities.SNIFF_COOLDOWN, Unit.INSTANCE, 100L);
            warden.getBrain().remember(MemoryModuleType.LOOK_TARGET, new BlockPosLookTarget(pos), 100L);
            warden.getBrain().remember(RegisterEntities.DISTURBANCE_LOCATION, pos, 100L);
            warden.getBrain().forget(MemoryModuleType.WALK_TARGET);
        }
    }



    static {
        SENSORS = List.of(SensorType.NEAREST_PLAYERS, WildMod.WARDEN_ENTITY_SENSOR);
        MEMORY_MODULES = List.of(MemoryModuleType.MOBS, MemoryModuleType.VISIBLE_MOBS, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_NEMESIS, MemoryModuleType.LOOK_TARGET, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.ATTACK_TARGET, MemoryModuleType.ATTACK_COOLING_DOWN, MemoryModuleType.NEAREST_ATTACKABLE, RegisterEntities.ROAR_TARGET, RegisterEntities.DISTURBANCE_LOCATION, RegisterEntities.RECENT_PROJECTILE, RegisterEntities.IS_SNIFFING, RegisterEntities.IS_EMERGING, RegisterEntities.ROAR_SOUND_DELAY, RegisterEntities.DIG_COOLDOWN, RegisterEntities.ROAR_SOUND_COOLDOWN, RegisterEntities.SNIFF_COOLDOWN, RegisterEntities.TOUCH_COOLDOWN, RegisterEntities.VIBRATION_COOLDOWN, RegisterEntities.SONIC_BOOM_COOLDOWN, RegisterEntities.SONIC_BOOM_SOUND_COOLDOWN, RegisterEntities.SONIC_BOOM_SOUND_DELAY);
        RESET_DIG_COOLDOWN_TASK = new Task<>(ImmutableMap.of(RegisterEntities.DIG_COOLDOWN, MemoryModuleState.REGISTERED)) {
            protected void run(ServerWorld serverWorld, WardenEntity wardenEntity, long l) {
                WardenBrain.resetDigCooldown(wardenEntity);
            }
        };
    }
}