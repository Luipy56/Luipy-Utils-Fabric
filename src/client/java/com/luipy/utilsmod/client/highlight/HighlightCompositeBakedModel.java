package com.luipy.utilsmod.client.highlight;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Baked model that draws the vanilla block geometry first, then an emphasis overlay on top.
 * Transparent pixels in the overlay texture let the base block show through the center.
 */
final class HighlightCompositeBakedModel implements BakedModel, FabricBakedModel {
	private final BakedModel base;
	private final BakedModel overlay;
	private final RenderMaterial overlayMaterial;

	HighlightCompositeBakedModel(BakedModel base, BakedModel overlay) {
		this.base = base;
		this.overlay = overlay;
		var renderer = RendererAccess.INSTANCE.getRenderer();
		this.overlayMaterial = renderer != null
			? renderer.materialFinder().blendMode(BlendMode.TRANSLUCENT).ambientOcclusion(TriState.FALSE).find()
			: null;
	}

	@Override
	public boolean isVanillaAdapter() {
		return false;
	}

	@Override
	public void emitBlockQuads(
		BlockAndTintGetter blockView,
		BlockState state,
		BlockPos pos,
		Supplier<RandomSource> randomSupplier,
		RenderContext context
	) {
		context.bakedModelConsumer().accept(base, state);
		if (overlayMaterial != null) {
			context.pushTransform(quad -> {
				quad.material(overlayMaterial);
				return true;
			});
			try {
				context.bakedModelConsumer().accept(overlay, state);
			} finally {
				context.popTransform();
			}
		} else {
			context.bakedModelConsumer().accept(overlay, state);
		}
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, RandomSource random) {
		List<BakedQuad> quads = new ArrayList<>(base.getQuads(state, face, random));
		quads.addAll(overlay.getQuads(state, face, random));
		return quads;
	}

	@Override
	public boolean useAmbientOcclusion() {
		return base.useAmbientOcclusion();
	}

	@Override
	public boolean isGui3d() {
		return base.isGui3d();
	}

	@Override
	public boolean usesBlockLight() {
		return base.usesBlockLight();
	}

	@Override
	public boolean isCustomRenderer() {
		return base.isCustomRenderer();
	}

	@Override
	public TextureAtlasSprite getParticleIcon() {
		return base.getParticleIcon();
	}

	@Override
	public ItemTransforms getTransforms() {
		return base.getTransforms();
	}

	@Override
	public ItemOverrides getOverrides() {
		return base.getOverrides();
	}
}
