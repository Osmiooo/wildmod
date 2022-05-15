package net.frozenblock.wildmod;

import com.chocohead.mm.api.ClassTinkerers;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.frozenblock.wildmod.entity.FrogBrain;
import net.frozenblock.wildmod.entity.ai.sensor.WardenAttackablesSensor;
import net.frozenblock.wildmod.event.GameEvent;
import net.frozenblock.wildmod.liukrastapi.*;
import net.frozenblock.wildmod.mixins.ActivityInvoker;
import net.frozenblock.wildmod.mixins.SensorTypeInvoker;
import net.frozenblock.wildmod.registry.*;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.brain.sensor.TemptationsSensor;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.Int2ObjectBiMap;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameRules;
import net.minecraft.world.gen.StructureWeightType;

import java.util.OptionalInt;

public class WildMod implements ModInitializer {

    public static final String MOD_ID = "twm";
    public static final EntityPose CROAKING = ClassTinkerers.getEnum(EntityPose.class, "CROAKING");
    public static final EntityPose USING_TONGUE = ClassTinkerers.getEnum(EntityPose.class, "USING_TONGUE");
    public static final EntityPose ROARING = ClassTinkerers.getEnum(EntityPose.class, "ROARING");
    public static final EntityPose SNIFFING = ClassTinkerers.getEnum(EntityPose.class, "SNIFFING");
    public static final EntityPose EMERGING = ClassTinkerers.getEnum(EntityPose.class, "EMERGING");
    public static final EntityPose DIGGING = ClassTinkerers.getEnum(EntityPose.class, "DIGGING");

    public static final StructureWeightType BEARD_AND_SHAVE = ClassTinkerers.getEnum(StructureWeightType.class, "BEARD_AND_SHAVE");

    public static final ItemCriterion ALLAY_DROP_ITEM_ON_BLOCK = new ItemCriterion(new Identifier(WildMod.MOD_ID, "allay_drop_item_on_block"));


    @Override
    public void onInitialize() {
        RegisterMemoryModules.RegisterMemoryModules();
        RegisterBlocks.RegisterBlocks();
        RegisterItems.RegisterItems();
        RegisterEntities.RegisterEntities();

        RegisterDispenser.RegisterDispenser();
        RegisterParticles.RegisterParticles();
        RegisterStatusEffects.RegisterStatusEffects();
        RegisterWorldgen.RegisterWorldgen();

        registerData(OPTIONAL_INT);

        TONGUE = ActivityInvoker.callRegister( "tongue");
        SWIM = ActivityInvoker.callRegister( "swim");
        LAY_SPAWN = ActivityInvoker.callRegister( "lay_spawn");
        SNIFF = ActivityInvoker.callRegister("sniff");
        INVESTIGATE = ActivityInvoker.callRegister("investigate");
        ROAR = ActivityInvoker.callRegister("roar");
        EMERGE = ActivityInvoker.callRegister("emerge");
        DIG = ActivityInvoker.callRegister("dig");

        //Transformation.Interpolations.registerInterpolations();

        RegisterAccurateSculk.RegisterAccurateSculk();
        GameEvent.RegisterGameEvents();
    }

    public static void registerData(TrackedDataHandler<?> handler) {
        DATA_HANDLERS.add(handler);
    }

    private static final Int2ObjectBiMap<TrackedDataHandler<?>> DATA_HANDLERS = Int2ObjectBiMap.create(16);

    public static <T> T registerInRegistryVanilla(Registry<T> registry, Identifier identifier, T values) {
        return Registry.register(registry, identifier, values);
    }

