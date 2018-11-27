package io.edgedev.appextractor;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.util.SortedList;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class AppExtractorFragment extends Fragment implements SearchView.OnQueryTextListener {
    private static final String TAG = "AppExtractorFragment";
    private static final Comparator<AppModel> ALPHABETICAL_COMPARATOR = new Comparator<AppModel>() {
        @Override
        public int compare(AppModel a, AppModel b) {
            return a.getAppName().compareTo(b.getAppName());
        }
    };
    private int count = 0;
    private RecyclerView mRecyclerView;
    private FileSaver mCallback;
    private AppModelAdapter mAdapter;
    private List<AppModel> mAppModels;
    private Intent startupIntent;
    private PackageManager packageManager;
    private List<ResolveInfo> activities;


    public static AppExtractorFragment newInstance() {
        Bundle args = new Bundle();
        AppExtractorFragment fragment = new AppExtractorFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private static List<AppModel> filter(List<AppModel> models, String query) {
        final String lowerCaseQuery = query.toLowerCase();

        final List<AppModel> filteredModelList = new ArrayList<>();
        for (AppModel model : models) {
            final String text = model.getAppName().toLowerCase();
            if (text.contains(lowerCaseQuery)) {
                filteredModelList.add(model);
            }
        }
        return filteredModelList;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mAppModels = new ArrayList<>();
        startupIntent = new Intent(Intent.ACTION_MAIN);
        packageManager = getActivity().getPackageManager();
        activities = packageManager.queryIntentActivities(startupIntent, 0);
    }

   /* void setSysApp() {
        if (activities == null){
            packageManager.queryIntentActivities(startupIntent, 0);
        }
        List<AppModel> appModels = new ArrayList<>();
        for (ResolveInfo resolveInfo : activities) {
            ApplicationInfo applicationInfo = resolveInfo.activityInfo.applicationInfo;
            if ((applicationInfo.flags & (ApplicationInfo.FLAG_UPDATED_SYSTEM_APP | ApplicationInfo.FLAG_SYSTEM)) > 0) {
                AppModel model = new AppModel(
                        count++
                        , resolveInfo.loadLabel(packageManager).toString()
                        , resolveInfo.activityInfo.packageName
                        , resolveInfo.loadIcon(packageManager)
                        , new File(resolveInfo.activityInfo.applicationInfo.publicSourceDir)
                );

                appModels.add(model);
            }
        }

        mAdapter = new AppModelAdapter(ALPHABETICAL_COMPARATOR);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.add(sortAppModelAlphabetically(appModels));
    }
    void setUserApps() {
        if (activities == null){
            packageManager.queryIntentActivities(startupIntent, 0);
        }
        List<AppModel> appModels = new ArrayList<>();
        for (ResolveInfo resolveInfo : activities) {
            ApplicationInfo applicationInfo = resolveInfo.activityInfo.applicationInfo;
            if (!((applicationInfo.flags & (ApplicationInfo.FLAG_UPDATED_SYSTEM_APP | ApplicationInfo.FLAG_SYSTEM)) > 0)) {
                AppModel model = new AppModel(
                        count++
                        , resolveInfo.loadLabel(packageManager).toString()
                        , resolveInfo.activityInfo.packageName
                        , resolveInfo.loadIcon(packageManager)
                        , new File(resolveInfo.activityInfo.applicationInfo.publicSourceDir)
                );

                appModels.add(model);
            }
        }
        mAdapter = new AppModelAdapter(ALPHABETICAL_COMPARATOR);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.add(sortAppModelAlphabetically(appModels));
        List<AppModel> sortAppModelAlphabetically(List<AppModel> appModels) {
        Collections.sort(appModels, new Comparator<AppModel>() {
            public int compare(AppModel a, AppModel b) {
                return String.CASE_INSENSITIVE_ORDER.compare(a.getAppName(), b.getAppName());
            }
        });
        return appModels;
    }
    }*/


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        /*if (id == R.id.rate_app) {
            mCallback.rateApp();
            return true;
        } else if (id== R.id.settings){
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            getActivity().startActivity(intent);
            return true;
        }*/
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.search_apps);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_app_extractor, container, false);
        mRecyclerView = view.findViewById(R.id.apps_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        setupAdapter();
        return view;
    }

    private void setupAdapter() {
        startupIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> activities = packageManager.queryIntentActivities(startupIntent, 0);

        for (ResolveInfo info : activities) {

            AppModel model = new AppModel(
                    count++
                    , info.loadLabel(packageManager).toString()
                    , info.activityInfo.packageName
                    , info.loadIcon(packageManager)
                    , info.activityInfo.applicationInfo.publicSourceDir
            );
            mAppModels.add(model);
        }

        Collections.sort(mAppModels, new Comparator<AppModel>() {
            public int compare(AppModel a, AppModel b) {
                return String.CASE_INSENSITIVE_ORDER.compare(a.getAppName(), b.getAppName());
            }
        });
        mAdapter = new AppModelAdapter(ALPHABETICAL_COMPARATOR);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.add(mAppModels);
        Log.i(TAG, "Found " + activities.size() + " activities.");
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback = (FileSaver) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        final List<AppModel> filteredModelList = filter(mAppModels, query);
        mAdapter.replaceAll(filteredModelList);
        mRecyclerView.scrollToPosition(0);
        return true;
    }

    public interface FileSaver {
        void saveFile(String fileString, View view, String fileName);
        void rateApp();
    }

    private class AppModelAdapter extends RecyclerView.Adapter<AppModelHolder> {
        private final Comparator<AppModel> mComparator;
        private final SortedList<AppModel> mSortedList = new SortedList<>(AppModel.class, new SortedList.Callback<AppModel>() {
            @Override
            public int compare(AppModel a, AppModel b) {
                return mComparator.compare(a, b);
            }

            @Override
            public void onInserted(int position, int count) {
                notifyItemRangeInserted(position, count);
            }

            @Override
            public void onRemoved(int position, int count) {
                notifyItemRangeRemoved(position, count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                notifyItemMoved(fromPosition, toPosition);
            }

            @Override
            public void onChanged(int position, int count) {
                notifyItemRangeChanged(position, count);
            }

            @Override
            public boolean areContentsTheSame(AppModel oldItem, AppModel newItem) {
                return oldItem.equals(newItem);
            }

            @Override
            public boolean areItemsTheSame(AppModel item1, AppModel item2) {
                return item1.getId() == item2.getId();
            }
        });

        public AppModelAdapter(Comparator<AppModel> comparator) {
            mComparator = comparator;

        }

        public void add(AppModel model) {
            mSortedList.add(model);
        }

        public void remove(AppModel model) {
            mSortedList.remove(model);
        }

        public void add(List<AppModel> models) {
            mSortedList.addAll(models);
            notifyDataSetChanged();
        }

        public void remove(List<AppModel> models) {
            mSortedList.beginBatchedUpdates();
            for (AppModel model : models) {
                mSortedList.remove(model);
            }
            mSortedList.endBatchedUpdates();
        }

        public void replaceAll(List<AppModel> models) {
            mSortedList.beginBatchedUpdates();
            for (int i = mSortedList.size() - 1; i >= 0; i--) {
                final AppModel model = mSortedList.get(i);
                if (!models.contains(model)) {
                    mSortedList.remove(model);
                }
            }
            mSortedList.addAll(models);
            mSortedList.endBatchedUpdates();
        }

        @Override
        public AppModelHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View binding = inflater.inflate(R.layout.single_app_layout, parent, false);
            return new AppModelHolder(binding);
        }

        @Override
        public void onBindViewHolder(AppModelHolder holder, int position) {
            AppModel appModel = mSortedList.get(position);
            holder.bindAppModel(appModel);
        }

        @Override
        public int getItemCount() {
            return mSortedList.size();
        }
    }

    private class AppModelHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private AppModel mAppModel;
        private ImageView mAppIcon;
        private ImageView info;
        private LinearLayout mLinearLayout;
        private View mBinding;

        public AppModelHolder(View binding) {
            super(binding);
            mBinding = binding;
            //mAppIcon = binding.appIcon;
            //info = binding.info;
            //mLinearLayout = binding.lLayout;
            mAppIcon.setOnClickListener(this);
            info.setOnClickListener(this);
            mLinearLayout.setOnClickListener(this);
        }

        public void bindAppModel(AppModel appModel) {
            mAppModel = appModel;
            //mBinding.setApp(appModel);

        }

        @Override
        public void onClick(View view) {
            int id = view.getId();
            if (id == R.id.app_icon /*|| id == R.id.l_layout*/) {
                mCallback.saveFile(mAppModel.getFilePathName(), view, mAppModel.getAppName());

            } else if (id == R.id.info) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", mAppModel.getAppPackageName(), null);
                intent.setData(uri);
                getContext().startActivity(intent);
            }
        }
    }
}
