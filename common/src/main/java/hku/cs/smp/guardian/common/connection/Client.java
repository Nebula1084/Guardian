package hku.cs.smp.guardian.common.connection;

import hku.cs.smp.guardian.common.protocol.Message;
import hku.cs.smp.guardian.common.protocol.Request;
import hku.cs.smp.guardian.common.protocol.Response;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.reactivex.functions.Consumer;
import io.reactivex.processors.PublishProcessor;

import java.util.HashMap;
import java.util.Map;

public class Client {
    private PublishProcessor<Message> messageProcessor;
    private Channel channel;
    private int seqNo;
    private Map<Integer, Handler> handlers;
    private Map<Integer, Request> requests;
    private EventLoopGroup group;

    public interface Handler {
        void handle(Request request, Response response);
    }

    public Client() {
        seqNo = 0;
        messageProcessor = PublishProcessor.create();
        handlers = new HashMap<Integer, Handler>();
        requests = new HashMap<Integer, Request>();
        messageProcessor.subscribe(new Consumer<Message>() {
            @Override
            public void accept(Message message) throws Exception {
                int seqNo = message.getSeqNo();
                synchronized (this) {
                    handlers.get(seqNo).handle(requests.get(seqNo), (Response) message);
                    handlers.remove(seqNo);
                    requests.remove(seqNo);
                }
            }
        });
    }

    public void connect(String host, int port) {
        group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();

        try {
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new HandlerInitializer(messageProcessor))
                    .option(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture channelFuture = bootstrap.connect(host, port);
            channel = channelFuture.sync().channel();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        channel.closeFuture().sync();
                    } catch (InterruptedException e) {
                        group.shutdownGracefully();
                        e.printStackTrace();
                    }
                }
            }).start();

        } catch (InterruptedException e) {
            e.printStackTrace();
            group.shutdownGracefully();
            System.out.println("Shutdown Client");
        }

    }

    public void post(Request request, Handler handler) {
        synchronized (this) {
            seqNo += 1;
            request.setSeqNo(seqNo);
            handlers.put(seqNo, handler);
            requests.put(seqNo, request);
        }
        channel.writeAndFlush(request);

    }

    public void shutdown() throws InterruptedException {
        group.shutdownGracefully();
    }

    Channel getChannel() {
        return channel;
    }
}
