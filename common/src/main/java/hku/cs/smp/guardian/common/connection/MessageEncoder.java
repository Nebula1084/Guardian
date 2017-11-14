package hku.cs.smp.guardian.common.connection;

import hku.cs.smp.guardian.common.protocol.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.logging.Logger;

public class MessageEncoder extends MessageToByteEncoder<Message> {

    private static Logger logger = Logger.getLogger(MessageEncoder.class.getName());

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        logger.info(msg.toString());
        msg.encode(out);
    }
}
