package com.zeno.wanderlustswaytosurvive.mixin;

import com.zeno.wanderlustswaytosurvive.config.MomentumConfig;
import com.zeno.wanderlustswaytosurvive.server.GoatHornSummonHandler;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class MixinItem {

    @Inject(method = "isFoil", at = @At("HEAD"), cancellable = true)
    private void isFoil(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!MomentumConfig.INSTANCE.enableGoatHornGlint.get())
            return;

        // 检查是否为山羊角
        if (stack.is(Items.GOAT_HORN)) {
            CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
            // 检查是否有 UUID 标签
            if (customData != null && customData.contains(GoatHornSummonHandler.TAG_MOUNT_UUID)) {
                cir.setReturnValue(true);
            }
        }
    }
}
