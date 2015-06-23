package net.md_5.chunkr;

import java.util.UUID;

public class TestClient
{

    public static void main(String[] args)
    {
        ConnectClient client = new ConnectClient( "localhost", "qwertyuiop", UUID.randomUUID(), new String[]
        {
            "PvP", "CTF"
        } );
        client.connect();
    }
}
