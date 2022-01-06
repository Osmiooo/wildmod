package frozenblock.wild.mod;

import frozenblock.wild.mod.registry.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;

public class WildMod implements ModInitializer {

    public static final String MOD_ID = "twm";

    public static final GameRules.Key<GameRules.BooleanRule> SHRIEKER_NEEDS_SCULK =
            GameRuleRegistry.register("shriekerNeedsSculk", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(false));
    public static final GameRules.Key<GameRules.BooleanRule> SHRIEKER_SHRIEKS =
            GameRuleRegistry.register("shriekerShrieks", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));
    public static final GameRules.Key<GameRules.BooleanRule> DO_CATALYST_POLLUTION =
            GameRuleRegistry.register("catalystPollution", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));
    public static final GameRules.Key<GameRules.BooleanRule> DO_CATALYST_VIBRATIONS =
            GameRuleRegistry.register("catalystVibrations", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(false));
    public static final GameRules.Key<GameRules.BooleanRule> CATALYST_DETECTS_ALL =
            GameRuleRegistry.register("catalystDetectsAll", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(false));
    public static final GameRules.Key<GameRules.BooleanRule> SHRIEKER_GARGLES =
            GameRuleRegistry.register("shriekerGargles", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(false));
    public static final GameRules.Key<GameRules.BooleanRule> SCULK_THREADING =
            GameRuleRegistry.register("sculkThreading", GameRules.Category.MISC, GameRuleFactory.createBooleanRule(true));
    public static final GameRules.Key<GameRules.IntRule> SCULK_MULTIPLIER =
            GameRuleRegistry.register("spreadMultiplier", GameRules.Category.MISC, GameRuleFactory.createIntRule(1, 1, 20));
    public static final GameRules.Key<GameRules.IntRule> SCULK_THREADS =
            GameRuleRegistry.register("sculkThreads", GameRules.Category.MISC, GameRuleFactory.createIntRule(2, 1, 21));
    public static final GameRules.Key<GameRules.IntRule> UPWARD_SPREAD =
            GameRuleRegistry.register("upwardSpread", GameRules.Category.MISC, GameRuleFactory.createIntRule(3, 1, 100));
    public static final GameRules.Key<GameRules.IntRule> DOWNWARD_SPREAD =
            GameRuleRegistry.register("downwardSpread", GameRules.Category.MISC, GameRuleFactory.createIntRule(8, 1, 100));

    @Override
    public void onInitialize() {
        RegisterBlocks.RegisterBlocks();
        RegisterItems.RegisterItems();
        RegisterEntities.RegisterEntities();
        RegisterDispenser.RegisterDispenser();
        RegisterParticles.RegisterParticles();
        RegisterSounds.RegisterSounds();
        RegisterStatusEffects.RegisterStatusEffects();
        RegisterWorldgen.RegisterWorldgen();

        RegisterAccurateSculk.RegisterAccurateSculk();
    }
}