package com.luipy.utilsmod.config;

import com.google.gson.annotations.SerializedName;

/**
 * Persisted mod configuration (JSON). Loaded on both logical server and client.
 */
@SuppressWarnings("CanBeFinal")
public final class LuipyUtilsConfig {
	public boolean masterEnabled = true;
	/** When true, the unified menu (Alt+L) includes a 3×9 ender chest panel (requires mod on server). */
	@SerializedName(value = "showEnderChestWithInventory", alternate = { "replaceInventoryWithEnderChest" })
	public boolean showEnderChestWithInventory = true;
	/** When true, item/block gates below are ignored for opening (server still validates). */
	public boolean alwaysAllowVirtualOpen = false;
	/** When {@link #alwaysAllowVirtualOpen} is false: require holding an ender chest item (OR with block). */
	public boolean requireEnderChestItem = true;
	/** When {@link #alwaysAllowVirtualOpen} is false: require a loaded ender chest block nearby (OR with item). */
	public boolean requireNearbyEnderChestBlock = true;
	public boolean showToastsOnFailure = true;
	/** When true, hovering an enchantment table option shows ALL enchantments that will be applied. */
	public boolean showEnchantmentPreview = true;
	/** When true, shift+right-clicking a shulker box in your inventory opens it as a container. */
	public boolean allowOpenShulkerFromInventory = true;
	/** When true, the unified menu (Alt+L) includes a 3×3 crafting table panel. */
	public boolean showCraftingTableWithInventory = false;
	/** When true, matching blocks from {@link #blockHighlightIds} are outlined in the world (client-only). */
	public boolean blockHighlightEnabled = false;
	/** Comma+space separated block resource ids, e.g. {@code redstone_ore, gravel, calcite}. */
	public String blockHighlightIds = "";
}
