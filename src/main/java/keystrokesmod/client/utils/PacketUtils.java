package keystrokesmod.client.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;

import java.util.ArrayList;
import java.util.List;

public class PacketUtils {

    public static final List<Packet<?>> silentPackets = new ArrayList<>();

    public static void sendPacket(Packet<?> packet) {
        Minecraft.getMinecraft().getNetHandler().addToSendQueue(packet);
    }

    public static void sendPacketSilent(Packet<?> packet) {
        silentPackets.add(packet);
        Minecraft.getMinecraft().getNetHandler().addToSendQueue(packet);
    }

}
