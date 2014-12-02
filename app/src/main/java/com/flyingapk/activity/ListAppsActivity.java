package com.flyingapk.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.flyingapk.R;
import com.flyingapk.adapters.AppsAdapter;
import com.flyingapk.api.ApiHelper;
import com.flyingapk.api.MapApiFunctions;
import com.flyingapk.api.wrappers.BaseResponse;
import com.flyingapk.api.wrappers.ListAndroidAppsResponse;
import com.flyingapk.api.wrappers.UserLogoutResponse;
import com.flyingapk.fragments.InfoAboutNewAppFragment;
import com.flyingapk.models.AndroidApp;
import com.flyingapk.utils.Tools;
import com.flyingapk.utils.UpdatingManagerHelper;

import java.io.File;
import java.util.List;

public class ListAppsActivity extends ActionBarActivity implements
        ApiHelper.ApiCallback,
        UpdatingManagerHelper.UpdatingManagerCallback,
        InfoAboutNewAppFragment.OnDialogInfoAboutNewAppListener {

    private ListView lvListApps;
    private AppsAdapter mAppsAdapter;
    private Menu mOptionsMenu;
    private ApiHelper mApiHelper;
    private UpdatingManagerHelper mUpdatingManagerHelper;
    private InfoAboutNewAppFragment mInfoAboutNewAppDialog;
    private String mFileNewApp;
    private String mChecksumFileNewApp;
    private ProgressDialog mProgressDialog;
    private ProgressDialog mProgressDialogLogout;
    private boolean isDownloadingFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_apps);

        mApiHelper = new ApiHelper(this);
        mApiHelper.onCreate();

        mUpdatingManagerHelper = new UpdatingManagerHelper(this);
        mUpdatingManagerHelper.setListener(this);

        lvListApps = (ListView) findViewById(R.id.lv_list_apps);
        mAppsAdapter = new AppsAdapter(Tools.getLayoutInflater(this));
        lvListApps.setAdapter(mAppsAdapter);
        lvListApps.setOnItemClickListener(mClickOnApp);
    }

    @Override
    public void onStart() {
        super.onStart();

        mUpdatingManagerHelper.onStart();

        mUpdatingManagerHelper.checkNewVersion();

        mApiHelper.addListener(this);
        mApiHelper.getListApps();
    }

    @Override
    public void onStop() {
        super.onStop();

        mUpdatingManagerHelper.onStop();

        mApiHelper.removeListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_list_apps, menu);

        mOptionsMenu = menu;

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh : {
                mApiHelper.getListApps();
                return true;
            }

            case R.id.action_about : {
                Tools.createOkDialog(this,
                        getString(R.string.about),
                        String.format(getString(R.string.info_about), Tools.getVersionNameApp(this))).show();
                return true;
            }

            case R.id.action_logout : {
                mApiHelper.logout();
                return true;
            }
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

    private ListView.OnItemClickListener mClickOnApp = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            AndroidApp androidApp = mAppsAdapter.getItem(position);

            Intent intent = new Intent(ListAppsActivity.this, ListBuildsActivity.class);
            intent.putExtra(ListBuildsActivity.INTENT_PARAM_APP_ID, androidApp.getId());
            intent.putExtra(ListBuildsActivity.INTENT_PARAM_APP_NAME, androidApp.getName());
            startActivity(intent);
        }
    };

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.updating_app));
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setMax(100);
            mProgressDialog.show();
            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    if (isDownloadingFile) {
                        mUpdatingManagerHelper.cancel();
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
    public void onUpdateApp() {
        mUpdatingManagerHelper.update(mFileNewApp, mChecksumFileNewApp);
    }

    @Override
    public void onStart(int code, String tag) {
        if (code == MapApiFunctions.Request.Command.LIST_APPS) {
            setRefreshMenuItem(true);
        } else if (code == MapApiFunctions.Request.Command.LOGOUT) {
            if (mProgressDialogLogout == null) {
                mProgressDialogLogout = Tools.getIndeterminateProgressDialog(this);
            }
        }
    }

    @Override
    public void onFinish(int code, BaseResponse struct, String tag) {
        if (code == MapApiFunctions.Response.Command.LIST_APPS) {
            setRefreshMenuItem(false);

            ListAndroidAppsResponse result = (ListAndroidAppsResponse) struct;

            if (result.getCode() == 200) {
                mAppsAdapter.clear();

                List<AndroidApp> listAndroidApps = result.getListAndroidApps();
                for (AndroidApp item : listAndroidApps) {
                    mAppsAdapter.addItem(item);
                }

                mAppsAdapter.notifyDataSetChanged();
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
        } else if (code == MapApiFunctions.Response.Command.LOGOUT) {
            if (mProgressDialogLogout != null) {
                mProgressDialogLogout.dismiss();
                mProgressDialogLogout = null;
            }

            UserLogoutResponse result = (UserLogoutResponse) struct;

            if (result.getCode() == 200) {
                Tools.clearAccessToken(this);
                Tools.navigateUpTo(this, new Intent(this, LoginActivity.class));
            } else {
                List<String> listErrors = result.getErrors();
                StringBuilder errors = new StringBuilder();
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
    public void onStartCheckingNewApp() {
    }

    @Override
    public void onStopCheckingNewApp(boolean isNewVersionApp, String versionApp, String whatsNew, String file, String checksumFile) {
        if (isNewVersionApp) {
            mFileNewApp = file;
            mChecksumFileNewApp = checksumFile;

            mInfoAboutNewAppDialog = InfoAboutNewAppFragment.newInstance(versionApp, whatsNew);
            mInfoAboutNewAppDialog.setListener(this);
            mInfoAboutNewAppDialog.show(getSupportFragmentManager(), InfoAboutNewAppFragment.TAG);
        }
    }

    @Override
    public void onStartUpdating() {
        showProgressDialog();
    }

    @Override
    public void onProgressUpdating(int progress) {
        if (mProgressDialog != null) {
            mProgressDialog.setProgress(progress);
        }
    }

    @Override
    public void onFinishUpdating(String pathToApp, boolean statusDownloading) {
        closeProgressDialog();

        if (statusDownloading) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(new File(pathToApp)), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

}
