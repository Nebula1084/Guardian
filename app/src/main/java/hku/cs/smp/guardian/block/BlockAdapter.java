package hku.cs.smp.guardian.block;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.CallLog;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import hku.cs.smp.guardian.R;
import hku.cs.smp.guardian.tag.TagHelper;
import hku.cs.smp.guardian.tag.UploadService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class BlockAdapter extends CursorAdapter {
    private TagHelper tagHelper;
    private ContactsHelper contactsHelper;
    private TelephonyManager telephonyManager;
    private Context context;

    public BlockAdapter(Context context, Cursor c) {
        super(context, c, false);
        this.context = context;
        tagHelper = TagHelper.getInstance();
        contactsHelper = ContactsHelper.getInstance();
        telephonyManager = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
    }

    @Override
    public View newView(final Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.block_item, parent, false);
        final ViewHolder viewHolder = new ViewHolder();
        viewHolder.number = view.findViewById(R.id.number);
        viewHolder.date = view.findViewById(R.id.date);
        viewHolder.tag = view.findViewById(R.id.tag);
        viewHolder.tag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolder.tag.setVisibility(View.INVISIBLE);
                tagHelper.write(viewHolder.number.getText().toString(), "12");
                context.startService(new Intent(context, UploadService.class));
            }
        });
        viewHolder.version = 0L;

        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            number = PhoneNumberUtils.formatNumber(number, telephonyManager.getSimCountryIso().toUpperCase());
        } else {
            number = PhoneNumberUtils.formatNumber(number);
        }

        String date = cursor.getString(cursor.getColumnIndex(CallLog.Calls.DATE));
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        date = format.format(new Date(Long.valueOf(date)));

        viewHolder.version += 1;
        new UpdateView(viewHolder, number, date).execute();
    }

    class ViewHolder {
        TextView number;
        TextView date;
        Button tag;
        Long version;
    }

    private class UpdateView extends AsyncTask<String, Void, Void> {

        private final ViewHolder viewHolder;
        private final String number;
        private final String date;
        private final Long version;
        private String name;
        private Boolean isTagged;

        UpdateView(ViewHolder viewHolder, String number, String date) {
            this.viewHolder = viewHolder;
            this.number = number;
            this.date = date;
            this.version = viewHolder.version;
        }

        @Override
        protected Void doInBackground(String... voids) {
            name = contactsHelper.findNameByNumber(number);
            isTagged = tagHelper.isTagged(number);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            synchronized (viewHolder) {
                if (!Objects.equals(this.version, viewHolder.version))
                    return;
                if (name != null) {
                    viewHolder.number.setText(name);
                    viewHolder.tag.setVisibility(View.INVISIBLE);
                } else {
                    viewHolder.number.setText(number);
                    if (isTagged)
                        viewHolder.tag.setVisibility(View.INVISIBLE);
                    else
                        viewHolder.tag.setVisibility(View.VISIBLE);
                }
                viewHolder.date.setText(date);
            }
        }
    }

}
