package hku.cs.smp.guardian.block;

import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.CallLog;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import hku.cs.smp.guardian.R;
import hku.cs.smp.guardian.tag.TagHelper;
import hku.cs.smp.guardian.tag.UploadService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class BlockAdapter extends BaseAdapter {
    private TagHelper tagHelper;
    private ContactsHelper contactsHelper;
    private TelephonyManager telephonyManager;
    private Context context;
    private List<Item> items;

    public BlockAdapter(Context context) {
        this.context = context;
        tagHelper = TagHelper.getInstance();
        contactsHelper = ContactsHelper.getInstance();
        telephonyManager = (TelephonyManager) context.getSystemService(Service.TELEPHONY_SERVICE);
        items = new ArrayList<>();
    }

    private View newView(ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.block_item, parent, false);
        final ViewHolder viewHolder = new ViewHolder();
        viewHolder.number = view.findViewById(R.id.number);
        viewHolder.date = view.findViewById(R.id.date);
        viewHolder.tag = view.findViewById(R.id.tag);
        viewHolder.tag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createTagDialog(viewHolder);
            }
        });
        viewHolder.add = view.findViewById(R.id.add);
        viewHolder.add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewContact(viewHolder.number.getText().toString());
            }
        });
        viewHolder.version = 0L;

        view.setTag(viewHolder);
        return view;
    }

    private void bindView(View view, Item item) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        viewHolder.version += 1;
        new UpdateView(viewHolder, item).execute();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return items.get(position)._id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
            convertView = this.newView(parent);
        bindView(convertView, items.get(position));

        return convertView;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    class ViewHolder {

        TextView number;
        TextView date;
        Button tag;
        ImageButton add;
        Long version;
    }

    static class Item {
        Integer _id;
        String number;
        String date;
        Integer time;
        Integer type;
    }

    private class UpdateView extends AsyncTask<String, Void, Void> {

        private final ViewHolder viewHolder;
        private Item item;
        private String number;
        private String date;
        private Long version;
        private String name;
        private Boolean isTagged;

        UpdateView(ViewHolder viewHolder, Item item) {
            this.viewHolder = viewHolder;
            this.version = viewHolder.version;
            this.item = item;
            this.number = item.number;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                this.number = PhoneNumberUtils.formatNumber(number, telephonyManager.getSimCountryIso().toUpperCase());
            } else {
                this.number = PhoneNumberUtils.formatNumber(number);
            }

            Date date = new Date(Long.valueOf(item.date));
            DateFormat format;

            if (!DateUtils.isToday(date.getTime()))
                format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            else
                format = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            this.date = format.format(date);
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
                    viewHolder.add.setVisibility(View.INVISIBLE);
                } else {
                    viewHolder.number.setText(number);
                    if (isTagged)
                        viewHolder.tag.setVisibility(View.INVISIBLE);
                    else
                        viewHolder.tag.setVisibility(View.VISIBLE);
                    viewHolder.add.setVisibility(View.VISIBLE);
                }
                if (item.type == CallLog.Calls.MISSED_TYPE)
                    viewHolder.number.setTextColor(ContextCompat.getColor(context, R.color.missingCall));
                else
                    viewHolder.number.setTextColor(ContextCompat.getColor(context, R.color.normalCall));
                viewHolder.date.setText(date);
            }
        }
    }

    private void addNewContact(String number) {
        Intent intent = new Intent(Intent.ACTION_INSERT, Uri.withAppendedPath(Uri.parse("content://com.android.contacts"), "contacts"));
        intent.setType("vnd.android.cursor.dir/person");
        intent.setType("vnd.android.cursor.dir/contact");
        intent.setType("vnd.android.cursor.dir/raw_contact");
        intent.putExtra(android.provider.ContactsContract.Intents.Insert.PHONE, number);

        context.startActivity(intent);
    }

    void createTagDialog(final ViewHolder viewHolder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Tag a number")
                .setItems(R.array.tags, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        viewHolder.tag.setVisibility(View.INVISIBLE);
                        tagHelper.write(viewHolder.number.getText().toString(), context.getResources().getStringArray(R.array.tags)[which]);
                        context.startService(new Intent(context, UploadService.class));
                    }
                });
        builder.create().show();
    }
}
