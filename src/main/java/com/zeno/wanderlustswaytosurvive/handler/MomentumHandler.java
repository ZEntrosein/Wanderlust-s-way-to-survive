package com.zeno.wanderlustswaytosurvive.handler;

import com.zeno.wanderlustswaytosurvive.attachment.MomentumData;
import com.zeno.wanderlustswaytosurvive.config.MomentumConfig;
import com.zeno.wanderlustswaytosurvive.registries.ModAttachmentTypes;
import com.zeno.wanderlustswaytosurvive.registries.ModEnchantments;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

/**
 * 动量处理器 - 处理旅者附魔的速度累积和应用
 * 在服务端每tick执行，根据玩家脚下的方块类型累积速度加成
 */
@EventBusSubscriber
public class MomentumHandler {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static int debugTickCounter = 0;

    private static final UUID MOMENTUM_MODIFIER_ID = UUID.fromString("12345678-1234-1234-1234-1234567890ab");
    // 属性修改器的ResourceLocation（1.21中推荐使用RL而非UUID）
    private static final ResourceLocation MOMENTUM_MODIFIER_RL = ResourceLocation
            .fromNamespaceAndPath("wanderlustswaytosurvive", "momentum_bonus");

    /**
     * 玩家tick事件处理 - 每tick检查并更新速度加成
     */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide)
            return; // 只在服务端处理，属性会自动同步到客户端

        MomentumData data = player.getData(ModAttachmentTypes.MOMENTUM);

        // 1. 检查附魔（使用ItemEnchantments组件，支持数据包驱动的附魔）
        var boots = player.getItemBySlot(EquipmentSlot.FEET);
        if (boots.isEmpty()) {
            return;
        }

        // 直接检查靴子上是否有旅者（Trek）附魔
        int enchantmentLevel = getEnchantmentLevelByKey(boots, ModEnchantments.TREK);

        if (enchantmentLevel <= 0) {
            if (data.getCurrentSpeedBonus() > 0) {
                resetMomentum(player, data);
            }
            // 调试：靴子没有旅者附魔
            debugTickCounter++;
            if (debugTickCounter >= 20) {
                debugTickCounter = 0;
                LOGGER.info("[旅者调试] 玩家 {} 的靴子没有旅者附魔（等级：{}）",
                        player.getName().getString(), enchantmentLevel);
            }
            return;
        }

        // 2. 检查移动状态
        // 只在地面上奔跑时累积动量。在空中时保持动量（跳跃可无缝切换）
        // 注意：玩家停止移动时不重置 - 只在方块类型改变时重置
        // 这样可以在穿过草丛等减速方块时保持动量

        // 获取实际的地面方块（跳过草丛、花朵等非实心方块）
        Block blockBelow = getActualGroundBlock(player);

        boolean isOnGround = player.onGround();
        boolean isSprinting = player.isSprinting();

        // 检查玩家是否正在移动（用于累积，不用于重置）
        double horizontalSpeed = Math.sqrt(
                player.getDeltaMovement().x * player.getDeltaMovement().x +
                        player.getDeltaMovement().z * player.getDeltaMovement().z);
        boolean isMoving = horizontalSpeed > 0.001;

        // 调试：每秒输出一次方块检测信息
        debugTickCounter++;
        if (debugTickCounter >= 20) {
            Block lastBlock = data.getLastBlock();
            LOGGER.info(
                    "[旅者调试] 地面检测 - 当前方块: {}, 上一方块: {}, 在地面: {}, 移动中: {}, 速度: {}",
                    blockBelow != null ? BuiltInRegistries.BLOCK.getKey(blockBelow) : "null",
                    lastBlock != null ? BuiltInRegistries.BLOCK.getKey(lastBlock) : "null",
                    isOnGround,
                    isMoving,
                    String.format("%.4f", horizontalSpeed));
        }

        if (isOnGround && blockBelow != null) {
            // 检查方块类型是否改变
            if (blockBelow != data.getLastBlock()
                    && data.getLastBlock() != net.minecraft.world.level.block.Blocks.AIR) {
                // 方块改变了！重置动量
                LOGGER.info("[旅者调试] 重置：方块从 {} 变为 {}",
                        data.getLastBlock() != null ? BuiltInRegistries.BLOCK.getKey(data.getLastBlock()) : "null",
                        BuiltInRegistries.BLOCK.getKey(blockBelow));
                resetMomentum(player, data);
                data.setLastBlock(blockBelow);
            } else {
                // 同一方块类型或刚开始
                data.setLastBlock(blockBelow);

                // 累积速度
                accumulateMomentum(player, data, blockBelow, enchantmentLevel);
            }
        } else if (isOnGround && blockBelow == null) {
            // 在非实心方块上 - 保持当前状态，不重置
            if (debugTickCounter >= 20) {
                LOGGER.info("[旅者调试] 在非实心方块上，保持动量");
            }
        } else {
            // 在空中 - 保持动量（跳跃时无缝切换）
        }

        // 重置调试计数器
        if (debugTickCounter >= 20) {
            debugTickCounter = 0;
        }

        // 应用速度属性修改
        applySpeedModifier(player, data.getCurrentSpeedBonus());

        // 调试：每秒输出一次速度信息
        debugTickCounter++;
        if (debugTickCounter >= 20) {
            debugTickCounter = 0;
            LOGGER.info("[旅者调试] 玩家: {}, 速度加成: {}, 材质tick数: {}, 方块: {}",
                    player.getName().getString(),
                    String.format("%.4f", data.getCurrentSpeedBonus()),
                    data.getTicksOnMaterial(),
                    data.getLastBlock() != null ? BuiltInRegistries.BLOCK.getKey(data.getLastBlock()) : "null");
        }
    }

    /**
     * 累积动量 - 根据配置计算并增加速度加成
     * 公式：速度上限 = (基础上限或方块上限 + 经验加成) × (1 + 附魔等级 × 附魔倍率)
     * 加速率 = 速度上限 / 100（5秒达到上限）
     */
    private static void accumulateMomentum(Player player, MomentumData data, Block block, int enchantmentLevel) {
        data.setTicksOnMaterial(data.getTicksOnMaterial() + 1);

        // 计算速度上限
        double baseCap = MomentumConfig.INSTANCE.baseSpeedCap.get();
        double xpBonus = player.experienceLevel * MomentumConfig.INSTANCE.xpSpeedMultiplier.get();

        // 检查是否有方块特定的速度上限
        ResourceLocation blockRL = BuiltInRegistries.BLOCK.getKey(block);
        double blockCap = getAllConfigBlockCaps(blockRL);

        // 如果配置中有该方块的上限则使用，否则使用基础上限
        double capBaseValue = (blockCap >= 0) ? blockCap : baseCap;

        // 计算最终速度上限
        double totalCap = (capBaseValue + xpBonus)
                * (1 + (enchantmentLevel * MomentumConfig.INSTANCE.enchantmentLevelMultiplier.get()));

        // 加速率：5秒（100 ticks）达到上限
        float acceleration = (float) (totalCap / 100.0f);

        if (data.getCurrentSpeedBonus() < totalCap) {
            data.setCurrentSpeedBonus(Math.min((float) totalCap, data.getCurrentSpeedBonus() + acceleration));
        }
    }

    /**
     * 重置动量 - 将所有状态归零
     */
    private static void resetMomentum(Player player, MomentumData data) {
        data.setCurrentSpeedBonus(0);
        data.setTicksOnMaterial(0);
        data.setLastBlock(net.minecraft.world.level.block.Blocks.AIR);
        applySpeedModifier(player, 0);
    }

    /**
     * 应用速度修改器 - 将速度加成应用到玩家的移动速度属性上
     */
    private static void applySpeedModifier(Player player, float value) {
        AttributeInstance attribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attribute != null) {
            attribute.removeModifier(MOMENTUM_MODIFIER_RL);
            if (value > 0) {
                attribute.addTransientModifier(new AttributeModifier(
                        MOMENTUM_MODIFIER_RL,
                        value,
                        AttributeModifier.Operation.ADD_VALUE));
            }
        }
    }

    /**
     * 从配置中获取特定方块的速度上限
     * 配置格式："modid:block,上限值"
     * 返回 -1 表示未找到
     */
    private static double getAllConfigBlockCaps(ResourceLocation blockRL) {
        List<? extends String> caps = MomentumConfig.INSTANCE.blockSpeedCaps.get();
        for (String s : caps) {
            String[] parts = s.split(",");
            if (parts.length == 2) {
                if (parts[0].trim().equals(blockRL.toString())) {
                    try {
                        return Double.parseDouble(parts[1].trim());
                    } catch (NumberFormatException e) {
                        return -1;
                    }
                }
            }
        }
        return -1;
    }

    /**
     * 通过ResourceKey从物品栈获取附魔等级
     * 支持1.21+的数据包驱动附魔
     */
    private static int getEnchantmentLevelByKey(net.minecraft.world.item.ItemStack stack,
            ResourceKey<Enchantment> enchantmentKey) {
        if (stack.isEmpty())
            return 0;

        net.minecraft.world.item.enchantment.ItemEnchantments enchantments = stack.getOrDefault(
                net.minecraft.core.component.DataComponents.ENCHANTMENTS,
                net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY);

        // 调试：输出物品上的所有附魔
        if (!enchantments.isEmpty()) {
            LOGGER.info("[旅者调试] 检查靴子附魔: {}", enchantmentKey.location());
            for (var entry : enchantments.entrySet()) {
                var holder = entry.getKey();
                var keyOpt = holder.unwrapKey();
                if (keyOpt.isPresent()) {
                    LOGGER.info("[旅者调试]   发现附魔: {} (等级 {})",
                            keyOpt.get().location(), entry.getIntValue());
                    if (keyOpt.get().equals(enchantmentKey)) {
                        return entry.getIntValue();
                    }
                }
            }
        }
        return 0;
    }

    /**
     * 获取玩家脚下的实际实心方块
     * 跳过草丛、花朵等没有碰撞体积的方块
     * 如果在玩家下方2格内找不到实心方块则返回null
     */
    private static Block getActualGroundBlock(Player player) {
        net.minecraft.core.BlockPos playerPos = player.blockPosition();
        net.minecraft.world.level.Level level = player.level();

        // 从玩家脚部位置向下检查
        for (int y = 0; y >= -2; y--) {
            net.minecraft.core.BlockPos checkPos = playerPos.offset(0, y, 0);
            BlockState state = level.getBlockState(checkPos);
            Block block = state.getBlock();

            // 跳过空气
            if (state.isAir())
                continue;

            // 跳过没有碰撞体积的方块（草丛、花朵等）
            // 这些方块玩家可以直接穿过
            if (!state.getCollisionShape(level, checkPos).isEmpty()) {
                return block;
            }
        }

        return null;
    }
}
