package net.md_5.chunkr.hook;

import net.md_5.chunkr.ConnectClient;
import net.md_5.chunkr.packet.AbstractPacket;

public class HookListener
{

    public void connected(ConnectClient client)
    {
    }

    public void disconnected(ConnectClient client)
    {
    }

    public void packetReceived(ConnectClient client, AbstractPacket packet)
    {
    }
}
