package net.md_5.chunkr.packet;

import io.netty.buffer.ByteBuf;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class HandshakePacket extends AbstractPacket
{

    private String password;
    private UUID uuid;
    private String[] tags;

    @Override
    public void read(ByteBuf buf)
    {
        password = readString( buf );
        uuid = UUID.fromString( readString( buf ) );
        tags = readStringArray( buf );
    }

    @Override
    public void write(ByteBuf buf)
    {
        writeString( password, buf );
        writeString( uuid.toString(), buf );
        writeStringArray( tags, buf );
    }
}
