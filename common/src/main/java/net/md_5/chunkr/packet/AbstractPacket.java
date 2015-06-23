package net.md_5.chunkr.packet;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractPacket
{

    private static Map<String, Class<? extends AbstractPacket>> nameToClass = new HashMap<>();

    static
    {
        registerPacket( EchoPacket.class );
        registerPacket( ErrorPacket.class );
        registerPacket( ForwardPacket.class );
        registerPacket( HandshakePacket.class );
    }

    public static void registerPacket(Class<? extends AbstractPacket> packet)
    {
        nameToClass.put( packet.getName(), packet );
    }

    public static AbstractPacket newInstance(String packet)
    {
        Class<? extends AbstractPacket> clazz = nameToClass.get( packet );
        if ( clazz == null )
        {
            return null;
        }

        try
        {
            return clazz.getConstructor().newInstance();
        } catch ( Exception ex )
        {
            Throwables.propagate( ex );
        }

        return null;
    }

    public static void writeString(String s, ByteBuf buf)
    {
        Preconditions.checkArgument( s.length() <= Short.MAX_VALUE, "Cannot send string longer than Short.MAX_VALUE (got %s characters)", s.length() );

        byte[] b = s.getBytes( Charsets.UTF_8 );
        buf.writeShort( s.length() );
        buf.writeBytes( b );
    }

    public static String readString(ByteBuf buf)
    {
        int len = buf.readShort();
        Preconditions.checkArgument( len <= Short.MAX_VALUE, "Cannot receive string longer than Short.MAX_VALUE (got %s characters)", len );

        byte[] b = new byte[ len ];
        buf.readBytes( b );

        return new String( b, Charsets.UTF_8 );
    }

    public static void writeStringArray(String[] s, ByteBuf buf)
    {
        buf.writeShort( s.length );
        for ( String str : s )
        {
            writeString( str, buf );
        }
    }

    public static String[] readStringArray(ByteBuf buf)
    {
        int len = buf.readShort();
        String[] ret = new String[ len ];
        for ( int i = 0; i < ret.length; i++ )
        {
            ret[i] = readString( buf );
        }
        return ret;
    }

    public abstract void write(ByteBuf buf);

    public abstract void read(ByteBuf buf);

    @Override
    public abstract String toString();
}
