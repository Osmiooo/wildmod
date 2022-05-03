package net.frozenblock.wildmod.entity;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import net.frozenblock.wildmod.WildMod;
import net.frozenblock.wildmod.liukrastapi.AnimationState;
import net.frozenblock.wildmod.registry.*;
import net.frozenblock.wildmod.tags.BiomeTags;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.control.AquaticMoveControl;
import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.entity.ai.pathing.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.network.DebugInfoSender;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.stat.Stats;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.random.AbstractRandom;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.IntStream;


public class FrogEntity extends AnimalEntity {
    public static final Ingredient SLIME_BALL = Ingredient.ofItems(Items.SLIME_BALL);
    protected static final ImmutableList<SensorType<? extends Sensor<? super FrogEntity>>> SENSORS = ImmutableList.of(
            SensorType.NEAREST_LIVING_ENTITIES, SensorType.HURT_BY, WildMod.FROG_ATTACKABLES, WildMod.FROG_TEMPTATIONS, WildMod.IS_IN_WATER
    );
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_MODULES = ImmutableList.of(
            MemoryModuleType.LOOK_TARGET,
            MemoryModuleType.MOBS,
            MemoryModuleType.VISIBLE_MOBS,
            MemoryModuleType.WALK_TARGET,
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
            MemoryModuleType.PATH,
            MemoryModuleType.BREED_TARGET,
            MemoryModuleType.LONG_JUMP_COOLING_DOWN,
            MemoryModuleType.LONG_JUMP_MID_JUMP,
            MemoryModuleType.ATTACK_TARGET,
            MemoryModuleType.TEMPTING_PLAYER,
            MemoryModuleType.TEMPTATION_COOLDOWN_TICKS,
            new MemoryModuleType[]{
                    MemoryModuleType.IS_TEMPTED,
                    MemoryModuleType.HURT_BY,
                    MemoryModuleType.HURT_BY_ENTITY,
                    MemoryModuleType.NEAREST_ATTACKABLE,
                    RegisterMemoryModules.IS_IN_WATER,
                    RegisterMemoryModules.IS_PREGNANT
            }
    );
    private static final TrackedData<Integer> VARIANT = DataTracker.registerData(FrogEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<OptionalInt> TARGET = DataTracker.registerData(FrogEntity.class, WildMod.OPTIONAL_INT);
    private static final int field_37459 = 5;
    public static final String VARIANT_KEY = "variant";
    public final AnimationState longJumpingAnimationState = new AnimationState();
    public final AnimationState croakingAnimationState = new AnimationState();
    public final AnimationState usingTongueAnimationState = new AnimationState();
    public final AnimationState walkingAnimationState = new AnimationState();
    public final AnimationState swimmingAnimationState = new AnimationState();
    public final AnimationState idlingInWaterAnimationState = new AnimationState();

    public FrogEntity(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
        this.lookControl = new FrogEntity.FrogLookControl(this);
        this.setPathfindingPenalty(PathNodeType.WATER, 4.0F);
        this.moveControl = new AquaticMoveControl(this, 85, 10, 0.02F, 0.1F, true);
        this.stepHeight = 1.0F;
    }

    protected Brain.Profile<FrogEntity> createBrainProfile() {
        return Brain.createProfile(MEMORY_MODULES, SENSORS);
    }

    @Override
    protected Brain<?> deserializeBrain(Dynamic<?> dynamic) {
        return FrogBrain.create(this.createBrainProfile().deserialize(dynamic));
    }

    public Brain<FrogEntity> getBrain() {
        return (Brain<FrogEntity>) super.getBrain();
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(VARIANT, 0);
        this.dataTracker.startTracking(TARGET, OptionalInt.empty());
    }

    public void clearFrogTarget() {
        this.dataTracker.set(TARGET, OptionalInt.empty());
    }

    public Optional<Entity> getFrogTarget() {
        IntStream var10000 = ((OptionalInt)this.dataTracker.get(TARGET)).stream();
        World var10001 = this.world;
        Objects.requireNonNull(var10001);
        return var10000.mapToObj(var10001::getEntityById).filter(Objects::nonNull).findFirst();
    }

    public void setFrogTarget(Entity entity) {
        this.dataTracker.set(TARGET, OptionalInt.of(entity.getId()));
    }

    @Override
    public int getMaxLookYawChange() {
        return 35;
    }

    @Override
    public int getMaxHeadRotation() {
        return 5;
    }

    public Variant getVariant() {
        return Variant.fromId(this.dataTracker.get(VARIANT));
    }

    public void setVariant(Variant variant) {
        this.dataTracker.set(VARIANT, variant.getId());
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putString("variant", Registry.FROG_VARIANT.getId(this.getVariant()).toString());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        Variant frogVarient = Registry.FROG_VARIANT.get(Identifier.tryParse(nbt.getString("variant")));
        if (frogVarient != null) {
            this.setVariant(frogVarient);
        }
    }

    @Override
    public boolean canBreatheInWater() {
        return true;
    }

    private boolean shouldWalk() {
        return this.onGround && this.getVelocity().horizontalLengthSquared() > 1.0E-6 && !this.isInsideWaterOrBubbleColumn();
    }

    private boolean shouldSwim() {
        return this.getVelocity().horizontalLengthSquared() > 1.0E-6 && this.isInsideWaterOrBubbleColumn();
    }

    @Override
    protected void mobTick() {
        this.world.getProfiler().push("frogBrain");
        this.getBrain().tick((ServerWorld)this.world, this);
        this.world.getProfiler().pop();
        this.world.getProfiler().push("frogActivityUpdate");
        FrogBrain.updateActivities(this);
        this.world.getProfiler().pop();
        super.mobTick();
    }

    @Override
    public void tick() {
        if (this.world.isClient()) {
            if (this.shouldWalk()) {
                this.walkingAnimationState.startIfNotRunning();
            } else {
                this.walkingAnimationState.stop();
            }

            if (this.shouldSwim()) {
                this.idlingInWaterAnimationState.stop();
                this.swimmingAnimationState.startIfNotRunning();
            } else if (this.isInsideWaterOrBubbleColumn()) {
                this.swimmingAnimationState.stop();
                this.idlingInWaterAnimationState.startIfNotRunning();
            } else {
                this.swimmingAnimationState.stop();
                this.idlingInWaterAnimationState.stop();
            }
        }

        super.tick();
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        if (POSE.equals(data)) {
            EntityPose entityPose = this.getPose();
            if (entityPose == EntityPose.LONG_JUMPING) {
                this.longJumpingAnimationState.start();
            } else {
                this.longJumpingAnimationState.stop();
            }
            if (entityPose == WildMod.CROAKING) {
                this.croakingAnimationState.start();
            } else {
                this.croakingAnimationState.stop();
            }
            if (entityPose == WildMod.USING_TONGUE) {
                this.usingTongueAnimationState.start();
            } else {
                this.usingTongueAnimationState.stop();
            }
        }
        super.onTrackedDataSet(data);
    }

    @Override
    @Nullable
    public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        FrogEntity frogEntity = RegisterEntities.FROG.create(world);
        if (frogEntity != null) {
            FrogBrain.coolDownLongJump(frogEntity, (AbstractRandom)world.getRandom());
        }
        return frogEntity;
    }

