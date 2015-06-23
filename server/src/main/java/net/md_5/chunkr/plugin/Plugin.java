package net.md_5.chunkr.plugin;

import lombok.Getter;
import net.md_5.chunkr.ChunkrServer;
import net.md_5.chunkr.RemoteClient;
import net.md_5.chunkr.packet.AbstractPacket;

public class Plugin
{

    @Getter
    private PluginDescription description;
    @Getter
    private ChunkrServer server;

    public void onLoad()
    {
    }

    public void onEnable()
    {
    }

    public void onDisable()
    {
    }

    public void clientConnect(RemoteClient client)
    {
    }

    public void clientDisconnect(RemoteClient client)
    {
    }

    public void packetReceived(RemoteClient client, AbstractPacket packet)
    {
    }

    final void init(ChunkrServer server, PluginDescription description)
    {
        this.server = server;
        this.description = description;
    }
}
