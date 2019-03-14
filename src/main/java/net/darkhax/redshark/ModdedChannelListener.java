package net.darkhax.redshark;

import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import it.unimi.dsi.fastutil.bytes.Byte2ObjectMap;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import net.minecraftforge.fml.common.network.FMLIndexedMessageToMessageCodec;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleIndexedCodec;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Sharable
public class ModdedChannelListener extends SimpleChannelInboundHandler<FMLProxyPacket> {

    private static SimpleDateFormat timestamp = new SimpleDateFormat("HH:mm:ss");

    private final Byte2ObjectMap<Class<?>> discriminators;
    private final ByteArrayList hexdumps;

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public ModdedChannelListener(SimpleIndexedCodec codec) {
        this.discriminators = ReflectionHelper.getPrivateValue(FMLIndexedMessageToMessageCodec.class, codec, "discriminators");

        hexdumps = new ByteArrayList();

        for (Map.Entry<Byte, Class<?>> entry : discriminators.entrySet()) {
            for (String clazz : RedShark.hexdumpClasses) {
                if (!clazz.isEmpty() && entry.getValue().toString().contains(clazz)) {
                    hexdumps.add(entry.getKey());
                }
            }
        }
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 5];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 5] = ' ';
            hexChars[j * 5 + 1] = '0';
            hexChars[j * 5 + 2] = 'x';
            hexChars[j * 5 + 3] = hexArray[v >>> 4];
            hexChars[j * 5 + 4] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FMLProxyPacket msg) throws Exception {
        try {
            try (FileWriter writer = new FileWriter(new File(RedShark.filename), true)) {

                final byte[] bytes = ByteBufUtil.getBytes(msg.payload());

                Class clazz = this.discriminators.get(bytes[0]);

                Date now = new Date();

                String line = String.format("[%s] Recieved a packet from %s. It has %d bytes of data. Type is %s.", timestamp.format(now), msg.channel(), bytes.length, clazz);

                if (hexdumps.contains(bytes[0])) {
                    line = line + String.format(" Packet data:%s", bytesToHex(bytes));
                }
                writer.write(line + SystemUtils.LINE_SEPARATOR);
            }
        } catch (final Exception e) {
            RedShark.log.catching(e);
        }

        ctx.fireChannelRead(msg);
    }
}