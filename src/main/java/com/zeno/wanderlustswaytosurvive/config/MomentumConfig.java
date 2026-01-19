package com.zeno.wanderlustswaytosurvive.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;

public class MomentumConfig {
    public static final ModConfigSpec SPEC;
    public static final MomentumConfig INSTANCE;

    static {
        Pair<MomentumConfig, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(MomentumConfig::new);
        SPEC = pair.getRight();
        INSTANCE = pair.getLeft();
    }

    public final ModConfigSpec.DoubleValue baseSpeedCap;
    public final ModConfigSpec.DoubleValue xpSpeedMultiplier;
    public final ModConfigSpec.DoubleValue enchantmentLevelMultiplier;
    public final ModConfigSpec.ConfigValue<List<? extends String>> blockSpeedCaps;

    MomentumConfig(ModConfigSpec.Builder builder) {
        builder.push("Momentum Settings");

        baseSpeedCap = builder
                .comment("Base maximum speed bonus cap (flat value added to movement speed). Default: 0.2")
                .defineInRange("baseSpeedCap", 0.2, 0.0, 10.0);

        xpSpeedMultiplier = builder
                .comment(
                        "Speed bonus cap increase per experience level. Cap = Base + (Level * Multiplier). Default: 0.005")
                .defineInRange("xpSpeedMultiplier", 0.005, 0.0, 1.0);

        enchantmentLevelMultiplier = builder
                .comment(
                        "Speed multiplier per level of Momentum enchantment. Total Speed = CalculatedSpeed * (1 + Level * EnchMultiplier). Default: 0.1")
                .defineInRange("enchantmentLevelMultiplier", 0.1, 0.0, 5.0);

        blockSpeedCaps = builder
                .comment(
                        "Specific speed caps for block IDs. Format: 'modid:blockid,cap_value'. Blocks not listed use the default calculation.")
                .defineList("blockSpeedCaps", Arrays.asList(
                        "minecraft:dirt_path,0.3",
                        "minecraft:soul_soil,0.4",
                        "minecraft:packed_ice,0.5"), entry -> true);

        builder.pop();
    }
}
