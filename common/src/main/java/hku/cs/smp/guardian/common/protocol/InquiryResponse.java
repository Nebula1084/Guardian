package hku.cs.smp.guardian.common.protocol;

import io.netty.buffer.ByteBuf;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class InquiryResponse extends Response {
    private Map<String, Integer> result;

    InquiryResponse() {
        result = new HashMap<>();
    }

    @Override
    public void decode(ByteBuf in) {
        super.seqNo = in.readInt();
        int size = in.readByte();
        for (int i = 0; i < size; i++) {
            int length = in.readByte();
            byte bytes[] = new byte[length];
            in.readBytes(bytes, 0, length);
            String key = new String(bytes);
            Integer value = in.readInt();
            result.put(key, value);
        }
    }

    @Override
    public void encode(ByteBuf out) {
        out.writeByte(Message.INQUIRY_RESPONSE);
        out.writeShort(length() - 3);

        out.writeInt(super.seqNo);

        out.writeByte(result.size());
        for (Map.Entry<String, Integer> entry : result.entrySet()) {
            out.writeByte(entry.getKey().length());
            out.writeBytes(entry.getKey().getBytes());
            out.writeInt(entry.getValue());
        }
    }

    @Override
    public int length() {
        short len = 8;
        for (Map.Entry<String, Integer> entry : result.entrySet()) {
            len += 1 + entry.getKey().length() + 4;
        }
        return len;
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(),
                "InquiryResponse[seqNo:%d, result:%s]",
                super.seqNo, this.result.toString()
        );
    }

    public void setResult(Map<String, Integer> result) {
        this.result = result;
    }

    public Map<String, Integer> getResult() {
        return result;
    }
}
