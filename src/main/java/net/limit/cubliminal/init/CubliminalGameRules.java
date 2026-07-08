package net.limit.cubliminal.init;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.limit.cubliminal.util.DebugLogger;
import net.minecraft.world.GameRules;

public class CubliminalGameRules {
    public static final GameRules.Key<GameRules.BooleanRule> CORPSES_ON_DEATH =
            GameRuleRegistry.register(
                    "corpsesOnDeath",
                    GameRules.Category.PLAYER,
                    GameRuleFactory.createBooleanRule(true)
            );


    public static void init() {}
}
