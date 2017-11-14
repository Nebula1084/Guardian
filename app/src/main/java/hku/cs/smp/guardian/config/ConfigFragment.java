package hku.cs.smp.guardian.config;


import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.View;
import hku.cs.smp.guardian.R;

public class ConfigFragment extends PreferenceFragmentCompat {
    private View rootView;

    public static ConfigFragment newInstance() {
        return new ConfigFragment();
    }


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_guardian);
    }

    //    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        if (rootView != null)
//            return rootView;
//        rootView = inflater.inflate(R.layout.frag_config, container, false);
//        return rootView;
//    }
}
