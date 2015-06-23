package net.md_5.chunkr;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitConnect extends JavaPlugin
{

    private ConnectClient client;
    private static BukkitConnect instance;

    @Override
    public void onEnable()
    {
        instance = this;

        FileConfiguration conf = getConfig();
        conf.addDefault( "host", "127.0.0.1:" + Util.DEFAULT_PORT );
        conf.addDefault( "password", "qwerty" );
        conf.addDefault( "uuid", UUID.randomUUID().toString() );
        conf.addDefault( "tags", Arrays.asList( "Hub" ) );
        getConfig().options().copyDefaults( true );
        saveConfig();

        List<String> tags = conf.getStringList( "tags" );

        client = new ConnectClient( conf.getString( "host" ), conf.getString( "password" ), UUID.fromString( conf.getString( "uuid" ) ), tags.toArray( new String[ tags.size() ] ) );
        client.connect();
    }

    @Override
    public void onDisable()
    {
        client.disconnect();
        client.kill();
    }

    public static BukkitConnect getInstance()
    {
        return instance;
    }

    public ConnectClient getClient()
    {
        return client;
    }
}
