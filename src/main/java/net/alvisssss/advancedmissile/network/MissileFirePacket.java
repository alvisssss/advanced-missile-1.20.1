package net.alvisssss.advancedmissile.network;

import net.alvisssss.advancedmissile.item.custom.CommandLaunchUnitItem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import static net.alvisssss.advancedmissile.item.custom.CommandLaunchUnitItem.MISSILE_SPEED;

public class MissileFirePacket {
    public static final Identifier ID = new Identifier("advancedmissile", "missile_fire");

    public static void send() {
        PacketByteBuf buf = PacketByteBufs.create();
        ClientPlayNetworking.send(MissileFirePacket.ID, buf);
    }
}
