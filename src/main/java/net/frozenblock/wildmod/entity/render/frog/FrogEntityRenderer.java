package net.frozenblock.wildmod.entity.render.frog;

import net.frozenblock.wildmod.WildMod;
import net.frozenblock.wildmod.WildModClient;
import net.frozenblock.wildmod.entity.FrogEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public class FrogEntityRenderer extends MobEntityRenderer<FrogEntity, FrogEntityModel> {

    private static final Identifier TEMPERATE_TEXTURE = new Identifier(WildMod.MOD_ID, "textures/entity/frog/temperate_frog.png");
    private static final Identifier COLD_TEXTURE = new Identifier(WildMod.MOD_ID, "textures/entity/frog/cold_frog.png");
    private static final Identifier WARM_TEXTURE = new Identifier(WildMod.MOD_ID, "textures/entity/frog/warm_frog.png");
    private static final Identifier SUS_TEXTURE = new Identifier(WildMod.MOD_ID, "textures/entity/frog/sus_frog.png");

    public FrogEntityRenderer(EntityRendererFactory.Context context) {
        super(context, new FrogEntityModel(context.getPart(WildModClient.MODEL_FROG_LAYER)), 0.5f);
    }

    @Override
    public Identifier getTexture(FrogEntity entity) {
        String string = Formatting.strip(entity.getName().getString());
        if ("Xfrtrex".equals(string)) {
            return SUS_TEXTURE;
        }
        if(entity.getVariant() == FrogEntity.Variant.WARM) {
            return WARM_TEXTURE;
        } else if(entity.getVariant() == FrogEntity.Variant.COLD) {
            return COLD_TEXTURE;
        } else {
            return TEMPERATE_TEXTURE;
        }
    }

    public void render(FrogEntity frogEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i) {
        super.render(frogEntity, f, g, matrixStack, vertexConsumerProvider, i);
    }



}