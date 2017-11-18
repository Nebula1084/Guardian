package hku.cs.smp.guardian.common.connection;

import hku.cs.smp.guardian.common.protocol.*;
import io.reactivex.functions.Consumer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
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

        new Thread(new Runnable() {
            @Override
            public void run() {
                Server server = new Server();
                server.ofType(TagRequest.class).subscribe(new Consumer<TagRequest>() {
                    @Override
                    public void accept(TagRequest m) throws Exception {
                        logger.info("server[t] " + m);
                        m.response(m.getResponse());
                    }
                });
                server.ofType(InquiryRequest.class).subscribe(new Consumer<InquiryRequest>() {
                    @Override
                    public void accept(InquiryRequest m) throws Exception {

                        InquiryResponse ir = m.getResponse();
                        Map<String, Integer> result=new HashMap<>();
                        result.put("12", 12);
                        result.put("1a2", 1221);
                        ir.setResult(result);
                        logger.info("server[i] " + m);
                        m.response(ir);
                    }
                });

                try {
                    server.run(8080);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
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

        final Counter counter = new Counter();

        client.connect("0.0.0.0", 8080);

        Client.Handler handler = new Client.Handler() {
            @Override
            public void handle(Request request, Response response) {
                Assert.assertEquals(request.getSeqNo(), response.getSeqNo());
                counter.done();
            }
        };

        for (int i = 0; i < 10; i++) {
            counter.add();
            client.post(new InquiryRequest("123"), handler);
            counter.add();
            client.post(new TagRequest("123" + i, "A"), handler);
        }

        counter.check();
        client.shutdown();

        client.getChannel().closeFuture().sync();
    }
}
