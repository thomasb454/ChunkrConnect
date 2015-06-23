package net.md_5.chunkr;

import com.google.common.base.Preconditions;
import io.netty.channel.Channel;
import java.util.UUID;
import lombok.Data;
import net.md_5.chunkr.packet.AbstractPacket;

@Data
public class RemoteClient
{

    private final Channel channel;
    private final UUID uuid;
    private final String[] tags;

    /*========================================================================*/
    /**
     * Forcibly disconnect this client: n.b in many cases the connection will
     * automatically be re-established.
     */
    public void disconnect()
    {
        Preconditions.checkState( channel.isOpen(), "Channel is closed" );
        channel.close();
    }

    /**
     * Sends the specified packet to the other endpoint of this client. If this
     * is a local client, it will be sent to the server, if this is a remote
     * client it will be sent to the client.
     *
     * @param packet the packet to send
     */
    public void send(AbstractPacket packet)
    {
        Preconditions.checkArgument( packet != null, "packet is null" );
        Preconditions.checkState( channel.isActive(), "channel is closed" );

        channel.writeAndFlush( packet );
    }
}
