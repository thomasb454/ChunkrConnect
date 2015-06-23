package net.md_5.chunkr;

import net.md_5.chunkr.hook.HookListener;
import net.md_5.chunkr.packet.AbstractPacket;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitExample extends JavaPlugin implements Listener
{

    @Override
    public void onEnable()
    {
        getLogger().info( "Bukkit example plugin loading" );

        AbstractPacket.registerPacket( PlayerCountPacket.class );

        getServer().getPluginManager().registerEvents( this, this );

        BukkitConnect.getInstance().getClient().getHookManager().addListener( new HookListener()
        {

            @Override
            public void connected(ConnectClient client)
            {
                getLogger().info( "Connected" );
            }

            @Override
            public void disconnected(ConnectClient client)
            {
                getLogger().info( "Disconnected" );
            }

            @Override
            public void packetReceived(ConnectClient client, AbstractPacket packet)
            {
                getLogger().info( "Packet received " + packet );
            }
        } );
    }

    @Override
    public void onDisable()
    {
        getLogger().info( "Bukkit example plugin unloading" );
        updateCount( -1 );
    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent event)
    {
        updateCount( getServer().getOnlinePlayers().length );
    }

    @EventHandler
    public void playerQuit(PlayerQuitEvent event)
    {
        updateCount( getServer().getOnlinePlayers().length );
    }

    private void updateCount(int count)
    {
        getLogger().info( "Sending player count update " + count );
        BukkitConnect.getInstance().getClient().send( new PlayerCountPacket( count ) );
    }
}
