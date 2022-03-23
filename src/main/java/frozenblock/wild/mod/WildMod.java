package frozenblock.wild.mod;

import frozenblock.wild.mod.registry.*;
import frozenblock.wild.mod.worldgen.ReplaceInTagStructureProcessor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameRules;

public class WildMod implements ModInitializer {

    public static final String MOD_ID = "twm";

    public static final StructureProcessorType<ReplaceInTagStructureProcessor> REPLACE_IN_TAG = StructureProcessorType.register("replace_in_tag", ReplaceInTagStructureProcessor.CODEC);

    @Override
    public void onInitialize() {
        RegisterBlocks.RegisterBlocks();
        RegisterItems.RegisterItems();
        RegisterEntities.RegisterEntities();
        RegisterDispenser.RegisterDispenser();
        RegisterParticles.RegisterParticles();
        RegisterStatusEffects.RegisterStatusEffects();
        RegisterWorldgen.RegisterWorldgen();

        RegisterAccurateSculk.RegisterAccurateSculk();
    }

    public static final GameRules.Key<GameRules.BooleanRule> DARKNESS_ENABLED =
            GameRuleRegistry.register("doDarkness", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));
    public static final GameRules.Key<GameRules.BooleanRule> SHRIEKER_NEEDS_SCULK =
            GameRuleRegistry.register("shriekerNeedsSculk", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(false));
    public static final GameRules.Key<GameRules.BooleanRule> SHRIEKER_SHRIEKS =
            GameRuleRegistry.register("shriekerShrieks", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));
    public static final GameRules.Key<GameRules.BooleanRule> DO_CATALYST_POLLUTION =
            GameRuleRegistry.register("catalystPollution", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));
    public static final GameRules.Key<GameRules.BooleanRule> DO_CATALYST_VIBRATIONS =
            GameRuleRegistry.register("catalystVibrations", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(false));
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

    //TESTING GAMERULES. TODO: DELETE THESE AT 1.0. DO NOT WRITE TOO MUCH CODE THAT DEPENDS ON THESE.
    public static final GameRules.Key<GameRules.BooleanRule> NO_WARDEN_COOLDOWN =
            GameRuleRegistry.register("noWardenCooldown", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(false));
    public static final GameRules.Key<GameRules.BooleanRule> WARDEN_BURNS =
            GameRuleRegistry.register("wardenBurns", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(false));
    public static final GameRules.Key<GameRules.BooleanRule> WARDEN_SPAWNING =
            GameRuleRegistry.register("wardenSpawns", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));
}
