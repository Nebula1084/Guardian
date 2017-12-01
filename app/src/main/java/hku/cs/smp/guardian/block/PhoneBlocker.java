package hku.cs.smp.guardian.block;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.ITelephony;
import hku.cs.smp.guardian.common.Counter;
import hku.cs.smp.guardian.common.connection.Client;
import hku.cs.smp.guardian.common.protocol.InquiryRequest;
import hku.cs.smp.guardian.common.protocol.InquiryResponse;
import hku.cs.smp.guardian.common.protocol.Request;
import hku.cs.smp.guardian.common.protocol.Response;
import hku.cs.smp.guardian.config.ConfigHolder;
import hku.cs.smp.guardian.tag.TagResult;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static hku.cs.smp.guardian.MainActivity.HOST;
import static hku.cs.smp.guardian.MainActivity.PORT;
import static hku.cs.smp.guardian.block.AlertActivity.*;

public class PhoneBlocker extends BroadcastReceiver {
    private TelephonyManager telephonyManager;
    private ConfigHolder configHolder;
    private Context context;
    private Counter counter = new Counter();
    private TagResult result = null;
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
                String rawNumber = number;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    number = PhoneNumberUtils.formatNumber(number, telephonyManager.getSimCountryIso().toUpperCase());
                } else {
                    number = PhoneNumberUtils.formatNumber(number);
                }

                if (configHolder.isBlockMode()) {
                    if (configHolder.shouldBlockUnknown() && isUnknown(number))
                        if (block(rawNumber)) return;
                    if (configHolder.shouldBlockHighFrequency() && isHighFrequency(number))
                        if (block(rawNumber)) return;
                }

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
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(COUNT, 1);
        intent.putExtra(UNKNOWN, false);
        intent.putExtra(TAGS, result);

        context.startActivity(intent);
    }


    private boolean block(String rawNumber) {
        if (configHolder.isBlockRepeatedCall() && isRecent(rawNumber))
            return true;
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

    private boolean isRecent(String rawNumber) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, -5);
        Cursor cursor = context.getContentResolver().query(
                CallLog.Calls.CONTENT_URI,
                new String[]{
                        CallLog.Calls._ID,
                        CallLog.Calls.CACHED_NAME,
                        CallLog.Calls.NUMBER,
                        CallLog.Calls.DATE,
                },
                CallLog.Calls.NUMBER + " = ? AND " +
                        CallLog.Calls.DATE + " > ?",
                new String[]{
                        rawNumber,
                        String.valueOf(calendar.getTimeInMillis())
                },
                CallLog.Calls.DEFAULT_SORT_ORDER
        );
        boolean ret = cursor.moveToFirst();
        cursor.close();
        return ret;
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

        TagResult getResult() {
            return new TagResult(result);
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
            if (!handler.getResult().isEmpty()) {
                this.result = handler.getResult();
                return true;
            }
        } catch (Exception e) {
            counter.reset();
            e.printStackTrace();
        }
        return false;
    }
}