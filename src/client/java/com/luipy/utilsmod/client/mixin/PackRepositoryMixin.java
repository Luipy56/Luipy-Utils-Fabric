package com.luipy.utilsmod.client.mixin;

import com.luipy.utilsmod.client.highlight.HighlightEmphasisTextures;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.PackRepository;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PackRepository.class)
public abstract class PackRepositoryMixin {
	@Inject(method = "openAllSelected", at = @At("RETURN"), cancellable = true)
	private void luipy$injectCustomHighlightPack(CallbackInfoReturnable<List<PackResources>> cir) {
		if (!HighlightEmphasisTextures.isCustomActive()) {
			return;
		}
		List<PackResources> packs = new ArrayList<>(cir.getReturnValue());
		packs.add(0, HighlightEmphasisTextures.createPack());
		cir.setReturnValue(packs);
	}
}
