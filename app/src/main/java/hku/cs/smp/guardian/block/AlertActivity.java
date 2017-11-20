package hku.cs.smp.guardian.block;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import hku.cs.smp.guardian.R;

public class AlertActivity extends AppCompatActivity {

    private static final int MSG_ID_CHECK_TOP_ACTIVITY = 1;
    private static final long DELAY_INTERVAL = 2000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);

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
                int count = getIntent().getIntExtra("count", 0);
                if (count <= 0)
                    return;
                Intent intent = new Intent();
                intent.putExtra("count", count - 1);
                intent.setClass(AlertActivity.this, AlertActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
                startActivity(intent);
                finish();

            }
        }

    };
}
