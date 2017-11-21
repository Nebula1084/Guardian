package hku.cs.smp.guardian.block;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import hku.cs.smp.guardian.R;
import hku.cs.smp.guardian.tag.TagResult;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.view.PieChartView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AlertActivity extends AppCompatActivity {

    private static final int MSG_ID_CHECK_TOP_ACTIVITY = 1;
    private static final long DELAY_INTERVAL = 2000;
    private PieChartView frequency;
    private TextView message;

    public static final String TAGS = "TAGS";
    public static final String UNKNOWN = "UNKNOWN";
    public static final String COUNT = "COUNT";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);

        TagResult tagResult = (TagResult) getIntent().getSerializableExtra(TAGS);
        Boolean unknown = getIntent().getBooleanExtra(UNKNOWN, true);
        if (tagResult == null)
            unknown = true;

        frequency = findViewById(R.id.frequency);
        message = findViewById(R.id.alert_message);

        PieChartData chartData = new PieChartData();
        if (!unknown) {
            List<SliceValue> values = new ArrayList<>();
            for (Map.Entry<String, Integer> tag : tagResult.getResult().entrySet()) {
                SliceValue value = new SliceValue();
                value.setColor(Color.RED);
                value.setValue(tag.getValue());
                value.setLabel(tag.getKey());
                values.add(value);
                chartData.setValues(values);
                chartData.setHasLabels(true);
            }
            frequency.setPieChartData(chartData);
            message.setText(getString(R.string.alert_message_tagged));
        } else {
            frequency.setVisibility(View.GONE);
            message.setText(getString(R.string.alert_message_unknown));
        }


        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        handler.sendEmptyMessageDelayed(MSG_ID_CHECK_TOP_ACTIVITY,
                DELAY_INTERVAL);
    }

    @Override
    public void finish() {
        super.finish();
    }

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == MSG_ID_CHECK_TOP_ACTIVITY) {
                int count = getIntent().getIntExtra(COUNT, 0);
                if (count <= 0)
                    return;
                Intent intent = new Intent();
                intent.putExtra(COUNT, count - 1);
                intent.setClass(AlertActivity.this, AlertActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
                startActivity(intent);
                finish();

            }
        }

    };
}
