package com.zeno.wanderlustswaytosurvive.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;

/**
 * 旅者附魔配置类 (现在包含所有通用配置)
 * 使用NeoForge的配置系统定义可调整的参数
 */
public class MomentumConfig {
        public static enum HorseSwimMode {
                PHYSICAL,
                TELEPORT
        }

        public static final ModConfigSpec SPEC;
        public static final MomentumConfig INSTANCE;

        static {
                Pair<MomentumConfig, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(MomentumConfig::new);
                SPEC = pair.getRight();
                INSTANCE = pair.getLeft();
        }

        // ==================== 旅者附魔设置 ====================
        public final ModConfigSpec.DoubleValue baseSpeedCap;
        public final ModConfigSpec.DoubleValue xpSpeedMultiplier;
        public final ModConfigSpec.DoubleValue enchantmentLevelMultiplier;
        public final ModConfigSpec.ConfigValue<List<? extends String>> blockSpeedCaps;

        // ==================== 铜铁轨设置 ====================
        public final ModConfigSpec.DoubleValue copperRailSpeed;
        public final ModConfigSpec.DoubleValue exposedCopperRailSpeed;
        public final ModConfigSpec.DoubleValue weatheredCopperRailSpeed;
        public final ModConfigSpec.DoubleValue oxidizedCopperRailSpeed;
        public final ModConfigSpec.DoubleValue maxAscendingSpeed;

        // ==================== 苦力怕设置 ====================
        public final ModConfigSpec.BooleanValue enableCreeperScaling;
        public final ModConfigSpec.DoubleValue maxHealthMultiplier;
        public final ModConfigSpec.DoubleValue minHealthMultiplier;

        // ==================== 马匹设置 ====================
        public final ModConfigSpec.BooleanValue enableHorseLeafPassthrough;
        public final ModConfigSpec.IntValue horseLeafGracePeriod;
        public final ModConfigSpec.BooleanValue enableHorseSwim;
        public final ModConfigSpec.DoubleValue horseSwimUpwardDrift;
        public final ModConfigSpec.DoubleValue horseSwimHorizontalMultiplier;
        public final ModConfigSpec.BooleanValue horseSwimDeepWaterCheck;
        public final ModConfigSpec.EnumValue<HorseSwimMode> horseSwimMode;

        public final ModConfigSpec.BooleanValue enableHorseTransparency;
        public final ModConfigSpec.DoubleValue horseTransparencyFadeStart;
        public final ModConfigSpec.DoubleValue horseTransparencyFadeEnd;
        public final ModConfigSpec.DoubleValue horseMinOpacity;

        public final ModConfigSpec.BooleanValue enableGoatHornSummon;
        public final ModConfigSpec.BooleanValue enableGoatHornGlint;
        public final ModConfigSpec.BooleanValue enableGoatHornSummonParticles;
        public final ModConfigSpec.DoubleValue goatHornSummonParticleRadius;
        public final ModConfigSpec.IntValue goatHornSummonParticleDelay;
        public final ModConfigSpec.DoubleValue goatHornSummonMaxDistance;

        // ==================== 船只旗帜设置 ====================
        public final ModConfigSpec.BooleanValue enableBoatBanner;
        public final ModConfigSpec.DoubleValue boatBannerSpeedMultiplier;
        public final ModConfigSpec.DoubleValue boatBannerScale;
        public final ModConfigSpec.DoubleValue boatBannerTranslationX;
        public final ModConfigSpec.DoubleValue boatBannerTranslationY;
        public final ModConfigSpec.DoubleValue boatBannerTranslationZ;
        public final ModConfigSpec.DoubleValue boatBannerRotationX;
        public final ModConfigSpec.DoubleValue boatBannerRotationY;
        public final ModConfigSpec.DoubleValue boatBannerRotationZ;
        public final ModConfigSpec.DoubleValue goatHornSummonTeleportDistance;

