package frozenblock.wild.mod.entity;


import frozenblock.wild.mod.fromAccurateSculk.ShriekCounter;
import frozenblock.wild.mod.liukrastapi.WardenGoal;
import frozenblock.wild.mod.registry.RegisterAccurateSculk;
import frozenblock.wild.mod.registry.RegisterEntities;
import frozenblock.wild.mod.registry.RegisterSounds;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.Vibration;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.PositionSource;
import net.minecraft.world.event.PositionSourceType;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import java.util.Optional;

public class WardenEntity extends HostileEntity {

    private int attackTicksLeft1;

    private int roarTicksLeft1;

    private double roarAnimationProgress;

    private static final double speed = 0.5D;

    public BlockPos lasteventpos;
    public World lasteventworld;
    public LivingEntity lastevententity;
    public ArrayList<UUID> entityList = new ArrayList<UUID>();
    public ArrayList<Integer> susList = new ArrayList<Integer>();

    public BlockPos outPos;

    public boolean isDiggingToLocation=false;
    public boolean hasDetected=false;
    public int emergeTicksLeft;
    public int timeStuck=0;
    public BlockPos stuckPos;
    public boolean hasEmerged;
    public long vibrationTimer = 0;
    public long leaveTime;
    protected int delay = 0;
    protected int distance;