    //public static Registry<Animation> ANIMATION_DEFINITIONS = FabricRegistryBuilder.createSimple(Animation.class, new Identifier(WildMod.MOD_ID, "animation_definitions")).buildAndRegister();
    //public static Registry<Transformation.Interpolations> ANIMATION_CHANNEL_INTERPOLATIONS = FabricRegistryBuilder.createSimple(Transformation.Interpolations.class, new Identifier(WildMod.MOD_ID, "animation_channel_interpolations")).buildAndRegister();
    //public static Registry<Transformation.Target> ANIMATION_CHANNEL_TARGETS = FabricRegistryBuilder.createSimple(Transformation.Target.class, new Identifier(WildMod.MOD_ID, "animation_channel_targets")).buildAndRegister();

    public static final SensorType<TemptationsSensor> FROG_TEMPTATIONS = SensorTypeInvoker.callRegister("frog_temptations", () -> new TemptationsSensor(FrogBrain.getTemptItems()));
    public static final SensorType<FrogAttackablesSensor> FROG_ATTACKABLES = SensorTypeInvoker.callRegister("frog_attackables", FrogAttackablesSensor::new);
    public static final SensorType<IsInWaterSensor> IS_IN_WATER = SensorTypeInvoker.callRegister("is_in_water", IsInWaterSensor::new);
    public static final SensorType<WardenAttackablesSensor> WARDEN_ENTITY_SENSOR = SensorTypeInvoker.callRegister("warden_entity_sensor", WardenAttackablesSensor::new);

    public static Activity TONGUE;
    public static Activity SWIM;
    public static Activity LAY_SPAWN;
    public static Activity SNIFF;
    public static Activity INVESTIGATE;
    public static Activity ROAR;
    public static Activity EMERGE;
    public static Activity DIG;

    public static final TrackedDataHandler<OptionalInt> OPTIONAL_INT = new TrackedDataHandler<OptionalInt>() {
        public void write(PacketByteBuf packetByteBuf, OptionalInt optionalInt) {
            packetByteBuf.writeVarInt(optionalInt.orElse(-1) + 1);
        }

        public OptionalInt read(PacketByteBuf packetByteBuf) {
            int i = packetByteBuf.readVarInt();
            return i == 0 ? OptionalInt.empty() : OptionalInt.of(i - 1);
        }

        public OptionalInt copy(OptionalInt optionalInt) {
            return optionalInt;
        }
    };

    public static final GameRules.Key<GameRules.BooleanRule> DARKNESS_ENABLED =
            GameRuleRegistry.register("doDarkness", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));
    public static final GameRules.Key<GameRules.BooleanRule> SHRIEKER_NEEDS_SCULK =
            GameRuleRegistry.register("shriekerNeedsSculk", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(false));
    public static final GameRules.Key<GameRules.BooleanRule> SHRIEKER_GARGLES =
            GameRuleRegistry.register("shriekerGargles", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(false));
    public static final GameRules.Key<GameRules.IntRule> SCULK_MULTIPLIER =
            GameRuleRegistry.register("spreadMultiplier", GameRules.Category.MISC, GameRuleFactory.createIntRule(1, 1, 20));
    public static final GameRules.Key<GameRules.IntRule> UPWARD_SPREAD =
            GameRuleRegistry.register("upwardSpread", GameRules.Category.MISC, GameRuleFactory.createIntRule(12, 1, 100));
    public static final GameRules.Key<GameRules.IntRule> DOWNWARD_SPREAD =
            GameRuleRegistry.register("downwardSpread", GameRules.Category.MISC, GameRuleFactory.createIntRule(12, 1, 100));
    public static final GameRules.Key<GameRules.BooleanRule> SCULK_STOPS_SCULKCHECK = //PERFORMANCE GAMERULE, STOPS SCULKCHECK SEARCHING IF THE CURRENT BLOCK IS SCULK
            GameRuleRegistry.register("sculkStopsSculkCheck", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(false));

    public static final GameRules.Key<GameRules.BooleanRule> DO_WARDEN_SPAWNING =
            GameRuleRegistry.register("doWardenSpawning", GameRules.Category.SPAWNING, GameRuleFactory.createBooleanRule(true));
}
