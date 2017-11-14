package hku.cs.smp.guardian.common.connection;

import hku.cs.smp.guardian.common.protocol.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.reactivex.processors.FlowableProcessor;

import java.util.logging.Logger;

public class MessageHandler extends ChannelInboundHandlerAdapter {
    private static Logger logger = Logger.getLogger(MessageHandler.class.getName());
    private FlowableProcessor<Message> messageProcessor;

    public MessageHandler(FlowableProcessor<Message> messageProcessor) {
        this.messageProcessor = messageProcessor;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        logger.info("Receive message " + msg);
        Message message = (Message) msg;
        message.setContext(ctx);
        this.messageProcessor.onNext(message);
    }
}
