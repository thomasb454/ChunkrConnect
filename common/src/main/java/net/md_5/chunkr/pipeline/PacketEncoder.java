package net.md_5.chunkr.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import net.md_5.chunkr.packet.AbstractPacket;

public class PacketEncoder extends MessageToByteEncoder<AbstractPacket>
{

    @Override
    protected void encode(ChannelHandlerContext ctx, AbstractPacket msg, ByteBuf out) throws Exception
    {
        AbstractPacket.writeString( msg.getClass().getName(), out );
        msg.write( out );
    }
}
