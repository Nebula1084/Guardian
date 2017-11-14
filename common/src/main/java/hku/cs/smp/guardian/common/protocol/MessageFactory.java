package hku.cs.smp.guardian.common.protocol;

import java.util.Locale;

public class MessageFactory {

    static public Message create(byte type) {
        switch (type) {
            case Message.INQUIRY_REQUEST:
                return new InquiryRequest();
            case Message.INQUIRY_RESPONSE:
                return new InquiryResponse();
            case Message.TAG_REQUEST:
                return new TagRequest();
            case Message.TAG_RESPONSE:
                return new TagResponse();
            default:
                String message = String.format(Locale.getDefault(), "No such type key %d", type);
                throw new IllegalArgumentException(message);
        }
    }
}
