package com.flyingapk.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.flyingapk.R;
import com.flyingapk.adapters.BuildsAdapter;
import com.flyingapk.api.ApiHelper;
import com.flyingapk.api.MapApiFunctions;
import com.flyingapk.api.wrappers.BaseResponse;
import com.flyingapk.api.wrappers.ListBuildsResponse;
import com.flyingapk.fragments.BuildDetailsFragment;
import com.flyingapk.fragments.RegisterFragment;
import com.flyingapk.models.Build;
import com.flyingapk.utils.FilesDownloaderHelper;
import com.flyingapk.utils.Tools;

import java.io.File;
import java.util.List;

public class ListBuildsActivity extends ActionBarActivity
        implements ApiHelper.ApiCallback, FilesDownloaderHelper.FilesDownloaderCallback, BuildsAdapter.BuildsAdapterListener {

    public static final String TAG = ListBuildsActivity.class.getSimpleName();

    public static final String INTENT_PARAM_APP_ID = "app_id";
    public static final String INTENT_PARAM_APP_NAME = "app_name";

    private ListView lvListBuilds;
    private BuildsAdapter mBuildsAdapter;
    private Menu mOptionsMenu;
    private ApiHelper mApiHelper;
    private int mAppId;
    private String mAppName = "";
    private BuildDetailsFragment mBuildDetailsFragment;
    private FilesDownloaderHelper mFilesDownloaderHelper;
    private ProgressDialog mProgressDialog;
    private boolean isDownloadingFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_apps);

        if (getIntent() != null) {
            mAppId = getIntent().getIntExtra(INTENT_PARAM_APP_ID, 0);
            mAppName = getIntent().getStringExtra(INTENT_PARAM_APP_NAME);
        }

        getSupportActionBar().setTitle(mAppName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mApiHelper = new ApiHelper(this);
        mApiHelper.onCreate();

        mFilesDownloaderHelper = new FilesDownloaderHelper(this);

        lvListBuilds = (ListView) findViewById(R.id.lv_list_apps);
        mBuildsAdapter = new BuildsAdapter(Tools.getLayoutInflater(this));
        mBuildsAdapter.setListener(this);
        lvListBuilds.setAdapter(mBuildsAdapter);
        lvListBuilds.setOnItemClickListener(mClickOnBuild);
    }

    @Override
    public void onStart() {
        super.onStart();

        mApiHelper.addListener(this);
        mApiHelper.getListBuilds(mAppId);

        mFilesDownloaderHelper.setListener(this);
        mFilesDownloaderHelper.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();

        mApiHelper.removeListener(this);

        mFilesDownloaderHelper.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list_builds, menu);

        mOptionsMenu = menu;

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            mApiHelper.getListBuilds(mAppId);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setRefreshMenuItem(boolean refreshing) {
        if (mOptionsMenu == null) {
            return;
        }

        final MenuItem refreshItem = mOptionsMenu.findItem(R.id.action_refresh);
        if (refreshItem != null) {
            if (refreshing) {
                MenuItemCompat.setActionView(refreshItem, R.layout.actionbar_indeterminate_progress);
            } else {
                MenuItemCompat.setActionView(refreshItem, null);
            }
        }
    }

    private ListView.OnItemClickListener mClickOnBuild = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Build build = mBuildsAdapter.getItem(position);

            mBuildDetailsFragment = BuildDetailsFragment.newInstance(build.getVersion(), build.getFixes());
            mBuildDetailsFragment.show(getSupportFragmentManager(), RegisterFragment.TAG);
        }
    };

    @Override
    public void onDownloadBuild(int position) {
        Build build = mBuildsAdapter.getItem(position);

        mFilesDownloaderHelper.getFile(build.getFileName(), build.getFileChecksum());
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
    public void onStart(int code, String tag) {
        if (code == MapApiFunctions.Request.Command.LIST_BUILDS) {
            setRefreshMenuItem(true);
        }
    }

    @Override
    public void onFinish(int code, BaseResponse struct, String tag) {
        if (code == MapApiFunctions.Response.Command.LIST_BUILDS) {
            setRefreshMenuItem(false);

            ListBuildsResponse result = (ListBuildsResponse) struct;

            if (result.getCode() == 200) {
                mBuildsAdapter.clear();

                List<Build> listBuilds = result.getListBuilds();
                for (Build item : listBuilds) {
                    mBuildsAdapter.addItem(item);
                }

                mBuildsAdapter.notifyDataSetChanged();
            } else {
                List<String> listErrors = result.getErrors();
                StringBuffer errors = new StringBuffer();
                for (int i = 0; i < listErrors.size(); i++) {
                    errors.append(listErrors.get(i));

                    if ((i < listErrors.size() - 1) || (listErrors.size() > 1)) {
                        errors.append("\n");
                    }
                }

                Tools.showToast(this, errors.toString(), Toast.LENGTH_LONG);
            }
        }
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

}