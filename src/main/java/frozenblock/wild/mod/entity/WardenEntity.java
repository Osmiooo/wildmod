package frozenblock.wild.mod.entity;


import frozenblock.wild.mod.WildMod;
import frozenblock.wild.mod.liukrastapi.*;
import frozenblock.wild.mod.registry.RegisterAccurateSculk;
import frozenblock.wild.mod.registry.RegisterSounds;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.Vibration;
import net.minecraft.world.World;
import net.minecraft.world.event.*;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class WardenEntity extends HostileEntity implements IAnimatable {
    private AnimationFactory factor = new AnimationFactor(this);
    public WardenEntity(EntityType<? extends HostileEntity> type, World worldIn)
    {
        super(type, worldIn);
        this.ignoreCameraFrustum = true;
    }

    private <E extends IAnimatable> PlayState idle(AnimationEvent<E> event)
    {
        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.warden.idle",true));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data)
    {
        data.addAnimationController(new AnimationController(this, "idle", 0, this::idle));
    }
    @Override
    public void AnimationFactory getFactory()
    {
        return this.factory
    }
    private int attackTicksLeft1;

    private int roarTicksLeft1;

    private double roarAnimationProgress;

    private static final double speed = 0.4D;

    public BlockPos lasteventpos;
    public World lasteventworld;
    public LivingEntity lastevententity;
    public LivingEntity navigationEntity;
    public IntArrayList entityList = new IntArrayList();
    public IntArrayList susList = new IntArrayList();
    public String trackingEntity = "null";
    public String sniffEntity = "null";
    public int nonEntityAnger;
    public long timeSinceNonEntity;

    public boolean hasDetected=false;
    public int emergeTicksLeft;
    public boolean hasEmerged;
    public long vibrationTimer = 0;
    public long timeSinceLastTracking = 0;
    public long leaveTime;
    protected int delay = 0;
    protected int distance;

    public int followingEntity;
    public int followTicksLeft;
    public int attackCooldown;

    public int hearbeatTime = 60;
    public long nextHeartBeat;
    public int timeStuck=0;
    public BlockPos stuckPos;
    public long timeSinceLastRecalculation;
    public long reactionSoundTimer;
    public int sniffTicksLeft;
    public int sniffCooldown;
    public int sniffX;
    public int sniffY;
    public int sniffZ;

    public WardenEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
        this.stepHeight = 1.0F;
    }

    public static DefaultAttributeContainer.Builder createWardenAttributes() {

        return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 500.0D).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, speed).add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0D).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 31.0D);
    }

    protected boolean burnsInDaylight() {
        return true;
    }

    @Override
    @Nullable
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityNbt) {
        this.handleStatus((byte) 5);
        this.leaveTime=this.world.getTime()+1200;
        return super.initialize(world, difficulty, spawnReason, entityData, entityNbt);
    }


    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(3, new WardenGoal(this, speed));
        this.goalSelector.add(4, new WardenFollowGoal(this, speed));
        this.goalSelector.add(2, new SniffGoal(this, speed));
        this.goalSelector.add(1, new WardenWanderGoal(this, 0.4));
    }
    @Override
    public void emitGameEvent(GameEvent event, @Nullable Entity entity, BlockPos pos) {}
    @Override
    public void emitGameEvent(GameEvent event, @Nullable Entity entity) {}
    @Override
    public void emitGameEvent(GameEvent event, BlockPos pos) {}
    @Override
    public void emitGameEvent(GameEvent event) {}


    public void tickMovement() {
            if (this.isAlive()) {
                boolean bl = this.burnsInDaylight() && this.isAffectedByDaylight();
                if (bl) {
                    ItemStack itemStack = this.getEquippedStack(EquipmentSlot.HEAD);
                    if (!itemStack.isEmpty()) {
                        if (itemStack.isDamageable()) {
                            itemStack.setDamage(itemStack.getDamage() + this.random.nextInt(2));
                            if (itemStack.getDamage() >= itemStack.getMaxDamage()) {
                                this.sendEquipmentBreakStatus(EquipmentSlot.HEAD);
                                this.equipStack(EquipmentSlot.HEAD, ItemStack.EMPTY);
                            }
                        }
                        bl = false;
                    }
                    if (bl) {
                        this.setOnFireFor(8);
                    }
                }
            }
        if(this.attackTicksLeft1 > 0) {
            --this.attackTicksLeft1;
        }
        if(this.roarTicksLeft1 > 0) {
            --this.roarTicksLeft1;
        }
        if(this.attackCooldown > 0) {
            --this.attackCooldown;
        }
        if(this.emergeTicksLeft > 0 && !this.hasEmerged) {
            digParticles(this.world, this.getBlockPos(), this.emergeTicksLeft);
            this.setInvulnerable(true);
            this.setVelocity(0,0,0);
            this.emergeTicksLeft--;
        }
        if (this.emergeTicksLeft==0 && !this.hasEmerged) {
            this.setInvulnerable(false);
            this.hasEmerged=true;
            this.emergeTicksLeft=-1;
        }
        if(this.emergeTicksLeft > 0 && this.hasEmerged) {
            digParticles(this.world, this.getBlockPos(), this.emergeTicksLeft);
            this.setInvulnerable(true);
            this.setVelocity(0,0,0);
            --this.emergeTicksLeft;
        }
        if (this.emergeTicksLeft==0 && this.hasEmerged) {
            this.remove(RemovalReason.DISCARDED);
        }
        if(world.getTime()==this.leaveTime) {
            this.handleStatus((byte) 6);
        }
        //Movement
        if(this.stuckPos!=null && this.getBlockPos().getSquaredDistance(this.stuckPos)<2 && this.hasEmerged && this.hasDetected && !(this.sniffTicksLeft>0)) {
            this.timeStuck++;
        } else {
            this.timeStuck=0;
            this.stuckPos=this.getBlockPos();
        }
        if(this.timeStuck>=30 && this.hasEmerged && this.world.getTime()-this.vibrationTimer<120 && this.world.getTime()-this.timeSinceLastRecalculation>49 && !(this.sniffTicksLeft>0)) {
            this.getNavigation().recalculatePath();
            this.timeSinceLastRecalculation=this.world.getTime();
        }
        if(this.followTicksLeft > 0) {
            --this.followTicksLeft;
        }
        //Sniffing
        if(this.sniffTicksLeft > 0) {
            --this.sniffTicksLeft;
        }
        if(this.sniffCooldown > 0) {
            --this.sniffCooldown;
        }
        if(this.sniffTicksLeft==0) {
            this.sniffTicksLeft=-1;
            int extraSuspicion=1;
            if (this.getSniffEntity()!=null) {
            LivingEntity sniffEntity = this.getSniffEntity();
            if (this.getBlockPos().getSquaredDistance(sniffEntity.getBlockPos(), true) <= 8) {
                extraSuspicion = extraSuspicion + UniformIntProvider.create(0,2).get(this.getRandom());
            }
            if (sniffEntity.getType()== EntityType.PLAYER) {
                extraSuspicion = extraSuspicion + 1;
            }
            this.addSuspicion(sniffEntity, extraSuspicion);
            if (sniffEntity!=this.getTrackingEntity()) {
                this.getNavigation().startMovingTo(sniffX, sniffY, sniffZ, (speed + (MathHelper.clamp(this.getSuspicion(sniffEntity), 0, 15) * 0.02) + (this.overallAnger() * 0.004)));
            } else if (sniffEntity==this.getTrackingEntity()) {
                this.followForTicks(sniffEntity, 35);
            }
            }
        }
        //Heartbeat & Anger
        this.hearbeatTime = (int) (60 - ((MathHelper.clamp(this.overallAnger(),0,15)*3.3)));
        if (this.world.getTime()>=this.nextHeartBeat) {
            this.world.playSound(null, this.getBlockPos().up(), RegisterSounds.ENTITY_WARDEN_HEARTBEAT, SoundCategory.HOSTILE, 1F, (float) (0.85F + (MathHelper.clamp(this.overallAnger(),0,15)*0.02)));
            this.nextHeartBeat=this.world.getTime()+hearbeatTime;
        }
        if (this.world.getTime()-this.timeSinceNonEntity>300 && this.nonEntityAnger>0) {
            --this.nonEntityAnger;
        }
        super.tickMovement();
    }

    private float getAttackDamage() {
        return (float)this.getAttributeValue(EntityAttributes.GENERIC_ATTACK_DAMAGE);
    }

    public void setRoarAnimationProgress(double a) {
        this.roarAnimationProgress = a;
    }
    public void roar() {
        this.attackTicksLeft1 = 10;
        this.world.sendEntityStatus(this, (byte)3);
    }

    public boolean tryAttack(Entity target) {
        float f = this.getAttackDamage();
        float g = (int)f > 0 ? f / 2.0F + (float)this.random.nextInt((int)f) : f;
        boolean bl = target.damage(DamageSource.mob(this), g);
        if (bl && this.attackCooldown<=0) {
            this.attackTicksLeft1 = 10;
            this.world.sendEntityStatus(this, (byte)4);
            target.setVelocity(target.getVelocity().add(0.0D, 0.4000000059604645D, 0.0D));
            this.applyDamageEffects(this, target);
            world.playSound(null, this.getBlockPos(), RegisterSounds.ENTITY_WARDEN_ATTACK, SoundCategory.HOSTILE, 1.0F,1.0F);
            this.attackCooldown=35;
        }
        return bl;
    }

    public int getAttackTicksLeft1() {
        return this.attackTicksLeft1;
    }

    public double getRoarAnimationProgress() {
        return this.roarAnimationProgress;
    }

    public int getRoarTicksLeft1() {
        return this.roarTicksLeft1;
    }

    public int getEmergeTicksLeft() {
        return this.emergeTicksLeft;
    }

    public int getSniffTicksLeft() {
        return this.sniffTicksLeft;
    }

    public boolean getHasEmerged() {
        return this.hasEmerged;
    }

    public void handleStatus(byte status) {
        if (status == 4) {
            this.attackTicksLeft1 = 10;
            world.playSound(null, this.getBlockPos(), RegisterSounds.ENTITY_WARDEN_AMBIENT, SoundCategory.HOSTILE, 1.0F,1.0F);
        } else if(status == 3) {
            this.roarTicksLeft1 = 10;
        } else if(status == 5) {
            //Emerging
            this.emergeTicksLeft=120;
            this.hasEmerged=false;
            world.playSound(null, this.getBlockPos(), RegisterSounds.ENTITY_WARDEN_EMERGE, SoundCategory.HOSTILE, 1F, 1F);
        } else if(status == 6) {
            //Digging Back
            this.emergeTicksLeft=60;
            this.hasEmerged=true;
            world.playSound(null, this.getBlockPos(), RegisterSounds.ENTITY_WARDEN_DIG, SoundCategory.HOSTILE, 1F, 1F);
        }  else {
            super.handleStatus(status);
        }

    }

    public void digParticles(World world, BlockPos pos, int ticks) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeBlockPos(pos);
        buf.writeInt(ticks);
        for (ServerPlayerEntity player : PlayerLookup.around((ServerWorld) world, pos, 32)) {
            ServerPlayNetworking.send(player, RegisterAccurateSculk.WARDEN_DIG_PARTICLES, buf);
        }
    }

    protected SoundEvent getHurtSound(DamageSource source) {return RegisterSounds.ENTITY_WARDEN_HURT;}

    protected SoundEvent getStepSound() {return RegisterSounds.ENTITY_WARDEN_STEP;}

    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(this.getStepSound(), 1.0F, 1.0F);
    }

    protected SoundEvent getAmbientSound(){return RegisterSounds.ENTITY_WARDEN_AMBIENT;}

    public void listen(BlockPos eventPos, World eventWorld, LivingEntity eventEntity, int suspicion) {
        if (!(this.emergeTicksLeft > 0) && this.world.getTime() - this.vibrationTimer >= 23) {
            this.sniffTicksLeft=-1;
            this.lasteventpos = eventPos;
            this.lasteventworld = eventWorld;
            this.lastevententity = eventEntity;
            this.hasDetected = true;
            this.vibrationTimer = this.world.getTime();
            this.leaveTime = this.world.getTime() + 1200;
            this.world.playSound(null, this.getBlockPos().up(2), RegisterSounds.ENTITY_WARDEN_VIBRATION, SoundCategory.HOSTILE, 0.5F, world.random.nextFloat() * 0.2F + 0.8F);
            CreateVibration(this.world, this, lasteventpos);
            if (eventEntity != null) {
                addSuspicion(eventEntity, suspicion);
                if(this.getTrackingEntity()!=null && eventEntity==this.getTrackingEntity()) {
                    this.timeSinceLastTracking=this.world.getTime();
                }
                if (this.world.getTime()-reactionSoundTimer>40) {
                    this.reactionSoundTimer=this.world.getTime();
                    if (getSuspicion(eventEntity)<15 && getSuspicion(eventEntity)>10) {
                        //ENTITY_WARDEN_ANGRY or VERY_ANGRY
                        this.world.playSound(null, this.getCameraBlockPos(), RegisterSounds.ENTITY_WARDEN_ANGRY, SoundCategory.HOSTILE, 1.0F, world.random.nextFloat() * 0.2F + 0.8F);
                    } else if (getSuspicion(eventEntity)>=15) {
                        //ENTITY_WARDEN_VERY_ANGRY or ANGRY
                        this.world.playSound(null, this.getCameraBlockPos(), RegisterSounds.ENTITY_WARDEN_ANGRY, SoundCategory.HOSTILE, 1.0F, world.random.nextFloat() * 0.2F + 0.8F);
                    } else if (this.overallAnger()<5) {
                        this.world.playSound(null, this.getCameraBlockPos(), RegisterSounds.ENTITY_WARDEN_SLIGHTLY_ANGRY, SoundCategory.HOSTILE, 1.0F, world.random.nextFloat() * 0.2F + 0.8F);
                    } else if (this.overallAnger()<10) {
                        this.world.playSound(null, this.getCameraBlockPos(), RegisterSounds.ENTITY_WARDEN_SLIGHTLY_ANGRY, SoundCategory.HOSTILE, 1.0F, world.random.nextFloat() * 0.2F + 0.8F);
                    }
                }
            } else {
                this.timeSinceNonEntity = this.world.getTime();
                this.nonEntityAnger=this.nonEntityAnger+1;
                if (this.world.getTime()-reactionSoundTimer>40) {
                    this.reactionSoundTimer = this.world.getTime();
                    if (this.overallAnger()<5) {
                        this.world.playSound(null, this.getCameraBlockPos(), RegisterSounds.ENTITY_WARDEN_SLIGHTLY_ANGRY, SoundCategory.HOSTILE, 1.0F, world.random.nextFloat() * 0.2F + 0.8F);
                    } else {
                        this.world.playSound(null, this.getCameraBlockPos(), RegisterSounds.ENTITY_WARDEN_SLIGHTLY_ANGRY, SoundCategory.HOSTILE, 1.0F, world.random.nextFloat() * 0.2F + 0.8F);
                    }
                }
            }
        }
    }

    public void sculkSensorListen(BlockPos eventPos, BlockPos vibrationPos, World eventWorld, LivingEntity eventEntity, int suspicion) {
        if (!(this.emergeTicksLeft > 0) && this.world.getTime() - this.vibrationTimer >= 23) {
            this.sniffTicksLeft=-1;
            this.lasteventpos = eventPos;
            this.lastevententity = eventEntity;
            this.lasteventworld = eventWorld;
            this.hasDetected = true;
            this.vibrationTimer = this.world.getTime();
            this.leaveTime = this.world.getTime() + 1200;
            this.world.playSound(null, this.getBlockPos().up(2), RegisterSounds.ENTITY_WARDEN_VIBRATION, SoundCategory.HOSTILE, 0.5F, world.random.nextFloat() * 0.2F + 0.8F);
            CreateVibration(this.world, this, vibrationPos);
            if (eventEntity != null) {
                addSuspicion(eventEntity, suspicion);
                if(this.getTrackingEntity()!=null && eventEntity==this.getTrackingEntity()) {
                    this.timeSinceLastTracking=this.world.getTime();
                }
                if (this.world.getTime()-reactionSoundTimer>60) {
                    this.reactionSoundTimer=this.world.getTime();
                if (getSuspicion(eventEntity)<15 && getSuspicion(eventEntity)>10) {
                    //ENTITY_WARDEN_ANGRY or VERY_ANGRY
                    this.world.playSound(null, this.getCameraBlockPos(), RegisterSounds.ENTITY_WARDEN_SLIGHTLY_ANGRY, SoundCategory.HOSTILE, 1.0F, world.random.nextFloat() * 0.2F + 0.8F);
                } else if (getSuspicion(eventEntity)>=15) {
                    //ENTITY_WARDEN_VERY_ANGRY or ANGRY
                    this.world.playSound(null, this.getCameraBlockPos(), RegisterSounds.ENTITY_WARDEN_SLIGHTLY_ANGRY, SoundCategory.HOSTILE, 1.0F, world.random.nextFloat() * 0.2F + 0.8F);
                } else if (this.overallAnger()<5) {
                        this.world.playSound(null, this.getCameraBlockPos(), RegisterSounds.ENTITY_WARDEN_SLIGHTLY_ANGRY, SoundCategory.HOSTILE, 1.0F, world.random.nextFloat() * 0.2F + 0.8F);
                } else if (this.overallAnger()<10) {
                    this.world.playSound(null, this.getCameraBlockPos(), RegisterSounds.ENTITY_WARDEN_SLIGHTLY_ANGRY, SoundCategory.HOSTILE, 1.0F, world.random.nextFloat() * 0.2F + 0.8F);
                }
                }
            } else {
                this.timeSinceNonEntity = this.world.getTime();
                this.nonEntityAnger=this.nonEntityAnger+1;
                if (this.world.getTime()-reactionSoundTimer>60) {
                    this.reactionSoundTimer = this.world.getTime();
                    if (this.overallAnger()<5) {
                    this.world.playSound(null, this.getCameraBlockPos(), RegisterSounds.ENTITY_WARDEN_SLIGHTLY_ANGRY, SoundCategory.HOSTILE, 1.0F, world.random.nextFloat() * 0.2F + 0.8F);
                } else {
                    this.world.playSound(null, this.getCameraBlockPos(), RegisterSounds.ENTITY_WARDEN_SLIGHTLY_ANGRY, SoundCategory.HOSTILE, 1.0F, world.random.nextFloat() * 0.2F + 0.8F);
                    }
                }
            }
        }
    }

    public void CreateVibration(World world, WardenEntity warden, BlockPos blockPos2) {
        EntityPositionSource wardenPositionSource = new EntityPositionSource(this.getId()) {
            @Override
            public Optional<BlockPos> getPos(World world) {
                return Optional.of(warden.getCameraBlockPos());
            }
            @Override
            public PositionSourceType<?> getType() {
                return PositionSourceType.ENTITY;
            }
        };
        this.delay = this.distance = (int)Math.floor(Math.sqrt(warden.getCameraBlockPos().getSquaredDistance(blockPos2, false))) * 2;
        ((ServerWorld)world).sendVibrationPacket(new Vibration(blockPos2, wardenPositionSource, this.delay));
    }

    public void addSuspicion(LivingEntity entity, int suspicion) {
        if (!this.entityList.isEmpty()) {
            if (this.entityList.contains(entity.getUuid().hashCode())) {
                int slot = this.entityList.indexOf(entity.getUuid().hashCode());
                this.susList.set(slot, this.susList.getInt(slot) + suspicion);
                if (this.susList.getInt(slot)>=15 && this.getTrackingEntity()==null) {
                    this.trackingEntity=entity.getUuidAsString();
                    this.world.playSound(null, this.getBlockPos().up(), RegisterSounds.ENTITY_WARDEN_ROAR, SoundCategory.HOSTILE, 1F, 1F);
                    this.roar();
                }
            } else {
                this.entityList.add(entity.getUuid().hashCode());
                this.susList.add(suspicion);
            }
        } else {
            this.entityList.add(entity.getUuid().hashCode());
            this.susList.add(suspicion);
        }
    }

    public void followForTicks(LivingEntity entity, int ticks) {
        this.followingEntity = entity.getId();
        this.followTicksLeft = ticks;
    }

    public int getSuspicion(Entity entity) {
        if (!this.entityList.isEmpty() && entity!=null) {
            if (this.entityList.contains(entity.getUuid().hashCode())) {
                int slot = this.entityList.indexOf(entity.getUuid().hashCode());
                return this.susList.getInt(slot);
            }
        }
        return 0;
    }

    public int getHighestSuspicionInt() {
        int highest = 0;
        if (!this.susList.isEmpty()) {
            for (int i=0; i<this.susList.size(); i++) {
                if (this.susList.getInt(i)>highest) {
                    highest=this.susList.getInt(i);
                }
            }
        }
        return highest;
    }

    public LivingEntity mostSuspiciousAround() {
        int highest = 0;
        LivingEntity most = null;
        Box box = new Box(this.getBlockPos().add(-16,-16,-16), this.getBlockPos().add(16,16,16));
        List<LivingEntity> entities = world.getNonSpectatingEntities(LivingEntity.class, box);
        if (!entities.isEmpty()) {
            for (LivingEntity target : entities) {
                if (this.getBlockPos().getSquaredDistance(target.getBlockPos())<=16) {
                    if (this.getSuspicion(target)>highest) {
                        highest = this.getSuspicion(target);
                        most = target;
                    }
                }
            }
        }
        return most;
    }
    public int mostSuspiciousAroundInt() {
        int value=0;
        if (mostSuspiciousAround()!=null) {
            value=this.getSuspicion(mostSuspiciousAround());
        }
        return value;
    }
    public LivingEntity getTrackingEntity() {
        Box box = new Box(this.getBlockPos().add(-18,-18,-18), this.getBlockPos().add(18,18,18));
        List<LivingEntity> entities = this.world.getNonSpectatingEntities(LivingEntity.class, box);
        if (!entities.isEmpty()) {
            for (LivingEntity target : entities) {
                if (Objects.equals(this.trackingEntity, target.getUuidAsString())) {
                    if (MathAddon.distance(target.getX(), target.getY(), target.getZ(), this.getX(), this.getY(), this.getZ()) <= 16) {
                        return target;
                    }
                }
            }
        }
        return null;
    }
    public LivingEntity getSniffEntity() {
        Box box = new Box(this.getBlockPos().add(-18,-18,-18), this.getBlockPos().add(18,18,18));
        List<LivingEntity> entities = this.world.getNonSpectatingEntities(LivingEntity.class, box);
        if (!entities.isEmpty()) {
            for (LivingEntity target : entities) {
                if (Objects.equals(this.sniffEntity, target.getUuidAsString())) {
                    if (MathAddon.distance(target.getX(), target.getY(), target.getZ(), this.getX(), this.getY(), this.getZ()) <= 16) {
                        return target;
                    }
                }
            }
        }
        return null;
    }
    public boolean canFollow(Entity entity, boolean mustBeTracking) {
        Box box = new Box(this.getBlockPos().add(-20,-20,-20), this.getBlockPos().add(20,20,20));
        List<Entity> entities = world.getNonSpectatingEntities(Entity.class, box);
        if (!entities.isEmpty() && entities.contains(entity)) {
            if (MathAddon.distance(entity.getX(), entity.getY(), entity.getZ(), this.getX(), this.getY(), this.getZ()) <= 18) {
                if (mustBeTracking) {
                    return entity == this.getTrackingEntity();
                } else {
                    return true;
                }
            }
        }
        return false;
    }
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putLong("vibrationTimer", this.vibrationTimer);
        nbt.putLong("timeSinceLastTracking", this.timeSinceLastTracking);
        nbt.putLong("leaveTime", this.leaveTime);
        nbt.putInt("emergeTicksLeft", this.emergeTicksLeft);
        nbt.putBoolean("hasEmerged", this.hasEmerged);
        nbt.putIntArray("entityList", this.entityList);
        nbt.putIntArray("susList", this.susList);
        nbt.putString("trackingEntity", this.trackingEntity);
        nbt.putInt("sniffTicksLeft", this.sniffTicksLeft);
        nbt.putInt("sniffCooldown", this.sniffCooldown);
        nbt.putInt("attackCooldown", this.attackCooldown);
        nbt.putInt("sniffX", this.sniffX);
        nbt.putInt("sniffY", this.sniffY);
        nbt.putInt("sniffZ", this.sniffZ);
        nbt.putString("sniffEntity", this.sniffEntity);
        nbt.putInt("followTicksLeft", this.followTicksLeft);
        nbt.putInt("followingEntity", this.followingEntity);
        nbt.putInt("nonEntityAnger", this.nonEntityAnger);
        nbt.putLong("timeSinceNonEntity", this.timeSinceNonEntity);
    }
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.vibrationTimer = nbt.getLong("vibrationTimer");
        this.timeSinceLastTracking = nbt.getLong("timeSinceLastTracking");
        this.leaveTime = nbt.getLong("leaveTime");
        this.emergeTicksLeft = nbt.getInt("emergeTicksLeft");
        this.hasEmerged = nbt.getBoolean("hasEmerged");
        this.entityList = IntArrayList.wrap(nbt.getIntArray("entityList"));
        this.susList = IntArrayList.wrap(nbt.getIntArray("susList"));
        this.trackingEntity = nbt.getString("trackingEntity");
        this.sniffTicksLeft = nbt.getInt("sniffTicksLeft");
        this.sniffCooldown = nbt.getInt("sniffCooldown");
        this.attackCooldown = nbt.getInt("attackCooldown");
        this.sniffX = nbt.getInt("sniffX");
        this.sniffY = nbt.getInt("sniffY");
        this.sniffZ = nbt.getInt("sniffZ");
        this.sniffEntity = nbt.getString("sniffEntity");
        this.followTicksLeft = nbt.getInt("followTicksLeft");
        this.followingEntity = nbt.getInt("followingEntity");
        this.nonEntityAnger = nbt.getInt("nonEntityAnger");
        this.timeSinceNonEntity = nbt.getLong("timeSinceNonEntity");
    }

    public int eventSuspicionValue(GameEvent event, LivingEntity livingEntity) {
        int total=1;
        EntityType entity=livingEntity.getType();
        if (entity==EntityType.PLAYER) {
            total=total+1;
        }
        if(this.getBlockPos().getSquaredDistance(livingEntity.getBlockPos(), true)<=8) {
            total=total+UniformIntProvider.create(0,2).get(this.getWorld().getRandom());
        }
        if(event==GameEvent.PROJECTILE_LAND && total>1) {
            total=total-1;
        }
        return total;
    }
    public int overallAnger() {
        int anger=0;
        Box box = new Box(this.getBlockPos().add(-32,-32,-32), this.getBlockPos().add(32,32,32));
        List<LivingEntity> entities = world.getNonSpectatingEntities(LivingEntity.class, box);
        if (!entities.isEmpty()) {
            for (LivingEntity target : entities) {
                if (this.getBlockPos().getSquaredDistance(target.getBlockPos())<=24 && target.isAlive()) {
                    anger = anger + this.getSuspicion(target);
                }
            }
        }
        anger = anger + nonEntityAnger;
        return MathHelper.clamp(anger,0,25);
    }
}
