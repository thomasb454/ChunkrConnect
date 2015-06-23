package net.md_5.chunkr;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import net.md_5.chunkr.packet.AbstractPacket;
import net.md_5.chunkr.packet.ErrorPacket;

@RequiredArgsConstructor
@ChannelHandler.Sharable
public class ClientHandler extends SimpleChannelInboundHandler<AbstractPacket>
{

    private final ConnectClient parent;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception
    {
        System.out.println( "Channel has become active " + ctx );
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception
    {
        System.out.println( "Channel has become inactive " + ctx );
        parent.getHookManager().fireDisconnect( parent );
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        System.out.println( "Exception caught, disconnecting channel " + ctx );
        cause.printStackTrace();
        ctx.channel().close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractPacket msg) throws Exception
    {
        if ( msg instanceof ErrorPacket )
        {
            ErrorPacket error = (ErrorPacket) msg;
            if ( "OK".equals( error.getMessage() ) )
            {
                // We are actually ok!
                parent.getHookManager().fireConnect( parent );
            } else
            {
                System.out.println( "Got remote error: " + error.getMessage() );
            }
        } else
        {
            parent.getHookManager().firePacketReceived( parent, msg );
        }
    }
}
