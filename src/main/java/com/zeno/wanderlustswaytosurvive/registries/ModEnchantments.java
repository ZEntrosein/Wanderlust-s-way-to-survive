package com.zeno.wanderlustswaytosurvive.registries;

import com.zeno.wanderlustswaytosurvive.WanderlustsWayToSurvive;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

public class ModEnchantments {
    // In 1.21, Enchantments are data-driven regular registries, but primarily
    // accessed via ResourceKey in code for logic checks.
    // The actual definition is in JSON (data generation).

    public static final ResourceKey<Enchantment> MOMENTUM = ResourceKey.create(
            Registries.ENCHANTMENT,
            ResourceLocation.fromNamespaceAndPath(WanderlustsWayToSurvive.MOD_ID, "momentum"));
}
