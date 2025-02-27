package mod.azure.azurelib.network;

import java.util.Map;
import java.util.function.Supplier;

import mod.azure.azurelib.AzureLib;
import mod.azure.azurelib.config.ConfigHolder;
import mod.azure.azurelib.config.adapter.TypeAdapter;
import mod.azure.azurelib.config.value.ConfigValue;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class S2C_SendConfigData implements IPacket<S2C_SendConfigData> {

    private final String config;

    S2C_SendConfigData() {
        this.config = null;
    }

    public S2C_SendConfigData(String config) {
        this.config = config;
    }

    @Override
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(this.config);
        ConfigHolder.getConfig(this.config).ifPresent(data -> {
            Map<String, ConfigValue<?>> serialized = data.getNetworkSerializedFields();
            buffer.writeInt(serialized.size());
            for (Map.Entry<String, ConfigValue<?>> entry : serialized.entrySet()) {
                String id = entry.getKey();
                ConfigValue<?> value = entry.getValue();
                TypeAdapter adapter = value.getAdapter();
                buffer.writeUtf(id);
                adapter.encodeToBuffer(value, buffer);
            }
        });
    }

    @Override
    public S2C_SendConfigData decode(FriendlyByteBuf buffer) {
        String config = buffer.readUtf();
        int i = buffer.readInt();
        ConfigHolder.getConfig(config).ifPresent(data -> {
            Map<String, ConfigValue<?>> serialized = data.getNetworkSerializedFields();
            for (int j = 0; j < i; j++) {
                String fieldId = buffer.readUtf();
                ConfigValue<?> value = serialized.get(fieldId);
                if (value == null) {
                	AzureLib.LOGGER.fatal(Networking.MARKER, "Received unknown config value " + fieldId);
                    throw new RuntimeException("Unknown config field: " + fieldId);
                }
                setValue(value, buffer);
            }
        });
        return new S2C_SendConfigData(config);
    }

    @Override
    public void handle(Supplier<NetworkEvent.Context> supplier) {
        supplier.get().setPacketHandled(true);
    }

    @SuppressWarnings("unchecked")
    private <V> void setValue(ConfigValue<V> value, FriendlyByteBuf buffer) {
        TypeAdapter adapter = value.getAdapter();
        V v = (V) adapter.decodeFromBuffer(value, buffer);
        value.set(v);
    }
}
