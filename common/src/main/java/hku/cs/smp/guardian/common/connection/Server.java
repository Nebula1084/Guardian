package hku.cs.smp.guardian.common.connection;

import hku.cs.smp.guardian.common.protocol.Message;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.reactivex.Flowable;
import io.reactivex.processors.PublishProcessor;
import org.reactivestreams.Subscriber;

import java.util.logging.Logger;

public class Server {

    private static Logger logger = Logger.getLogger(Server.class.getName());
    private PublishProcessor<Message> messageProcessor;

    public Server() {
        this.messageProcessor = PublishProcessor.create();
    }

    public void run(int port) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new HandlerInitializer(messageProcessor))
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .option(ChannelOption.SO_KEEPALIVE, true);


            bootstrap.bind(port).channel().closeFuture().sync();
        } finally {
            logger.info("Shutdown Server");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }

    public void subscribe(Subscriber<Message> s) {
        this.messageProcessor.subscribeActual(s);
    }

    public <T> Flowable<T> ofType(Class<T> cls) {
        return this.messageProcessor.ofType(cls);
    }
}
