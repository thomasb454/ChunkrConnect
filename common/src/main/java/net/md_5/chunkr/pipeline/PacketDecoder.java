package net.md_5.chunkr.pipeline;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.io.IOException;
import java.util.List;
import net.md_5.chunkr.packet.AbstractPacket;

public class PacketDecoder extends MessageToMessageDecoder<ByteBuf>
{

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception
    {
        String packetClass = AbstractPacket.readString( in );
        AbstractPacket packet = AbstractPacket.newInstance( packetClass );
        if ( packet == null )
        {
            throw new IOException( "Unknown packet ID " + packetClass );
        }

        try
        {
            packet.read( in );
        } catch ( Throwable t )
        {
            t.printStackTrace();
        }
        out.add( packet );
    }
}
