package com.zeno.wanderlustswaytosurvive.mixin;

import com.zeno.wanderlustswaytosurvive.mixinterface.IBannerBoat;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class MixinEntityBoatDrop {

    @Inject(method = "remove", at = @At("HEAD"))
    private void onRemove(Entity.RemovalReason reason, CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        // Check if it is a boat and is being killed (destroyed)
        if (self instanceof Boat && reason == Entity.RemovalReason.KILLED && !self.level().isClientSide) {
            if (self instanceof IBannerBoat bannerBoat) {
                ItemStack banner = bannerBoat.wanderlustswaytosurvive$getBanner();
                if (!banner.isEmpty()) {
                    self.spawnAtLocation(banner);
                    bannerBoat.wanderlustswaytosurvive$setBanner(ItemStack.EMPTY);
                }
            }
        }
    }
}
