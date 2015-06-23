package net.md_5.chunkr.plugin;

import java.io.File;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO representing the plugin.yml file.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PluginDescription
{

    /**
     * Friendly name of the plugin.
     */
    private String name;
    /**
     * Plugin main class. Needs to extend {@link Plugin}.
     */
    private String main;
    /**
     * Plugin version.
     */
    private String version;
    /**
     * Plugin author.
     */
    private String author;
    /**
     * File we loaded from.
     */
    private File file = null;
}
