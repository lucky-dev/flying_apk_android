package com.flyingapk.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

import com.flyingapk.R;
import com.flyingapk.api.ApiHelper;
import com.flyingapk.fragments.BuildDetailsFragment;
import com.flyingapk.fragments.ListBuildsFragment;
import com.flyingapk.fragments.RegisterFragment;
import com.flyingapk.utils.FilesDownloaderHelper;
import com.flyingapk.utils.Tools;

import java.io.File;

public class ListBuildsActivity extends ActionBarActivity
        implements FilesDownloaderHelper.FilesDownloaderCallback {

    public static final String TAG = ListBuildsActivity.class.getSimpleName();

    public static final String INTENT_PARAM_APP_ID = "app_id";
    public static final String INTENT_PARAM_APP_NAME = "app_name";
    public static final String INTENT_PARAM_TYPE_BUILDS = "type_build";

    private static final String URI_HOST = "flyingapk";

    private final String[] TITLE_PAGES = {"All", "Debug", "Release"};
    private final int COUNT_PAGES = TITLE_PAGES.length;

    private ApiHelper mApiHelper;
    private int mAppId;
    private String mAppName = "";
    private String mTypeBuilds = "";
    private FilesDownloaderHelper mFilesDownloaderHelper;
    private ProgressDialog mProgressDialog;
    private boolean isDownloadingFile;
    private ListBuildsPagerAdapter mListBuildsPagerAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Tools.getAccessToken(this) == null) {
            Tools.navigateUpTo(this, new Intent(this, LoginActivity.class));
            return;
        }

        Intent intent = getIntent();
        if (intent != null) {
            String action = intent.getAction();
            // Get params of URL from an email
            if ((action != null) && (action.equals(Intent.ACTION_VIEW))) {
                Uri uri = intent.getData();

                // If the current host is not equal URI_HOST then finish this activity
                String host = uri.getHost();
                if ((host != null) && (!host.equals(URI_HOST))) {
                    finish();
                    return;
                }

                mAppId = Tools.getIntFromString(uri.getQueryParameter(INTENT_PARAM_APP_ID));
                mAppName = uri.getQueryParameter(INTENT_PARAM_APP_NAME);
                mTypeBuilds = uri.getQueryParameter(INTENT_PARAM_TYPE_BUILDS);
            } else {
                mAppId = getIntent().getIntExtra(INTENT_PARAM_APP_ID, 0);
                mAppName = getIntent().getStringExtra(INTENT_PARAM_APP_NAME);
            }
        }

        setContentView(R.layout.activity_list_builds);

        getSupportActionBar().setTitle(mAppName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mListBuildsPagerAdapter = new ListBuildsPagerAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mListBuildsPagerAdapter);

        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        for (int i = 0; i < COUNT_PAGES; i++) {
            getSupportActionBar().addTab(getSupportActionBar()
                    .newTab()
                    .setText(TITLE_PAGES[i])
                    .setTabListener(tabListener));
        }

        mViewPager.setOnPageChangeListener(mPageChangeListener);

        int currentPage = 0;
        if (mTypeBuilds.equals("debug")) {
            currentPage = 1;
        } else if (mTypeBuilds.equals("release")) {
            currentPage = 2;
        }

        mViewPager.setCurrentItem(currentPage);

        mApiHelper = new ApiHelper(this);
        mApiHelper.onCreate();

        mFilesDownloaderHelper = new FilesDownloaderHelper(this);
    }

    @Override
    public void onStart() {
        super.onStart();

        mFilesDownloaderHelper.setListener(this);
        mFilesDownloaderHelper.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();

        mFilesDownloaderHelper.onStop();
    }

    public void showBuildDetailsDialog(String version, String fixes) {
        BuildDetailsFragment buildDetailsFragment = BuildDetailsFragment.newInstance(version, fixes);
        buildDetailsFragment.show(getSupportFragmentManager(), RegisterFragment.TAG);
    }

    public ApiHelper getApiHelper() {
        return mApiHelper;
    }

    public FilesDownloaderHelper getFilesDownloaderHelper() {
        return mFilesDownloaderHelper;
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.downloading_file));
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setMax(100);
            mProgressDialog.show();
            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    if (isDownloadingFile) {
                        mFilesDownloaderHelper.cancel();
                        closeProgressDialog();
                    }
                }
            });
        }

        isDownloadingFile = true;
    }

    private void closeProgressDialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }

        isDownloadingFile = false;
    }

    @Override
    public void onStartDownloading() {
        showProgressDialog();
    }

    @Override
    public void onProgressDownloading(int progress) {
        if (mProgressDialog != null) {
            mProgressDialog.setProgress(progress);
        }
    }

    @Override
    public void onFinishDownloading(String pathToApp, boolean statusDownloading) {
        closeProgressDialog();

        if (statusDownloading) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(new File(pathToApp)), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    public class ListBuildsPagerAdapter extends FragmentStatePagerAdapter {
        public ListBuildsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: {
                    return ListBuildsFragment.newInstance(mAppId, ListBuildsFragment.TYPE_ALL_BUILDS);
                }

                case 1: {
                    return ListBuildsFragment.newInstance(mAppId, ListBuildsFragment.TYPE_DEBUG_BUILDS);
                }

                case 2: {
                    return ListBuildsFragment.newInstance(mAppId, ListBuildsFragment.TYPE_RELEASE_BUILDS);
                }

                default: {
                    return null;
                }
            }
        }

        @Override
        public int getCount() {
            return COUNT_PAGES;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TITLE_PAGES[position];
        }
    }

    private ActionBar.TabListener tabListener = new ActionBar.TabListener() {
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
            mViewPager.setCurrentItem(tab.getPosition());
        }

        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
        }

        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
        }
    };

    private ViewPager.SimpleOnPageChangeListener mPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            getSupportActionBar().setSelectedNavigationItem(position);
        }
    };

}