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
                builder.push("Traveler Settings");

                baseSpeedCap = builder
                                .comment("Base maximum speed bonus cap (flat value added to movement speed). Default: 0.2")
                                .comment("基础速度加成上限（直接加到移动速度）。默认值：0.2")
                                .translation("wanderlusts_way_to_survive.config.baseSpeedCap")
                                .defineInRange("baseSpeedCap", 0.2, 0.0, 10.0);

                xpSpeedMultiplier = builder
                                .comment("Speed bonus cap increase per experience level. Cap = Base + (Level * Multiplier). Default: 0.005")
                                .comment("每经验等级增加的速度上限。上限 = 基础 + (等级 * 倍率)。默认值：0.005")
                                .translation("wanderlusts_way_to_survive.config.xpSpeedMultiplier")
                                .defineInRange("xpSpeedMultiplier", 0.005, 0.0, 1.0);

                enchantmentLevelMultiplier = builder
                                .comment("Speed multiplier per level of Traveler enchantment. Total Speed = CalculatedSpeed * (1 + Level * EnchMultiplier). Default: 0.1")
                                .comment("旅者附魔每级的速度倍率。总速度 = 计算速度 * (1 + 附魔等级 * 倍率)。默认值：0.1")
                                .translation("wanderlusts_way_to_survive.config.enchantmentLevelMultiplier")
                                .defineInRange("enchantmentLevelMultiplier", 0.1, 0.0, 5.0);

                blockSpeedCaps = builder
                                .comment("Specific speed caps for block IDs. Format: 'modid:blockid,cap_value'. Blocks not listed use the default calculation.")
                                .comment("特定方块的速度上限。格式：'modid:blockid,上限值'。未列出的方块使用默认计算。")
                                .translation("wanderlusts_way_to_survive.config.blockSpeedCaps")
                                .defineListAllowEmpty("blockSpeedCaps",
                                                Arrays.asList(
                                                                "minecraft:dirt_path,0.3",
                                                                "minecraft:soul_soil,0.4",
                                                                "minecraft:packed_ice,0.5"),
                                                () -> "minecraft:block_id,0.2",
                                                MomentumConfig::validateBlockSpeedEntry);

                builder.pop();
        }

        /**
         * Validates a block speed entry in the format "modid:blockid,cap_value"
         */
        private static boolean validateBlockSpeedEntry(Object entry) {
                if (!(entry instanceof String str))
                        return false;
                String[] parts = str.split(",");
                if (parts.length != 2)
                        return false;
                // Check block ID format
                if (!parts[0].contains(":"))
                        return false;
                // Check cap value is a number
                try {
                        Double.parseDouble(parts[1].trim());
                        return true;
                } catch (NumberFormatException e) {
                        return false;
                }
        }
}
