package com.zeno.wanderlustswaytosurvive.server;

import com.zeno.wanderlustswaytosurvive.config.MomentumConfig;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.UUID;

import com.zeno.wanderlustswaytosurvive.WanderlustsWayToSurvive;

@EventBusSubscriber(modid = WanderlustsWayToSurvive.MOD_ID)
public class GoatHornSummonHandler {

    private static final String TAG_MOUNT_UUID = "WanderlustMountUUID";
    private static final String TAG_MOUNT_NAME = "WanderlustMountName";

    // 1. 绑定坐骑：手持山羊角右键实体
    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (!MomentumConfig.INSTANCE.enableGoatHornSummon.get())
            return;

        Player player = event.getEntity();
        Entity target = event.getTarget();
        ItemStack stack = event.getItemStack();

        // 检查物品是否为山羊角
        if (!stack.is(net.minecraft.world.item.Items.GOAT_HORN))
            return;

        // 检查目标是否为马类坐骑 (AbstractHorse 涵盖 Horse, Donkey, Mule, Llama, Camel 等)
        if (!(target instanceof AbstractHorse abstractHorse))
            return;

        // 必须是驯服的且属于该玩家
        // AbstractHorse 不继承 TamableAnimal，需单独检查
        if (!abstractHorse.isTamed() || !player.getUUID().equals(abstractHorse.getOwnerUUID())) {
            if (!event.getLevel().isClientSide) {
                player.displayClientMessage(Component.translatable("message.wanderlusts_way_to_survive.horn.not_owner"),
                        true);
            }
            return;
        }

        // 绑定逻辑
        if (!event.getLevel().isClientSide) {
            UUID mountUUID = abstractHorse.getUUID();
            String mountName = abstractHorse.getDisplayName().getString();

            // 写入数据到 CustomData
            CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
                tag.putUUID(TAG_MOUNT_UUID, mountUUID);
                tag.putString(TAG_MOUNT_NAME, mountName);
            });

            // 播放音效提示
            event.getLevel().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.PLAYERS, 1.0f, 1.0f);

            // 发送消息
            player.displayClientMessage(
                    Component.translatable("message.wanderlusts_way_to_survive.horn.bound", mountName), true);
        }

        // 阻止默认交互（防止骑乘或打开UI），确保存入手中的是山羊角
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }

    // 2. 召唤坐骑：右键使用山羊角 (当吹响时触发)
    // 使用 RightClickItem 事件来拦截，或者监听 ItemUseTick?
    // 原版山羊角使用时会触发 RightClickItem。
    // 但是我们希望在吹响的时候不仅播放声音，还执行召唤。
    // 如果我们在 RightClickItem 中执行召唤，那是开始吹的瞬间。
    // 用户体验上，开始吹的瞬间召唤是可以的。

    @SubscribeEvent
    public static void onItemUse(PlayerInteractEvent.RightClickItem event) {
        if (!MomentumConfig.INSTANCE.enableGoatHornSummon.get())
            return;

        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();

        if (!stack.is(net.minecraft.world.item.Items.GOAT_HORN))
            return;
        if (event.getLevel().isClientSide)
            return;

        // 读取数据
        CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
        if (customData == null || !customData.contains(TAG_MOUNT_UUID))
            return; // 未绑定的号角不做处理，走原版逻辑

        try {
            // 正确读取 NBT
            net.minecraft.nbt.CompoundTag tag = customData.copyTag();
            UUID uuid = tag.getUUID(TAG_MOUNT_UUID);
            String name = tag.getString(TAG_MOUNT_NAME);

            ServerLevel level = (ServerLevel) event.getLevel();
            Entity entity = level.getEntity(uuid);

            double maxDistance = MomentumConfig.INSTANCE.goatHornSummonMaxDistance.get();

            if (entity instanceof LivingEntity mount) {
                // 检查距离
                if (mount.distanceTo(player) > maxDistance) {
                    player.displayClientMessage(
                            Component.translatable("message.wanderlusts_way_to_survive.horn.not_found"), true);
                } else {
                    // 传送
                    // 寻找安全落脚点？简单处理：传送到玩家位置
                    mount.teleportTo(player.getX(), player.getY(), player.getZ());

                    player.displayClientMessage(
                            Component.translatable("message.wanderlusts_way_to_survive.horn.summoned", name), true);
                }
            } else {
                player.displayClientMessage(Component.translatable("message.wanderlusts_way_to_survive.horn.not_found"),
                        true);
            }

        } catch (Exception e) {
            // 数据损坏或读取失败
        }

        // 我们不取消事件，让原版号角继续吹响
    }
}
