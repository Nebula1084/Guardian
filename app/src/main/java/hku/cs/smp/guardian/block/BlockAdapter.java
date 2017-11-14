package hku.cs.smp.guardian.block;

import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import hku.cs.smp.guardian.R;
import hku.cs.smp.guardian.common.connection.Client;
import hku.cs.smp.guardian.common.protocol.Request;
import hku.cs.smp.guardian.common.protocol.Response;
import hku.cs.smp.guardian.common.protocol.TagRequest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BlockAdapter extends CursorAdapter {
    private Client client;

    public BlockAdapter(Context context, Cursor c) {
        super(context, c, false);
        client = new Client();
        client.connect("192.168.0.105", 8000);
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
                client.post(new TagRequest(viewHolder.number.getText().toString(), "12"),
                        new Client.Handler() {
                            @Override
                            public void handle(Request request, Response response) {
                                Log.i("S", "Success!!!");
                            }
                        });
            }
        });
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
        Button tag;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        client.shutdown();
    }
}
