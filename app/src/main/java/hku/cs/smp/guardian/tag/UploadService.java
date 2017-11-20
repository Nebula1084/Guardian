package hku.cs.smp.guardian.tag;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import hku.cs.smp.guardian.common.Counter;
import hku.cs.smp.guardian.common.connection.Client;
import hku.cs.smp.guardian.common.protocol.Request;
import hku.cs.smp.guardian.common.protocol.Response;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static hku.cs.smp.guardian.MainActivity.HOST;
import static hku.cs.smp.guardian.MainActivity.PORT;

public class UploadService extends Service {

    private Client client;
    private TagHelper tagHelper;
    private BlockingQueue<Boolean> works;
    private static Thread uploadThread;
    private Counter counter = new Counter();
    private ChannelFutureListener closeListener = new ChannelFutureListener() {
        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
            counter.reset();
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        client = new Client();
        tagHelper = TagHelper.getInstance();
        works = new LinkedBlockingQueue<>();
        if (uploadThread == null) {
            uploadThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            if (!works.take())
                                break;
                            if (!client.isConnected())
                                client.connect(HOST, PORT, closeListener);
                            upload();
                            client.shutdown();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }
            });
            uploadThread.start();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (works.isEmpty())
            works.add(true);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            works.add(false);
            client.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void upload() throws InterruptedException {
        while (true) {
            List<TagCommand> commands = tagHelper.getByUploaded(false);
            if (commands.isEmpty())
                break;
            for (final TagCommand command : commands) {
                counter.add();
                client.post(command.toTagRequest(), new Client.Handler() {
                    @Override
                    public void handle(Request request, Response response) {
                        command.uploaded = true;
                        tagHelper.update(command);
                        counter.done();
                    }
                });
            }
            counter.check();
        }
    }
}
