package hku.cs.smp.guardian;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import hku.cs.smp.guardian.block.BlockFragment;
import hku.cs.smp.guardian.block.ContactsHelper;
import hku.cs.smp.guardian.tag.TagHelper;
import hku.cs.smp.guardian.config.ConfigFragment;
import hku.cs.smp.guardian.tag.UploadService;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_CALL_LOG;
import static android.Manifest.permission.READ_CONTACTS;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView navigation;
    private ViewPager viewPager;
    private PagerAdapter pagerAdapter;

    private static final int BLOCK_LIST_REQUEST_CODE = 11;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.container);

        navigation = findViewById(R.id.navigation);

        mayRequestPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startService(new Intent(this, UploadService.class));
    }

    private void mayRequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(INTERNET) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{READ_CALL_LOG, INTERNET, READ_CONTACTS}, BLOCK_LIST_REQUEST_CODE);
                return;
            }
        }

        initFragments();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case BLOCK_LIST_REQUEST_CODE:
                if (grantResults.length >= 3) {
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            Log.i("RR", "Really?");
                            finish();
                            return;
                        }
                    }
                } else {
                    Log.i("R", "Really?");
                    finish();
                    return;
                }
                initFragments();
                break;
        }
    }

    private void initFragments() {
        TagHelper.init(getApplicationContext());
        ContactsHelper.init(getApplicationContext());
        pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_block:
                        viewPager.setCurrentItem(0);
                        return true;
                    case R.id.navigation_config:
                        viewPager.setCurrentItem(1);
                        return true;
                }
                return false;
            }
        });
    }

    public class PagerAdapter extends FragmentPagerAdapter {
        List<Fragment> fragments;

        PagerAdapter(FragmentManager fm) {
            super(fm);
            fragments = new ArrayList<>();
            fragments.add(BlockFragment.newInstance());
            fragments.add(ConfigFragment.newInstance());

        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }
}
