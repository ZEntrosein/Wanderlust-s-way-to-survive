package com.zeno.wanderlustswaytosurvive.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zeno.wanderlustswaytosurvive.client.HorseTransparencyHandler;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HorseArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.Horse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.item.AnimalArmorItem;

@Mixin(HorseArmorLayer.class)
public abstract class MixinHorseArmorLayer extends RenderLayer<Horse, HorseModel<Horse>> {

    @Shadow
    protected HorseModel<Horse> model;

    // 临时存储正在渲染的实体
    private Horse currentEntity;

    public MixinHorseArmorLayer(RenderLayerParent<Horse, HorseModel<Horse>> renderer) {
        super(renderer);
    }

    // 1. 捕获实体
    @Inject(method = "render", at = @At("HEAD"))
    private void captureEntity(PoseStack poseStack, MultiBufferSource buffer, int packedLight, Horse entity,
            float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw,
            float headPitch, CallbackInfo ci) {
        this.currentEntity = entity;
    }

    // 2. 拦截 getBuffer 调用，使用手动获取的纹理创建 Translucent RenderType
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MultiBufferSource;getBuffer(Lnet/minecraft/client/renderer/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;"))
    private VertexConsumer getArmorBuffer(MultiBufferSource buffer, RenderType originalRenderType) {
        Horse entity = this.currentEntity;
        if (entity != null && HorseTransparencyHandler.shouldBeTransparent(entity)) {
            // 手动获取马铠纹理
            net.minecraft.world.item.ItemStack itemStack = entity.getBodyArmorItem();
            if (itemStack.getItem() instanceof AnimalArmorItem armorItem) {
                ResourceLocation location = armorItem.getTexture();
                return buffer.getBuffer(RenderType.entityTranslucent(location));
            }
        }
        return buffer.getBuffer(originalRenderType);
    }

    // 4. 重定向 model.renderToBuffer 调用，修改 alpha 值
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/HorseModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V"))
    private void renderToBufferWithTransparency(
            HorseModel<Horse> instance,
            PoseStack poseStack,
            VertexConsumer vertexConsumer,
            int packedLight,
            int packedOverlay,
            int color) {
        Horse entity = this.currentEntity;

        // 计算新的 Alpha
        if (entity != null && HorseTransparencyHandler.shouldBeTransparent(entity)) {
            float opacity = HorseTransparencyHandler.getOpacity(entity);

            // 从 packed color 中提取原始 alpha (ARGB)
            int originalAlpha = (color >> 24) & 0xFF;

            // 计算新 alpha
            int newAlpha = (int) (originalAlpha * opacity);

            // 重组颜色 integer
            color = (newAlpha << 24) | (color & 0x00FFFFFF);
        }

        instance.renderToBuffer(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }
}
