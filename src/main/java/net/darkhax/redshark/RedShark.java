package net.darkhax.redshark;

import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.netty.channel.ChannelHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleIndexedCodec;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = "redshark", name = "Red Shark", version = "@VERSION@", acceptableRemoteVersions = "*", certificateFingerprint = "@FINGERPRINT@")
public class RedShark {

    public static final Logger log = LogManager.getLogger("Red Shark");

    @EventHandler
    public void loadComplete (FMLLoadCompleteEvent event) {

        final EnumMap<Side, Map<String, FMLEmbeddedChannel>> channels = ReflectionHelper.getPrivateValue(NetworkRegistry.class, NetworkRegistry.INSTANCE, "channels");

        for (final Entry<Side, Map<String, FMLEmbeddedChannel>> sidedChanelInfo : channels.entrySet()) {

            for (final Entry<String, FMLEmbeddedChannel> channel : sidedChanelInfo.getValue().entrySet()) {

                for (final Entry<String, ChannelHandler> handler : channel.getValue().pipeline()) {

                    if (handler.getValue() instanceof SimpleIndexedCodec) {

                        log.info("Inserting listener to {}'s packet channel.", channel.getKey());
                        channel.getValue().pipeline().addFirst(new WrappedChannel((SimpleIndexedCodec) handler.getValue()));
                        break;
                    }
                }
            }
        }
    }
}