package com.zeno.wanderlustswaytosurvive.handler;

import com.zeno.wanderlustswaytosurvive.WanderlustsWayToSurvive;
import com.zeno.wanderlustswaytosurvive.config.MomentumConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 骑马珍珠传送处理器
 */
@EventBusSubscriber(modid = WanderlustsWayToSurvive.MOD_ID)
public class EnderPearlTeleportHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnderPearlTeleportHandler.class);
    private static final Map<UUID, AbstractHorse> playerHorseMap = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onEnderPearlThrown(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }

        if (!(event.getEntity() instanceof ThrownEnderpearl pearl)) {
            return;
        }

        Entity owner = pearl.getOwner();
        if (!(owner instanceof Player player)) {
            return;
        }

        Entity vehicle = player.getVehicle();
        if (vehicle instanceof AbstractHorse horse) {
            playerHorseMap.put(player.getUUID(), horse);
        }
    }

    @SubscribeEvent
    public static void onEnderPearlTeleport(EntityTeleportEvent.EnderPearl event) {
        try {
            if (MomentumConfig.INSTANCE == null ||
                    !MomentumConfig.INSTANCE.enableHorseEnderPearlTeleport.get()) {
                return;
            }
        } catch (Exception e) {
            return;
        }

        Entity entity = event.getEntity();
        if (!(entity instanceof ServerPlayer player)) {
            return;
        }

        AbstractHorse horse = playerHorseMap.remove(player.getUUID());
        if (horse == null || !horse.isAlive()) {
            return;
        }

        // 取消原版传送
        event.setCanceled(true);

        double targetX = event.getTargetX();
        double targetY = event.getTargetY();
        double targetZ = event.getTargetZ();
        ServerLevel level = player.serverLevel();

        // 1. 让玩家下马
        player.stopRiding();

        // 2. 归零速度
        horse.setDeltaMovement(Vec3.ZERO);
        player.setDeltaMovement(Vec3.ZERO);

        // 3. 传送马
        horse.absMoveTo(targetX, targetY, targetZ, horse.getYRot(), horse.getXRot());
        horse.setOldPosAndRot();

        // 4. 发送显式的实体传送包给玩家，强制更新客户端已知的马的位置
        player.connection.send(new ClientboundTeleportEntityPacket(horse));

        // 5. 传送玩家
        player.connection.teleport(targetX, targetY, targetZ, player.getYRot(), player.getXRot());

        // 6. 延迟由于网络延迟，需要多等几tick让客户端接受马的新位置
        level.getServer().execute(() -> {
            // 延迟 5 ticks
            player.server.tell(new net.minecraft.server.TickTask(player.server.getTickCount() + 5, () -> {
                if (player.isAlive() && horse.isAlive() && !player.isPassenger()) {
                    player.startRiding(horse, true);
                    LOGGER.info("[PearlTP] Remounting after delay");
                }
            }));
        });

        // 音效和粒子
        level.playSound(null, targetX, targetY, targetZ,
                SoundEvents.PLAYER_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);

        level.sendParticles(ParticleTypes.PORTAL, targetX, targetY + 1, targetZ,
                32, 0.5, 0.5, 0.5, 0.1);

        player.hurt(player.damageSources().fall(), 5.0F);

        LOGGER.info("[PearlTP] Teleported with packet sync and 5-tick delay");
    }
}
