package hku.cs.smp.guardian.common.protocol;

import io.netty.buffer.ByteBuf;

import java.util.Locale;

public class InquiryResponse extends Response {
    private int rejectNumber;

    InquiryResponse() {

    }

    @Override
    public void decode(ByteBuf in) {
        super.seqNo = in.readInt();
        this.rejectNumber = in.readInt();
    }

    @Override
    public void encode(ByteBuf out) {
        out.writeByte(Message.INQUIRY_RESPONSE);
        out.writeShort(8);
        out.writeInt(super.seqNo);
        out.writeInt(rejectNumber);
    }

    @Override
    public int length() {
        return 11;
    }

    public void setRejectNumber(int rejectNumber) {
        this.rejectNumber = rejectNumber;
    }

    public int getRejectNumber() {
        return rejectNumber;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "InquiryResponse[seqNo:%d, rejectNumber:%d]",
                super.seqNo, this.rejectNumber
        );
    }
}
