package com.luipy.utilsmod.config;

import com.google.gson.annotations.SerializedName;

/**
 * Persisted mod configuration (JSON). Loaded on both logical server and client.
 */
@SuppressWarnings("CanBeFinal")
public final class LuipyUtilsConfig {
	public boolean masterEnabled = true;
	/** When true, pressing inventory (E) opens survival inventory and ender chest together (requires mod on server). */
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
	/** When true, the combined inventory screen also shows a 3x3 crafting table panel. */
	public boolean showCraftingTableWithInventory = false;
}
