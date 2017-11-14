package hku.cs.smp.guardian.common.protocol;

import io.netty.buffer.ByteBuf;

import java.util.Locale;
import java.util.Map;

public class InquiryResponse extends Response {
    private int rejectNumber;
    private Map<String, Integer> result;

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

    public void setResult(Map<String, Integer> result) {
        this.result = result;
    }
}
