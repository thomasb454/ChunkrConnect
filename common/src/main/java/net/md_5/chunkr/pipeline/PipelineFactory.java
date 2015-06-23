package net.md_5.chunkr.pipeline;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.RequiredArgsConstructor;
import net.md_5.chunkr.packet.AbstractPacket;

@RequiredArgsConstructor
public class PipelineFactory extends ChannelInitializer<Channel>
{

    private static final int FRAME_BYTES = 4;
    private final SimpleChannelInboundHandler<AbstractPacket> baseHandler;

    @Override
    protected void initChannel(Channel ch) throws Exception
    {
        ch.pipeline().addLast( new ReadTimeoutHandler( 30 ) );
        ch.pipeline().addLast( "length-decoder", new LengthFieldBasedFrameDecoder( Integer.MAX_VALUE, 0, FRAME_BYTES, 0, FRAME_BYTES ) );
        ch.pipeline().addLast( "packet-decoder", new PacketDecoder() );

        ch.pipeline().addLast( "length-encoder", new LengthFieldPrepender( FRAME_BYTES ) );
        ch.pipeline().addLast( "packet-encoder", new PacketEncoder() );
        ch.pipeline().addLast( "handler", baseHandler );
    }
}
