package net.md_5.chunkr.packet;

import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ForwardPacket extends AbstractPacket
{

    public enum Style
    {

        UNICAST, MULTICAST, BROADCAST;
    }
    private Style style;
    private String target;
    private AbstractPacket packet;

    @Override
    public void read(ByteBuf buf)
    {
        style = Style.values()[buf.readByte()];
        if ( style != Style.BROADCAST )
        {
            target = readString( buf );
        }

        String packetClazz = readString( buf );
        packet = newInstance( packetClazz );
        if ( packet == null )
        {
            throw new RuntimeException( "Unknown packet ID " + packetClazz );
        }
        packet.read( buf );
    }

    @Override
    public void write(ByteBuf buf)
    {
        buf.writeByte( style.ordinal() );
        if ( style != Style.BROADCAST )
        {
            writeString( target, buf );
        }

        writeString( packet.getClass().getName(), buf );
        packet.write( buf );
    }
}
