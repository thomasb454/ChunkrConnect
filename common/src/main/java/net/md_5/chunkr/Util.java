package net.md_5.chunkr;

import com.google.common.base.Throwables;
import java.net.InetSocketAddress;

public class Util
{

    public static final int DEFAULT_PORT = 13543;

    public static InetSocketAddress parseHostPort(String s)
    {
        String[] split = s.split( ":", 2 );
        int port = DEFAULT_PORT;
        if ( split.length == 2 )
        {
            try
            {
                port = Integer.parseInt( split[1] );
            } catch ( NumberFormatException ex )
            {
            }
        }

        return new InetSocketAddress( split[0], port );
    }
}
