package hku.cs.smp.guardian.common.protocol;

import io.netty.buffer.ByteBuf;

import java.util.Locale;

public class TagResponse extends Response {

    TagResponse() {

    }

    @Override
    public void decode(ByteBuf in) {
        super.seqNo = in.readInt();
    }

    @Override
    public void encode(ByteBuf out) {
        out.writeByte(Message.TAG_RESPONSE);
        out.writeShort(4);
        out.writeInt(super.seqNo);
    }

    @Override
    public int length() {
        return 7;
    }

    @Override

    public String toString() {
        return String.format(Locale.getDefault(),
                "TagResponse[seqNo:%d]",
                super.seqNo);

    }
}
