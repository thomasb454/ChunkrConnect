package net.md_5.chunkr;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.ByteStreams;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.AttributeKey;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import jline.console.ConsoleReader;
import jline.internal.Log;
import lombok.Getter;
import net.md_5.bungee.log.BungeeLogger;
import net.md_5.bungee.log.LoggingOutputStream;
import net.md_5.chunkr.packet.AbstractPacket;
import net.md_5.chunkr.packet.EchoPacket;
import net.md_5.chunkr.packet.ErrorPacket;
import net.md_5.chunkr.packet.ForwardPacket;
import net.md_5.chunkr.packet.HandshakePacket;
import net.md_5.chunkr.pipeline.PipelineFactory;
import net.md_5.chunkr.plugin.Plugin;
import net.md_5.chunkr.plugin.PluginManager;
import org.fusesource.jansi.AnsiConsole;

@ChannelHandler.Sharable
public class ChunkrServer extends SimpleChannelInboundHandler<AbstractPacket>
{

    private final Map<UUID, RemoteClient> remoteConnections = new ConcurrentHashMap<>();
    private final Multimap<String, RemoteClient> remoteTags = HashMultimap.create();
    private static final AttributeKey<RemoteClient> UUID_ATTR = AttributeKey.valueOf( "UUID" );
    private EventLoopGroup group = new NioEventLoopGroup();
    private Channel channel;
    @Getter
    private PluginManager pluginManager = new PluginManager( this );
    private String password;
    /*========================================================================*/
    public static ConsoleReader consoleReader;
    @Getter
    private static ChunkrServer instance;
    private static Logger logger;
    private static boolean isRunning = true;

    /*========================================================================*/
    public ChunkrServer() throws Exception
    {
        File pluginsFolder = new File( "plugins" );
        if ( !pluginsFolder.exists() )
        {
            pluginsFolder.mkdir();
        }
        getLogger().log( Level.INFO, "Loading plugins" );
        pluginManager.loadPlugins( pluginsFolder );

        Properties props = new Properties();
        props.putAll( new HashMap<String, String>()
        {
            {
                put( "host", "0.0.0.0:" + Util.DEFAULT_PORT );
                put( "password", "qwertyuiop" );
            }
        } );

        File propsFile = new File( "chunkr.properties" );
        if ( propsFile.isFile() )
        {
            try ( FileReader fr = new FileReader( propsFile ) )
            {
                props.load( fr );
            }
        }
        try ( FileWriter fw = new FileWriter( propsFile ) )
        {
            props.store( fw, "Chunkr Props file, written " + new Date() );
        }
        getLogger().log( Level.INFO, "Loaded properties {0}", props );

        bind( props.getProperty( "host" ) );
        password = props.getProperty( "password" );

        getLogger().log( Level.INFO, "Enabling plugins" );
        pluginManager.enablePlugins();
    }

    public void bind(String host)
    {
        channel = new ServerBootstrap().channel( NioServerSocketChannel.class ).
                childHandler( new PipelineFactory( this ) ).group( group ).
                localAddress( Util.parseHostPort( host ) ).bind().syncUninterruptibly().channel();
        getLogger().log( Level.INFO, "Bound channel {0}", channel );
    }

