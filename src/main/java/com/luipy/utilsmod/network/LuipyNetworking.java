package com.luipy.utilsmod.network;

import com.luipy.utilsmod.LuipyUtilsMod;
import com.luipy.utilsmod.server.EnderChestOpeners;
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
	public static final ResourceLocation C2S_OPEN_ENDER = LuipyUtilsMod.id("c2s_open_ender");
	public static final ResourceLocation S2C_SERVER_PRESENT = LuipyUtilsMod.id("s2c_server_present");

	private LuipyNetworking() {
	}

	public static void registerServer() {
		ServerPlayNetworking.registerGlobalReceiver(C2S_OPEN_ENDER, LuipyNetworking::handleOpenEnder);
		ServerPlayConnectionEvents.JOIN.register(LuipyNetworking::onJoin);
	}

	private static void onJoin(ServerGamePacketListenerImpl handler, PacketSender sender, MinecraftServer server) {
		ServerPlayer player = handler.player;
		server.execute(() -> ServerPlayNetworking.send(player, S2C_SERVER_PRESENT, PacketByteBufs.empty()));
	}

	private static void handleOpenEnder(MinecraftServer server, ServerPlayer player, ServerGamePacketListenerImpl handler, FriendlyByteBuf buf, PacketSender responseSender) {
		server.execute(() -> EnderChestOpeners.tryOpenFor(player));
	}
}
