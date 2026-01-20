package com.zeno.wanderlustswaytosurvive.handler;

import com.zeno.wanderlustswaytosurvive.WanderlustsWayToSurvive;
import com.zeno.wanderlustswaytosurvive.config.MomentumConfig;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * 马匹游泳处理器
 * 允许骑乘马匹在深水中游泳，防止玩家被迫下马
 */
@EventBusSubscriber(modid = WanderlustsWayToSurvive.MOD_ID)
public class HorseSwimHandler {

    /**
     * 实体 tick 事件处理
     * 检测马匹是否在深水中且被骑乘，如果是则给予向上的移动力
     */
    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Pre event) {
        // 检查配置是否启用
        if (!MomentumConfig.INSTANCE.enableHorseSwim.get()) {
            return;
        }

        // 检查是否是马类实体
        if (!(event.getEntity() instanceof AbstractHorse horse)) {
            return;
        }

        // 检查是否被骑乘且在水中
        boolean isRidden = !horse.getPassengers().isEmpty();
        boolean isInWater = horse.isInWater();

        if (isRidden && isInWater) {
            // 深水检测 (可选)
            if (MomentumConfig.INSTANCE.horseSwimDeepWaterCheck.get()) {
                boolean isDeepWater = horse.level().isWaterAt(horse.blockPosition().below());
                if (!isDeepWater) {
                    return;
                }
            }

            // 应用上浮速度
            double upwardDrift = MomentumConfig.INSTANCE.horseSwimUpwardDrift.get();
            if (upwardDrift > 0) {
                MomentumConfig.HorseSwimMode mode = MomentumConfig.INSTANCE.horseSwimMode.get();
                if (mode == MomentumConfig.HorseSwimMode.TELEPORT) {
                    // 传送模式：强制向上位移
                    // 必须重置垂直速度，否则重力累积会抵消传送效果
                    horse.setDeltaMovement(horse.getDeltaMovement().multiply(1, 0, 1));
                    horse.teleportTo(horse.getX(), horse.getY() + upwardDrift, horse.getZ());
                } else {
                    // 物理模式：施加向上速度（默认）
                    horse.move(MoverType.PLAYER, new Vec3(0.0d, upwardDrift, 0.0d));
                }
            }

            // 应用水平速度倍率
            double horizontalMultiplier = MomentumConfig.INSTANCE.horseSwimHorizontalMultiplier.get();
            if (horizontalMultiplier != 1.0) {
                Vec3 motion = horse.getDeltaMovement();
                horse.setDeltaMovement(motion.x * horizontalMultiplier, motion.y, motion.z * horizontalMultiplier);
            }
        }
    }
}
