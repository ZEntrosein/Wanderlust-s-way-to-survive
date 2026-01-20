package com.zeno.wanderlustswaytosurvive.mixin;

import com.zeno.wanderlustswaytosurvive.config.MomentumConfig;
import com.zeno.wanderlustswaytosurvive.mixinterface.IBannerBoat;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Boat.class)
public abstract class MixinBoat extends Entity implements IBannerBoat {

    public MixinBoat(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Unique
    private static final EntityDataAccessor<ItemStack> BANNER_ITEM = SynchedEntityData.defineId(MixinBoat.class,
            EntityDataSerializers.ITEM_STACK);

    @Inject(method = "defineSynchedData", at = @At("TAIL"))
    private void defineSynchedData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(BANNER_ITEM, ItemStack.EMPTY);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void addAdditionalSaveData(net.minecraft.nbt.CompoundTag compound, CallbackInfo ci) {
        ItemStack banner = this.entityData.get(BANNER_ITEM);
        if (!banner.isEmpty()) {
            compound.put("WanderlustBanner", banner.saveOptional(this.registryAccess()));
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void readAdditionalSaveData(net.minecraft.nbt.CompoundTag compound, CallbackInfo ci) {
        if (compound.contains("WanderlustBanner")) {
            this.entityData.set(BANNER_ITEM,
                    ItemStack.parseOptional(this.registryAccess(), compound.getCompound("WanderlustBanner")));
        }
    }

    @Override
    public void wanderlustswaytosurvive$setBanner(ItemStack banner) {
        this.entityData.set(BANNER_ITEM, banner);
    }

    @Override
    public ItemStack wanderlustswaytosurvive$getBanner() {
        return this.entityData.get(BANNER_ITEM);
    }

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void interact(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (!MomentumConfig.INSTANCE.enableBoatBanner.get())
            return;

        ItemStack stack = player.getItemInHand(hand);
        ItemStack currentBanner = this.entityData.get(BANNER_ITEM);

        // 放置旗帜
        if (currentBanner.isEmpty() && stack.is(ItemTags.BANNERS)) {
            if (!this.level().isClientSide) {
                this.wanderlustswaytosurvive$setBanner(stack.copyWithCount(1));
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                this.gameEvent(GameEvent.BLOCK_CHANGE, player);
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        net.minecraft.sounds.SoundEvents.WOOD_PLACE, net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F,
                        1.0F);
            }
            cir.setReturnValue(InteractionResult.sidedSuccess(this.level().isClientSide));
            return;
        }

        // 取下旗帜 (潜行 + 右键)
        if (!currentBanner.isEmpty() && player.isShiftKeyDown() && stack.isEmpty()) {
            if (!this.level().isClientSide) {
                this.spawnAtLocation(currentBanner);
                this.wanderlustswaytosurvive$setBanner(ItemStack.EMPTY);
                this.gameEvent(GameEvent.BLOCK_CHANGE, player);
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        net.minecraft.sounds.SoundEvents.WOOD_BREAK, net.minecraft.sounds.SoundSource.NEUTRAL, 1.0F,
                        1.0F);
            }
            cir.setReturnValue(InteractionResult.sidedSuccess(this.level().isClientSide));
            return;
        }
    }

    // 注入 hurt 方法来处理掉落
    // @Inject(method = "hurt", at = @At("RETURN"))
    // private void onDropBoat(net.minecraft.world.damagesource.DamageSource source,
    // float amount,
    // CallbackInfoReturnable<Boolean> cir) {
    // if (!this.level().isClientSide && this.isRemoved()) {
    // ItemStack banner = this.entityData.get(BANNER_ITEM);
    // if (!banner.isEmpty()) {
    // this.spawnAtLocation(banner);
    // this.wanderlustswaytosurvive$setBanner(ItemStack.EMPTY);
    // }
    // }
    // }

    // 提升水面摩擦力 (保持速度)
    @Inject(method = "getGroundFriction", at = @At("RETURN"), cancellable = true)
    private void getGroundFriction(CallbackInfoReturnable<Float> cir) {
        if (MomentumConfig.INSTANCE.enableBoatBanner.get() && !this.entityData.get(BANNER_ITEM).isEmpty()) {
            // 原始值通常是 0.9 (Status.IN_WATER)
            float original = cir.getReturnValue();

            // 增加摩擦力系数，使其更接近 1.0，从而减少减速效果
            // 使用配置倍率来计算提升量?
            double multiplier = MomentumConfig.INSTANCE.boatBannerSpeedMultiplier.get();
            // 假设 mult = 1.2. 我们希望速度保持能力提升。
            // 简单的数学公式：newFriction = original + (1 - original) * (multiplier - 1) * 0.5?
            // 或者直接加一点点?

            // 如果 multiplier = 1.0, 保持不变
            // 如果 multiplier = 1.2, 提升一点
            if (multiplier > 1.0) {
                float boost = (float) ((multiplier - 1.0) * 0.05f); // 微调系数，避免太快
                float newFriction = Math.min(0.99f, original + boost);
                cir.setReturnValue(newFriction);
            }
        }
    }
}
