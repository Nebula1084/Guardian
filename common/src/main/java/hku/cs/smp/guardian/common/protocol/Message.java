package hku.cs.smp.guardian.common.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.Serializable;

public abstract class Message implements Serializable{
    public final static byte INQUIRY_REQUEST = 1;
    public final static byte INQUIRY_RESPONSE = 2;
    public final static byte TAG_REQUEST = 3;
    public final static byte TAG_RESPONSE = 4;

    protected int seqNo;

    abstract public void decode(ByteBuf in);

    abstract public void setContext(ChannelHandlerContext ctx);

    abstract public void encode(ByteBuf out);

    abstract public int length();

    public void setSeqNo(int seqNo) {
        this.seqNo = seqNo;
    }

    public int getSeqNo() {
        return seqNo;
    }

}
