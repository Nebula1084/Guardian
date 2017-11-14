package hku.cs.smp.guardian.block;

import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import hku.cs.smp.guardian.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BlockAdapter extends CursorAdapter {
    public BlockAdapter(Context context, Cursor c) {
        super(context, c, false);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.block_item, parent, false);
        ViewHolder viewHolder = new ViewHolder();
        viewHolder.number = view.findViewById(R.id.number);
        viewHolder.date = view.findViewById(R.id.date);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
        String date = cursor.getString(cursor.getColumnIndex(CallLog.Calls.DATE));
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        viewHolder.number.setText(number);
        viewHolder.date.setText(format.format(new Date(Long.valueOf(date))));
    }

    class ViewHolder {
        TextView number;
        TextView date;
    }
}
