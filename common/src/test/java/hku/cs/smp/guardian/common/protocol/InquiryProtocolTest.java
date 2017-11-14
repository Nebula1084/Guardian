package hku.cs.smp.guardian.common.protocol;

import io.reactivex.functions.Consumer;
import io.reactivex.subscribers.TestSubscriber;
import org.junit.Assert;
import org.junit.Test;

public class InquiryProtocolTest extends ProtocolTest {


    @Test
    public void testRequestResolve() {
        TestSubscriber<Message> subscriber = new TestSubscriber<Message>();
        serverProcessor.subscribe(subscriber);
        subscriber.assertSubscribed();
        final String prefix = "178388542";
        serverProcessor.subscribe(new Consumer<Message>() {
            @Override
            public void accept(Message message) throws Exception {

                InquiryRequest request = (InquiryRequest) message;
                try {
                    Assert.assertEquals(request.getPhoneNumber(), prefix + request.getSeqNo());
                } catch (Throwable e) {
                    serverProcessor.onError(e);
                }
            }
        });

        final int num = 10;
        for (int i = 0; i < num; i++) {
            InquiryRequest request = new InquiryRequest(prefix + i);
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
        TestSubscriber<Message> subscriber = new TestSubscriber<Message>();
        clientProcessor.subscribe(subscriber);
        subscriber.assertSubscribed();

        clientProcessor.subscribe(new Consumer<Message>() {
            @Override
            public void accept(Message message) throws Exception {

                InquiryResponse response = (InquiryResponse) message;
                try {
                    Assert.assertEquals(response.getSeqNo() + 12, response.getRejectNumber());
                } catch (Throwable e) {
                    clientProcessor.onError(e);
                }
            }
        });

        final int num = 7;

        for (int i = 0; i < num; i++) {
            InquiryResponse response = new InquiryResponse();
            response.setSeqNo(i);
            response.setRejectNumber(i + 12);
            serverChannel.writeOutbound(response);
            clientChannel.writeInbound(serverChannel.readOutbound());
        }

        clientProcessor.onComplete();

        subscriber.assertComplete();
        subscriber.assertNoErrors();
        subscriber.assertValueCount(num);
    }
}