    public WardenEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
    }

    public static DefaultAttributeContainer.Builder createWardenAttributes() {
        return HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_MAX_HEALTH, 500.0D).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, speed).add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0D).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 31.0D);
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
        this.goalSelector.add(2, new WardenGoal(this, speed));
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
        if(this.attackTicksLeft1 > 0) {
            --this.attackTicksLeft1;
        }
        if(this.roarTicksLeft1 > 0) {
            --this.roarTicksLeft1;
        }
        if(this.emergeTicksLeft > 0 && !this.hasEmerged && !this.isDiggingToLocation) {
            digParticles(this.world, this.getBlockPos(), this.emergeTicksLeft);
            this.setInvulnerable(true);
            this.setVelocity(0,0,0);
            this.emergeTicksLeft--;
        }
        if (this.emergeTicksLeft==0 && !this.hasEmerged && !this.isDiggingToLocation) {
            this.setInvulnerable(false);
            this.hasEmerged=true;
            this.emergeTicksLeft=-1;
        }
        if(this.emergeTicksLeft > 0 && this.hasEmerged && !this.isDiggingToLocation) {
            digParticles(this.world, this.getBlockPos(), this.emergeTicksLeft);
            this.setInvulnerable(true);
            this.setVelocity(0,0,0);
            --this.emergeTicksLeft;
        }
        if (this.emergeTicksLeft==0 && this.hasEmerged && !this.isDiggingToLocation) {
            this.remove(RemovalReason.DISCARDED);
        }
        if(!this.isDiggingToLocation && world.getTime()==this.leaveTime) {
            this.handleStatus((byte) 6);
        }
        //DIGGING TO NEW LOCATION
        if(this.stuckPos!=null && this.getBlockPos().getSquaredDistance(this.stuckPos)<2 && this.hasEmerged && this.hasDetected) {
            this.timeStuck++;
        } else {
            this.timeStuck=0;
            this.stuckPos=this.getBlockPos();
        }
        if(this.timeStuck>=200 && !this.isDiggingToLocation && this.hasEmerged && world.getTime()-this.vibrationTimer<200) {
            this.handleStatus((byte) 8);
        }
        if(this.emergeTicksLeft>-2 && this.hasEmerged && this.isDiggingToLocation) {
            this.setInvulnerable(true);
            this.setVelocity(0,0,0);
            if (this.emergeTicksLeft!=-2) {
                digParticles(this.world, this.getBlockPos(), this.emergeTicksLeft);
            }
            --this.emergeTicksLeft;
        }
        if (this.emergeTicksLeft==-2 && this.hasEmerged && this.isDiggingToLocation) {
            this.emergeTicksLeft=-20;
            newWarden(this.world, outPos);
            this.remove(RemovalReason.DISCARDED);
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
        if (bl) {
            this.attackTicksLeft1 = 10;
            this.world.sendEntityStatus(this, (byte)4);
            target.setVelocity(target.getVelocity().add(0.0D, 0.4000000059604645D, 0.0D));
            this.applyDamageEffects(this, target);
            world.playSound(null, this.getBlockPos(), RegisterSounds.ENTITY_WARDEN_SLIGHTLY_ANGRY, SoundCategory.HOSTILE, 1.0F,1.0F);
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
            this.stuckPos=this.getBlockPos();
            world.playSound(null, this.getBlockPos(), RegisterSounds.ENTITY_WARDEN_EMERGE, SoundCategory.HOSTILE, 1F, 1F);
        } else if(status == 6) {
            //Digging Back
            this.emergeTicksLeft=60;
            this.hasEmerged=true;
            world.playSound(null, this.getBlockPos(), RegisterSounds.ENTITY_WARDEN_DIG, SoundCategory.HOSTILE, 1F, 1F);
        } else if(status == 8) {
            //Digging To Last Event Location If It's Spawnable And Warden is Stuck
            Box box = new Box(this.getBlockPos().add(-16,-16,-16), this.getBlockPos().add(16,16,16));
            List<LivingEntity> entities = world.getNonSpectatingEntities(LivingEntity.class, box);
                if (entities.size() > 0) {
                    for (LivingEntity target : entities) {
                        if (target.isAlive() && target!=this && ShriekCounter.wardenOutLocation(target.getBlockPos(), this.world)!=null) {
                            this.emergeTicksLeft = 60;
                            this.isDiggingToLocation = true;
                            this.hasEmerged = true;
                            world.playSound(null, this.getBlockPos(), RegisterSounds.ENTITY_WARDEN_DIG, SoundCategory.HOSTILE, 1F, 1F);
                            this.outPos=ShriekCounter.wardenOutLocation(target.getBlockPos(), this.world);
                            break;
                        }
                    }
                }
        } else {
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

    protected SoundEvent getAmbientSound(){return RegisterSounds.ENTITY_WARDEN_AMBIENT;}
    
    protected SoundEvent getHurtSound(DamageSource source) {
        return RegisterSounds.ENTITY_WARDEN_HURT;
    }

    public void listen(BlockPos eventPos, World eventWorld, LivingEntity eventEntity) {
        if(this.lasteventpos == eventPos && this.lasteventworld == eventWorld && this.lastevententity == eventEntity && this.world.getTime()-this.vibrationTimer>=23 && this.hasEmerged) {
            this.hasDetected=true;
            this.vibrationTimer=this.world.getTime();
            this.leaveTime=this.world.getTime()+1200;
            world.playSound(null, this.getBlockPos().up(2), SoundEvents.BLOCK_SCULK_SENSOR_CLICKING, SoundCategory.HOSTILE, 0.5F,world.random.nextFloat() * 0.2F + 0.8F);
            BlockPos WardenHead = this.getBlockPos().up((int) 3);
            PositionSource wardenPositionSource = new PositionSource() {
                @Override
                public Optional<BlockPos> getPos(World world) {
                    return Optional.of(WardenHead);
                }
                @Override
                public PositionSourceType<?> getType() {
                    return null;
                }
            };
                CreateVibration(this.world, lasteventpos, wardenPositionSource, WardenHead);
            if (eventEntity!=null) {
                addSuspicion(eventEntity);
            }
            }
        this.lasteventpos = eventPos;
        this.lasteventworld = eventWorld;
        this.lastevententity = eventEntity;
    }
    
        public void addSuspicion(LivingEntity entity) {
        if (!this.entityList.isEmpty()) {
            if (this.entityList.contains(entity.getUuid())) {
                int slot = this.entityList.indexOf(entity.getUuid());
                this.susList.set(slot, this.susList.get(slot) + 1);
            } else {
                this.entityList.add(entity.getUuid());
                this.susList.add(1);
            }
        } else {
            this.entityList.add(entity.getUuid());
            this.susList.add(1);
        }
    }

    public int getSuspicion(UUID id) {
        if (!this.entityList.isEmpty()) {
            if (this.entityList.contains(id)) {
                int slot = this.entityList.indexOf(id);
                return this.susList.get(slot);
            }
        }
        return 0;
    }
    
    public void newWarden(World world, BlockPos currentCheck) {
        if (currentCheck!=null) {
            WardenEntity warden = (WardenEntity) RegisterEntities.WARDEN.create(world);
            warden.refreshPositionAndAngles((double) currentCheck.getX() + 1D, (double) currentCheck.up(1).getY(), (double) currentCheck.getZ() + 1D, 0.0F, 0.0F);
            world.spawnEntity(warden);
            warden.setHealth(this.getHealth());
            warden.lasteventpos = this.lasteventpos;
            if (this.lastevententity != null) {
                warden.lastevententity = this.lastevententity;
            }
            if (this.lasteventworld != null) {
                warden.lasteventworld = this.lasteventworld;
            }
            warden.handleStatus((byte) 5);
            warden.leaveTime = world.getTime() + 1200;
            world.playSound(null, currentCheck, RegisterSounds.ENTITY_WARDEN_EMERGE, SoundCategory.HOSTILE, 1F, 1F);
        }
    }

    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.putLong("vibrationTimer", this.vibrationTimer);
        nbt.putLong("leaveTime", this.leaveTime);
        nbt.putInt("emergeTicksLeft", this.emergeTicksLeft);
        nbt.putBoolean("hasEmerged", this.hasEmerged);
    }
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        this.vibrationTimer = nbt.getLong("vibrationTimer");
        this.leaveTime = nbt.getLong("leaveTime");
        this.emergeTicksLeft = nbt.getInt("emergeTicksLeft");
        this.hasEmerged = nbt.getBoolean("hasEmerged");
    }
    public void CreateVibration(World world, BlockPos blockPos, PositionSource positionSource, BlockPos blockPos2) {
        this.delay = this.distance = (int)Math.floor(Math.sqrt(blockPos.getSquaredDistance(blockPos2, false))) * 2 ;
        ((ServerWorld)world).sendVibrationPacket(new Vibration(blockPos, positionSource, this.delay));
    }
}
