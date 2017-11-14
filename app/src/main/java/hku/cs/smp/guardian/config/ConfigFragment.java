package hku.cs.smp.guardian.config;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import hku.cs.smp.guardian.R;

public class ConfigFragment extends Fragment {
    private View rootView;

    public static ConfigFragment newInstance() {
        return new ConfigFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView != null)
            return rootView;
        rootView = inflater.inflate(R.layout.frag_config, container, false);
        return rootView;
    }
}
