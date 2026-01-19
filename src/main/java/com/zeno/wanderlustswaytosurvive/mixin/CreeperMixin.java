package com.zeno.wanderlustswaytosurvive.mixin;

import com.zeno.wanderlustswaytosurvive.config.MomentumConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Creeper.class)
public abstract class CreeperMixin {

    @Shadow
    private int explosionRadius;

    // Use casting instead of Shadowing for inherited methods
    @Inject(method = "explodeCreeper", at = @At("HEAD"))
    private void DynamicExplosionScale(CallbackInfo ci) {
        if (!MomentumConfig.INSTANCE.enableCreeperScaling.get()) {
            return;
        }

        // Safe cast to LivingEntity to access health methods
        LivingEntity self = (LivingEntity) (Object) this;

        // Calculate health ratio (0.0 to 1.0)
        float currentHealth = self.getHealth();
        float maxHealth = self.getMaxHealth();
        // Safety check to avoid division by zero
        if (maxHealth <= 0)
            maxHealth = 1;

        float ratio = currentHealth / maxHealth;
        // Clamp ratio
        if (ratio < 0)
            ratio = 0;
        if (ratio > 1)
            ratio = 1;

        // Get config values
        double maxHpMult = MomentumConfig.INSTANCE.maxHealthMultiplier.get();
        double minHpMult = MomentumConfig.INSTANCE.minHealthMultiplier.get();

        // Linear interpolation
        // Multiplier = min + ratio * (max - min)
        // If ratio is 1.0 (Full HP), result is maxHpMult (e.g. 0.5)
        // If ratio is 0.0 (Dead), result is minHpMult (e.g. 2.5)
        double multiplier = minHpMult + (ratio * (maxHpMult - minHpMult));

        // Apply multiplier to radius
        // Default radius is 3, Power charged is *2 (done later in vanilla code)
        // We modify the base radius here.
        this.explosionRadius = (int) (this.explosionRadius * multiplier);

        // Ensure at least radius 1 so it still explodes
        if (this.explosionRadius < 0)
            this.explosionRadius = 0;
    }
}
