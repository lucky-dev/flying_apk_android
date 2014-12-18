package com.flyingapk.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.view.MenuItemCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.flyingapk.R;
import com.flyingapk.activity.ListBuildsActivity;
import com.flyingapk.adapters.BuildsAdapter;
import com.flyingapk.api.ApiHelper;
import com.flyingapk.api.MapApiFunctions;
import com.flyingapk.api.wrappers.BaseResponse;
import com.flyingapk.api.wrappers.ListBuildsResponse;
import com.flyingapk.models.Build;
import com.flyingapk.utils.FilesDownloaderHelper;
import com.flyingapk.utils.Tools;

import java.util.List;

public class ListBuildsFragment extends ListFragment
        implements ApiHelper.ApiCallback, BuildsAdapter.BuildsAdapterListener {

    public static final String TAG = "ListBuildsFragment";

    public static final int TYPE_ALL_BUILDS = 1;
    public static final int TYPE_DEBUG_BUILDS = 2;
    public static final int TYPE_RELEASE_BUILDS = 3;

    private static final String APP_ID = "app_id";
    private static final String TYPE = "type";

    private Menu mOptionsMenu;
    private int mAppId;
    private String mType = "";
    private BuildsAdapter mBuildsAdapter;
    private String mTagApi = TAG;

    public static ListBuildsFragment newInstance(int appId, int type) {
        ListBuildsFragment fragment = new ListBuildsFragment();
        Bundle data = new Bundle();
        data.putInt(APP_ID, appId);
        data.putInt(TYPE, type);
        fragment.setArguments(data);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        if (getArguments() != null) {
            mAppId = getArguments().getInt(APP_ID, 0);

            int type = getArguments().getInt(TYPE);

            if (type == TYPE_DEBUG_BUILDS) {
                mType = "debug";
            } else if (type == TYPE_RELEASE_BUILDS) {
                mType = "release";
            }

            mTagApi = TAG + "_" + type;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setRetainInstance(true);

        mBuildsAdapter = new BuildsAdapter(Tools.getLayoutInflater(getActivity()));
        mBuildsAdapter.setListener(this);
        getListView().setAdapter(mBuildsAdapter);
        getListView().setOnItemClickListener(mClickOnBuild);

        setListAdapter(mBuildsAdapter);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        mOptionsMenu = menu;

        inflater.inflate(R.menu.menu_list_builds, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            getApiHelper().getListBuilds(mAppId, mType, mTagApi);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (getApiHelper() != null) {
            getApiHelper().addListener(this);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        getApiHelper().getListBuilds(mAppId, mType, mTagApi);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (getApiHelper() != null) {
            getApiHelper().removeListener(this);
        }
    }

    @Override
    public void onDownloadBuild(int position) {
        Build build = mBuildsAdapter.getItem(position);

        if (getFilesDownloaderHelper() != null) {
            getFilesDownloaderHelper().getFile(build.getFileName(), build.getFileChecksum());
        }
    }

    public ListBuildsActivity getParentActivity() {
        if (getActivity() != null) {
            return (ListBuildsActivity) getActivity();
        }

        return null;
    }

    public ApiHelper getApiHelper() {
        if (getParentActivity() != null) {
            return getParentActivity().getApiHelper();
        }

        return null;
    }

    public FilesDownloaderHelper getFilesDownloaderHelper() {
        if (getParentActivity() != null) {
            return getParentActivity().getFilesDownloaderHelper();
        }

        return null;
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

            ((ListBuildsActivity) getActivity()).showBuildDetailsDialog(build.getVersion(), build.getFixes());
        }
    };

    @Override
    public void onStart(int code, String tag) {
        if (!mTagApi.equals(tag)) {
            return;
        }

        if (code == MapApiFunctions.Request.Command.LIST_BUILDS) {
            setRefreshMenuItem(true);

            if (getListView() != null) {
                setListShownNoAnimation(false);
            }
        }
    }

    @Override
    public void onFinish(int code, BaseResponse struct, String tag) {
        if (!mTagApi.equals(tag)) {
            return;
        }

        if (code == MapApiFunctions.Response.Command.LIST_BUILDS) {
            setRefreshMenuItem(false);

            if (getListView() != null) {
                setListShownNoAnimation(true);
            }

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

                Tools.showToast(getParentActivity(), errors.toString(), Toast.LENGTH_LONG);
            }
        }
    }

}