        // ==================== 末影珍珠传送设置 ====================
        public final ModConfigSpec.BooleanValue enableMountedPearlTeleport;
        public final ModConfigSpec.IntValue mountedPearlSyncTicks;

        MomentumConfig(ModConfigSpec.Builder builder) {
                // Traveler Settings
                builder.comment("Settings for the Traveler Enchantment").push("traveler");

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

                // Copper Rail Settings
                builder.comment("Settings for Copper Rails").push("copper_rails");

                copperRailSpeed = builder
                                .comment("Max speed for Copper Rails (Unaffected). Default: 0.8")
                                .comment("铜铁轨（未氧化）的最大速度。默认值：0.8")
                                .translation("wanderlusts_way_to_survive.config.copper_rails.copperSpeed")
                                .defineInRange("copperRailSpeed", 0.8, 0.0, 5.0);

                exposedCopperRailSpeed = builder
                                .comment("Max speed for Exposed Copper Rails. Default: 0.6")
                                .comment("斑驳铜铁轨的最大速度。默认值：0.6")
                                .translation("wanderlusts_way_to_survive.config.copper_rails.exposedCopperRailSpeed")
                                .defineInRange("exposedCopperRailSpeed", 0.6, 0.0, 5.0);

                weatheredCopperRailSpeed = builder
                                .comment("Max speed for Weathered Copper Rails. Default: 0.3")
                                .comment("锈蚀铜铁轨的最大速度。默认值：0.3")
                                .translation("wanderlusts_way_to_survive.config.copper_rails.weatheredCopperRailSpeed")
                                .defineInRange("weatheredCopperRailSpeed", 0.3, 0.0, 5.0);

                oxidizedCopperRailSpeed = builder
                                .comment("Max speed for Oxidized Copper Rails. Default: 0.2")
                                .comment("氧化铜铁轨的最大速度。默认值：0.2")
                                .translation("wanderlusts_way_to_survive.config.copper_rails.oxidizedCopperRailSpeed")
                                .defineInRange("oxidizedCopperRailSpeed", 0.2, 0.0, 5.0);

                maxAscendingSpeed = builder
                                .comment("Max speed when ascending (climbing slopes). Default: 0.5")
                                .comment("爬坡时的最大速度。默认值：0.5")
                                .translation("wanderlusts_way_to_survive.config.copper_rails.maxAscendingSpeed")
                                .defineInRange("maxAscendingSpeed", 0.5, 0.0, 5.0);

                builder.pop();

                // Creeper Settings
                builder.comment("Settings for Creeper Explosion Scaling").push("creeper");

                enableCreeperScaling = builder
                                .comment("Enable dynamic explosion scaling based on Creeper health.")
                                .comment("启用基于苦力怕生命值的动态爆炸缩放。")
                                .translation("wanderlusts_way_to_survive.config.creeper.enableCreeperScaling")
                                .define("enableCreeperScaling", true);

                maxHealthMultiplier = builder
                                .comment("Explosion multiplier when Creeper is at 100% health. Default: 0.5 (Weaker explosion)")
                                .comment("苦力怕满血时的爆炸倍率。默认值：0.5（较弱爆炸）")
                                .translation("wanderlusts_way_to_survive.config.creeper.maxHealthMultiplier")
                                .defineInRange("maxHealthMultiplier", 0.5, 0.0, 10.0);

                minHealthMultiplier = builder
                                .comment("Explosion multiplier when Creeper is near 0% health. Default: 2.5 (Stronger explosion)")
                                .comment("苦力怕接近死亡时的爆炸倍率。默认值：2.5（较强爆炸）")
                                .translation("wanderlusts_way_to_survive.config.creeper.minHealthMultiplier")
                                .defineInRange("minHealthMultiplier", 2.5, 0.0, 10.0);

                builder.pop();

                // Horse Settings
                builder.comment("Settings for Horse-related features").push("horse");

                enableHorseLeafPassthrough = builder
                                .comment("Allow horses to pass through leaves when standing on non-leaf blocks.")
                                .comment("允许马在踩着非树叶方块时穿过树叶。")
                                .translation("wanderlusts_way_to_survive.config.horse.enableHorseLeafPassthrough")
                                .define("enableHorseLeafPassthrough", true);

                horseLeafGracePeriod = builder
                                .comment("Grace period (in milliseconds) after leaving non-leaf ground during which leaves remain passable. Default: 500")
                                .comment("离开非树叶地面后的宽限期（毫秒），在此期间树叶仍可穿过。默认值：500")
                                .translation("wanderlusts_way_to_survive.config.horse.horseLeafGracePeriod")
                                .defineInRange("horseLeafGracePeriod", 500, 0, 5000);

                enableHorseSwim = builder
                                .comment("Allow mounted horses to swim in deep water instead of sinking.")
                                .comment("允许骑乘的马在深水中游泳，而不是下沉。")
                                .translation("wanderlusts_way_to_survive.config.horse.enableHorseSwim")
                                .define("enableHorseSwim", true);

                horseSwimUpwardDrift = builder
                                .comment("Upward drift speed when swimming (Y-axis movement per tick).")
                                .comment("游泳时的上浮速度（每 tick Y 轴移动量）。")
                                .translation("wanderlusts_way_to_survive.config.horse.horseSwimUpwardDrift")
                                .defineInRange("horseSwimUpwardDrift", 0.1, 0.0, 1.0);

                horseSwimHorizontalMultiplier = builder
                                .comment("Horizontal speed multiplier when swimming.")
                                .comment("游泳时的水平移动速度倍率。")
                                .translation("wanderlusts_way_to_survive.config.horse.horseSwimHorizontalMultiplier")
                                .defineInRange("horseSwimHorizontalMultiplier", 1.0, 0.0, 5.0);

                horseSwimDeepWaterCheck = builder
                                .comment("Only enable swimming in deep water (checks block below).")
                                .comment("仅在深水（检查脚下方块）时启用游泳。")
                                .translation("wanderlusts_way_to_survive.config.horse.horseSwimDeepWaterCheck")
                                .define("horseSwimDeepWaterCheck", true);

                horseSwimMode = builder
                                .comment("Swimming mode: PHYSICAL (Motion Mode - smooth physics) or TELEPORT (Fixed Height Mode - maintains position).")
                                .comment("游泳模式：PHYSICAL（运动模式 - 平滑物理）或 TELEPORT（定高模式 - 保持高度）。")
                                .translation("wanderlusts_way_to_survive.config.horse.horseSwimMode")
                                .defineEnum("horseSwimMode", HorseSwimMode.PHYSICAL);

                enableHorseTransparency = builder
                                .comment("Enable making the horse transparent when looking down while riding.")
                                .comment("启用骑乘时低头视角马匹透明化。")
                                .translation("wanderlusts_way_to_survive.config.horse.enableHorseTransparency")
                                .define("enableHorseTransparency", true);

                horseTransparencyFadeStart = builder
                                .comment("Pitch angle (degrees) to start fading the horse. (0 = horizontal, 90 = straight down)")
                                .comment("开始透明化的俯视角度。（0 = 水平，90 = 垂直向下）")
                                .translation("wanderlusts_way_to_survive.config.horse.horseTransparencyFadeStart")
                                .defineInRange("horseTransparencyFadeStart", 1.0, 0.0, 90.0);

                horseTransparencyFadeEnd = builder
                                .comment("Pitch angle (degrees) to reach minimum opacity. Must be greater than start angle.")
                                .comment("达到最小不透明度的俯视角度。必须大于开始角度。")
                                .translation("wanderlusts_way_to_survive.config.horse.horseTransparencyFadeEnd")
                                .defineInRange("horseTransparencyFadeEnd", 50.0, 0.0, 90.0);

                horseMinOpacity = builder
                                .comment("Minimum opacity of the horse when looking straight down. (0.0 = invisible, 1.0 = opaque)")
                                .comment("完全低头时的最小不透明度。（0.0 = 不可见，1.0 = 不透明）")
                                .translation("wanderlusts_way_to_survive.config.horse.horseMinOpacity")
                                .defineInRange("horseMinOpacity", 0.05, 0.0, 1.0);

                enableGoatHornSummon = builder
                                .comment("Enable binding Goat Horns to mounts and summoning them.")
                                .comment("启用山羊角绑定和召唤坐骑功能。")
                                .translation("wanderlusts_way_to_survive.config.horse.enableGoatHornSummon")
                                .define("enableGoatHornSummon", true);

                enableGoatHornGlint = builder
                                .comment("Enable enchantment glint for bound Goat Horns.")
                                .comment("启用已绑定山羊角的附魔光效。")
                                .translation("wanderlusts_way_to_survive.config.horse.enableGoatHornGlint")
                                .define("enableGoatHornGlint", true);

                enableGoatHornSummonParticles = builder
                                .comment("Enable particle effects (white smoke) when a mount is summoned.")
                                .comment("启用坐骑召唤时的粒子特效（白烟）。")
                                .translation("wanderlusts_way_to_survive.config.horse.enableGoatHornSummonParticles")
                                .define("enableGoatHornSummonParticles", true);

                goatHornSummonParticleRadius = builder
                                .comment("Radius of the particle circle when summoning a mount.")
                                .comment("召唤坐骑时粒子圆环的半径（格）。")
                                .translation("wanderlusts_way_to_survive.config.horse.goatHornSummonParticleRadius")
                                .defineInRange("goatHornSummonParticleRadius", 1.0, 0.1, 5.0);

                goatHornSummonParticleDelay = builder
                                .comment("Delay in ticks before spawning summon particles. 20 ticks = 1 second.")
                                .comment("召唤粒子特效的延迟时间（Tick）。20 Tick = 1 秒。")
                                .translation("wanderlusts_way_to_survive.config.horse.goatHornSummonParticleDelay")
                                .defineInRange("goatHornSummonParticleDelay", 5, 0, 100);

                goatHornSummonMaxDistance = builder
                                .comment("Maximum distance (in blocks) to summon a mount via Goat Horn. Only works for loaded chunks.")
                                .comment("山羊角召唤坐骑的最大距离（格）。仅对已加载的区块生效。")
                                .translation("wanderlusts_way_to_survive.config.horse.goatHornSummonMaxDistance")
                                .defineInRange("goatHornSummonMaxDistance", 128.0, 0.0, 10000.0);

                goatHornSummonTeleportDistance = builder
                                .comment("Distance from player to teleport the mount to. 0 = At player's position.")
                                .comment("召唤时坐骑传送到玩家前方的距离（格）。0 = 玩家当前位置。")
                                .translation("wanderlusts_way_to_survive.config.horse.goatHornSummonTeleportDistance")
                                .defineInRange("goatHornSummonTeleportDistance", 2.5, 0.0, 16.0);

                builder.pop();

                // Boat Banner Settings
                builder.comment("Settings for Boat Banner feature")
                                .translation("wanderlusts_way_to_survive.configuration.boat_banner")
                                .push("boat_banner");

                enableBoatBanner = builder
                                .comment("Allow placing banners on boats for decoration and speed boost.")
                                .comment("允许将旗帜放置在船上以进行装饰并获得速度提升。")
                                .translation("wanderlusts_way_to_survive.config.boat.enableBoatBanner")
                                .define("enableBoatBanner", true);

                boatBannerSpeedMultiplier = builder
                                .comment("Speed multiplier when a boat has a banner equipped. 1.0 = No boost.")
                                .comment("船只装备旗帜时的速度倍率。1.0 = 无加速。")
                                .translation("wanderlusts_way_to_survive.config.boat.boatBannerSpeedMultiplier")
                                .defineInRange("boatBannerSpeedMultiplier", 1.2, 1.0, 5.0);

                builder.comment("Rendering settings for boat banner")
                                .translation("wanderlusts_way_to_survive.configuration.boat_banner.rendering")
                                .push("rendering");

                boatBannerScale = builder
                                .comment("Scale of the banner on the boat.")
                                .comment("船上旗帜的缩放大小。")
                                .translation("wanderlusts_way_to_survive.config.boat.boatBannerScale")
                                .defineInRange("boatBannerScale", 0.75, 0.1, 5.0);

                boatBannerTranslationX = builder
                                .comment("X offset of the banner.")
                                .comment("旗帜的 X 轴偏移量。")
                                .translation("wanderlusts_way_to_survive.config.boat.boatBannerTranslationX")
                                .defineInRange("boatBannerTranslationX", 0.0, -10.0, 10.0);

                boatBannerTranslationY = builder
                                .comment("Y offset of the banner.")
                                .comment("旗帜的 Y 轴偏移量。")
                                .translation("wanderlusts_way_to_survive.config.boat.boatBannerTranslationY")
                                .defineInRange("boatBannerTranslationY", 0.4, -10.0, 10.0);

                boatBannerTranslationZ = builder
                                .comment("Z offset of the banner.")
                                .comment("旗帜的 Z 轴偏移量。")
                                .translation("wanderlusts_way_to_survive.config.boat.boatBannerTranslationZ")
                                .defineInRange("boatBannerTranslationZ", 0.8, -10.0, 10.0);

                boatBannerRotationX = builder
                                .comment("Rotation angles (degrees) around X axis.")
                                .comment("绕 X 轴旋转角度（度）。")
                                .translation("wanderlusts_way_to_survive.config.boat.boatBannerRotationX")
                                .defineInRange("boatBannerRotationX", -10.0, -180.0, 180.0);

                boatBannerRotationY = builder
                                .comment("Rotation angles (degrees) around Y axis.")
                                .comment("绕 Y 轴旋转角度（度）。")
                                .translation("wanderlusts_way_to_survive.config.boat.boatBannerRotationY")
                                .defineInRange("boatBannerRotationY", 180.0, -180.0, 180.0);

                boatBannerRotationZ = builder
                                .comment("Rotation angles (degrees) around Z axis.")
                                .comment("绕 Z 轴旋转角度（度）。")
                                .translation("wanderlusts_way_to_survive.config.boat.boatBannerRotationZ")
                                .defineInRange("boatBannerRotationZ", 0.0, -180.0, 180.0);

                builder.pop();

                builder.pop();

                // Mounted Pearl Settings
                builder.comment("Settings for Mounted Ender Pearl Teleportation")
                                .translation("wanderlusts_way_to_survive.configuration.mountedPearl")
                                .push("mountedPearl");

                enableMountedPearlTeleport = builder
                                .comment("Allow mounts to teleport with the player when using Ender Pearls.")
                                .comment("允许骑乘坐骑时使用末影珍珠传送，坐骑会跟随一起传送。")
                                .translation("wanderlusts_way_to_survive.config.mountedPearl.enableMountedPearlTeleport")
                                .define("enableMountedPearlTeleport", true);

                mountedPearlSyncTicks = builder
                                .comment("Number of ticks to synchronize mount position before player remounts. Default: 3")
                                .comment("玩家重新骑乘前同步坐骑位置的tick数。默认值：3")
                                .translation("wanderlusts_way_to_survive.config.mountedPearl.mountedPearlSyncTicks")
                                .defineInRange("mountedPearlSyncTicks", 3, 1, 20);

                builder.pop();
        }

        /**
         * 验证方块速度条目格式
         * 格式：'modid:blockid,上限值'
         */
        private static boolean validateBlockSpeedEntry(Object entry) {
                if (!(entry instanceof String str))
                        return false;
                String[] parts = str.split(",");
                if (parts.length != 2)
                        return false;
                // 检查方块ID格式（必须包含冒号）
                if (!parts[0].contains(":"))
                        return false;
                // 检查上限值是否为数字
                try {
                        Double.parseDouble(parts[1].trim());
                        return true;
                } catch (NumberFormatException e) {
                        return false;
                }
        }
}
