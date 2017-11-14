package hku.cs.smp.guardian.common.connection;

import hku.cs.smp.guardian.common.protocol.Message;
import hku.cs.smp.guardian.common.protocol.MessageFactory;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;
import java.util.logging.Logger;

public class MessageDecoder extends ByteToMessageDecoder {
    private static Logger logger = Logger.getLogger(MessageDecoder.class.getName());

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() < 3)
            return;

        in.markReaderIndex();
        byte type = in.readByte();
        Short length = in.readShort();

        if (in.readableBytes() < length) {
            in.resetReaderIndex();
            return;
        }

        Message message = MessageFactory.create(type);
        message.decode(in.readSlice(length));
        out.add(message);
    }
}
