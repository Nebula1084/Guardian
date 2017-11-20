package hku.cs.smp.guardian.block;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.android.internal.telephony.ITelephony;
import hku.cs.smp.guardian.common.Counter;
import hku.cs.smp.guardian.common.connection.Client;
import hku.cs.smp.guardian.common.protocol.InquiryRequest;
import hku.cs.smp.guardian.common.protocol.InquiryResponse;
import hku.cs.smp.guardian.common.protocol.Request;
import hku.cs.smp.guardian.common.protocol.Response;
import hku.cs.smp.guardian.config.ConfigHolder;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static hku.cs.smp.guardian.MainActivity.HOST;
import static hku.cs.smp.guardian.MainActivity.PORT;

public class PhoneBlocker extends BroadcastReceiver {
    private TelephonyManager telephonyManager;
    private ConfigHolder configHolder;
    private Context context;
    private Counter counter = new Counter();
    private ChannelFutureListener closeListener = new ChannelFutureListener() {
        @Override
        public void operationComplete(ChannelFuture future) throws Exception {
            counter.reset();
        }
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        telephonyManager = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
        configHolder = new ConfigHolder(context);
        this.context = context;
        switch (telephonyManager.getCallState()) {
            case TelephonyManager.CALL_STATE_RINGING:
                String number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                if (configHolder.isBlockMode()) {
                    if (configHolder.shouldBlockUnknown() && isUnknown(number))
                        if (block()) return;
                    if (configHolder.shouldBlockHighFrequency() && isHighFrequency(number))
                        if (block()) return;
                }

                Log.i("Block unknown", String.valueOf(configHolder.shouldAlertUnknown()));
                if (configHolder.shouldAlertUnknown() && isUnknown(number)) {
                    alert();
                    return;
                }

                if (configHolder.shouldAlertHighFrequency() && isHighFrequency(number)) {
                    alert();
                    return;
                }

                break;
            case TelephonyManager.CALL_STATE_IDLE:
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                break;
        }
    }


    private void alert() {
        Intent intent = new Intent(context, AlertActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        context.startActivity(intent);
    }


    private boolean block() {
        Class<TelephonyManager> c = TelephonyManager.class;
        Method getITelephonyMethod;
        try {
            getITelephonyMethod = c.getDeclaredMethod("getITelephony", (Class[]) null);
            getITelephonyMethod.setAccessible(true);
            ITelephony iTelephony = (ITelephony) getITelephonyMethod.invoke(telephonyManager, (Object[]) null);
            iTelephony.endCall();
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean isUnknown(String number) {
        Cursor cursor = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER,
                },
                ContactsContract.CommonDataKinds.Phone.NUMBER + " = ?",
                new String[]{number},
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " DESC"
        );
        boolean ret = !cursor.moveToFirst();
        Log.i("isUnknown", String.valueOf(ret));
        cursor.close();
        return ret;
    }

    class InquiryHandler implements Client.Handler {
        private Map<String, Integer> result = new HashMap<>();

        @Override
        public void handle(Request request, Response response) {
            InquiryResponse ir = (InquiryResponse) response;
            result = ir.getResult();
            counter.done();
        }

        public Map<String, Integer> getResult() {
            return result;
        }
    }

    private boolean isHighFrequency(String number) {
        Client client = new Client();

        try {
            client.connect(HOST, PORT, closeListener);
            counter.add();
            InquiryHandler handler = new InquiryHandler();
            client.post(new InquiryRequest(number), handler);
            client.shutdown();
            counter.check();
            if (!handler.getResult().isEmpty())
                return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}