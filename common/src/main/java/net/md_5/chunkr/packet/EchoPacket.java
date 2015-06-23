package net.md_5.chunkr.packet;

import io.netty.buffer.ByteBuf;
import lombok.ToString;

@ToString
public class EchoPacket extends AbstractPacket
{

    @Override
    public void read(ByteBuf buf)
    {
    }

    @Override
    public void write(ByteBuf buf)
    {
    }
}
