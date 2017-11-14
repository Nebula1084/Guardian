package hku.cs.smp.guardian.common.protocol;

import io.netty.buffer.ByteBuf;

import java.util.Locale;
import java.util.Objects;
import java.util.logging.Logger;

public class TagRequest extends Request {
    private String phone;
    private String tag;

    TagRequest() {

    }

    public TagRequest(String phone, String tag) {
        this.phone = phone;
        this.tag = tag;
    }

    @Override
    public void decode(ByteBuf in) {
        super.seqNo = in.readInt();

        int len = in.readableBytes() - 1;
        int phoneLen = in.readByte();
        int tagLen = len - phoneLen;

        byte[] phoneBytes = new byte[phoneLen];
        in.readBytes(phoneBytes, 0, phoneLen);
        phone = new String(phoneBytes);

        byte[] tagBytes = new byte[tagLen];
        in.readBytes(tagBytes);
        tag = new String(tagBytes);
    }

    @Override
    public void encode(ByteBuf out) {
        out.writeByte(Message.TAG_REQUEST);
        out.writeShort(5 + phone.length() + tag.length());
        out.writeInt(super.seqNo);
        out.writeByte(phone.length());
        out.writeBytes(phone.getBytes());
        out.writeBytes(tag.getBytes());
    }

    @Override
    public int length() {
        return 7 + tag.length();
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "TagRequest[seqNo:%d, phone:%s, tag:%s]",
                super.seqNo, this.phone, this.tag);
    }

    @Override
    public Response getResponse() {
        TagResponse tagResponse = new TagResponse();
        tagResponse.setSeqNo(super.seqNo);
        return tagResponse;
    }

    public String getPhone() {
        return phone;
    }

    public String getTag() {
        return tag;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TagRequest) {
            TagRequest tr = (TagRequest) obj;
            return tr.seqNo == this.seqNo && Objects.equals(tr.tag, this.tag) && Objects.equals(tr.phone, this.phone);
        } else
            return false;
    }
}
