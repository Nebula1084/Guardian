package hku.cs.smp.guardian.common.connection;

import hku.cs.smp.guardian.common.protocol.Message;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.reactivex.processors.FlowableProcessor;


public class HandlerInitializer extends ChannelInitializer<SocketChannel> {


    private FlowableProcessor<Message> messageProcessor;

    public HandlerInitializer(FlowableProcessor<Message> messageProcessor) {
        this.messageProcessor = messageProcessor;
    }

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        this.init(channel);
    }

    public void init(Channel channel) {
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast("Message Decoder", new MessageDecoder());
        pipeline.addLast("Message Handler", new MessageHandler(messageProcessor));
        pipeline.addLast("Message Encoder", new MessageEncoder());
    }
}
