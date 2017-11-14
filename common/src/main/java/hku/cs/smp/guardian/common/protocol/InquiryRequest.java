package hku.cs.smp.guardian.common.protocol;

import io.netty.buffer.ByteBuf;

import java.util.Locale;
import java.util.Objects;
import java.util.logging.Logger;

public class InquiryRequest extends Request {
    private String phoneNumber;

    public InquiryRequest() {
    }

    public InquiryRequest(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public void decode(ByteBuf in) {
        super.seqNo = in.readInt();
        byte[] bytes = new byte[in.readableBytes()];
        in.readBytes(bytes);
        phoneNumber = new String(bytes);
    }

    @Override
    public void encode(ByteBuf out) {
        out.writeByte(Message.INQUIRY_REQUEST);
        out.writeShort(4 + phoneNumber.length());
        out.writeInt(super.seqNo);
        out.writeBytes(phoneNumber.getBytes());
    }

    @Override
    public int length() {
        return 7 + phoneNumber.length();
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "InquiryRequest[seqNo:%d, phoneNumber:%s]",
                super.seqNo, this.phoneNumber
        );
    }

    @Override
    public InquiryResponse getResponse() {
        InquiryResponse inquiryResponse = new InquiryResponse();
        inquiryResponse.setSeqNo(super.seqNo);
        return inquiryResponse;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof InquiryRequest) {
            InquiryRequest ir = (InquiryRequest) obj;
            return ir.seqNo == this.seqNo && Objects.equals(ir.phoneNumber, this.phoneNumber);
        } else
            return false;
    }
}
