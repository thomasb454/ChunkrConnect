package net.md_5.chunkr;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.md_5.chunkr.packet.AbstractPacket;
import net.md_5.chunkr.plugin.Command;
import net.md_5.chunkr.plugin.Plugin;

public class ChunkrExample extends Plugin
{

    private final Map<UUID, Integer> otherServers = new HashMap<>();

    @Override
    public void onEnable()
    {
        System.out.println( "Player count plugin enabled" );
        AbstractPacket.registerPacket( PlayerCountPacket.class );
        getServer().getPluginManager().registerCommand( this, new PlayerCountCommand() );
    }

    @Override
    public void onDisable()
    {
        System.out.println( "Player count plugin disabled" );
    }

    @Override
    public void clientConnect(RemoteClient client)
    {
        System.out.println( "We just got a connection from " + client );
        otherServers.put( client.getUuid(), 0 );
    }

    @Override
    public void clientDisconnect(RemoteClient client)
    {
        System.out.println( "We just lost a connection from " + client );
        otherServers.remove( client.getUuid() );
    }

    @Override
    public void packetReceived(RemoteClient client, AbstractPacket packet)
    {
        System.out.println( "Client " + client + " just sent us a packet " + packet );
        if ( packet instanceof PlayerCountPacket )
        {
            otherServers.put( client.getUuid(), ( (PlayerCountPacket) packet ).getCount() );
        }
    }

    private class PlayerCountCommand extends Command
    {

        public PlayerCountCommand()
        {
            super( "playercount", "lplayers" );
        }

        @Override
        public void execute(String[] args)
        {
            System.out.println( "Displaying player count from all servers:" );
            for ( Map.Entry<UUID, Integer> entry : otherServers.entrySet() )
            {
                System.out.println( entry.getKey() + " has " + entry.getValue() + " players" );
            }
        }
    }
}
