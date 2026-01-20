package com.zeno.wanderlustswaytosurvive.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zeno.wanderlustswaytosurvive.client.HorseTransparencyHandler;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer<T extends LivingEntity, M extends EntityModel<T>>
        extends EntityRenderer<T> {

    @Shadow
    protected M model;

    // 临时存储正在渲染的实体，供 Redirect 使用
    private T currentEntity;

    protected MixinLivingEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    // 注入 render 方法头部，捕获 entity
    @Inject(method = "render", at = @At("HEAD"))
    private void captureEntity(T entity, float entityYaw, float partialTicks, PoseStack matrixStack,
            MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        this.currentEntity = entity;
    }

    // 注入 getRenderType，强制使用 Translucent
    @Inject(method = "getRenderType", at = @At("HEAD"), cancellable = true)
    private void getRenderTypeWithTransparency(T entity, boolean bodyVisible, boolean translucent, boolean glowing,
            CallbackInfoReturnable<RenderType> cir) {
        if (HorseTransparencyHandler.shouldBeTransparent(entity)) {
            // 强制返回半透明渲染类型
            cir.setReturnValue(RenderType.entityTranslucent(this.getTextureLocation(entity)));
        }
    }

    // 重定向 model.renderToBuffer 调用，修改 alpha 值
    @Redirect(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V"))
    private void renderToBufferWithTransparency(
            M instance,
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            int packedLight,
            int packedOverlay,
            int color) {
        T entity = this.currentEntity;

        // 计算新的 Alpha
        if (entity != null && HorseTransparencyHandler.shouldBeTransparent(entity)) {
            float opacity = HorseTransparencyHandler.getOpacity(entity);

            // 从 packed color 中提取原始 alpha (ARGB)
            int originalAlpha = (color >> 24) & 0xFF;

            // 计算新 alpha (原始 alpha * 透明度)
            int newAlpha = (int) (originalAlpha * opacity);

            // 重组颜色 integer: (newAlpha << 24) | (RGB components)
            color = (newAlpha << 24) | (color & 0x00FFFFFF);
        }

        instance.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
