package net.md_5.chunkr.hook;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import net.md_5.chunkr.ConnectClient;
import net.md_5.chunkr.packet.AbstractPacket;

public class HookManager
{

    private final List<HookListener> hookListeners = new CopyOnWriteArrayList<>();

    public void fireConnect(ConnectClient client)
    {
        for ( HookListener l : hookListeners )
        {
            l.connected( client );
        }
    }

    public void fireDisconnect(ConnectClient client)
    {
        for ( HookListener l : hookListeners )
        {
            l.disconnected( client );
        }
    }

    public void firePacketReceived(ConnectClient client, AbstractPacket packet)
    {
        for ( HookListener l : hookListeners )
        {
            l.packetReceived( client, packet );
        }
    }

    public void addListener(HookListener listener)
    {
        hookListeners.add( listener );
    }
}
