package mod.azure.azurelib.network;

import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import mod.azure.azurelib.AzureLib;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class Networking {

    public static final Marker MARKER = MarkerManager.getMarker("Network");
    private static final String NETWORK_VERSION = "2.0.0";
    private static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(AzureLib.MOD_ID, "network_channel"))
            .networkProtocolVersion(() -> NETWORK_VERSION)
            .clientAcceptedVersions(NETWORK_VERSION::equals)
            .serverAcceptedVersions(NETWORK_VERSION::equals)
            .simpleChannel();

    public static void sendClientPacket(ServerPlayer target, IPacket<?> packet) {
        CHANNEL.sendTo(packet, target.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static final class PacketRegistry {

        private static int packetIndex;

        public static void register() {
            registerNetworkPacket(S2C_SendConfigData.class);
        }

        private static <P extends IPacket<P>> void registerNetworkPacket(Class<P> packetType) {
            P packet;
            try {
                packet = packetType.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new ReportedException(CrashReport.forThrowable(e, "Couldn't instantiate packet for registration. Make sure you have provided public constructor with no parameters."));
            }
            CHANNEL.registerMessage(packetIndex++, packetType, IPacket::encode, packet::decode, IPacket::handle);
        }
    }
}
