package net.alvisssss.advancedmissile.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class MissileReloadPacket {
    public static final Identifier ID = new Identifier("advancedmissile", "missile_reload");

    public static void send() {
        PacketByteBuf buf = PacketByteBufs.create();
        ClientPlayNetworking.send(MissileReloadPacket.ID, buf);
    }
}