    @Override
    public boolean isBaby() {
        return false;
    }

    @Override
    public void setBaby(boolean baby) {
    }

    public void breed(ServerWorld world, AnimalEntity other) {
        ServerPlayerEntity serverPlayerEntity = this.getLovingPlayer();
        if (serverPlayerEntity == null) {
            serverPlayerEntity = other.getLovingPlayer();
        }

        if (serverPlayerEntity != null) {
            serverPlayerEntity.incrementStat(Stats.ANIMALS_BRED);
            Criteria.BRED_ANIMALS.trigger(serverPlayerEntity, this, other, null);
        }

        this.setBreedingAge(6000);
        other.setBreedingAge(6000);
        this.resetLoveTicks();
        other.resetLoveTicks();
        this.getBrain().remember(RegisterMemoryModules.IS_PREGNANT, Unit.INSTANCE);
        world.sendEntityStatus(this, (byte)18);
        if (world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT)) {
            world.spawnEntity(new ExperienceOrbEntity(world, this.getX(), this.getY(), this.getZ(), this.getRandom().nextInt(7) + 1));
        }

    }

    public EntityData initialize(
            ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt
    ) {
        RegistryEntry<Biome> registryEntry = world.getBiome(this.getBlockPos());
        if (registryEntry.isIn(net.frozenblock.wildmod.tags.BiomeTags.SPAWNS_COLD_VARIANT_FROGS)) {
            this.setVariant(Variant.COLD);
        } else if (registryEntry.isIn(BiomeTags.SPAWNS_WARM_VARIANT_FROGS)) {
            this.setVariant(Variant.WARM);
        } else {
            this.setVariant(Variant.TEMPERATE);
        }

        FrogBrain.coolDownLongJump(this, (AbstractRandom)world.getRandom());
        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
    }

    public static DefaultAttributeContainer.Builder createFrogAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 1.0)
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 10.0);
    }

    @Override
    @Nullable
    protected SoundEvent getAmbientSound() {
        return RegisterSounds.ENTITY_FROG_AMBIENT;
    }

    @Override
    @Nullable
    protected SoundEvent getHurtSound(DamageSource source) {
        return RegisterSounds.ENTITY_FROG_HURT;
    }

    @Override
    @Nullable
    protected SoundEvent getDeathSound() {
        return RegisterSounds.ENTITY_FROG_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(RegisterSounds.ENTITY_FROG_STEP, 0.15F, 1.0F);
    }

    @Override
    public boolean isPushedByFluids() {
        return false;
    }

    @Override
    protected void sendAiDebugData() {
        super.sendAiDebugData();
        DebugInfoSender.sendBrainDebugData(this);
    }

    @Override
    protected int computeFallDamage(float fallDistance, float damageMultiplier) {
        return super.computeFallDamage(fallDistance, damageMultiplier) - 5;
    }

    @Override
    public void travel(Vec3d movementInput) {
        if (this.canMoveVoluntarily() && this.isTouchingWater()) {
            this.updateVelocity(this.getMovementSpeed(), movementInput);
            this.move(MovementType.SELF, this.getVelocity());
            this.setVelocity(this.getVelocity().multiply(0.9));
        } else {
            super.travel(movementInput);
        }

    }

    public static boolean isValidFrogFood(@NotNull LivingEntity entity) {
        if (entity instanceof SlimeEntity slimeEntity) {
            if (slimeEntity.getSize() != 1) {
                return false;
            }
        }

        return entity.getType().isIn(RegisterTags.FROG_FOOD);
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        return new FrogSwimNavigation(this, world);
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return SLIME_BALL.test(stack);
    }

    class FrogLookControl extends LookControl {
        FrogLookControl(MobEntity entity) {
            super(entity);
        }

        @Override
        protected boolean shouldStayHorizontal() {
            return FrogEntity.this.getFrogTarget().isEmpty();
        }
    }

    public enum Variant {
        TEMPERATE(0, "temperate"),
        WARM(1, "warm"),
        COLD(2, "cold");

        private static final Variant[] VALUES;
        private final int id;
        private final String name;

        Variant(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return this.id;
        }

        public String getName() {
            return this.name;
        }

        public static Variant fromId(int id) {
            if (id < 0 || id >= VALUES.length) {
                id = 0;
            }
            return VALUES[id];
        }

        static {
            VALUES = Arrays.stream(Variant.values()).sorted(Comparator.comparingInt(Variant::getId)).toArray(Variant[]::new);
        }
    }

    static class FrogSwimNavigation extends SwimNavigation {
        FrogSwimNavigation(FrogEntity frog, World world) {
            super(frog, world);
        }

        @Override
        protected PathNodeNavigator createPathNodeNavigator(int range) {
            this.nodeMaker = new FrogSwimPathNodeMaker(true);
            return new PathNodeNavigator(this.nodeMaker, range);
        }

        @Override
        protected boolean isAtValidPosition() {
            return true;
        }

        @Override
        public boolean isValidPosition(BlockPos pos) {
            return !this.world.getBlockState(pos.down()).isAir();
        }
    }

    static class FrogSwimPathNodeMaker extends AmphibiousPathNodeMaker {
        private final BlockPos.Mutable pos = new BlockPos.Mutable();

        public FrogSwimPathNodeMaker(boolean bl) {
            super(bl);
        }

        @Override
        public PathNodeType getDefaultNodeType(BlockView world, int x, int y, int z) {
            this.pos.set(x, y - 1, z);
            BlockState blockState = world.getBlockState(this.pos);
            return blockState.isIn(RegisterTags.FROG_PREFER_JUMP_TO) ? PathNodeType.OPEN : getLandNodeType(world, this.pos.move(Direction.UP));
        }
    }
}