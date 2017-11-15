package hku.cs.smp.guardian.block;

import android.app.Service;
import android.content.Context;
import android.database.Cursor;
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
import hku.cs.smp.guardian.buffer.TagHelper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BlockAdapter extends CursorAdapter {
    private TagHelper tagHelper;
    private ContactsHelper contactsHelper;
    private TelephonyManager telephonyManager;

    public BlockAdapter(Context context, Cursor c) {
        super(context, c, false);
        tagHelper = TagHelper.getInstance();
        contactsHelper = ContactsHelper.getInstance();
        telephonyManager = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.block_item, parent, false);
        final ViewHolder viewHolder = new ViewHolder();
        viewHolder.number = view.findViewById(R.id.number);
        viewHolder.date = view.findViewById(R.id.date);
        viewHolder.tag = view.findViewById(R.id.tag);
        viewHolder.tag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tagHelper.write(viewHolder.number.getText().toString(), "12");
            }
        });

        String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            number = PhoneNumberUtils.formatNumber(number, telephonyManager.getSimCountryIso().toUpperCase());
        } else {
            number = PhoneNumberUtils.formatNumber(number);
        }

        String date = cursor.getString(cursor.getColumnIndex(CallLog.Calls.DATE));
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String name = contactsHelper.findNameByNumber(number);
        if (name != null) {
            viewHolder.number.setText(name);
            viewHolder.tag.setVisibility(View.INVISIBLE);
        } else
            viewHolder.number.setText(number);
        viewHolder.date.setText(format.format(new Date(Long.valueOf(date))));

        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

    }

    class ViewHolder {
        TextView number;
        TextView date;
        Button tag;
    }


}
