package com.zeno.wanderlustswaytosurvive.handler;

import com.zeno.wanderlustswaytosurvive.WanderlustsWayToSurvive;
import com.zeno.wanderlustswaytosurvive.attachment.MountedPearlData;
import com.zeno.wanderlustswaytosurvive.config.MomentumConfig;
import com.zeno.wanderlustswaytosurvive.registries.ModAttachmentTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

/**
 * 骑乘末影珍珠传送处理器
 * 允许玩家在骑乘坐骑时使用末影珍珠，坐骑会跟随玩家一起传送
 * 
 * 实现原理：
 * 1. 在玩家右键使用末影珍珠时（PlayerInteractEvent.RightClickItem）记录骑乘状态
 * 2. 在传送事件触发时（EntityTeleportEvent.EnderPearl）使用记录的坐骑信息
 * 3. 通过 PlayerTickEvent 处理坐骑的同步传送和重新骑乘
 */
@EventBusSubscriber(modid = WanderlustsWayToSurvive.MOD_ID)
public class MountedPearlHandler {
    private static final Logger LOGGER = LogUtils.getLogger();

    /**
     * 玩家右键使用物品事件处理
     * 在玩家使用末影珍珠时记录骑乘状态
     */
    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();

        // 检查是否是末影珍珠
        if (!player.getItemInHand(event.getHand()).is(Items.ENDER_PEARL)) {
            return;
        }

        // 检查功能是否启用
        if (!MomentumConfig.INSTANCE.enableMountedPearlTeleport.get()) {
            return;
        }

        // 只在服务端处理
        if (player.level().isClientSide()) {
            return;
        }

        // 检查玩家是否正在骑乘
        Entity vehicle = player.getVehicle();
        if (vehicle == null) {
            return;
        }

        LOGGER.info("[骑乘传送] 玩家 {} 骑乘 {} 使用末影珍珠，记录坐骑状态",
                player.getName().getString(),
                vehicle.getType().getDescriptionId());

        // 记录坐骑信息到附件
        MountedPearlData data = player.getData(ModAttachmentTypes.MOUNTED_PEARL);
        data.setVehicleId(vehicle.getId());
        // 目标坐标将在传送事件中设置
        data.setTicksRemaining(-1); // -1 表示等待传送事件
    }

    /**
     * 末影珍珠传送事件处理
     * 设置目标坐标
     */
    @SubscribeEvent
    public static void onEnderPearlTeleport(EntityTeleportEvent.EnderPearl event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player player)) {
            return;
        }

        // 只在服务端处理
        if (player.level().isClientSide()) {
            return;
        }

        // 检查是否有待处理的骑乘传送
        MountedPearlData data = player.getData(ModAttachmentTypes.MOUNTED_PEARL);
        if (data.getVehicleId() <= 0 || data.getTicksRemaining() != -1) {
            return;
        }

        // 设置目标坐标和同步 tick 数
        data.setTargetPosition(event.getTargetX(), event.getTargetY(), event.getTargetZ());
        data.setTicksRemaining(MomentumConfig.INSTANCE.mountedPearlSyncTicks.get());

        // 取消传送伤害
        event.setAttackDamage(0);

        LOGGER.info("[骑乘传送] 传送事件触发，目标: ({}, {}, {})",
                event.getTargetX(), event.getTargetY(), event.getTargetZ());
    }

    /**
     * 玩家 tick 事件处理
     * 处理坐骑的延迟传送和重新骑乘
     */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        // 只在服务端处理
        if (player.level().isClientSide()) {
            return;
        }

        MountedPearlData data = player.getData(ModAttachmentTypes.MOUNTED_PEARL);

        // 检查是否有待传送的坐骑（ticksRemaining >= 0 表示传送事件已触发）
        if (!data.hasPendingVehicle() || data.getTicksRemaining() < 0) {
            return;
        }

        // 获取坐骑实体
        Entity vehicle = player.level().getEntity(data.getVehicleId());
        if (vehicle == null) {
            LOGGER.warn("[骑乘传送] 坐骑实体不存在，重置数据");
            data.reset();
            return;
        }

        int ticksRemaining = data.getTicksRemaining();

        if (ticksRemaining > 0) {
            // 倒计时中，持续将坐骑传送到目标位置
            vehicle.teleportTo(data.getTargetX(), data.getTargetY(), data.getTargetZ());
            data.setTicksRemaining(ticksRemaining - 1);
        } else {
            // 倒计时结束，让玩家重新骑上坐骑
            player.startRiding(vehicle, true);
            data.reset();

            LOGGER.info("[骑乘传送] 玩家 {} 重新骑上坐骑", player.getName().getString());
        }
    }
}
