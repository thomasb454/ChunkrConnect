package net.md_5.chunkr.plugin;

import com.google.common.base.Preconditions;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import net.md_5.chunkr.ChunkrServer;
import org.yaml.snakeyaml.Yaml;

@RequiredArgsConstructor
public class PluginManager
{

    private static final Pattern argsSplit = Pattern.compile( " " );
    /*========================================================================*/
    private final ChunkrServer server;
    /*========================================================================*/
    private final Yaml yaml = new Yaml();
    private final Map<String, Plugin> plugins = new LinkedHashMap<>();
    private final Map<String, Command> commandMap = new HashMap<>();

    public void registerCommand(Plugin plugin, Command command)
    {
        commandMap.put( command.getName().toLowerCase(), command );
        for ( String alias : command.getAliases() )
        {
            commandMap.put( alias.toLowerCase(), command );
        }
    }

    public boolean dispatchCommand(String commandLine)
    {
        String[] split = argsSplit.split( commandLine );
        // Check for chat that only contains " "
        if ( split.length == 0 )
        {
            return false;
        }

        String commandName = split[0].toLowerCase();
        Command command = commandMap.get( commandName );
        if ( command == null )
        {
            return false;
        }

        String[] args = Arrays.copyOfRange( split, 1, split.length );
        try
        {
            command.execute( args );
        } catch ( Exception ex )
        {
            server.getLogger().log( Level.WARNING, "Error in dispatching command", ex );
        }
        return true;
    }

    public Plugin getPlugin(String name)
    {
        return plugins.get( name );
    }

    public Collection<Plugin> getPlugins()
    {
        return this.plugins.values();
    }

    public void loadPlugins(File folder)
    {
        Preconditions.checkNotNull( folder, "folder" );
        Preconditions.checkArgument( folder.isDirectory(), "Must load from a directory" );

        List<PluginDescription> toLoad = new ArrayList<>();
        for ( File file : folder.listFiles() )
        {
            if ( file.isFile() && file.getName().endsWith( ".jar" ) )
            {
                try ( JarFile jar = new JarFile( file ) )
                {
                    JarEntry pdf = jar.getJarEntry( "chunkr.yml" );
                    if ( pdf == null )
                    {
                        pdf = jar.getJarEntry( "plugin.yml" );
                    }
                    Preconditions.checkNotNull( pdf, "Plugin must have a plugin.yml or bungee.yml" );

                    try ( InputStream in = jar.getInputStream( pdf ) )
                    {
                        PluginDescription desc = yaml.loadAs( in, PluginDescription.class );
                        desc.setFile( file );

                        toLoad.add( desc );
                    }
                } catch ( Exception ex )
                {
                    server.getLogger().log( Level.WARNING, "Could not load plugin from file " + file, ex );
                }
            }
        }

        for ( PluginDescription plugin : toLoad )
        {
            try
            {
                URLClassLoader loader = new PluginClassloader( new URL[]
                {
                    plugin.getFile().toURI().toURL()
                } );
                Class<?> main = loader.loadClass( plugin.getMain() );
                Plugin clazz = (Plugin) main.getDeclaredConstructor().newInstance();

                clazz.init( server, plugin );
                plugins.put( plugin.getName(), clazz );
                clazz.onLoad();

                server.getLogger().log( Level.INFO, "Loaded plugin {0} version {1} by {2}", new Object[]
                {
                    plugin.getName(), plugin.getVersion(), plugin.getAuthor()
                } );
            } catch ( Throwable t )
            {
                server.getLogger().log( Level.WARNING, "Error enabling plugin " + plugin.getName(), t );
            }
        }
    }

    public void enablePlugins()
    {
        for ( Plugin plugin : plugins.values() )
        {
            try
            {
                plugin.onEnable();
                server.getLogger().log( Level.INFO, "Enabled plugin {0} version {1} by {2}", new Object[]
                {
                    plugin.getDescription().getName(), plugin.getDescription().getVersion(), plugin.getDescription().getAuthor()
                } );
            } catch ( Throwable t )
            {
                server.getLogger().log( Level.WARNING, "Exception encountered when loading plugin: " + plugin.getDescription().getName(), t );
            }
        }
    }
}
