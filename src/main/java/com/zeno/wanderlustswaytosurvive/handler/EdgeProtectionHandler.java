package com.zeno.wanderlustswaytosurvive.handler;

import com.zeno.wanderlustswaytosurvive.WanderlustsWayToSurvive;
import com.zeno.wanderlustswaytosurvive.config.MomentumConfig;
import com.zeno.wanderlustswaytosurvive.registries.ModEnchantments;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;

/**
 * 边缘保护处理器 - 客户端功能
 * 当玩家高速移动时，如果即将进入不同类型的方块，自动停止输入
 * 防止玩家因高速而失控冲入悬崖或其他区域
 */
@EventBusSubscriber(modid = WanderlustsWayToSurvive.MOD_ID, value = Dist.CLIENT)
public class EdgeProtectionHandler {

    /**
     * 输入更新事件 - 在客户端检查并可能取消移动输入
     */
    @SubscribeEvent
    public static void onInputUpdate(MovementInputUpdateEvent event) {
        if (!(event.getEntity() instanceof LocalPlayer player))
            return;

        // 1. 检查附魔
        // 注意：客户端的EnchantmentHelper应该可以工作（如果标签已同步）
        var momentumHolderInfo = player.registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getHolder(ModEnchantments.MOMENTUM);

        if (momentumHolderInfo.isEmpty())
            return;

        int enchantmentLevel = EnchantmentHelper.getItemEnchantmentLevel(
                momentumHolderInfo.get(),
                player.getItemBySlot(EquipmentSlot.FEET));

        if (enchantmentLevel <= 0)
            return;

        // 2. 检查条件：在地面、没有跳跃、正在疾跑
        // input.jumping为true表示正在按住空格键
        if (!player.onGround() || event.getInput().jumping || !player.isSprinting()) {
            return;
        }

        // 3. 检查速度阈值
        // 只有当速度足够高时才启用边缘保护
        double currentSpeed = player.getAttributeValue(Attributes.MOVEMENT_SPEED);
        // 默认玩家速度是0.1，疾跑会增加，动量会进一步增加
        // 设定阈值为0.2（大约是基础行走速度的两倍）
        double threshold = MomentumConfig.INSTANCE.baseSpeedCap.get() * 0.5 + 0.1; // 粗略估算

        if (currentSpeed < threshold)
            return;

        // 4. 预测移动方向
        // 检查输入的前进/左/右状态
        float forward = event.getInput().forwardImpulse;
        float strafe = event.getInput().leftImpulse;

        if (forward == 0 && strafe == 0)
            return;

        // 简单检查：查看玩家视角前方1格
        Vec3 lookAngle = player.getLookAngle();
        // 将视角向量投影到水平面
        lookAngle = new Vec3(lookAngle.x, 0, lookAngle.z).normalize();

        // 计算预测位置（相对于脚部）
        // 如果正在向前移动：
        double checkDist = 0.6; // 稍微超出碰撞箱
        Vec3 checkPos = player.position().add(lookAngle.scale(checkDist));

        BlockPos currentPos = player.blockPosition();
        BlockPos nextPos = BlockPos.containing(checkPos);

        if (currentPos.equals(nextPos))
            return; // 仍在同一方块内

        BlockState currentState = player.level().getBlockState(currentPos);
        BlockState nextState = player.level().getBlockState(nextPos);

        // 如果当前方块和下一方块类型不同，则触发边缘保护
        // "边缘保护"的目的是防止玩家因高速而误入不同区域
        if (currentState.getBlock() != nextState.getBlock()) {
            // 方块类型不同！停止输入
            event.getInput().forwardImpulse = 0;
            event.getInput().leftImpulse = 0;

            // 可以添加轻微的后退推力或视觉震动效果
            // 目前只是停止输入
        }
    }
}
