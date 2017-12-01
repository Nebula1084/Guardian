package hku.cs.smp.guardian.block;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.CallLog;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import hku.cs.smp.guardian.R;

import java.util.ArrayList;
import java.util.List;

public class BlockFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private View rootView;
    private ListView blockList;
    private BlockAdapter adapter;

    public static BlockFragment newInstance() {
        return new BlockFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getLoaderManager().initLoader(0, null, this);
        if (rootView != null)
            return rootView;
        rootView = inflater.inflate(R.layout.frag_block, container, false);
        blockList = rootView.findViewById(R.id.block_list);
        adapter = new BlockAdapter(getContext());
        blockList.setAdapter(adapter);
        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this.getActivity(),
                CallLog.Calls.CONTENT_URI,
                new String[]{
                        CallLog.Calls._ID,
                        CallLog.Calls.NUMBER,
                        CallLog.Calls.DATE,
                        CallLog.Calls.TYPE
                },
                CallLog.Calls.TYPE + " != ?",
                new String[]{
                        String.valueOf(CallLog.Calls.OUTGOING_TYPE)
                },
                CallLog.Calls.DEFAULT_SORT_ORDER);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        List<BlockAdapter.Item> items = new ArrayList<>();
        data.moveToFirst();
        do {
            Integer _id = data.getInt(data.getColumnIndex(CallLog.Calls._ID));
            String number = data.getString(data.getColumnIndex(CallLog.Calls.NUMBER));
            String date = data.getString(data.getColumnIndex(CallLog.Calls.DATE));
            Integer type = data.getInt(data.getColumnIndex(CallLog.Calls.TYPE));
            if (items.size() == 0 || !items.get(items.size() - 1).number.equals(number)) {
                BlockAdapter.Item item = new BlockAdapter.Item();
                item._id = _id;
                item.number = number;
                item.date = date;
                item.type = type;
                item.time = 0;
                items.add(item);
            } else {
                items.get(items.size() - 1).time += 1;
            }

        } while (data.moveToNext());

        adapter.setItems(items);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
