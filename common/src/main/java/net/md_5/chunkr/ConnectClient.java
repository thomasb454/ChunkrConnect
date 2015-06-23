package net.md_5.chunkr;

import com.google.common.base.Preconditions;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.md_5.chunkr.hook.HookListener;
import net.md_5.chunkr.hook.HookManager;
import net.md_5.chunkr.packet.AbstractPacket;
import net.md_5.chunkr.packet.EchoPacket;
import net.md_5.chunkr.packet.ForwardPacket;
import net.md_5.chunkr.packet.HandshakePacket;
import net.md_5.chunkr.pipeline.PipelineFactory;

@RequiredArgsConstructor
public class ConnectClient extends HookListener
{

    private final EventLoopGroup group = new NioEventLoopGroup();
    //
    private final String host;
    private final String password;
    private final UUID uuid;
    private final String[] tags;
    //
    private Channel channel;
    private ClientHandler baseHandler = new ClientHandler( this );
    @Getter
    private HookManager hookManager = new HookManager();
    private boolean gracefulDisconnect;

    
    {
        hookManager.addListener( this );
    }

    public void connect()
    {
        ChannelFuture future = new Bootstrap().channel( NioSocketChannel.class ).group( group )
                .handler( new PipelineFactory( baseHandler ) ).remoteAddress( Util.parseHostPort( host ) )
                .connect();
        channel = future.channel();

        future.addListener( new ChannelFutureListener()
        {

            @Override
            public void operationComplete(ChannelFuture future) throws Exception
            {
                if ( !future.isSuccess() )
                {
                    System.out.println( "Failed connect, retrying in 10 seconds!" );
                    group.schedule( new Runnable()
                    {

                        @Override
                        public void run()
                        {
                            connect();
                        }
                    }, 10, TimeUnit.SECONDS );
                    return;
                }
                System.out.println( "Attempting to handshake with " + future.channel() );
                send( new HandshakePacket( password, uuid, tags ) );
            }
        } );

        group.scheduleWithFixedDelay(new Runnable()
        {

            @Override
            public void run()
            {
                if ( channel.isActive() )
                {
                    channel.writeAndFlush( new EchoPacket() );
                }
            }
        }, 15, 15, TimeUnit.SECONDS );
    }

    /*========================================================================*/
    /**
     * Forcibly disconnect this client: n.b in many cases the connection will
     * automatically be re-established.
     */
    public void disconnect()
    {
        if ( channel.isOpen() )
        {
            channel.close();
            gracefulDisconnect = true;
        }
    }

    @Override
    public void disconnected(ConnectClient client)
    {
        if ( !gracefulDisconnect )
        {
            System.out.println( "Lost connection, reconnecting in 10 seconds" );
            group.schedule( new Runnable()
            {

                @Override
                public void run()
                {
                    connect();
                }
            }, 10, TimeUnit.SECONDS );
            return;
        }
    }

    public void kill()
    {
        group.shutdownGracefully();
        try
        {
            group.awaitTermination( 30, TimeUnit.SECONDS );
        } catch ( InterruptedException ex )
        {
        }
        System.out.println( "Waiting 30 seconds for graceful shutdown" );
    }

    /**
     * Sends the specified packet to the other endpoint of this client. If this
     * is a local client, it will be sent to the server, if this is a remote
     * client it will be sent to the client.
     *
     * @param packet the packet to send
     */
    public void send(AbstractPacket packet)
    {
        Preconditions.checkArgument( packet != null, "packet is null" );
        Preconditions.checkState( channel.isActive(), "channel is closed" );

        channel.writeAndFlush( packet );
    }

    /**
     * Attempts to send a packet to a single target.
     *
     * @param target the target to send to
     * @param packet the packet to send
     */
    public void sendUnicast(UUID target, AbstractPacket packet)
    {
        Preconditions.checkArgument( target != null, "target is null" );
        Preconditions.checkArgument( packet != null, "packet is null" );
        Preconditions.checkState( channel.isActive(), "channel is closed" );

        channel.writeAndFlush( new ForwardPacket( ForwardPacket.Style.UNICAST, target.toString(), packet ) );
    }

    /**
     * Attempts to send a packet to all targets matching the specified tag.
     *
     * @param tag the tag to send to
     * @param packet to send
     */
    public void sendMulticast(String tag, AbstractPacket packet)
    {
        Preconditions.checkArgument( tag != null, "tag is null" );
        Preconditions.checkArgument( packet != null, "packet is null" );
        Preconditions.checkState( channel.isActive(), "channel is closed" );

        channel.writeAndFlush( new ForwardPacket( ForwardPacket.Style.MULTICAST, tag, packet ) );
    }

    /**
     * Sends a broadcast to every other connected node in the network.
     *
     * @param packet the packet to send
     */
    public void sendBroadcast(AbstractPacket packet)
    {
        Preconditions.checkArgument( packet != null, "packet is null" );
        Preconditions.checkState( channel.isActive(), "channel is closed" );

        channel.writeAndFlush( new ForwardPacket( ForwardPacket.Style.BROADCAST, null, packet ) );
    }
}
