package net.md_5.chunkr;

import net.md_5.chunkr.packet.AbstractPacket;
import net.md_5.lib.io.netty.buffer.ByteBuf;

public class PlayerCountPacket extends AbstractPacket
{

    private int count;

    public PlayerCountPacket()
    {
    }

    public PlayerCountPacket(int count)
    {
        this.count = count;
    }

    @Override
    public void read(ByteBuf bb)
    {
        count = bb.readInt();
    }

    @Override
    public void write(ByteBuf bb)
    {
        bb.writeInt( count );
    }

    public int getCount()
    {
        return count;
    }

    @Override
    public String toString()
    {
        return "PlayerCountPacket{" + count + "}";
    }
}