    public RemoteClient getClient(UUID uuid)
    {
        return remoteConnections.get( uuid );
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception
    {
        getLogger().log( Level.INFO, "Got remote connection from address {0}", ctx.channel().remoteAddress() );
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception
    {
        getLogger().log( Level.INFO, "Lost connection from address {0}", ctx.channel().remoteAddress() );

        RemoteClient client = ctx.attr( UUID_ATTR ).get();
        if ( client != null )
        {
            remoteConnections.remove( client.getUuid() );
            for ( String s : client.getTags() )
            {
                remoteTags.remove( s, client );
            }
            // Event
            for ( Plugin p : pluginManager.getPlugins() )
            {
                p.clientDisconnect( client );
            }
            getLogger().log( Level.INFO, "Cleaned up client " + client );
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        RemoteClient client = ctx.attr( UUID_ATTR ).get();
        getLogger().log( Level.SEVERE, "Caught exception for channel address " + ctx.channel().remoteAddress() + " : " + ( ( client != null ) ? client.getUuid() : "unregistered" ), cause );
        ctx.channel().writeAndFlush( new ErrorPacket( cause.getMessage() ) );
        ctx.channel().close();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractPacket msg) throws Exception
    {
        if ( msg instanceof HandshakePacket )
        {
            HandshakePacket handshake = (HandshakePacket) msg;
            if ( ctx.attr( UUID_ATTR ).get() != null )
            {
                throw new RuntimeException( "Duplicate handshake" ); // TODO alert client
            }
            RemoteClient existing = getClient( handshake.getUuid() );
            if ( existing != null )
            {
                throw new RuntimeException( "Existing client with UUID  " + handshake.getUuid() + " " + ctx.channel().remoteAddress() ); // TODO: alert client
            }

            if ( !password.equals( handshake.getPassword() ) )
            {
                throw new RuntimeException( "Bad password from " + handshake.getUuid() + " " + ctx.channel().remoteAddress() ); // TODO: alert client
            }

            RemoteClient newClient = new RemoteClient( channel, handshake.getUuid(), handshake.getTags() );
            ctx.attr( UUID_ATTR ).set( newClient );
            remoteConnections.put( handshake.getUuid(), newClient );
            for ( String s : newClient.getTags() )
            {
                remoteTags.put( s, newClient );
            }

            getLogger().log( Level.INFO, "Accepted new client " + newClient );

            // Event
            for ( Plugin p : pluginManager.getPlugins() )
            {
                p.clientConnect( newClient );
            }

            newClient.send( new ErrorPacket( "OK" ) );
        } else if ( msg instanceof ForwardPacket )
        {
            ForwardPacket forward = (ForwardPacket) msg;
            switch ( forward.getStyle() )
            {
                case BROADCAST:
                    sendBroadcast( forward.getPacket() );
                    break;
                case MULTICAST:
                    sendMulticast( forward.getTarget(), forward.getPacket() );
                    break;
                case UNICAST:
                    sendUnicast( UUID.fromString( forward.getTarget() ), forward.getPacket() );
                    break;
            }
        } else if ( msg instanceof EchoPacket )
        {
            ctx.writeAndFlush( msg );
        } else
        {
            RemoteClient client = ctx.attr( UUID_ATTR ).get();
            if ( client == null )
            {
                throw new RuntimeException( "Got unexpected packet from unregistered client" );
            }
            // Event
            for ( Plugin p : pluginManager.getPlugins() )
            {
                p.packetReceived( client, msg );
            }
        }
    }

    public static void main(String[] args) throws Exception
    {
        Log.setOutput( new PrintStream( ByteStreams.nullOutputStream() ) );
        AnsiConsole.systemInstall();
        consoleReader = new ConsoleReader();
        consoleReader.setExpandEvents( false );

        logger = new BungeeLogger();
        System.setErr( new PrintStream( new LoggingOutputStream( logger, Level.SEVERE ), true ) );
        System.setOut( new PrintStream( new LoggingOutputStream( logger, Level.INFO ), true ) );

        instance = new ChunkrServer();

        while ( isRunning )
        {
            String line = consoleReader.readLine( ">" );
            if ( line != null )
            {
                if ( !getInstance().getPluginManager().dispatchCommand( line ) )
                {
                    logger.info( "Command not found" );
                }
            }
        }
    }

    public Logger getLogger()
    {
        return logger;
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

        RemoteClient remote = getClient( target );
        if ( remote != null )
        {
            remote.send( packet );
        }
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

        for ( RemoteClient client : remoteTags.get( tag ) )
        {
            client.send( packet );
        }
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

        for ( RemoteClient client : remoteConnections.values() )
        {
            client.send( packet );
        }
    }
}
