package net.md_5.chunkr;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeConnect extends Plugin
{

    private ConnectClient client;

    @Override
    public void onEnable()
    {
        try
        {
            File propsFile = new File( getDataFolder(), "config.properties" );
            propsFile.getParentFile().mkdir();

            Properties props = new Properties();
            props.putAll( new HashMap<String, String>()
            {
                {
                    put( "host", "0.0.0.0:" + Util.DEFAULT_PORT );
                    put( "password", "qwertyuiop" );
                    put( "uuid", UUID.randomUUID().toString() );
                    put( "tags", "Bungee" );
                }
            } );

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

            client = new ConnectClient( props.getProperty( "host" ), props.getProperty( "password" ), UUID.fromString( props.getProperty( "uuid" ) ),
                    Iterables.toArray( Splitter.on( ',' ).split( props.getProperty( "tags" ) ), String.class ) );
            client.connect();
        } catch ( IOException ex )
        {
            getLogger().log( Level.WARNING, "Error loading", ex );
        }
    }

    @Override
    public void onDisable()
    {
        client.disconnect();
        client.kill();
    }

    public ConnectClient getClient()
    {
        return client;
    }
}
