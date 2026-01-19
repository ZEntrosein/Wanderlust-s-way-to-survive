package com.zeno.wanderlustswaytosurvive.registries;

import com.zeno.wanderlustswaytosurvive.WanderlustsWayToSurvive;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * 模组附魔注册类
 * 在1.21中，附魔是数据驱动的注册表，但代码中主要通过ResourceKey访问来进行逻辑检查。
 * 实际的附魔定义在JSON文件中（data/模组ID/enchantment/附魔名.json）。
 */
public class ModEnchantments {
    // 旅者附魔的ResourceKey - 用于在代码中引用附魔
    public static final ResourceKey<Enchantment> TREK = ResourceKey.create(
            Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(WanderlustsWayToSurvive.MOD_ID, "trek"));
}
