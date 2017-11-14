package hku.cs.smp.guardian.common.protocol;

import io.reactivex.subscribers.TestSubscriber;
import org.junit.Assert;
import org.junit.Test;

public class TagProtocolTest extends ProtocolTest {

    @Test
    public void testRequestResolve() {

        TestSubscriber<Message> subscriber = new TestSubscriber<>();
        serverProcessor.subscribe(subscriber);
        subscriber.assertSubscribed();
        final String prefix = "tag prefix";
        serverProcessor.subscribe(message -> {
            TagRequest request = (TagRequest) message;

            try {
                Assert.assertEquals(request.getTag(), prefix + request.getSeqNo());
            } catch (Throwable e) {
                serverProcessor.onError(e);
            }
        });

        final int num = 10;
        for (int i = 0; i < num; i++) {
            TagRequest request = new TagRequest(prefix + i);
            request.setSeqNo(i);
            clientChannel.writeOutbound(request);
            serverChannel.writeInbound(clientChannel.readOutbound());
        }

        serverProcessor.onComplete();

        subscriber.assertNoErrors();
        subscriber.assertComplete();
        subscriber.assertValueCount(num);
    }

    @Test
    public void testResponseResolve() {
        TestSubscriber<Message> subscriber = new TestSubscriber<>();
        clientProcessor.subscribe(subscriber);
        subscriber.assertSubscribed();
        clientProcessor.subscribe(message -> {
            TagResponse response = (TagResponse) message;
            try {
                Assert.assertEquals(response.getSeqNo(), response.getSeqNo());
            } catch (Throwable e) {
                clientProcessor.onError(e);
            }
        });

        final int num = 7;

        for (int i = 0; i < num; i++) {
            TagResponse response = new TagResponse();
            response.setSeqNo(i);
            serverChannel.writeOutbound(response);
            clientChannel.writeInbound(serverChannel.readOutbound());
        }

        clientProcessor.onComplete();

        subscriber.assertComplete();
        subscriber.assertNoErrors();
        subscriber.assertValueCount(num);

    }

}
