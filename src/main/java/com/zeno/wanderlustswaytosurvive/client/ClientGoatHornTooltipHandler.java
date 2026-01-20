package com.zeno.wanderlustswaytosurvive.client;

import com.zeno.wanderlustswaytosurvive.WanderlustsWayToSurvive;
import com.zeno.wanderlustswaytosurvive.server.GoatHornSummonHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = WanderlustsWayToSurvive.MOD_ID, value = Dist.CLIENT)
public class ClientGoatHornTooltipHandler {

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        // 检查是否为山羊角
        if (!event.getItemStack().is(Items.GOAT_HORN))
            return;

        // 获取自定义数据
        CustomData customData = event.getItemStack().get(DataComponents.CUSTOM_DATA);
        if (customData != null && customData.contains(GoatHornSummonHandler.TAG_MOUNT_NAME)) {
            try {
                // 读取已绑定的坐骑名称
                String mountName = customData.copyTag().getString(GoatHornSummonHandler.TAG_MOUNT_NAME);

                // 添加 Tooltip (绿色/灰色?)
                event.getToolTip()
                        .add(Component.translatable("tooltip.wanderlusts_way_to_survive.horn.bound_to", mountName)
                                .withStyle(ChatFormatting.GRAY));
            } catch (Exception e) {
                // Ignore NBT errors in tooltip
            }
        }
    }
}
