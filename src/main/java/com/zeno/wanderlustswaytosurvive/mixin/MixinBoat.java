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

    @org.spongepowered.asm.mixin.Shadow
    public abstract Boat.Status getStatus();

    // 注入 tick 方法来在水下提供速度加成 (解决硬编码摩擦力问题)
    @Inject(method = "tick", at = @At("TAIL"))
    private void tick(CallbackInfo ci) {
        if (!MomentumConfig.INSTANCE.enableBoatBanner.get())
            return;

        ItemStack banner = this.entityData.get(BANNER_ITEM);
        if (banner.isEmpty())
            return;

        Boat.Status status = this.getStatus();

        // 仅在水中生效 (硬编码 0.9F 的地方)
        if (status == Boat.Status.IN_WATER || status == Boat.Status.UNDER_FLOWING_WATER) {
            double multiplier = MomentumConfig.INSTANCE.boatBannerSpeedMultiplier.get();
            if (multiplier > 1.0) {
                // 原本逻辑: newVel = oldVel * 0.9.
                double boost = (1.0 - (0.1 / multiplier)) / 0.9;

                net.minecraft.world.phys.Vec3 delta = this.getDeltaMovement();
                this.setDeltaMovement(delta.multiply(boost, 1.0, boost));
            }
        }
    }

    // 提升地面/冰面摩擦力 (保持速度) - 保留作为陆地/滑冰优化
    @Inject(method = "getGroundFriction", at = @At("RETURN"), cancellable = true)
    private void getGroundFriction(CallbackInfoReturnable<Float> cir) {
        if (MomentumConfig.INSTANCE.enableBoatBanner.get() && !this.entityData.get(BANNER_ITEM).isEmpty()) {
            // 不再调用 getStatus() 以避免 StackOverflowError (getStatus 内部会调用 getGroundFriction)
            // 假设 getGroundFriction 主要影响陆地/滑冰逻辑。
            float original = cir.getReturnValue();
            double multiplier = MomentumConfig.INSTANCE.boatBannerSpeedMultiplier.get();
            if (multiplier > 1.0) {
                float boost = (float) ((multiplier - 1.0) * 0.05f);
                float newFriction = Math.min(0.99f, original + boost);
                cir.setReturnValue(newFriction);
            }
        }
    }
}
