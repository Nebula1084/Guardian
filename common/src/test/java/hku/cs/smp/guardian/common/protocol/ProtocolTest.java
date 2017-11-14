package hku.cs.smp.guardian.common.protocol;

import hku.cs.smp.guardian.common.connection.MessageDecoder;
import hku.cs.smp.guardian.common.connection.MessageEncoder;
import hku.cs.smp.guardian.common.connection.MessageHandler;
import io.netty.channel.embedded.EmbeddedChannel;
import io.reactivex.processors.PublishProcessor;
import org.junit.Before;

public class ProtocolTest {
    protected EmbeddedChannel serverChannel;
    protected EmbeddedChannel clientChannel;
    protected PublishProcessor<Message> serverProcessor;
    protected PublishProcessor<Message> clientProcessor;

    @Before
    public void before() {
        clientProcessor = PublishProcessor.create();
        serverProcessor = PublishProcessor.create();

        serverChannel = new EmbeddedChannel(
                new MessageDecoder(),
                new MessageHandler(serverProcessor),
                new MessageEncoder()
        );

        clientChannel = new EmbeddedChannel(
                new MessageDecoder(),
                new MessageHandler(clientProcessor),
                new MessageEncoder()
        );
    }

}
