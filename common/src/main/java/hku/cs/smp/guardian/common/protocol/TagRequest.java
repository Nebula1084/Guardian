package hku.cs.smp.guardian.common.protocol;

import io.netty.buffer.ByteBuf;

import java.util.Locale;
import java.util.Objects;
import java.util.logging.Logger;

public class TagRequest extends Request {
    private String tag;

    TagRequest() {

    }

    public TagRequest(String tag) {
        this.tag = tag;
    }

    @Override
    public void decode(ByteBuf in) {
        super.seqNo = in.readInt();
        byte[] bytes = new byte[in.readableBytes()];
        in.readBytes(bytes);
        tag = new String(bytes);
    }

    @Override
    public void encode(ByteBuf out) {
        out.writeByte(Message.TAG_REQUEST);
        out.writeShort(4 + tag.length());
        out.writeInt(super.seqNo);
        out.writeBytes(tag.getBytes());
    }

    @Override
    public int length() {
        return 7 + tag.length();
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "TagRequest[seqNo:%d, tag:%s]",
                super.seqNo, this.tag);
    }

    @Override
    public Response getResponse() {
        TagResponse tagResponse = new TagResponse();
        tagResponse.setSeqNo(super.seqNo);
        return tagResponse;
    }

    public String getTag() {
        return tag;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TagRequest) {
            TagRequest tr = (TagRequest) obj;
            return tr.seqNo == this.seqNo && Objects.equals(tr.tag, this.tag);
        } else
            return false;
    }
}
