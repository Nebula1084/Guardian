package hku.cs.smp.guardian.tag;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import hku.cs.smp.guardian.common.connection.Client;
import hku.cs.smp.guardian.common.protocol.Request;
import hku.cs.smp.guardian.common.protocol.Response;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class UploadService extends Service {
    private Client client;
    private TagHelper tagHelper;
    private BlockingQueue<Boolean> works;

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
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        if (works.take())
                            break;
                        List<TagCommand> commands = tagHelper.getByUploaded(false);
                        client.connect("", 8000);
                        for (final TagCommand command : commands) {
                            client.post(command.toTagRequest(), new Client.Handler() {
                                @Override
                                public void handle(Request request, Response response) {
                                    command.uploaded = true;
                                    tagHelper.update(command);
                                }
                            });
                        }

                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        List<TagCommand> commands = tagHelper.getByUploaded(false);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            client.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
