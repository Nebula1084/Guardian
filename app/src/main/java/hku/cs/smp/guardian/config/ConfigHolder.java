package hku.cs.smp.guardian.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

public class ConfigHolder {

    private SharedPreferences preferences;

    public ConfigHolder(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean shouldAlertUnknown() {
        return preferences.getBoolean("alert_unknown", false);
    }

    public boolean shouldAlertHighFrequency() {
        return preferences.getBoolean("alert_high_frequency", false);
    }

    public boolean isBlockMode() {
        return preferences.getBoolean("block_mode", false);
    }

    public boolean shouldBlockUnknown() {
        return preferences.getBoolean("block_unknown", false);
    }


    public boolean shouldBlockHighFrequency() {
        return preferences.getBoolean("block_high_frequency", false);
    }

    public boolean isBlockRepeatedCall() {
        return preferences.getBoolean("block_repeated_call", false);
    }
}
