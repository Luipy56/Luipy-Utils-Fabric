package com.luipy.utilsmod.client.highlight;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * Unbaked wrapper that bakes a vanilla base model together with a shared emphasis overlay model.
 */
final class HighlightCompositeUnbakedModel implements UnbakedModel {
	private final UnbakedModel base;
	private final UnbakedModel overlay;

	HighlightCompositeUnbakedModel(UnbakedModel base, UnbakedModel overlay) {
		this.base = base;
		this.overlay = overlay;
	}

	@Override
	public Collection<ResourceLocation> getDependencies() {
		List<ResourceLocation> dependencies = new ArrayList<>(base.getDependencies());
		for (ResourceLocation dependency : overlay.getDependencies()) {
			if (!dependencies.contains(dependency)) {
				dependencies.add(dependency);
			}
		}
		return dependencies;
	}

	@Override
	public void resolveParents(Function<ResourceLocation, UnbakedModel> modelLoader) {
		base.resolveParents(modelLoader);
		overlay.resolveParents(modelLoader);
	}

	@Nullable
	@Override
	public BakedModel bake(
		ModelBaker baker,
		Function<Material, TextureAtlasSprite> spriteGetter,
		ModelState modelState,
		ResourceLocation modelId
	) {
		BakedModel baseBaked = base.bake(baker, spriteGetter, modelState, modelId);
		BakedModel overlayBaked = overlay.bake(baker, spriteGetter, modelState, modelId);
		if (baseBaked == null) {
			return overlayBaked;
		}
		if (overlayBaked == null) {
			return baseBaked;
		}
		return new HighlightCompositeBakedModel(baseBaked, overlayBaked);
	}
}
