package com.luipy.utilsmod.client.highlight.pack;

import com.luipy.utilsmod.client.highlight.HighlightEmphasisTextures;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.Nullable;

/**
 * Serves the user highlight PNG in place of the bundled block texture when active.
 */
public final class HighlightCustomTexturePack implements PackResources {
	private final Path textureFile;

	public HighlightCustomTexturePack(Path textureFile) {
		this.textureFile = textureFile;
	}

	@Override
	public @Nullable IoSupplier<InputStream> getRootResource(String... path) {
		return null;
	}

	@Override
	public @Nullable IoSupplier<InputStream> getResource(PackType type, ResourceLocation location) {
		if (type != PackType.CLIENT_RESOURCES || !HighlightEmphasisTextures.BUNDLED_TEXTURE.equals(location)) {
			return null;
		}
		if (!Files.isRegularFile(textureFile)) {
			return null;
		}
		return () -> Files.newInputStream(textureFile);
	}

	@Override
	public void listResources(PackType type, String namespace, String path, ResourceOutput output) {
		if (type != PackType.CLIENT_RESOURCES || !"luipy-utils-mod".equals(namespace)) {
			return;
		}
		if ("textures/block".equals(path) && Files.isRegularFile(textureFile)) {
			output.accept(HighlightEmphasisTextures.BUNDLED_TEXTURE, () -> Files.newInputStream(textureFile));
		}
	}

	@Override
	public Set<String> getNamespaces(PackType type) {
		return type == PackType.CLIENT_RESOURCES ? Set.of("luipy-utils-mod") : Set.of();
	}

	@Override
	public void close() {
	}

	@Override
	public String packId() {
		return "luipy-utils-mod/custom_highlight_texture";
	}

	@Override
	public <T> @Nullable T getMetadataSection(MetadataSectionSerializer<T> serializer) throws IOException {
		return null;
	}
}
