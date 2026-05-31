package com.luipy.utilsmod.network;

import com.luipy.utilsmod.LuipyUtilsMod;
import com.luipy.utilsmod.server.UnifiedMenuOpeners;
import com.luipy.utilsmod.server.ShulkerBoxOpener;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

public final class LuipyNetworking {
	public static final ResourceLocation C2S_OPEN_UNIFIED_MENU = LuipyUtilsMod.id("c2s_open_unified_menu");
	public static final ResourceLocation C2S_OPEN_SHULKER = LuipyUtilsMod.id("c2s_open_shulker");
	public static final ResourceLocation S2C_SERVER_PRESENT = LuipyUtilsMod.id("s2c_server_present");

	private LuipyNetworking() {
	}

	public static void registerServer() {
		ServerPlayNetworking.registerGlobalReceiver(C2S_OPEN_UNIFIED_MENU, LuipyNetworking::handleOpenUnifiedMenu);
		ServerPlayNetworking.registerGlobalReceiver(C2S_OPEN_SHULKER, LuipyNetworking::handleOpenShulker);
		ServerPlayConnectionEvents.JOIN.register(LuipyNetworking::onJoin);
	}

	private static void onJoin(ServerGamePacketListenerImpl handler, PacketSender sender, MinecraftServer server) {
		ServerPlayer player = handler.player;
		server.execute(() -> ServerPlayNetworking.send(player, S2C_SERVER_PRESENT, PacketByteBufs.empty()));
	}

	private static void handleOpenUnifiedMenu(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
		server.execute(() -> UnifiedMenuOpeners.tryOpenFor(player));
	}

	private static void handleOpenShulker(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
		int slotIndex = buf.readInt();
		server.execute(() -> ShulkerBoxOpener.tryOpenFor(player, slotIndex));
	}
}
