package net.darkhax.redshark;

import java.io.File;
import java.io.FileWriter;

import org.apache.commons.lang3.SystemUtils;

import gnu.trove.map.hash.TByteObjectHashMap;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraftforge.fml.common.network.FMLIndexedMessageToMessageCodec;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleIndexedCodec;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

@Sharable
public class WrappedChannel extends SimpleChannelInboundHandler<FMLProxyPacket> {

    private final TByteObjectHashMap<Class<?>> discriminators;

    public WrappedChannel (SimpleIndexedCodec codec) {

        this.discriminators = ReflectionHelper.getPrivateValue(FMLIndexedMessageToMessageCodec.class, codec, "discriminators");
    }

    @Override
    protected void channelRead0 (ChannelHandlerContext ctx, FMLProxyPacket msg) throws Exception {

        try {

            try (FileWriter writer = new FileWriter(new File("packets.log"), true)) {

                final byte[] bytes = ByteBufUtil.getBytes(msg.payload());

                final String line = String.format("Recieved a packet from %s. It has %d bytes of data. Type is %s.", msg.channel(), bytes.length, this.discriminators.get(bytes[0]));

                writer.write(line + SystemUtils.LINE_SEPARATOR);
            }
        }

        catch (final Exception e) {

            RedShark.log.catching(e);
        }

        ctx.fireChannelRead(msg);
    }
}