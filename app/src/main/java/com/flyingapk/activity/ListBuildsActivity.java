package com.flyingapk.activity;

import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import com.flyingapk.R;
import com.flyingapk.api.ApiHelper;
import com.flyingapk.api.MapApiFunctions;
import com.flyingapk.api.wrappers.BaseResponse;
import com.flyingapk.api.wrappers.ListBuildsResponse;
import com.flyingapk.models.AndroidApp;
import com.flyingapk.models.Build;
import com.flyingapk.utils.Tools;

import java.util.List;

public class ListBuildsActivity extends ActionBarActivity implements ApiHelper.ApiCallback {

    private ListView lvListBuilds;
    private BuildsAdapter mBuildsAdapter;
    private Menu mOptionsMenu;
    private ApiHelper mApiHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_apps);

        mApiHelper = new ApiHelper(getApplicationContext());
        mApiHelper.onCreate();

        lvListBuilds = (ListView) findViewById(R.id.lv_list_apps);
        mBuildsAdapter = new BuildsAdapter(Tools.getLayoutInflater(this));
        lvListBuilds.setAdapter(mBuildsAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();

        mApiHelper.addListener(this);
        mApiHelper.getListBuilds();
    }

    @Override
    public void onStop() {
        super.onStop();

        mApiHelper.removeListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list_apps, menu);

        mOptionsMenu = menu;

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            mApiHelper.getListBuilds();
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

    @Override
    public void onStart(int code, String tag) {
        if (code == MapApiFunctions.Request.Command.LIST_APPS) {
            setRefreshMenuItem(true);
        }
    }

    @Override
    public void onFinish(int code, BaseResponse struct, String tag) {
        if (code == MapApiFunctions.Response.Command.LIST_APPS) {
            setRefreshMenuItem(false);

            ListBuildsResponse result = (ListBuildsResponse) struct;

            if (result.getCode() == 200) {
                mBuildsAdapter.clear();

                List<Build> listBuilds = result.getListBuilds();
                for (AndroidApp item : listBuilds) {
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
}