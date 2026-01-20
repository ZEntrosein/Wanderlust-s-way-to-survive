package com.zeno.wanderlustswaytosurvive.client;

import com.zeno.wanderlustswaytosurvive.config.MomentumConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class HorseTransparencyHandler {

    /**
     * 判断实体是否应该应用透明渲染
     */
    public static boolean shouldBeTransparent(LivingEntity entity) {
        if (!MomentumConfig.INSTANCE.enableHorseTransparency.get()) {
            return false;
        }

        Player player = Minecraft.getInstance().player;
        if (player == null || entity.getPassengers().isEmpty()) {
            return false;
        }

        // 检查玩家是否骑乘该实体
        if (!entity.getPassengers().contains(player)) {
            return false;
        }

        // 检查俯视角度是否超过开始阈值
        float pitch = player.getXRot();
        double start = MomentumConfig.INSTANCE.horseTransparencyFadeStart.get();
        return pitch > start;
    }

    /**
     * 获取实体的目标不透明度 (Alpha)
     */
    public static float getOpacity(LivingEntity entity) {
        if (!shouldBeTransparent(entity)) {
            return 1.0f;
        }

        Player player = Minecraft.getInstance().player;
        if (player == null)
            return 1.0f;

        float pitch = player.getXRot();
        float start = MomentumConfig.INSTANCE.horseTransparencyFadeStart.get().floatValue();
        float end = MomentumConfig.INSTANCE.horseTransparencyFadeEnd.get().floatValue();
        float minOpacity = MomentumConfig.INSTANCE.horseMinOpacity.get().floatValue();

        // 线性插值计算透明度
        // pitch: start -> end
        // opacity: 1.0 -> minOpacity

        if (pitch >= end)
            return minOpacity;
        if (pitch <= start)
            return 1.0f;

        // 防止除以零 (如果 start == end)
        if (Math.abs(end - start) < 0.001f)
            return minOpacity;

        float progress = (pitch - start) / (end - start);
        return 1.0f - progress * (1.0f - minOpacity);
    }
}
