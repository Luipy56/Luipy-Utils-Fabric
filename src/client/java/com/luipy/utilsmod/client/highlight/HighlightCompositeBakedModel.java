package com.luipy.utilsmod.client.highlight;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Baked model that draws the vanilla block geometry first, then an emphasis overlay on top.
 * Transparent pixels in the overlay texture let the base block show through the center.
 */
final class HighlightCompositeBakedModel implements BakedModel {
	private final BakedModel base;
	private final BakedModel overlay;

	HighlightCompositeBakedModel(BakedModel base, BakedModel overlay) {
		this.base = base;
		this.overlay = overlay;
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
