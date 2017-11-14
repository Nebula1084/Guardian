package hku.cs.smp.guardian.common.connection;

import hku.cs.smp.guardian.common.protocol.InquiryRequest;
import hku.cs.smp.guardian.common.protocol.InquiryResponse;
import hku.cs.smp.guardian.common.protocol.TagRequest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.logging.Logger;

public class IntegrateTest {
    private Client client;

    private static Logger logger = Logger.getLogger(IntegrateTest.class.getName());

    class Counter {
        int c;

        synchronized void add() {
            c++;
            notifyAll();
        }

        synchronized void done() {
            c--;
            notifyAll();
        }

        synchronized void check() {
            for (; ; ) {
                if (c != 0) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else break;
            }
        }

    }

    @BeforeClass
    public static void setupServer() throws InterruptedException {

        new Thread(() -> {
            Server server = new Server();
            server.ofType(TagRequest.class).subscribe(m -> {
                logger.info("server[t] " + m);
                m.response(m.getResponse());
            });
            server.ofType(InquiryRequest.class).subscribe(m -> {
                InquiryResponse ir = m.getResponse();
                ir.setRejectNumber(m.getSeqNo());
                logger.info("server[i] " + m);
                m.response(ir);
            });

            try {
                server.run(8080);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        Thread.sleep(2000);
    }

    @Before
    public void setupClient() {
        client = new Client();
    }

    @Test(timeout = 5000)
    public void testTag() throws InterruptedException {

        Counter counter = new Counter();

        client.connect("0.0.0.0", 8080);

        Client.Handler handler = (request, response) -> {
            Assert.assertEquals(request.getSeqNo(), response.getSeqNo());
            counter.done();
        };

        for (int i = 0; i < 10; i++) {
            counter.add();
            client.post(new InquiryRequest("123"), handler);
            counter.add();
            client.post(new TagRequest("A"), handler);
        }

        counter.check();
        client.shutdown();

        client.getChannel().closeFuture().sync();
    }
}
