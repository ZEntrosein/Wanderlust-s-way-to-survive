package com.zeno.wanderlustswaytosurvive.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zeno.wanderlustswaytosurvive.mixinterface.IBannerBoat;
import com.zeno.wanderlustswaytosurvive.config.MomentumConfig;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BoatRenderer.class)
public abstract class MixinBoatRenderer extends EntityRenderer<Boat> {

    protected MixinBoatRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void render(Boat boat, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer,
            int packedLight, CallbackInfo ci) {
        if (!MomentumConfig.INSTANCE.enableBoatBanner.get())
            return;

        if (boat instanceof IBannerBoat bannerBoat) {
            ItemStack banner = bannerBoat.wanderlustswaytosurvive$getBanner();
            if (!banner.isEmpty()) {
                poseStack.pushPose();

                // 调整位置到船尾
                // 船的模型原点通常在中心底部
                // 向后移动 (Z轴负方向? 正方向?)
                // Boat Model local coord: Z is length?
                // 需反复调试位置。假设船长约 1.5 - 2 格。
                // 默认尝试放置在后座位置。

                // 这里的变换是在 entityRender 的局部坐标系中
                // 此时已经应用了 entityYaw 旋转? 是的，render 方法头部已经处理了插值

                // 旋转修正: 船模型可能已经旋转了 180 度 (Minecraft船模型的 quirky)
                // BoatRenderer line: poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - f));
                // 如果我们是在 TAIL 注入，此时 PoseStack 已经 pop 了模型旋转吗？
                // EntityRenderer.render 不需要 pop. BoatRenderer.render 内部 push/pop?
                // 查看 BoatRenderer.render 源码:
                // poseStack.pushPose();
                // poseStack.translate(0.0F, 0.375F, 0.0F);
                // poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - f));
                // ... model.renderToBuffer ...
                // poseStack.popPose();

                // 所以在 TAIL 注入时，PoseStack 已经回到了 entity 的原点 (只包含 entity 的全局位置插值，没有模型旋转和偏移)
                // 我们需要重新应用这些旋转和偏移，或者自己计算

                float f = entityYaw; // 插值后的 Yaw，传入参数
                // 手动重做 BoatRenderer 的变换
                poseStack.pushPose();
                poseStack.translate(0.0F, 0.375F, 0.0F);
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(180.0F - f));

                // --- Configurable Transformations ---
                // Translate
                double transX = MomentumConfig.INSTANCE.boatBannerTranslationX.get();
                double transY = MomentumConfig.INSTANCE.boatBannerTranslationY.get();
                double transZ = MomentumConfig.INSTANCE.boatBannerTranslationZ.get();
                poseStack.translate(transX, transY, transZ);

                // Scale
                float scale = MomentumConfig.INSTANCE.boatBannerScale.get().floatValue();
                poseStack.scale(scale, scale, scale);

                // Rotate
                // Config values are in degrees
                float rotX = MomentumConfig.INSTANCE.boatBannerRotationX.get().floatValue();
                float rotY = MomentumConfig.INSTANCE.boatBannerRotationY.get().floatValue();
                float rotZ = MomentumConfig.INSTANCE.boatBannerRotationZ.get().floatValue();

                poseStack.mulPose(com.mojang.math.Axis.XP.rotationDegrees(rotX));
                poseStack.mulPose(com.mojang.math.Axis.YP.rotationDegrees(rotY)); // Usually 180 to face backwards
                poseStack.mulPose(com.mojang.math.Axis.ZP.rotationDegrees(rotZ));

                // 渲染物品
                net.minecraft.client.Minecraft.getInstance().getItemRenderer().renderStatic(
                        banner,
                        ItemDisplayContext.FIXED, // 或者 GROUND? FIXED 通常是正立的
                        packedLight,
                        net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,
                        poseStack,
                        buffer,
                        boat.level(),
                        0);

                poseStack.popPose();
                poseStack.popPose(); // Pop the wrapper pose
            }
        }
    }
}
